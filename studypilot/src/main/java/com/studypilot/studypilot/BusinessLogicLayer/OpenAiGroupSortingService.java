package com.studypilot.studypilot.BusinessLogicLayer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.GroupFormationSkillOption;
import com.studypilot.studypilot.DomainModel.GroupFormationTopicOption;
import com.studypilot.studypilot.DomainModel.StudentGroupPreference;
import com.studypilot.studypilot.DomainModel.User;

@Service
public class OpenAiGroupSortingService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key:}")
    private String apiKey;

    private final HttpClient httpClient;

    public OpenAiGroupSortingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public List<GroupAssignment> sortStudents(
            GroupFormationActivity activity,
            List<GroupFormationTopicOption> topicOptions,
            List<GroupFormationSkillOption> skillOptions,
            List<StudentGroupPreference> preferences,
            List<User> enrolledStudents) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured. Set openai.api.key in application.properties.");
        }

        String prompt = buildPrompt(activity, topicOptions, skillOptions, preferences, enrolledStudents);

        String aiResponse = callOpenAi(prompt);

        return parseResponse(aiResponse, enrolledStudents);
    }

    private String buildPrompt(
            GroupFormationActivity activity,
            List<GroupFormationTopicOption> topicOptions,
            List<GroupFormationSkillOption> skillOptions,
            List<StudentGroupPreference> preferences,
            List<User> enrolledStudents) {

        Map<Long, User> userById = enrolledStudents.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, StudentGroupPreference> prefByStudentId = preferences.stream()
                .collect(Collectors.toMap(StudentGroupPreference::getStudentId, p -> p));

        StringBuilder sb = new StringBuilder();
        sb.append("You are a group formation assistant for a university course.\n\n");
        sb.append("TASK: Sort students into balanced groups based on their survey responses.\n\n");

        sb.append("ACTIVITY: ").append(activity.getActivityName()).append("\n");
        sb.append("Preferred group size: ").append(activity.getPreferredGroupSize()).append("\n");
        sb.append("Minimum team size: ").append(activity.getMinTeamSize()).append("\n");
        sb.append("Maximum team size: ").append(activity.getMaxTeamSize()).append("\n");
        sb.append("Topic matching: ").append(activity.isGroupTopicsSimilarly()
                ? "Group students with SIMILAR topic interests together"
                : "Group students with DIFFERENT topic interests together (diverse)").append("\n");
        sb.append("Skill matching: ").append(activity.isGroupSkillsSimilarly()
                ? "Group students with SIMILAR skills together"
                : "Group students with DIFFERENT skills together (complementary)").append("\n\n");

        sb.append("AVAILABLE TOPICS: ");
        sb.append(topicOptions.stream().map(GroupFormationTopicOption::getTopicText).collect(Collectors.joining(", ")));
        sb.append("\n");

        sb.append("AVAILABLE SKILLS: ");
        sb.append(skillOptions.stream().map(GroupFormationSkillOption::getSkillText).collect(Collectors.joining(", ")));
        sb.append("\n\n");

        sb.append("STUDENTS AND THEIR PREFERENCES:\n");
        for (User student : enrolledStudents) {
            StudentGroupPreference pref = prefByStudentId.get(student.getId());
            sb.append("- Student ID: ").append(student.getId());
            sb.append(", Name: ").append(student.getFullName());
            if (pref != null) {
                sb.append(", Topic: ").append(pref.getTopicChoice());
                sb.append(", Skill: ").append(pref.getSkillChoice());
                if (pref.getAvailabilitySlots() != null && !pref.getAvailabilitySlots().isBlank()) {
                    sb.append(", Availability: ").append(pref.getAvailabilitySlots());
                }
                if (pref.getNotes() != null && !pref.getNotes().isBlank()) {
                    sb.append(", Notes: ").append(pref.getNotes());
                }
            } else {
                sb.append(", (did not submit survey)");
            }
            sb.append("\n");
        }

        sb.append("\nINSTRUCTIONS:\n");
        sb.append("1. Create groups respecting the min/max team size constraints.\n");
        sb.append("2. Target the preferred group size when possible.\n");
        sb.append("3. Every student must be assigned to exactly one group.\n");
        sb.append("4. Students who did not submit the survey should still be placed in groups.\n");
        sb.append("5. Follow the topic and skill matching preferences specified above.\n");
        sb.append("6. Use simple group names: 'Group 1', 'Group 2', etc.\n");
        sb.append("7. Consider students' availability when forming groups. Try to group students who share common available time slots so they can meet.\n\n");

        sb.append("RESPONSE FORMAT: Return ONLY a valid JSON array. No markdown, no code fences, no explanation.\n");
        sb.append("Each element must have: \"groupNumber\" (int starting at 1), \"groupName\" (string), ");
        sb.append("\"studentIds\" (array of student ID numbers).\n");
        sb.append("Example: [{\"groupNumber\":1,\"groupName\":\"Group 1\",\"studentIds\":[1,2,3]},");
        sb.append("{\"groupNumber\":2,\"groupName\":\"Group 2\",\"studentIds\":[4,5,6]}]\n");

        return sb.toString();
    }

    private String callOpenAi(String prompt) {
        String requestBody = buildRequestJson(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("OpenAI API returned status " + response.statusCode() + ": " + response.body());
            }

            return extractContent(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OpenAI API call was interrupted.", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }

    private String buildRequestJson(String prompt) {
        String escapedPrompt = escapeJson(prompt);
        return "{" +
                "\"model\":\"gpt-4o-mini\"," +
                "\"temperature\":0.3," +
                "\"max_tokens\":4096," +
                "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}]" +
                "}";
    }

    private String extractContent(String responseBody) {
        int contentStart = responseBody.indexOf("\"content\":");
        if (contentStart == -1) {
            throw new RuntimeException("Could not parse OpenAI response: no content field found.");
        }

        int valueStart = responseBody.indexOf("\"", contentStart + 10) + 1;
        int valueEnd = findClosingQuote(responseBody, valueStart);

        String raw = responseBody.substring(valueStart, valueEnd);
        return unescapeJson(raw);
    }

    private int findClosingQuote(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '\\') {
                i++;
            } else if (s.charAt(i) == '"') {
                return i;
            }
        }
        return s.length();
    }

    private List<GroupAssignment> parseResponse(String content, List<User> enrolledStudents) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }

        List<GroupAssignment> assignments = new ArrayList<>();

        int arrayStart = trimmed.indexOf('[');
        int arrayEnd = trimmed.lastIndexOf(']');
        if (arrayStart == -1 || arrayEnd == -1) {
            throw new RuntimeException("AI response did not contain a valid JSON array.");
        }

        String arrayContent = trimmed.substring(arrayStart + 1, arrayEnd);

        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    objStart = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart != -1) {
                    String obj = arrayContent.substring(objStart, i + 1);
                    assignments.add(parseGroupObject(obj));
                    objStart = -1;
                }
            }
        }

        if (assignments.isEmpty()) {
            throw new RuntimeException("AI response contained no group assignments.");
        }

        return assignments;
    }

    private GroupAssignment parseGroupObject(String json) {
        int groupNumber = extractInt(json, "groupNumber");
        String groupName = extractString(json, "groupName");
        List<Long> studentIds = extractLongArray(json, "studentIds");

        return new GroupAssignment(groupNumber, groupName, studentIds);
    }

    private int extractInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex == -1) {
            return 0;
        }
        int colonIndex = json.indexOf(':', keyIndex + pattern.length());
        int start = colonIndex + 1;
        while (start < json.length() && !Character.isDigit(json.charAt(start)) && json.charAt(start) != '-') {
            start++;
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex == -1) {
            return "Group";
        }
        int colonIndex = json.indexOf(':', keyIndex + pattern.length());
        int quoteStart = json.indexOf('"', colonIndex + 1) + 1;
        int quoteEnd = findClosingQuote(json, quoteStart);
        return unescapeJson(json.substring(quoteStart, quoteEnd));
    }

    private List<Long> extractLongArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex == -1) {
            return List.of();
        }
        int bracketStart = json.indexOf('[', keyIndex);
        int bracketEnd = json.indexOf(']', bracketStart);
        String arrayStr = json.substring(bracketStart + 1, bracketEnd);

        List<Long> ids = new ArrayList<>();
        for (String part : arrayStr.split(",")) {
            String trimmedPart = part.trim();
            if (!trimmedPart.isEmpty()) {
                ids.add(Long.parseLong(trimmedPart));
            }
        }
        return ids;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public record GroupAssignment(
            int groupNumber,
            String groupName,
            List<Long> studentIds) {
    }
}
