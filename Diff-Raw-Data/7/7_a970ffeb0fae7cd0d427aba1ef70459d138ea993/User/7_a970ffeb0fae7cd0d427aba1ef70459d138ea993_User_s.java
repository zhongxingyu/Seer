 package models;
 
 import java.util.*;
 
 import play.Play;
 import play.db.ebean.*;
 import providers.MyUsernamePasswordAuthUser;
 import utils.PlayDozerMapper;
 
 import javax.persistence.*;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 //import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 import models.TokenAction.Type;
 
 import be.objectify.deadbolt.core.models.Permission;
 import be.objectify.deadbolt.core.models.Role;
 import be.objectify.deadbolt.core.models.Subject;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.ExpressionList;
 import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
 import com.feth.play.module.pa.user.AuthUser;
 import com.feth.play.module.pa.user.AuthUserIdentity;
 import com.feth.play.module.pa.user.EmailIdentity;
 import com.feth.play.module.pa.user.NameIdentity;
 import com.feth.play.module.pa.user.PicturedIdentity;
 
 import enums.MyRoles;
 
 @Entity
 @Table(name = "User")
 public class User extends Model implements Subject {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 822847283908287240L;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "user_id")
 	public Long userId;
 
 	@OneToOne(cascade = CascadeType.ALL)
 	@MapsId
 	@JoinColumn(name = "person_id", updatable=true, insertable=true )
 	private Person person;
 
 	@Column(length = 60, name = "nickname")
 	private String username;
 
 	@Column(length = 50)
 	private String email;
 
 	@Column
 	private String locale;
 
 	@Column(name = "email_verified")
 	private Boolean emailVerified;
 
 	@Column(name = "nickname_verified")
 	private Boolean usernameVerified;
 
 	@Column(name = "profile_pic")
 	private String profilePic;
 	//
 	// @Column
 	// private String cryptpass;
 
 	@Column(name = "conf_type")
 	private String confType;
 
 	@Temporal(TemporalType.DATE)
 	@Column
 	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	private DateTime creationDate;
 
 	@Column
 	private boolean active;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
 	private List<LinkedAccount> linkedAccounts;
 
 	@ManyToMany(cascade = CascadeType.ALL)
 	@JoinTable(name = "User_Security_Roles", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = true, insertable = true) }, inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "role_id", updatable = true, insertable = true) })
 	private List<SecurityRole> roles;
 
 	@ManyToMany(cascade = CascadeType.ALL)
 	@JoinTable(name = "User_User_Permission", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = true, insertable = true) }, inverseJoinColumns = { @JoinColumn(name = "permission_id", referencedColumnName = "permission_id", updatable = true, insertable = true) })
 	private List<UserPermission> permissions;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "targetUser", cascade = CascadeType.ALL)
 	private List<TokenAction> tokenActions;
 
 	public static Model.Finder<Long, User> find = new Model.Finder<Long, User>(
 			Long.class, User.class);
 
 	public static List<User> all() {
 		return find.all();
 	}
 
 	public static void create(User user) {
 		user.save();
 	}
 
 	public static User createObject(User user) {
 		user.save();
 		return user;
 	}
 
 	/**
 	 * A lazy delete method, it updates the user to become inactive
 	 * @param id of the user
 	 */
 	public static void delete(Long id) {
 		User u = find.ref(id);
 		u.setActive(false);
 		u.update();
 	}
 	
 
 	/**
 	 * The real delete. Deletes all the content that the user created also
 	 * @param id of the user
 	 */
 	public static void deleteForce(Long uid) {
 		find.ref(uid).delete();
 	}
 	
 	public static User read(Long id) {
 		return find.byId(id);
 	}
 
 	public static String readLocaleByPersonId(Long personId) {
		return find.where().eq("person.personId", personId).findUnique().getLocale();
 	}
 	
 	public static User getByEmail(String email) {
 		return find.where().eq("email", email).findUnique();
 	}
 
 	/* AUTHENTICATION AND AUTHORIZATION */
 	public static boolean existsByAuthUserIdentity(
 			final AuthUserIdentity identity) {
 		final ExpressionList<User> exp = getAuthUserFind(identity);
 		return exp.findRowCount() > 0;
 	}
 
 	private static ExpressionList<User> getAuthUserFind(
 			final AuthUserIdentity identity) {
 		return find.where().eq("active", true)
 				.eq("linkedAccounts.providerUserId", identity.getId())
 				.eq("linkedAccounts.providerKey", identity.getProvider());
 	}
 
 	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
 		if (identity == null) {
 			return null;
 		}
 		if (identity instanceof UsernamePasswordAuthUser) {
 			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
 		} else {
 			return getAuthUserFind(identity).findUnique();
 		}
 	}
 
 	public void merge(final User otherUser) {
 		for (final LinkedAccount acc : otherUser.linkedAccounts) {
 			this.linkedAccounts.add(LinkedAccount.create(acc));
 		}
 		// do all other merging stuff here - like resources, etc.
 
 		// deactivate the merged user that got added to this one
 		otherUser.active = false;
 		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
 	}
 
 	public static User create(final AuthUser authUser) {
 		/*
 		 * 0. Zero step, create a new User instance
 		 */
 		User user = new User();
 
 		/*
 		 * 1. We start by already adding the role MEMBER and a LINKEDACCOUNT to
 		 * the user instance to be created
 		 */
 		user.roles = Collections.singletonList(SecurityRole
 				.findByRoleName(MyRoles.MEMBER.toString()));
 		user.linkedAccounts = Collections.singletonList(LinkedAccount
 				.create(authUser));
 		user.active = true;
 		Long userId = null;
 
 		/*
 		 * 2. Second, we will try to see if the email is sent and find if the
 		 * user is already part of the system
 		 */
 		if (authUser instanceof EmailIdentity) {
 			final EmailIdentity identity = (EmailIdentity) authUser;
 			/*
 			 * Remember, even when getting them from FB & Co., emails should be
 			 * verified within the application as a security breach there might
 			 * break your security as well!
 			 */
 			user.email = identity.getEmail();
 			user.emailVerified = false;
 			userId = User.getByEmail(identity.getEmail()) != null ? User
 					.getByEmail(identity.getEmail()).getUserId() : null;
 
 		}
 
 		/*
 		 * 3. Third, we create a new Person instance to associate to this user
 		 */
 		if (authUser instanceof MyUsernamePasswordAuthUser) {
 			final MyUsernamePasswordAuthUser identity = (MyUsernamePasswordAuthUser) authUser;
 
 			if (identity.getPerson() != null) {
 				models.Person p = PlayDozerMapper.getInstance().map(
 						identity.getPerson(), Person.class);
 				user.setPerson(p);
 
 				City c = p.getBirthplace();
 
 				if (c != null) {
 					if (c.getCityId() == null) {
 						c = models.City.getCityByName(c.getName());
 					} else {
 						c = models.City.read(c.getCityId());
 					}
 				}
 
 				p.setBirthplace(c);
 			} else {
 				user.setPerson(null);
 			}
 		}
 
 		/*
 		 * 4. If part of the signup form there is also a name, and the person
 		 * bean does not have the name on it add this name as Firstname
 		 */
 		if (authUser instanceof NameIdentity) {
 			final NameIdentity identity = (NameIdentity) authUser;
 			final String name = identity.getName();
 			if (user.getPerson() != null
 					&& user.getPerson().getFirstname() == null) {
 				user.getPerson().setFirstname(name);
 			}
 		}
 
 		/*
 		 * 5. If the picture URL is also set on the User form, add the picture
 		 * to the user
 		 */
 		if (authUser instanceof PicturedIdentity) {
 			final PicturedIdentity identity = (PicturedIdentity) authUser;
 			final String picture = identity.getPicture();
 			if (picture != null) {
 				user.setProfilePic(picture);
 			} else {
 				user.setProfilePic(Play.application().configuration().getString("default.profilepic"));
 			}
 		} else {
 			user.setProfilePic(Play.application().configuration().getString("default.profilepic"));
 		}
 
 		/*
 		 * 6. always the email is going to be validated by google
 		 */
 		if (authUser instanceof GoogleAuthUser) {
 			user.setEmailValidated(true);
 		}
 
 		/*
 		 * 7. Generate the username
 		 */
 
 		user.setUsername(models.User.generateUsername(user.getEmail()));
 		user.setUsernameVerified(false);
 		
 		/*
 		 * 8. Set language of user
 		 */
 		// TODO get the default language from request		
 		String userLocale = user.getLocale() == null ? Play.application().configuration().getString("default.language") : user.getLocale();		
 		user.setLocale(userLocale);
 		
 		/*
 		 * 9. Create the new user
 		 */
 		if (userId != null) {
 			user.update(userId);
 		} else {
 			user.setCreationDate(DateTime.now());
 			user.save();
 			user.refresh();
 			
 			// create Birth life story
 			models.LifeStory.createBirthStory(user);
 		}
 		
 		return user;
 	}
 
 	private static String generateUsername(String email) {
 		String newUsername = email.split("@")[0];
 		int count = models.User.usernameExists(newUsername);
 		if (count > 0) {
 			newUsername += (count++);
 		}
 		return newUsername;
 	}
 
 	private static int usernameExists(String newUsername) {
 		return find.where().eq("active", true)
 				.like("username", "%" + newUsername + "%").findList().size();
 	}
 	
 
 	// TODO check play authenticate inherited code
 	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
 		User.findByAuthUserIdentity(oldUser).merge(
 				User.findByAuthUserIdentity(newUser));
 	}
 	
 
 	public Set<String> getProviders() {
 		final Set<String> providerKeys = new HashSet<String>(
 				linkedAccounts.size());
 		for (final LinkedAccount acc : linkedAccounts) {
 			providerKeys.add(acc.getProviderKey());
 		}
 		return providerKeys;
 	}
 	
 
 	public static void addLinkedAccount(final AuthUser oldUser,
 			final AuthUser newUser) {
 		final User u = User.findByAuthUserIdentity(oldUser);
 		u.linkedAccounts.add(LinkedAccount.create(newUser));
 		u.save();
 	}
 
 	
 	/**
 	 * used by authentication, it will return only active users
 	 * 
 	 * @param email
 	 * @return the active user
 	 */
 	public static User findByEmail(final String email) {
 		return getEmailUserFind(email).findUnique();
 	}
 	
 
 	private static ExpressionList<User> getEmailUserFind(final String email) {
 		return find.where().eq("active", true).eq("email", email);
 	}
 	
 
 	public LinkedAccount getAccountByProvider(final String providerKey) {
 		return LinkedAccount.findByProviderKey(this, providerKey);
 	}
 	
 
 	public static User findByUsernamePasswordIdentity(
 			final UsernamePasswordAuthUser identity) {
 		return getUsernamePasswordAuthUserFind(identity).findUnique();
 	}
 	
 
 	private static ExpressionList<User> getUsernamePasswordAuthUserFind(
 			final UsernamePasswordAuthUser identity) {
 		return getEmailUserFind(identity.getEmail()).eq(
 				"linkedAccounts.providerKey", identity.getProvider());
 	}
 	
 
 	public void resetPassword(final UsernamePasswordAuthUser authUser,
 			final boolean create) {
 		// You might want to wrap this into a transaction
 		this.changePassword(authUser, create);
 		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
 	}
 	
 
 	public void changePassword(final UsernamePasswordAuthUser authUser,
 			final boolean create) {
 		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
 		if (a == null) {
 			if (create) {
 				a = LinkedAccount.create(authUser);
 				a.setUser(this);
 			} else {
 				throw new RuntimeException(
 						"Account not enabled for password usage");
 			}
 		}
 		a.setProviderUserId(authUser.getHashedPassword());
 		a.save();
 	}
 	
 
 	@Transactional
 	public static void verify(final User unverified) {
 		// You might want to wrap this into a transaction
 		// Model..em().getTransaction()
 		User user = User.read(unverified.getUserId());
 		user.setEmailValidated(true);
 		user.save();
 		// user.update(unverified.getId());
 		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
 	}
 
 	
 	/*
 	 * Simple Getters and Setters
 	 */
 	
 	@Override
 	public String getIdentifier() {
 		return Long.toString(userId);
 	}
 	
 
 	/* GETTERS AND SETTERS */
 
 	public Long getUserId() {
 		return userId;
 	}
 	
 
 	public void setUserId(Long userId) {
 		this.userId = userId;
 	}
 	
 
 	public Person getPerson() {
 		return person;
 	}
 	
 
 	public void setPerson(Person person) {
 		this.person = person;
 	}
 	
 
 	public String getUsername() {
 		return username;
 	}
 	
 
 	public void setUsername(String nickname) {
 		this.username = nickname;
 	}
 	
 
 	public String getEmail() {
 		return email;
 	}
 	
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 
 	public String getLocale() {
 		return locale;
 	}
 	
 
 	public void setLocale(String lang) {
 		this.locale = lang;
 	}
 	
 
 	public Boolean getEmailVerified() {
 		return emailVerified;
 	}
 	
 
 	public void setEmailVerified(Boolean email_verified) {
 		this.emailVerified = email_verified;
 	}
 	
 
 	public Boolean getUsernameVerified() {
 		return usernameVerified;
 	}
 	
 
 	public void setUsernameVerified(Boolean nickname_verified) {
 		this.usernameVerified = nickname_verified;
 	}
 
 	public String getProfilePic() {
 		return profilePic;
 	}
 
 	public void setProfilePic(String profile_pic) {
 		this.profilePic = profile_pic;
 	}
 	
 	public String getConfType() {
 		return confType;
 	}
 
 	public void setConfType(String conf_type) {
 		this.confType = conf_type;
 	}
 
 	public DateTime getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(DateTime creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public boolean isEmailValidated() {
 		return this.emailVerified;
 	}
 
 	public void setEmailValidated(boolean emailValidated) {
 		this.emailVerified = emailValidated;
 	}
 
 	public List<LinkedAccount> getLinkedAccounts() {
 		return linkedAccounts;
 	}
 
 	public void setLinkedAccounts(List<LinkedAccount> linkedAccounts) {
 		this.linkedAccounts = linkedAccounts;
 	}
 
 	public Long getPersonId() {
 		if (this.person != null) {
 			return this.person.getPersonId();
 		} else {
 			return null;
 		}
 
 	}
 
 	public void setPersonId(Long personId) {
 		if (this.person == null) {
 			this.person = new Person();
 		}
 		this.person.setPersonId(personId);
 	}
 
 	@Override
 	public List<? extends Role> getRoles() {
 		return roles;
 	}
 	
 	
 	public void addSecurityRole(SecurityRole role) {
 		roles.add(role);
 	}
 	
 
 	public void resetSecurityRole(SecurityRole role) {
 		roles = Collections.singletonList(role);
 	}
 	
 
 	@Override
 	public List<? extends Permission> getPermissions() {
 		return this.permissions;
 	}
 	
 
 	public void setRoles(List<SecurityRole> singletonList) {
 		this.roles = singletonList;
 	}
 	
 }
