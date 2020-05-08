 package org.sukrupa.smallneeds;
 
 

 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.sukrupa.app.needs.SmallNeedsController;
 import org.sukrupa.bigneeds.BigNeed;
 import org.sukrupa.smallNeeds.SmallNeedRepository;
 
 import java.util.HashMap;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class SmallNeedsControllerTest {
 
     @Mock
     SmallNeedRepository smallNeedRepository;
     private HashMap<String, Object> studentModel = new HashMap<String, Object>();
     public SmallNeedsController controller ;
 
     private HashMap<String,Object> model= new HashMap<String, Object>();
 
     @Before
     public void setUp(){
     initMocks(this);
     controller = new SmallNeedsController(smallNeedRepository);
     }
 
     @Test
     public void shouldDisplaySmallNeedsPage(){
       controller= new SmallNeedsController();
       assertEquals(controller.list(model), "smallNeeds/list");
     }
 
     @Test
     public void shouldAddSmallNeed() {
          controller= new SmallNeedsController();
          ArgumentCaptor<BigNeed> bigNeedCaptor = ArgumentCaptor.forClass(BigNeed.class);
          String view= controller.create("1","SchoolUniform",5000L,"For Aarthi",model);
          assertThat((String) model.get("message"), is("Added Successfully"));
     }
 }
