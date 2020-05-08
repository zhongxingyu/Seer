 package coupling.app.data;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.ResourceBundle;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import com.nit.coupling.R;
 
import coupling.app.App;

 import android.app.Activity;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 
 public class XMLParser {
 
	public XMLParser() {
 	}
 	
 	public LinkedList<String> parseItems() throws XmlPullParserException, IOException{
 		LinkedList<String> list = new LinkedList<String>();
		Resources res = App.getAppCtx().getResources();
 		XmlResourceParser xpp = res.getXml(R.xml.items);
 		xpp.next();
 		int eventType = xpp.getEventType();
 		while (eventType != XmlPullParser.END_DOCUMENT)
 		{
 
 			if(eventType == XmlPullParser.TEXT)
 			{
 				list.add(xpp.getText());
 			}
 			eventType = xpp.next();
 		}
 		
 		return list;
 	}
 
 }
