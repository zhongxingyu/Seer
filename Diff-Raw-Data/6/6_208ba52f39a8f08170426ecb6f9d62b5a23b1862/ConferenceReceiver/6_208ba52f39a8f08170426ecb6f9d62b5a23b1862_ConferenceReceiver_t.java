 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.voip.server;
 
 import com.sun.voip.Logger;
 import com.sun.voip.RtpPacket;
 
 import java.io.IOException;
 
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 
 import java.nio.ByteBuffer;
 
 import java.nio.channels.DatagramChannel;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import com.sun.stun.StunServerImpl;
 
 /**
  * Receive data from each member in a conference and dispatch it to
  * the appropriate ConferenceMember so the data can be given to the mixer.
  */
 public class ConferenceReceiver extends Thread {
     /*
      * For debugging
      */
     private static int receiverPause = 0;   // ms to pause	
 
     private String conferenceId;
 
     private Selector selector;
 
     private StunServerImpl stunServerImpl;
 
     private boolean done;
 
     ConferenceReceiver(String conferenceId) throws SocketException {
 	this.conferenceId = conferenceId;
 
 	stunServerImpl = new StunServerImpl();
 
 	try {
 	    selector = Selector.open();
 	} catch (IOException e) {
 	    Logger.println("Conference receiver failed to open selector "
 		+ conferenceId + " " + e.getMessage());
 	    
 	    throw new SocketException(
 		"Conference receiver failed to open selector "
                 + conferenceId + " " + e.getMessage());
 	}
 
 	setName("Receiver-" + conferenceId);
 	start();
     }
 
     /*
      * We're not sure of the selector synchronization issues so we add
      * members to register to a vector and have the thread below actually
      * do the register.
      */
     private Vector membersToRegister = new Vector();
 
     private Vector membersToUnregister = new Vector();
 
     private int memberCount = 0;
 
     public void addMember(ConferenceMember member) throws IOException {
 	synchronized(membersToRegister) {
 	    if (selector == null) {
 		return;
 	    }
 
 	    membersToRegister.add(member);
 	    Logger.writeFile("ConferenceReceiver Adding member to register " 
 		+ member + " size " + membersToRegister.size());
 	    selector.wakeup();
 	}
     }
 
     public void removeMember(ConferenceMember member) {
 	synchronized(membersToRegister) {
 	    if (selector == null) {
 		return;
 	    }
 
 	    membersToUnregister.add(member);
 	    Logger.writeFile("ConferenceReceiver adding member to unregister " 
 		+ member + " size " + membersToUnregister.size());
 	    selector.wakeup();
 	}
     }
 
     private void registerMembers() {
 	synchronized(membersToRegister) {
 	    for (int i = 0; i < membersToRegister.size(); i++) {
 		ConferenceMember member = 
 		    (ConferenceMember) membersToRegister.get(i);
 
 	    	Logger.writeFile("ConferenceReceiver registering " + member);
 
 		try {
 		    member.getMemberReceiver().register(selector);
 		    memberCount++;
 		} catch (Exception e) {
 		    Logger.println(
 			"ConferenceReceiver failed to register member "
 			+ member + " " + e.getMessage());
 
 		    membersToRegister.remove(member);
 
		    if (member.getCallHandler() != null) {
		        member.getCallHandler().cancelRequest(
			    "ConferenceReceiver failed to register member ");
		    }
 		}
 	    }
 
 	    membersToRegister.clear();
 
             for (int i = 0; i < membersToUnregister.size(); i++) {
                 ConferenceMember member =
                     (ConferenceMember) membersToUnregister.get(i);
 
                 Logger.writeFile("ConferenceReceiver unregistering " + member);
 
                 member.getMemberReceiver().unregister();
 		memberCount--;
             }
 
 	    membersToUnregister.clear();
         }
     }
 
     /**
      * Receive data and dispatch the data to the appropriate member.
      */
     public void run() {
         while (!done) {
             try {
 	        registerMembers();
 
 		/* 
 		 * Wait for packets to arrive
 		 */
 		int n;
 
 		if ((n = selector.select()) <= 0) {
 		    if (Logger.logLevel == -1) {
 		        Logger.println("select returned " + n
 			    + " isOpen " + selector.isOpen());
 
 			Logger.println("membersToRegister size " 
 			    + membersToRegister.size()
 			    + " membersToUnregister size " 
 			    + membersToUnregister.size());
 
 			Logger.println("keys size " + selector.keys().size()
 			    + " member count " + memberCount);
 		    }
 		    continue;
 		}
 
 		if (Logger.logLevel == -1) {
 	            if (memberCount != selector.keys().size()) {
 		        Logger.println("memberCount " + memberCount
 			    + " not equal to selector key count " 
 			    + selector.keys().size());
 		    }
 		}
 
                 Iterator it = selector.selectedKeys().iterator();
 
 		MemberReceiver memberReceiver;
 
 		/*
 	 	 * Use this buffer to receive the packet if the member hasn't
 		 * finished initializing.  Any buffer will do so it doesn't
 		 * matter that this get changed for every successful receive.
 		 */
                 byte[] data = new byte[100];
                 ByteBuffer byteBuffer = ByteBuffer.wrap(data);
 
                 while (it.hasNext()) {
 		    try {
                         SelectionKey sk = (SelectionKey)it.next();
 
                         it.remove();
 
                         DatagramChannel datagramChannel = 
 			    (DatagramChannel)sk.channel();
 
 		        memberReceiver = (MemberReceiver) sk.attachment();
 
 		        if (!memberReceiver.readyToReceiveData()) {
 		            if (memberReceiver.traceCall() || 
 				    Logger.logLevel == -11) {
 
 		 	        Logger.println("receiver not ready, conference "
 			            + conferenceId
 			            + " " + memberReceiver
 			            + " address " 
 			            + memberReceiver.getReceiveAddress());
 		            }
 
 			    InetSocketAddress isa = (InetSocketAddress)
 				datagramChannel.receive(byteBuffer);
 
 			    if (isStunBindingRequest(data) == true) {
 			        stunServerImpl.processStunRequest(datagramChannel, 
 				    isa, data);
 			    }
 		            continue;	// member isn't ready to receive yet
 		        }
 
                         data = new byte[memberReceiver.getLinearBufferSize()];
 
                         byteBuffer = ByteBuffer.wrap(data);
 
                         InetSocketAddress isa = (InetSocketAddress)
 			    datagramChannel.receive(byteBuffer);
 
 			if (isStunBindingRequest(data) == true) {
 			    stunServerImpl.processStunRequest(datagramChannel,
 				isa, data);
 
 			    continue;
 			}
 		    } catch (NullPointerException e) {
 
                 	/*
                  	 * It's possible to get a null pointer exception when
                  	 * end is called.  The way to avoid this non-fatal error
                  	 * is to synchronize on selector.  
 			 * Catching the exception eliminates the overhead 
 			 * of synchonization in the main receiver loop.
                          */
                         if (!done) {
                             Logger.println(
 				"ConferenceReceiver:  non-fatal NPE.");
                 	}
 			continue;
             	    }
 
 		    if (memberReceiver.traceCall()) {
 			Logger.println("Received data for " + memberReceiver);
 		    }
 
 		    long start = 0;
 
 		    if (memberReceiver.traceCall()) {
 		        start = System.nanoTime();
 		    }
 
 		    /*
 		     * Dispatch to member
 		     */
 		    memberReceiver.receive(data, byteBuffer.position());
 
 		    if (memberReceiver.traceCall()) {
 			memberReceiver.traceCall(false);
 
 			Logger.println("Call " + memberReceiver + " receive time "
 			    + ((System.nanoTime() - start) / 1000000000.) 
 			    + " seconds");
 		    }
 		}
 
                 /*  
                  * XXX For debugging
                  */
                 if (receiverPause != 0) {
 		    if (receiverPause >= 20) {
 		        Logger.println("pause Receiving " 
 			    + receiverPause + "ms");
 		    }
 
                     long start = System.currentTimeMillis();
 
                     while (System.currentTimeMillis() - start < receiverPause)
                         ;
 
 		    if (receiverPause >= 20) {
 		        receiverPause = 0;
 		    }
 		}
             } catch (IOException e) {
 		if (!done) {
 		    /*
 		     * We're not sure why this happens but there appears to be
 		     * a timing problem with selectors when a call ends.
 		     */
                     Logger.error("ConferenceReceiver:  receive failed! " + 
 		        e.getMessage());
 		    e.printStackTrace();
 		}
             } catch (Exception e) {
 		if (!done) {
                     Logger.error("ConferenceReceiver:  unexpected exception " 
 			+ e.getMessage());
 		    e.printStackTrace();
 		}
 	    }
         }
     }
 
     private boolean isStunBindingRequest(byte[] data) {
         /*
          * If this is an RTP packet, the first byte
          * must have bit 7 set indicating RTP v2.
 	 * If byte 0 is 0 and byte 1 is 1, then we
 	 * assume this packet is a STUN Binding request.
          */
         return data[0] == 0 && data[1] == 1;
     }
 
     public static void setReceiverPause(int receiverPause) {
 	ConferenceReceiver.receiverPause = receiverPause;	
     }
 
     /*
      * finished
      */
     public void end() {
 	Logger.writeFile("Conference receiver done " + conferenceId);
 
         done = true;
 	
         synchronized(membersToRegister) {
 	    if (selector != null) {
 	        try {
 	            selector.close();
 	        } catch (IOException e) {
 		    Logger.println(
 			"Conference receiver failed to close selector "
 			+ conferenceId + " " + e.getMessage());
 	    	}
 	        selector = null;
 	    }
 	}
     }
 
 }
