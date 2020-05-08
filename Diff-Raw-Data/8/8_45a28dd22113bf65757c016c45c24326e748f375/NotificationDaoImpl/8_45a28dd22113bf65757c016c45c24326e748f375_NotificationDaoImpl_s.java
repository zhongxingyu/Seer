 package it.antreem.birretta.service.dao.impl;
 
 import it.antreem.birretta.service.dao.NotificationDao;
 import com.mongodb.*;
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.model.Notification;
 import it.antreem.birretta.service.util.NotificationStatusCodes;
 import java.util.ArrayList;
 import java.util.Date;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bson.types.ObjectId;
 
 /**
  *
  * @author gmorlini
  */
 public class NotificationDaoImpl extends AbstractMongoDao implements NotificationDao{
     public final static String NOTIFICATION_COLLNAME = "notifications";
      
      private static final Log log = LogFactory.getLog(NotificationDaoImpl.class);
      
     @Override
     public ArrayList<Notification> findByUser(String user) throws DaoException {
         DB db = null;
         ArrayList<Notification> list = new ArrayList<Notification>();
         try
         {
             db = getDB();
             db.requestStart();
            DBCollection beers = db.getCollection(NOTIFICATION_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("idUser", user);
            DBCursor cur = beers.find(query);
             
             while (cur.hasNext()){
                 DBObject _b = cur.next();
                 Notification act = createNotificationFromDBObject(_b);
                 list.add(act);
             }
             
            return null;
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
 
     @Override
     public Notification findById(String id) throws DaoException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int saveNotification(Notification n) throws DaoException {
          DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection activities = db.getCollection(NOTIFICATION_COLLNAME);
             BasicDBObject activity = createDBObjectFromNotification(n);
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
     @Override
     public boolean setNotificationRead(Notification n)
     {
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection friendsrelations = db.getCollection(NOTIFICATION_COLLNAME);
             n.setStatus(NotificationStatusCodes.READ.getStatus());
             DBObject obj= createDBObjectFromNotification(n);
              WriteResult wr = friendsrelations.update(new BasicDBObject().append("_id", n.getIdNotification()),obj);
            if(wr.getN()<1)
            {
                log.error("impossibile fare update");
                return false;
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
         return true;
     }
     private BasicDBObject createDBObjectFromNotification(Notification n) {
         BasicDBObject _n = new BasicDBObject();
 //        _n.put("id_notification", n.getIdNotification());
         _n.put("idUser", n.getIdUser());
         _n.put("jumpTo", n.getJumpTo());
         _n.put("targetName", n.getTargetName());
         _n.put("idBeer", n.getIdBeer());
         _n.put("beerName", n.getBeerName());
         _n.put("idFriend", n.getIdFriend());
         _n.put("friendName", n.getFriendName());
         _n.put("idPlace", n.getIdPlace());
         _n.put("placeName", n.getPlaceName());
         //solo di notification
         _n.put("image", n.getImage());  
         _n.put("type", n.getType());
         _n.put("status", n.getStatus());
         _n.put("insertedOn", n.getInsertedOn());
         /*
         _n.put("status", n.getStatus());
         _n.put("like", n.getLike());
         _n.put("date", n.getDate());
         _n.put("idActivity", n.getIdActivity());
         _n.put("avatar", n.getAvatar());
         * */
         
         return _n;
     }
 
     private Notification createNotificationFromDBObject(DBObject obj) {
         Notification a = new Notification();
         a.setId((ObjectId) obj.get("_id"));
         //comuni ad Activity
         a.setIdUser((String)obj.get("idUser"));
         a.setJumpTo((String) obj.get("jumpTo"));
         a.setTargetName((String) obj.get("targetName"));
         a.setIdBeer((String) obj.get("idBeer"));      
         a.setBeerName((String) obj.get("beerName"));
         a.setIdPlace((String) obj.get("idPlace"));
         a.setPlaceName((String) obj.get("placeName"));
         a.setIdFriend((String) obj.get("idFriend"));
         a.setFriendName((String) obj.get("friendName"));
         //solo di notification
         a.setImage((String)obj.get("image"));
         a.setType((Integer)obj.get("type"));
         a.setStatus((Integer)obj.get("status"));
         String insertedOn=(String) obj.get("insertedOn");
         if(insertedOn!=null && !"".equals(insertedOn))
             a.setInsertedOn(new Date(insertedOn));
         /*Activity
         a.setType((Integer) obj.get("Type"));
         a.setStatus((Integer) obj.get("Status"));
         a.setLike((Integer) obj.get("Like"));
         a.setDate(new Date((String) obj.get("Date")));
                 a.setAvatar((String) obj.get("Avatar"));
                 * /
                 */
         return a;
     }
     
     @Override
     public int deleteNotificationByMongoID(String id) throws DaoException 
     {
         log.info("NotificationDaoImpl - deleteNotificationByMongoID - id: "+id);
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection nottab = db.getCollection(NOTIFICATION_COLLNAME);
             BasicDBObject req = new BasicDBObject();
             req.put("_id", id);
             int a = nottab.remove(req).getN();
             return a;
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
      
 }
