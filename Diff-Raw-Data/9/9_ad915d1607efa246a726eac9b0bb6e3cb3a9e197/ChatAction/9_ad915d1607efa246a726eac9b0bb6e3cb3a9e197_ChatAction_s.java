 package demo.controller;
 
 import demo.model.MyDAOException;
 import demo.model.Preferable;
 import edu.smu.tspell.wordnet.Synset;
 import edu.smu.tspell.wordnet.SynsetType;
 import edu.smu.tspell.wordnet.WordNetDatabase;
 
 /**
  * Main chat action when a member is chatting with a partner.
  * 
  * Observes the chat and identifies strength of related interests between
  * the partners, while forwarding the chat messages back and forth between
  * chat members.
  */
 public class ChatAction implements Action {
 	public void perform(ChatMember user, Message message) {		
 		Message echo = new Message();
 		Message partnerMessage = new Message();
 		
 		System.out.println("chat action");
 		
 		// echo message back to user
 		echo.setBody(message.getBody());
 		echo.setSender(user.getUsername());
 		echo.setHeader("echo");
 		user.sendMessage(echo);
 		
 		// identify preferable terms
		String newBody = identifyPreferables(message.getBody(), user);
 		
 		// forward message to partner
 		partnerMessage.setBody(newBody);
 		partnerMessage.setSender(user.getUsername());
 		partnerMessage.setHeader("chat");
 		user.getPartner().sendMessage(partnerMessage);
 	}
 	
 	private String identifyPreferables(String msg, ChatMember user) {
 		if (msg == null) return msg;
 		
 		// get instance of wordnet
 		WordNetDatabase database = WordNetDatabase.getFileInstance();
 	
 		String newBody = msg;
 		String stripped = msg.replaceAll("[^a-zA-Z ]", "");
 		String[] words = stripped.split(" ");
 		for(String word : words) {
 			word = word.trim();
 			// minimum word length to consider (for performance)
 			if (word.length() > 3) {
 				// lookup if any form of word is noun or exact form is verb
 	    	    Synset[] nounSynsets = database.getSynsets(word, SynsetType.NOUN, true);
 	    	    Synset[] verbSynsets = database.getSynsets(word, SynsetType.VERB, false);
 	    	 // check if word or derivative can be noun, but exact form not a verb	
 	    	    if (nounSynsets.length > 0 && verbSynsets.length == 0) {
 		    	    Preferable preferable = new Preferable();
 	    	    	preferable.setTerm(word);
 	    	    	try {
 	    	    		Preferable lookahead = ChatMember.getPreferenceDAO().lookupPreferable(preferable);
 	    	    		if(lookahead != null) preferable = lookahead;
 	    	    		else ChatMember.getPreferenceDAO().create(preferable);
 					} catch (MyDAOException e) {
 						System.out.println(e);
 					}
 		    	    int pid = preferable.getPid();
 		    	    
 		    	    if (user.getPreferables().contains(pid)) {
 		    	    	// do not allow users to have duplicate preferables
 		    	    	continue;
 		    	    }
 		    	    else {
 		    	    	newBody = newBody.replaceFirst(word, "<span class=\"likeable\" id='" + pid + "' onmouseover='makeSlider(\"#" + pid + "\");' onmouseout='removeSlider(\"#" + pid + "\");'>" + word + "</span>");
 		    	    	user.getPreferables().add(pid);
 		    	    }
 	    	    }
 			}
 		}
 		
 		return newBody;
 	}
 }
