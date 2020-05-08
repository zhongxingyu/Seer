 import com.google.gson.*;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dextor
  * Date: 8/7/13
  * Time: 7:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CSPViewPanel extends JPanel {
 
     private final MainWindow parent;
     private String lastCSP;
 
     private JTextArea cspDisplayArea;
     private final JScrollPane scroll;
 
     public CSPViewPanel(MainWindow parent) {
 
         this.parent = parent;
 
         setLayout(new GridBagLayout());
 
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 1.0;
         c.weighty = 1.0;
 
         cspDisplayArea = new JTextArea("Generate a CSP, it will be shown here", 10, 40);
         cspDisplayArea.setEditable(false);
         cspDisplayArea.setLineWrap(true);
         cspDisplayArea.setWrapStyleWord(true);
 
         scroll = new JScrollPane(cspDisplayArea);
         scroll.setWheelScrollingEnabled(true);
         scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
         add(scroll, c);
     }
 
     public void setCSP(String csp) {
        lastCSP = csp;
 
        JsonObject root = new JsonParser().parse(csp).getAsJsonObject();
         int nVars = root.get("vnum").getAsInt();
         int nCons = root.get("cnum").getAsInt();
 
         JsonArray coeffs = root.getAsJsonArray("coeffs");
         JsonArray cterms = root.getAsJsonArray("cterms");
 
         String[] constraints = new String[nCons];
 
         // rebuilding constraints..
         for (int i=0; i<nCons; i++) {
             String constraint = "";
             for (int j=0; j<nVars; j++) {
                 int coeff = coeffs.get(i * nVars + j).getAsInt();
                 if (coeff > 0) {
                     constraint = constraint.concat("x" + (j + 1) + " + ");
                 }
             }
 
             constraint = constraint.substring(0, constraint.lastIndexOf('+'));
             constraint = constraint.concat("= " + cterms.get(i).getAsInt());
             constraint = constraint.concat("\n");
 
             constraints[i] = constraint;
         }
 
         cspDisplayArea.setText("");
 
         for (String constraint : constraints) {
             cspDisplayArea.append(constraint);
         }
 
         cspDisplayArea.setCaretPosition(0);
     }
 
 }
