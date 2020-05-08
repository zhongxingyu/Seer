 /**
  * 
  */
 package ch.zhaw.mdp.lhb.citr.rest.impl;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import ch.zhaw.mdp.lhb.citr.jpa.entity.MessageDVO;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Component;
 
 import ch.zhaw.mdp.lhb.citr.dto.MessageDTO;
 import ch.zhaw.mdp.lhb.citr.jpa.service.IDBMessageService;
 import ch.zhaw.mdp.lhb.citr.rest.IRMessageServices;
 
import java.util.Calendar;
import java.util.Date;

 /**
  * @author Daniel Brun
  * @author Simon Lang
  *
  * Implementation of the Service-Interface {@link IRMessageServices}.
  */
 @Component
 @Path("/message")
 @Scope("request")
 public class MessageServiceRestImpl implements IRMessageServices {
 
 	@Autowired
 	private IDBMessageService messageService;
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.mdp.lhb.citr.rest.IRMessageServices#sendMessage(ch.zhaw.mdp.lhb.citr.dto.MessageDTO)
 	 */
 	@POST
 	@Override
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Secured("ROLE_USER")
 	@Path("/create")
 	public long createMessage(MessageDTO messageDTO) {
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		MessageDVO messageDVO = new MessageDVO();
 		messageDVO.setMessage(messageDTO.getMessageText());
 		messageDVO.setGroupId(messageDTO.getGroupId());
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		messageDVO.setSendDate(today.getTime());
 		messageService.save(messageDVO);
 		return messageDVO.getId();
 	}
 }
