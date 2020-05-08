 package mx.ferreyra.dogapp;
 
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.net.UnknownServiceException;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import mx.ferreyra.dogapp.org.ksoap2.SoapEnvelope;
 import mx.ferreyra.dogapp.org.ksoap2.serialization.SoapObject;
 import mx.ferreyra.dogapp.org.ksoap2.serialization.SoapSerializationEnvelope;
 import mx.ferreyra.dogapp.org.ksoap2.transport.HttpTransportSE;
 import static mx.ferreyra.dogapp.ui.UI.*;
import static mx.ferreyra.dogapp.ui.DialogHelper.*;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.RectF;
 import android.graphics.drawable.GradientDrawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.RoundRectShape;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LoginScreen extends Activity{
 
 	private String result;
 	private SoapParseLogin soapParseLogin;
 	private EditText email,password;
 	private Intent i; 
 	private String returnValue;
 	private String userEmail,userPassword; 
 	private ProgressBar progressBar;
 
 	private Button btnLeftTitle, btnRightTitle;
 	private TextView txtTitle;
 	
 	private LoginHandler loginHandler;
 
 	public void onCreate(Bundle savedInstanceState)
 	{ 
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.login); 
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
 				R.layout.title_bar);
 
 
 		btnLeftTitle = (Button)findViewById(R.id.tbutton_left);
 		txtTitle = (TextView)findViewById(R.id.title_txt);
 		btnRightTitle = (Button)findViewById(R.id.tbutton_right);
 
 		txtTitle.setText(getResources().getString(R.string.login_title));
 		btnRightTitle.setVisibility(View.INVISIBLE);
 
 
 		email = (EditText)findViewById(R.id.emailEdit);  
 		password = (EditText)findViewById(R.id.passwordEdit); 
 		progressBar = (ProgressBar)findViewById(R.id.login_progress);
 		progressBar.setVisibility(View.INVISIBLE);
 		
 
 
 		btnLeftTitle.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 
 
 	} 
 
     public void onClickForgotPasswordButton(View view) {
         startActivity(new Intent(this, RetrievePassword.class));
     }
 
     public void onClickSubmitButton(View view) {
         if(validateForm()) {
             String input[] = {
                 this.userEmail = this.email.getText().toString(),
                 this.userPassword = this.password.getText().toString()
             };
             soapParseLogin = new SoapParseLogin();
             soapParseLogin.execute(input);
         }
     }
 
     /**
      * Validates form to send for login.
      */
     public boolean validateForm() {
         String userEmail = this.email.getText().toString();
         String userPassword = this.password.getText().toString();
 
         // Email validation
         if("".equals(userEmail.trim())) {
             // Email is empty
            showOkDialog(this, getString(R.string.email_empty_message), null);
             return false;
         }
 
         // Password validation
         if("".equals(userPassword.trim())) {
             // Password is empty
            showOkDialog(this, getString(R.string.password_empty_message), null);
             return false;
         }
 
         return true;
     }
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if(progressBar != null)
 			progressBar.setVisibility(View.INVISIBLE);
 		
 		if(soapParseLogin != null && (soapParseLogin.getStatus() == Status.PENDING || soapParseLogin.getStatus() == Status.RUNNING)){
 			soapParseLogin.cancel(true);
 		}
 	}
 
 	private class SoapParseLogin extends AsyncTask<String, String, Boolean> {
 		private String errorMsg;
 		@Override
 		protected void onPreExecute() {
 			if(progressBar != null){
 				progressBar.setVisibility(View.VISIBLE);
 				
 				int cornerRadius = 10;
 				float[] outerR = new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
 				RectF inset = new RectF(2, 2, 2, 2);
 				float[] innerR = new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
 				ShapeDrawable circle = new ShapeDrawable(new RoundRectShape(outerR, inset, innerR));
 				circle.setPadding(6, 0, 6, 0);
 				 
 				GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.BLUE, Color.RED});
 				gradient.setCornerRadius(cornerRadius);
 				gradient.setBounds(circle.getBounds());		
 
 				
 				progressBar.setProgressDrawable(gradient);   
 				progressBar.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
 			}
 		}
 
 
 		protected Boolean doInBackground(String... results) {
 			try {
 				SoapObject request = new SoapObject(AppData.NAMESPACE,	AppData.METHOD_NAME_USER_LOGIN);
 
 				request.addProperty("username", userEmail);
 				request.addProperty("password", userPassword);
 
 				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
 						SoapEnvelope.VER11);
 				envelope.dotNet=true;
 
 				envelope.setOutputSoapObject(request);
 
 				HttpTransportSE androidHttpTransport = new HttpTransportSE(AppData.URL);
 				androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 				androidHttpTransport.debug=true;				
 
 				androidHttpTransport.call(AppData.SOAP_ACTION+AppData.METHOD_NAME_USER_LOGIN, envelope); 
 
 				result = androidHttpTransport.responseDump;	
 
 				return getParsedMyXML(result);
 			}
 			catch (UnknownHostException e) {
 				errorMsg = getResources().getString(R.string.no_connection);
 				Log.e(DogUtil.DEBUG_TAG, errorMsg);
 				return false;
 			}catch (UnknownServiceException e) {
 				errorMsg = getResources().getString(R.string.service_unavailable);				
 				Log.e(DogUtil.DEBUG_TAG,  getResources().getString(R.string.service_unavailable));
 				return false;
 			}catch (MalformedURLException e) {
 				errorMsg = getResources().getString(R.string.url_malformed);	
 				//e.printStackTrace();
 				Log.e(DogUtil.DEBUG_TAG, errorMsg );
 				return false;
 			}catch (Exception e) {
 				errorMsg = getResources().getString(R.string.unable_to_get_data);	
 				Log.e(DogUtil.DEBUG_TAG, errorMsg);
 				//e.printStackTrace();
 				return false;
 			}
 
 
 		}
 
 		protected void onProgressUpdate(String... values ) {
 		}
 
 		protected void onPostExecute(Boolean b) {
 			try{
 				if(progressBar != null)
 					progressBar.setVisibility(View.INVISIBLE);
 
 				if (loginHandler != null && !loginHandler.getData())
 					Toast.makeText(getApplicationContext(), R.string.service_error, Toast.LENGTH_SHORT).show();
 				else if(!b){
 					Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
 					return;
 				}	
 				if (returnValue.equals("-1")){
 					Toast.makeText(getApplicationContext(), R.string.invalid_params, Toast.LENGTH_SHORT).show();
 				}else if (returnValue.equals("-2")) {
 					Toast.makeText(getApplicationContext(), R.string.invalid_username_pwd, Toast.LENGTH_SHORT).show();
 				}else if (returnValue.equals("-3")) {
 					Toast.makeText(getApplicationContext(), R.string.user_not_exist, Toast.LENGTH_SHORT).show();
 				}else{
 					Integer userId = Integer.parseInt( returnValue );
 					// Save user id
                     DogUtil.getInstance().saveCurrentUserId(userId);
 
                     Intent intent = new Intent();
 			        intent.putExtra("ID_USER", userId);
 					
 					setResult(RESULT_OK, intent);
 					finish();
 				}
 			}catch (Exception e) { 
 
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 
 			super.onCancelled();
 			try{ 
 				if(progressBar != null)
 					progressBar.setVisibility(View.INVISIBLE);
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 
 	private  class LoginHandler extends DefaultHandler{
 
 		private boolean isreturn;
 		private boolean hasData = false;
 
 
 		@Override
 		public void startDocument() throws SAXException {
 
 		}
 
 		@Override
 		public void endDocument() throws SAXException {
 
 		}
 
 		@Override
 		public void startElement(String namespaceURI, String localName,
 				String qName, Attributes atts) throws SAXException {		
 			if (localName.equals("return")){
 				isreturn = true;
 				hasData = true;
 			}
 		} 
 		@Override
 		public void endElement(String namespaceURI, String localName, String qName)
 		throws SAXException {
 
 			if (localName.equals("return")){
 				isreturn = false;
 
 			}
 		}  
 
 		@Override
 		public void characters(char ch[], int start, int length) {
 			String value = new String(ch, start, length);
 			if(value == null || value.length()<=0)
 				return;
 
 			if (isreturn){
 				returnValue = value;
 			}
 		}
 
 		public boolean getData(){
 			return hasData;
 		}
 	}
 	
 	private boolean getParsedMyXML(String xml) throws Exception {
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 		SAXParser sp = spf.newSAXParser();
 
 		XMLReader xr = sp.getXMLReader();
 
 		loginHandler = new LoginHandler();
 		xr.setContentHandler(loginHandler);   
 		try {
 			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
 			xr.parse(new InputSource(is));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return false;
 		} 
 
 		if(!loginHandler.getData())
 			return false;
 
 
 		return true;
 	}
 }
