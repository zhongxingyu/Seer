 package com.level42.mixit.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Looper;
 import android.util.Log;
 
 import com.google.zxing.common.BitMatrix;
 import com.level42.mixit.BuildConfig;
 import com.level42.mixit.R;
 import com.level42.mixit.exceptions.TechnicalException;
 import com.level42.mixit.models.Talk;
 
 /**
  * Classe utilitaire.
  */
 public final class Utils {
 
     /**
      * Tag des logs.
      */
     public static final String LOGTAG = "[MIXIT]";
 
     /**
      * On masque le constructeur par défaut.
      */
     private Utils() {
     }
     
     /**
      * Contrôle si le thread courant est le thread principal.
      */
     public static void checkOnMainThread() {
         if (BuildConfig.DEBUG && Thread.currentThread() != Looper.getMainLooper().getThread()) {
             throw new IllegalStateException("This method should be called from the Main Thread");
         }
     }
 
     /**
      * Retourne une image bitmap à partir d'une URL.
      * @param imageUrl URL de l'image
      * @return Bitmap
      * @throws TechnicalException
      */
     public static Bitmap loadBitmap(final String imageUrl) throws TechnicalException {
         try {
             return BitmapFactory.decodeStream((InputStream) new URL(imageUrl)
                     .getContent());
         } catch (MalformedURLException e) {
             throw new TechnicalException(e.getMessage(), e);
         } catch (IOException e) {
             throw new TechnicalException(e.getMessage(), e);
         }
     }
     
     /**
      * Transforme des chaines de caractère dates : 2013-04-25T09:30:00.000+02:00
      * en objet date
      * @param dateStr chaines de caractère date
      * @return Objet date
      */
     public static Date formatWsDate(final String dateStr) {
         try {
             if (dateStr == null) {
                 return null;
             }
             //2013-04-25T09:30:00.000+02:00
             SimpleDateFormat formatOrigin = new SimpleDateFormat(
                     "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
             return formatOrigin.parse(dateStr);
         } catch (ParseException e) {
             Log.w(Utils.LOGTAG, "Date format exception");
             return null;
         }
     }
     
     /**
      * Génère une imlage à partir d'une matrice QRCode
      * @param matrix Matrice du QRCode
      * @return Image
      */
     public static Bitmap generateImageFromQRCode(BitMatrix matrix) 
     {
         int width = matrix.getWidth(); 
         int height = matrix.getHeight(); 
 
         Config conf = Bitmap.Config.RGB_565;
         Bitmap bmp = Bitmap.createBitmap(width, height, conf);
             
         for (int x = 0; x < width; x++) 
         {
             for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
             }
         }
         return bmp;
     }
     
     /**
      * 
      * @param talk
      * @return
      */
     public static String getPeriodeSession(final Talk talk, final Context context) {
         
         Date dateDebut = Utils.formatWsDate(talk.getStart());
         Date dateFin = Utils.formatWsDate(talk.getEnd());
         
         if (dateDebut == null || dateFin == null) {
             return null;
         } else {
             SimpleDateFormat formatJour = new SimpleDateFormat("dd/MM");
             SimpleDateFormat formatHeure = new SimpleDateFormat("HH:mm");
             
             return String.format(context.getResources().getString(R.string.label_duree_session),
                         formatJour.format(dateDebut),
                         formatHeure.format(dateDebut),
                         formatHeure.format(dateFin)
                     );
         }
     }
 }
