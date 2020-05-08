 package org.sukrupa.bigneeds;
 
 import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
 import org.eclipse.jetty.io.View;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.sukrupa.app.needs.BigNeedsController;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertFalse;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 
 public class BigNeedsControllerTest {
 
     private BigNeedsController controller;
 
     private HashMap<String, Object> model = new HashMap<String, Object>();
 
     @Mock
     private BigNeedRepository bigNeedRepository;
 
     @Before
     public void setUp() {
         initMocks(this);
         controller = new BigNeedsController(bigNeedRepository);
     }
 
     @Test
     public void shouldDisplayBigNeedsPage() {
        assertThat(controller.list(model), is("bigNeeds/list"));
     }
 
     @Test
     public void shouldRetrieveBigNeedListToModel() {
         BigNeed schoolBusBigNeed = new BigNeed("School Bus", 200000);
         BigNeed waterPurifierBigNeed = new BigNeed("Water Purifier", 5000);
         List<BigNeed> ourList = new ArrayList<BigNeed>();
         ourList.add(schoolBusBigNeed);
         ourList.add(waterPurifierBigNeed);
         when(bigNeedRepository.getList()).thenReturn(ourList);
         controller.list(model);
         List<BigNeed> bigNeedList = (List<BigNeed>) model.get("bigNeedList");
         assertThat(bigNeedList.contains(schoolBusBigNeed), is(true));
         assertThat(bigNeedList.contains(waterPurifierBigNeed), is(true));
     }
 
     @Test
     public void shouldCreateABigNeed() {
         ArgumentCaptor<BigNeed> bigNeedCaptor = ArgumentCaptor.forClass(BigNeed.class);
 
         controller.create("sample", "60000", model);
 
         assertThat((String) model.get("message"), is("Added Successfully"));
         verify(bigNeedRepository).put(bigNeedCaptor.capture());
         assertThat(bigNeedCaptor.getValue().getItemName(), is("sample"));
         assertThat(bigNeedCaptor.getValue().getCost(), is(60000));
     }
 
 }
