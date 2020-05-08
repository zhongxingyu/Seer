 package ru.alepar.tdt.backend.dao.memcached;
 
 import com.googlecode.objectify.Key;
 import ru.alepar.tdt.backend.dao.UserAccountDao;
 import ru.alepar.tdt.backend.model.UserAccount;
 
import java.util.Map;

 /**
  * User: looser
  * Date: 11.07.2010
  */
 public class UserAccountMemcachedDao implements UserAccountDao {
     private final UserAccountDao delegate;
 
     public UserAccountMemcachedDao(UserAccountDao delegate) {
         this.delegate = delegate;
     }
 
     @Override
     public Key<UserAccount> insert(UserAccount userAccount) {
         // invalidate cache
         return delegate.insert(userAccount);
     }
 
     @Override
     public UserAccount find(Key<UserAccount> id) {
         return delegate.find(id);
     }
 
     @Override
     public UserAccount find(String id) {
         // try to find in the cache
         // if not found get from delegate
         UserAccount obj = delegate.find(id);
         // put obj to cache
         return obj;
     }

    @Override
    public Map<Key<UserAccount>, UserAccount> find(Iterable<Key<UserAccount>> keys) {
        return delegate.find(keys);
    }
 }
