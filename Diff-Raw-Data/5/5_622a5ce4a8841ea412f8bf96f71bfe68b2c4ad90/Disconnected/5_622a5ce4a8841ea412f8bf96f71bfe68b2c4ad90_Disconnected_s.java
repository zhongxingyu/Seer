 package States;
 
 import java.io.IOException;
 
 import Communications.*;
 import Messages.*;
 import Utilities.*;
 
 
 public class Disconnected extends State {
 	public State process(String input, TCP tcp, UDPSender us,Message udpMessage,Message tcpMessage,long timeEnteredState){
 	
 		if(tcp.getActive()==true){
 			return new Connected();
 		}
 		else if(input.startsWith(":ip")){
 			if(input.length()<5){
 				System.out.println("\rAn argument is required");
 				return this;
 			}
 			if(0>tcp.connect(input.substring(4))){
 				System.out.println("\rUnable to connect to IP address");
 			}
 			return this;
 		}
 		else if(input.startsWith(":host")){
 			if(input.length()<7){
 				System.out.println("\rAn argument is required");
 				return this;
 			}
 			if(0>tcp.connect(input.substring(6))){
 				System.out.println("\rUnable to connect to IP address");
 			}
 			return this;
 		}
 		else if(input.startsWith(":local")){
 			if(input.length()<8){
 				System.out.println("\rAn argument is required");
 				return this;
 			}
 			try {
 				String senderUsername=User.getUserName();
 				String targetUsername=input.substring(7);
 				String ip=tcp.getIP();
 				UDPBroadcastMessage message=new UDPBroadcastMessage(1,(long)144,(long)0,"",senderUsername,targetUsername,ip);
 				us.sendMessage(message);
 			} catch (IOException e) {}
 			return new Waiting();
 		}
 		else if(input.startsWith(":")){
 			System.out.println("\rInvalid command");
 			return this;
 		}
 		else if(!input.equals("")){
 			System.out.println("\rYou cannot chat in this state");
 			return this;
 		}
 		else if(udpMessage instanceof UDPBroadcastMessage && udpMessage.getCorrect()){
 				System.out.println("UDP message processed");
 				UDPBroadcastMessage m=(UDPBroadcastMessage)udpMessage;
 				System.out.println("\""+m.targetUsername+"\"");
 				System.out.println("\""+User.userName+"\"");
				if(m.targetUsername.equals(User.userName)){
 					System.out.println("\rReceived a broadcast with to your username from "+m.senderUsername+" at "+m.senderIP);
 					tcp.pendingIP=m.senderIP;
 					return new UserConfirmCallback();
 				}
 			return this;
 		}
 		else{
 			return this;
 		}
 	}
 	
 
 }
