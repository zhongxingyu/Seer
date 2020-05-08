 package util.input;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.util.List;
 
 import javax.swing.JComponent;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.XMLOutputter;
 
 import edu.cmu.sphinx.frontend.util.Microphone;
 import edu.cmu.sphinx.recognizer.Recognizer;
 import edu.cmu.sphinx.result.Result;
 import edu.cmu.sphinx.util.props.ConfigurationManager;
 
 /**
  * Voice Input listens for a specific word from a set of commands to be sopoken before notifying Input
  * @author Ying Chen
  */
 public class VoiceInput extends InputDevice implements Runnable {
 
     private static final String DEVICE = "Voice";
     private final String DefaultConfigFilePath = "dict.config.xml";
     private final String DefaultDictionaryFilePath = "util/input";
     private final String DefaultDictionaryFileName = "dict.gram";
 
     private ConfigurationManager voiceManager;
     private Recognizer myRecognizer;
     private String configFilePath = DefaultConfigFilePath;
     private String dictFilePath = DefaultDictionaryFilePath;
     private String dictFileName = DefaultDictionaryFileName;
     private boolean configFileUpdated = false;
 
     /**
      * Constructs a Voice Input object which listens for specific voice commands and notifies input.
      * @param component
      * @param input
      */
     public VoiceInput(JComponent component, Input input) {
         super(DEVICE, input);
 
         setVoiceManager();
         //this.changeDictionary("input/newDict.gram");
         new Thread(this).start();
     }
 
     /**
      * Load the config file and set up the environment for speech recognition
      */
     private void setVoiceManager() {
         voiceManager = new ConfigurationManager(
                 VoiceInput.class.getResource(configFilePath));
        //System.out.println(configFilePath);
         myRecognizer = (Recognizer) voiceManager.lookup("recognizer");
         myRecognizer.allocate();
         Microphone microphone = (Microphone) voiceManager.lookup("microphone");
         if (!microphone.startRecording()) {
             System.out.println("Cannot start microphone.");
             myRecognizer.deallocate();
             System.exit(1);
         }
     }
 
     /**
      * Generates an Alert Object for game behaviors to get the event time.
      * @return
      */
     private AlertObject generateAlertObject() {
         long currentTimeStamp = System.currentTimeMillis();
         return new AlertObject(currentTimeStamp);
     }
 
     /**
      * Starts listening to the user.
      */
     @Override
     public void run() {
         while (true) {
             System.out.println("Start speaking. Press Ctrl-C to quit.");
             Result result = myRecognizer.recognize();
             if (result != null) {
                 String resultText = result.getBestFinalResultNoFiller();
                 notifyInputAction(DEVICE + "_" + resultText.substring(0, 1).toUpperCase() + resultText.substring(1),
                         generateAlertObject());
             } else {
                 System.out.println("I can't hear what you said.\n");
             }
         }
     }
 
     /**
      * Change the dictionary file Need to duplicate the default config file and
      * generate a new one Then change the file path and file name in the new
      * config file
      * 
      * @param filePath
      */
     public void changeDictionary(String filePath) {
         updateDictFile(filePath);
 
         if (!configFileUpdated) {
             try {
                 duplicateConfigFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         // Update the dict file info in the setting file
         SAXBuilder builder = new SAXBuilder();
         File xmlFile = new File("src/util/input/" + configFilePath);
         try {
             Document doc = (Document) builder.build(xmlFile);
             Element rootNode = doc.getRootElement();
 
             List<Element> elements = rootNode.getChildren("component");
             List<Element> childElements = null;
             for (Element e : elements) {
                 if (e.getAttributeValue("name").equals("jsgfGrammar")) {
                     childElements = e.getChildren();
                     break;
                 }
             }
             
             for (Element element : childElements) {
                 if (element.getAttributeValue("name").equals("grammarLocation")) {
                     element.getAttribute("value").setValue(
                             "resource:/" + dictFilePath+"/");
                 } else if (element.getAttributeValue("name").equals(
                         "grammarName")) {
                     element.getAttribute("value").setValue(dictFileName);
                 }
             }
 
             XMLOutputter xmlOutput = new XMLOutputter();
             xmlOutput
                     .output(doc, new FileWriter("src/util/input/" + configFilePath));
 
             // Reload the setting file
             setVoiceManager();
 
         } catch (JDOMException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Update the dictFile path and name Hard coded
      * 
      * @param filePath
      */
     private void updateDictFile(String filePath) {
         System.out.println(filePath);
         int pos = filePath.lastIndexOf(File.separator);
         if (pos == -1) {
             dictFilePath = "";
             dictFileName = filePath.substring(0, filePath.length() - 5);
         } else {
             dictFilePath = filePath.substring(0, pos);
             dictFileName = filePath.substring(pos + 1, filePath.length() - 5);
         }
 //        System.out.println(dictFilePath);
 //        System.out.println(dictFileName);
     }
 
     /**
      * Duplicate the default config file The new file's name is
      * "duplicated.config.xml"
      * 
      * @throws IOException
      */
     private void duplicateConfigFile() throws IOException {
         InputStream in = new FileInputStream("src/util/input/" + configFilePath);
         configFilePath = "newDict.config.xml";
         OutputStream out = new FileOutputStream("src/util/input/" + configFilePath);
         byte[] buffer = new byte[1024];
 
         int len;
         while ((len = in.read(buffer)) > 0) {
             out.write(buffer, 0, len);
         }
         in.close();
         out.close();
     }
 }
