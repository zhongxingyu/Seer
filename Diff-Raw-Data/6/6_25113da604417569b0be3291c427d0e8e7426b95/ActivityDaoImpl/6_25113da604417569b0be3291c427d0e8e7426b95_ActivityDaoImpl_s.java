 package it.antreem.birretta.service.dao.impl;
 
 import com.mongodb.*;
 import it.antreem.birretta.service.dao.ActivityDao;
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.model.Activity;
 import java.util.ArrayList;
 import java.util.Date;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bson.types.ObjectId;
 
 /**
  *
  * @author gmorlini
  */
 public class ActivityDaoImpl extends AbstractMongoDao implements ActivityDao{
      public final static String ACTIVITY_COLLNAME = "activities";
      
      private static final Log log = LogFactory.getLog(ActivityDaoImpl.class);
      
     @Override
     public ArrayList<Activity> findByUser(String user) throws DaoException {
         DB db = null;
         ArrayList<Activity> list = new ArrayList<Activity>();
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection activities = db.getCollection(ACTIVITY_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("idUser", user);
             DBCursor cur = activities.find(query);
             
             while (cur.hasNext()){
                 DBObject _b = cur.next();
                 Activity act = createActivityFromDBObject(_b);
                 list.add(act);
             }
            
         }
         catch(MongoException ex){
             log.error(ex.getLocalizedMessage(), ex);
             throw new DaoException(ex.getLocalizedMessage(), ex);
         }
         finally {
             if (db != null){
                 db.requestDone();
             }
         }
         return list;
     }
 
     @Override
     public Activity findById(String id) throws DaoException {
          DBObject obj = findById(id, ACTIVITY_COLLNAME);
         if (obj == null) return null;
         Activity a = createActivityFromDBObject(obj);
         return a;
     }
 
     @Override
     public int saveActivity(Activity b) throws DaoException {
          DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection activities = db.getCollection(ACTIVITY_COLLNAME);
             BasicDBObject activity = createDBObjectFromActivity(b);
             return activities.insert(activity).getN();
         }
         catch(MongoException ex){
             log.error(ex.getLocalizedMessage(), ex);
             throw new DaoException(ex.getLocalizedMessage(), ex);
         }
         finally {
             if (db != null){
                 db.requestDone();
             }
         }
     }
 
     private BasicDBObject createDBObjectFromActivity(Activity a) {
         BasicDBObject _a = new BasicDBObject();
        // _a.put("idActivity", a.getIdActivity());
         _a.put("image", a.getImage());
         _a.put("displayName", a.getDisplayName());
         _a.put("idUser", a.getIdUser());
         _a.put("idBeer", a.getIdBeer());
         _a.put("idPlace", a.getIdPlace());
         _a.put("idFriend", a.getIdFriend());
         _a.put("beerName", a.getBeerName());
         _a.put("placeName", a.getPlaceName());
         _a.put("friendName", a.getFriendName());
         _a.put("type", a.getType());
         _a.put("status", a.getStatus());
         _a.put("like", a.getLike());
         _a.put("date", a.getDate());
         _a.put("jumpTo",a.getJumpTo());
         return _a;
     }
 
     private Activity createActivityFromDBObject(DBObject obj) {
         Activity a = new Activity();
         a.setId((ObjectId) obj.get("_id"));
         a.setImage((String) obj.get("image"));
         a.setDisplayName((String) obj.get("displayName"));
         a.setIdUser((String) obj.get("idUser"));
         a.setIdBeer((String) obj.get("idBeer"));
         a.setIdPlace((String) obj.get("idPlace"));
         a.setIdFriend((String) obj.get("idFriend"));
         a.setBeerName((String) obj.get("beerName"));
         a.setPlaceName((String) obj.get("placeName"));
         a.setFriendName((String) obj.get("friendName"));
         a.setType((Integer) obj.get("type"));
         a.setStatus((Integer) obj.get("status"));
         a.setLike((Integer) obj.get("like"));
         a.setJumpTo((String)obj.get("jumpTo"));
        String date=(String) obj.get("date");
        if(date!=null && !date.equals(""))
              a.setDate(new Date(date));
         return a;
     }
     
 }
