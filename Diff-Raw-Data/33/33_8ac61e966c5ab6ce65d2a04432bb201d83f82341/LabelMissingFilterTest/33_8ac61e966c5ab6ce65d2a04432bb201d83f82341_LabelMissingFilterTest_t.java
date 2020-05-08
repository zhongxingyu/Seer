 package org.raoxunrong.check.accessibility.filter;
 
 import org.junit.Test;
 import org.openqa.selenium.WebElement;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.Iterator;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class LabelMissingFilterTest extends AbstractHtmlFilterTest {
 
     private LabelMissingFilter labelMissingFilter;
 
     @Override
     public void setup() throws IOException, URISyntaxException {
         super.setup();
         labelMissingFilter = new LabelMissingFilter();
     }
 
     @Test
     public void shouldFilterEmptyElements() throws IOException, URISyntaxException {
         StringBuffer elementBuffer = new StringBuffer();
        elementBuffer.append("<input/>").append("<input type='checkbox'/>").append("<input type='password'/>")
                .append("<input type='radio'/>").append("<input type='text'/>").append("<input type=''/>");
 
         setBodyResource(elementBuffer.toString());
         writeHtmlResource();
         openTestPage();
 
         Collection<WebElement> filterElements = labelMissingFilter.filter(getCheckablePage());
        assertThat(filterElements.size(), is(6));
     }
 }
