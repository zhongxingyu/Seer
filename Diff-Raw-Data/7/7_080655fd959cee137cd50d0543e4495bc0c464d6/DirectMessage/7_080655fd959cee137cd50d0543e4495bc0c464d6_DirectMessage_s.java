 package weibo4j.model;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import weibo4j.http.Response;
 import weibo4j.org.json.JSONArray;
 import weibo4j.org.json.JSONException;
 import weibo4j.org.json.JSONObject;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Say something?
  * <pre>
 * User: zhangzhi.cao
  * Date: 12-5-7
  * Time: 下午4:08
  * </pre>
  */
 public class DirectMessage extends WeiboResponse implements Serializable {
     private static final Logger LOG = LoggerFactory.getLogger(DirectMessage.class);
     private static final boolean debug = LOG.isDebugEnabled();
     private static final long serialVersionUID = 6655843199922366404L;
     private Long id;
     private String text;
     private long senderId;
     private long recipientId;
     private Date createdAt;
     private String senderScreenName;
     private String recipientScreenName;
 
     public DirectMessage(JSONObject json) throws WeiboException {
         try {
             id = json.getLong("id");
             text = json.getString("text");
            senderId = json.getInt("sender_id");
            recipientId = json.getInt("recipient_id");
             createdAt = parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
             senderScreenName = json.getString("sender_screen_name");
             recipientScreenName = json.getString("recipient_screen_name");
             if (!json.isNull("sender"))
                 sender = new User(json.getJSONObject("sender"));
         } catch (JSONException jsone) {
             throw new WeiboException(jsone.getMessage() + ":" + json.toString(), jsone);
         }
 
     }
 
     public long getId() {
         return id;
     }
 
     public String getText() {
         return text;
     }
 
     public long getSenderId() {
         return senderId;
     }
 
     public long getRecipientId() {
         return recipientId;
     }
 
     /**
      * @return created_at
      * @since Weibo4J 1.1.0
      */
     public Date getCreatedAt() {
         return createdAt;
     }
 
     public String getSenderScreenName() {
         return senderScreenName;
     }
 
     public String getRecipientScreenName() {
         return recipientScreenName;
     }
 
     private User sender;
 
     public User getSender() {
         return sender;
     }
 
     private User recipient;
 
     public User getRecipient() {
         return recipient;
     }
 
     /*package*/
     static List<DirectMessage> constructDirectMessages(Response res
     ) throws WeiboException {
         JSONArray list = res.asJSONArray();
 
         try {
             int size = list.length();
             List<DirectMessage> messages = new ArrayList<DirectMessage>(size);
             for (int i = 0; i < size; i++) {
 
                 messages.add(new DirectMessage(list.getJSONObject(i)));
             }
             return messages;
         } catch (JSONException jsone) {
             throw new WeiboException(jsone);
         }
     }
 
     @Override
     public int hashCode() {
         return id.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (null == obj) {
             return false;
         }
         if (this == obj) {
             return true;
         }
         return obj instanceof DirectMessage && ((DirectMessage) obj).id == this.id;
     }
 
     @Override
     public String toString() {
         return "DirectMessage{" +
                 "id=" + id +
                 ", text='" + text + '\'' +
                 ", sender_id=" + senderId +
                 ", recipient_id=" + recipientId +
                 ", created_at=" + createdAt +
                 ", sender_screen_name='" + senderScreenName + '\'' +
                 ", recipient_screen_name='" + recipientScreenName + '\'' +
                 ", sender=" + sender +
                 ", recipient=" + recipient +
                 '}';
     }
 }
