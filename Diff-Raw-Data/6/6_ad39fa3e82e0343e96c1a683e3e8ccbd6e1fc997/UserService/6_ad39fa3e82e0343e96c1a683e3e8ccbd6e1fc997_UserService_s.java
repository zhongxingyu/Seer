 package cashlets;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.transaction.SystemException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.modules.morphia.Model;
 
 import com.google.code.morphia.query.Query;
 
 import securesocial.provider.AuthenticationMethod;
 import securesocial.provider.SocialUser;
 import securesocial.provider.UserId;
 import securesocial.provider.UserServiceDelegate;
 
 import models.*;
 
 
 public class UserService implements UserServiceDelegate {
 
 	static Logger log = LoggerFactory.getLogger(UserService.class);
 	
 	@Override
 	public SocialUser find(UserId id) {
 		// TODO Auto-generated method stub
 		
 //		play.modules.morphia.Model.MorphiaQuery query =  User.find(id.toString()).first();
 		
 		System.out.println("Find user"+id);
 		
 		User user = User.findById(id);				
 		
 		if(user!=null)
 			{
 			log.info("user found : "+id);
 			return user.details;
 			}
 		else {
 			System.out.println("user not found");
 			return null;
 		}
 				
 		
 	}
 
 	@Override
 	public SocialUser find(String email) {
 		// TODO Auto-generated method stub
 		
 		System.out.println("Find user by email ID : "+email);
 		log.info("Logger is working: finding user by email ");
 		
 		return null;
 	}
 
 	/*userid;
 	password;firstName;lastName;phone;city;photoID;
 	registeredDate;
 	email;
 	lastAccess;
 	authMethod;
 	isEmailVerified;*/
 	
 	
 	@Override
 	public void save(SocialUser suser) {
 		// TODO Auto-generated method stub
 		
 		
 		User newUser = new User();
 		
 		/*user2.userid = suser.id.toString();
 		user2.username = suser.id.id;
 		user2.password =suser.password;
 		user2.firstName = suser.displayName;
 		user2.registeredDate = new Date().toString();
 		user2.email = suser.email;
 		user2.lastAccess = suser.lastAccess;
 		user2.authMethod=suser.authMethod;
 		user2.isEmailVerified=suser.isEmailVerified;
 		user2.city = suser.city;
 		user2.phone= suser.phone;
 		user2.gender = suser.gender;
 		*/
 		
 		
 		//save user object
 		
 		//suser.isEmailVerified=true;
 
 		/*newUser.userid = suser.id.id;
 
 		newUser.details=suser;
 
 		newUser.save();
 		*/
 



 		log.info("Saving user id:"+suser.id.id);
 		
 		
 	}
 
 	@Override
 	public String createActivation(SocialUser user) {
 		// TODO Auto-generated method stub
 		
 		log.info("createActivation Method");
 		
 		return null;
 	}
 
 	@Override
 	public boolean activate(String uuid) {
 		// TODO Auto-generated method stub
 		log.info("activate Method");
 		
 		return false;
 	}
 
 	@Override
 	public String createPasswordReset(SocialUser user) {
 		// TODO Auto-generated method stub
 		return "";
 	}
 
 	@Override
 	public SocialUser fetchForPasswordReset(String username, String uuid) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void disableResetCode(String username, String uuid) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void deletePendingActivations() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	
 	
 }
