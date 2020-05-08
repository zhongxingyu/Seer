 package fr.cg95.cvq.service.request.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.request.IAutofillService;
 import fr.cg95.cvq.service.request.IAutofillTriggerService;
 import fr.cg95.cvq.service.users.IUserSearchService;
 
 /**
  * @author jsb@zenexity.fr
  *
  */
 public class AutofillService implements IAutofillService {
 
     private static IUserSearchService userSearchService;
 
     private enum TriggerType {
         SUBJECTID {
             @Override
             public IAutofillTriggerService getService() {
                 return userSearchService;
             }
         },
         REQUESTERID {
             @Override
             public IAutofillTriggerService getService() {
                 return userSearchService;
             }
         };
         public abstract IAutofillTriggerService getService();
     }
 
     public Map<String, String> getValues(String triggerName, Long id, Map<String, String> keys)
         throws CvqObjectNotFoundException {
         Object trigger = TriggerType.valueOf(triggerName.toUpperCase()).getService().getById(id);
         Object currentObject;
         Map<String, String> values = new HashMap<String, String>();
         for (Entry<String, String> listener : keys.entrySet()) {
             currentObject = trigger;
             for (String field : listener.getValue().split("\\.")) {
                 if (currentObject == null) {
                     break;
                 }
                 try {
                     currentObject = currentObject.getClass().getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length())).invoke(currentObject);
                 } catch (NoSuchMethodException e) {
                     currentObject = null;
                     break;
                 } catch (IllegalAccessException e) {
                     currentObject = null;
                     break;
                 } catch (InvocationTargetException e) {
                     currentObject = null;
                     break;
                 }
             }
            if (currentObject instanceof Date) {
                 Calendar cal = Calendar.getInstance();
                 cal.setTime((Date)currentObject);
                 values.put(listener.getKey() + ".day", String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                 values.put(listener.getKey() + ".month", String.valueOf(cal.get(Calendar.MONTH) + 1));
                 values.put(listener.getKey() + ".year", String.valueOf(cal.get(Calendar.YEAR)));
             } else {
                 values.put(listener.getKey(), currentObject != null ? currentObject.toString() : null);
             }
         }
         return values;
     }
 
     public void setUserSearchService(IUserSearchService userSearchService) {
         AutofillService.userSearchService = userSearchService;
     }
 
 }
