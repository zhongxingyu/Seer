 package icm.server.intenthandler;
 
 import icm.dao.ICMPersistentManager;
 import icm.dao.User;
 import icm.dao.UserDAO;
 import icm.intent.LoginIntent;
 
 import org.orm.PersistentException;
 import org.orm.PersistentSession;
 
 
 /**
  * command handler for login request (server side)
  * @author Ni
  *
  */
 public class LoginIntentHandler implements IntentHandler<LoginIntent>
 {
 
 	@Override
 	public Object execute(LoginIntent command) 
 	{
 		try 
 		{
 			PersistentSession session = ICMPersistentManager.instance().getSession();
 			User user = UserDAO.getUserByORMID(session, Integer.parseInt(command.getUser()));
 			session.close();
 			
 			if( user==null )
 			{
 				//user doesn't exist
 				System.out.println("I GOT NOTHIN!!");
 			}
 			else if( user.getPassword().equals(command.getPassword()) )
 			{
 				//password correct
 				System.out.println("FUCK OFF GOOD PASS!");
 			}
 			else
 			{
 				//password incorrect
 				System.out.println("FUCK OFF WRONG PASS!");
 			}
 		}
 		catch (PersistentException e) {e.printStackTrace();}
 		return null;
 	}
 }
