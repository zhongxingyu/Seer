 package models;
 
 import be.objectify.deadbolt.core.models.Permission;
 import be.objectify.deadbolt.core.models.Role;
 import be.objectify.deadbolt.core.models.Subject;
 import com.github.jmkgreen.morphia.annotations.Embedded;
 import com.github.jmkgreen.morphia.annotations.Entity;
 import com.github.jmkgreen.morphia.annotations.Id;
 import com.github.jmkgreen.morphia.annotations.Index;
 import com.github.jmkgreen.morphia.annotations.Indexes;
 import com.github.jmkgreen.morphia.mapping.Mapper;
 import com.github.jmkgreen.morphia.query.Query;
 import com.github.jmkgreen.morphia.query.UpdateOperations;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.mongodb.WriteConcern;
 import leodagdag.play2morphia.Model;
 import leodagdag.play2morphia.MorphiaPlugin;
 import org.apache.commons.lang3.StringUtils;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.joda.time.DateTime;
 import security.JSecurityRole;
 import security.SecurityRole;
 import utils.AppConfig;
 import utils.MD5;
 
 import javax.annotation.Nullable;
 import java.util.List;
 
 /**
  * @author f.patin
  */
 @Entity("User")
 @Indexes({
 	         @Index("username"),
 	         @Index("username, password"),
 	         @Index("lastName, firstName")
 })
 public class JUser extends Model implements Subject {
 
 	@Id
 	public ObjectId id;
 	public String username;
 	public String trigramme;
 	public String firstName;
 	public String lastName;
 	public String email;
 	public String role = SecurityRole.employee();
 	public ObjectId managerId;
 	public Boolean isManager = Boolean.FALSE;
 	@Embedded
 	public List<JAffectedMission> affectedMissions = Lists.newArrayList();
 	@SuppressWarnings({"unused"})
 	private String password;
 
 	private static Query<JUser> q() {
 		return MorphiaPlugin.ds().createQuery(JUser.class);
 	}
 
 	private static Query<JUser> queryToFindMe(final ObjectId id) {
 		return q()
 			       .field(Mapper.ID_KEY).equal(id);
 	}
 
 	private static Query<JUser> queryToFindMe(final String username) {
 		return q()
 			       .field("username").equal(username);
 	}
 
 	private static List<JAffectedMission> filterByDates(final List<JAffectedMission> xs, final DateTime startDate, final DateTime endDate) {
 		if(startDate == null || endDate == null) {
 			return xs;
 		}
 		return Lists.newArrayList(Collections2.filter(xs, new Predicate<JAffectedMission>() {
 			@Override
 			public boolean apply(@Nullable final JAffectedMission affectedMission) {
				return ((affectedMission.startDate == null || affectedMission.startDate.isBefore(startDate))
					        && (affectedMission.endDate == null || affectedMission.endDate.isAfter(endDate)));
 			}
 		}));
 	}
 
 	private static UpdateOperations<JUser> ops() {
 		return MorphiaPlugin.ds().createUpdateOperations(JUser.class);
 	}
 
 	public static Boolean checkAuthentication(final String username, final String password) {
 		return queryToFindMe(username)
 			       .field("password").equal(MD5.apply(password))
 			       .countAll() > 0;
 	}
 
 	public static Subject getSubject(final String username) {
 		return queryToFindMe(username)
 			       .retrievedFields(true, "username", "role")
 			       .disableValidation()
 			       .get();
 	}
 
 	public static ObjectId id(final String username) {
 		return queryToFindMe(username)
 			       .retrievedFields(true, Mapper.ID_KEY)
 			       .disableValidation()
 			       .get().id;
 	}
 
 	public static JUser account(final ObjectId id) {
 		return queryToFindMe(id)
 			       .retrievedFields(Boolean.FALSE, "role", "username", "password", "isManager")
 			       .disableValidation()
 			       .get();
 	}
 
 	public static JUser account(final String id) {
 		return account(ObjectId.massageToObjectId(id));
 	}
 
 	public static JUser identity(final ObjectId id) {
 		return queryToFindMe(id)
 			       .retrievedFields(true, "firstName", "lastName", "email")
 			       .disableValidation()
 			       .get();
 	}
 
 	public static List<JUser> byRole(final String role) {
 		return q()
 			       .field("role").equal(role)
 			       .retrievedFields(true, "username", "firstName", "lastName", "role")
 			       .disableValidation()
 			       .order("lastName, firstName")
 			       .asList();
 	}
 
 	public static List<ObjectId> affectedMissions(final String username) {
 		return affectedMissions(username, null, null);
 	}
 
 	public static List<ObjectId> affectedMissions(final String username, final DateTime startDate, final DateTime endDate) {
 		final List<JAffectedMission> affectedMissions = queryToFindMe(username)
 			                                                .retrievedFields(true, "affectedMissions")
 			                                                .disableValidation()
 			                                                .get().affectedMissions;
 
 		final Function<JAffectedMission, ObjectId> getId = new Function<JAffectedMission, ObjectId>() {
 			@Nullable
 			@Override
 			public ObjectId apply(@Nullable final JAffectedMission am) {
 				return am.missionId;
 			}
 		};
 
 		return Lists.newArrayList(Collections2.transform(filterByDates(affectedMissions, startDate, endDate), getId));
 	}
 
 	public static List<JAffectedMission> affectedMissions(final ObjectId userId, final DateTime startDate, final DateTime endDate) {
 		final Query<JUser> q = queryToFindMe(userId)
 			                       .retrievedFields(true, "affectedMissions")
 			                       .disableValidation();
 		return filterByDates(q.get().affectedMissions,
 			                    startDate,
 			                    endDate);
 	}
 
 	public static List<JUser> all() {
 		return q()
 			       .retrievedFields(Boolean.FALSE, "affectedMissions")
 			       .disableValidation()
 			       .asList();
 	}
 
 	public static List<JUser> managers() {
 		return q()
 			       .field("isManager").equal(Boolean.TRUE)
 			       .retrievedFields(true, Mapper.ID_KEY, "lastName", "firstName")
 			       .disableValidation()
 			       .asList();
 	}
 
 	public static JUser update(final JUser user) {
 		boolean removeManager = false;
 		if(user.managerId == null) {
 			removeManager = true;
 		}
 		MorphiaPlugin.ds().merge(user, WriteConcern.ACKNOWLEDGED);
 		if(removeManager) {
 			final UpdateOperations<JUser> ops = ops().unset("managerId");
 			MorphiaPlugin.ds().update(queryToFindMe(user.id), ops, false, WriteConcern.ACKNOWLEDGED);
 		}
 		return user;
 	}
 
 	public static void password(final String username, final String newPassword) {
 		final UpdateOperations<JUser> ops = ops().set("password", MD5.apply(newPassword));
 		MorphiaPlugin.ds().update(queryToFindMe(username), ops, false, WriteConcern.ACKNOWLEDGED);
 	}
 
 	public static JUser add(final JUser user) {
 		MorphiaPlugin.ds().save(user, WriteConcern.ACKNOWLEDGED);
 		return user;
 	}
 
 	public static Boolean exist(final String username) {
 		return MorphiaPlugin.ds().getCount(queryToFindMe(username)) > 0;
 	}
 
 	public static JUser byUsername(final String username) {
 		return queryToFindMe(username).get();
 	}
 
 	public static JUser create(final JUser user) {
 		user.affectedMissions.addAll(JAffectedMission.genesis());
 		user.insert();
 		JUser.password(user.username, AppConfig.defaultPassword());
 		return user;
 	}
 
 	public static JUser fetch(final ObjectId id) {
 		return queryToFindMe(id).get();
 	}
 
 	public String fullName() {
 		return String.format("%s %s", StringUtils.capitalize(lastName.toLowerCase()), StringUtils.capitalize(firstName.toLowerCase()));
 	}
 
 	@Override
 	@JsonIgnore
 	public List<? extends Role> getRoles() {
 		return Lists.newArrayList(new JSecurityRole(role));
 	}
 
 	@Override
 	@JsonIgnore
 	public List<? extends Permission> getPermissions() {
 		return null;
 	}
 
 	@Override
 	@JsonIgnore
 	public String getIdentifier() {
 		return username;
 	}
 }
