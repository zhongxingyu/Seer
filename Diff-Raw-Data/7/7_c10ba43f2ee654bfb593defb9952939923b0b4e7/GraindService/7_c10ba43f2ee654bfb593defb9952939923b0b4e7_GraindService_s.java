 package de.graind.client.service;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 
 import de.graind.client.model.PicasaImageBase;
 
 public interface GraindService extends RemoteService {
 
   /**
    * Save the pictures per month.
    * 
    * @param images
    *          An array of 12 PicasaImages, one per month
    */
   void saveMonthlyPictureSelection(String username, PicasaImageBase[] images);
 
   /**
    * Remove all saved monthly pictures for the given username.
    * 
    * @param username
    */
   void deleteMonthlyPictureSelection(String username);
 
   /**
    * Returns a PicasaImageBase for the given username and month.
    * 
    * @param username
    * @param month
    * @return
    */
   PicasaImageBase getImageForMonth(String username, int month);
 
   /**
    * Returns an array of the 12 images for a given username.
    * 
    * @param username
    * @return
    */
   PicasaImageBase[] getAllImages(String username);
 }
