 package com.web.messaging;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class RoundRobinQueueMessagePicker extends Thread implements Runnable{
 	public static final BlockingQueue<MessagingParams> queue=new LinkedBlockingQueue();
 	private HashMap messagingClassMap;
 	public RoundRobinQueueMessagePicker(HashMap messagingClassMap){
 		this.messagingClassMap=messagingClassMap;
 	}
 	public void run(){
 		while(true){
 			
				int count=0;
 				MessagingParams messageParams = null;
 				HashMap queueMap=null;
 				synchronized (messagingClassMap) {
 					queueMap=(HashMap) messagingClassMap.get("RoundRobinQueue");
 				}
 				if(queueMap==null){
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					continue;
 				}
 				//ArrayList list=(ArrayList) queueMap.get(messageParams.getName());
 				if((messageParams=queue.peek())!=null){
 					ArrayList list=(ArrayList) queueMap.get(messageParams.getName());
 					
 					try {
 						if(list!=null&&list.size()>0)messageParams = queue.take();
 						else {
 							//Thread.sleep(1000);
 							//continue;
 						}
 					} catch (InterruptedException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					
 					Random rand=new Random(System.currentTimeMillis());
 					MessagingClass messagingClass= (MessagingClass) list.get(count%list.size());
					count=count%list.size();
 					Messager messager = null;
 					try {
 						messager = (Messager)messagingClass.getMessageClass().newInstance();
 					} catch (InstantiationException | IllegalAccessException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					if(messager!=null){
 						messager.setMessage(messageParams.getMessage());
 						messager.start();
 					}
 				}
 				
 				try {
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 			
 	}
 }
