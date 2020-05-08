 package deus.core.access.storage.user.inmemory;
 
 import java.util.HashMap;
 
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.stereotype.Component;
 
 import deus.core.access.storage.user.api.UserDao;
 import deus.core.access.storage.user.model.UserMetadataPO;
 import deus.core.access.storage.user.model.UserPO;
 import deus.model.user.UserMetadata;
 import deus.model.user.id.UserId;
 
 /**
  * User DAO allows access to UserID and UserMetadata as a facade to the UserPO entities.
  * 
  * @author cpn
  * 
  */
 @Component
public class UserDaoInmemoryImpl extends HibernateDaoSupport implements UserDao {
 
 	static HashMap<String, UserPO> userPoMap = new HashMap<String, UserPO>();
 
 	@Override
 	public void addNewEntity(UserId userId) {
 		UserPO userPO = new UserPO();
 		userPO.setUserId(userId);
 		userPO.setUserMetadataPO(new UserMetadataPO());
 		userPoMap.put(userId.getUsername(), userPO);
 	}
 
 
 	@Override
 	public void deleteByNaturalId(String concPrimKey) {
 		userPoMap.remove(concPrimKey);
 	}
 
 
 	@Override
 	public UserId getByNaturalId(String concPrimKey) {
 		return userPoMap.get(concPrimKey).getUserId();
 	}
 
 
 	@Override
 	public void updateEntity(UserId userId) {
 		UserPO entity = userPoMap.get(userId.getId());
 		entity.setUserId(userId);
 	}
 
 
 	@Override
 	public UserMetadata getUserMetadata(UserId userId) {
 		return userPoMap.get(userId.getId()).getUserMetadataPO();
 	}
 
 
 	@Override
 	public void updateUserMetadata(UserId userId, UserMetadata userMetadata) {
 		UserPO entity = userPoMap.get(userId.getId());
 		entity.getUserMetadataPO().update(userMetadata);
 	}
 
 }
