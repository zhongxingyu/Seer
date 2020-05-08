 package it.antreem.birretta.service.dao.impl;
 
 import com.mongodb.*;
 import it.antreem.birretta.service.dao.DaoException;
 import it.antreem.birretta.service.dao.DaoFactory;
 import it.antreem.birretta.service.dao.FriendDao;
 import it.antreem.birretta.service.model.Friend;
 import it.antreem.birretta.service.model.FriendsRelation;
 import it.antreem.birretta.service.model.User;
 import it.antreem.birretta.service.util.FriendStatusCodes;
 import java.util.ArrayList;
 import java.util.Date;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author alessio
  */
 public class FriendDaoImpl extends AbstractMongoDao implements FriendDao
 {
     public final static String FRIENDS_RELATION_COLLNAME = "users";
     
     private static final Log log = LogFactory.getLog(FriendDaoImpl.class);
     
     @Override
     public int saveFriend(String id1, String id2) throws DaoException 
     {
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection friends = db.getCollection(FRIENDS_RELATION_COLLNAME);
             BasicDBObject req = new BasicDBObject();
             req.put("id_1", id1);
             req.put("id_2", id2);
             return friends.insert(req).getN();
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
     public int deleteFriend(String id1, String id2) throws DaoException 
     {
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection friends = db.getCollection(FRIENDS_RELATION_COLLNAME);
             BasicDBObject req = new BasicDBObject();
             req.put("id_1", id1);
             req.put("id_2", id2);
             int a = friends.remove(req).getN();
             req = new BasicDBObject();
             req.put("id_1", id2);
             req.put("id_2", id1);
             int b = friends.remove(req).getN();
             return a+b;
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
     public ArrayList<Friend> getAllFriends(int maxElement) throws DaoException 
     {
         ArrayList<Friend> list=new ArrayList<Friend>();
         DB db = null;
         try
         {
             db = getDB();
             db.requestStart();
             DBCollection users = db.getCollection(FRIENDS_RELATION_COLLNAME);
       /*      BasicDBObject param = new BasicDBObject();
             query.put("id_1", maxElement);*/
             DBCursor cur = users.find();
        //     DBCursor curOrdered= cur.sort(param);            
             if(maxElement>0)
                    cur.limit(maxElement);
 
             while (cur.hasNext()){
                 DBObject _f = cur.next();
                 Friend f = createFriendFromDBObject(_f);
                list.add(f);
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
    public ArrayList<Friend> getAllMyFriends(int maxElemet, String id_user) {
         ArrayList<Friend> list= new ArrayList<Friend>();
        ArrayList<FriendsRelation> myFriends = DaoFactory.getInstance().getFriendRelationDao().getMyFriends(id_user, maxElemet);
         for(FriendsRelation friendRelation: myFriends)
         {
             User user = DaoFactory.getInstance().getUserDao().findById(friendRelation.getIdUser2());
             Friend friend = createFriendFromUserAndRelation(user,friendRelation);
             list.add(friend);
         }
         return list;
     }
     private Friend createFriendFromDBObject(DBObject obj) {
         Friend f=new Friend();
          f.setIdUser((String) obj.get("idUser"));
          //friend non è un un oggetto mongodb
    //     f.setId((ObjectId) obj.get("_id"));
         f.setUsername((String) obj.get("username"));
         f.setDisplayName((String)obj.get("displayName"));
         f.setFirstName((String) obj.get("firstName"));
         f.setLastName((String) obj.get("lastName"));
         f.setDescription((String)obj.get("description"));
         f.setEmail((String)obj.get("email"));
         f.setGender((Integer) obj.get("gender"));
         f.setNationality((String)obj.get("nationality"));
         
         String birthDate=(String)obj.get("birthDate");
         if(birthDate!=null && !birthDate.equals(""))
              f.setBirthDate(new Date(birthDate));
         
         f.setAvatar((String)obj.get("avatar"));
         
       
         f.setRole((Integer) obj.get("role"));
         f.setStatus((Integer) obj.get("status"));
       
         
         String activatedOn=(String)obj.get("activatedOn");
         if(activatedOn!=null && !activatedOn.equals(""))
                 f.setActivatedOn(new Date(activatedOn));
         
         String lastLoginOn=(String)obj.get("lastLoginOn");
         if(lastLoginOn!=null && !lastLoginOn.equals(""))
                 f.setActivatedOn(new Date(lastLoginOn));
         
         f.setBadges((String)obj.get("badges"));
         f.setFavorites((String) obj.get("favorites"));
         f.setLiked((String)obj.get("liked"));
         f.setCounter_checkIns((Integer)obj.get("counterCheckIns"));
         f.setCounter_friends((Integer)obj.get("counterFriends"));
         f.setCounter_badges((Integer)obj.get("counterBadges"));
         
         /** 
          * campi user non necessari in friend
          * 
           f.setShareFacebook((obj.get("shareFacebook")!=null ? (Boolean) obj.get("shareFacebook") : false));
         f.setShareTwitter((obj.get("shareFacebook")!=null ? (Boolean)obj.get("shareTwitter"): false));
         f.setEnableNotification(obj.get("enableNotification")!=null?(Boolean)obj.get("enableNotification"): false );
           f.setPwdHash((String) obj.get("pwdHash"));
           **/
         return f;  
     }
 
     private Friend createFriendFromUserAndRelation(User user, FriendsRelation friendRelation) {
         Friend f = new Friend();
         f.setIdUser(user.getIdUser());
         //friend non è un un oggetto mongodb
         //     f.setId((ObjectId) obj.get_id());
         f.setUsername(user.getUsername());
         f.setDisplayName(user.getDisplayName());
         f.setFirstName(user.getFirstName());
         f.setLastName(user.getLastName());
         f.setDescription(user.getDescription());
         f.setEmail(user.getEmail());
         f.setGender(user.getGender());
         f.setNationality(user.getNationality());
         f.setBirthDate(user.getBirthDate());
         f.setAvatar(user.getAvatar());
         f.setRole(user.getRole());
 
         //impostazione status:
         int status = 0;
         if (friendRelation.isFriend()) //richiesta accettata
         {
             status = FriendStatusCodes.FRIEND.getStatus();
         } else //il mio amico deve ancora accettare
         {
             status = FriendStatusCodes.PENDING.getStatus();
         }
 
         f.setStatus(status);
         f.setActivatedOn(user.getActivatedOn());
         f.setActivatedOn(user.getLastLoginOn());
         f.setBadges(user.getBadges());
         f.setFavorites(user.getFavorites());
         f.setLiked(user.getLiked());
         f.setCounter_checkIns(user.getCounterCheckIns());
         f.setCounter_friends(user.getCounterFriends());
         f.setCounter_badges(user.getCounterBadges());
 
         return f;
     }
     
 }
