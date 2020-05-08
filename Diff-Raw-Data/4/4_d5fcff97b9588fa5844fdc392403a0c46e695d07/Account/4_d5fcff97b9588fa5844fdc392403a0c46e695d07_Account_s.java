 package com.abid_mujtaba.fetchheaders.models;
 
 import android.util.Log;
 
 import com.abid_mujtaba.fetchheaders.Resources;
 import com.abid_mujtaba.fetchheaders.Settings;
 
 import com.sun.mail.imap.IMAPFolder;
 import com.sun.mail.util.MailConnectException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.mail.AuthenticationFailedException;
 import javax.mail.FetchProfile;
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.mail.Session;
 import javax.mail.Store;
 
 
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Properties;
 
 /**
  * Model representing an email account. It encapsulates the various methods which connect to an email account and interact with it
  */
 
 public class Account
 {
     private String mName;
     private String mHost;
     private String mUsername;
     private String mPassword;
 
     private int mId;
 
     private static ArrayList<Account> sInstances = new ArrayList<Account>();
     private static int sNumOfInstances = 0;
 
     private Message[] mMessages;        // Stores the Message objects fetched from the email account
     private Folder mInbox;              // Stores reference to the Inbox object, will be used to delete emails.
     private Folder mTrash;              // Trash Folder on the email account
 
     private boolean mUsesLabels = false;        // This flag indicates that the email account uses Labels rather than Folders (Gmail being the most common example)
 
 
     private Account()        // Default empty constructor used to track objects
     {
         mId = sNumOfInstances;
 
         sInstances.add(mId, this);      // Add instance to the list and increment number of instances
 
         sNumOfInstances++;
     }
 
     public Account(String _name, String _host, String _username, String _password)
     {
         this();
 
         mName = _name;
         mHost = _host;
         mUsername = _username;
         mPassword = _password;
     }
 
     public static Account newInstance()         // creates and return an empty instance of Account. update() should be called immediately after to populate it.
     {
         return new Account();
     }
 
     public String name() { return mName; }
     public String host() { return mHost; }
     public String username() { return mUsername; }
     public String password() { return mPassword; }
 
     public static Account get(int id)
     {
         return sInstances.get(id);
     }
 
     @Override
     public String toString()
     {
         return String.format("<Account - name: %s - host: %s - username: %s - password: %s>", mName, mHost, mUsername, mPassword);
     }
 
 
     public void update(String _name, String _host, String _username, String _password)      // Used to update account information
     {
         mName = _name;
         mHost = _host;
         mUsername = _username;
         mPassword = _password;
 
         writeAccountsToJson();
     }
 
 
     public static int numberOfAccounts()
     {
         return sNumOfInstances;
     }
 
 
     public static void createAccountsFromJson()     // Reads the specified json file and uses it to construct Account objects
     {
         try
         {
             File file = new File(Resources.INTERNAL_FOLDER, Settings.ACCOUNTS_JSON_FILE);
 
             if (!file.exists())
             {
                 createEmptyAccountsJsonFile(file);
             }
 
             FileInputStream fis = new FileInputStream(file);
 
             String content = Resources.getStringFromInputStream(fis);
             fis.close();
 
             JSONObject root = new JSONObject(content);
             JSONArray accounts = root.getJSONArray("accounts");
 
             for (int ii = 0; ii < accounts.length(); ii++)        // Iterate over all accounts in the JSONArray and use each to construct an Account object
             {
                 JSONObject account = accounts.getJSONObject(ii);
 
                 new Account(account.getString("name"), account.getString("host"), account.getString("username"), account.getString("password"));
             }
         }
         catch (FileNotFoundException e) { Log.e(Resources.LOGTAG, "Unable to open " + Settings.ACCOUNTS_JSON_FILE, e); }
         catch (IOException e) { Log.e(Resources.LOGTAG, "Unable to close FileInputStream for " + Settings.ACCOUNTS_JSON_FILE, e); }
         catch (JSONException e) { Log.e(Resources.LOGTAG, "Exception thrown while working with JSON", e); }
     }
 
 
     private static void writeAccountsToJson()       // Reads all Account objects (some of them updated) and uses them to write this data to accounts.json
     {
         try
         {
             JSONObject jRoot = new JSONObject();
             JSONArray jAccounts = new JSONArray();
 
             for (int ii = 0; ii < sNumOfInstances; ii++)
             {
                 JSONObject jAccount = new JSONObject();
                 Account account = sInstances.get(ii);
 
                 jAccount.put("name", account.name());
                 jAccount.put("host", account.host());
                 jAccount.put("username", account.username());
                 jAccount.put("password", account.password());
 
                 jAccounts.put(jAccount);
             }
 
             jRoot.put("accounts", jAccounts);
 
             // Save JSON to accounts.json
             FileWriter fw = new FileWriter(new File(Resources.INTERNAL_FOLDER, Settings.ACCOUNTS_JSON_FILE));    // Write root JSON object to file info.json
             fw.write(jRoot.toString());
             fw.flush();
             fw.close();
         }
         catch (JSONException e) { Log.e(Resources.LOGTAG, "Exception raised while manipulate JSON objects.", e); }
         catch (IOException e) { Log.e(Resources.LOGTAG, "Exception raised while saving content to json file.", e); }
     }
 
 
     private static void createEmptyAccountsJsonFile(File file)
     {
         try
         {
             // We start by creating the JSON tree for an empty accounts.json file.
             JSONObject root = new JSONObject();
             JSONArray accounts = new JSONArray();
 
             root.put("accounts", accounts);
 
             // Save JSON to accounts.json
             FileWriter fw = new FileWriter(file);    // Write root JSON object to file info.json
             fw.write(root.toString());
             fw.flush();
             fw.close();
         }
         catch (JSONException e) { Log.e(Resources.LOGTAG, "Exception raised while manipulate JSON objects.", e); }
         catch (IOException e) { Log.e(Resources.LOGTAG, "Exception raised while saving content to json file.", e); }
     }
 
 
     public ArrayList<Email> fetchEmails(boolean unSeenOnly) throws NoSuchProviderException, AuthenticationFailedException, MailConnectException, MessagingException      // Fetches messages from account and uses them to create an array of Email objects. Catches connection exception and re-throws them up the chain.
     {
         Properties props = new Properties();
 
         props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.imaps.host", mHost);
//        props.setProperty("mail.imaps.port", "993");
         props.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");          // Uses SSL to secure communication
         props.setProperty("mail.imaps.socketFactory.fallback", "false");
 
         Session imapSession = Session.getInstance(props);
 
         try
         {
             Store store = imapSession.getStore("imaps");
 
             // Connect to server by sending username and password:
             store.connect(mHost, mUsername, mPassword);
 
             mInbox = store.getFolder("Inbox");
             mInbox.open(Folder.READ_WRITE);               // Open Inbox as read-write since we will be deleting emails laster
 
             mTrash = store.getFolder("Trash");
 
             if (!mTrash.exists())
             {
                 mTrash = store.getFolder("[Gmail]/Trash");        // If a folder labelled "Trash" doesn't exist we attempt to check if it is a Gmail account whose trash folder is differently named
                 mUsesLabels = true;                               // Set flag to indicate that this account uses Labels rather than folders a la Gmail
 
                 if (!mTrash.exists())
                 {
                     mTrash = null;         // No trash folder found. Emails will be deleted directly
                     mUsesLabels = false;
                 }
             }
 
             mMessages = mInbox.getMessages();
 
             FetchProfile fp = new FetchProfile();
             fp.add(IMAPFolder.FetchProfileItem.HEADERS);        // Fetch header data
             fp.add(FetchProfile.Item.FLAGS);            // Fetch flags
 
             mInbox.fetch(mMessages, fp);
 
             ArrayList<Email> emails = new ArrayList<Email>();
 
             for (Message message: mMessages)
             {
                 Email email = new Email(message);
 
                 if (unSeenOnly)
                 {
                     if (! email.seen())
                     {
                         emails.add(email);
                     }
                 }
                 else                    // Since unSeenOnly is false all emails are sent through
                 {
                     emails.add(email);
                 }
             }
 
             return emails;
         }
         catch (NoSuchProviderException e) { Resources.Loge("Unable to get store from imapsession", e); throw e; }       // We log all of the possible exceptions and then re-throw them forcing the calling function to handle them - this includes using the UI to report errors to the user.
         catch (AuthenticationFailedException e) { Resources.Loge("Authentication failure while connecting to: " + mHost, e); throw e; }
         catch (MailConnectException e) { Resources.Loge("MailConnectException - Unable to connect to: " + mHost, e); throw e; }
         catch (MessagingException e) { Resources.Loge("Exception while attempting to connect to mail server", e); throw e; }         // The two above exceptions are caught by this one if they are not explicitly stated above.
     }
 
 
     public void delete(ArrayList<Email> emails)  // Delete emails with the specified ids
     {
         Message[] messages = new Message[emails.size()];
         try
         {
             for (int ii = 0; ii < emails.size(); ii++)
             {
                 messages[ii] = emails.get(ii).message();
             }
 
             if (mTrash != null) { mInbox.copyMessages(messages, mTrash); }
 
             if (! mUsesLabels)          // For Gmail-type accounts (that use Labels rather than folders) copying the message to Trash is enough to carry out deletion.
             {
                 mInbox.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
             }
 
             mInbox.close(true);         // This will cause the emails marked for deletion to be actually deleted.
         }
         catch (MessagingException e) { Resources.Loge("Exception raised while attempting to delete emails.", e); }
     }
 }
