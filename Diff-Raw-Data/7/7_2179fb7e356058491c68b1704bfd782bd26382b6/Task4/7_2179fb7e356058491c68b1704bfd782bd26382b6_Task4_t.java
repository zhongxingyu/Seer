 package fbw;
 
 import org.wings.SLabel;
 import org.wings.SPanel;
 
 /**
  * This demo opens the PDF document requested in task 4 in a new browser tab.
  *
  * @author Stephan Schuster
  */
 public class Task4 extends SPanel {
 
     public Task4() {
        String pdf = "../doc/Choosing_a_Web_Framework.pdf";
        add(new SLabel("<html><a href=\"" + pdf + "\" target=\"_blank\">Show PDF document</a>"));
     }
 }
