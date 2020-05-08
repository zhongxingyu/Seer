 package com.seventysevenagency.chat.mvc.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.seventysevenagency.chat.dao.UserDAO;
 import com.seventysevenagency.chat.dao.hibernate.UserHibernateDAOImpl;
 import com.seventysevenagency.chat.mvc.models.IModel;
 import com.seventysevenagency.chat.mvc.models.RegisterModel;
 
@Service
 public class RegisterController extends ControllerBase {
	@Autowired
	private UserDAO userDAO;
 	public void execute(IModel model, HttpServletRequest request) {
 		this.redirectUrl = null;
 		RegisterModel registerModel = (RegisterModel) model;
 		String requestMethod = request.getMethod();
 
 		if (requestMethod.equals("POST")) {
 			// validation new user data and saving to db
 			if (validateModel(registerModel)) {
				userDAO = applicationContext.getBean(UserDAO.class);
 
				userDAO.create(registerModel.user);
				this.redirectUrl = "login?msg=success";
 
 			}
 		} else {
 			// Get request
 		}
 	}
 
 	public boolean validateModel(RegisterModel model) {
 
 		boolean isValid = true;
 
 		String username = model.user.getUsername();
 		if (username == null || username.isEmpty()) {
 			model.addWarning("error", "Username is empty");
 			isValid = false;
 		}
 
 		String password = model.user.getPassword();
 		if (password == null || password.isEmpty()) {
 			model.addWarning("error", "Password is empty");
 			isValid = false;
 		} else if (password.length() < 4) {
 			model.addWarning("error", "Password is too short");
 			isValid = false;
 		}
 
 		String name = model.user.getName();
 		if (name == null || name.isEmpty()) {
 			model.addWarning("error", "Please, provide your name...");
 			isValid = false;
 		}
 		return isValid;
 	}
 }
