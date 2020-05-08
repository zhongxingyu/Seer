 package controllers;
 
 import java.io.File;
 import java.text.ParseException;
 
 import models.Answer;
 import models.Notification;
 import models.Question;
 import models.User;
 import models.Vote;
 import models.database.Database;
 import models.database.importers.Importer;
 import play.data.validation.Required;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.With;
 
 /**
  * The controller for all routes that concern the {@link User}
  * 
  * @author Group3
  * 
  */
 @With(Secure.class)
 public class CUser extends Controller {
 
 	/**
 	 * Deletes the {@link User} and all it's {@link Question}' {@link Answer}'s
 	 * {@link Vote}'s.
 	 * 
 	 * @param name
 	 *            the name of the {@link User} to be deleted.
 	 * @throws Throwable
 	 */
 	public static void deleteUser(String name) throws Throwable {
 		User user = Database.get().users().get(name);
 		if (hasPermissionToDelete(Session.get().currentUser(), user)) {
 			user.delete();
 			flash.success("secure.userdeletedflash");
 			Secure.logout();
 			Application.index(0);
 		}
 		flash.error("secure.userdeleteerror");
 		if (!CUser.redirectToCallingPage()) {
 			Application.index(0);
 		}
 	}
 
 	/**
 	 * Instead of deleting all {@link Entry}'s of a {@link User}. This method
 	 * anonymizes all of them replacing the username with 'anonymous'.
 	 * 
 	 * @param name
 	 *            the name of the {@link User} to be anonymized.
 	 * @throws Throwable
 	 */
 	public static void anonymizeUser(String name) throws Throwable {
 		User user = Database.get().users().get(name);
 		if (hasPermissionToDelete(Session.get().currentUser(), user)) {
 			user.anonymize(true, false);
 		}
 		deleteUser(name);
 	}
 
 	/**
 	 * Checks for permission to delete.
 	 * 
 	 * @param currentUser
 	 *            the currently logged in {@link User}.
 	 * @param user
 	 *            the owner of the profile.
 	 * @return true, if successful
 	 */
 	private static boolean hasPermissionToDelete(User currentUser, User user) {
 		return currentUser == user;
 	}
 
 	/**
 	 * Save profile the profile a {@link User}.
 	 * 
 	 * @param name
 	 *            the name of the {@link User}.
 	 * @param email
 	 *            the email of the {@link User}.
 	 * @param fullname
 	 *            the fullname of the {@link User}.
 	 * @param birthday
 	 *            the birthday of the {@link User}.
 	 * @param website
 	 *            the website of the {@link User}.
 	 * @param profession
 	 *            the profession of the{@link User}.
 	 * @param employer
 	 *            the employer of the{@link User}.
 	 * @param biography
 	 *            the biography of the {@link User}.
 	 * @param oldPassword
 	 *            the old password of the {@link User}.
 	 * @param newPassword
 	 *            the new password of the {@link User}.
 	 * @throws ParseException
 	 * 
 	 */
 	public static void saveProfile(String name, String email, String fullname,
 			String birthday, String website, String profession,
 			String employer, String biography, String oldPassword,
 			String newPassword) throws ParseException {
 		User user = Database.get().users().get(name);
 		if (!Application.userCanEditProfile(user)) {
 			flash.error("secure.editprofileerror");
 			Application.showprofile(user.getName());
 		}
 		if (email != null) {
 			user.setEmail(email);
 		}
 		if (fullname != null) {
 			user.setFullname(fullname);
 		}
 		if (birthday != null) {
 			user.setDateOfBirth(birthday);
 		}
 		if (website != null) {
 			user.setWebsite(website);
 		}
 		if (profession != null) {
 			user.setProfession(profession);
 		}
 		if (employer != null) {
 			user.setEmployer(employer);
 		}
 		if (biography != null) {
 			user.setBiography(biography);
 		}
 		if (!newPassword.isEmpty()) {
 			if (user.checkPW(newPassword)) {
 				user.setSHA1Password(newPassword);
 			} else if (!user.checkPW(oldPassword)) {
 				flash.error("secure.passwordChangeError");
 			}
 		}
 		if (!oldPassword.isEmpty() && newPassword.isEmpty()) {
 			flash.error("secure.passwordNewFieldEmptyError");
 		}
 
 		flash.success("secure.editprofileflash");
 		Application.showprofile(user.getName());
 	}
 
 	/**
 	 * Follow notification.
 	 * 
 	 * @param id
 	 *            the id of the notification.
 	 */
 	public static void followNotification(int id) {
 		User user = Session.get().currentUser();
 		Notification notification = user.getNotification(id);
 		if (notification != null) {
 			notification.unsetNew();
 		}
 		if (notification != null && notification.getAbout() instanceof Answer) {
 			Application.question(((Answer) notification.getAbout())
 					.getQuestion().id());
 		} else if (!CUser.redirectToCallingPage()) {
 			Application.notifications(0);
 		}
 	}
 
 	/**
 	 * Clear new notifications. Notifications will no longer appear as new.
 	 */
 	public static void clearNewNotifications() {
 		User user = Session.get().currentUser();
 		for (Notification n : user.getNewNotifications()) {
 			n.unsetNew();
 		}
 		flash.success("secure.notificationsmarkedasreadflash");
 		Application.notifications(0);
 	}
 
 	/**
 	 * Delete a notification.
 	 * 
 	 * @param id
 	 *            the id of the notification to be deleted.
 	 */
 	public static void deleteNotification(int id) {
 		User user = Session.get().currentUser();
 		Notification n = user.getNotification(id);
 		if (n != null) {
 			n.unregister();
 			flash.success("secure.deletenotificationflash");
 		}
 		Application.notifications(0);
 	}
 
 	/**
 	 * Block a {@link User}.
 	 * 
 	 * @param username
 	 *            the username of the {@link User} to be unblocked.
 	 * @param reason
 	 *            the reason the {@link User} is being blocked.
 	 */
 	public static void blockUser(String username, String reason) {
 		User user = Database.get().users().get(username);
 		User mod = Session.get().currentUser();
 		if (mod.isModerator() && mod != user) {
 			if (reason.equals("")) {
 				reason = "secure.blockreasonerror";
 			}
 			user.block(reason);
 			flash.success("secure.blockuserflash");
 		}
 		Application.showprofile(user.getName());
 	}
 
 	/**
 	 * Unblock a {@link User}.
 	 * 
 	 * @param username
 	 *            the username of the {@link User} to be unblocked.
 	 */
 	public static void unblockUser(String username) {
 		User user = Database.get().users().get(username);
 		User mod = Session.get().currentUser();
 		if (mod.isModerator() && mod != user) {
 			user.unblock();
 			flash.success("secure.unlockuserflash");
 		}
 		Application.showprofile(user.getName());
 	}
 
 	/**
 	 * Load an XML database file
 	 * 
 	 * @param xml
 	 *            the XML database file to be loaded. This field is mandatory.
 	 */
 	public static void loadXML(@Required File xml) {
 		if (!Session.get().currentUser().isModerator()) {
 			Application.index(0);
 		}
 		if (xml == null) {
 			flash.error("secure.xmlselecterror");
 			Application.admin();
 		}
 
 		try {
 			Importer.importXML(xml);
 			flash.success("secure.xmlloadflash");
 		} catch (Throwable e) {
 			flash.error("secure.xmlloaderror", e.getMessage());
 			e.printStackTrace();
 			Application.admin();
 		}
 		if (xml != null) {
 			xml.delete();
 		}
 		Application.index(0);
 	}
 
 	/**
 	 * Clear the entire database.
 	 */
 	public static void clearDB() {
 		if (!Session.get().currentUser().isModerator()) {
 			flash.error("secure.cleardberror");
 			Application.index(0);
 		}
 		Database.clearKeepAdmins();
 		flash.success("secure.cleardbflash");
 		Application.admin();
 	}
 
 	/**
 	 * Redirect to calling page.
 	 * 
 	 * @return true, if successful
 	 */
 	static boolean redirectToCallingPage() {
 		Http.Header referer = request.headers.get("referer");
 		if (referer == null)
 			return false;
 		redirect(referer.value());
 		return true;
 	}

	/**
	 * Leads to the edit-view of the {@link User}'s profile
	 * 
	 * @param userName
	 *            the name of the {@link User} who owns the profile
	 */
	public static void editProfile(String userName) {
		User showUser = Database.get().users().get(userName);
		if (!Application.userCanEditProfile(showUser)) {
			Application.showprofile(userName);
		}
		render(showUser);
	}
 }
