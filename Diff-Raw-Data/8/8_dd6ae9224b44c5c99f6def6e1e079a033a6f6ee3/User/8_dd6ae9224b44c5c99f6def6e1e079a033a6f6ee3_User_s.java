 package models;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 
 import com.google.gson.Gson;
 
 import controllers.Application;
 
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class User extends Model {
 
 	public String		username;
 
 	@Email
 	@Required
 	public String		email;
 
 	@Required
 	public String		password;
 
 	public String		requestToken;
 	public String		authUrl;
 	public String		oauth_verifier;
 	public String		oauth_token;
 	public String		requestTokenSecret;
 	public String		accessToken;
 	public String		accessTokenSecret;
 	public Timestamp	lastupdate;
 
 	@OneToOne
 	public MisoUser			miso;
 	
 	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
 	public List<MisoCheckin>	misoCheckins;
 	
 
 	public User(String email, String password, String username) {
 		this.email = email;
 		this.password = password;
 		this.username = username;
 		this.misoCheckins = new ArrayList<MisoCheckin>();
 		this.miso = new MisoUser();
 		this.miso.save();
 	}
 
 	public static User find(String email) {
 		return User.find("byEmail", email).first();
 	}
 
 	public static User connect(String email, String password) {
 		return find("byEmailAndPassword", email, password).first();
 	}
 
 	/**
 	 * @param user
 	 */
 	public static void updateMisoUserDetails(User user) {
 		DateTimeFormatter fmt =  DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.0");
		DateTime dtlast = fmt.parseDateTime(user.lastupdate.toString());
 		DateTime dtcur = new DateTime();
 	
 		if (user.miso == null || user.lastupdate == null || dtlast.isBefore(dtcur.minusHours(2))) {
 			Token accessToken = new Token(user.accessToken, user.accessTokenSecret);
 			OAuthRequest request = new OAuthRequest(Verb.GET, "https://gomiso.com/api/oauth/v1/users/show.json");
 			Application.getConnector().signRequest(accessToken, request);
 			Response response = request.send();
 	
 			String userdetails = response.getBody().substring(8);
 			userdetails = userdetails.substring(0, userdetails.length() - 1);
 	
 			MisoUser m = new Gson().fromJson(userdetails, MisoUser.class);
 			
 			user.miso.full_name = m.full_name;
 			user.miso.currently_followed = m.currently_followed;
 			user.miso.follower_count = m.follower_count;
 			user.miso.tagline = m.tagline;
 			user.miso.facebook_enabled = m.facebook_enabled;
 			user.miso.following_count = m.following_count;
 			user.miso.badge_count = m.badge_count;
 			user.miso.twitter_enabled = m.twitter_enabled;
 			user.miso.checkin_count = m.checkin_count;
 			user.miso.username = m.username;
 			user.miso.total_points = m.total_points;
 			user.miso.url = m.url;
 			user.miso.profile_image_url = m.profile_image_url;
 			user.miso.facebook = m.facebook;
 			user.miso.twitter = m.twitter;
 	
 			user.miso.save();
 			user.lastupdate = new Timestamp(System.currentTimeMillis());
 			user.save();
 		}
 	}
 
 }
