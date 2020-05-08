 package edu.uiuc.boltdb.groupmembership;
 
 import java.net.InetAddress;
 import java.util.Collection;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 
 public class SendGossipThread implements Runnable
 {
 	//private static org.apache.log4j.Logger log = Logger.getLogger(SendGossipThread.class);
 	@Override
 	public void run()
 	{
 		try
 		{
 			int listSize = GroupMembership.membershipList.size();
 			int activeMembersListSize = getActiveMembersCount();
 			int gossipGroupSize = (int) Math.sqrt(activeMembersListSize);
 			
 			Random generator = new Random();
 			Object[] keys = GroupMembership.membershipList.keySet().toArray(); 
 			while(gossipGroupSize > 0)
 			{
 				MembershipBean mBean = GroupMembership.membershipList.get(keys[generator.nextInt(listSize)]);
 				if(mBean.toBeDeleted)
 					continue;
				if((mBean.ipaddress).equals(InetAddress.getLocalHost().getHostAddress()))
 					continue;
 				sendMembershipList(mBean.ipaddress);
 				gossipGroupSize--;
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println("EXCEPTION:In HeartbeatIncrementerThread");
 		}
 	}
 	
 	public int getActiveMembersCount()
 	{
 		int activeMembersCount = 0;
 		try
 		{
 			Collection<MembershipBean> mbeans = GroupMembership.membershipList.values();
 			for(MembershipBean mBean : mbeans)
 			{
 				if(mBean.toBeDeleted)
 					continue;
 				if((mBean.ipaddress).equals(InetAddress.getLocalHost().getHostAddress()))
 					continue;
 				activeMembersCount++;
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.getMessage());
 			System.out.println("EXCEPTION:In method getActiveMembersCount in SendGossipThread");
 			e.printStackTrace();
 		}
 		return activeMembersCount;
 	}
 	
 	public void sendMembershipList(String ipaddress)
 	{
 		try
 		{
 			Thread newThread = new SendMembershipListThread(InetAddress.getByName(ipaddress), 8764);
 			newThread.start();
 		}
 		catch(Exception e)
 		{
 			System.out.println("EXCEPTION:In method SendMembershipList in SendGossipThread");
 		}
 	}
 }
