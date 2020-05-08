 package gr.teilar.dionysos;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class AboutDataScreen extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.about_data_screen);
 		
 		findViewById(R.id.update_data_now_info_screen).setOnClickListener(
 				new OnClickListener() {
 					public void onClick(View v) {
 						Intent i = new Intent(
 								getBaseContext(),
 								gr.teilar.dionysos.Dionysos.class);
 						startActivityForResult(i, 1);
 					}
 				});
 		
 //		TextView gradesDate = (TextView) findViewById(R.id.data_info_grades_date_tv);
 		TextView lessonsDate = (TextView) findViewById(R.id.data_info_lessons_date_tv);
 		TextView requestsDate = (TextView) findViewById(R.id.data_info_requests_date_tv);
 		
 		
 		/* TODO read the xml files and display the correct dates */
 		
 //		gradesDate.setText(getXmlDate("grades"));
 		lessonsDate.setText(getXmlDate("lessons"));
 		requestsDate.setText(getXmlDate("requests"));
 	}
 	
 	private String getXmlDate(String type) {
 		File file = new File("/sdcard/egrammatia/"+type+".xml");
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db;
 		Document doc = null;
 		try {
 			db = dbf.newDocumentBuilder();
 			doc = db.parse(file);
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return getResources().getString(R.string.not_found);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return getResources().getString(R.string.not_found);
 		} catch (ParserConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return getResources().getString(R.string.not_found);
 		}
 
 		doc.getDocumentElement().normalize();
 		return ((Element) doc.getElementsByTagName(type).item(0))
 				.getAttribute("date");
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 1) {
 			if (resultCode == RESULT_OK) {
 				Intent intent = getIntent();
 				finish();
 				startActivity(intent);
 			}

			if (resultCode == RESULT_CANCELED) {
				/* TODO 
				 * print a message that we display old xml data */
			}
 		}
 	}
 }
