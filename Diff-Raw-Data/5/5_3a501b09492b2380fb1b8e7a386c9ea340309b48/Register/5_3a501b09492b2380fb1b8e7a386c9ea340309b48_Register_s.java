 /*
  * The MIT License
  *
  * Copyright (c) 2013, benas (md.benhassine@gmail.com)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.benas.todolist.web.tapestry.pages.user;
 
 import net.benas.todolist.core.domain.User;
 import net.benas.todolist.core.service.api.UserService;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SessionState;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 import java.util.List;
 
 /**
  * @author benas (md.benhassine@gmail.com)
  */
 @SuppressWarnings("unused")
 public class Register {
 
     @SessionState
     private User loggedUser;
 
     @Inject
     private UserService userService;
     
     @Inject
     private Messages messages;
 
 	@Property
 	private String firstname;
 	
 	@Property
 	private String lastname;
 	
 	@Property
 	private String email;
 	
 	@Property
 	private String password;
 	
 	@Property
 	private String confirmationPassword;
 
     @Property
     private String error;
 
     @Property
 	@InjectComponent
 	private Form registerForm;
 	
 	@OnEvent(value=EventConstants.VALIDATE,component="registerForm")
 	public void validateForm() {
         User user = userService.getUserByEmail(email);
         if (user != null)
            registerForm.recordError(messages.format("register.email.error", email));
         if (!password.equals(confirmationPassword)) {
            registerForm.recordError(messages.get("register.password.error"));
         }
     }
 	
 	@OnEvent(value=EventConstants.SUCCESS,component="registerForm")
 	public Object onRegisterSuccess() {
         User user = new User(firstname, lastname, email, password);
         userService.create(user);
         loggedUser = userService.getUserByEmail(email);
         return Home.class;
     }
 
 }
