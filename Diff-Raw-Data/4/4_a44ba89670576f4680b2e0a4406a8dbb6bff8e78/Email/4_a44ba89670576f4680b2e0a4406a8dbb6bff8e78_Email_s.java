 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 
 import javax.mail.Address;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 
 import org.apache.commons.io.IOUtils;
 
 // Represents a single Email
 public class Email implements Visualizable {
   private Message message;
   private String from;
   private ArrayList<String> recipients;
   private String subject;
   private String text;
 
   Email(Message message) {
     try {
       this.message = message;
       this.from = message.getFrom()[0].toString();
       this.recipients = get_recipients();
       this.subject = message.getSubject();
       this.text = get_text();
     } catch (MessagingException | IOException e) {
       e.printStackTrace();
     }
 
   }
 
   @Override
   public float sentiment() {
     float sentiment_value = 0.0f;
     int words_found = 0;
     float average_sentiment = 0.0f;
 
     try {
       Sentiment sentiment = Sentiment.get_instance();
       String[] word_list = word_list();
 
       for (String word : word_list) {
         Float word_score = sentiment.score(word);
         if (word_score != null) {
           words_found++;
           sentiment_value += sentiment.score(word);
         }
       }
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     if (words_found != 0) {
       average_sentiment = sentiment_value / words_found;
     }
     return average_sentiment;
   }
 
   @Override
   public int age() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   @Override
   public float average_email_length() {
     // TODO Auto-generated method stub
     return 0;
   }
 
   private String[] word_list() {
     return text.split(" ");
   }
 
   // returns the emails textual content
   private String get_text() throws IOException, MessagingException {
     // TODO make this smarter and parse out HTML from html messages, and not get
     // the content of attachments
     InputStream stream = message.getInputStream();
     StringWriter writer = new StringWriter();
     IOUtils.copy(stream, writer);
     return writer.toString();
   }
 
   private ArrayList<String> get_recipients() throws MessagingException {
     ArrayList<String> recipients = new ArrayList<String>();
     Address[] addresses = message.getAllRecipients();
     if (addresses != null) {
       for (Address address : addresses) {
         recipients.add(address.toString());
       }
     }
     return recipients;
   }
 }
 
