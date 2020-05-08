 package hoplugins.flagsplugin;
 /**
  * FlagRenderer.java
  *
  * @author Daniel González Fisher
  */
 
 import hoplugins.FlagsPlugin;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.text.NumberFormat;
import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 public class FlagRenderer extends JLabel implements ListCellRenderer {
 	//private Properties paises;
    private HashMap<String,Integer> invert;
     private plugins.IHOMiniModel hoModel;
     private transient FlagCollection flagSet;
     final public static NumberFormat numberFormat = NumberFormat.getInstance();
 	private static final long serialVersionUID = 7966608444470977408L;
 
    public FlagRenderer(plugins.IHOMiniModel mod, HashMap<String,Integer> hm) {
         setOpaque(true);
         setHorizontalAlignment(LEADING);
         setVerticalAlignment(CENTER);
         hoModel = mod;
        invert = hm;
         flagSet = null;
         setOpaque(true);
         numberFormat.setMaximumFractionDigits(2);
     }
 
     public void setFlagCollection(FlagCollection f) {
         flagSet = f;
     }
 
     /*
      * This method finds the image and text corresponding
      * to the selected value and returns the label, set up
      * to display the text and image.
      */
     public Component getListCellRendererComponent(JList list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {
         //Get the selected index. (The index param isn't
         //always valid, so just use the value.)
         String pais = value.toString();
         //String pais = ((FlagObject)value).getName();
         int selectedIndex = ((FlagObject)value).getId();
 
         setFont(list.getFont());
         //if (pais.equals("\u65e5\u672c")) setFont(new Font("MS Gothic",Font.PLAIN,12));    // NIHONGO
         if (pais.equals("\u65e5\u672c")) {
 //             Font fntJp = Font.getFont("MS Gothic");
 //             if (fntJp.canDisplay('\u65e5')) setFont(fntJp);
             pais = "Nippon";
         }
         if (isSelected) {
             setBackground(list.getSelectionBackground());
             setForeground(list.getSelectionForeground());
         } else {
             setBackground(list.getBackground());
             setForeground(list.getForeground());
         }
         if (flagSet != null) {
             if (flagSet.contains(value)) {
                 setForeground(Color.LIGHT_GRAY);
                 setFont(getFont().deriveFont(Font.ITALIC));
             }
         }
 
         //Set the icon and text.  If icon was null, say so.
         ImageIcon icon = null;
         if (hoModel != null) {
             try {
                 //icon = new ImageIcon(hoModel.getHelper().loadImage("gui/bilder/flaggen/" + selectedIndex + "flag.png"));
                 icon = hoModel.getHelper().getImageIcon4Country(selectedIndex);
             } catch (Exception exc) {
                 icon = new ImageIcon(FlagsPlugin.HOM.getHelper().loadImage("flags/Unknownflag.png"));
             }
         }
         else {
             icon = new ImageIcon("flags/" + selectedIndex + "flag.png");
         }
         setIcon(icon);
         //if (icon != null) {
         setText(pais);
         if (FlagsPlugin.SORT_LIST_BY_COOLNESS) setToolTipText("Coolness rating = " +
                                                               numberFormat.format( ((FlagObject)value).getCoolness() ) );
         else setToolTipText("countryID = " + selectedIndex);
         return this;
     }
 
 }
