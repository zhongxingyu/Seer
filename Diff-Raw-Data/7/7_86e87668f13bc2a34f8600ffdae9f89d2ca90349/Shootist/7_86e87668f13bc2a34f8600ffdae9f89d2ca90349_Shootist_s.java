 package test.tck.msgflow.callflows.forkedinvite;
 
 import javax.sip.*;
 import javax.sip.address.*;
 import javax.sip.header.*;
 import javax.sip.message.*;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 
 import test.tck.TestHarness;
 import test.tck.msgflow.callflows.ProtocolObjects;
 
 import java.util.*;
 
 import junit.framework.TestCase;
 
 /**
  * This class is a UAC template. Shootist is the guy that shoots and shootme is
  * the guy that gets shot.
  * 
  * @author M. Ranganathan
  */
 
 public class Shootist implements SipListener {
 
 	private ContactHeader contactHeader;
 
 	private ClientTransaction inviteTid;
 
 	
 	private SipProvider sipProvider;
 
 	private String host = "127.0.0.1";
 
 	private int port;
 
 	private String peerHost = "127.0.0.1";
 
 	private int peerPort;
 
 	private ListeningPoint listeningPoint;
 
 	private static String unexpectedException = "Unexpected exception ";
 
 	private static Logger logger = Logger.getLogger(Shootist.class);
 
 	private ProtocolObjects protocolObjects;
 
 	private Dialog originalDialog;
 
 	private HashSet forkedDialogs;
 
 	private Dialog ackedDialog;
 
 	private Shootist() {
 		this.forkedDialogs = new HashSet();
 	}
 
 	public Shootist(int myPort, int proxyPort, ProtocolObjects protocolObjects) {
 		this();
 		this.protocolObjects = protocolObjects;
 		this.port = myPort;
 		this.peerPort = proxyPort;
 	}
 
 	public void processRequest(RequestEvent requestReceivedEvent) {
 		Request request = requestReceivedEvent.getRequest();
 		ServerTransaction serverTransactionId = requestReceivedEvent
 				.getServerTransaction();
 
 		logger.info("\n\nRequest " + request.getMethod() + " received at "
 				+ protocolObjects.sipStack.getStackName()
 				+ " with server transaction id " + serverTransactionId);
 
 		// We are the UAC so the only request we get is the BYE.
 		if (request.getMethod().equals(Request.BYE))
 			processBye(request, serverTransactionId);
 		else
 			TestHarness.fail("Unexpected request ! : " + request);
 
 	}
 
 	public void processBye(Request request,
 			ServerTransaction serverTransactionId) {
 		try {
 			logger.info("shootist:  got a bye .");
 			if (serverTransactionId == null) {
 				logger.info("shootist:  null TID.");
 				return;
 			}
 			Dialog dialog = serverTransactionId.getDialog();
 			logger.info("Dialog State = " + dialog.getState());
 			Response response = protocolObjects.messageFactory.createResponse(
 					200, request);
 			serverTransactionId.sendResponse(response);
 			logger.info("shootist:  Sending OK.");
 			logger.info("Dialog State = " + dialog.getState());
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.exit(0);
 
 		}
 	}
 
 	public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
 		logger.info("Got a response");
 		Response response = (Response) responseReceivedEvent.getResponse();
 		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
 		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
 
 		logger.info("Response received : Status Code = "
 				+ response.getStatusCode() + " " + cseq);
 
 		Dialog dialog = responseReceivedEvent.getDialog();
 
 		if (tid != null)
 			logger.info("transaction state is " + tid.getState());
 		else
 			logger.info("transaction = " + tid);
 
 		logger.info("Dialog = " + dialog);
 
 		if (dialog != null) {
 			logger.info("Dialog state is " + dialog.getState());
 		} else {
 			logger.info("Dialog is null -- ignoring response!");
 			return;
 		}
 
 		try {
 			if (response.getStatusCode() == Response.OK) {
 				if (cseq.getMethod().equals(Request.INVITE)) {
 					Request ackRequest = dialog.createAck(cseq
 							.getSeqNumber());
 
 						
 					if ( dialog == this.ackedDialog ) {
 						dialog.sendAck(ackRequest);
 						return;
 					}
 					// Proxy will fork. I will accept the second dialog
 					// but not the first.
				
					if ( forkedDialogs.size() == 1 ) {
 						logger.info("Sending ACK");
 						dialog.sendAck(ackRequest);
 						TestHarness.assertTrue(
 								"Dialog state should be CONFIRMED", dialog
 										.getState() == DialogState.CONFIRMED);
 						this.ackedDialog = dialog;
 						
 
 					} else {
 						
 						// Kill the first dialog by sending a bye.
 						SipProvider sipProvider = (SipProvider) responseReceivedEvent
 								.getSource();
 						Request byeRequest = dialog.createRequest(Request.BYE);
 						ClientTransaction ct = sipProvider
 								.getNewClientTransaction(byeRequest);
 						dialog.sendRequest(ct);
 						TestHarness.assertEquals(
 								"Dialog state should be terminated", dialog
 										.getState(), DialogState.TERMINATED);
 					}
					this.forkedDialogs.add(dialog);
 					
 				} else {
 					logger.info("Response method = " + cseq.getMethod());
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.exit(0);
 		}
 
 	}
 
 	public SipProvider createSipProvider() {
 		try {
 			listeningPoint = protocolObjects.sipStack.createListeningPoint(
 					host, port, protocolObjects.transport);
 
 			logger.info("listening point = " + host + " port = " + port);
 			logger.info("listening point = " + listeningPoint);
 			sipProvider = protocolObjects.sipStack
 					.createSipProvider(listeningPoint);
 			return sipProvider;
 		} catch (Exception ex) {
 			logger.error(unexpectedException, ex);
 			TestHarness.fail(unexpectedException);
 			return null;
 		}
 
 	}
 
 	public void checkState() {
 		TestHarness.assertEquals("Should see two distinct dialogs",
 				this.forkedDialogs.size(), 2);
 		TestHarness.assertTrue(
 				"Should see the original (default) dialog in the forked set",
 				this.forkedDialogs.contains(this.originalDialog));
 	}
 
 	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
 
 		logger.info("Transaction Time out");
 	}
 
 	public void sendInvite() {
 		try {
 
 			String fromName = "BigGuy";
 			String fromSipAddress = "here.com";
 			String fromDisplayName = "The Master Blaster";
 
 			String toSipAddress = "there.com";
 			String toUser = "LittleGuy";
 			String toDisplayName = "The Little Blister";
 
 			// create >From Header
 			SipURI fromAddress = protocolObjects.addressFactory.createSipURI(
 					fromName, fromSipAddress);
 
 			Address fromNameAddress = protocolObjects.addressFactory
 					.createAddress(fromAddress);
 			fromNameAddress.setDisplayName(fromDisplayName);
 			FromHeader fromHeader = protocolObjects.headerFactory
 					.createFromHeader(fromNameAddress, "12345");
 
 			// create To Header
 			SipURI toAddress = protocolObjects.addressFactory.createSipURI(
 					toUser, toSipAddress);
 			Address toNameAddress = protocolObjects.addressFactory
 					.createAddress(toAddress);
 			toNameAddress.setDisplayName(toDisplayName);
 			ToHeader toHeader = protocolObjects.headerFactory.createToHeader(
 					toNameAddress, null);
 
 			// create Request URI
 			String peerHostPort = peerHost + ":" + peerPort;
 			SipURI requestURI = protocolObjects.addressFactory.createSipURI(
 					toUser, peerHostPort);
 
 			// Create ViaHeaders
 
 			ArrayList viaHeaders = new ArrayList();
 			ViaHeader viaHeader = protocolObjects.headerFactory
 					.createViaHeader(host, sipProvider.getListeningPoint(
 							protocolObjects.transport).getPort(),
 							protocolObjects.transport, null);
 
 			// add via headers
 			viaHeaders.add(viaHeader);
 
 			SipURI sipuri = protocolObjects.addressFactory.createSipURI(null,
 					host);
 			sipuri.setPort(peerPort);
 			sipuri.setLrParam();
 
 			RouteHeader routeHeader = protocolObjects.headerFactory
 					.createRouteHeader(protocolObjects.addressFactory
 							.createAddress(sipuri));
 
 			// Create ContentTypeHeader
 			ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
 					.createContentTypeHeader("application", "sdp");
 
 			// Create a new CallId header
 			CallIdHeader callIdHeader = sipProvider.getNewCallId();
 			// JvB: Make sure that the implementation matches the messagefactory
 			callIdHeader = protocolObjects.headerFactory
 					.createCallIdHeader(callIdHeader.getCallId());
 
 			// Create a new Cseq header
 			CSeqHeader cSeqHeader = protocolObjects.headerFactory
 					.createCSeqHeader(1L, Request.INVITE);
 
 			// Create a new MaxForwardsHeader
 			MaxForwardsHeader maxForwards = protocolObjects.headerFactory
 					.createMaxForwardsHeader(70);
 
 			// Create the request.
 			Request request = protocolObjects.messageFactory.createRequest(
 					requestURI, Request.INVITE, callIdHeader, cSeqHeader,
 					fromHeader, toHeader, viaHeaders, maxForwards);
 			// Create contact headers
 
 			SipURI contactUrl = protocolObjects.addressFactory.createSipURI(
 					fromName, host);
 			contactUrl.setPort(listeningPoint.getPort());
 
 			// Create the contact name address.
 			SipURI contactURI = protocolObjects.addressFactory.createSipURI(
 					fromName, host);
 			contactURI.setPort(sipProvider.getListeningPoint(
 					protocolObjects.transport).getPort());
 			contactURI.setTransportParam(protocolObjects.transport);
 
 			Address contactAddress = protocolObjects.addressFactory
 					.createAddress(contactURI);
 
 			// Add the contact address.
 			contactAddress.setDisplayName(fromName);
 
 			contactHeader = protocolObjects.headerFactory
 					.createContactHeader(contactAddress);
 			request.addHeader(contactHeader);
 
 			// Dont use the Outbound Proxy. Use Lr instead.
 			request.setHeader(routeHeader);
 
 			// Add the extension header.
 			Header extensionHeader = protocolObjects.headerFactory
 					.createHeader("My-Header", "my header value");
 			request.addHeader(extensionHeader);
 
 			String sdpData = "v=0\r\n"
 					+ "o=4855 13760799956958020 13760799956958020"
 					+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
 					+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
 					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
 					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
 					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
 			byte[] contents = sdpData.getBytes();
 
 			request.setContent(contents, contentTypeHeader);
 
 			extensionHeader = protocolObjects.headerFactory.createHeader(
 					"My-Other-Header", "my new header value ");
 			request.addHeader(extensionHeader);
 
 			Header callInfoHeader = protocolObjects.headerFactory.createHeader(
 					"Call-Info", "<http://www.antd.nist.gov>");
 			request.addHeader(callInfoHeader);
 
 			// Create the client transaction.
 			inviteTid = sipProvider.getNewClientTransaction(request);
 			Dialog dialog = inviteTid.getDialog();
 
 			TestHarness.assertTrue("Initial dialog state should be null",
 					dialog.getState() == null);
 
 			// send the request out.
 			inviteTid.sendRequest();
 
 			this.originalDialog = dialog;
 			// This is not a valid test. There is a race condition in this test
 			// the response may have already come in and reset the state of the tx
 			// to proceeding.
 			// TestHarness.assertSame(
 			//		"Initial transaction state should be CALLING", inviteTid
 			//				.getState(), TransactionState.CALLING);
 
 		} catch (Exception ex) {
 			logger.error(unexpectedException, ex);
 			TestHarness.fail(unexpectedException);
 
 		}
 	}
 
 	public void processIOException(IOExceptionEvent exceptionEvent) {
 		logger.info("IOException happened for " + exceptionEvent.getHost()
 				+ " port = " + exceptionEvent.getPort());
 
 	}
 
 	public void processTransactionTerminated(
 			TransactionTerminatedEvent transactionTerminatedEvent) {
 		logger.info("Transaction terminated event recieved");
 	}
 
 	public void processDialogTerminated(
 			DialogTerminatedEvent dialogTerminatedEvent) {
 		logger.info("dialogTerminatedEvent");
 
 	}
 }
