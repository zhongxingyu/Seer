 package com.github.imaman.selector;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import org.junit.Ignore;
 import org.junit.Test;
 
 import com.github.imaman.selector.external.Response;
 import com.github.imaman.selector.external.Tracker;
 
 public class Selector_Test {
 
   private final Tracker tracker = mock(Tracker.class);
 
   private Response newResponse(String name, long age) {
     Response response = mock(Response.class, name);
     when(response.lastUpdatedAt()).thenReturn(age);
     return response;
   }
 
   private Response newResponse(String name, long age, Label label) {
     Response response = newResponse(name, age);
     when(response.label()).thenReturn(label);
     return response;
   }
 
   @Test
   public void filtersNothingByDefault() {
     List<Response> responses = new ArrayList<Response>();
     responses.add(newResponse("old", 900));
     responses.add(newResponse("older", 1));
 
     Selector selector = new Selector();
     List<Response> selected = selector.select(new Request(1500L, ""), responses, tracker);
 
     assertEquals(responses, selected);
   }
 
   @Test
   public void rejectsResponsesWhoseAgeExceedsTheThreshold() {
     Response young = newResponse("young", 70);
     Response tooOld = newResponse("too old", 60);
     Response younger = newResponse("younger", 75);
 
     List<Response> responses = new ArrayList<Response>();
     responses.add(young);
     responses.add(tooOld);
     responses.add(younger);
 
     Selector selector = new Selector();
     selector.setMaxAllowedAge(35);
     List<Response> selected = selector.select(new Request(100L,  ""), responses, tracker);
 
     assertEquals(Arrays.asList(young, younger), selected);
     verify(tracker).discardedResponse(tooOld, "Dropped due to age [40]");
   }
 
   @Test
   public void discardsResponseIfNoRevisionIsSpecieidForItsGenerator() {
     Response discarded = newResponse("discarded", 0, new Label("generator", "label", 1));
     Request request = new Request(0L,  "", new Label("other-generator", "label", 1));
 
     Selector selector = new Selector();
     List<Response> selected = selector.select(request, asList(discarded), tracker);
 
     assertEquals(0, selected.size());
     verify(tracker).discardedResponse(discarded, "Label [generator/label] was not requested");
   }
 
   @Test
   public void discardsResponseIfNoRevisionIsSpecieidForItsLabel() {
     Response discarded = newResponse("discarded", 0, new Label("G", "some-label", 1));
     Request request = new Request(0L,  "", new Label("G", "some-other-label", 1));
 
     Selector selector = new Selector();
     List<Response> selected = selector.select(request, asList(discarded), tracker);
 
     assertEquals(0, selected.size());
     verify(tracker).discardedResponse(discarded, "Label [G/some-label] was not requested");
   }
 
   @Test
   public void discardsResponseIfDifferentRevisionIsSpecieidForItsLabel() {
     Selector selector = new Selector();
 
     int actualRevision = 1;
     int requestedRevision = 300;
     Response discarded = newResponse("discarded", 0, new Label("G", "LABEL", actualRevision));
     Request request = new Request(0L,  "", new Label("G", "LABEL", requestedRevision));
     List<Response> selected = selector.select(request, asList(discarded), tracker);
 
     assertEquals(0, selected.size());
     verify(tracker).discardedResponse(discarded,
         "Requested revision [300] does not match the response's revision [1]");
   }
 
   @Test
   public void retainsResponseIfItsLabelMatchesTheRequestedLabel() {
     Response retained = newResponse("retained", 0, new Label("G", "LABEL", 1));
     Request request = new Request(0L,  "", new Label("G", "LABEL", 1));
 
     Selector selector = new Selector();
     List<Response> selected = selector.select(request, asList(retained), tracker);
 
     assertEquals(asList(retained), selected);
   }
 
   @Test
   public void handlesMultipleLabels() {
     Response a1 = newResponse("a1", 0, new Label("G", "A", 1));
     Response b500 = newResponse("a1", 0, new Label("G", "B", 500));
     Response c3 = newResponse("a1", 0, new Label("G", "C", 3));
     Response anotherA1 = newResponse("anotherA1", 0, new Label("G", "A", 1));
     Request request = new Request(0L,  "", new Label("G", "A", 1), new Label("G", "B", 2),
         new Label("G", "C", 3));
 
     Selector selector = new Selector();
     List<Response> selected = selector.select(request, asList(a1, b500, c3, anotherA1), tracker);
 
     assertEquals(asList(a1, c3, anotherA1), selected);
   }
 
   @Test
   public void usesTheSelectAllPolicyWhenRequestSaysSo() {
     Map<String, String> map = new HashMap<String, String>();
     map.put(Selector.SELECTION_POLICY_KEY, SelectionPolicy.SELECT_ALL.name());
     Request a1Request = new Request(0L,  "", map, new Label("G", "A", 1));
 
     Response a2Response = newResponse("retained", 0, new Label("G", "A", 2));
 
     Selector selector = new Selector();
     List<Response> selected = selector.select(a1Request, asList(a2Response), tracker);
 
     assertEquals(asList(a2Response), selected);
   }
 
   @Test
   public void retainsResponseIfNoRequestedLabelIsSpecifiedButItMatchesTheDefaultLabel() {
     Response a1 = newResponse("a1", 0, new Label("G_G", "A", 1));
     Request request = new Request(0L,  "", new Label("G_G", "SOME-OTHER-LABEL", 500));
 
     SelectorConfig config = new SelectorConfig();
     config.LoadFromString("{" +
     		"generators: [" +
         "  { name: 'G_G', default_labels: [['A', 1], ['B', 2]] }]}");
 
     Selector selector = new Selector(config);
     List<Response> selected = selector.select(request, asList(a1), tracker);
 
     assertEquals(asList(a1), selected);
   }
 
   @Test
   public void discardsResponseIfNoRequestedLabelIsSpecifiedAndDefaultLabelDoesNotMatch() {
     Response a1 = newResponse("a1", 0, new Label("G_G", "A", 1));
     Request request = new Request(0L,  "");
 
     SelectorConfig config = new SelectorConfig();
     config.LoadFromString("{" +
         "generators: [" +
         "  { name: 'G_G', default_labels: [['A', 2]] }]}");
 
     Selector selector = new Selector(config);
     List<Response> selected = selector.select(request, asList(a1), tracker);
 
     assertEquals(0, selected.size());
     verify(tracker).discardedResponse(a1,
         "Default revision [2] does not match the response's revision [1]");
   }
 
   @Test
   public void temporaryTestJutsToMakeSureWeDoNotForgetTheFakeImpl() {
     Response b2 = newResponse("a1", 0, new Label("G_G", "B", 2));
     Request request = new Request(0L,  "");
 
     SelectorConfig config = new SelectorConfig();
     config.LoadFromString("{" +
         "generators: [" +
         "  { name: 'G_G', default_labels: [['B', 2]] }]}");
 
     Selector selector = new Selector(config);
     List<Response> selected = selector.select(request, asList(b2), tracker);
 
     assertEquals(asList(b2), selected);
   }
 }
