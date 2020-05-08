 package gr.forth.ics.icardea.pid;
 
 import gr.forth.ics.icardea.mllp.HL7MLLPServer;
 
 import java.util.HashSet;
 
 import org.apache.log4j.Logger;
 
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.app.Application;
 import ca.uhn.hl7v2.app.ApplicationException;
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.model.Type;
 import ca.uhn.hl7v2.model.v25.message.QBP_Q21;
 import ca.uhn.hl7v2.model.v25.message.RSP_K23;
 import ca.uhn.hl7v2.model.v25.segment.QPD;
 import ca.uhn.hl7v2.util.Terser;
 
 /**
  * PIX Query: ITI-9 (ITI TF-2a / 3.9)
  * 
  * This transaction involves a request by the Patient Identifier Cross-reference
  * Consumer Actor for a list of patient identifiers that correspond to a patient
  * identifier known by the consumer. The request is received by the Patient
  * Identifier Cross-reference Manager. The Patient Identifier Cross-reference
  * Manager immediately processes the request and returns a response in the form
  * of a list of corresponding patient identifiers, if any.
  * 
  * The Request for Corresponding Patient Identifiers transaction is conducted by
  * the HL7 QBP^Q23 message. Responds with RSP_K23
  * 
  */
 final class PIXQueryHandler implements Application {
 	static Logger logger = Logger.getLogger(PIXQueryHandler.class);
 
 	final static int QIP_FLD_NUM = 3;
 	
 	public void register(HL7MLLPServer s) {
 		s.registerApplication("QBP", "Q23", this);
 	}
 	/*
 	 * What we expect is QBP_Q23 v2.5 message but this is implemented through
 	 * QBP_Q21 according to
 	 * hapi-structures-v25-1.0.1-sources.jar/ca/uhn/hl7v2/parser/eventmap/2.5.properties
 	 */
 	public boolean canProcess(Message msg) {
 		return msg instanceof QBP_Q21;
 	}
 	/**
 	 * See ITI-vol2a, 3.9
 	 */
 	public Message processMessage(Message msg) throws ApplicationException{
 		try {
 			logger.debug("Received:"+msg.encode());
 		} catch (HL7Exception e) {
 			
 		}
 		QBP_Q21 m = (QBP_Q21) msg;
 		QPD qpd = m.getQPD();
 		String qt = qpd.getQpd2_QueryTag().getValue();
 
 		RSP_K23 resp = new RSP_K23();
 		try {
 			String reqMsgCtrlId = m.getMSH().getMessageControlID().getValue();
 			// String reqRcvApp = m.getMSH().getReceivingApplication().encode();
 			String reqSndApp = m.getMSH().getSendingApplication().encode();
 			
 			HL7Utils.fillResponseHeader(m.getMSH(), resp.getMSH());
 			
 			resp.getMSH().getMsh9_MessageType().parse("RSP^K23^RSP_K23");
 			// resp.getMSH().getSendingApplication().parse(reqRcvApp);
 			resp.getMSH().getReceivingApplication().parse(reqSndApp);
 			resp.getMSA().getMessageControlID().setValue(reqMsgCtrlId);
 			
 			AssigningAuthority fromAuth = null;
 			String id = Terser.get(qpd, 3, 0, 1, 1);
 			String ns = Terser.get(qpd, 3, 0, 4, 1);
 			if (ns == null || "".equals(ns)) {
 				String uid = Terser.get(qpd, 3, 0, 4, 2);
				String uid_type = Terser.get(qpd, 3, 0, 4, 3);
				// System.out.println("GOT uid='"+uid+"' type='"+uid_type+"'");
 				fromAuth = AssigningAuthority.find_by_uid(uid, uid_type);
				ns = fromAuth.namespace;
 			}
 			else 
 				fromAuth = AssigningAuthority.find(ns);
 			if (fromAuth == null) {
 				HL7Exception ex = new HL7Exception("Unsupported authority:"+qpd.getField(3, 0).encode(), HL7Exception.UNKNOWN_KEY_IDENTIFIER);
 				ex.setSegmentName("QPD");
 				ex.setSegmentRepetition(1);
 				ex.setFieldPosition(3);
 				throw ex;
 			}
 			
 			iCARDEA_Patient.ID fromId = new iCARDEA_Patient.ID(ns, id);
 
 			HashSet<String> hs = new HashSet<String>();
 			Type[] tt = qpd.getField(4);
 			for (Type t: tt) {
 				AssigningAuthority auth = null;
 				String tons = Terser.getPrimitive(t, 4, 1).getValue();
 				if (tons == null || "".equals(tons)) {
 					String uid = Terser.getPrimitive(t, 4, 2).getValue();
 					String uid_type = Terser.getPrimitive(t, 4, 3).getValue();
 					auth = AssigningAuthority.find_by_uid(uid, uid_type);
 				}
 				else {
 					auth = AssigningAuthority.find(tons);	
 				}
 					
 				if (auth == null) {
 					HL7Exception ex = new HL7Exception("Unsupported authority:"+t.encode(), HL7Exception.UNKNOWN_KEY_IDENTIFIER);
 					ex.setSegmentName("QPD");
 					ex.setSegmentRepetition(1);
 					ex.setFieldPosition(4);
 					throw ex;
 				}
 				hs.add(auth.namespace);
 			}
 			String[] toNS = new String[0];
 			toNS = hs.toArray(toNS);
 			iCARDEA_Patient p = StorageManager.getInstance().retrieve(fromId, toNS);
 								
 			resp.getQAK().getQak1_QueryTag().setValue(qt);
 			resp.getQAK().getQak2_QueryResponseStatus().setValue("OK");
 			resp.getQPD().parse(qpd.encode());
 
 			if (p != null) {
 				// We need to remove namespaces that were not requested
 				// unless no namespace was requested where we return all
 				for (java.util.ListIterator<iCARDEA_Patient.ID> it = p.ids.listIterator(); !hs.isEmpty() && it.hasNext();) {
 					iCARDEA_Patient.ID n = it.next();
 					if (!hs.contains(n.namespace))
 						it.remove();
 				}
 				ca.uhn.hl7v2.model.v25.segment.PID pid = resp.getQUERY_RESPONSE().getPID();
 				p.toPidv25(pid, true);
 				/*
 				 * "To eliminate the issue of conflicting name values between
 				 * Patient Identifier Domains, the Patient Identifier
 				 * Cross-reference Manager Actor shall return in an empty (not
 				 * present) value in the first repetition of field PID-5-Patient
 				 * Name, and shall return a second repetition of field
 				 * PID-5-Patient Name in which the only populated component is
 				 * Component 7 (Name Type Code). Component 7 of repetition 2
 				 * shall contain a value of S (Coded Pseudo-name to assure
 				 * anonymity). All other components of repetition 2 shall be
 				 * empty (not present)."
 				 */
 				pid.getPid5_PatientName(0).parse("");
 				pid.getPid5_PatientName(1).parse("^^^^^^S");
 			}
 			else
 				resp.getQAK().getQak2_QueryResponseStatus().setValue("NF");
 
 			resp.getMSA().getAcknowledgmentCode().setValue("AA");
 		} catch (HL7Exception e) {
 			e.printStackTrace();
 			try {
 				resp.getMSA().getMsa1_AcknowledgmentCode().setValue("AE");
 				// resp.getMSA().getTextMessage().setValue(e.getMessage());
 				HL7Utils.fillErrHeader(resp, e);
 				
 				resp.getQAK().getQak1_QueryTag().setValue(qt);
 				resp.getQAK().getQak2_QueryResponseStatus().setValue("AE");
 				resp.getQPD().parse(qpd.encode());
 			} catch (HL7Exception ex) {
 				ex.printStackTrace();
 				throw new ApplicationException(ex.getMessage(), ex);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				resp.getMSA().getAcknowledgmentCode().setValue("AR");
 				resp.getMSA().getTextMessage().setValue(e.getMessage());
 				resp.getERR().getErr3_HL7ErrorCode().getCwe1_Identifier().setValue(""+HL7Exception.APPLICATION_INTERNAL_ERROR);
 				resp.getERR().getErr3_HL7ErrorCode().getCwe2_Text().setValue(e.getMessage());
 
 				resp.getQAK().getQak1_QueryTag().setValue(qt);
 				resp.getQAK().getQak2_QueryResponseStatus().setValue("AR");
 				resp.getQPD().parse(qpd.encode());
 			} catch (HL7Exception ex) {
 				ex.printStackTrace();
 				throw new ApplicationException(ex.getMessage(), ex);
 			}
 		}
 		try {
 			logger.debug("Sending:"+resp.encode());
 		} catch (HL7Exception e) {
 		}
 		return resp;
 	}
 }
