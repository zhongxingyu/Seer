 package synclogic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.ntnu.fp.net.co.Connection;
 import model.Appointment;
 import model.Invitation;
 import model.InvitationStatus;
 import model.Meeting;
 import model.Notification;
 import model.NotificationType;
 import model.SaveableClass;
 import model.User;
 import model.XmlSerializerX;
 
 public class ClientHandler implements Runnable {
 
 	private Connection connection;
 	private ServerSynchronizationUnit serverSynchronizationUnit;
 	private User user;
 	private List<Object> sendQueue;
 	
 	public ClientHandler(Connection con, ServerSynchronizationUnit ssu) {
 		this.sendQueue = new ArrayList<Object>();
 		this.connection = con;
 		this.serverSynchronizationUnit = ssu;
 	}
 	
 	@Override
 	public void run() {
 		this.receive(false);
 		// Receive login request
 //		try {
 //			this.receive(this.login((LoginRequest) XmlSerializerX.toObject(this.connection.receive())));
 //		} catch (Exception e) {
 //			// Do nothing
 //			e.printStackTrace();
 //		}
 		this.serverSynchronizationUnit.removeClientConnection(this);
 		System.out.println("Client handler should be dead now.");
 	}
 	
 	public boolean login(LoginRequest loginRequest) {
 		System.out.println("Entered login!");
 		try {
 			// Get the user
 			User user = serverSynchronizationUnit.getUser(loginRequest.getUsername());
 			// Check if login is OK
 			if(user == null || user.isOnline()) {
 				loginRequest.setLoginAccepted(false);
 			} else {
 				loginRequest.setLoginAccepted(loginRequest.getUsername().equalsIgnoreCase(user.getUsername()) && user.getPassword().equals(loginRequest.getPassword()) ? true : false);
 			}
 			System.out.println("Sending response!");
 			this.connection.send(XmlSerializerX.toXml(loginRequest, SaveableClass.LoginRequest));
 			user.setOnline(loginRequest.getLoginAccepted());
 			if(loginRequest.getLoginAccepted()) {
 				this.user = user;
 				// Load users, notifications, appointments + meetings
 				// Users
 				this.addToSendQueue(this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.User, null));
 				// Appointments
 				for (SyncListener app : this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.Appointment, null)) {
 					if(((Appointment) app).getOwner() == this.getUser() || this.getUser().getSubscribesTo().contains(((Appointment) app).getOwner())) {
 						this.addToSendQueue(app);
 					}
 				}
 				// Meetings
 				for (SyncListener meeting : this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.Meeting, null)) {
 					if(((Meeting) meeting).getOwner() == this.getUser() || this.getUser().getSubscribesTo().contains(((Meeting) meeting).getOwner())) {
 						this.addToSendQueue(meeting);
 					}
 				}
 				// Notifications
 				for (SyncListener not : this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.Notification, null)) {
 					if(this.user.getNotifications().contains(not)) {
 						this.addToSendQueue(not);
 					}
 				}
 			}
 			return loginRequest.getLoginAccepted();
 		} catch (Exception e) {
 			this.user = null;
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public void receive(boolean isloggedIn) {
 		while(true) {
 			try {
 				// TODO: Timeout!
 				Object o = XmlSerializerX.toObject(this.connection.receive());
 				if(o instanceof LoginRequest && !isloggedIn) {
 					isloggedIn = this.login((LoginRequest) o);
 				} else if(isloggedIn){
 					if(o instanceof ObjectRequest) {
 						// Handle object request
 						ObjectRequest request = (ObjectRequest) o;
 						// Get requested object
 						List<SyncListener> requestedObjects = this.serverSynchronizationUnit.getObjectsFromID(request.getSaveableClass(), request.getObjectID());
 						// Send requested object
 						this.connection.send(XmlSerializerX.toXml(requestedObjects, request.getSaveableClass()));
 						// Remember that the requested object is sent
 						// Den under skal vel ikke vaere her?
 						// this.serverSynchronizationUnit.addUpdatedObjects(requestedObjects);
 					} else if(o instanceof UpdateRequest) {
 						UpdateRequest updateReq = (UpdateRequest) o;
 						// Antar naa at alt som brukeren er interessert i blir puttet i sendQueue. Er det greit?
 						updateReq.addAllObjects(this.sendQueue);
 						// Toem sendQueue
 						this.sendQueue.clear();
 						this.connection.send(XmlSerializerX.toXml(updateReq, SaveableClass.UpdateRequest));
 					} else if(o instanceof List) {
 						this.processReceivedObjects((List) o);
 					} else if(o instanceof SyncListener) {
 						List<SyncListener> l = new ArrayList<SyncListener>();
 						l.add((SyncListener) o);
 						this.processReceivedObjects(l);
 					} else {
 						throw new RuntimeException("Oops! Something went wrong.");
 					}
 				}
 			} catch (Exception e) {
 				// TODO Er denne ok?
 				if(!(e instanceof NullPointerException)) {
 					e.printStackTrace();
 				}
 				System.out.println("Connection was closed by client (or died for some reason)!");
				this.user.setOnline(false);
 				this.serverSynchronizationUnit.removeClientConnection(this);
 				break;
 			}
 		}
 		System.out.println("Should stop receiving now!");
 	}
 	
 	public User getUser() {
 		return this.user;
 	}
 	
 	public void addToSendQueue(Object o) {
 		this.sendQueue.add(o);
 	}
 	
 	public void addToSendQueue(List<Object> objects) {
 		this.sendQueue.addAll(objects);
 	}
 	
 	public void processReceivedObjects(List<SyncListener> objects) {
 		for (SyncListener o : objects) {
 			SyncListener original = this.serverSynchronizationUnit.getObjectFromID(o.getSaveableClass(), o.getObjectID());
 			if(!this.serverSynchronizationUnit.isValidUpdate(o, original, this.getUser())) {
 				this.sendQueue.add(new ErrorMessage(original, o));
 			} else {
 				// Execute update
 				if(original != null) {
 					// Object exists and should be updated
 					// TODO Maa ogsaa gaa inn i alle referansene i objektet for aa oppdatere disse!!!!!!!!!!
 					switch(o.getSaveableClass()) {
 					case Meeting:
 						Meeting originalM = (Meeting) original;
 						Meeting m = (Meeting) o;
 						if(m.isDeleted()) {
 							for(String invID : m.getInvitations()) {
 								Invitation tempInv = (Invitation) this.serverSynchronizationUnit.getObjectFromID(SaveableClass.Invitation, invID);
 								if(tempInv.getStatus() != InvitationStatus.REJECTED && tempInv.getStatus() != InvitationStatus.REVOKED) {
 									for (SyncListener sl : this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.User, null)) {
 										User user = (User) sl;
 										for (Notification uNot : user.getNotifications()) {
 											if(uNot.getInvitation() == tempInv || uNot.getInvitation().getObjectID().equalsIgnoreCase(tempInv.getObjectID())) {
 												Notification newNot = new Notification(tempInv, NotificationType.MEETING_CANCELLED, this.serverSynchronizationUnit.getNewKey(SaveableClass.Notification), this.getUser());
 												user.addNotification(newNot);
 												if(this.serverSynchronizationUnit.getActiveUsers().contains(user)) {
 													this.serverSynchronizationUnit.getClientHandler(user).addToSendQueue(uNot);
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 						// Sjekk om moete har endret tidspunkt
 						if(!originalM.getDate().equals(m.getDate())) {
 							for (String invitationID: m.getInvitations()) {
 								Invitation tempInv = (Invitation) this.serverSynchronizationUnit.getObjectFromID(SaveableClass.Invitation, invitationID);
 								for (SyncListener user : this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.User, null)) {
 									for (Notification uNot : ((User) user).getNotifications()) {
 										if(uNot.getInvitation().getObjectID().equalsIgnoreCase(tempInv.getObjectID())) {
 											Notification newNot = new Notification(uNot.getInvitation(), NotificationType.MEETING_TIME_CHANGED, this.serverSynchronizationUnit.getNewKey(SaveableClass.Notification), m.getOwner());
 											((User) user).addNotification(newNot);
 											// Sjekk om brukeren er online
 											if(this.serverSynchronizationUnit.getActiveUsers().contains((User) user)) {
 												ClientHandler ch = this.serverSynchronizationUnit.getClientHandler((User) user);
 												ch.addToSendQueue(newNot);
 												ch.addToSendQueue(m);
 												
 											}
 										}
 									}
 								}
 							}
 						}
 						// Invite new users
 						this.serverSynchronizationUnit.sendMeetingInvitations(m);
 						// Sjekk om invitasjoner er blitt fjernet
 						for (String originalInvID : originalM.getInvitations()) {
 							boolean invitationFound = false;
 							for (String newInvID : m.getInvitations()) {
 								if(originalInvID.equalsIgnoreCase(newInvID)) {
 									invitationFound = true;
 								}
 							}
 							// Hvis invitasjonen er blitt fjernet
 							if(!invitationFound) {
 								Invitation deletedInvitation = (Invitation) this.serverSynchronizationUnit.getObjectFromID(SaveableClass.Invitation,originalInvID);
 								deletedInvitation.setStatus(InvitationStatus.REVOKED);
 								Notification not = new Notification(deletedInvitation, NotificationType.INVITATION_REVOKED, this.serverSynchronizationUnit.getNewKey(SaveableClass.Notification), deletedInvitation.getMeeting().getOwner());
 								List<SyncListener> users = this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.User, null);
 								outer: for (SyncListener user : users) {
 									for(Notification notification : ((User) user).getNotifications()) {
 										if(notification.getInvitation().getID().equalsIgnoreCase(originalInvID)) {
 											((User) user).addNotification(not);
 											// TODO: Burde notifikasjonen med invitasjonen fjernes (Dette ble vi enig om aa diskutere senere)??
 											break outer;
 										}
 									}
 								}
 							}
 						}
 					case Appointment:
 						// Antar at alt er sjekket (ogsaa romreservasjon), slik at fire tar seg av all oppdatering
 						break;
 					case Invitation:
 						// Invitation er med Notification og trenger ikke aa bli behandlet separat
 						break;
 					case Notification:
 						Notification newNot = (Notification) o;
 						Notification oldNot = (Notification) this.serverSynchronizationUnit.getObjectFromID(SaveableClass.Notification, newNot.getObjectID());
 						if(newNot.getInvitation().getStatus() != oldNot.getInvitation().getStatus()) {
 							if(oldNot.getInvitation().getStatus() == InvitationStatus.NOT_ANSWERED_TIME_CHANGED && newNot.getInvitation().getStatus() == InvitationStatus.REJECTED) {
 								// Send notification til alle inviterte brukere som ikke har svart nei
 								for (String invitationID : newNot.getInvitation().getMeeting().getInvitations()) {
 									Invitation tempInv = (Invitation) this.serverSynchronizationUnit.getObjectFromID(SaveableClass.Invitation, invitationID);
 									if(tempInv.getStatus() != InvitationStatus.REJECTED && tempInv.getStatus() != InvitationStatus.REVOKED) {
 										for (SyncListener user : this.serverSynchronizationUnit.getObjectsFromID(SaveableClass.User, null)) {
 											for (Notification no : ((User) user).getNotifications()) {
 												if(no.getObjectID().equalsIgnoreCase(tempInv.getObjectID())) {
 													// Vi har funnet brukeren som skal ha notificationen
 													((User) user).addNotification(new Notification(tempInv, NotificationType.MEETING_CHANGE_REJECTED, this.serverSynchronizationUnit.getNewKey(SaveableClass.Notification), this.getUser()));
 												}
 											}
 										}
 									}
 								}
 							} else if((oldNot.getInvitation().getStatus() == InvitationStatus.NOT_ANSWERED || oldNot.getInvitation().getStatus() == InvitationStatus.NOT_ANSWERED_TIME_CHANGED) && (newNot.getInvitation().getStatus() == InvitationStatus.ACCEPTED)) {
 								// Bruker godtar invitasjon
 								newNot.getInvitation().getMeeting().getOwner().addNotification(new Notification(newNot.getInvitation(), NotificationType.INVITATION_ACCEPTED, this.serverSynchronizationUnit.getNewKey(SaveableClass.Notification), this.getUser()));
 								newNot.getInvitation().getMeeting().addParticipant(this.getUser());
 								if(this.serverSynchronizationUnit.getActiveUsers().contains(oldNot.getInvitation().getMeeting().getOwner())) {
 									this.serverSynchronizationUnit.getClientHandler(oldNot.getInvitation().getMeeting().getOwner()).addToSendQueue(oldNot.getInvitation().getMeeting());
 								}
 							} else if((oldNot.getInvitation().getStatus() == InvitationStatus.NOT_ANSWERED || oldNot.getInvitation().getStatus() == InvitationStatus.NOT_ANSWERED_TIME_CHANGED) && (newNot.getInvitation().getStatus() == InvitationStatus.REJECTED)) {
 								// Bruker avslaar invitasjon
 								newNot.getInvitation().getMeeting().getOwner().addNotification(new Notification(newNot.getInvitation(), NotificationType.INVITATION_REJECTED, this.serverSynchronizationUnit.getNewKey(SaveableClass.Notification), this.getUser()));
 							}
 							oldNot.getInvitation().fire(SaveableClass.Invitation, newNot.getInvitation());
 						}
 						break;
 					case User:
 						// TODO: Hvis ny user blir subscribet to, maa dennes appointments sendes
 						break;
 					default:
 						throw new RuntimeException("An unexpected object was received!");
 					}
 					this.serverSynchronizationUnit.fire(original.getSaveableClass(), original.getObjectID(), o);
 				} else {
 					// Object does not exist, but should be added
 					this.serverSynchronizationUnit.addObject(o);
 				}
 			}
 		}
 	}
 }
