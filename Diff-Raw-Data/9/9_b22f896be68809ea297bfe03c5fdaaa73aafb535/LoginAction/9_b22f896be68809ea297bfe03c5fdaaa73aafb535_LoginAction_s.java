 package controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import model.Model;
 import model.UserDAO;
 import model.ItemDAO;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.forms.FormBeanFactory;
 
 import databean.User;
 
 import formbeans.LoginForm;
 
 public class LoginAction extends Action{
 
 	private FormBeanFactory<LoginForm> formBeanFactory = FormBeanFactory.getInstance(LoginForm.class,"<>\"");
 
 	private UserDAO userDAO;
 	private ItemDAO itemDAO;
 	
 	public LoginAction(Model model) {
 		// TODO Auto-generated constructor stub
 		userDAO = model.getUserDAO();
 		itemDAO = model.getItemDAO();
 	}
 
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "login.do";
 	}
 
 	@Override
 	public String perform(HttpServletRequest request) {
 		
 		LoginForm form = formBeanFactory.create(request);
     	
         List<String> errors = new ArrayList<String>();
         
         request.setAttribute("errors",errors);
         request.setAttribute("form",form);
         
         try {
 			request.setAttribute("allItemList", itemDAO.getAllItems());
 		} catch (DAOException e) {
 			// TODO Auto-generated catch block
 			errors.add(e.getMessage());
         	return "index.jsp";
 		}

         if (!form.isPresent()) {
             return "index.jsp";
         }
 
         errors.addAll(form.getValidationErrors());
         if (errors.size() != 0) {
             return "index.jsp";
         }
 
         User user;
         try {
         	user = userDAO.lookup(form.getUserName());
         } catch (DAOException e) {
         	errors.add(e.getMessage());
         	return "index.jsp";
         }
         
         if (user == null) {
             errors.add("Username not found");
             return "index.jsp";
         }
 
         if (!user.checkPassword(form.getPassword())) {
             errors.add("Incorrect password");
             return "index.jsp";
         }
 
         HttpSession session = request.getSession();
         session.setAttribute("user",user);
         
         
         request.setAttribute("success", "Welcome back! " + user.getUserName());
         return "index.jsp";
         
 	}
 
 
 }
