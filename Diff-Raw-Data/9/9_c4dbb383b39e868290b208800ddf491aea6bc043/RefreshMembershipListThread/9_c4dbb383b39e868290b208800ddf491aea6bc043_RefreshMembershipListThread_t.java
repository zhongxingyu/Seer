 package edu.uiuc.boltdb.groupmembership;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 public class RefreshMembershipListThread implements Runnable {
 	//private static org.apache.log4j.Logger log = Logger.getLogger(RefreshMembershipListThread.class);
 	// TODO take tFail from property file
 	private int tFail = 2;
 
 	@Override
 	public void run() {
 		Iterator<Map.Entry<String, MembershipBean>> iterator = GroupMembership.membershipList
 				.entrySet().iterator();
 		while (iterator.hasNext()) {
 			Map.Entry<String, MembershipBean> entry = iterator.next();
 			MembershipBean membershipBean = entry.getValue();
 			if (membershipBean.toBeDeleted) {
 				GroupMembership.membershipList.remove(entry.getKey());
 			} else if (System.currentTimeMillis() - membershipBean.timeStamp >= tFail * 1000) {
 				membershipBean.toBeDeleted = true;
 			}
 		}
		//System.out.println("RefreshMemberShipThread : "+GroupMembership.membershipList);
 	}
 }
