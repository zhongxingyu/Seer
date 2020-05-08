 package com.orange.play.events.types.templates;
 
 import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_DEFAULT_SYNTAX;
 import static eu.play_project.play_commons.constants.Namespace.EVENTS;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.event_processing.events.types.Event;
 import org.event_processing.events.types.UcTelcoComposeMail;
 import org.junit.Test;
 import org.ontoware.rdf2go.exception.ModelRuntimeException;
 import org.ontoware.rdf2go.exception.SyntaxNotSupportedException;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 
 import com.orange.play.events.types.templates.rdf.FacebookStatusFeedEvent;
 import com.orange.play.events.types.templates.rdf.UcTelcoCall;
 import com.orange.play.events.types.templates.rdf.UcTelcoEsrRecom;
 import com.orange.play.events.types.templates.rdf.UcTelcoGeoLocation;
 import com.orange.play.events.types.templates.rdf.geo_Point;
 import com.orange.play.events.types.templates.rdf.esrActions.esr_Action;
 import com.orange.play.events.types.templates.rdf.esrActions.uctelco_ComposeMail;
 
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 
 public class RDFTemplatesValidationTests {
 
 	final String callerPhoneNumberPar = "33638611526";
 	final String calleePhoneNumberPar = "33638860864";
 	final String directionPar = "outgoing";
 	final String messagePar = "test";
 	final int sequenceNumber = 1234;
 	
 	final String uniqueId = "esr1340688541673999872";
 	final String eventId = EVENTS.getUri() + uniqueId;
 	
 	final Calendar now = Calendar.getInstance();
 	
 	@Test
 	public void testCallEvents() throws ModelRuntimeException, IOException, URISyntaxException {
 
 		
 		/*
 		 * Create and print an event with the template: 
 		 */
 		UcTelcoCall templEvent = new UcTelcoCall(){{
 			setEndTime(now.getTime());
 			setUctelco_sequenceNumber(sequenceNumber);
 			setStream(new URI(Stream.TaxiUCCall.getUri()));
 			setMessage(messagePar);
 			setUctelco_calleePhoneNumber(calleePhoneNumberPar);
 			setUctelco_callerPhoneNumber(callerPhoneNumberPar);
 			setUctelco_direction(directionPar);
 			setUctelco_uniqueId(uniqueId);
 			setLocation(new geo_Point(){{
 				setGeo_lat(1.0);
 				setGeo_long(2.0);
 			}});
 		}};
 		
 		/*
 		 * Create and print an event with SDK:
 		 */
 		org.event_processing.events.types.UcTelcoCall sdkEvent = new org.event_processing.events.types.UcTelcoCall(EventHelpers.createEmptyModel(eventId),
 				eventId + EVENT_ID_SUFFIX, true);
 		sdkEvent.setEndTime(now);
 		sdkEvent.setUcTelcoSequenceNumber(sequenceNumber);
 		sdkEvent.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
 		sdkEvent.setUcTelcoCalleePhoneNumber(calleePhoneNumberPar);
 		sdkEvent.setUcTelcoCallerPhoneNumber(callerPhoneNumberPar);
 		sdkEvent.setUcTelcoUniqueId(new URIImpl(eventId + EVENT_ID_SUFFIX));
 		sdkEvent.setMessage(messagePar);
 		sdkEvent.setUcTelcoDirection(directionPar);
 		sdkEvent.setUcTelcoUniqueId(uniqueId);
 		EventHelpers.setLocationToEvent(sdkEvent, "blank://0", 1.0, 2.0);
 		
 		/*
 		 * Compare the two: 
 		 */
 		compareEvents(templEvent.toRDF(uniqueId), sdkEvent);
 	}
 	
 	@Test
 	public void testEsrRecomEvents() throws ModelRuntimeException, IOException, URISyntaxException {
 		
 		/*
 		 * Create and print an event with the template: 
 		 */
 		UcTelcoEsrRecom templEvent = new UcTelcoEsrRecom(){{
 			setEndTime(now.getTime());
 			setUctelco_sequenceNumber(sequenceNumber);
 			setStream(new URI(Stream.TaxiUCESRRecom.getUri()));
 			setMessage("write a mail to your friend");
 			setUctelco_calleePhoneNumber(calleePhoneNumberPar);
 			setUctelco_callerPhoneNumber(callerPhoneNumberPar);
 			setUctelco_uniqueId(uniqueId);
 			setLocation(new geo_Point(){{
 				setGeo_lat(1.0);
 				setGeo_long(2.0);
 			}});
 			Set<esr_Action> esr_Actions = new HashSet<esr_Action>();
 			esr_Actions.add(new uctelco_ComposeMail(){{
 				setUctelco_mailAddress(new URI("mailto:differenziale@gmail.com"));
 				setUctelco_mailContent("This is the initial content for the mail");
 			}});
 			setEsr_recommendation(new URI("http://imu.ntua.gr/san/esr/1.1/recommendation/esr1234567890123456789"));
 			setUctelco_ackRequired(true);
 			setUctelco_answerRequired(true);
 			setUctelco_action(esr_Actions);
 		}};
 		
 		/*
 		 * Create and print an event with SDK:
 		 */
 		org.event_processing.events.types.UcTelcoEsrRecom sdkEvent = new org.event_processing.events.types.UcTelcoEsrRecom(EventHelpers.createEmptyModel(eventId),
 				eventId + EVENT_ID_SUFFIX, true);
 		sdkEvent.setEndTime(now);
 		sdkEvent.setUcTelcoSequenceNumber(sequenceNumber);
 		sdkEvent.setStream(new URIImpl(Stream.TaxiUCESRRecom.getUri()));
 		sdkEvent.setUcTelcoCalleePhoneNumber(calleePhoneNumberPar);
 		sdkEvent.setUcTelcoCallerPhoneNumber(callerPhoneNumberPar);
 		sdkEvent.setUcTelcoUniqueId(new URIImpl(eventId + EVENT_ID_SUFFIX));
 		sdkEvent.setMessage("write a mail to your friend");
 		sdkEvent.setUcTelcoUniqueId(uniqueId);
 		sdkEvent.setEsrRecommendation(new URIImpl("http://imu.ntua.gr/san/esr/1.1/recommendation/esr1234567890123456789"));
 		sdkEvent.setUcTelcoAnswerRequired(true);
 		sdkEvent.setUcTelcoAckRequired(true);
 		EventHelpers.setLocationToEvent(sdkEvent, "blank://0", 1.0, 2.0);
 		UcTelcoComposeMail action1 = new UcTelcoComposeMail(sdkEvent.getModel(), "blank://1", true);
 		action1.setUcTelcoMailAddress(new URIImpl("mailto:differenziale@gmail.com"));
 		action1.setUcTelcoMailContent("This is the initial content for the mail");
 		sdkEvent.addUcTelcoAction(action1);
 		
 		/*
 		 * Compare the two: 
 		 */
 		compareEvents(templEvent.toRDF(uniqueId), sdkEvent);
 	}
 
 	@Test
 	public void testFacebookEvents() throws ModelRuntimeException, IOException, URISyntaxException {
 	
 		
 		/*
 		 * Create and print an event with the template: 
 		 */
 		FacebookStatusFeedEvent templEvent = new FacebookStatusFeedEvent(){{
 			setEndTime(now.getTime());
 			setStream(new URI(Stream.FacebookStatusFeed.getUri()));
 			setMessage("This is going to be shown");
 			setLocation(new geo_Point(){{
 				setGeo_lat(1.0);
 				setGeo_long(2.0);
 			}});
 		}};
 		
 		/*
 		 * Create and print an event with SDK:
 		 */
 		org.event_processing.events.types.FacebookStatusFeedEvent sdkEvent = new org.event_processing.events.types.FacebookStatusFeedEvent(EventHelpers.createEmptyModel(eventId),
 				eventId + EVENT_ID_SUFFIX, true);
 		sdkEvent.setEndTime(now);
 		sdkEvent.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));
 		sdkEvent.setMessage("This is going to be shown");
 		EventHelpers.setLocationToEvent(sdkEvent, "blank://0", 1.0, 2.0);
 		
 		/*
 		 * Compare the two: 
 		 */
 		compareEvents(templEvent.toRDF(uniqueId), sdkEvent);
 	}
 	
 	@Test
 	public void testGeolocationEvents() throws ModelRuntimeException, IOException, URISyntaxException {
 	
 		
 		/*
 		 * Create and print an event with the template: 
 		 */
 		UcTelcoGeoLocation templEvent = new UcTelcoGeoLocation(){{
 			setEndTime(now.getTime());
 			setStream(new URI(Stream.TaxiUCGeoLocation.getUri()));
 			setLocation(new geo_Point(){{
 				setGeo_lat(1.0);
 				setGeo_long(2.0);
 			}});
 			setScreenNumber("roland.stuehmer");
 			setUctelco_mailAddress(new URI("mailto:roland.stuehmer@fzi.de"));
 			setUctelco_phoneNumber(callerPhoneNumberPar);
 			setUctelco_sequenceNumber(sequenceNumber);
 			setUctelco_uniqueId(uniqueId);
 		}};
 		
 		/*
 		 * Create and print an event with SDK:
 		 */
 		org.event_processing.events.types.UcTelcoGeoLocation sdkEvent = new org.event_processing.events.types.UcTelcoGeoLocation(EventHelpers.createEmptyModel(eventId),
 				eventId + EVENT_ID_SUFFIX, true);
 		sdkEvent.setEndTime(now);
 		sdkEvent.setStream(new URIImpl(Stream.TaxiUCGeoLocation.getUri()));
 		EventHelpers.setLocationToEvent(sdkEvent, "blank://0", 1.0, 2.0);
 		sdkEvent.setTwitterScreenName("roland.stuehmer");
 		sdkEvent.setUcTelcoMailAddress(new URIImpl("mailto:roland.stuehmer@fzi.de"));
 		sdkEvent.setUcTelcoPhoneNumber(callerPhoneNumberPar);
 		sdkEvent.setUcTelcoSequenceNumber(sequenceNumber);
 		sdkEvent.setUcTelcoUniqueId(uniqueId);
 		
 		/*
 		 * Compare the two: 
 		 */
 		compareEvents(templEvent.toRDF(uniqueId), sdkEvent);
 	}
 
 
 	private void compareEvents(String templEvent, Event sdkEvent) throws SyntaxNotSupportedException, ModelRuntimeException, IOException {
 
 		/*
 		 * Template event:
 		 */
 		ModelSet templRdf = EventHelpers.createEmptyModelSet();
 		System.out.println("=============== templRdf: =============================");
 		System.out.println(templEvent); // Print raw template output
 		templRdf.readFrom(new StringReader(templEvent), Syntax.forMimeType(WSN_MSG_DEFAULT_SYNTAX));
 		//System.out.println(templRdf.getModels().next().serialize(Syntax.Turtle)); // Print parsed and reformatted RDF
 
 		/*
 		 * SDK event:
 		 */
 		ModelSet sdkRdf = EventHelpers.createEmptyModelSet();
 		sdkRdf.addModel(sdkEvent.getModel());
		System.out.println("=============== templRdf: =============================");
 		System.out.println(sdkRdf.getModels().next().serialize(Syntax.Turtle));
 
 		/*
 		 * Compare the two: 
 		 */
		// This is bit of a hack but both iterators only have one model:
 		assertTrue(sdkRdf.getModels().next().isIsomorphicWith(templRdf.getModels().next()));
 
 	}
 
 }
