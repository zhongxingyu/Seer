 package com.fornacif.lotocado.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeUtility;
 
 import com.floreysoft.jmte.Engine;
 import com.fornacif.lotocado.model.DrawingLotsRequest;
 import com.fornacif.lotocado.model.DrawingLotsResponse;
 import com.fornacif.lotocado.model.Event;
 import com.fornacif.lotocado.model.EventParticipantIds;
 import com.fornacif.lotocado.model.Participant;
 import com.fornacif.lotocado.utils.Constants;
 import com.fornacif.lotocado.utils.Encryptor;
 import com.google.api.server.spi.IoUtil;
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.response.BadRequestException;
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
 	
 	private final Logger LOGGER = Logger.getLogger(RandomMatcher.class.getName());
 
 	public DrawingLotsResponse createDrawingLots(DrawingLotsRequest drawingLotsRequest) throws BadRequestException {
 		Event event = drawingLotsRequest.getEvent();
 		List<Participant> participants = drawingLotsRequest.getParticipants();
 
 		Transaction transaction = datastoreService.beginTransaction();
 		try {
 			saveEvent(event);
 			saveParticipants(event.getKey(), participants);
 
 			boolean performRandom = true;
 			int retryCount = 0;
 			while (performRandom && retryCount < MAX_RETRY_COUNT) {
 				performRandom = randomize(participants);
 				retryCount++;
 			}
 
 			if (retryCount != MAX_RETRY_COUNT) {
 				try {
 					saveResults(participants);
 				} catch (EntityNotFoundException e) {
 					throw new BadRequestException("{\"code\": \"" + Constants.NOT_PERSISTED_ERROR_CODE + "\"}");
 				}
 
 				try {
 					sendEmailToOrganizer(event);
 				} catch (MessagingException | IOException e) {
 					throw new BadRequestException("{\"code\": \"" + Constants.SEND_MAIL_TO_ORGANIZER_ERROR_CODE + "\"}");
 				}
 
 				for (Participant participant : participants) {
 					try {
 						sendEmailToParticipant(event, participant);
 					} catch (MessagingException | IOException e) {
 						throw new BadRequestException("{\"code\": \"" + Constants.SEND_MAIL_TO_PARTICIPANT_ERROR_CODE + "\",\"participantName\":" + participant.getName() + "}");
 					}
 				}
 
 				transaction.commit();
 				
 				DrawingLotsResponse response = new DrawingLotsResponse();
 				response.setOrganizerLink(getOrganizerLink(event));
 				return response;
 			} else {
 				throw new BadRequestException("{\"code\": \"" + Constants.NO_RESULT_ERROR_CODE + "\"}");
 			}
 		}
 		catch (BadRequestException e) {
 			throw e;
 		} catch (Throwable t) {
 			throw new BadRequestException("{\"code\": \"" + Constants.DATA_ERROR_CODE + "\"}");
 		} finally {
 			if (transaction.isActive()) {
 				transaction.rollback();
 			}
 		}
 	}
 
 	private void saveEvent(Event event) {
 		Entity eventEntity = new Entity(Constants.EVENT_ENTITY);
 		eventEntity.setProperty(Constants.EVENT_NAME, event.getName());
 		eventEntity.setProperty(Constants.EVENT_ORGANIZER_NAME, event.getOrganizerName());
 		eventEntity.setProperty(Constants.EVENT_ORGANIZER_EMAIL, event.getOrganizerEmail());
 		eventEntity.setProperty(Constants.EVENT_DATE, event.getDate());
 		datastoreService.put(eventEntity);
 		event.setKey(eventEntity.getKey());
 	}
 
 	private void saveParticipants(Key eventKey, List<Participant> participants) {
 		Map<String, Key> hashKeyToKey = new HashMap<>();
 
 		for (Participant participant : participants) {
 			Entity participantEntity = new Entity(Constants.PARTICIPANT_ENTITY, eventKey);
 			participantEntity.setProperty(Constants.PARTICIPANT_EMAIL, participant.getEmail());
 			participantEntity.setProperty(Constants.PARTICIPANT_NAME, participant.getName());
 			participantEntity.setProperty(Constants.PARTICIPANT_IS_RESULT_CONSULTED, participant.isResultConsulted());
 			participantEntity.setProperty(Constants.PARTICIPANT_EVENT_KEY, eventKey);
 			datastoreService.put(participantEntity);
 
 			Key key = participantEntity.getKey();
 			participant.setKey(key);
 			participant.setEntity(participantEntity);
 			
 			hashKeyToKey.put(participant.getHashKey(), key);
 		}
 
 		for (Participant participant : participants) {
 			List<String> exclusionHashKeys = participant.getExclusionHashKeys();
 			List<Key> exclusionKeys = new ArrayList<>();
 
 			for (String exclusionHashKey : exclusionHashKeys) {
 				exclusionKeys.add(hashKeyToKey.get(exclusionHashKey));
 			}
 			participant.setExclusionKeys(exclusionKeys);
 		}
 	}
 
 	private void saveResults(List<Participant> participants) throws EntityNotFoundException {
 		for (Participant participant : participants) {
 			Entity participantEntity = participant.getEntity();
 			participantEntity.setProperty(Constants.PARTICIPANT_TO_KEY, participant.getToKey());
 			participantEntity.setProperty(Constants.PARTICIPANT_TO_NAME, participant.getToName());
 			participantEntity.setProperty(Constants.PARTICIPANT_EXCLUSION_KEYS, participant.getExclusionKeys());
 			datastoreService.put(participantEntity);
 		}
 	}
 
 	private void resetDrawingLots(List<Participant> participants) {
 		for (Participant participant : participants) {
 			participant.setToKey(null);
 			participant.setToName(null);
 		}
 	}
 
 	private boolean randomize(List<Participant> participants) {
 		Random random = new Random();
 
 		for (Participant currentParticipant : participants) {
 			List<Participant> recievers = new ArrayList<Participant>();
 			for (Participant reciever : participants) {
 				if (!currentParticipant.equals(reciever) && !currentParticipant.getExclusionKeys().contains(reciever.getKey()) && reciever.getToKey() == null) {
 					recievers.add(reciever);
 				}
 			}
 
 			if (recievers.size() != 0) {
 				int receiverPosition = random.nextInt(recievers.size());
 				Participant reciever = recievers.get(receiverPosition);
 				reciever.setToKey(currentParticipant.getEntity().getKey());
 				reciever.setToName(currentParticipant.getName());
 			} else {
 				resetDrawingLots(participants);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private void sendEmailToOrganizer(Event event) throws MessagingException, IOException {
 		String recipientEmail = event.getOrganizerEmail();
 		String recipientName = event.getOrganizerName();
 		String subject = "[Lotocado] Evènement '" + event.getName() + "'";
 		String body = getEmailToOrganizerBodyContent(event);
 		sendEmail(recipientEmail, recipientName, subject, body);
 	}
 
 	private void sendEmailToParticipant(Event event, Participant participant) throws MessagingException, IOException {
		String recipientEmail = event.getOrganizerEmail();
		String recipientName = event.getOrganizerName();
 		String subject = "[Lotocado] Invitation à l'évènement '" + event.getName() + "'";
 		String body = getEmailToParticipantBodyContent(event, participant);
 		sendEmail(recipientEmail, recipientName, subject, body);
 	}
 
 	private void sendEmail(String recipientEmail, String recipientName, String subject, String body) throws MessagingException, UnsupportedEncodingException {
 		Properties properties = new Properties();
 		Session session = Session.getDefaultInstance(properties, null);
 
 		Message message = new MimeMessage(session);
 		message.setFrom(new InternetAddress("contact.lotocado@gmail.com", "Lotocado"));
 		message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, recipientName));
 		message.setSubject(MimeUtility.encodeText(subject, "utf-8", "B"));
 		message.setContent(body, "text/html; charset=utf-8");
 		Transport.send(message);
 	}
 
 	private String getEmailToOrganizerBodyContent(Event event) throws IOException {
 		String input = IoUtil.readFile(new File("templates/event.html"));
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("event", event);
 		model.put("link", getOrganizerLink(event));
 		Engine engine = new Engine();
 		return engine.transform(input, model);
 	}
 
 	private String getEmailToParticipantBodyContent(Event event, Participant participant) throws IOException {
 		String input = IoUtil.readFile(new File("templates/participant.html"));
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("event", event);
 		model.put("participant", participant);
 		model.put("link", getParticipantLink(event, participant));
 		Engine engine = new Engine();
 		return engine.transform(input, model);
 	}
 	
 	private String getOrganizerLink(Event event) {
 		return getHostUrl() + "/#/event/" + Encryptor.encryptEventId(event.getKey().getId());
 	}
 	
 	private String getParticipantLink(Event event, Participant participant) {
 		EventParticipantIds ids = new EventParticipantIds(event.getKey().getId(), participant.getKey().getId());
 		return getHostUrl() + "/#/participant/" + Encryptor.encryptEventParticipantIds(ids);
 	}
 
 	private String getHostUrl() {
 		String hostUrl;
 		String environment = System.getProperty("com.google.appengine.runtime.environment");
 		if ("Production".equals(environment)) {
 			String applicationId = System.getProperty("com.google.appengine.application.id");
 			String version = System.getProperty("com.google.appengine.application.version");
 			hostUrl = "http://" + applicationId + ".appspot.com/";
 		} else {
 			hostUrl = "http://localhost:8888";
 		}
 		return hostUrl;
 	}
 }
