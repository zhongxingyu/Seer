 package killheart.knay.trombinoscope;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 /**
  * @author David et Jonathan
  *
  * Classe permet de gérer une caméra sous Android.
  * Cette classe permet de d'afficher la caméra à l'écran 
  * et de prendre des photos. Les photos sont enregistrées
  * dans le dossier de l'application avec le nom donné en 
  * paramètre.
  * 
  * @todo Ajouter le un attribut pour le chemin de la photo
  * @todo Lancer l'activity qui propose de reprendre une photo ou non
  * @todo Ajouter un accesseur pour savoir si la caméra marche ou non (is preview)
  * @todo Créer une Image android à partir de la photo prise
  */
 public class CameraActivity extends Activity implements SurfaceHolder.Callback {
 	// ----- ----- Les classes Android ----- ----- 
 	private Camera camera = null;                           //< La caméra Android
 	private SurfaceView surfaceCamera = null;               //< La view sur laquelle on va afficher une preview de la caméra
 	private ImageButton boutonPrendrePhoto = null;          //< Le bouton qui permet de prendre une photo
 	private Camera.PictureCallback callBackPhoto = null;    //< La fonction de callback lors de la prise d'une photo    
 	
 	// ----- ----- Les classes et variables classiques ----- ----- 
 	private FileOutputStream stream = null;                 //< Le flux vers le fichier ou sera enregistrée la photo
 	private Boolean isPreview;                              //< Booleen permettant de savoir si la caméra est en mode preview ou non (pause de l'application)
 	private String dossierPhoto = null;                     //< Le dossier ou stocker les photos
 	private String path = null;                             //< Le chemin vers le fichier
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Cette fonction est appelée à la création de l'activité Android.
 	 * Elle initialise les paramètres de la classe et affiche la caméra.
 	 * 
 	 * @param savedInstanceState L'état de l'application.
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState); //< Appel au super-constructeur
 		
 		// On met l'application en plein écran et sans barre de titre
 		getWindow().setFormat(PixelFormat.TRANSLUCENT);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_camera); //< On applique le layout prévue pour la caméra
 		
 		boutonPrendrePhoto = (ImageButton) findViewById(R.id.boutonPhoto); //< On recherche le bouton pour prendre la photo pour lui appliquer un listener
 		
 		// Listener du bouton pour prendre une photo
 		boutonPrendrePhoto.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				PrendrePhoto(); //< Si on clic sur le bouton on prend la photo
 			}
 		});
 
 		surfaceCamera = (SurfaceView) findViewById(R.id.surfaceViewCamera); //< On recherche la surface prévue pour heberger la prévisualisation de la caméra
 
 		InitialiserCamera(); //< Initialisation de la caméra !
 	}
 
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Permet d'initialiser les paramètres de la caméra.
 	 */
 	@SuppressWarnings("deprecation") //< On enleve les warnings pour deprecated car setType() nécessaire pour android 3.0
     public void InitialiserCamera() {
 		isPreview = false; //< Au démarrage la caméra n'est pas en mode preview
 		
 		dossierPhoto = "photos"; //< On définit le répertoire pour stocker les photos
 		
 		path = getIntent().getExtras().getString("url"); //< On récupère le l'id de lélève passé à l'activité pour definir le nom du fichier de sortie
 		
 		surfaceCamera.getHolder().addCallback(this); //< On attache les retours du holder à notre activité
 
 		surfaceCamera.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //< On spécifie le type du holder en mode SURFACE_TYPE_PUSH_BUFFERS
 		
 		// On initialise la callback pour la prise de photo
 		callBackPhoto = new Camera.PictureCallback() {
 			/**
 			 * @author David et Jonathan
 			 * 
 			 * Permet de définir le traitement à faire après une prise de photo : 
 			 * sauvegarde la photo dans le fichier ciblé par la variable stream.
 			 * 
 			 * @param data L'image sous forme de tableau d'octet.
 			 * @param camera La caméra ayant pris la photo.
 			 */
 			public void onPictureTaken(byte[] data, Camera camera) {
 				// Si il y a des datas, on essaie d'enregistrer l'image sur un stream déjà renseigné
 				if (data != null) {
 					try {
 						if (stream != null) {
 							Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length); //< On crée une image a partir d'un array de byte
 							bmp = Bitmap.createScaledBitmap(bmp, 150, 120, true); //< On réduit la taille de l'image
 							
 							Matrix mat = new Matrix(); //< On crée une matrice pour pivoter l'image
 					        mat.postRotate(90); //< On rotate la matrice
 					        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true); //< On applique la matrice à l'image
 							
 							ByteArrayOutputStream baos = new ByteArrayOutputStream(); //< On crée un byteouput pour recupérer les bytes de la bmp
 							bmp.compress(CompressFormat.PNG, 0, baos); //< On met les octets de l'image pivoter et redimmensionner dans un tableau d'octet
 							byte[] bitmapdata = baos.toByteArray(); 
 							
 							stream.write(bitmapdata); //< Ecriture des datas dans le buffer
 							stream.flush();           //< Flush pour vider le buffer
 							stream.close();           //< On ferme le fichier
 							
 							setResult(getIntent().getExtras().getInt("idEleve"), getIntent()); //< On renvoie comme resultat l'id de l'élève
 							finish();  //< On arrête la caméra
 						}
 					} 
 					catch (Exception e) {
 						// Si une exception à lieu on affiche un Toast d'erreur
 						Toast.makeText(getApplicationContext(), "Erreur écriture fichier : " + e.getMessage(), Toast.LENGTH_LONG).show();
 					}
 					camera.startPreview(); //< On relance la caméra
 				}
 			}
 		};
 	}
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Permet de réagir lorsqu'il y a eu un changement d'état sur la surface (à la création 
 	 * de la surface, etc.).
 	 * 
 	 * @param holder Le holder de la surface.
 	 * @param format Le nouveau PixelFormat de la surface.
 	 * @param width La largeur de la surface de la surface.
 	 * @param height La hauteur de la surface de la surface.
 	 */
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		// Si le mode preview est lancé alors on le stop, pour éviter tout problèmes
 		if (isPreview) {
 			camera.stopPreview();
 		}
 		
 		Camera.Parameters parameters = camera.getParameters(); //< On récupère les paramètres de la caméra
 		parameters.setRotation(90); //< On dit à la caméra qu'elle n'est pas droite pour avec une photo dans le bon sens
 
		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes(); 
		Camera.Size previewSize = sizes.get(0);
		parameters.setPreviewSize(previewSize.width, previewSize.height); //< On change la taille de la prévisualisation
 
 		camera.setParameters(parameters); //< On applique nos nouveaux paramètres
 
 		try {
 			camera.setPreviewDisplay(surfaceCamera.getHolder()); //< On redirige le preview de la caméra vers notre surface
 		} catch (IOException e) {
 			// Si une exception à lieu on affiche un Toast d'erreur
 			Toast.makeText(getApplicationContext(), "Erreur prévisualisation caméra : " + e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 
 		camera.startPreview(); //< On lance la preview
 
 		isPreview = true; //< La preview est démarrée, on le signale en actualisant la variable
 	}
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Quand la surface (où on affiche la preview) est créée 
 	 * on démarre la caméra.
 	 * 
 	 * @param holder Le holder de la surface créée.
 	 */
 	public void surfaceCreated(SurfaceHolder holder) {
 		// On prend le controle de la camera
 		if (camera == null) {
 			camera = Camera.open();
 		}
     }
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Fonction appelée lors de la desctruction du holder.
 	 * La fonction arrête la prévisualisation de la caméra
 	 * et la libère.
 	 * 
 	 * @param holder Le holder de la surface détruite.
 	 */
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// On arrête la caméra et on la libère
 		if (camera != null) {
 			camera.stopPreview();
 			isPreview = false;
 			camera.release();
 		}
 	}
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Permet de définir le comportement de l'activité lorsque l'utilisateur
 	 * retourne dessus (après avoir quitté le focus de celle-ci).
 	 */
 	public void onResume() {
 		super.onResume();
 		
 		camera = Camera.open(); //< On relance la caméra
 		isPreview = true;
 	}
 
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Permet de définir le comportement de l'activité lorsque l'utilisateur quitte
 	 * l'application (quitte le focus).
 	 */
 	public void onPause() {
 		super.onPause();
 
 		// Si la caméra était lancée on l'arrête et on la libère 
 		if (camera != null) {
 			camera.release();
 			camera = null;
 			isPreview = false;
 		}
 	}
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Permet de changer le nom du dossier ou l'on va enregistrer la photo.
 	 * 
 	 * @param p Le path que vous souhaitez mettre (sans l'extension).
 	 */
 	public void setPath(String p) {
 		if (p != null)
 			path = p;
 		else
 			path = "sans_nom";
 	}
 	
 	/**
 	 * @author David et Jonathan
 	 * 
 	 * Fonction permettant de prendre une photo, et de l'enregistrer dans le dossier 
 	 * de l'application. L'enregistre dans /trombiscol/photos/nom.jpg. 
 	 */
 	public void PrendrePhoto() {
 		try {
 			AndroidTree.CreateFolder(".", dossierPhoto);  //< On créait le dossier photo s'il ne l'est pas !
 
 			stream = new FileOutputStream(path); //< Ouverture du flux pour la sauvegarde
 			camera.takePicture(null, callBackPhoto, callBackPhoto); //< On prend une photo avec la caméra
 		} 
 		catch (Exception e) {
 			// Si une exception à lieu on affiche un Toast d'erreur
 			Toast.makeText(getApplicationContext(), "Erreur prise de photo : " + e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 	}	
 }
