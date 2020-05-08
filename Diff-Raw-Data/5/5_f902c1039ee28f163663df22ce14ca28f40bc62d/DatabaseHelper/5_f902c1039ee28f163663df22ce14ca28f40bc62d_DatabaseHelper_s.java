 package com.fh.voting.db;
 
 import java.io.File;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.db4o.Db4oEmbedded;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 import com.db4o.query.Predicate;
 import com.fh.voting.model.User;
 import com.fh.voting.model.Vote;
 import com.fh.voting.model.VoteLookup;
 
 public class DatabaseHelper {
 	private static ObjectContainer oc = null;
 	private Context context;
 
 	public DatabaseHelper(Context ctx) {
 		context = ctx;
 	}
 
 	private ObjectContainer db() {
 		try {
 			if (oc == null || oc.ext().isClosed())
 				oc = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), db4oDBFullPath(context));
 			return oc;
 		} catch (Exception e) {
 			Log.e(DatabaseHelper.class.getName(), e.toString());
 			return null;
 		}
 	}
 
 	public String db4oDBFullPath(Context ctx) {
 		return ctx.getDir("data", 0) + "/" + "android.db4o";
 	}
 
 	public void close() {
 		if (oc != null) {
 			oc.close();
 			oc = null;
 		}
 	}
 
 	public void commit() {
 		db().commit();
 	}
 
 	public void rollback() {
 		db().rollback();
 	}
 
 	public void deleteDatabase() {
 		close();
 		new File(db4oDBFullPath(context)).delete();
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void syncItem(DataTransferObject obj) throws Exception {
 		// check id
 		if (obj.id == 0) {
 			throw new InvalidParameterException();
 		}
 
 		// kill old item(s)
 		Class cls = obj.getClass();
 		ObjectSet<DataTransferObject> result = db().query(cls);
 		while (result.hasNext()) {
 			DataTransferObject old = result.next();
 			if (old.getId() == obj.getId()) {
 				db().delete(old);
 			}
 		}
 		db().commit();
 
 		// store new item
 		db().store(obj);
 		db().commit();
 	}
 
 	@SuppressWarnings("serial")
 	public List<Vote> get_votesByOwner(User owner) {
 		final User o = owner;
 		return db().query(new Predicate<Vote>() {
 			public boolean match(Vote vote) {
 				return vote.getAuthorId() == o.getId();
 			}
 		});
 	}
 
 	public List<Vote> get_topVotes(User m_user) {
 		List<Vote> votes = db().query(Vote.class);
 		List<Vote> result = new ArrayList<Vote>();
 		for (Vote vote : votes) {
 			// filter user votes
 			if (vote.getAuthorId() == m_user.getId()) {
 				continue;
 			}
 
 			// filter not public and ended votes
 			if (vote.getStatus() != Vote.Status.Public && vote.getStatus() != Vote.Status.Started) {
 				continue;
 			}
 
 			result.add(vote);
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings("serial")
 	public User getUser(String phoneId) {
 		final String id = phoneId;
 		return db().query(new Predicate<User>() {
 			public boolean match(User user) {
 				return user.getPhoneId().equals(id);
 			}
 		}).next();
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public DataTransferObject getById(Class cls, int id) {
 		ObjectSet<DataTransferObject> items = db().query(cls);
 		while (items.hasNext()) {
 			DataTransferObject obj = items.next();
 			if (obj.getId() == id) {
 				return obj;
 			}
 		}
 
 		return null;
 	}
 
 	public List<User> getUsers() {
 		return db().query(User.class);
 	}
 
 	public VoteLookup getVoteLookup() {
 		ObjectSet<VoteLookup> items = db().query(VoteLookup.class);
 		if (items.hasNext())
 			return items.next();
 		else
 			return new VoteLookup();
 	}
 
 	public void saveVoteLookup(VoteLookup lookup) {
 		db().store(lookup);
 		db().commit();
 	}
 }
