 package wingset;
 
 import org.wings.*;
 import org.wings.border.SEmptyBorder;
 import org.wings.script.JavaScriptListener;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 
 /**
  * @author <a href="mailto:B.Schmid@eXXcellent.de">Benjamin Schmid</a>
  */
 public class NestingTest
     extends WingSetPane
 {
     private final SComboBox selectComboBox = new SComboBox();
     private final SPanel mainPanel = new SPanel(new SGridLayout(1, 2, 10, 0));
     private Color[] colors = new Color[] { Color.red, Color.green, Color.yellow};
 
     protected SComponent createControls() {
        selectComboBox.addScriptListener(JavaScriptListener.JS_ON_CHANGE_SUBMIT_FORM);
         for(int i = 5; i< 30; i++) {
             selectComboBox.addItem(new Integer(i));
         }
         selectComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 update();
             }
         });
         selectComboBox.setSelectedIndex(0);
         selectComboBox.setHorizontalAlignment(SConstants.LEFT_ALIGN);
         return selectComboBox;
     }
 
     protected SComponent createExample() {
         update();
         return mainPanel;
     }
 
     private void update() {
         mainPanel.removeAll();
 
         SPanel buttonPanel = new SPanel(new SFlowDownLayout());
         buttonPanel.setBackground(new  Color(0xD0, 0xD0, 0xFF));
         buttonPanel.setPreferredSize(new SDimension(150,-1));
         buttonPanel.add(createButton(0, "Root"));
         buttonPanel.add(createButton(1, "Menu 1"));
         buttonPanel.add(createButton(1, "Menu 2"));
         buttonPanel.add(createButton(2, "Menu 2a"));
         buttonPanel.add(createButton(2, "Menu 2b"));
         buttonPanel.add(createButton(1, "Menu 3"));
 
 
         SPanel labelPanel = new SPanel(new SFlowDownLayout());
         labelPanel.setBackground(new  Color(0xD0, 0xFF, 0xD0));
         labelPanel.setPreferredSize(new SDimension(150,-1));
         labelPanel.add(createLabel(0, "Root"));
         labelPanel.add(createLabel(1, "Menu 1"));
         labelPanel.add(createLabel(1, "Menu 2"));
         labelPanel.add(createLabel(2, "Menu 2a"));
         labelPanel.add(createLabel(2, "Menu 2b"));
         labelPanel.add(createLabel(1, "Menu 3"));
 
         SLabel leftLabel = new SLabel("Below you will see panels with GridLayout nested into panels.\n" +
             "You will realize that the browsers stop rendering at a specific level of table nesting.\n" +
             "(in IE try 9 vs. 10; in FireFox 20 vs. 21");
         leftLabel.setHorizontalAlignment(LEFT_ALIGN);
         SPanel nestedPanel = nestPanel(((Integer)selectComboBox.getSelectedItem()).intValue());
         nestedPanel.setHorizontalAlignment(LEFT_ALIGN);
 
         SLabel rightLabel = new SLabel("\nPadding test");
         rightLabel.setHorizontalAlignment(LEFT_ALIGN);
         buttonPanel.setHorizontalAlignment(LEFT_ALIGN);
         labelPanel.setHorizontalAlignment(LEFT_ALIGN);
 
         SPanel left = new SPanel(new SBoxLayout(SConstants.VERTICAL));
         left.add(leftLabel);
         left.add(nestedPanel);
 
         SPanel right = new SPanel(new SBoxLayout(SConstants.VERTICAL));
         right.add(rightLabel);
         right.add(buttonPanel);
         right.add(labelPanel);
 
         mainPanel.add(left);
         mainPanel.add(right);
     }
 
     private SPanel nestPanel(int depth) {
         SPanel panel = new SPanel(new SGridLayout());
         panel.add(new SLabel ("Depth "+depth));
         panel.setBackground(colors[depth % colors.length]);
         panel.setBorder(new SEmptyBorder(20,20,20,20));
         if (depth > 1)
             panel.add(nestPanel(depth-1));
         return panel;
     }
 
     private SButton createButton(int level, String text) {
         SButton button = new SButton(text);
         //button.setShowAsFormComponent(false);
         button.setBorder(new SEmptyBorder(0, level*20, 0, 0));
         return button;
     }
 
     private SLabel createLabel(int level, String text) {
         SLabel button = new SLabel(text);
         button.setBorder(new SEmptyBorder(0, level*20, 0, 0));
         return button;
     }
 
 }
