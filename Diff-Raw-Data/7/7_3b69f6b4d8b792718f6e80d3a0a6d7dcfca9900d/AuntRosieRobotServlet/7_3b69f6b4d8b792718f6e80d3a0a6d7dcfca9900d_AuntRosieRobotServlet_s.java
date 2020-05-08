 package org.andrewhitchcock.auntrosie;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.google.wave.api.AbstractRobot;
 import com.google.wave.api.Annotation;
 import com.google.wave.api.Blip;
 import com.google.wave.api.Event;
 import com.google.wave.api.EventType;
 import com.google.wave.api.Gadget;
 import com.google.wave.api.Range;
 import com.google.wave.api.RobotMessageBundle;
 import com.google.wave.api.TextView;
 
 public class AuntRosieRobotServlet extends AbstractRobot {
 
 	private static final long serialVersionUID = -1546080376029476133L;
 
	private static final String myAddress = "webmaster@appspot.com";
	private static final String buttonUrl = "http://webmaster.appspot.com/translate-button.xml";
 	
 	@Override
 	public String getRobotName() {
 		return "Aunt Rosie";
 	}
 
 	@Override
 	public void processEvents(RobotMessageBundle bundle) {
 		for (Event e : bundle.getEvents()) {
 		  if (e.getType() == EventType.DOCUMENT_CHANGED) {
 		    Blip blip = e.getBlip();
 		    
 		    // Skip blips I created.
 		    if (blip.getCreator().equals(myAddress)) {
 		      continue;
 		    }
 		    
 		    TextView doc = blip.getDocument();
 		    
 		    Gadget button = doc.getGadgetView().getGadget(buttonUrl);
 		    if (button == null && !doc.getAnnotations("lang").isEmpty()) {
 		      doc.insert(0, "\n");
 		      doc.insertElement(0, new Gadget(buttonUrl));
 		      doc.insert(0, "\n");
 		      continue;
 		    }
 		    
 		    String languageTarget = button.getField("lang");
		    if (languageTarget.equals("none")) {
 		      continue;
 		    }
 		    
 		    List<Annotation> annotationsToTranslate = new ArrayList<Annotation>();
 		    for (Annotation langAnnotations : doc.getAnnotations("lang")) {
 		      // Don't try to translate a language to itself
 		      if (! (langAnnotations.getValue().equals(languageTarget)
 		          || langAnnotations.getValue().equals("unknown"))) {
 		        annotationsToTranslate.add(langAnnotations);
 		      }
 		    }
 
         Blip myBlip = null;
         for (Blip child : blip.getChildren()) {
           if (child.getCreator().equals(myAddress)) {
             myBlip = child;
             break;
           }
         }
         if (myBlip == null) {
           myBlip = blip.createChild();
         }
         
         TextView myDoc = myBlip.getDocument();
         myDoc.delete();
         /*
         myDoc.append(languageTarget + "\n");
         myDoc.append(doc.getAnnotations().size() + "\n");
 		    for (Annotation a : doc.getAnnotations()) {
 		      myDoc.append(a + "\n");
 		    }
 		    myDoc.append(doc.getText() + "\n");
 		    */
         
 		    for (Annotation a : annotationsToTranslate) {
 		      Range r = a.getRange();
 		      String translation = translateText(
 		          a.getValue(),
 		          languageTarget,
 		          doc.getText(new Range(Math.max(0, r.getStart()), r.getEnd())));
     		  myDoc.append(translation);
 			  }
 		  }
 		}
 	}
 	
 	private String translateText(String from, String to, String text) {
 	  try {
 	    String encoded = URLEncoder.encode(text, "UTF-8");
 	    URL url = new URI("http://ajax.googleapis.com/ajax/services/language/translate?langpair=" + from + "%7C" + to + "&v=1.0&q=" + encoded).toURL();
 	    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
 	    
 	    StringBuilder sb = new StringBuilder();
 	    String line;
 	    while ((line = reader.readLine()) != null) {
 	      sb.append(line);
 	    }
 	    
 	    JSONObject json = new JSONObject(new JSONTokener(sb.toString()));
 	    if (json.isNull("responseData")) {
 	      return json.getString("responseDetails");
 	    } else {
 	      return json.getJSONObject("responseData").getString("translatedText");
 	    }
 	  } catch (Exception e) {
 	    return e.toString();
 	  }
 	}
 
 	@Override
 	public void registerForEvents() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
