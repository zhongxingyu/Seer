 package com.fornacif.lotocado.service;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import com.fornacif.lotocado.model.DrawingLotsRequest;
 import com.fornacif.lotocado.model.Event;
 import com.fornacif.lotocado.model.Participant;
 import com.fornacif.lotocado.utils.Constants;
import com.google.api.server.spi.ServiceException;
 import com.google.api.server.spi.config.Api;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Transaction;
 
 @Api(name = "lotocado", version = "v1")
 public class RandomMatcher {
 
 	private static final int MAX_RETRY_COUNT = 10000;
 
 	private final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
 
	public List<Participant> createDrawingLots(DrawingLotsRequest drawingLotsRequest) throws ServiceException {
 		Event event = drawingLotsRequest.getEvent();
 		List<Participant> participants = drawingLotsRequest.getParticipants();
 
 		Transaction transaction = datastoreService.beginTransaction();
 		try {
 			Key eventKey = saveEvent(event);
 			saveParticipants(eventKey, participants);
 
 			boolean performRandom = true;
 			int retryCount = 0;
 			while (performRandom && retryCount < MAX_RETRY_COUNT) {
 				performRandom = randomize(participants);
 				retryCount++;
 			}
 
 			if (retryCount != MAX_RETRY_COUNT) {
 				try {
 					saveResults(participants);
 					sendEmails(event, participants);
 					transaction.commit();
 				} catch (EntityNotFoundException e) {
					throw new ServiceException(Constants.NOT_PERSISTED_ERROR_CODE, "A problem occured during the drawing lots computation");
 				} catch (MessagingException | UnsupportedEncodingException e) {
					throw new ServiceException(Constants.SEND_MAIL_ERROR_CODE, "A problem occured during emails sending");
 				}
 				return participants;
 			} else {
				throw new ServiceException(Constants.NO_RESULT_ERROR_CODE, "A problem occured during the drawing lots computation");
 			}
 		} finally {
 			if (transaction.isActive()) {
 				transaction.rollback();
 			}
 		}
 	}
 
 	private Key saveEvent(Event event) {
 		Entity eventEntity = new Entity(Constants.EVENT_ENTITY);
 		eventEntity.setProperty(Constants.EVENT_NAME, event.getName());
 		eventEntity.setProperty(Constants.EVENT_ORGANIZER_NAME, event.getOrganizerName());
 		eventEntity.setProperty(Constants.EVENT_ORGANIZER_EMAIL, event.getOrganizerEmail());
 		datastoreService.put(eventEntity);
 		return eventEntity.getKey();
 	}
 
	private void saveParticipants(Key eventKey, List<Participant> participants) {
 		Map<String, Long> hashKeyToId = new HashMap<>();
 
 		for (Participant participant : participants) {
 			Entity participantEntity = new Entity(Constants.PARTICIPANT_ENTITY, eventKey);
 			participantEntity.setProperty(Constants.PARTICIPANT_EMAIL, participant.getEmail());
 			participantEntity.setProperty(Constants.PARTICIPANT_NAME, participant.getName());
 			participantEntity.setProperty(Constants.PARTICIPANT_EVENT_KEY, eventKey);
 			datastoreService.put(participantEntity);
 
 			Long id = participantEntity.getKey().getId();
 			hashKeyToId.put(participant.getHashKey(), id);
 			participant.setId(id);
 			participant.setEntity(participantEntity);
 		}
 
 		for (Participant participant : participants) {
 			List<String> exclusionHashKeys = participant.getExclusionHashKeys();
 			List<Long> exclusionIds = new ArrayList<>();
 
 			for (String exclusionHashKey : exclusionHashKeys) {
 				exclusionIds.add(hashKeyToId.get(exclusionHashKey));
 			}
 			participant.setExclusionIds(exclusionIds);
 		}
 	}
 
 	private void saveResults(List<Participant> participants) throws EntityNotFoundException {
 		for (Participant participant : participants) {
 			Entity participantEntity = participant.getEntity();
 			participantEntity.setProperty(Constants.PARTICIPANT_GIVER_ID, participant.getGiverId());
 			participantEntity.setProperty(Constants.PARTICIPANT_RECEIVER_ID, participant.getReceiverId());
 			participantEntity.setProperty(Constants.PARTICIPANT_EXCLUSION_IDS, participant.getExclusionIds());
 			datastoreService.put(participantEntity);
 		}
 	}
 
 	private void resetDrawingLots(List<Participant> participants) {
 		for (Participant participant : participants) {
 			participant.setGiverId(null);
 			participant.setReceiverId(null);
 		}
 	}
 
 	private boolean randomize(List<Participant> participants) {
 		Random random = new Random();
 
 		for (Participant currentParticipant : participants) {
 			List<Participant> recievers = new ArrayList<Participant>();
 			for (Participant reciever : participants) {
 				if (!currentParticipant.equals(reciever) && !currentParticipant.getExclusionIds().contains(reciever.getId()) && reciever.getGiverId() == null) {
 					recievers.add(reciever);
 				}
 			}
 
 			if (recievers.size() != 0) {
 				int receiverPosition = random.nextInt(recievers.size());
 				Participant reciever = recievers.get(receiverPosition);
 
 				currentParticipant.setReceiverId(reciever.getId());
 				reciever.setGiverId(currentParticipant.getId());
 			} else {
 				resetDrawingLots(participants);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private void sendEmails(Event event, List<Participant> participants) throws MessagingException, UnsupportedEncodingException {
 		sendEmailToOrganizer(event);
 	}
 	
 	private void sendEmailToOrganizer(Event event) throws MessagingException, UnsupportedEncodingException {
 		String recipientEmail = event.getOrganizerEmail();
 		String recipientName = event.getOrganizerName();
 		String subject = "Event " + event.getName() + " created";
 		String body = "Event " + event.getName() + " created";
 		sendEmail(recipientEmail, recipientName, subject, body);
 	}
 	
 	private void sendEmail(String recipientEmail, String recipientName, String subject, String body) throws MessagingException, UnsupportedEncodingException {
 		Properties properties = new Properties();
 		Session session = Session.getDefaultInstance(properties, null);
 
 		Message msg = new MimeMessage(session);
 		msg.setFrom(new InternetAddress("francois.fornaciari@gmail.com", "Admin"));
 		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, recipientName));
 		msg.setSubject(subject);
 		msg.setText(body);
 		Transport.send(msg);
 	}
 }
