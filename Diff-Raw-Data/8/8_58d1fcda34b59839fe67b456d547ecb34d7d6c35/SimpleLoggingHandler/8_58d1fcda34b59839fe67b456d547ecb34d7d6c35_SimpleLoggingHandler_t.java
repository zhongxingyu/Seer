 package edu.nyu.grouper.xmpp;
 
 import java.util.Iterator;
 import java.util.List;
 
 
 import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
 import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
 import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler;
 import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob;
 import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;
 
 public class SimpleLoggingHandler implements GrouperClientXmppHandler {
 	
 	private static final Log log = GrouperClientUtils.retrieveLog(SimpleLoggingHandler.class); 
 
 	public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
 			String groupName, String groupExtension,
 			List<GrouperClientXmppSubject> newSubjectList,
 			List<GrouperClientXmppSubject> previousSubjectList,
 			GrouperClientXmppSubject changeSubject, String action) {
 		
 		if (log.isDebugEnabled()){
 			log.debug("jobName = " + grouperClientXmppJob.getJobName() +
 						"\n groupName = " + groupName +
 						"\n groupExtension = " + groupExtension +
 						"\n previousSubjectList = " + SimpleLoggingHandler.joinSubjectIds(previousSubjectList) +
						"\n, newSubjectList = " + SimpleLoggingHandler.joinSubjectIds(newSubjectList) +
 						"\n changeSubject = " + changeSubject.getSubjectId() +
						"\n action = " + action);
 		}
 	}
 
 	public void handleAll(GrouperClientXmppJob grouperClientXmppJob,
 			String groupName, String groupExtension,
 			List<GrouperClientXmppSubject> newSubjectList) {
 		
 		if (log.isDebugEnabled()){
 			log.debug("jobName = " + grouperClientXmppJob.getJobName() +
 						"\n groupName = " + groupName +
 						"\n groupExtension = " + groupExtension +
						"\n newSubjectList = " + SimpleLoggingHandler.joinSubjectIds(newSubjectList));
 		}
 	}
 
 	/**
 	 * Flatten a List of {@link GrouperClientXmppSubject}s into a String
 	 * @param s the Subjects 
 	 * @return the list joined by commas
 	 */
 	public static String joinSubjectIds(List<GrouperClientXmppSubject> s) {
 	    if (s.isEmpty()) return "";
 	    Iterator<GrouperClientXmppSubject> iter = s.iterator();
 	    StringBuffer buffer = new StringBuffer(iter.next().getSubjectId());
 	    while (iter.hasNext()) buffer.append(", ").append(iter.next().getSubjectId());
 	    return buffer.toString();
 	}
 }
