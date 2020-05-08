 package newsrack;
 
 import newsrack.user.User;
 import newsrack.filter.Issue;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class UserMigration
 {
    private static Log _log = LogFactory.getLog((new UserMigration()).getClass());
 
 	public static void updateArtCounts()
 	{
       List<Issue> issues = User.getAllValidatedIssues();
 		for (Issue i: issues) {
 			i.storeNewsToArchive();
 		}
 	}
 
	public static void migrateUser(String uid)
	{
		System.out.println("--- Migrating user .. " + uid);
		try {
			User.getUser(uid).validateIssues(true);
		}
		catch (Exception e) {
			_log.error("ERROR VALIDATING user: " + uid, e);
		}
	}

 	public static void migrateAllV1UsersToV2()
 	{
 		List<User> allUsers = User.getAllUsers();
 		List<User> validatedUsers = new ArrayList<User>();
 		for (User u: allUsers) {
 			if (u.isValidated())
 				validatedUsers.add(u);
 		}
 
 		System.out.println("--- Invalidating first ---");
 		for (User u: validatedUsers) {
 			System.out.println("UID: " + u.getUid());
 			try { u.invalidateAllIssues(); } catch (Exception e) { _log.error("ERROR INVALIDATING:", e); }
 		}
 
 			// Validating these 3 first in this order ensures that all other users migrate successfully!
		migrateUser("subbu");
		migrateUser("demo");
		migrateUser("quesoboy");
 
 		for (User u: validatedUsers) {
 			String uid = u.getUid();
 			if (!uid.equals("subbu") && !uid.equals("demo") && !uid.equals("quesoboy")) {
 				System.out.println("UID: " + u.getUid());
 				try { u.validateIssues(true); } catch (Exception e) { _log.error("ERROR VALIDATING:", e); }
 			}
 		}
 
 		System.out.println("Done!");
 	}
 
 	public static void main(String[] args)
 	{
 		if (args.length < 2) {
			System.out.println("Usage: java newsrack.UserMigration <properties-file> <action> [<other-optional-args>]");
 			System.exit(0);
 		}
 
    	String appPropertiesFile = args[0];
 		String action = args[1];
 
 		System.out.println("Properties file: " + appPropertiesFile);
 		GlobalConstants.startup(null, appPropertiesFile);
 
 		if (action.equals("migrate")) {
 			migrateAllV1UsersToV2();
 		}
 		else if (action.equals("update")) {
 			updateArtCounts();
 		}
		else if (action.equals("migrate-user")) {
			migrateUser(args[2]);
		}
 		else {
 			System.out.println("Unknown action:" + action);
 		}
 		System.out.println("All Done!");
 		System.exit(0);
 	}
 }
