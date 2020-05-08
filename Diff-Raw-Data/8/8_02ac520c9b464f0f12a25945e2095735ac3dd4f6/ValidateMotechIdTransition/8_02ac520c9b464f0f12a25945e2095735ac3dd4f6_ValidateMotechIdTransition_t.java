 package org.motechproject.ghana.national.domain.ivr;
 
 import ch.lambdaj.Lambda;
 import org.apache.commons.collections.CollectionUtils;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.motechproject.decisiontree.model.*;
 import org.motechproject.ghana.national.domain.AlertType;
 import org.motechproject.ghana.national.domain.IVRClipManager;
 import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
 import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
 import org.motechproject.ghana.national.repository.AllPatientsOutbox;
 import org.motechproject.ghana.national.repository.AllSpringBeans;
 import org.motechproject.ghana.national.service.ExecuteAsOpenMRSAdmin;
 import org.motechproject.ghana.national.service.MobileMidwifeService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.outbox.api.domain.OutboundVoiceMessage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static org.hamcrest.Matchers.hasEntry;
 import static org.hamcrest.Matchers.not;
 import static org.motechproject.ghana.national.domain.ivr.AudioPrompts.INVALID_MOTECH_ID_PROMPT;
 import static org.motechproject.ghana.national.domain.ivr.AudioPrompts.NO_MESSAGE_IN_OUTBOX;
 import static org.motechproject.ghana.national.domain.mobilemidwife.Language.valueOf;
 import static org.motechproject.ghana.national.repository.AllPatientsOutbox.AUDIO_CLIP_NAME;
 import static org.motechproject.ghana.national.repository.AllPatientsOutbox.TYPE;
 import static org.springframework.util.CollectionUtils.isEmpty;
 
 public class ValidateMotechIdTransition extends Transition {
 
     @JsonProperty
     String language;
 
     // Required for Ektorp
     public ValidateMotechIdTransition() {
     }
 
     public ValidateMotechIdTransition(String language) {
         this.language = language;
     }
 
     @Override
     public Node getDestinationNode(String input) {
         if (!isValidMotechId(input)) {
             return invalidMotechIdTransition();
         } else if (hasValidMobileMidwifeVoiceRegistration(input)) {
             return playAudioClipsFromOutbox(input);
         }
         return null;
 
 //        else {
 //            return ∂invalidMotechIdTransition(INVALID_MOTECH_ID_PROMPT);
 //        }
 
     }
 
     private Node playAudioClipsFromOutbox(String motechId) {
         AllPatientsOutbox allPatientsOutbox = (AllPatientsOutbox) AllSpringBeans.applicationContext.getBean("allPatientsOutbox");
         IVRClipManager ivrClipManager = (IVRClipManager) AllSpringBeans.applicationContext.getBean("ivrClipManager");
         List<OutboundVoiceMessage> audioClips = allPatientsOutbox.getAudioFileNames(motechId);
         List<OutboundVoiceMessage> scheduleClips = Lambda.filter(having(on(OutboundVoiceMessage.class).getParameters(), not(hasEntry(TYPE, AlertType.MOBILE_MIDWIFE.name()))), audioClips);
         List<OutboundVoiceMessage> mmClips = Lambda.filter(having(on(OutboundVoiceMessage.class).getParameters(), hasEntry(TYPE, AlertType.MOBILE_MIDWIFE.name())), audioClips);
 
         Node node = new Node();
         List<String> scheduleClipNames = getClipNames(scheduleClips);
         List<String> mmClipNames = getClipNames(mmClips);
         if (isEmpty(scheduleClipNames))
             return node.addPrompts(new AudioPrompt().setAudioFileUrl(ivrClipManager.urlFor(NO_MESSAGE_IN_OUTBOX.value(), valueOf(language))));
 
         for (String scheduleClipName : scheduleClipNames) {
             node.addPrompts(new AudioPrompt().setAudioFileUrl(ivrClipManager.urlFor(scheduleClipName, valueOf(language))));
         }
 
         for (String mmClipName : mmClipNames) {
             MobileMidwifeAudioClips mobileMidwifeAudioClips = MobileMidwifeAudioClips.valueOf(mmClipName);
             playMultipleMMClips(mobileMidwifeAudioClips.getClipNames(), mobileMidwifeAudioClips.getPromptClipNames(), node, null, ivrClipManager);
         }
         return node;
     }
 
     private void playMultipleMMClips(List<String> pendingClips, List<String> pendingPrompts, Node node, Node previousNode, IVRClipManager ivrClipManager) {
 
         if (CollectionUtils.isEmpty(pendingClips))
             return;
 
         Node rootNode = new Node();
 
         Map<String, ITransition> transitions = new HashMap<String, ITransition>();
         transitions.put("1", new Transition().setDestinationNode(rootNode));
 
         for (int i = 1; i < pendingClips.size(); i++) {
             Node transitionNode = new Node();
             playMultipleMMClips(pendingClips.subList(i, pendingClips.size()), pendingPrompts.subList(i, pendingPrompts.size()), transitionNode, node, ivrClipManager);
             transitions.put(i + 1 + "", new Transition().setDestinationNode(transitionNode));
         }
         if (pendingClips.size() == 1 && previousNode != null) {
             transitions.put("2", new Transition().setDestinationNode(previousNode));
         }
 
         rootNode.addPrompts(new AudioPrompt().setAudioFileUrl(ivrClipManager.urlFor(pendingClips.get(0), valueOf(language))))
                 .addPrompts(new AudioPrompt().setAudioFileUrl(ivrClipManager.urlFor(pendingPrompts.get(0), valueOf(language))))
                 .setTransitions(transitions);
 
         for (Prompt prompt : rootNode.getPrompts()) {
             node.addPrompts(prompt);
         }
         node.setTransitions(transitions);
     }
 
     private List<String> getClipNames(List<OutboundVoiceMessage> clips) {
         List<String> clipNames = new ArrayList<String>();
         for (OutboundVoiceMessage clip : clips) {
             clipNames.add((String) clip.getParameters().get(AUDIO_CLIP_NAME));
         }
         return clipNames;
     }
 
     private boolean isValidMotechId(final String input) {
         ExecuteAsOpenMRSAdmin executeAsOpenMRSAdmin = (ExecuteAsOpenMRSAdmin) AllSpringBeans.applicationContext.getBean("executeAsOpenMRSAdmin");
 
         return executeAsOpenMRSAdmin.execute(new ExecuteAsOpenMRSAdmin.OpenMRSServiceCall() {
             @Override
             public Object invoke() {
                 PatientService patientService = (PatientService) AllSpringBeans.applicationContext.getBean("patientService");
                 return patientService.getPatientByMotechId(trimInputForTrailingHash(input)) != null;
             }
         });
     }
 
     private Node invalidMotechIdTransition() {
         return new Node()
                 .addPrompts(new AudioPrompt().setAudioFileUrl(((IVRClipManager) AllSpringBeans.applicationContext.getBean("ivrClipManager")).urlFor(INVALID_MOTECH_ID_PROMPT.value(), valueOf(language))))
                 .addTransition("?", new ValidateMotechIdTransition(language));
     }
 
     private String trimInputForTrailingHash(String input) {
         return input.replaceAll("#", "");
     }
 
     private boolean hasValidMobileMidwifeVoiceRegistration(String input) {
         MobileMidwifeService mobileMidwifeService = (MobileMidwifeService) AllSpringBeans.applicationContext.getBean("mobileMidwifeService");
         MobileMidwifeEnrollment midwifeEnrollment = mobileMidwifeService.findActiveBy(trimInputForTrailingHash(input));
         return midwifeEnrollment != null && midwifeEnrollment.getMedium().equals(Medium.VOICE);
     }
 }
