 package com.hrambelo.codestory.web.controller;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.http.MediaType;
 import org.springframework.test.web.servlet.MockMvc;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bensmania <a href="mailto:bensmania@gmail.com">hrambelo</a>
  * Date: 30/01/13 - Time: 15:59
  * Package: com.hrambelo.codestory.web.controller
  * Codestory
  */
 public class CodestoryControllerTest {
 
     private MockMvc mockMvc;
 
     @Before
     public void setUp() {
         CodestoryController codestoryController = new CodestoryController();
         mockMvc = standaloneSetup(codestoryController)
                 .build();
     }
 
     @Ignore
     @Test
     public void testHome() throws Exception {
         mockMvc.perform(get("/"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(content().string("go and see <a href=\"http://code-story.net/\">codestory 2013</a>"));
     }
 
     @Test
     public void testShowEmail() throws Exception {
         mockMvc.perform(get("/").param("q", "email"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(content().string("bensmania@gmail.com"));
     }
 
     @Test
     public void testSayYes() throws Exception {
         mockMvc.perform(get("/").param("q", "heureux"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(content().string("OUI"));
     }
 
     @Test
     public void shouldNotPost() throws Exception {
         mockMvc
                .perform(post("/").param("q", "sample"))
                 .andExpect(status().isMethodNotAllowed())
                 .andExpect(header()
                         .string("Allow", is("GET")));
     }
 
     @Test
     public void printInfo() throws Exception {
         mockMvc
                 .perform(get("/?q=email"))
                 .andDo(print());
 
     }
 }
