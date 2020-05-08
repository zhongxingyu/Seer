 package org.motechproject.ghana.national.repository;
 
 import ch.lambdaj.group.Group;
 import org.motechproject.MotechException;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.domain.AlertWindow;
 import org.motechproject.ghana.national.domain.SmsTemplateKeys;
 import org.motechproject.ghana.national.messagegateway.domain.AggregationStrategy;
 import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
 import org.motechproject.ghana.national.messagegateway.domain.SMS;
 import org.motechproject.ghana.national.messagegateway.domain.SMSDatum;
 import org.motechproject.ghana.national.tools.Utility;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.collect;
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.filter;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.join;
 import static ch.lambdaj.Lambda.joinFrom;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.selectDistinct;
 import static ch.lambdaj.group.Groups.by;
 import static ch.lambdaj.group.Groups.group;
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
     public static final String SMS_SEPARATOR = "%0A";
 
     @Override
     public List<SMS> aggregate(List<SMS> smsMessages) {
         List<SMS> aggregatedSMS = new ArrayList<SMS>();
         List<SMS> smsForFacility = filter(having(on(SMS.class).getMessageRecipientType(), equalTo(MessageRecipientType.FACILITY)), smsMessages);
         List<SMS> smsForPatient = filter(having(on(SMS.class).getMessageRecipientType(), equalTo(MessageRecipientType.PATIENT)), smsMessages);
         if (!smsForFacility.isEmpty()) {
             aggregatedSMS.addAll(processMessagesForFacility(smsForFacility));
         }
         if (!smsForPatient.isEmpty()) {
             aggregatedSMS.addAll(processMessagesForPatient(smsForPatient));
         }
         return aggregatedSMS;
     }
 
     private List<SMS> processMessagesForPatient(List<SMS> smsForPatient) {
         StringBuilder builder = new StringBuilder();
         for (SMS sms : smsForPatient) {
             builder.append(sms.getText()).append(SMS_SEPARATOR);
         }
         return Arrays.asList(SMS.fromText(builder.toString(), smsForPatient.get(0).getPhoneNumber(), DateUtil.now(), null, MessageRecipientType.PATIENT));
     }
 
     private List<SMS> processMessagesForFacility(List<SMS> smsMessages) {
        String standardMessage = SMS.fill(getSMSTemplate(FACILITIES_DEFAULT_MESSAGE_KEY), new HashMap<String, String>() {{
             put(WINDOW_NAMES, join(AlertWindow.ghanaNationalWindowNames(), ", "));
             put(FACILITY, "");
         }});
        List<SMS> filteredMessages = filter(having(on(SMS.class).getText(), not(containsString(standardMessage))), smsMessages);
        List<SMS> defaultMessagesList = filter(having(on(SMS.class).getText(), containsString(standardMessage)), smsMessages);
        String facilityName = minus(defaultMessagesList.get(0).getText(), standardMessage);
        return (filteredMessages.isEmpty()) ? defaultMessagesList : aggregateMessages(filteredMessages, facilityName);
     }
 
    private String minus(String string1, String string2) {
        return string1.replace(string2, "").trim();
    }

    private List<SMS> aggregateMessages(List<SMS> smsMessages, final String facilityName) {
         final SMS firstSMS = Utility.nullSafe(smsMessages, 0, null);
         final String phoneNumber = firstSMS != null ? firstSMS.getPhoneNumber() : null;
         ArrayList<SMSDatum> smsData = getSMSData(smsMessages);
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
 
         final List<SMS> messages = new ArrayList<SMS>();
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
                 messages.add(SMS.fromText(builder.toString(), phoneNumber, DateUtil.now(), null, MessageRecipientType.FACILITY));
             } else {
                 windowsWithoutSMS.add(window);
             }
         }
         messages.add(SMS.fromTemplate(getSMSTemplate(SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY), new HashMap<String, String>() {{
             put(WINDOW_NAMES, join(windowsWithoutSMS, ", "));
            put(FACILITY, facilityName);
         }}, phoneNumber, DateUtil.now(), null, MessageRecipientType.FACILITY));
         return messages;
     }
 
     private ArrayList<SMSDatum> getSMSData(List<SMS> smsMessages) {
         List<String> smsList = collect(smsMessages, on(SMS.class).getText());
 
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
