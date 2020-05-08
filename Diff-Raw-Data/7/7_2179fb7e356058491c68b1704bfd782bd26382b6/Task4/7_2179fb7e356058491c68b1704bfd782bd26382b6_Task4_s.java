 package fbw;
 
 import org.wings.SLabel;
 import org.wings.SPanel;
import org.wings.resource.FileResource;
 
 /**
  * This demo opens the PDF document requested in task 4 in a new browser tab.
  *
  * @author Stephan Schuster
  */
 public class Task4 extends SPanel {
 
     public Task4() {
        final FileResource pdf = new FileResource("../doc/Choosing_a_Web_Framework.pdf");
        pdf.getId();
        add(new SLabel("<html><a href=\"" + pdf.getURL() + "\" target=\"_blank\">Show PDF document</a>"));
     }
 }
