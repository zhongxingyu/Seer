 package com.napthats.android.evernote;
 
 import java.io.File;
 import java.util.List;
 
 import java.util.regex.*;
 
 import java.lang.reflect.Proxy;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 import android.app.Activity;
 import android.os.Environment;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 
 import com.evernote.edam.type.*;
 import com.evernote.edam.notestore.*;
 import com.evernote.edam.userstore.*;
 import com.evernote.edam.error.*;
 import com.evernote.client.oauth.android.EvernoteSession;
 import com.evernote.thrift.transport.TTransportException;
 
 

 public class EvernoteActivity extends Activity
 {
   private final String EVERNOTE_HOST = "www.evernote.com";
 
   private EvernoteSession session = null;
 
 
   /**
   * Some utilities related to notes with attributes.
    */
   protected static class Util {
     private static final String NOTE_PREFIX =
       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
           "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
               "<en-note>";
     private static final String NOTE_SUFFIX = "</en-note>";
     private static final String LINE_PREFIX = "<div>";
     private static final String LINE_SUFFIX = "</div>";
     private static final String ATTR_REGEX = "<span style=\"EvernoteActivity,.*?\"></span>";
 
 
     /**
      * Get a plain content from a note.
      * It removes all tags/attributes and adds newlines corresponded to div tags.
      *
      * @param note a taget note
      * @return a result string
      */
     public static final String getPlainContent(Note note) {
       return note.getContent()
                  .replaceAll(ATTR_REGEX, "")
                  .replaceAll(LINE_SUFFIX, "\n")
                  .replaceAll("<.*?>", "");
     }
 
     /** 
      * Set a plain content to a note.
      * It retains attributes in the note.
      * 
      * @param note a target note
      * @param plain_content a target plain content
      */
     public static final void setPlainContent(Note note, String plain_content) {
       String old_content = note.getContent();
       Pattern pattern = Pattern.compile(ATTR_REGEX);
       Matcher matcher = pattern.matcher(old_content == null ? "" : old_content);
      String note_attr = matcher.find() ? matcher.group() : "";
       note.setContent(
         NOTE_PREFIX
         + LINE_PREFIX
         + plain_content.replaceAll("\n", LINE_SUFFIX + LINE_PREFIX)
         + LINE_SUFFIX
         + note_attr
         + NOTE_SUFFIX
       );
     }
   }
 
  
   /**
    * Initialize settings of Evernote.
    * This method have to be called before all other methods.
    *
    * @param c_key The consumer key.
    * @param c_secret The consumer secret.
    * @param evernote_host (optional) The hostname for the Evernote. "www.evernote.com" is used
    * for default. This cannot be omitted if temp_dir_name is provided.
    * @param temp_dir_name (optional) The data directory to store evernote files. Evernote sdk's
    * default setting is used for default. This cannot be provided if evernote_host is ommited.
    */
   protected final void initEvernote(String c_key, String c_secret, String evernote_host, String temp_dir_name) {
     _initEvernote(c_key, c_secret, evernote_host, temp_dir_name);
   }
 
   protected final void initEvernote(String c_key, String c_secret, String evernote_host) {
     _initEvernote(c_key, c_secret, evernote_host, null);
   }
 
   protected final void initEvernote(String c_key, String c_secret) {
     _initEvernote(c_key, c_secret, EVERNOTE_HOST, null);
   }
 
   private void _initEvernote(String c_key, String c_secret, String evernote_host, String temp_dir_name) {
     File temp_file = null;
     if (temp_dir_name != null) {
       temp_file = new File(Environment.getExternalStorageDirectory(), temp_dir_name);
     }
     session = EvernoteSession.init((android.content.Context)this, c_key, c_secret, evernote_host, temp_file);
   }
 
 
   /**
    * Authenticate Evernote and set callback following authentication.
    *
    * @param succeeded Callback after succeeding authentication.
    * @param failed Callback after failing authentication.
    */
   protected final void authenticate(CallBack succeeded, CallBack failed) {
       checkInitialized();
       succeededCallbackAfterAuthentication = succeeded;
       failedCallbackAfterAuthentication = failed;
       session.authenticate(this);
   }
     
   private CallBack succeededCallbackAfterAuthentication = null;
   private CallBack failedCallbackAfterAuthentication = null;
 
   protected interface CallBack {
     public void call();
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (session == null) {return;}
     if (requestCode == EvernoteSession.REQUEST_CODE_OAUTH) {
         assert succeededCallbackAfterAuthentication != null;
         assert failedCallbackAfterAuthentication != null;
         if (resultCode == Activity.RESULT_OK) {
           succeededCallbackAfterAuthentication.call();
         }
         else
         {
           failedCallbackAfterAuthentication.call();
         }
     }
   }
 
 
   /**
    * Get a NoteStore proxy.
    * It has same methods from the original NoteStore, which methods don't throw Exceptions.
    * Additionally, callback version methods is provided. They are executed in AsyncTask.
    */
   protected final NoteStoreProxy createNoteStore() throws TTransportException {
     checkInitialized();
     return (NoteStoreProxy) Proxy.newProxyInstance(
       NoteStoreProxy.class.getClassLoader(),
       new Class[] {NoteStoreProxy.class},
       new InvocationHandler() {
         private NoteStore.Client note_store = null;
         {
           note_store = session.createNoteStore();
         }
         public Object invoke(Object proxy, Method proxy_method, Object[] args)
         throws Throwable {
           try {
             Method nocallback_method = note_store.getClass().getMethod(proxy_method.getName(), proxy_method.getParameterTypes());
             try {
               return nocallback_method.invoke(note_store, args);
             }
             catch (Throwable e) {
               throw e.getCause();
             }
           }
           catch (Exception e) {
             //Try to remove callback in a following part.
           }
 
           final Object callback = args[args.length - 1];
           Object[] callback_deleted_args = new Object[args.length - 1];
           System.arraycopy(args, 0, callback_deleted_args, 0, args.length - 1);
           Class[] callback_deleted_argsclass = new Class[args.length - 1];
           System.arraycopy(proxy_method.getParameterTypes(), 0, callback_deleted_argsclass, 0, args.length - 1);
           final Method method = note_store.getClass().getMethod(proxy_method.getName(), callback_deleted_argsclass);
 
           new AsyncTask<Object, Void, APIResult<Object>>() {
             @Override
             protected APIResult<Object> doInBackground(Object... api_args) {
               try {
                 final Object result = method.invoke(note_store, api_args);
                 return new APIResult<Object>() {
                   @Override
                   public Object get() {
                     return result;
                   }
                 };
               }
               catch (Throwable e) {
                 try {
                   throw e.getCause();
                 }
                 catch (final EDAMUserException eu) {
                   return new APIResult<Object>() {
                     @Override
                     public Object get() throws EDAMUserException {
                       throw eu;
                     }
                   };
                 }
                 catch (final EDAMSystemException es) {
                   return new APIResult<Object>() {
                     @Override
                     public Object get() throws EDAMSystemException {
                       throw es;
                     }
                   };
                 }
                 catch (final EDAMNotFoundException en) {
                   return new APIResult<Object>() {
                     @Override
                     public Object get() throws EDAMNotFoundException {
                       throw en;
                     }
                   };
                 }
                 catch (final RuntimeException er) {
                   return new APIResult<Object>() {
                     @Override
                     public Object get() {
                       throw er;
                     }
                   };
                 }
                 catch (Throwable t) {
                   assert false; //Never reach.
                 }
                 assert false; //Never reach.
                 return null;
              }
            }
             
             @Override
             protected void onPostExecute(APIResult<Object> result) {
               try {
                 Method method = callback.getClass().getMethod("call", APIResult.class);
                 method.invoke(callback, result);
               }
               catch (Throwable e) {
                 assert false; //Never reach.
               }
             }
           }.execute(callback_deleted_args);
           return null; //Never used.
         }
       }
     );
   }
 
   public interface APIResult<T> {
     public T get() throws EDAMUserException, EDAMSystemException, EDAMNotFoundException;
   }
 
   public interface APICallBack<T> {
     public void call(APIResult<T> result);
   }
 
   public interface NoteStoreProxy {
     public SyncState getSyncState(String authToken);
     public void getSyncState(String authToken, APICallBack<SyncState> callback);
     public SyncState getSyncStateWithMetrics(String authToken, ClientUsageMetrics metrics);
     public void getSyncStateWithMetrics(String authToken, ClientUsageMetrics metrics, APICallBack<SyncState> callback);
     public SyncChunk getSyncChunk(String authToken, int afterUSN, int maxEntries, boolean fullSyncOnly);
     public void getSyncChunk(String authToken, int afterUSN, int maxEntries, boolean fullSyncOnly, APICallBack<SyncChunk> callback);
     public SyncChunk getFilteredSyncChunk(String authToken, int afterUSN, int maxEntries, SyncChunkFilter filter);
     public void getFilteredSyncChunk(String authToken, int afterUSN, int maxEntries, SyncChunkFilter filter, APICallBack<SyncChunk> callback);
     public SyncState getLinkedNotebookSyncState(String authToken, LinkedNotebook notebook);
     public void getLinkedNotebookSyncState(String authToken, LinkedNotebook notebook, APICallBack<SyncState> callback);
     public SyncChunk getLinkedNotebookSyncChunk(String authToken, LinkedNotebook notebook, int afterUSN, int maxEntries, boolean fullSyncOnly);
     public void getLinkedNotebookSyncChunk(String authToken, LinkedNotebook notebook, int afterUSN, int maxEntries, boolean fullSyncOnly, APICallBack<SyncChunk> callback);
     public List<Notebook> listNotebooks(String authToken);
     public void listNotebooks(String authToken, APICallBack<List<Notebook>> callback);
     public Notebook getNotebook(String authToken, String guid);
     public void getNotebook(String authToken, String guid, APICallBack<Notebook> callback);
     public Notebook getDefaultNotebook(String authToken);
     public void getDefaultNotebook(String authToken, APICallBack<Notebook> callback);
     public Notebook createNotebook(String authToken, Notebook notebook);
     public void createNotebook(String authToken, Notebook notebook, APICallBack<Notebook> callback);
     public int updateNotebook(String authToken, Notebook notebook);
     public void updateNotebook(String authToken, Notebook notebook, APICallBack<Integer> callback);
     public int expungeNotebook(String authToken, int guid);
     public void expungeNotebook(String authToken, int guid, APICallBack<Integer> callback);
     public List<Tag> listTags(String authToken);
     public void listTags(String authToken, APICallBack<List<Tag>> callback);
     public List<Tag> listTagsByNotebook(String authToken, int notebookGuid);
     public void listTagsByNotebook(String authToken, int notebookGuid, APICallBack<List<Tag>> callback);
     public Tag getTag(String authToken, int guid);
     public void getTag(String authToken, int guid, APICallBack<Tag> callback);
     public Tag createTag(String authToken, Tag tag);
     public void createTag(String authToken, Tag tag, APICallBack<Tag> callback);
     public int updateTag(String authToken, Tag tag);
     public void updateTag(String authToken, Tag tag, APICallBack<Tag> callback);
     public void untagAll(String authToken, int guid);
     public void untagAll(String authToken, int guid, APICallBack<Void> callback);
     public int expungeTag(String authToken, int guid);
     public void expungeTag(String authToken, int guid, APICallBack<Integer> callback);
     public List<SavedSearch> listSearches(String authToken);
     public void listSearches(String authToken, APICallBack<List<SavedSearch>> callback);
     public SavedSearch getSearch(String authToken, int guid);
     public void getSearch(String authToken, int guid, APICallBack<SavedSearch> callback);
     public SavedSearch createSearch(String authToken, SavedSearch search);
     public void createSearch(String authToken, SavedSearch search, APICallBack<SavedSearch> callback);
     public int updateSearch(String authToken, SavedSearch search);
     public void updateSearch(String authToken, SavedSearch search, APICallBack<Integer> callback);
     public int expungeSearch(String authToken, int guid);
     public void expungeSearch(String authToken, int guid, APICallBack<Integer> callback);
     public NoteList findNotes(String authToken, NoteFilter filter, int i1, int i2);
     public void findNotes(String authToken, NoteFilter filter, int i1, int i2, APICallBack<NoteList> callback);
     public int findNoteOffset(String authToken, NoteFilter filter, int guid);
     public void findNoteOffset(String authToken, NoteFilter filter, int guid, APICallBack<Integer> callback);
     public NotesMetadataList findNotesMetadata(String authToken, NoteFilter filter, int offset, int maxNotes, NotesMetadataResultSpec resultSpec);
     public void findNotesMetadata(String authToken, NoteFilter filter, int offset, int maxNotes, NotesMetadataResultSpec resultSpec, APICallBack<NotesMetadataList> callback);
     public NoteCollectionCounts findNoteCounts(String authToken, NoteFilter filter, boolean withTrash);
     public void findNoteCounts(String authToken, NoteFilter filter, boolean withTrash, APICallBack<NoteCollectionCounts> callback);
     public Note getNote(String authToken ,String guid , boolean b1, boolean b2, boolean b3, boolean b4);
     public void getNote(String authToken ,String guid, boolean b1, boolean b2, boolean b3, boolean b4, APICallBack<Note> callback);
     public LazyMap getNoteApplicationData(String authToken, int guid);
     public void getNoteApplicationData(String authToken, int guid, APICallBack<LazyMap> callback);
     public String getNoteApplicationDataEntry(String authToken, int guid, String key);
     public void getNoteApplicationDataEntry(String authToken, int guid, String key, APICallBack<String> callback);
     public int setNoteApplicationDataEntry(String authToken, int guid, String key, String value);
     public void setNoteApplicationDataEntry(String authToken, int guid, String key, String value, APICallBack<Integer> callback);
     public int unsetNoteApplicationDataEntry(String authToken, int guid, String key);
     public void unsetNoteApplicationDataEntry(String authToken, int guid, String key, APICallBack<Integer> callback);
     public String getNoteContent(String authToken, int guid);
     public void getNoteContent(String authToken, int guid, APICallBack<String> callback);
     public String getNoteSearchText(String authToken, int guid, boolean noteOnly, boolean tokenizeForIndexing);
     public void getNoteSearchText(String authToken, int guid, boolean noteOnly, boolean tokenizeForIndexing, APICallBack<String> callback);
     public String getResourceSearchText(String authToken, int guid);
     public void getResourceSearchText(String authToken, int guid, APICallBack<String> callback);
     public List<String> getNoteTagNames(String authToken, int guid);
     public void getNoteTagNames(String authToken, int guid, APICallBack<List<String>> callback);
     public Note createNote(String authToken, Note note);
     public void createNote(String authToken, Note note, APICallBack<Note> callback);
     public Note updateNote(String authToken, Note note);
     public void updateNote(String authToken, Note note, APICallBack<Note> callback);
     public int deleteNote(String authToken, int guid);
     public void deleteNote(String authToken, int guid, APICallBack<Integer> callback);
     public int expungeNote(String authToken, int guid);
     public void expungeNote(String authToken, int guid, APICallBack<Integer> callback);
     public int expungeNotes(String authToken, List<Integer> noteGuids);
     public void expungeNotes(String authToken, List<Integer> noteGuids, APICallBack<Integer> callback);
     public int expungeInactiveNotes(String authToken);
     public void expungeInactiveNotes(String authToken, APICallBack<Integer> callback);
     public Note copyNote(String authToken, int noteGuid, int toNotebookGuid);
     public void copyNote(String authToken, int noteGuid, int toNotebookGuid, APICallBack<Note> callback);
     public List<NoteVersionId> listNoteVersions(String authToken, int noteGuid);
     public void listNoteVersions(String authToken, int noteGuid, APICallBack<List<NoteVersionId>> callback);
     public Note getNoteVersion(String authToken, int noteGuid, int updateSequenceNum, boolean withResourcesData, boolean withResourcesRecognition, boolean withResourcesAlternateData);
     public void getNoteVersion(String authToken, int noteGuid, int updateSequenceNum, boolean withResourcesData, boolean withResourcesRecognition, boolean withResourcesAlternateData, APICallBack<Note> callback);
     public Resource getResource(String authToken, int guid, boolean withData, boolean withRecognition, boolean withAttributes, boolean withAlternateData);
     public void getResource(String authToken, int guid, boolean withData, boolean withRecognition, boolean withAttributes, boolean withAlternateData, APICallBack<Resource> callback);
     public LazyMap getResourceApplicationData(String authToken, int guid);
     public void getResourceApplicationData(String authToken, int guid, APICallBack<LazyMap> callback);
     public String getResourceApplicationDataEntry(String authToken, int guid, String key);
     public void getResourceApplicationDataEntry(String authToken, int guid, String key, APICallBack<String> callback);
     public int setResourceApplicationDataEntry(String authToken, int guid, String key, String value);
     public void setResourceApplicationDataEntry(String authToken, int guid, String key, String value, APICallBack<Integer> callback);
     public int unsetResourceApplicationDataEntry(String authToken, int guid, String key);
     public void unsetResourceApplicationDataEntry(String authToken, int guid, String key, APICallBack<Integer> callback);
     public int updateResource(String authToken, Resource resource);
     public void updateResource(String authToken, Resource resource, APICallBack<Integer> callback);
     public String getResourceData(String authToken, int guid);
     public void getResourceData(String authToken, int guid, APICallBack<String> callback);
     public Resource getResourceByHash(String authToken, int noteGuid, String contentHash, boolean withData, boolean withRecognition, boolean withAlternateData);
     public void getResourceByHash(String authToken, int guid, String contentHash, boolean withData, boolean withRecognition, boolean withAttributes, APICallBack<Resource> callback);
     public String getResourceRecognition(String authToken, int guid);
     public void getResourceRecognition(String authToken, int guid, APICallBack<String> callback);
     public String getResourceAlternateData(String authToken, int guid);
     public void getResourceAlternateData(String authToken, int guid, APICallBack<String> callback);
     public ResourceAttributes getResourceAttributes(String authToken, int guid);
     public void getResourceAttributes(String authToken, int guid, APICallBack<ResourceAttributes> callback);
     public Notebook getPublicNotebook(int userId, String publicUri);
     public void getPublicNotebook(int userId, String publicUri, APICallBack<Notebook> callback);
     public SharedNotebook createSharedNotebook(String authToken, SharedNotebook notebook);
     public void createSharedNotebook(String authToken, SharedNotebook notebook, APICallBack<SharedNotebook> callback);
     public int updateSharedNotebook(String authToken, SharedNotebook notebook);
     public void updateSharedNotebook(String authToken, SharedNotebook notebook, APICallBack<Integer> callback);
     public int sendMessageToSharedNotebookMembers(String authToken, int notebookGuid, String messageText, List<String> recipients);
     public void sendMessageToSharedNotebookMembers(String authToken, int notebookGuid, String messageText, List<String> recipients, APICallBack<Integer> callback);
     public List<SharedNotebook> listSharedNotebooks(String authToken);
     public void listSharedNotebooks(String authToken, APICallBack<List<SharedNotebook>> callback);
     public int expungeSharedNotebooks(String authToken, List<Integer> sharedNotebookIds);
     public void expungeSharedNotebooks(String authToken, List<Integer> sharedNotebookIds, APICallBack<Integer> callback);
     public LinkedNotebook createLinkedNotebook(String authToken, LinkedNotebook notebook);
     public void createLinkedNotebook(String authToken, LinkedNotebook notebook, APICallBack<LinkedNotebook> callback);
     public int updateLinkedNotebook(String authToken, LinkedNotebook notebook);
     public void updateLinkedNotebook(String authToken, LinkedNotebook notebook, APICallBack<Integer> callback);
     public List<LinkedNotebook> listLinkedNotebooks(String authToken);
     public void listLinkedNotebooks(String authToken, APICallBack<List<LinkedNotebook>> callback);
     public int expungeLinkedNotebook(String authToken, int guid);
     public void expungeLinkedNotebook(String authToken, int guid, APICallBack<Integer> callback);
     public AuthenticationResult authenticateToSharedNotebook(String shareKey, String authToken);
     public void authenticateToSharedNotebook(String shareKey, String authToken, APICallBack<AuthenticationResult> callback);
     public SharedNotebook getSharedNotebookByAuth(String authToken);
     public void getSharedNotebookByAuth(String authToken, APICallBack<SharedNotebook> callback);
     public void emailNote(String authToken, NoteEmailParameters params);
     public void emailNote(String authToken, NoteEmailParameters params, APICallBack<Void> callback);
     public String shareNote(String authToken, int guid);
     public void shareNote(String authToken, int guid, APICallBack<String> callback);
     public void stopSharingNote(String authToken, int guid);
     public void stopSharingNote(String authToken, int guid, APICallBack<Void> callback);
     public AuthenticationResult authenticateToSharedNote(String guid, String notekey);
     public void authenticateToSharedNote(String guid, String notekey, APICallBack<AuthenticationResult> callback);
     public RelatedResult findRelated(String authToken, RelatedQuery query, RelatedResultSpec resultSpec);
     public void findRelated(String authToken, RelatedQuery query, RelatedResultSpec resultSpec, APICallBack<RelatedResult> callback);
   }
   
 
   /**
    * Delegation methods.
    */
   protected final String getAuthToken() {
     checkInitialized();
     return session.getAuthToken();
   }
 
   protected final boolean isLoggedIn() {
     checkInitialized();
     return session.isLoggedIn();
   }
 
 
   /**
    * Utilities for other methods.
    */
   private void checkInitialized() {
     if (session == null) {throw new NotInitializedException();}
   }
 
   public class NotInitializedException extends RuntimeException {}
 }
