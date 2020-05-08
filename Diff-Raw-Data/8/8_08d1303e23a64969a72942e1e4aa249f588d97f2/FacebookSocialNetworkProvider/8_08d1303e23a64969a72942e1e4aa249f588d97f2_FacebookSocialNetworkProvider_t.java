 package org.karmaexchange.provider;
 
 import static java.lang.String.format;
 import static org.karmaexchange.security.OAuthFilter.OAUTH_LOG_LEVEL;
 
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 
 import org.karmaexchange.dao.ContactInfo;
 import org.karmaexchange.dao.ModificationInfo;
 import org.karmaexchange.dao.OAuthCredential;
 import org.karmaexchange.dao.User;
 import org.karmaexchange.resources.msg.ErrorResponseMsg;
 import org.karmaexchange.resources.msg.ErrorResponseMsg.ErrorInfo;
 
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Parameter;
 import com.restfb.exception.FacebookException;
import com.restfb.exception.FacebookOAuthException;
 
 public final class FacebookSocialNetworkProvider extends SocialNetworkProvider {
 
   private static final Logger log = Logger.getLogger(Filter.class.getName());
 
   private static final String PROFILE_IMAGE_URL_FMT = "https://graph.facebook.com/%s/picture";
 
   public FacebookSocialNetworkProvider(OAuthCredential credential,
       SocialNetworkProviderType providerType) {
     super(credential, providerType);
   }
 
   @Override
   public boolean verifyCredential() {
     DefaultFacebookClient fbClient = new DefaultFacebookClient(credential.getToken());
     com.restfb.types.User fbUser;
     try {
       fbUser = fbClient.fetchObject("me", com.restfb.types.User.class,
         Parameter.with("fields", "id"));
    } catch (FacebookException e) {
      throw ErrorResponseMsg.createException(e,
        (e instanceof FacebookOAuthException) ? ErrorInfo.Type.AUTHENTICATION :
          ErrorInfo.Type.PARTNER_SERVICE_FAILURE);
     }
     if (fbUser.getId().equals(credential.getUid())) {
       return true;
     } else {
       log.log(OAUTH_LOG_LEVEL,
         format("Uid of user=[%s] does not match credential uid=[%s]",
           fbUser.getId(), credential.getUid()));
       return false;
     }
   }
 
   @Override
   public User createUser() {
     DefaultFacebookClient fbClient = new DefaultFacebookClient(credential.getToken());
     com.restfb.types.User fbUser;
     fbUser = fbClient.fetchObject("me", com.restfb.types.User.class);
     User user = User.create(credential);
     user.setModificationInfo(ModificationInfo.create());
     user.setFirstName(fbUser.getFirstName());
     user.setLastName(fbUser.getLastName());
     ContactInfo contactInfo = new ContactInfo();
     user.setContactInfo(contactInfo);
     // TODO(avaliani):
     // Access tokens required:
     //   - email
     //   - location
     contactInfo.setEmail(fbUser.getEmail());
 
     // NamedFacebookType fbLocationKey = fbUser.getLocation();
     // TODO(avaliani): fix this
     // if (fbLocationKey != null) {
     //   contactInfo.setAddress(initAddress(fbClient, fbLocationKey));
     // }
 
     return user;
   }
 
   @Override
   public String getProfileImageUrl() {
     return String.format(PROFILE_IMAGE_URL_FMT, credential.getUid());
   }
 
   /*
   private Address initAddress(DefaultFacebookClient fbClient, NamedFacebookType fbLocationKey) {
     Address address = new Address();
     com.restfb.types.Location fbLocation = fetchObject(
       fbClient, fbLocationKey.getName(), com.restfb.types.Location.class);
     address.setStreet(fbLocation.getStreet());
     address.setCity(fbLocation.getCity());
     address.setState(fbLocation.getState());
     address.setCountry(fbLocation.getCountry());
     address.setZip(fbLocation.getZip());
     if ((fbLocation.getLatitude() != null) && (fbLocation.getLongitude() != null)) {
       GeoPt geoPt = new GeoPt((float) ((double) fbLocation.getLatitude()),
         (float) ((double) fbLocation.getLongitude()));
       address.setGeoPt(GeoPtWrapper.create(geoPt));
     }
     return address;
   }
 
   private <T> T fetchObject(DefaultFacebookClient fbClient, String name, Class<T> objClass) {
     try {
       return fbClient.fetchObject(name, objClass);
     } catch(FacebookException e) {
       throw ErrorResponseMsg.createException(e, ErrorInfo.Type.PARTNER_SERVICE_FAILURE);
     }
   }
   */
 }
