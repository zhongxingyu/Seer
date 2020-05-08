 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.SqlRow;
 import com.avaje.ebean.validation.NotNull;
 
 @Entity
 @Table(name = "user_like")
 public class UserLike extends Model {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	public Long id;
 	
 	@Column(name = "created_at")
 	public Date createdAt;
 	
 	@NotNull
 	public Integer uid;
 	
 	@NotNull
 	public Long feedId;
 	
 	@ManyToOne
 	@JoinColumn(name = "uid", referencedColumnName = "uid", insertable = false, updatable = false)
 	public User user;
 	
 	@Override
 	public String toString() {
 		return "ユーザ : " + user.name + "は,フィード : " + feedId + "をLikeしてます。";
 	}
 	
 	
 	/** finder */
 	private static Finder<Long, UserLike> find = new Finder<Long, UserLike>(Long.class, UserLike.class);
 	
 	
 	// -------------------------
 	
 	
 	public static List<UserLike> findAllJoinUser() {
 		return find
 				.fetch("user")
 				.findList();
 	}
 	
 	
 	public static List<UserLike> findAllJoinUserUsingSql() {
 		final String SQL = "SELECT ul.id, ul.created_at, ul.uid, ul.feed_id, u.name FROM user_like ul " +
 				"LEFT OUTER JOIN user u ON ul.uid = u.uid";
 		List<SqlRow> sqlRows = Ebean.createSqlQuery(SQL).findList();
 		List<UserLike> userLikes = new ArrayList<UserLike>();
 		for (SqlRow sqlRow : sqlRows) {
 			UserLike userLike = new UserLike();
 			User user = new User();
 			userLike.user = user;
			if (sqlRow.containsKey("id"))
 				userLike.id = sqlRow.getLong("id");
 			if (sqlRow.containsKey("created_at"))
 				userLike.createdAt = sqlRow.getUtilDate("created_at");
 			if (sqlRow.containsKey("uid")) {
 				userLike.uid = sqlRow.getInteger("uid");
 				user.uid = userLike.uid;
 			}
 			if (sqlRow.containsKey("feed_id"))
 				userLike.feedId = sqlRow.getLong("feed_id");
 			if (sqlRow.containsKey("name"))
 				user.name = sqlRow.getString("name");
 			
 			userLikes.add(userLike);
 		}
 		
 		return userLikes;
 	}
 	
 	public static List<UserLike> findByUid(Integer uid) {
 		return find
 				.fetch("user")
 				.where()
 				.eq("uid", uid)
 				.findList();
 	}
 	
 	public static List<UserLike> findByUidUsingSql(Integer uid) {
 		// ＊使ってもおｋ
 //		final String SQL = "SELECT * FROM user_like ul " +
 //				"LEFT OUTER JOIN user u ON ul.uid = u.uid " +
 //				"WHERE ul.uid = " + uid;
 		final String SQL = "SELECT ul.id, ul.created_at, ul.uid, ul.feed_id, u.name FROM user_like ul " +
 				"LEFT OUTER JOIN user u ON ul.uid = u.uid " +
 				"WHERE ul.uid = " + uid;
 		List<SqlRow> sqlRows = Ebean.createSqlQuery(SQL).findList();
 		List<UserLike> userLikes = new ArrayList<UserLike>();
 		for (SqlRow sqlRow : sqlRows) {
 			UserLike userLike = new UserLike();
 			User user = new User();
 			userLike.user = user;
			if (sqlRow.containsKey("id"))
 				userLike.id = sqlRow.getLong("id");
 			if (sqlRow.containsKey("created_at"))
 				userLike.createdAt = sqlRow.getUtilDate("created_at");
 			if (sqlRow.containsKey("uid")) {
 				userLike.uid = sqlRow.getInteger("uid");
 				user.uid = userLike.uid;
 			}
 			if (sqlRow.containsKey("feed_id"))
 				userLike.feedId = sqlRow.getLong("feed_id");
 			if (sqlRow.containsKey("name"))
 				user.name = sqlRow.getString("name");
 			
 			userLikes.add(userLike);
 		}
 		
 		return userLikes;
 	}
 
 }
