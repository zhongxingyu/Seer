 package au.com.dius.resilience.persistence.repository.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import roboguice.inject.ContextSingleton;
 import android.os.AsyncTask;
 import android.util.Log;
 import au.com.dius.resilience.Constants;
 import au.com.dius.resilience.model.Impact;
 import au.com.dius.resilience.model.Incident;
 import au.com.dius.resilience.model.Photo;
 import au.com.dius.resilience.persistence.repository.IncidentRepository;
 import au.com.dius.resilience.persistence.repository.RepositoryCommandResult;
 import au.com.dius.resilience.persistence.repository.RepositoryCommandResultListener;
 
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.SaveCallback;
 
 /**
  * @author georgepapas
  */
 @ContextSingleton
 public class ParseIncidentRepository implements IncidentRepository,
     RepositoryCommandResultListener<Photo> {
 
   public static final String LOG_TAG = ParseIncidentRepository.class.getName();
 
   ParsePhotoRepository photoRepository;
 
   @Override
   public void findById(
       final RepositoryCommandResultListener<Incident> listener, final String id) {
     final ParseQuery query = new ParseQuery(Constants.TABLE_INCIDENT);
     query.getInBackground(id, new GetCallback() {
       @Override
       public void done(ParseObject parseObject, ParseException ex) {
         Incident incident = parseObjectToIncident(parseObject);
         incident.setId(id);
         listener.commandComplete(new RepositoryCommandResult<Incident>(
             ex == null, incident));
       }
     });
   }
 
   @Override
   public void save(final RepositoryCommandResultListener<Incident> listener,
       final Incident incident) {
     AsyncTask.execute(new Runnable() {
       @Override
       public void run() {
         final ParseObject parseObject = ParseObject.createWithoutData(Constants.TABLE_INCIDENT, incident.getId());
         try {
           if(parseObject.isDataAvailable()) {
             parseObject.fetchIfNeeded();
           }
         } catch (ParseException e) {
           listener.commandComplete(new RepositoryCommandResult<Incident>(
               false, incident));
           return;
         }
         
         updateParseIncidentAttributes(parseObject, incident);
 
         Log.d(LOG_TAG, "Saving incident in async task, thread is "
             + Thread.currentThread().getName());
         parseObject.saveEventually(new SaveCallback() {
           @Override
           public void done(ParseException ex) {
             if (incident.getId() == null) {
              Log.d(LOG_TAG, "Saved new incident " + parseObject.getObjectId() + " with photo " + (ex == null ? "succeeded." : "failed."));
             }
             else {
              Log.d(LOG_TAG, "Updated incident " + incident.getId() + " with photo " + (ex == null ? "succeeded." : "failed."));
             }
             incident.setId(parseObject.getObjectId());
             
             if (incident.getPhotos().size() > 0) {
               photoRepository = new ParsePhotoRepository(incident);
               photoRepository.save(listener, incident.getPhotos().get(0));
             }
             else {
               listener.commandComplete(new RepositoryCommandResult<Incident>(
                   ex == null, incident));
             }
           }
         });
       }
     });
   }
 
   private ParseObject updateParseIncidentAttributes(ParseObject parseObject,
       Incident incident) {
     parseObject.put(Constants.COL_INCIDENT_NAME, incident.getName());
     parseObject.put(Constants.COL_INCIDENT_CATEGORY, incident.getCategory());
     parseObject.put(Constants.COL_INCIDENT_SUBCATEGORY,
         incident.getSubCategory());
     parseObject.put(Constants.COL_INCIDENT_IMPACT, incident.getImpact().name());
     parseObject.put(Constants.COL_INCIDENT_CREATION_DATE,
         incident.getDateCreated());
     parseObject.put(Constants.COL_INCIDENT_NOTE, incident.getNote());
 
     return parseObject;
   }
 
   @Override
   public void findAll(final RepositoryCommandResultListener<Incident> listener) {
     ParseQuery query = new ParseQuery(Constants.TABLE_INCIDENT);
     query.findInBackground(new FindCallback() {
       @Override
       public void done(List<ParseObject> results, ParseException ex) {
         List<Incident> incidentList = new ArrayList<Incident>();
         if (ex == null) {
           incidentList.addAll(toIncidentList(results));
         }
         listener.commandComplete(new RepositoryCommandResult<Incident>(
             ex == null, incidentList));
       }
     });
   }
 
   @Override
   public void findClosest(RepositoryCommandResultListener<Incident> listener) {
 
   }
 
   @Override
   public void findTracked(RepositoryCommandResultListener<Incident> listener) {
 
   }
 
   private List<Incident> toIncidentList(List<ParseObject> parseArray) {
 
     List<Incident> incidents = new ArrayList<Incident>();
     for (ParseObject pObject : parseArray) {
       Incident incident = parseObjectToIncident(pObject);
       incidents.add(incident);
     }
     return incidents;
 
   }
 
   private Incident parseObjectToIncident(ParseObject pObject) {
     String id = pObject.getString(Constants.COL_ID);
     String name = pObject.getString(Constants.COL_INCIDENT_NAME);
     String category = pObject.getString(Constants.COL_INCIDENT_CATEGORY);
     String subCategory = pObject.getString(Constants.COL_INCIDENT_SUBCATEGORY);
     String impact = pObject.getString(Constants.COL_INCIDENT_IMPACT);
     long creationDate = pObject.getLong(Constants.COL_INCIDENT_CREATION_DATE);
     String note = pObject.getString(Constants.COL_INCIDENT_NOTE);
 
     Impact impactScale = Impact.valueOf(impact);
     Incident incident = new Incident(id, name, creationDate, note, category,
         subCategory, impactScale);
 
     return incident;
   }
 
   @Override
   public void commandComplete(RepositoryCommandResult<Photo> result) {
 
   }
 }
