package com.studypilot.studypilot.BusinessLogicLayer;

import com.studypilot.studypilot.DataAccessLayer.QuizQuestionRepo;
import com.studypilot.studypilot.DataAccessLayer.QuizSubmissionAnswerRepo;
import com.studypilot.studypilot.DataAccessLayer.QuizSubmissionRepo;
import com.studypilot.studypilot.DataAccessLayer.QuizTestRepo;
import com.studypilot.studypilot.DomainModel.QuizQuestion;
import com.studypilot.studypilot.DomainModel.QuizSubmission;
import com.studypilot.studypilot.DomainModel.QuizSubmissionAnswer;
import com.studypilot.studypilot.DomainModel.QuizTest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizTestRepo quizTestRepo;
    private final QuizQuestionRepo quizQuestionRepo;
    private final QuizSubmissionRepo quizSubmissionRepo;
    private final QuizSubmissionAnswerRepo quizSubmissionAnswerRepo;

    public QuizService(QuizTestRepo quizTestRepo,
            QuizQuestionRepo quizQuestionRepo,
            QuizSubmissionRepo quizSubmissionRepo,
            QuizSubmissionAnswerRepo quizSubmissionAnswerRepo) {
        this.quizTestRepo = quizTestRepo;
        this.quizQuestionRepo = quizQuestionRepo;
        this.quizSubmissionRepo = quizSubmissionRepo;
        this.quizSubmissionAnswerRepo = quizSubmissionAnswerRepo;
    }

    @Transactional
    public QuizTest createQuizFromUpload(Long professorId, String courseId, String sourceFileName) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course is required.");
        }

        String safeFileName = sourceFileName == null ? "uploaded-document.pdf" : sourceFileName.trim();
        if (safeFileName.isBlank()) {
            safeFileName = "uploaded-document.pdf";
        }

        QuizTest quizTest = new QuizTest(
                courseId,
                professorId,
                "Auto Quiz: " + safeFileName,
                safeFileName
        );
        QuizTest savedQuiz = quizTestRepo.save(quizTest);

        List<QuizQuestion> questions = buildDemoQuestions(savedQuiz.getId(), safeFileName);
        quizQuestionRepo.saveAll(questions);
        return savedQuiz;
    }

    public Optional<QuizTest> getLatestQuizForCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return Optional.empty();
        }
        return quizTestRepo.findTopByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public List<QuizQuestion> getQuestionsForQuiz(Long quizTestId) {
        if (quizTestId == null) {
            return List.of();
        }
        return quizQuestionRepo.findByQuizTestIdOrderByQuestionOrderAsc(quizTestId);
    }

    public Optional<QuizSubmission> getLatestSubmission(Long quizTestId, Long studentId) {
        if (quizTestId == null || studentId == null) {
            return Optional.empty();
        }
        return quizSubmissionRepo.findTopByQuizTestIdAndStudentIdOrderBySubmittedAtDesc(quizTestId, studentId);
    }

    @Transactional
    public QuizResult submitQuiz(Long quizTestId,
            String courseId,
            Long studentId,
            Map<Long, String> selectedOptionsByQuestionId) {
        if (quizTestId == null) {
            throw new IllegalArgumentException("Quiz ID is missing.");
        }
        if (studentId == null) {
            throw new IllegalArgumentException("Student must be logged in.");
        }

        QuizTest quizTest = quizTestRepo.findById(quizTestId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found."));

        if (!quizTest.getCourseId().equals(courseId)) {
            throw new IllegalArgumentException("Quiz does not belong to this course.");
        }

        List<QuizQuestion> questions = quizQuestionRepo.findByQuizTestIdOrderByQuestionOrderAsc(quizTestId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz has no questions yet.");
        }

        int score = 0;
        QuizSubmission submission = quizSubmissionRepo.save(new QuizSubmission(quizTestId, courseId, studentId, 0, questions.size()));

        List<QuizSubmissionAnswer> answersToSave = new ArrayList<>();
        Map<Long, String> normalizedSelection = selectedOptionsByQuestionId == null
                ? Map.of()
                : selectedOptionsByQuestionId;

        for (QuizQuestion question : questions) {
            String selected = normalizeOption(normalizedSelection.get(question.getId()));
            boolean isCorrect = question.getCorrectOption().equalsIgnoreCase(selected);
            if (isCorrect) {
                score++;
            }
            answersToSave.add(new QuizSubmissionAnswer(
                    submission.getId(),
                    question.getId(),
                    selected,
                    isCorrect
            ));
        }

        quizSubmissionAnswerRepo.saveAll(answersToSave);

        submission.setScore(score);
        submission.setTotalQuestions(questions.size());
        QuizSubmission scored = quizSubmissionRepo.save(submission);

        return new QuizResult(scored, questions, answersToSaveAsMap(answersToSave));
    }

    public List<QuizAnswerReview> buildAnswerReview(QuizSubmission submission) {
        List<QuizSubmissionAnswer> answerRows = quizSubmissionAnswerRepo.findBySubmissionId(submission.getId());
        Map<Long, QuizSubmissionAnswer> answerByQuestion = new LinkedHashMap<>();
        for (QuizSubmissionAnswer answer : answerRows) {
            answerByQuestion.put(answer.getQuestionId(), answer);
        }

        List<QuizQuestion> questions = quizQuestionRepo.findByQuizTestIdOrderByQuestionOrderAsc(submission.getQuizTestId());
        List<QuizAnswerReview> reviewRows = new ArrayList<>();
        for (QuizQuestion question : questions) {
            QuizSubmissionAnswer answer = answerByQuestion.get(question.getId());
            reviewRows.add(new QuizAnswerReview(
                    question,
                    answer == null ? "" : answer.getSelectedOption(),
                    answer != null && answer.isCorrect()
            ));
        }
        return reviewRows;
    }

    private Map<Long, String> answersToSaveAsMap(List<QuizSubmissionAnswer> answers) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (QuizSubmissionAnswer answer : answers) {
            map.put(answer.getQuestionId(), answer.getSelectedOption());
        }
        return map;
    }

    private String normalizeOption(String rawOption) {
        if (rawOption == null) {
            return "";
        }
        String option = rawOption.trim().toUpperCase(Locale.ROOT);
        if (!option.equals("A") && !option.equals("B") && !option.equals("C") && !option.equals("D")) {
            return "";
        }
        return option;
    }

    private List<QuizQuestion> buildDemoQuestions(Long quizId, String sourceFileName) {
        return List.of(
                new QuizQuestion(
                        quizId,
                        1,
                        "What is the primary topic emphasized in " + sourceFileName + "?",
                        "Core course concepts",
                        "Campus parking rules",
                        "Dormitory maintenance",
                        "Meal plan pricing",
                        "A"
                ),
                new QuizQuestion(
                        quizId,
                        2,
                        "Which approach best supports long-term software maintainability?",
                        "Ignoring documentation",
                        "Using clear abstractions and tests",
                        "Duplicating logic to move faster",
                        "Avoiding code reviews",
                        "B"
                ),
                new QuizQuestion(
                        quizId,
                        3,
                        "In a team project, what should be done first when requirements are unclear?",
                        "Implement random features",
                        "Delay the project indefinitely",
                        "Clarify assumptions with stakeholders",
                        "Skip planning",
                        "C"
                ),
                new QuizQuestion(
                        quizId,
                        4,
                        "What is generally a good indicator of quality in student code submissions?",
                        "Consistent structure and meaningful tests",
                        "Very long methods",
                        "No comments and no naming conventions",
                        "Only passing on one machine",
                        "A"
                ),
                new QuizQuestion(
                        quizId,
                        5,
                        "Which behavior is most aligned with effective collaboration?",
                        "Keeping all work private until deadline",
                        "Sharing progress and blockers early",
                        "Avoiding issue tracking",
                        "Skipping peer feedback",
                        "B"
                )
        );
    }

    public record QuizResult(QuizSubmission submission, List<QuizQuestion> questions, Map<Long, String> selectedOptions) {

    }

    public record QuizAnswerReview(QuizQuestion question, String selectedOption, boolean correct) {

    }
}
