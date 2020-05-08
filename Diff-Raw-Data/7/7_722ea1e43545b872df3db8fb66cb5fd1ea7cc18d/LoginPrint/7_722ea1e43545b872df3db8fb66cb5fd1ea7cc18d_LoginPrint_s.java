 package bitter.action;
 
 import java.util.*;
 import bitter.util.*;
 import bitter.*;
 
 public class LoginPrint implements Action {
 
 	private MessageList mList;
 	private User user;
 
 	public LoginPrint(MessageHandler mHandler, User user) {
 		this.user = user;
 		mList = new MessageList();
 		List<User> subs = user.getSubscriptions();
 		for (User sub : subs) {
 			String subName = sub.getName();
			for (Message message : mHandler.getMessagesByUser(subName)) {
				mList.add(message);
 			}
 		}
 	}
 
 	@Override
 	public String doAction() {
 		if (mList.length() < 0) {
 			return "Subscribe to someone, already!";
 		}
 		String messages = "";
 		int sloppyCounter = 0;
         for (Message message : mList) {
 			if (sloppyCounter > 20)
 				break;
 			User poster = message.getPoster();
 			if ((user == null || !user.subscribesTo(poster)) && 
 					message.isPrivate()) {
 				continue;
 			}
             messages += MessageFormatter.toPrintableString(message);
             messages += "\n\n";
 			sloppyCounter++;
         }
 
         return "\n" + messages;
 	}
 } // End class LoginPrint
