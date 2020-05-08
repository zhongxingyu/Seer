 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.codehaus.jackson.map.ObjectMapper;
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
 import wad.spring.domain.ViiteItem;
 
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = {TestContext.class, WebAppContext.class})
 @WebAppConfiguration
 public class ViiteControllerTest {
 
     private MockMvc mockMvc;
     
     private List<Viite> viitteet; 
     Viite viite1, viite2, viite3, viite4;
 
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
 
         this.viitteet = new ArrayList<Viite>();
        
         viite1 = new Viite();
         viite1.setId(0L);
         viite1.setReferenceType("book");
         viite1.setReferenceId("HS01");
         viite1.setItems(new ArrayList<ViiteItem>());
         viite1.addItem("author/editor","Hawking, Stephen");
         viite1.addItem("title", "A Brief History of Time");
         viite1.addItem("publisher","Bantam Dell Publishing Group");
         viite1.addItem("year", "1988");
         this.viitteet.add(viite1);
         
         viite2 = new Viite();
         viite2.setId(2L);
         viite2.setReferenceType("inproceedings");
         viite2.setReferenceId("KSEP");
         viite2.setItems(new ArrayList<ViiteItem>());
         viite2.addItem("author","Kääriäinen, Seppo");
         viite2.addItem("title", "Kepulaisuuden ähkyt ökyrikkaat");
         viite2.addItem("booktitle","Ääliöt ja Örkit");
         viite2.addItem("year", "2013");
         this.viitteet.add(viite2);
         
         viite3 = new Viite();
         viite3.setId(3L);
         viite3.setReferenceType("article");
         viite3.setReferenceId("FEYR");
         viite3.setItems(new ArrayList<ViiteItem>());
         viite3.addItem("author","Feynman, Richard");
         viite3.addItem("title", "Quantum Physics for Dummies");
         viite3.addItem("journal","The Dummy things for Dummies");
         viite3.addItem("year", "2991");
         this.viitteet.add(viite3);
 
         viite4 = new Viite();
         viite4.setId(4L);
         viite4.setReferenceType("misc");
         viite4.setReferenceId("DFJ1");
         viite4.setItems(new ArrayList<ViiteItem>());
         viite4.addItem("author","Doesntfit, James");
         viite4.addItem("title", "The misfit's reflection");
         viite4.addItem("howpublished","it was a miracle believe me");
         viite4.addItem("month","oct");
         viite4.addItem("year", "1982");
         viite4.addItem("note","remove this before publication you doughnut!");
         viite4.addItem("key", "forgot mine at home");
         this.viitteet.add(viite4);
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
                 
                 //Test first item in JSON response. Should be a "book" kind of viite
                 .andExpect(jsonPath("$..id[0]").value(viitteet.get(0).getId().intValue()))
                .andExpect(jsonPath("$..viiteType[0]").value(viitteet.get(0).getReferenceType()))
                 .andExpect(jsonPath("$..referenceId[0]").value(viitteet.get(0).getReferenceId()))
                 .andExpect(jsonPath("$..items[0]..fieldName[0]").value("author/editor"))
                 .andExpect(jsonPath("$..items[0]..fieldValue[0]").value(viitteet.get(0).getItemValueWithFieldName("author/editor")))
                 .andExpect(jsonPath("$..items[1]..fieldName[0]").value("title"))
                 .andExpect(jsonPath("$..items[1]..fieldValue[0]").value(viitteet.get(0).getItemValueWithFieldName("title")))
                 .andExpect(jsonPath("$..items[2]..fieldName[0]").value("publisher"))
                 .andExpect(jsonPath("$..items[2]..fieldValue[0]").value(viitteet.get(0).getItemValueWithFieldName("publisher")))
                 .andExpect(jsonPath("$..items[3]..fieldName[0]").value("year"))
                 .andExpect(jsonPath("$..items[3]..fieldValue[0]").value(viitteet.get(0).getItemValueWithFieldName("year")))
                 
                 //Test second item in JSON response. should be an "inproceedings" kind of viite
                 .andExpect(jsonPath("$..id[1]").value(viitteet.get(1).getId().intValue()))
                .andExpect(jsonPath("$..viiteType[1]").value(viitteet.get(1).getReferenceType()))
                 .andExpect(jsonPath("$..referenceId[1]").value(viitteet.get(1).getReferenceId()))
                 .andExpect(jsonPath("$..items[4]..fieldName[0]").value("author"))
                 .andExpect(jsonPath("$..items[4]..fieldValue[0]").value(viitteet.get(1).getItemValueWithFieldName("author")))
                 .andExpect(jsonPath("$..items[5]..fieldName[0]").value("title"))
                 .andExpect(jsonPath("$..items[5]..fieldValue[0]").value(viitteet.get(1).getItemValueWithFieldName("title")))
                 .andExpect(jsonPath("$..items[6]..fieldName[0]").value("booktitle"))
                 .andExpect(jsonPath("$..items[6]..fieldValue[0]").value(viitteet.get(1).getItemValueWithFieldName("booktitle")))
                 .andExpect(jsonPath("$..items[7]..fieldName[0]").value("year"))
                 .andExpect(jsonPath("$..items[7]..fieldValue[0]").value(viitteet.get(1).getItemValueWithFieldName("year")))
                 
                 .andExpect(jsonPath("$..id[2]").doesNotExist())
                 ;
 
         verify(viiteServiceMock, times(1)).list();
         verifyNoMoreInteractions(viiteServiceMock);
 
 }
     
      @Test
      public void viiteControllerLisaaViiteTest() throws Exception {
          
          when(viiteServiceMock.create(any(Viite.class))).thenReturn(viite1);
          
          ObjectMapper mapper = new ObjectMapper();
          String jsonViite = mapper.writeValueAsString(viitteet.get(0));
 
         mockMvc.perform(post("/lisaaviite.do")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(jsonViite.getBytes())
         )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.").exists())
                .andExpect(jsonPath("$..id[0]").exists())
                .andExpect(jsonPath("$..id[1]").doesNotExist())
                .andExpect(jsonPath("$..id[0]").value(viite1.getId().intValue()))
                .andExpect(jsonPath("$..referenceId[0]").value(viite1.getReferenceId()))
               .andExpect(jsonPath("$..viiteType[0]").value(viite1.getReferenceType()))
                .andExpect(jsonPath("$..items[0]..fieldName[0]").value("author/editor"))
                .andExpect(jsonPath("$..items[0]..fieldValue[0]").value(viite1.getItemValueWithFieldName("author/editor")))
                .andExpect(jsonPath("$..items[1]..fieldName[0]").value("title"))
                .andExpect(jsonPath("$..items[1]..fieldValue[0]").value(viite1.getItemValueWithFieldName("title")))
                .andExpect(jsonPath("$..items[2]..fieldName[0]").value("publisher"))
                .andExpect(jsonPath("$..items[2]..fieldValue[0]").value(viite1.getItemValueWithFieldName("publisher")))
                .andExpect(jsonPath("$..items[3]..fieldName[0]").value("year"))
                .andExpect(jsonPath("$..items[3]..fieldValue[0]").value(viite1.getItemValueWithFieldName("year")));
                
         verify(viiteServiceMock, times(1)).create(any(Viite.class));
         verify(viiteServiceMock, times(0)).list();
         verifyNoMoreInteractions(viiteServiceMock);
      
      }
     
      @Test
      public void viiteControllerPoistaViiteWhenItIsInDatabaseTest() throws Exception {
          when(viiteServiceMock.exists(any(Long.class))).thenReturn(true);
          //when(viiteServiceMock.delete(any(Long.class))).thenReturn(Long.class);
          
          ObjectMapper mapper = new ObjectMapper();
          String jsonViite = mapper.writeValueAsString(viitteet.get(0));
 
         mockMvc.perform(post("/poistaviite.do")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(jsonViite.getBytes())
         )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.").exists())
                .andExpect(jsonPath("$..id[0]").exists())
                .andExpect(jsonPath("$..id[1]").doesNotExist())
                .andExpect(jsonPath("$..id[0]").value(-1));
 
   
         verify(viiteServiceMock, times(1)).delete(viite1.getId());
         verify(viiteServiceMock, times(1)).exists(any(Long.class));
         verify(viiteServiceMock, times(0)).findOne(any(Long.class));
         verify(viiteServiceMock, times(0)).list();
         verifyNoMoreInteractions(viiteServiceMock);
      
      }
      
      @Test
      public void viiteControllerPoistaViiteWhenItIsNotInDatabaseTest() throws Exception {
          when(viiteServiceMock.exists(any(Long.class))).thenReturn(false);
          
          ObjectMapper mapper = new ObjectMapper();
          String jsonViite = mapper.writeValueAsString(viitteet.get(0));
 
         mockMvc.perform(post("/poistaviite.do")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content(jsonViite.getBytes())
         )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.").exists())
                .andExpect(jsonPath("$..id[0]").exists())
                .andExpect(jsonPath("$..id[1]").doesNotExist())
                .andExpect(jsonPath("$..id[0]").value(viitteet.get(0).getId().intValue()));
 
   
         verify(viiteServiceMock, times(0)).delete(viite1.getId());
         verify(viiteServiceMock, times(1)).exists(any(Long.class));
         verify(viiteServiceMock, times(0)).findOne(any(Long.class));
         verify(viiteServiceMock, times(0)).list();
         verifyNoMoreInteractions(viiteServiceMock);
      
      }
 
      @Test
     public void viiteControllerLataaBiBTexTest() throws Exception {
         String bibtex = "";
         for (Viite v : viitteet) {
             bibtex += v.toStringBiBTex() + "\n";
         }
         
         when(viiteServiceMock.listAllBiBTeX()).thenReturn(bibtex);
 
         String name = "";
          
         mockMvc.perform(get("/lataaBibtext.do").param("name", name))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string(bibtex));
 
   
         verify(viiteServiceMock, times(0)).delete(viite1.getId());
         verify(viiteServiceMock, times(0)).exists(any(Long.class));
         verify(viiteServiceMock, times(0)).findOne(any(Long.class));
         verify(viiteServiceMock, times(1)).listAllBiBTeX();
         verifyNoMoreInteractions(viiteServiceMock); 
      }
 }
