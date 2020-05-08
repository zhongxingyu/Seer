 package org.motechproject.ghana.national.domain.ivr;
 
 import org.motechproject.ghana.national.domain.AlertWindow;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 
 public enum AudioPrompts {
     LANGUAGE_PROMPT("prompt_A", null, null),
     REASON_FOR_CALL_PROMPT("prompt_B", null, null),
     MOTECH_ID_PROMPT("prompt_C", null, null),
     INVALID_MOTECH_ID_PROMPT("prompt_F", null, null),
     NO_MESSAGE_IN_OUTBOX("prompt_H", null, null),
     GHS("GHS", null, null),
 
     ANC_DUE("prompt_IR1", AlertWindow.DUE, asList(ANC_DELIVERY.getName())),
     TT_DUE("prompt_IR2", AlertWindow.DUE, asList(TT_VACCINATION.getName())),
     IPT_DUE("prompt_IR3", AlertWindow.DUE, asList(ANC_IPT_VACCINE.getName())),
     PNC_MOTHER_DUE("prompt_IR4", AlertWindow.DUE, asList(PNC_MOTHER_1.getName(), PNC_MOTHER_2.getName(), PNC_MOTHER_3.getName())),
     PNC_CHILD_DUE("prompt_IR5", AlertWindow.DUE, asList(PNC_CHILD_1.getName(), PNC_CHILD_2.getName(), PNC_CHILD_3.getName())),
    OTHER_VACCINE_DUE("prompt_IR6", AlertWindow.DUE, asList(CWC_BCG.getName(), CWC_MEASLES_VACCINE.getName(), CWC_OPV_0.getName(), CWC_OPV_OTHERS.getName(), CWC_PENTA.getName(), CWC_ROTAVIRUS.getName(), CWC_YELLOW_FEVER.getName())),
     IPTi_DUE("prompt_IR7", AlertWindow.DUE, asList(CWC_IPT_VACCINE.getName())),
 
     ANC_LATE("prompt_IO1", AlertWindow.OVERDUE, asList(ANC_DELIVERY.getName())),
     TT_LATE("prompt_IO2", AlertWindow.OVERDUE, asList(TT_VACCINATION.getName())),
     IPT_LATE("prompt_IO3", AlertWindow.OVERDUE, asList(ANC_IPT_VACCINE.getName())),
     PNC_MOTHER_LATE("prompt_IO4", AlertWindow.OVERDUE, asList(PNC_MOTHER_1.getName(), PNC_MOTHER_2.getName(), PNC_MOTHER_3.getName())),
     PNC_CHILD_LATE("prompt_IO5", AlertWindow.OVERDUE, asList(PNC_CHILD_1.getName(), PNC_CHILD_2.getName(), PNC_CHILD_3.getName())),
    OTHER_VACCINE_LATE("prompt_IO6", AlertWindow.OVERDUE, asList(CWC_BCG.getName(), CWC_MEASLES_VACCINE.getName(), CWC_OPV_0.getName(), CWC_OPV_OTHERS.getName(), CWC_PENTA.getName(), CWC_ROTAVIRUS.getName(), CWC_YELLOW_FEVER.getName())),
     IPTi_LATE("prompt_IO7", AlertWindow.OVERDUE, asList(CWC_IPT_VACCINE.getName()));
 
     private String message;
     private AlertWindow window;
     private List<String> scheduleNames;
 
     AudioPrompts(String message, AlertWindow window, List<String> scheduleNames) {
         this.message = message;
         this.window = window;
         this.scheduleNames = scheduleNames;
     }
 
     public String value() {
         return message;
     }
 
     public static String fileNameForCareSchedule(String scheduleName, AlertWindow alertWindow) {
         for (AudioPrompts prompt : values()) {
             if (alertWindow.equals(prompt.window) && prompt.scheduleNames.contains(scheduleName))
                 return prompt.message;
         }
         return null;
     }
 
 }
