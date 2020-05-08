 package org.motechproject.ghana.national.repository;
 
 import ch.lambdaj.group.Group;
 import org.motechproject.MotechException;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.domain.AlertWindow;
 import org.motechproject.ghana.national.domain.SmsTemplateKeys;
 import org.motechproject.ghana.national.messagegateway.domain.*;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.tools.Utility;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.*;
 
 import static ch.lambdaj.Lambda.*;
 import static ch.lambdaj.group.Groups.by;
 import static ch.lambdaj.group.Groups.group;
 import static java.util.Arrays.asList;
 import static java.util.Locale.getDefault;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.core.IsNot.not;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.FACILITY;
 import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.WINDOW_NAMES;
 import static org.motechproject.ghana.national.domain.AlertWindow.ghanaNationalWindowNames;
 import static org.motechproject.ghana.national.domain.SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY;
 
 public class AggregationStrategyImpl implements AggregationStrategy {
 
     @Autowired
     private CMSLiteService cmsLiteService;
 
     @Autowired
     PatientService patientService;
 
     @Autowired
     AllFacilities allFacilities;
 
     public static final String SMS_SEPARATOR = "%0A";
 
     @Override
    @LoginAsAdmin
    @ApiSession
     public List<SMS> aggregate(List<SMSPayload> smsPayloadMessages) {
         List<SMS> aggregatedSMSPayload = new ArrayList<SMS>();
         List<SMSPayload> smsPayloadForFacility = filter(having(on(SMSPayload.class).getMessageRecipientType(), equalTo(MessageRecipientType.FACILITY)), smsPayloadMessages);
         List<SMSPayload> smsPayloadForPatient = filter(having(on(SMSPayload.class).getMessageRecipientType(), equalTo(MessageRecipientType.PATIENT)), smsPayloadMessages);
         if (!smsPayloadForFacility.isEmpty()) {
             aggregatedSMSPayload.addAll(processMessagesForFacility(smsPayloadForFacility));
         }
         if (!smsPayloadForPatient.isEmpty()) {
             aggregatedSMSPayload.addAll(processMessagesForPatient(smsPayloadForPatient));
         }
         return aggregatedSMSPayload;
     }
 
     private List<SMS> processMessagesForPatient(List<SMSPayload> smsPayloadForPatient) {
         StringBuilder builder = new StringBuilder();
         for (SMSPayload smsPayload : smsPayloadForPatient) {
             builder.append(smsPayload.getText()).append(SMS_SEPARATOR);
         }
         SMSPayload smsPayload = SMSPayload.fromText(builder.toString(), smsPayloadForPatient.get(0).getUniqueId(), DateUtil.now(), null, MessageRecipientType.PATIENT);
 
         return asList(new SMS(smsPayload, patientService.getPatientPhoneNumber(smsPayload.getUniqueId())));
     }
 
     private List<SMS> processMessagesForFacility(List<SMSPayload> smsPayloadMessages) {
         String standardMessage = SMSPayload.fill(getSMSTemplate(FACILITIES_DEFAULT_MESSAGE_KEY), new HashMap<String, String>() {{
             put(WINDOW_NAMES, join(AlertWindow.ghanaNationalWindowNames(), ", "));
             put(FACILITY, "");
         }});
         List<SMSPayload> filteredMessages = filter(having(on(SMSPayload.class).getText(), not(containsString(standardMessage))), smsPayloadMessages);
         List<SMSPayload> defaultMessagesList = filter(having(on(SMSPayload.class).getText(), containsString(standardMessage)), smsPayloadMessages);
         String facilityName = minus(defaultMessagesList.get(0).getText(), standardMessage);
         List<SMSPayload> smsPayloads = (filteredMessages.isEmpty()) ? defaultMessagesList : aggregateMessages(filteredMessages, facilityName);
         List<String> phoneNumbers = allFacilities.getFacilityByMotechId(smsPayloads.get(0).getUniqueId()).getPhoneNumbers();
 
         List<SMS> smsList = new ArrayList<SMS>();
         for (SMSPayload smsPayload : smsPayloads) {
             for (String phoneNumber : phoneNumbers) {
                 smsList.add(new SMS(smsPayload, phoneNumber));
             }
         }
         return smsList;
     }
 
     private String minus(String string1, String string2) {
         return string1.replace(string2, "").trim();
     }
 
     private List<SMSPayload> aggregateMessages(List<SMSPayload> smsPayloadMessages, final String facilityName) {
         final SMSPayload firstSMSPayload = Utility.nullSafe(smsPayloadMessages, 0, null);
         final String uniqueId = firstSMSPayload != null ? firstSMSPayload.getUniqueId() : null;
         ArrayList<SMSDatum> smsData = getSMSData(smsPayloadMessages);
         Comparator<String> alphabeticalOrder = new Comparator<String>() {
 
             @Override
             public int compare(String s, String s1) {
                 return s.compareTo(s1);
             }
         };
         Collection<String> motechIds = selectDistinct(extract(smsData, on(SMSDatum.class).getMotechId()), alphabeticalOrder);
         Group<SMSDatum> groupedByWindow = group(smsData, by(on(SMSDatum.class).getWindowName()),
                 by(on(SMSDatum.class).getMotechId()), by(on(SMSDatum.class).getMilestone()));
 
         Group<SMSDatum> motechIdSubGroup;
         Group<SMSDatum> subWindowGroup;
 
         final List<SMSPayload> messages = new ArrayList<SMSPayload>();
         final List<String> windowsWithoutSMS = new ArrayList<String>();
         for (String window : ghanaNationalWindowNames()) {
             StringBuilder builder = new StringBuilder();
             builder.append(window).append(": ");
             subWindowGroup = groupedByWindow.findGroup(window);
             int count = 0;
             if (subWindowGroup != null) {
                 for (String motechId : motechIds) {
                     if ((motechIdSubGroup = subWindowGroup.findGroup(motechId)) != null) {
                         List<SMSDatum> all = motechIdSubGroup.findAll();
                         count += 1;
                         SMSDatum datum = all.get(0);
                         if (count != 1) builder.append(", ");
                         builder.append(datum.getFirstName()).append(" ").append(datum.getLastName()).append(", ").append(datum.getMotechId())
                                 .append(", ").append(datum.getSerialNumber()).append(", ").append(joinFrom(all).getMilestone());
                     }
                 }
             }
             if (count != 0) {
                 messages.add(SMSPayload.fromText(builder.toString(), uniqueId, DateUtil.now(), null, MessageRecipientType.FACILITY));
             } else {
                 windowsWithoutSMS.add(window);
             }
         }
         messages.add(SMSPayload.fromTemplate(getSMSTemplate(SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY), new HashMap<String, String>() {{
             put(WINDOW_NAMES, join(windowsWithoutSMS, ", "));
             put(FACILITY, facilityName);
         }}, uniqueId, DateUtil.now(), null, MessageRecipientType.FACILITY));
         return messages;
     }
 
     private ArrayList<SMSDatum> getSMSData(List<SMSPayload> smsPayloadMessages) {
         List<String> smsList = collect(smsPayloadMessages, on(SMSPayload.class).getText());
 
         ArrayList<SMSDatum> smsData = new ArrayList<SMSDatum>();
         for (String sms : smsList) {
             String[] strings = sms.split(",");
             if (strings.length == 6) {
                 smsData.add(new SMSDatum(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]));
             }
         }
         return smsData;
     }
 
     private String getSMSTemplate(String templateKey) {
         try {
             return cmsLiteService.getStringContent(getDefault().getLanguage(), templateKey).getValue();
         } catch (ContentNotFoundException e) {
             throw new MotechException("Encountered exception while aggregating SMS, ", e);
         }
     }
 }
