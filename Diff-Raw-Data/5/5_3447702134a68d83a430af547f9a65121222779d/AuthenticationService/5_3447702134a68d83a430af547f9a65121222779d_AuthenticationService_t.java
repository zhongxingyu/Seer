 package com.gc.irc.server.service.impl;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.xml.sax.SAXException;
 
 import com.gc.irc.common.abs.AbstractLoggable;
 import com.gc.irc.common.entity.IRCUser;
 import com.gc.irc.common.protocol.item.IRCMessageItemPicture;
 import com.gc.irc.common.utils.IOStreamUtils;
 import com.gc.irc.common.utils.IOUtils;
 import com.gc.irc.server.model.UserInformations;
 import com.gc.irc.server.service.api.IAuthenticationService;
 import com.gc.irc.server.service.api.IUserPictureService;
 import com.gc.irc.server.service.utils.UserInformationScanner;
 
 /**
  * Singleton class use for login and register all the users.
  * 
  * @author gcauchis
  * 
  */
 @Service("authenticationService")
 @Scope("singleton")
 public class AuthenticationService extends AbstractLoggable implements IAuthenticationService {
 
     /** The last id. */
     private int lastId;
 
     /** The path fichier. */
     private final String pathFichier = "auth.xml";
 
     /** The user picture service. */
     @Autowired
     private IUserPictureService userPictureService;
 
     /** The list users. */
     private Map<Integer, UserInformations> users;
 
     /**
      * Read the Users data.
      */
     private AuthenticationService() {
         getLog().debug("Read the Users data.");
         try {
             final UserInformationScanner informationScanner = new UserInformationScanner(pathFichier);
             setLastId(informationScanner.getLastId());
             users = informationScanner.getListUserInfomation();
             getLog().debug("End init auth.");
         } catch (final ParserConfigurationException e) {
             getLog().warn("Fail to parse xml.", e);
         } catch (final SAXException e) {
             getLog().warn("Fail to parse xml.", e);
         } catch (final IOException e) {
             getLog().warn("Fail to read xml file. If file didn't exist yet, don't worry with this error", e);
         }
        if (users == null) {
        	users = new HashMap<Integer, UserInformations>();
        }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.gc.irc.server.auth.AuthentificationInterface#addUser(java.lang.String
      * , java.lang.String, java.lang.String)
      */
     @Override
     public boolean addUser(final String login, final String password, final String nickname) {
         if (userLoginExist(login)) {
             getLog().info("Login already exist");
             return false;
         }
         final int newId = getNewId();
         users.put(newId, new UserInformations(newId, nickname, login, password));
         saveModification();
         return true;
     }
 
     /**
      * Generate xml code.
      * 
      * @return xml code in a String.
      */
     private String generateXML() {
         String result = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\" ?>\n\n";
         result += "<IRCUsers lastId=\"" + lastId + "\">\n";
         for (final Map.Entry<Integer, UserInformations> entry : users.entrySet()) {
             result += entry.getValue().toStringXML("\t");
         }
 
         result += "</IRCUsers>\n";
 
         return result;
     }
 
     /**
      * Gets the new id.
      * 
      * @return the new id
      */
     private int getNewId() {
         lastId++;
         return lastId;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.gc.irc.server.auth.AuthentificationInterface#getUser(int)
      */
     @Override
     public UserInformations getUser(final int id) {
         return users.get(id);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.gc.irc.server.auth.AuthentificationInterface#logUser(java.lang.String
      * , java.lang.String)
      */
     @Override
     public IRCUser logUser(final String login, final String password) {
         for (final Map.Entry<Integer, UserInformations> entry : users.entrySet()) {
             if (entry.getValue().getLogin().equals(login) && entry.getValue().getPassword().equals(password)) {
                 if (entry.getValue().isConnected()) {
                     return null;
                 }
                 return entry.getValue().getUser();
             }
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.gc.irc.server.auth.IAuthentificationService#saveModification()
      */
     @Override
     public void saveModification() {
         synchronized (users) {
             IOUtils.writeFile(pathFichier, generateXML());
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.gc.irc.server.auth.AuthentificationInterface#sendUsersPicture(java
      * .io.ObjectOutputStream)
      */
     @Override
     public synchronized void sendUsersPicture(final ObjectOutputStream outObject) {
         IRCMessageItemPicture messagePicture;
         for (final Map.Entry<Integer, UserInformations> entry : users.entrySet()) {
             if (entry.getValue().isConnected() && entry.getValue().hasPictur()) {
                 messagePicture = userPictureService.getPictureOf(entry.getValue().getId());
                 if (messagePicture != null) {
                     try {
                         IOStreamUtils.sendMessage(outObject, messagePicture);
                     } catch (final IOException e) {
                         getLog().warn("Fail to send the picture of " + entry.getValue().getNickname() + " : ", e);
                     }
                 } else {
                     getLog().warn("Fail to open the picture of {}", entry.getValue().getNickname());
                 }
             }
         }
     }
 
     /**
      * Sets the last id.
      * 
      * @param lastId
      *            the new last id
      */
     private void setLastId(final int lastId) {
         this.lastId = lastId;
     }
 
     /**
      * Sets the user picture service.
      * 
      * @param userPictureService
      *            the new user picture service
      */
     public void setUserPictureService(final IUserPictureService userPictureService) {
         this.userPictureService = userPictureService;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.gc.irc.server.auth.AuthentificationInterface#changeNickUser(int,
      * java.lang.String)
      */
     @Override
     public void updateUserNickName(final int id, final String nickname) {
         final UserInformations user = getUser(id);
         if (user != null) {
             user.setNickname(nickname);
             saveModification();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.gc.irc.server.auth.AuthentificationInterface#changePasswordUser(int,
      * java.lang.String)
      */
     @Override
     public void updateUserPasword(final int id, final String password) {
         final UserInformations user = getUser(id);
         if (user != null) {
             user.setPassword(password);
             saveModification();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.gc.irc.server.auth.AuthentificationInterface#userLoginExist(java.
      * lang.String)
      */
     @Override
     public boolean userLoginExist(final String login) {
         for (final Map.Entry<Integer, UserInformations> entry : users.entrySet()) {
             if (entry.getValue().loginEquals(login)) {
                 return true;
             }
         }
         return false;
     }
 }
