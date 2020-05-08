 package edu.nyu.grouper.xmpp;
 
 import java.util.List;
 
 import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
 import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
 import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler;
 import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob;
 import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;
 import edu.nyu.grouper.xmpp.api.NakamuraGroupAdapter;
 import edu.nyu.grouper.xmpp.exceptions.GroupModificationException;
 
 /**
  * 
  * Respond to Grouper Changelog events by creating, updating, 
  * or deleting group information in nakamura.
  */
 public class NakamuraGroupXmppHandler implements GrouperClientXmppHandler {
 	
 	private Log log = GrouperClientUtils.retrieveLog(NakamuraGroupXmppHandler.class);
 	
 	private NakamuraGroupAdapter groupAdapter;
 	
 	public NakamuraGroupXmppHandler(){
 		this.groupAdapter = new HttpNakamuraGroupAdapter();
 		this.groupAdapter.configure();
 	}
 
 	/**
 	 * @param grouperClientXmppJob the job responsible for this handler.
 	 * @param groupName the name attribute from grouper.
 	 * @param groupExtension the stem:groupName from grouper.
 	 * @param newSubjectList the list of subjects in the group.
 	 * @param previousSubjectList 
 	 * @param changeSubject the subjectId that is the target of this action
 	 * @param action The type of action we're responding to
 	 */
 	public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
 			String groupName, String groupExtension,
 			List<GrouperClientXmppSubject> newSubjectList,
 			List<GrouperClientXmppSubject> previousSubjectList,
 			GrouperClientXmppSubject changeSubject, String action) {
 		
 		try {
			log.debug("action=" + action + " groupName=" + groupName + " groupExtension=" + groupExtension + " changeSubject=" + changeSubject.getName());
 			
 			if (GrouperClientUtils.equals(action, "MEMBERSHIP_ADD")) {
 			    groupAdapter.addMembership(groupName, groupExtension, changeSubject);
 			} 
 			else if (GrouperClientUtils.equals(action, "MEMBERSHIP_DELETE")) {
 				groupAdapter.deleteMembership(groupName, groupExtension, changeSubject);
 			} 
 			else if (GrouperClientUtils.equals(action, "GROUP_ADD")) {
 				groupAdapter.createGroup(groupName, groupExtension);
 			}
 			else if (GrouperClientUtils.equals(action, "GROUP_DELETE")) {
 				groupAdapter.deleteGroup(groupName, groupExtension);
 			}
 			else {
 				throw new RuntimeException("Not expecting action: '" + action + "'");
 			}
 		}
 		catch (GroupModificationException gme){
 			log.error(gme.toString());
 		}
 	}
 
 	/**
 	 * This method receives the whole group membership. Useful for full refresh jobs.
 	 * @param grouperClientXmppJob
 	 * @param groupName
 	 * @param groupExtension
 	 * @param newSubjectList
 	 */
 	public void handleAll(GrouperClientXmppJob grouperClientXmppJob,
 			String groupName, String groupExtension,
 			List<GrouperClientXmppSubject> newSubjectList) {
 		// TODO Auto-generated method stub
 	}
 
 }
