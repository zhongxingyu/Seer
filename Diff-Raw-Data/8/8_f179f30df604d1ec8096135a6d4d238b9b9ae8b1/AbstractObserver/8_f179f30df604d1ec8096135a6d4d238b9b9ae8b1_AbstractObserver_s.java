 package utilits.aspect.observer;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 import utilits.aspect.ActionType;
 import utilits.entity.Action;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Calendar;
 
 /**
  * Here will be javadoc
  *
  * @author karlovsky
  * @since 1.0
  */
 public abstract class AbstractObserver implements IObserver {
 
     protected final ActionType actionType;
 
     public AbstractObserver(ActionType actionType) {
         this.actionType = actionType;
     }
 
    protected Action buildAction() {
         HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
         Action entity = new utilits.entity.Action();
         entity.setIp(request.getRemoteAddr());
         entity.setHost(request.getRemoteHost());
         entity.setType(actionType);
         entity.setUserAgent(request.getHeader("user-agent"));
         StringBuffer requestURL = request.getRequestURL();
         String queryParams = request.getQueryString();
         if (StringUtils.isNotEmpty(queryParams)) {
            requestURL.append("?").append(queryParams);
         }
         entity.setRequestURL(requestURL.toString());
         entity.setActionTimestamp(Calendar.getInstance().getTime());
         return entity;
     }
 
 }
