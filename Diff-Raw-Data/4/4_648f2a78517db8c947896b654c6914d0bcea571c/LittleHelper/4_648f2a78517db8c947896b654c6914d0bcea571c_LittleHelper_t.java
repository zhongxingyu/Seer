 import e.gui.*;
 import e.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 import java.util.regex.*;
 import javax.swing.*;
 
 /**
  * Yet another attempt at the whole Google onebox/Mac OS Spotlight/GNOME Deskbar genre.
  * 
  * Under GNOME, to run little-helper when F4 (the Dashboard key on Apple keyboards) is pressed:
  * 
 * % gconftool-2 -t string -s /apps/metacity/global_keybindings/run_command_4 F4
 * % gconftool-2 -t string -s /apps/metacity/keybinding_commands/command_4 `which little-helper`
  * 
  * You can also use gconf-editor(1) to edit these from the GUI.
  */
 public class LittleHelper extends JFrame {
     private static class Verb {
         private final Pattern pattern;
         private final String urlTemplate;
         
         public Verb(String regularExpression, String urlTemplate) {
             this.pattern = Pattern.compile(regularExpression);
             this.urlTemplate = urlTemplate;
         }
         
         public String getUrl(String query) {
             final Matcher matcher = pattern.matcher(query);
             if (matcher.matches()) {
                 final String urlEncodedQuery = StringUtilities.urlEncode(matcher.group(1));
                 return new Formatter().format(urlTemplate, urlEncodedQuery).toString();
             }
             return null;
         }
     }
     
     private static final List<Verb> verbs = new ArrayList<Verb>();
     
     private JTextField textField;
     private JList resultList;
     
     public LittleHelper() {
         super("Little Helper");
         
         setContentPane(makeUi());
         pack();
         
         setLocationRelativeTo(null);
         
         // FIXME: this is a bit harsh (but effective: MainFrame doesn't seem to work right, and even when it does it's slow).
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JFrameUtilities.closeOnEsc(this);
         
         GuiUtilities.finishGnomeStartup();
     }
     
     private JPanel makeUi() {
         final JPanel ui = new JPanel(new BorderLayout());
         ui.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
         
         this.textField = new JTextField(40);
         textField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 help();
             }
         });
         ui.add(textField, BorderLayout.NORTH);
         
         this.resultList = new JList();
         resultList.setVisibleRowCount(8);
         ui.add(new JScrollPane(resultList), BorderLayout.CENTER);
         
         return ui;
     }
     
     private void help() {
         resultList.setModel(new DefaultListModel());
         final String query = textField.getText().trim();
         if (query.length() == 0) {
             return;
         }
         
         // FIXME: do everything from this point down on another thread.
         
         // Do we have a "verb"?
         for (Verb verb : verbs) {
             final String url = verb.getUrl(query);
             if (url != null) {
                 try {
                     BrowserLauncher.openURL(url);
                 } catch (java.io.IOException ex) {
                     // bark
                 }
                 // Our work here is done.
                 setVisible(false);
                 return;
             }
         }
         
         DefaultListModel model = new DefaultListModel();
         
         // Convert bases.
         NumberDecoder numberDecoder = new NumberDecoder(query);
         if (numberDecoder.isValid()) {
             for (String item : numberDecoder.toStrings()) {
                 model.addElement(item);
             }
         }
         
         // Convert units.
         final String unitConversion = convertUnits(query);
         if (unitConversion != null) {
             model.addElement(unitConversion);
         }
         
         // FIXME: Calculator.
         
         resultList.setModel(model);
     }
     
     private static String convertUnits(String s) {
         // Temperature?
         // "0C" => "32 F"
         // "-40C" => "-40 F"
         // "100C" => "212 F"
         final Matcher temperatureMatcher = Pattern.compile("^(-?[\\d.]+) *([CF])").matcher(s);
         if (temperatureMatcher.matches()) {
             final double originalValue = Double.parseDouble(temperatureMatcher.group(1));
             final char originalUnit = temperatureMatcher.group(2).charAt(0);
             switch (originalUnit) {
             case 'C':
                 return String.format("%.1f F", 32.0 + (9.0 * originalValue / 5.0));
             case 'F':
                 return String.format("%.1f C", 5.0 * (originalValue - 32.0) / 9.0);
             }
         }
         
         // Imperial length?
         // First try to normalize.
         final String maybeImperialLength = s.replaceAll("f(?:eet|oot|t)", "'").replaceAll("in(?:ch|ches)?", "\"");
         final String maybeMetricLength = convertImperial("'", 12.0, "\"", 0.0254, "m", maybeImperialLength);
         if (maybeMetricLength != null) {
             return maybeMetricLength;
         }
         // 13.3"
         // 13.3 "
         // 13.3 inches
         // 6'
         // 6 foot
         // 5'4"
         // 5' 4"
         // 5'4"
         // 5feet 4inches
         // 5 feet 4 inches
         // 5ft 4in
         // 5 ft 4 in
         
         // FIXME: Imperial distances?
         // 200 miles / 200 mi
         
         // FIXME: Metric length?
         // 1.37m
         // 1m 37cm
         // 2 meters
         // 24.3 cm
         // 90.7 mm
         // 200 km
         
         // Imperial Weight?
         // First try to normalize.
         final String maybeImperialWeight = s.replaceAll("(?:pound|lb)s?", "lb").replaceAll("(?:ounce|ounces|oz)", "oz");
         final String maybeMetricWeight = convertImperial("lb", 16.0, "oz", 0.0283495231, "kg", maybeImperialWeight);
         if (maybeMetricWeight != null) {
             return maybeMetricWeight;
         }
         
         // 5.0 pounds
         // 5lbs
         // 1.3 ounces
         // 1.3 oz
         
         // 2.27kg
         // 36.8 grams
         // 36.8 g
         
         // FIXME: Currency?
         
         return null;
     }
     
     private static String convertImperial(String bigUnit, double smallUnitsPerBigUnit, String smallUnit, double toMetric, String metricUnit, String input) {
         final Matcher imperialMatcher = Pattern.compile("^(?:([\\d.]+) *" + bigUnit + ")? *(?:(([\\d.]+)) *" + smallUnit + ")?").matcher(input);
         if (!imperialMatcher.matches()) {
             return null;
         }
         String bigValue = imperialMatcher.group(1);
         if (bigValue == null) {
             bigValue = "0";
         }
         String smallValue = imperialMatcher.group(2);
         if (smallValue == null) {
             smallValue = "0";
         }
         final double value = (smallUnitsPerBigUnit * Double.parseDouble(bigValue)) + Double.parseDouble(smallValue);
         // FIXME: choose an appropriate SI prefix and precision based on the input.
         return String.format("%.2f %s", toMetric * value, metricUnit);
     }
     
     private static void initVerbs() {
         verbs.add(new Verb("^(?:i(?:mdb)?) +(.*)", "http://www.imdb.com/find?s=all&q=%s&x=0&y=0"));
         verbs.add(new Verb("^(?:g(?:oogle)?) +(.*)", "http://www.google.com/search?hl=en&q=%s&btnG=Google+Search&aq=f&oq="));
         verbs.add(new Verb("^(?:w(?:ikipedia)?) +(.*)", "http://en.wikipedia.org/wiki/Special:Search?search=%s&go=Go"));
     }
     
     public static void main(String[] args) {
         initVerbs();
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 GuiUtilities.initLookAndFeel();
                 new LittleHelper().setVisible(true);
             }
         });
     }
 }
