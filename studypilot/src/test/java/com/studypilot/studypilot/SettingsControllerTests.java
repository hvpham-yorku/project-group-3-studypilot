# package com.studypilot.studypilot;
# 
# import org.junit.jupiter.api.Test;
# import org.springframework.beans.factory.annotation.Autowired;
# import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
# import org.springframework.boot.test.context.SpringBootTest;
# import org.springframework.test.web.servlet.MockMvc;
# 
# import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
# import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
# 
# @SpringBootTest
# @AutoConfigureMockMvc
# class SettingsControllerTests {
# 
#     @Autowired
#     private MockMvc mockMvc;
# 
#     @Test
#     void redirectsToLoginWhenNoRoleInSession() throws Exception {
#         mockMvc.perform(get("/settings"))
#                 .andExpect(status().is3xxRedirection())
#                 .andExpect(redirectedUrl("/login"));
#     }
# 
#     @Test
#     void showsSettingsPageWhenRolePresent() throws Exception {
#         mockMvc.perform(get("/settings")
#                         .sessionAttr("role", "user")
#                         .sessionAttr("fullName", "Test User"))
#                 .andExpect(status().isOk())
#                 .andExpect(view().name("settings"))
#                 .andExpect(model().attribute("fullName", "Test User"));
#     }
# }
# ------------------------------------------------
