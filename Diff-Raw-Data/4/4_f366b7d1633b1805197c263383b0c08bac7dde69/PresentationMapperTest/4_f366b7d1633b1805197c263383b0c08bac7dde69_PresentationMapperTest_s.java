 package com.thoughtworks.twu.domain;
 
 import com.thoughtworks.twu.persistence.PresentationMapper;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 public class PresentationMapperTest extends IntegrationTest {
 
     @Autowired
     private PresentationMapper presentationMapper;
 
 
     @Test
     public void shouldChoosePresentationByTitle() {
         String pechaKucha = "pechaKucha";
         String description = "pecha Kucha description";
         String owner = "Teddy";
         presentationMapper.insertPresentation(new Presentation(pechaKucha, description, owner));
         Presentation presentation = presentationMapper.getPresentation(pechaKucha);
         assertEquals(presentation, (new Presentation("pechaKucha", "pecha Kucha description", "Teddy")));
     }
 
     @Test
     public void shouldRetrievePresentationListByOwner() throws Exception {
         presentationMapper.insertPresentation(new Presentation("pechaKucha", "Today at 8", "Prateek"));
         presentationMapper.insertPresentation(new Presentation("blah", "Today at 25", "Prateek"));
         presentationMapper.insertPresentation(new Presentation("bleh", "Yesterday at 26", "Manan"));
 
        ArrayList<Presentation> expectedPresentationList=new ArrayList<>();
         expectedPresentationList.add(new Presentation("pechaKucha", "Today at 8", "Prateek"));
         expectedPresentationList.add(new Presentation("blah", "Today at 25", "Prateek"));
         String owner = "Prateek";
         assertThat(expectedPresentationList, is(presentationMapper.getPresentationsByOwner(owner)));

     }
 }
