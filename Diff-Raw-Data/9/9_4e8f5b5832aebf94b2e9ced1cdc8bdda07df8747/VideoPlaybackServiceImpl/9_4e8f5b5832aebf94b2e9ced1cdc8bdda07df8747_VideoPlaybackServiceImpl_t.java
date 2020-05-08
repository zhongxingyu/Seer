 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.services.playback;
 
 import com.team33.entities.LoginToken;
 import com.team33.entities.dao.VideoAccessDao;
 import com.team33.services.exception.AccountNotActivatedException;
 import com.team33.services.exception.DataAccessException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Caleb
  */
@Service
 public class VideoPlaybackServiceImpl implements VideoPlaybackService{
 
     @Autowired
     private VideoAccessDao videoAccessDao;
     
     public void setVideoAccessDao(VideoAccessDao videoAccessDao) {
         this.videoAccessDao = videoAccessDao;
     }
     
     private VideoAccessDao getVideoAccessDao() {
         return videoAccessDao;
     }
     
     /**
      * Takes the unique id of a login token to determine whether an account is
      * active
      *
      * @param uuid
      * @return boolean
      * @throws AccountNotActivatedException
      */
     private boolean isActivated(int uuid) throws AccountNotActivatedException {
         try {
             LoginToken loginToken = this.getVideoAccessDao().getLoginToken(uuid);
 
             if (!loginToken.getAccount().getActivated()) {
                 throw new AccountNotActivatedException("Account Inactive");
             }
         } catch (DataAccessException dae) {
             dae.printStackTrace();
             return false;
         } catch (AccountNotActivatedException ae) {
             ae.printStackTrace();
             return false;
         }
         return true;
     }
     
     
     @Override
     @Transactional
     public void saveInformation(int loginToken, int orderId, int videoId, int seconds) {
         try {
             if (!isActivated(loginToken)){
                 return;
             }
         } catch (AccountNotActivatedException ex) {
             return;
         }
         // Save Stuff
     }
 
     @Override
     @Transactional
     public int getInformation(int loginToken, int orderId, int videoId) {
         try {
             if (!isActivated(loginToken)){
                 return -1;
             }
         } catch (AccountNotActivatedException ex) {
             return -1;
         }
         throw new UnsupportedOperationException("Not supported yet.");
         //Get Stuff
     }
 
     @Override
     @Transactional
     public String getVideoFileInformation(int loginToken, int videoId) {
         try {
             if (!isActivated(loginToken)){
                 return "";
             }
         } catch (AccountNotActivatedException ex) {
             return "";
         }
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
