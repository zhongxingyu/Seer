 package net.multassociais.mobile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.text.SpannableString;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MultasSociaisActivity extends Activity {
 
 	private static final int TAKE_PICTURE_REQUEST_CODE = 666; // \,,/
 	private static final int ACTIVITY_SELECT_IMAGE = 777;
 	private String imageFilePath;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		imageFilePath = "";
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Activity.RESULT_OK) {
 			ImageView thumbnailImageView = (ImageView) findViewById(R.id.thumbnail);
 			switch (requestCode) {
 			case TAKE_PICTURE_REQUEST_CODE:
 				File f = new File(imageFilePath);
 				if (f.exists()) {
 					try {
 						reduzEComprimeBitmap(f);
 						Uri contentUri = Uri.fromFile(f);
 						Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
 						thumbnailImageView.setImageBitmap(bm);
 				        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 				        mediaScanIntent.setData(contentUri);
 				        this.sendBroadcast(mediaScanIntent);
 				        showToast("Descreva a multa!");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						imageFilePath = "";
 						thumbnailImageView.setImageBitmap(null);
 						e.printStackTrace();
 					}
 				}
 				break;
 			case ACTIVITY_SELECT_IMAGE:
 				Uri selectedImage = data.getData();
 				String[] filePathColumn = {MediaStore.Images.Media.DATA};
 				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
 				cursor.moveToFirst();
 				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
 				String filePath = cursor.getString(columnIndex);
 				cursor.close();
 				thumbnailImageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
 				imageFilePath = filePath;
 				showToast("Descreva a multa!");
 			}
 		}
 	}
 
 	private File criaArquivoDeImagem() {
 		File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MultasSociais");
 		storageDir.mkdirs();
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		String imageFileName = "multa_" + timeStamp;
 		File image = new File(storageDir, imageFileName + ".jpg");
 		imageFilePath = image.getAbsolutePath();
 		return image;
 	}
 
 	public void bateFoto(View view) {
 		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(criaArquivoDeImagem())); //imageFilePath defined inside createImageFile()
 		startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
 	}
 
 	public void selecionaFotoDaGaleria(View view) {
 		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 		startActivityForResult(i, ACTIVITY_SELECT_IMAGE); 
 	}
 
 	// TODO: catch exception
 	public void enviaMulta(View view) throws IOException {
 		
 		//verify if there's an image and description to be sent
 		String descricao = ((EditText) findViewById(R.id.edt_descricao)).getText().toString();
 		if (imageFilePath.equals("")) {
 			showToast("Selecione uma imagem ou use a cmera!");
 			return;
 		}
 		if (descricao.equals("")) {
 			showToast("Adicione uma descrio!");
 			return;
 		}
 		
 		File f = new File(imageFilePath);
 		Date modifiedDate = new Date(f.lastModified());
 		String year = new SimpleDateFormat("yyyy").format(modifiedDate);
 		String month = new SimpleDateFormat("MM").format(modifiedDate);
 		String day = new SimpleDateFormat("dd").format(modifiedDate);
 		String hour = new SimpleDateFormat("HH").format(modifiedDate);
 		String minute = new SimpleDateFormat("mm").format(modifiedDate);
 		String placa = ((EditText) findViewById(R.id.edt_placa)).getText().toString();
 		
 		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 		entity.addPart("api_id", new StringBody("908237202"));
 		entity.addPart("api_secret", new StringBody("14feefc9725729307649b526bd83x11"));
 		entity.addPart("multa[data_ocorrencia(1i)]", new StringBody(year));
 		entity.addPart("multa[data_ocorrencia(2i)]", new StringBody(month));
 		entity.addPart("multa[data_ocorrencia(3i)]", new StringBody(day));
 		entity.addPart("multa[data_ocorrencia(4i)]", new StringBody(hour));
 		entity.addPart("multa[data_ocorrencia(5i)]", new StringBody(minute));
		entity.addPart("multa[placa]", new StringBody(placa));
 		entity.addPart("multa[foto]", new FileBody(f));
 		entity.addPart("multa[video]", new StringBody(""));
		entity.addPart("multa[descricao]", new StringBody(descricao));
 
 		WebServiceCallTask task = new WebServiceCallTask();
 		task.execute(entity);
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.about_menu_item:
         	mostraDialogoSobre();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     public void mostraDialogoSobre(){
     	  final TextView message = new TextView(this);
     	  final SpannableString s = new SpannableString(this.getText(R.string.txt_sobre));
     	  Linkify.addLinks(s, Linkify.WEB_URLS);
     	  message.setText(s);
     	  message.setMovementMethod(LinkMovementMethod.getInstance());
 
     	  AlertDialog about = new AlertDialog.Builder(this)
     	  	.setTitle(R.string.app_name)
     	  	.setIcon(android.R.drawable.ic_dialog_info)
     	  	.setPositiveButton(this.getString(android.R.string.ok), null)
     	  	.setNeutralButton("Deixe sua opinio!", new DialogInterface.OnClickListener() {
     	  		public void onClick(DialogInterface dialog, int id) {
     	  			Intent intent = new Intent(Intent.ACTION_VIEW);
     	  			intent.setData(Uri.parse("market://details?id=net.multassociais.mobile"));
     	  			startActivity(intent);
              }
     	  	})
     	  	.setView(message)
     	  	.create();
     	  about.show();
       }
     
 	private void showToast(String string) {
 		Toast feedback;
 		feedback = Toast.makeText(MultasSociaisActivity.this, string, Toast.LENGTH_SHORT);
 		feedback.show();
 	}
 
 	private void reduzEComprimeBitmap(File imageFile) throws FileNotFoundException, IOException {
 		Uri contentUri = Uri.fromFile(imageFile);
 		Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
 		Options opt = new Options();
 		opt.inPurgeable = true;
 		opt.inScaled = true;
 		if (bm.getWidth() > 2048 || bm.getHeight() > 2048) {
 			opt.inSampleSize = 4;
 		} else if (bm.getWidth() > 1024 || bm.getHeight() > 1024) {
 			opt.inSampleSize = 2;
 		}
 		Bitmap result = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opt);
 		FileOutputStream newFile = new FileOutputStream(imageFile);
 		result.compress(Bitmap.CompressFormat.JPEG, 85, newFile);
 	}
 
 	private class WebServiceCallTask extends AsyncTask<MultipartEntity, Void, Integer> {
 		ProgressDialog dialog;
 		
 		@Override
 		protected void onPreExecute() {
 			dialog = new ProgressDialog(MultasSociaisActivity.this);
 			
 			super.onPreExecute();
 			dialog.setMessage("Enviando...");
 			dialog.show();
 		}
 
 		@Override
 		protected Integer doInBackground(MultipartEntity... entity) {
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpContext localContext = new BasicHttpContext();
 			HttpPost httpPost = new HttpPost("http://testes.multassociais.net/api");
 
 			httpPost.setEntity(entity[0]);
 			HttpResponse response;
 			try {
 				response = httpClient.execute(httpPost, localContext);
 				int statusCode = response.getStatusLine().getStatusCode();
 				return statusCode;
 				
 			} catch (ClientProtocolException e) {
 				showToast("Falhou");
 				cancel(true);
 				return null;
 			} catch (IOException e) {
 				showToast("Falhou");
 				cancel(true);
 				return null;
 			}
 		}
 		
 		@Override
 		protected void onCancelled() {
 			dialog.dismiss();
 			super.onCancelled();
 		}
 		
 		@Override
 		protected void onPostExecute(Integer statusCode) {
 			super.onPostExecute(statusCode);
 			
 			if (statusCode >= 200 && statusCode < 300) {
 				showToast("Sucesso");
 			}
 			else {
 				showToast("Falhou!");
 			}
 			dialog.dismiss();
 		}
 	}
 
 }
