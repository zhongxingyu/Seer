 package net.anei.cadpage;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 
 public class SmsMessageQueue implements Serializable {
   
   private static final long serialVersionUID = 1L;
 
   private static String QUEUE_FILENAME = "message.queue";
   
   private List<SmsMmsMessage> queue = new ArrayList<SmsMmsMessage>();
   private Context context;
   private Adapter adapter = null;
   
   @SuppressWarnings("unchecked")
   private SmsMessageQueue(Context context) {
     this.context = context;
     ObjectInputStream is = null;
     try {
       is = new ObjectInputStream(
         context.openFileInput(QUEUE_FILENAME));
       queue = (ArrayList<SmsMmsMessage>)is.readObject();
     } catch (FileNotFoundException ex) {
     } catch (Exception ex) {
       Log.e(ex);
     } finally {
       if (is != null) try {is.close();} catch (IOException ex) {}
     }
     
     msgQueue = this;
   }
 
   /**
    * Save queue to persistent file
    */
   private synchronized void save() {
     ObjectOutputStream os = null;
     try {
       os = new ObjectOutputStream(
         context.openFileOutput(QUEUE_FILENAME, Context.MODE_PRIVATE));
       os.writeObject(queue);
     } catch (IOException ex) {
       Log.e(ex);
     } finally {
       if (os != null) try {os.close();} catch (IOException ex) {}
     }
   }
   
   public void notifyDataChange() {
     if (adapter != null) adapter.notifyDataSetChanged();
     save();
   }
   
   /**
    * Add new message to queue and delete any old messages down to the
    * requested history queue count
    * @param msg message to be added
    */
   public synchronized void addNewMsg(SmsMmsMessage msg) {
     
     // Add message to beginning of queue
     queue.add(0, msg);
     
     // Get history limit
     int limit = ManagePreferences.historyCount();
     
     // Do we have to delete anything
     int deleteCnt = queue.size() - limit;
     if (deleteCnt > 0) {
       
       // Count the number of messages that can be deleted
       // (read and not locked)
       int availCnt = 0;
       for (SmsMmsMessage m : queue) {
         if (m.isRead() && !m.isLocked()) availCnt++;
       }
       
       // How many of these need to be kept to get us to the right limit
       int keepCnt = Math.max(0, availCnt - deleteCnt);
       
       // Make another pass through the list deleting anything over the keep limit
      for (Iterator<SmsMmsMessage> itr = queue.iterator(); ; itr.hasNext()) {
         SmsMmsMessage m = itr.next();
         if (m.isRead() && !m.isLocked()) {
           if (keepCnt <= 0) itr.remove();
           else keepCnt--;
         }
       }
     }
     notifyDataChange();
   }
   
   /**
    * Remove all expendable (read and not locked) messages
    */
   public void clearAll() {
     
     // Delete everything this has been read and isn't locked
     for (Iterator<SmsMmsMessage> itr = queue.iterator(); itr.hasNext(); ) {
       SmsMmsMessage m = itr.next();
       if (m.isRead() && !m.isLocked()) itr.remove();
     }
     
     notifyDataChange();
   }
 
   /**
    * @return the single unread message if there is one and only one.  
    * Null otherwise
    */
   public SmsMmsMessage getDisplayMsg() {
     SmsMmsMessage result = null;
     for (SmsMmsMessage msg : queue) {
       if (! msg.isRead()) {
         if (result != null) return null;
         result = msg;
       }
     }
     return result;
   }
   
   /**
    * @return ListAdapter that can be bound to ListView to display call history
    */
   public ListAdapter listAdapter(Activity context) {
    return new Adapter(context);
   }
   
   /**
    * Private ListAdapter class
    */
   private class Adapter extends ArrayAdapter<SmsMmsMessage> {
     
     Context context;
 
     public Adapter(Context context) {
       super(context, 0, queue);
       this.context = context;
     }
 
     /* (non-Javadoc)
      * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
      */
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       View view = convertView;
       if (view == null) {
         LayoutInflater li = LayoutInflater.from(context);
         view = li.inflate(R.layout.msg_list_item, parent, false);
       }
       ((HistoryMsgTextView)view).setMessage(queue.get(position));
       return view;
     }
   }
   
   private static SmsMessageQueue msgQueue = null;
   
   /**
    * Set up singleton message queue object
    * @param context context used to create object
    */
   public static void setupInstance(Context context) {
     if (msgQueue == null) {
       msgQueue = new SmsMessageQueue(context);
     }
   }
   
   /**
    * @return singleton message queue object
    */
   public static SmsMessageQueue getInstance() {
     return msgQueue;
   }
 }
