 package ee.mattijagula.mikker;
 
 import ee.mattijagula.mikker.recorder.SoundRecorder;
 import ee.mattijagula.mikker.ui.RecorderPanel;
 import netscape.javascript.JSObject;
 
 import javax.swing.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 public class RecorderApplet extends JApplet implements Configuration.ParameterSource {
     public void init() {
         Configuration ctx = new Configuration(this);
         SoundRecorder recorder = new SoundRecorder(ctx.getRecordingFilename());
         RecorderPanel recorderPanel = new RecorderPanel(ctx, recorder);
         
         getContentPane().add(recorderPanel);
 
         usePlatformLookAndFeel();
 
         System.out.println("Initializing microphone");
         recorder.initializeMicrophone();
     }
 
     @Override
     public String[][] getParameterInfo() {
         return Configuration.supportedParameters();
     }
 
     private void usePlatformLookAndFeel() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public URI getBaseUri() {
         try {
             return getDocumentBase().toURI();
         } catch (URISyntaxException e) {
             // cannot happen
             throw new IllegalStateException("Applet.getDocumentBase() returns illegal URI: " + getDocumentBase());
         }
     }
 
     public String getCookies() {
         System.out.println("reading cookies");
         JSObject window = JSObject.getWindow(this);
         Object result = window.eval("document.cookie");
         return result.toString();
     }
 
     public String determineUserAgent() {
         JSObject window = JSObject.getWindow(this);
         System.out.println("Determining user agent");
         Object result = window.eval("navigator.userAgent");
         return result.toString();
     }
 }
