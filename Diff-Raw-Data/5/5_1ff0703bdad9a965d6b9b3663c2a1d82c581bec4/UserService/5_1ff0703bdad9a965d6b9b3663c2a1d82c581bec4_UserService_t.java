 package service;
 
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import models.LocalToken;
 import models.LocalUser;
 import play.Application;
 import play.Logger;
 import scala.Option;
 import scala.Some;
 import securesocial.core.AuthenticationMethod;
 import securesocial.core.Identity;
 import securesocial.core.PasswordInfo;
 import securesocial.core.SocialUser;
 import securesocial.core.IdentityId;
 import securesocial.core.java.BaseUserService;
 import securesocial.core.java.Token;
 
 public class UserService extends BaseUserService {
 
     public UserService(Application application) {
         super(application);
     }
 
     @Override
     public void doDeleteExpiredTokens() {
         if (Logger.isDebugEnabled()) {
             Logger.debug("deleteExpiredTokens...");
         }
         List<LocalToken> list = LocalToken.find.where().lt("expireAt", new DateTime().toString()).findList();
         for(LocalToken localToken : list) {
             localToken.delete();
         }
     }
 
     @Override
     public void doDeleteToken(String uuid) {
         if (Logger.isDebugEnabled()) {
             Logger.debug("deleteToken...");
             Logger.debug(String.format("uuid = %s", uuid));
         }
         LocalToken localToken = LocalToken.find.byId(uuid);
         if(localToken != null) {
             localToken.delete();
         }
     }
 
     @Override
     //public Identity doFind(UserId userId) {
     public Identity doFind(IdentityId identityId){
         if (Logger.isDebugEnabled()) {
             Logger.debug(String.format("finding by Id = %s", identityId.userId()));
 
         }
 
         //Might should be findByEmail
         LocalUser localUser;
 
         localUser = LocalUser.findById(identityId.userId());
         if(localUser == null){
             localUser = LocalUser.findByEmail(identityId.userId());
         }
         if(localUser == null){
             localUser = LocalUser.findByUsername(identityId.userId());
         }
 
         Logger.debug(String.format("localUser = " + localUser));
         if(localUser == null) return null;
         SocialUser socialUser = new SocialUser(new IdentityId(localUser.id, localUser.provider),    
             localUser.firstName, 
             localUser.lastName, 
             String.format("%s %s", localUser.firstName, localUser.lastName),
             Option.apply(localUser.email), 
             null, 
             new AuthenticationMethod("userPassword"),
             null, 
             null, 
             Some.apply(new PasswordInfo("bcrypt", localUser.password, null))
         );  
         if (Logger.isDebugEnabled()) {
             Logger.debug(String.format("socialUser = %s", socialUser));
         }
         return socialUser;
     }
 
 
     @Override
     public Identity doFindByEmailAndProvider(String email, String providerId) {
         List<LocalUser> list = LocalUser.find.where().eq("email", email).eq("provider", providerId).findList();
         if(list.size() != 1){
             Logger.debug("found a null in findByEmailAndProvider..." + "Provider: " + providerId + " Email: " + email + " #Results: " + list.size());
             return null;
         }
         //Logger.debug("Provider: "+ list.get(0).provider + " Password: " + list.get(0).password);
         LocalUser localUser = list.get(0);
         SocialUser socialUser = 
                 new SocialUser(new IdentityId(localUser.id, localUser.provider),
                         localUser.firstName, 
                         localUser.lastName, 
                         String.format("%s %s", localUser.firstName, localUser.lastName),
                         Option.apply(localUser.email), 
                         null, 
                         new AuthenticationMethod("userPassword"),
                         null, 
                         null, 
                         Some.apply(new PasswordInfo("bcrypt", localUser.password, null))
                    );  
         return socialUser;
     }
 
     @Override
     public Token doFindToken(String token) {
         if (Logger.isDebugEnabled()) {
             Logger.debug("findToken...");
             Logger.debug(String.format("token = %s", token));
         }
         LocalToken localToken = LocalToken.find.byId(token);
         if(localToken == null) return null;
         Token result = new Token();
         result.uuid = localToken.uuid;
         result.creationTime = new DateTime(localToken.createdAt);
         result.email = localToken.email;
         result.expirationTime = new DateTime(localToken.expireAt);
         result.isSignUp = localToken.isSignUp;
         if (Logger.isDebugEnabled()) {
             Logger.debug(String.format("foundToken = %s", result));
         }
         return result;
     }
 
     @Override
     public Identity doSave(Identity user) {
         if (Logger.isDebugEnabled()) {
             Logger.debug("save...!_!");
             Logger.debug(String.format("user = %s", user));
         }
         LocalUser localUser = null;
         localUser = LocalUser.find.byId(user.identityId().userId());
         /*
         Logger.debug("id = " + user.identityId().userId());
         Logger.debug("provider = " + user.identityId().providerId());
         Logger.debug("firstName = " + user.firstName());
         Logger.debug("lastName = " + user.lastName());
         Logger.debug(user.fullName() + "");
         Logger.debug("email = " + user.email());
         Logger.debug(user.email().getClass() + "");
         */
         if (localUser == null) {
             Logger.debug("adding new...");
             localUser = new LocalUser();
             localUser.id = user.identityId().userId();
             localUser.provider = user.identityId().providerId();
             localUser.firstName = user.firstName();
             localUser.lastName = user.lastName();
            localUser.avatarUrl = user.avatarUrl().get();
             
             //Temporary solution for twitter which does not have email in OAuth answer
             if(!(user.email().toString()).equals("None")){
                 localUser.email = user.email().get();
             }
             if(!(user.passwordInfo() + "").equals("None")){
 
                 localUser.password = user.passwordInfo().get().password();
                 Logger.debug(localUser.password + "");
             }
             localUser.save();
         } else {
             Logger.debug("existing one...");
             localUser.id = user.identityId().userId();
             localUser.provider = user.identityId().providerId();
             localUser.firstName = user.firstName();
             localUser.lastName = user.lastName();
            localUser.avatarUrl = user.avatarUrl().get();
             
             //Temporary solution for twitter which does not have email in OAuth answer
             if(!(user.email().toString()).equals("None")){
                 localUser.email = user.email().get();
             }
             if(!(user.passwordInfo() + "").equals("None")){
                 localUser.password = user.passwordInfo().get().password();
             }
             localUser.update();
         }
         return user;
     }
 
     @Override
     public void doSave(Token token) {
         LocalToken localToken = new LocalToken();
         localToken.uuid = token.uuid;
         localToken.email = token.email;
         try {
             SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             localToken.createdAt = df.parse(token.creationTime.toString("yyyy-MM-dd HH:mm:ss"));
             localToken.expireAt = df.parse(token.expirationTime.toString("yyyy-MM-dd HH:mm:ss"));
         } catch (ParseException e) {
             Logger.error("UserService.doSave(): ", e);
         }
         localToken.isSignUp = token.isSignUp;
         localToken.save();
     }
 }
