 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.http.MediaType;
 
 import wad.spring.config.WebAppContext;
 import wad.spring.domain.Viite;
 import wad.spring.service.ViiteService;
 
 import static org.mockito.Mockito.*;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = {TestContext.class, WebAppContext.class})
 @WebAppConfiguration
 public class ViiteControllerTest {
 
     private MockMvc mockMvc;
     private List<Viite> viitteet; 
 
     @Autowired
     private ViiteService viiteServiceMock;
 
     @Autowired
     private WebApplicationContext webApplicationContext;
 
     @Before
     public void setUp() {
         //We have to reset our mock between tests because the mock objects
         //are managed by the Spring container. If we would not reset them,
         //stubbing and verified behavior would "leak" from one test to another.
         Mockito.reset(viiteServiceMock);
 
         mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
         
         String [] authors = {"King, Stephen", "Vihavainen, Arto", "Ankka, Aku"};
         String [] bookTitles = {"The Shining", "Webpalvelinohjelmointi", "Elämäni epäonnistumiset"};
         String [] itemYears = {"1977", "2013", "2001"};
         String [] titles = {"Chapter 1", "joku osa", "Lapsuuteni"};
         
         viitteet = new ArrayList<Viite>();
         
         for (int i = 0; i < authors.length; i++) {
             Viite viite = new Viite();
             viite.setId(new Long(i));
             viite.setAuthor(authors[i]);
             viite.setBookTitle(bookTitles[i]);
             viite.setItemYear(itemYears[i]);
             viite.setTitle(titles[i]);
             viitteet.add(viite);
         }
     }
 
     @Test
     public void testRoot() throws Exception {
         mockMvc.perform(get("/")).
         andExpect(status().isOk()); 
     }
     
     @Test
     public void viiteControllerListaaViitteetTest() throws Exception {
   
         when(viiteServiceMock.list()).thenReturn(Arrays.asList(viitteet.get(0), viitteet.get(1)));
 
         mockMvc.perform(get("/listaaviitteet.do"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType("application/json;charset=UTF-8"))
                 .andExpect(jsonPath("$.").isArray())
                 
                 //Test first item (viite) in JSON response
                 .andExpect(jsonPath("$..id[0]").value(viitteet.get(0).getId().intValue()))
                 .andExpect(jsonPath("$..author[0]").value(viitteet.get(0).getAuthor()))
                 .andExpect(jsonPath("$..bookTitle[0]").value(viitteet.get(0).getBookTitle()))
                 .andExpect(jsonPath("$..title[0]").value(viitteet.get(0).getTitle()))
                 
                 //Test second item (viite) in JSON response
                 .andExpect(jsonPath("$..id[1]").value(viitteet.get(1).getId().intValue()))
                 .andExpect(jsonPath("$..author[1]").value(viitteet.get(1).getAuthor()))
                 .andExpect(jsonPath("$..bookTitle[1]").value(viitteet.get(1).getBookTitle()))
                 .andExpect(jsonPath("$..title[1]").value(viitteet.get(1).getTitle()))
                 
                // We deliberately do not test the third item in JSON response as it should not be there
/*
                 //Test third item (viite) in JSON response
                 .andExpect(jsonPath("$..id[2]").value(viitteet.get(1).getId().intValue()))
                 .andExpect(jsonPath("$..author[2]").value(viitteet.get(1).getAuthor()))
                 .andExpect(jsonPath("$..bookTitle[2]").value(viitteet.get(1).getBookTitle()))
                 .andExpect(jsonPath("$..title[2]").value(viitteet.get(1).getTitle()))
*/                
                 .andExpect(jsonPath("$..id[3]").doesNotExist())
                 ;
 
         verify(viiteServiceMock, times(1)).list();
         verifyNoMoreInteractions(viiteServiceMock);
     }
 
     
     
      @Test
      public void viiteControllerLisaaViiteTest() throws Exception {
         String createJson = "{\"id\":%d,\"author\":\"%s\",\"title\":\"%s\",\"bookTitle\":\"%s\"}";
 
         mockMvc.perform(post("/lisaaviite.do")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(String.format(createJson, viitteet.get(0).getId(),
                                                    viitteet.get(0).getAuthor(),
                                                    viitteet.get(0).getTitle(),
                                                    viitteet.get(0).getBookTitle())
                 .getBytes())
  
         )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.").exists())
                .andExpect(jsonPath("$..id[0]").value(viitteet.get(0).getId().intValue()))
                .andExpect(jsonPath("$..author[0]").value(viitteet.get(0).getAuthor()))
                .andExpect(jsonPath("$..bookTitle[0]").value(viitteet.get(0).getBookTitle()))
                .andExpect(jsonPath("$..title[0]").value(viitteet.get(0).getTitle()));
         
         verify(viiteServiceMock, times(0)).list();
         //verifyNoMoreInteractions(viiteServiceMock);
         
      } 
 }
