 package scripts.before_add_message.itsm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import scripts.itsm.CommonITSM;
 import scripts.itsm.assignee.PeekAssigneeStrategy;
 import scripts.itsm.assignee.RandomAssignee;
 
 import com.trackstudio.app.adapter.AdapterManager;
 import com.trackstudio.exception.GranException;
 import com.trackstudio.external.OperationTrigger;
 import com.trackstudio.secured.SecuredMessageTriggerBean;
 import com.trackstudio.secured.SecuredPrstatusBean;
 import com.trackstudio.secured.SecuredTaskBean;
 import com.trackstudio.secured.SecuredTaskTriggerBean;
 import com.trackstudio.secured.SecuredUDFBean;
 import com.trackstudio.secured.SecuredUDFValueBean;
 import com.trackstudio.secured.SecuredUserBean;
 
 
 public class CreateRFC extends CommonITSM implements OperationTrigger {
     public SecuredMessageTriggerBean execute(SecuredMessageTriggerBean message) throws GranException {
        String txt = message.getDescription();
        
         SecuredTaskBean task = message.getTask();
         SecuredUDFBean relatedUdf = AdapterManager.getInstance().getSecuredFindAdapterManager().findUDFById(task.getSecure(), PROBLEM_RFC_UDFID);
        
             HashMap<String, String> udfMap = new HashMap<String, String>();
 
             SecuredUDFValueBean udf = task.getUDFValues().get(PROBLEM_PRODUCT_UDFID);
             if (udf!=null){
                 Object value = udf.getValue();
                 List<SecuredTaskBean> productsInvolved = null;
                 if (value != null) {
                 productsInvolved = (List<SecuredTaskBean>) value;
                     String products = "";
                     for (SecuredTaskBean p: productsInvolved){
                         products+=";#"+p.getNumber();
                     }
                      if (products.length()>0) {
                          SecuredUDFBean sUdf = AdapterManager.getInstance().getSecuredFindAdapterManager().findUDFById(task.getSecure(), RFC_PRODUCT_UDFID);
                          udfMap.put(sUdf.getCaption(), products.substring(1));
                      }
             }
             }
             ArrayList<SecuredUserBean> possibleHandlers = AdapterManager.getInstance().getSecuredStepAdapterManager().getTaskEditHandlerList(message.getSecure(), RFC_ROOT_ID, RFC_CATEGORY_ID, true);
              ArrayList<SecuredPrstatusBean> possibleGroupHandlers = AdapterManager.getInstance().getSecuredStepAdapterManager().getTaskEditGroupHandlerList(message.getSecure(), RFC_ROOT_ID, RFC_CATEGORY_ID, true);
             String handlerUser = null;
             String handlerGroup = null;
 
             if (possibleHandlers!=null && !possibleHandlers.isEmpty())
             {
                 PeekAssigneeStrategy choosen = new RandomAssignee(possibleHandlers);
                 Object peek = choosen.peek();
                 if (peek!=null)
                 handlerUser = ((SecuredUserBean)peek).getId();
             } else if (possibleGroupHandlers!=null && !possibleGroupHandlers.isEmpty()){
                 PeekAssigneeStrategy choosen = new RandomAssignee(possibleGroupHandlers);
                 Object peek = choosen.peek();
                 if (peek!=null)
                 handlerGroup = ((SecuredPrstatusBean)peek).getId();
             }
             SecuredTaskTriggerBean rfc = new SecuredTaskTriggerBean(RFC_ROOT_ID, txt,message.getTask().getName(),
 null, null, null, null, null, null,
 task.getDeadline(), null, message.getSubmitterId(), null,
 handlerUser, handlerGroup, RFC_ROOT_ID, RFC_CATEGORY_ID,
 null, null, null,  null,  udfMap, message.getSecure()).create();
             message.setUdfValue(relatedUdf.getCaption(), "#" +rfc.getNumber());
        
 
 
         return message;
 }
 }
