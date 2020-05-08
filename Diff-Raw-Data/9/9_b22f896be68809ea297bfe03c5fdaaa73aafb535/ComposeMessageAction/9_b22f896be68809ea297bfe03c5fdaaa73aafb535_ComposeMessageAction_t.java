 package controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import model.MessageDAO;
 import model.Model;
 import model.UserDAO;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.forms.FormBeanFactory;
 
 import databean.Message;
 import databean.User;
 import formbeans.MessageForm;
 
 public class ComposeMessageAction extends Action{
 
	private FormBeanFactory<MessageForm> formBeanFactory = FormBeanFactory.getInstance(MessageForm.class, "<>\"\''");
 	private MessageDAO messageDAO;
 	private UserDAO userDAO;
 	
 	public ComposeMessageAction(Model model) {
 		messageDAO = model.getMessageDAO();
 		userDAO = model.getUserDAO();
 	}
 	
 	@Override
 	public String getName() {
 		return "composeMessage.do";
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 		MessageForm form = formBeanFactory.create(request);
 		List<String> errors = new ArrayList<String>();
 		request.setAttribute("form", form);
 		request.setAttribute("errors", errors);
 		if (!form.isPresent()) {
 			return "message.jsp";
 		}
 		
 		errors.addAll(form.getValidationErrors());
 		if (errors.size() != 0) return "message.jsp";
 		
 		User sender = null, receiver = null;
 		try {
 			sender = userDAO.lookup(form.getSender());
 			if (sender == null)
 				errors.add("Cannot find user named " + form.getSender());
 			receiver = userDAO.lookup(form.getReceiver());
 			if (receiver == null)
 				errors.add("Cannot find user named " + form.getReceiver());
 		} catch(DAOException e) {
 			errors.add(e.getMessage());
 		}
 		if (errors.size() != 0) return "message.jsp";
 		
 		Message msg = new Message();
 		msg.setContent(form.getContent());
 		msg.setSender(sender);
 		msg.setReceiver(receiver);
 		msg.setTitle(form.getTitle());
 		msg.setSentDate(new Date());
 		try {
 			messageDAO.create(msg);
 		} catch (DAOException e) {
 			errors.add(e.getMessage());
 			return "message.jsp";
 		}
 		
 		request.setAttribute("success", "Your message has been sent");
 		return "showMessage.do";
 			
 	}
 
 
 
 }
