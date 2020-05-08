 package com.fatjay.subfunction;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.util.EntityUtils;
 import org.apache.http.NameValuePair;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 
 import com.fatjay.R;
 import com.fatjay.main.userinfo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.Toast;
 
 public class newThread extends Activity implements OnClickListener {
 	userinfo mUserinfo = null;
 	String pidString = "0", reidString = "0", urlString, boardname;
 	boolean is_compress = false;
 	String photoPath;
 	String filenameString;
 	static Uri photoUri;
 	String picURL;
 	String author;
 	String TAG = "New thread";
 	boolean quote = false;
 	private int compress_rate;
 	private ProgressDialog waitDialog;
 	private static final int CAMERA_WITH_DATA = 3023;  
     private static final int PHOTO_PICKED_WITH_DATA = 3021;
     private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/lily/original");
     private static final String TEMP_PHOTO_DIR = Environment.getExternalStorageDirectory() + "/lily/temp";
     
    private int timeoutConnection = 3000;  
    private int timeoutSocket = 5000;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.newthread);
 		mUserinfo = (userinfo)getApplication(); 
 		Bundle mBundle = getIntent().getExtras();
 		String action = mBundle.getString("action");							//specisc the action, reply or new
 		String titleString = mBundle.getString("title");
 		author = mBundle.getString("author");
 		urlString = mBundle.getString("url");
 		SharedPreferences favor = this.getSharedPreferences("compress_rate", 0);
         String temp = favor.getString("rate", null);
         compress_rate = Integer.valueOf(temp);
 		
 		boardname = urlString.substring(urlString.indexOf("=")+1);
 		if (boardname.contains("&")) {
 			boardname = boardname.substring(0, boardname.indexOf("&"));
 		}
 		if (action.equals("reply")) {
 			String title = null;
 			quote = true;
 			title = "Re: " + titleString;
 			title = title.replace(" ", "");
 			((EditText)findViewById(R.id.new_title)).setText(title);
 			((EditText)findViewById(R.id.new_title)).setEnabled(false);
 			reidString = urlString.substring(urlString.indexOf("M.") + 2);
 			reidString = reidString.substring(0, reidString.indexOf(".A"));
 			String pid = mBundle.getString("pid");
 			int count = 3;
 			if (pid.equals("-2")) {
 				while (count > 0) {
 					if (!getPreINFO()) {
 						getIdentify();
 					} else {
 						break;
 					}
 				}
 			} else {
 				pidString = pid;
 			}
 		} else {
 			((EditText)findViewById(R.id.new_title)).setText("");
 		}
 		Button mButton1 = (Button)findViewById(R.id.new_send);
 		mButton1.setOnClickListener(this);
 		Button mButton2 = (Button)findViewById(R.id.new_cancel);
 		mButton2.setOnClickListener(this);
 		Button photoButton = (Button)findViewById(R.id.new_photo);
 		photoButton.setOnClickListener(this);
 	}
 
 	public void onClick(View arg0) {
 		Intent restartFavor = new Intent(); 
 		restartFavor.setClass(newThread.this, threadContent.class);
 		switch (arg0.getId()) {
 		case R.id.new_send:
 			if (send()) {
 				setResult(RESULT_OK, restartFavor);
 				this.finish();
 			}
 			break;
 		case R.id.new_cancel:
 			setResult(RESULT_CANCELED, restartFavor);
 			this.finish();
 			break;
 		case R.id.new_photo:
 			doPickPhotoAction();
 			break;
 		}
 	}
 
 	@Override  
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         super.onActivityResult(requestCode, resultCode, data);  
         ContentResolver mContentResolver = getContentResolver();
         Bitmap photoCaptured = null;
         InputStream iStream = null;
         switch (requestCode) {
 		case CAMERA_WITH_DATA: 
             if (resultCode != RESULT_OK)
             	return;
             if (photoUri == null) {
 				return;
 			}
 			try {
 				iStream = mContentResolver.openInputStream(photoUri);
 			} catch (FileNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try {
                 photoCaptured = BitmapFactory.decodeStream(iStream);
                 int width = photoCaptured.getWidth();
                 int height = photoCaptured.getHeight();
                 if (height==0) {
 					return;
 				}
                 if (width*height > 480000) {
                 	if (width > height) {
                 		photoCaptured = Bitmap.createScaledBitmap(photoCaptured, 800, 800*height/width, true);
                 	} else {
                 		photoCaptured = Bitmap.createScaledBitmap(photoCaptured, 800*width/height, 800, true);
                 	}
                 }
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
                 is_compress = ((CheckBox)findViewById(R.id.new_compress)).isChecked();
                 if (is_compress) {
                 	photoCaptured.compress(Bitmap.CompressFormat.JPEG, compress_rate, baos);
 				} else {
 					photoCaptured.compress(Bitmap.CompressFormat.JPEG, 70, baos);
 				}
                 
                 byte[] photoBytes = baos.toByteArray();
                 Time t=new Time();
                 t.setToNow();
                 String year = String.valueOf(t.year);
                 String month = String.valueOf(t.month+1);
                 String day = String.valueOf(t.monthDay);
                 String hour;
                 if (t.hour<10) {
     				hour = "0" + String.valueOf(t.hour);
     			} else {
     				hour = String.valueOf(t.hour);
     			}
                 String minute = String.valueOf(t.minute);
                 String second = String.valueOf(t.second);
     			filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";
                 File dir = new File(TEMP_PHOTO_DIR);
 				if (!dir.exists()) {
 					dir.mkdirs();
 				}
                 File aFile = new File(TEMP_PHOTO_DIR, filenameString);  
                 photoPath = aFile.getAbsolutePath();  
                 
                 if (aFile.exists())
                 	aFile.delete();  
                 aFile.createNewFile();
 
                 FileOutputStream fos = new FileOutputStream(aFile);  
                 fos.write(photoBytes);
                 fos.close();
                 Log.i(TAG, "дļ" + TEMP_PHOTO_DIR + filenameString);
 
             } catch (FileNotFoundException e) {
                 e.printStackTrace();  
             } catch (IOException e) {  
                 e.printStackTrace();  
             }
             waitDialog = new ProgressDialog(newThread.this);
 	        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	        waitDialog.setMessage("Ժ...");
 	        waitDialog.setTitle("ϴƬ");
 	        waitDialog.setIndeterminate(true);
 	        waitDialog.setCancelable(false);
 	        waitDialog.show();
             uploadFile2Svr();
         	break;
 		case PHOTO_PICKED_WITH_DATA:
 			if (resultCode != RESULT_OK) {
 				return;
 			}
 			Uri picUri = data.getData();
 			if (picUri == null) {
 				return;
 			}
 			try {
 				iStream = mContentResolver.openInputStream(picUri);
 			} catch (FileNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try {
                 photoCaptured = BitmapFactory.decodeStream(iStream);
                 int width = photoCaptured.getWidth();
                 int height = photoCaptured.getHeight();
                 if (height==0) {
 					return;
 				}
                 if (width*height > 480000) {
                 	if (width > height) {
                 		photoCaptured = Bitmap.createScaledBitmap(photoCaptured, 800, 800*height/width, true);
                 	} else {
                 		photoCaptured = Bitmap.createScaledBitmap(photoCaptured, 800*width/height, 800, true);
                 	}
                 }
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
                 is_compress = ((CheckBox)findViewById(R.id.new_compress)).isChecked();
                 if (is_compress) {
                 	photoCaptured.compress(Bitmap.CompressFormat.JPEG, compress_rate, baos);
 				} else {
 					photoCaptured.compress(Bitmap.CompressFormat.JPEG, 70, baos);
 				}
                 
                 byte[] photoBytes = baos.toByteArray();
                 Time t=new Time();
                 t.setToNow();
                 String year = String.valueOf(t.year);
                 String month = String.valueOf(t.month+1);
                 String day = String.valueOf(t.monthDay);
                 String hour;
                 if (t.hour<10) {
     				hour = "0" + String.valueOf(t.hour);
     			} else {
     				hour = String.valueOf(t.hour);
     			}
                 String minute;
                 if (t.minute<10) {
                 	minute = "0" + String.valueOf(t.minute);
     			} else {
     				minute = String.valueOf(t.minute);
     			}
                 String second;
                 if (t.second<10) {
                 	second = "0" + String.valueOf(t.second);
     			} else {
     				second = String.valueOf(t.second);
     			}
     			filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";                
                 File dir = new File(TEMP_PHOTO_DIR);
 				if (!dir.exists()) {
 					dir.mkdirs();
 				}
                 File aFile = new File(TEMP_PHOTO_DIR, filenameString);  
                 photoPath = aFile.getAbsolutePath();
                 
                 if (aFile.exists())
                 	aFile.delete();  
                 aFile.createNewFile();
 
                 FileOutputStream fos = new FileOutputStream(aFile);  
                 fos.write(photoBytes);
                 fos.close();
                 Log.i(TAG, "дļ" + TEMP_PHOTO_DIR + filenameString);
 
             } catch (FileNotFoundException e) {
                 e.printStackTrace();  
             } catch (IOException e) {  
                 e.printStackTrace();  
             }
             waitDialog = new ProgressDialog(newThread.this);
 	        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	        waitDialog.setMessage("Ժ...");
 	        waitDialog.setTitle("ϴƬ");
 	        waitDialog.setIndeterminate(true);
 	        waitDialog.setCancelable(false);
 	        waitDialog.show();
             uploadFile2Svr();
         	break;
 		default:
 			break;
 		}
     }   
 
 	private void doPickPhotoAction() {  
         Context context = newThread.this;  
       
         // Wrap our context to inflate list items using correct theme  
         final Context dialogContext = new ContextThemeWrapper(context,  
                 android.R.style.Theme_Light);  
         String cancel="";  
         String[] choices;  
         choices = new String[2];  
         choices[0] = "һͼƬ";  //  
         choices[1] = "ѡͼƬ";  //ѡ  
         final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,  
                 android.R.layout.simple_list_item_1, choices);  
       
         final AlertDialog.Builder builder = new AlertDialog.Builder(  
                 dialogContext);  
         builder.setTitle("ͼƬѡ");  
         builder.setSingleChoiceItems(adapter, -1,  
                 new DialogInterface.OnClickListener() {  
                     public void onClick(DialogInterface dialog, int which) {  
                         dialog.dismiss();  
                         switch (which) {  
                         case 0:{  
                             String status=Environment.getExternalStorageState();  
                             if(status.equals(Environment.MEDIA_MOUNTED)){//жǷSD  
                             	doTakePhoto();// û˴ȡ  
                             }  
                             else{  
                                 Toast.makeText(newThread.this, "ûSD", Toast.LENGTH_LONG);
                             }  
                             break;  
                               
                         }  
                         case 1:  
                             doPickPhotoFromGallery();// ȥȡ  
                             break;  
                         }  
                     }  
                 });  
         builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {  
       
             @Override  
             public void onClick(DialogInterface dialog, int which) {  
                 dialog.dismiss();  
             }
         });  
         builder.create().show();  
     }
 	
 	protected void doTakePhoto() {  
         try {  
             // Launch camera to take photo for selected contact  
             PHOTO_DIR.mkdirs();// ƬĴ洢Ŀ¼  
             Time t = new Time();
             t.setToNow();
             String year = String.valueOf(t.year);
             String month = String.valueOf(t.month+1);
             String day = String.valueOf(t.monthDay);
             String hour;
             if (t.hour<10) {
 				hour = "0" + String.valueOf(t.hour);
 			} else {
 				hour = String.valueOf(t.hour);
 			}
             String minute = String.valueOf(t.minute);
             String second = String.valueOf(t.second);
 			String filenameString = "lily_" + year + month + day + hour + minute + second + ".jpeg";
 			File outFile = new File(PHOTO_DIR, filenameString);
             final Intent intent = getTakePickIntent(outFile);  
             startActivityForResult(intent, CAMERA_WITH_DATA);  
         } catch (ActivityNotFoundException e) {  
             Toast.makeText(this, "޷ͼƬ",  
                     Toast.LENGTH_LONG).show();  
         }  
     }  
       
     public static Intent getTakePickIntent(File f) {  
         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);  
         photoUri = Uri.fromFile(f);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);  
         return intent;  
     }
 	
 	protected void doPickPhotoFromGallery() {  
         try {  
             // Launch picker to choose photo for selected contact  
             final Intent intent = getPhotoPickIntent();  
             startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);  
         } catch (ActivityNotFoundException e) {  
             Toast.makeText(this, "޷",  
                     Toast.LENGTH_LONG).show();  
         }  
     }  
       
     // װGalleryintent  
     public static Intent getPhotoPickIntent() {  
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);  
         intent.setType("image/*");
         intent.putExtra("return-data", true);  
         return intent;  
     } 
 	
     /*
 	protected void doCropPhoto(Uri file) {  
         try {  
             // galleryȥƬ  
             final Intent intent = getCropImageIntent(file);  
             startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);  
         } catch (Exception e) {  
             Toast.makeText(this, "޷",  
                     Toast.LENGTH_LONG).show();  
         }  
     }
     */
 
     /**  
     * Constructs an intent for image cropping. ͼƬ  
     */  
     /*
     public static Intent getCropImageIntent(Uri photoUri) {  
         Intent intent = new Intent("com.android.camera.action.CROP");  
         intent.setDataAndType(photoUri, "image/*");  
         intent.putExtra("crop", "true");  
         intent.putExtra("aspectX", 1);  
         intent.putExtra("aspectY", 1);  
         intent.putExtra("outputX", 80);  
         intent.putExtra("outputY", 80);  
         intent.putExtra("return-data", true);  
         return intent;  
     }
     */
     
 	
 	private boolean getIdentify() {
 		try {
 			Random random = new Random();
 			int s = random.nextInt(99999)%(90000) + 10000;
 			String username = mUserinfo.getUsername();
 			String pwd = mUserinfo.getPwd();
 			if (username == "") {
 				Toast.makeText(this, "ʺϢ", Toast.LENGTH_LONG).show();
 				finish();
 			}
 			String urlString = "http://bbs.nju.edu.cn/vd" + String.valueOf(s) + "/bbslogin?type=2&id=" + username + "&pw=" + pwd;
 			String doc = Jsoup.connect(urlString).get().toString();
 			int t = doc.indexOf("setCookie");
 			if (t == -1) {
 				return false;
 			}
 			else {
 				String tempString = doc.substring(t);
 				tempString = tempString.substring(11, tempString.indexOf(")")-1);
 				String[] tm =  tempString.split("\\+");
 				String _U_KEY = String.valueOf(Integer.parseInt(tm[1])-2);
 				String[] tm2 = tm[0].split("N");
 				String _U_UID = tm2[1];
 				String _U_NUM = "" + String.valueOf(Integer.parseInt(tm2[0]) + 2);
 				mUserinfo.setCookies("_U_KEY=" + _U_KEY + "; " + "_U_UID=" + _U_UID + "; " + "_U_NUM=" + _U_NUM + ";");
 				mUserinfo.setUsername(username);
 				mUserinfo.setPwd(pwd);
 				mUserinfo.setCode("vd" + String.valueOf(s));
 				return true;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean getPreINFO() {
 		Bundle mBundle = getIntent().getExtras();
 		urlString = mBundle.getString("url");							//reply or new thread link, get the pid and reid
 		String action = mBundle.getString("action");							//specisc the action, reply or new
 		String code = mUserinfo.getCode();
 		String cookie = mUserinfo.getCookies();
 		URL mUrl;
 		HttpURLConnection conn;
 		try {
 			if (urlString.contains("/vd")) {
 				String[] url = urlString.split("cn/");
 				urlString = "http://bbs.nju.edu.cn/" + code+ "/" + url[1];
 			} else {
 				String[] url = urlString.split("cn/");
 				urlString = "http://bbs.nju.edu.cn/" + code + "/" + url[1];
 			}
 			boardname = urlString.substring(urlString.indexOf("=")+1);
 			if (boardname.contains("&")) {
 				boardname = boardname.substring(0, boardname.indexOf("&"));
 			}
 			StringBuffer sb;
 			if (action.equals("reply")) {
 				mUrl = new URL(urlString);
 				conn = (HttpURLConnection) mUrl.openConnection();
 				if(cookie!=null && cookie.length()>0){
 	                conn.setRequestProperty("Cookie", cookie);
 	            }
 				conn.setRequestMethod("GET");
 				conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
 				conn.connect();
 				InputStream in = conn.getInputStream(); 
 	            BufferedReader reader1 = new BufferedReader(new InputStreamReader(in,"gb2312")); 
 	            String inputLine = null;
 	            sb = new StringBuffer();  
 	            while ((inputLine = reader1.readLine()) != null) {  
 	                sb.append(inputLine).append("\n");
 	                if (inputLine.contains("")) {
 						Toast.makeText(newThread.this, inputLine, Toast.LENGTH_SHORT).show();
 						newThread.this.finish();
 						return true;
 					}
 	            }
 	            reader1.close();
 	            in.close();
 	            Document doc = Jsoup.parse(sb.toString());
 	            pidString = doc.select("input[name=pid]").attr("value");
 	            if (Integer.valueOf(pidString)<1 && Integer.valueOf(reidString)<1) {
 					return false;
 				} else {
 					return true;
 				}
 			} else {
 				pidString = "0";
 				reidString = "0";
 				return true;
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean sendThread() {
 		String code = mUserinfo.getCode();
 		String cookie = mUserinfo.getCookies();
 		String contentString = ((EditText)findViewById(R.id.new_content)).getText().toString();
 		String title = ((EditText)findViewById(R.id.new_title)).getText().toString();
 		String newurlString;
 		if (quote) {
 			contentString = contentString + "\n\n    " + author + " Ĵᵽ: ";
 		}
 		try {
 			newurlString = "http://bbs.nju.edu.cn/" + code + "/bbssnd?board=" + boardname;
 			HttpPost httpRequest = new HttpPost(newurlString);
 		    ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
 		    postData.add(new BasicNameValuePair("title", title));
 		    postData.add(new BasicNameValuePair("pid", pidString));
 		    postData.add(new BasicNameValuePair("reid", reidString));
 		    postData.add(new BasicNameValuePair("signature", "1"));
 		    postData.add(new BasicNameValuePair("autocr", "on"));
 		    postData.add(new BasicNameValuePair("text", contentString));
 		    httpRequest.addHeader("Cookie", cookie);
 		    httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
 		    HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				Toast.makeText(getApplicationContext(), "ͳɹ",
 					     Toast.LENGTH_SHORT).show();
 				return true;
 			} else {
 				Toast.makeText(getApplicationContext(), "ʧܣ",
 					     Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean send() {
 		boolean success = sendThread();
 		int trycount = 3;
 		while (!success && trycount > 0) {
 			getIdentify();
 			success = sendThread();
 			trycount --;
 		}
 		if (!success) {
 			Toast.makeText(this, "ƺе...", Toast.LENGTH_SHORT);
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			switch (msg.what) {
 				case 0:
 					waitDialog.dismiss();
 					if (!picURL.equals("")) {
 			        	String oldString = ((EditText)findViewById(R.id.new_content)).getText().toString();
 			            ((EditText)findViewById(R.id.new_content)).setText(oldString + picURL);
 			            Toast.makeText(newThread.this, "ͼƬϴɹ", Toast.LENGTH_SHORT);
 					} else {
 						Toast.makeText(newThread.this, "ͼƬϴʧܣ", Toast.LENGTH_SHORT);
 					}
 					break;
 				default:
 					waitDialog.dismiss();
 					super.handleMessage(msg);
 			}
 		}
 	};
 	
 	private ExecutorService executorService = Executors.newFixedThreadPool(5);
 	
 	public void uploadFile2Svr() {
 		executorService.submit(new Runnable() {
 			public void run() {
 	    		Message msg = new Message();
 	    		msg.what = 0;
 	    		picURL = "";
 	    		photoUri = null;
 		        HttpClient client;
 		        BasicHttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
 			    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 			    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 		        client = new DefaultHttpClient(httpParameters);
 				client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 				
 			    
 				String url = "http://bbs.nju.edu.cn/" + mUserinfo.getCode() + "/bbsdoupload";
 				
 				HttpPost imgPost = new HttpPost(url);
 				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 				File file = new File(photoPath);
 				try {
 					reqEntity.addPart("up", new FileBody(file));
 					reqEntity.addPart("exp", new StringBody("", Charset.forName("GB2312")));
 					reqEntity.addPart("ptext", new StringBody("", Charset.forName("GB2312")));
 					reqEntity.addPart("board", new StringBody(boardname, Charset.forName("GB2312")));
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				imgPost.setEntity(reqEntity);
 				imgPost.addHeader("Cookie", mUserinfo.getCookies());
 				imgPost.addHeader(reqEntity.getContentType());
 				
 				for (int i = 0; i < imgPost.getAllHeaders().length; i++) {
 					Log.d(TAG, imgPost.getAllHeaders()[i].toString());
 				}
 				Log.d(TAG, "executing request " + imgPost.getRequestLine());
 		
 				String result;
 			    HttpResponse httpResponse;
 				try {
 					Log.d(TAG, "before excuting...");
 					httpResponse = client.execute(imgPost);
 					Log.d(TAG, "after excuting...");
 					if (httpResponse.getStatusLine().getStatusCode() == 200) {
 			            result = EntityUtils.toString(httpResponse.getEntity());
 			            result = result.substring(result.indexOf("url=")+2, result.indexOf(">")-1);
 			            String fileNum = result.substring(result.indexOf("file=")+5, result.indexOf("&name"));
 			            url = "http://bbs.nju.edu.cn/" + mUserinfo.getCode() + "/bbsupload2?board=" + boardname + "&file=" + fileNum + "&name=" + filenameString + "&exp=&ptext=";
 			            Log.i(TAG, url);
 			            HttpGet uploadGet = new HttpGet(url);
 			            uploadGet.addHeader("Cookie", mUserinfo.getCookies());
 			            httpResponse = client.execute(uploadGet);
 			            if (httpResponse.getStatusLine().getStatusCode() == 200) {
 							result = EntityUtils.toString(httpResponse.getEntity());
 							if (result.contains("name=")) {
 								String tempString = result.substring(result.indexOf("name=")+5, result.length()-3);
 								picURL = "\nhttp://bbs.nju.edu.cn/file/" + boardname + "/" + tempString +"\n";
 								Log.i(TAG, "ַ" + picURL);
 								System.gc();
 								File temp = new File(TEMP_PHOTO_DIR, filenameString);
 					            if (temp.exists())
 					            	temp.delete();
 							}
 						}
 					}
 					else {
 			            Log.d(TAG, "Fail");
 			            System.gc();
 					} 
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 					handler.sendMessage(msg);
 				} catch (ConnectTimeoutException e) {
 					Toast.makeText(newThread.this, "ƺЩ⣬ҪϴղŵͼƬ뵽ýѡ", Toast.LENGTH_LONG);
 					picURL = "";
 					handler.sendMessage(msg);
 				} catch (IOException e) {
 					e.printStackTrace();
 					handler.sendMessage(msg);
 				}
 				handler.sendMessage(msg);
 			}
 		});
 	}
 }
 
