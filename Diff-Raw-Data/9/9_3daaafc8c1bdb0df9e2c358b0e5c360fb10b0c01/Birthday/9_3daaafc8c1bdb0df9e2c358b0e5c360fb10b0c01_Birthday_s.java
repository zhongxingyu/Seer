 package net.jtmcgee.project.happybirthday.model;
 
 import java.text.DateFormat;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.FacebookClient;
 import com.restfb.types.User;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.servlet.http.HttpServletRequest;
 
 import net.jtmcgee.project.happybirthday.utils.Config;
 
 @PersistenceCapable
 public class Birthday {
 	
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 	
 	@Persistent
 	private Date birthday;
 	
 	@Persistent
 	private String message;
 	
 	@Persistent
 	private String uid;
 	
 	@Persistent
 	private String wisherID;
 	
 	@Persistent
 	private String token;
 	
 	public Birthday(String birthday, User user, String message, String access_token, HttpServletRequest req) throws ParseException {
 		try {
 		    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 		    this.birthday = (Date)formatter.parse(birthday);
 		} catch (ParseException pe) {
 		    DateFormat formatter = new SimpleDateFormat("MM/dd");
 		    this.birthday = (Date)formatter.parse(birthday);
 		}
 	    this.uid = user.getId();
 	    this.message = message;
 	    this.token = access_token;
 	    FacebookClient fbClient = new DefaultFacebookClient(Config.ACCESS_TOKEN(req));
 		this.wisherID = fbClient.fetchObject("me", User.class).getId();
 	}
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	public boolean isToday() {
 	    DateFormat formatter = new SimpleDateFormat("MM/dd");
 	    formatter.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
 
		if(formatter.format((new Date())).equals(formatter.format(this.birthday)))
 			return true;
 		return false;
 	}
 	public void setToken(String token) {
 		if(!token.equals("")) {
 			this.token = token;
 		}
 	}
 	public String getWisher() {
 		return this.wisherID;
 	}
 	public String getToken() {
 		return this.token;
 	}
 	public String getMessage() {
 		return this.message;
 	}
 	public String getUid() {
 		return this.uid;
 	}
 	public User getUser(HttpServletRequest req) {
 		FacebookClient fbClient = new DefaultFacebookClient(Config.ACCESS_TOKEN(req));
 		return fbClient.fetchObject(this.uid, User.class);	
 	}
 	public Key getKey() {
 		return this.key;
 	}
 	public String getDate() {
 	    DateFormat formatter = new SimpleDateFormat("MMMMM dd");
 		return formatter.format(birthday);
 	}
 	public int getAge() {
 		if(birthday.getYear() == 70)
 			return 0;
 		// Create a calendar object with the date of birth
 		Calendar dateOfBirth = new GregorianCalendar(birthday.getYear(), birthday.getMonth(), birthday.getDay());
 
 		// Create a calendar object with today's date
 		Calendar then = new GregorianCalendar((new Date()).getYear(), birthday.getMonth(), birthday.getDay());
 
 		// Get age based on year
 		int age = then.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
 
 		// Add the tentative age to the date of birth to get this year's birthday
 		dateOfBirth.add(Calendar.YEAR, age);
 
 		return age;
 	}
 }
