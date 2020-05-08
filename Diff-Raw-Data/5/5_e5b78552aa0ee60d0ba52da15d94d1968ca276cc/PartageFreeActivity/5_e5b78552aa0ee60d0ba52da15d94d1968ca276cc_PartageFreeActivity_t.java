 package fr.blasters.partagefree;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
import java.net.URISyntaxException;

 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import org.apache.commons.net.ftp.*;
 
 public class PartageFreeActivity extends Activity {
 	
 	
 	EditText email;
 	EditText password;
 	TextView fichierTxt;
 	Uri fichierUri;
 	String destination;
 	public static final String PREFS_NAME = "PartageFreePrefs";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // apparament ça se met au début
         setContentView(R.layout.main);
         
         fichierTxt = (TextView) findViewById(R.id.textSrcUri);
         TextView destTxt = (TextView) findViewById(R.id.textDestPath);
         Button up = (Button) findViewById(R.id.buttonUp);
         Button save = (Button) findViewById(R.id.buttonSave);
         
         loadPreferences();
         
         up.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		savePreferences();
         		uploadFile();
         	}
         });
         
         save.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View v) {
         		savePreferences();
         	}
         });
         
        // Après le load, on regarde si on a lancé l'appli via le menu share
         
       Intent intent = getIntent();
       Bundle extras = intent.getExtras();
       String action = intent.getAction();
 
       // if this is from the share menu
       if (Intent.ACTION_SEND.equals(action)) {   
     	  if (extras.containsKey(Intent.EXTRA_STREAM)) {
     		  // Get resource path
     		  fichierUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 		      fichierTxt.setText(fichierUri.toString());
		      destination = "/"+ PartageFreeActivity.sanitizeFilename(fichierUri.getLastPathSegment());
 		      destTxt.setText(destination);
     	  }
       }
         
     }
     
     
     private void loadPreferences() {
     	
     	email = (EditText) findViewById(R.id.editMail);
         password = (EditText) findViewById(R.id.editPwd);
     	
     	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
     	email.setText(settings.getString("email", ""));
 		password.setText(settings.getString("password", ""));
 		
 	}
 
 
 	private void savePreferences() {
     	
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	    SharedPreferences.Editor editor = settings.edit();
 	    editor.putString("email", email.getText().toString());
 	    editor.putString("password", password.getText().toString());
 
 	    // Commit the edits!
 	    editor.commit();
 		
     	Toast.makeText(this, "Modifications enregistrées", Toast.LENGTH_SHORT).show();
     }
     
     private void uploadFile() {
     	
     	loadPreferences();
     	
     	printNotif("Upload...", "PartageFree", "Début de l'upload");
     	
     	FTPClient ftp = new FTPClient();
     	
         try
         {
             int reply;
             ftp.connect("dl.free.fr");
 
             // After connection attempt, you should check the reply code to verify
             // success.
             reply = ftp.getReplyCode();
 
             if (!FTPReply.isPositiveCompletion(reply))
             {
                 ftp.disconnect();
                 printNotif("Erreur !","PartageFree","Erreur au connect()");
                 //TODO: Afficher un message d'erreur
                 
             }
         }
         catch (IOException e)
         {
             if (ftp.isConnected())
             {
                 try
                 {
                     ftp.disconnect();
                 }
                 catch (IOException f)
                 {
                     // do nothing
                 }
             }
             printNotif("Erreur !","PartageFree","Exception au connect()");
             e.printStackTrace();
             // System.exit(1);
             //TODO: Afficher un message d'erreur
         }
 
     	// La, on est connecté
         printNotif("Connecté","PartageFree","Là, on est connecté");
     	
         
         try
         {
             if (!ftp.login(email.getText().toString(), password.getText().toString()))
             {
                 ftp.logout();
                 printNotif("Erreur !","PartageFree","Erreur de login");
                 return;
             }
 
             ftp.setFileType(FTP.BINARY_FILE_TYPE);
             
             ftp.enterLocalPassiveMode();
 
             printNotif("Envoi","PartageFree","Envoi du fichier");
             // Upload
             InputStream input = this.getContentResolver().openInputStream(fichierUri);;
                         
             if (ftp.storeFile(destination, input))
             	printNotif("Done","PartageFree","Envoi terminé");
             else
             	printNotif("Erreur !","PartageFree","Envoi échoué");
             
             input.close();
           
             ftp.noop(); // check that control connection is working OK
 
             ftp.logout();
         }
         catch (FileNotFoundException e)
         {
         	printNotif("Erreur !","PartageFree","Fichier non trouvé");
             e.printStackTrace();
         }
         catch (FTPConnectionClosedException e)
         {
             // error = true;
             //System.err.println("Server closed connection.");
         	printNotif("Erreur !","PartageFree","Server closed connection");
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // error = true;
         	printNotif("Erreur !","PartageFree","Exception à l'upload");
             e.printStackTrace();
         }
         finally
         {
         	// si on a eu une Exception, on ferme na connexion
             if (ftp.isConnected())
             {
                 try
                 {
                     ftp.disconnect();
                 }
                 catch (IOException f)
                 {
                     // do nothing
                 }
             }
         }
 
     }
     
     private void printNotif(String entete, String titre, String message) {
     	//On crée un "gestionnaire de notification"
     	NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);        
  
     	//On crée la notification
     	//Avec son icône et son texte défilant (optionnel si l'on ne veut pas de texte défilant on met cet argument à null)
     	Notification notification = new Notification(R.drawable.ic_launcher, entete, System.currentTimeMillis());  
  
     	//Intent intent = new Intent(this, NotificationViewer.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);
     	
         //On configure notre notification avec tous les paramètres que l'on vient de créer
         notification.setLatestEventInfo(this, titre, message, contentIntent);
         
         //Enfin on ajoute notre notification et son ID à notre gestionnaire de notification
         notificationManager.notify(5487354, notification);
     }
     
 	/**
 	 * Retourne un objet String qui contient une version compatible avec dl.free.fr du nom de fichier entré
 	 * @param name Le mom de fichier à rendre compatible
 	 * @return Le nom de fichier rendu compatible
 	 * @author Gilles Maurer
 	 * Fonction récupérée dans le programme DLUploadClient de Gilles Maurer
 	 */
 	public static String sanitizeFilename(String name)
 	{
 		return name.replaceAll("[^a-zA-Z0-9\\.\\(\\)\\-\\_\\[\\]]", "_");
 	}
     
 }
