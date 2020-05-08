 
 package se.slide.timy.service;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.text.format.DateFormat;
 import android.util.Log;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
 import com.google.api.client.googleapis.json.GoogleJsonResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.client.util.DateTime;
 import com.google.api.services.calendar.CalendarScopes;
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.EventDateTime;
 
 import se.slide.timy.db.DatabaseManager;
 import se.slide.timy.model.Project;
 import se.slide.timy.model.Report;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.List;
 
 public class SyncService extends Service {
     
     public static final int GOOD_RESULT = 0;
     public static final int ERROR_BAD_ACCOUNT = 1;
     public static final int ERROR_BAD_CALENDAR = 2;
     public static final int ERROR_NETWORK = 3;
     
     private static final String TAG = "SyncService";
 
     private boolean mIsTaskRunning;
     
     public int retries = 0;
     
     public static final int MAX_RETRIES = 10;
     
     private CreateCalendarEventsTask mTask;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         Log.d(TAG, "Created service");
         
         // ORMLite needs to be initiated
         DatabaseManager.init(this);
         
         mIsTaskRunning = false;
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
      */
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "Started service");
         
         createEvents(false);
 
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "Destroyed service.");
         
         mIsTaskRunning = false;
     }
 
     // Uses AsyncTask to download the XML feed from the server.
     public void createEvents(boolean retry) {
         if (mTask == null || retry)
             mTask = createTask();
         
         if (!mTask.getStatus().equals(AsyncTask.Status.PENDING))
             mTask = createTask();
         
         if (!mIsTaskRunning)
             mTask.execute();
         
     }
     
     public void runAgain(List<Project> projects) {
         updateSyncedEvents(projects);
         
         // Check to see if we got reports added to the database while we were busy creating calendar events
         if (DatabaseManager.getInstance().haveUnsyncedReports())
             createEvents(false);
     }
     
     public void updateSyncedEvents(List<Project> projects) {
         for (int i = 0; i < projects.size(); i++) {
             List<Report> reports = projects.get(i).getReports();
             
             for (int a = 0; a < reports.size(); a++)
                 DatabaseManager.getInstance().updateReport(reports.get(a));
         }
     }
     
     private CreateCalendarEventsTask createTask() {
         Log.d(TAG, "Create new task");
         
         String accountName = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_google_calendar_account", null);
         String calendarId = PreferenceManager.getDefaultSharedPreferences(this).getString("sync_google_calendar_calendar_id", null);
         
         List<Project> projects = DatabaseManager.getInstance().getProjectsWithUnsyncedReports();
         
         return new CreateCalendarEventsTask(this, accountName, calendarId, projects);
     }
 
     private class CreateCalendarEventsTask extends AsyncTask<String, Void, Integer> {
         
         private WeakReference<Service> weakService;
         private String accountName;
         private String calendarId;
         private List<Project> projects;
         
         public CreateCalendarEventsTask(Service service, String accountName, String calendarId, List<Project> projects) {
             weakService = new WeakReference<Service>(service);
             this.accountName = accountName;
             this.calendarId = calendarId;
             this.projects = projects;
         }
         
         /* (non-Javadoc)
          * @see android.os.AsyncTask#onPreExecute()
          */
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             
             mIsTaskRunning = true;
         }
 
         @Override
         protected Integer doInBackground(String... urls) {
             int result = SyncService.GOOD_RESULT;
 
             if (accountName == null)
                 return SyncService.ERROR_BAD_ACCOUNT;
             if (calendarId == null)
                 return SyncService.ERROR_BAD_CALENDAR;
             
             GoogleAccountCredential credential;
             final com.google.api.services.calendar.Calendar client;
             final HttpTransport transport = AndroidHttp.newCompatibleTransport();
             final JsonFactory jsonFactory = new GsonFactory();
             
             // Google Accounts
             credential = GoogleAccountCredential.usingOAuth2(weakService.get(), CalendarScopes.CALENDAR);
             credential.setSelectedAccountName(accountName);
             // Calendar client
             client = new com.google.api.services.calendar.Calendar.Builder(
                 transport, jsonFactory, credential).setApplicationName("Timy/1.0")
                 .build();
             
             for (int i = 0; i < projects.size(); i++) {
                 Project project = projects.get(i);
                 List<Report> reports = project.getReports();
                 
                 for (int a = 0; a < reports.size(); a++) {
                     Report report = reports.get(a);
                     
                     String allDayTime = DateFormat.format("yyyy-MM-dd", report.getDate()).toString();
                     DateTime dt = new DateTime(allDayTime);    
                     
                     StringBuilder builder = new StringBuilder();
                     builder.append(project.getName());
                     builder.append(": ");
                     if (report.getHours() > 0) {
                         builder.append(report.getHours());
                         builder.append("h");
                     }
                     if (report.getMinutes() > 0) {
                         builder.append(report.getMinutes());
                         builder.append("m");
                     }
                     
                     Event event = new Event();
                     event.setSummary(builder.toString());
                     event.setStart(new EventDateTime().setDate(dt));
                     event.setEnd(new EventDateTime().setDate(dt));
                     
                     boolean update = false;
                     if (report.getGoogleCalendarEventId() != null && report.getGoogleCalendarEventId().length() > 0) {
                         update = true;
                         event.setId(report.getGoogleCalendarEventId());
                     }
                     
                     try {
                         Event createdEvent = null;
                         if (update)
                             createdEvent = client.events().update(calendarId, report.getGoogleCalendarEventId(), event).execute();
                         else
                             createdEvent = client.events().insert(calendarId, event).execute();
                         
                         report.setGoogleCalendarEventId(createdEvent.getId());
                         report.setGoogleCalendarSync(true);
                         Log.d(TAG, "Created event");
                     } catch (GoogleJsonResponseException e) {
                         if (e.getStatusCode() == 400) {
                             Log.d(TAG, "Error creating event");
                             
                             // The event has most likely been manually removed, so clear the eventId
                             report.setGoogleCalendarEventId(null);
                         }
                         e.printStackTrace();
                         
                         result = SyncService.ERROR_NETWORK;
                         
                     } catch (IOException e) {
                         e.printStackTrace();
                         
                         result = SyncService.ERROR_NETWORK;
                         
                     }   
                 }
                 
                 
             }
             
             return result;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             super.onPostExecute(result);
             
             mIsTaskRunning = false;
             
             if (result == GOOD_RESULT)
                 runAgain(projects);
             else if (result == ERROR_NETWORK) {
                 if (retries++ < MAX_RETRIES) {
                     Log.d(TAG, "Retry to create events");
                     updateSyncedEvents(projects); // Comment this line to test MAX_RETRIES
                     createEvents(true);
                 }
                 else {
                     retries = 0;
                     // Show notification error
                 }
             }
                 
         }
     }
 
 }
