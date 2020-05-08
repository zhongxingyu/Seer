 package ch.zhaw.i11b.pwork.sem2.beans;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * The Main Message class, defines the client server com and and the Message in the Server.
  * 
 * the values need to be public for jaxb. Don't know why it doesn't work..
 * any way also with get and setters, it doesn't work how it shut.. =(
  * @author oups
  *
  */
 @XmlRootElement
 public class Message {
 	@XmlElement(required=false)
 	public String id = null;
 	public String from = "test@test.com";
 	public String message = "Hallo World!";
 	public List<Target> targets = new ArrayList<Target>();
 	public Date sendtime = null;
 	public boolean reminder = false;
 
 	public Message() {} // JAXB needs this
 }
