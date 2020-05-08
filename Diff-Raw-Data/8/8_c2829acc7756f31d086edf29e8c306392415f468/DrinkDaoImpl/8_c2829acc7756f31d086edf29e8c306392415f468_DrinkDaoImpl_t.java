 package it.antreem.birretta.service.dao.impl;
 
 import com.mongodb.*;
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.dao.DaoFactory;
 import it.antreem.birretta.service.dao.DrinkDao;
 import it.antreem.birretta.service.model.Drink;
 import it.antreem.birretta.service.model.User;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bson.types.ObjectId;
 
 /**
  *
  * @author alessio
  */
 public class DrinkDaoImpl extends AbstractMongoDao implements DrinkDao
 {
     public final static String DRINKS_COLLNAME = "drinks";
     
     private static final Log log = LogFactory.getLog(DrinkDaoImpl.class);
     
     @Override
     public int saveDrink(Drink d) throws DaoException 
     {
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection drinks = db.getCollection(DRINKS_COLLNAME);
             BasicDBObject drink = createDBObjectFromDrink(d);
             return drinks.insert(drink).getN();
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
     public Drink findLastDrinkByUsername(String username) throws DaoException {
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         if (u == null) return null;
         String idUser = u.getIdUser();
         
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection beers = db.getCollection(DRINKS_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("id_user", idUser);
             DBCursor cur = beers.find(query).sort(new BasicDBObject("timestamp", -1));
             
             while (cur.hasNext()){
                 DBObject _d = cur.next();
                 Drink d = createDrinkFromDBObject(_d);
                 return d;
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
     public List<Drink> findDrinksByUsername(String username, Integer limit) throws DaoException 
     {
         List<Drink> list = new ArrayList<Drink>();
         
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         if (u == null) return list;
         String idUser = u.getIdUser();
         
         if (limit == null || limit < 0){
             limit = 10;
         }
         
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection drinks = db.getCollection(DRINKS_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("id_user", idUser);
             DBCursor cur = drinks.find(query).sort(new BasicDBObject("timestamp", -1)).limit(limit);
             
             while (cur.hasNext()){
                 DBObject _d = cur.next();
                 Drink d = createDrinkFromDBObject(_d);
                 list.add(d);
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
     public List<Drink> findRecentDrinks(String username, Integer minutesAgo) throws DaoException 
     {
         if (minutesAgo == null || minutesAgo < 0 || minutesAgo > 3600){
             minutesAgo = 10;
         }
         
         List<Drink> list = new ArrayList<Drink>();
         
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         if (u == null) return list;
         String idUser = u.getIdUser();
         
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.MINUTE, -minutesAgo);
         Date _date = cal.getTime();
         
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection drinks = db.getCollection(DRINKS_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("id_user", idUser);
             query.put("timestamp", new BasicDBObject("$gte", _date));
             DBCursor cur = drinks.find(query);
             
             while (cur.hasNext()){
                 DBObject _d = cur.next();
                 Drink d = createDrinkFromDBObject(_d);
                 list.add(d);
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
     public int countDrinksByUsername(String username) throws DaoException 
     {
         User u = DaoFactory.getInstance().getUserDao().findUserByUsername(username);
         if (u == null) return 0;
         String idUser = u.getIdUser();
         
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection drinks = db.getCollection(DRINKS_COLLNAME);
             BasicDBObject query = new BasicDBObject();
             query.put("id_user", idUser);
             return drinks.find(query).count();
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
     public ArrayList<Drink> getDrinksList(Integer maxElement) throws DaoException 
     {
         ArrayList<Drink> list = new ArrayList<Drink>();
         
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection drinks = db.getCollection(DRINKS_COLLNAME);
             BasicDBObject param = new BasicDBObject();
             //ordinamento crescente(1) per il nome
             //decrescente sarebbe stato -1
             param.put("username", 1);
             log.info("sort param:"+param.toString());
             DBCursor cur=null;
             cur = drinks.find();
             DBCursor curOrdered= cur.sort(param);            
             if(maxElement>0)
                    curOrdered.limit(maxElement);
             while (curOrdered.hasNext()){
                 DBObject _d = curOrdered.next();
                 Drink d = createDrinkFromDBObject(_d);
                 list.add(d);
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
     
     
     protected static Drink createDrinkFromDBObject(DBObject obj)
     {
         Drink d = new Drink();
         
         d.setId((ObjectId) obj.get("_id"));
         d.setUsername((String)obj.get("username"));
         d.setImage((String)obj.get("image"));
         d.setIdUser((String) obj.get("id_user"));
         d.setIdBeer((String) obj.get("id_beer"));
         d.setIdPlace((String) obj.get("id_place"));
         d.setIdActivity((String)obj.get("idActivity"));
         d.setIdFeedback((String)obj.get("idFeedback"));
        d.setStatus((Integer)obj.get("status"));
        d.setScore((Integer)obj.get("score"));
        String date=(String)obj.get("timestamp");
        if(date!=null && !date.equals(""))
              d.setTimestamp(new Date(date));
         return d;
     }
     
     // For first insert
     protected static BasicDBObject createDBObjectFromDrink (Drink d)
     {
         BasicDBObject _d = new BasicDBObject();
         _d.put("username",d.getUsername());
         _d.put("image", d.getImage());
         _d.put("id_user", d.getIdUser());
         _d.put("id_beer", d.getIdBeer());
         _d.put("id_place", d.getIdPlace());
         _d.put("id_activity", d.getIdActivity());
         _d.put("id_feedback", d.getIdFeedback());
         _d.put("status", d.getStatus());
         _d.put("score",d.getScore());
         _d.put("timestamp", d.getTimestamp());
         return _d;
     }
 }
