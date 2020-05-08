 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.tripbrush.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionErrors;
 import uk.tripbrush.form.LoginForm;
 import uk.tripbrush.util.CommandConstant;
 import uk.tripbrush.util.Constant;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.util.MessageResources;
 import uk.tripbrush.model.core.Plan;
 import uk.tripbrush.model.core.User;
 import uk.tripbrush.service.LoginService;
 import uk.tripbrush.service.PlanService;
 import uk.tripbrush.service.UserService;
 import uk.tripbrush.util.StringUtil;
 import uk.tripbrush.view.MResult;
 
 /**
  *
  * @author Samir
  */
 public class LoginAction extends org.apache.struts.action.Action {
 
     @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response) throws Exception {
         LoginForm qform = (LoginForm)form;
 
         MessageResources messageResources = getResources(request);
         qform.setResources(messageResources);
         ActionErrors errors = qform.validate(mapping, request);
 
         if (CommandConstant.VALIDATE_PASSWORD.equals(qform.getCommand())) {
             if (true) {
                 request.setAttribute(Constant.REQUEST_MESSAGE,"OK");
                 request.getSession().setAttribute(Constant.SESSION_ACCESS,"AAA");
             }
             else {
                 request.setAttribute(Constant.REQUEST_MESSAGE,"NOTOK");
             }
         }
         else if (CommandConstant.CONFIRM_PASSWORD.equals(qform.getCommand())) {
             String keyPass = (String)request.getSession().getAttribute(Constant.SESSION_ACCESS);
             if (keyPass==null) {
                 return mapping.findForward("index");
             }
             else {
                 User user = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                 if (user==null) {
                     user = UserService.createTempUser();
                 }
                 Plan plan = PlanService.createNewPlan(qform.getDestination(),qform.getFromdate(),qform.getHowlong());
                 PlanService.createPlan(user, plan);
                 request.getSession().setAttribute(Constant.SESSION_PLAN,plan);
                 request.getSession().setAttribute(Constant.SESSION_USER,user);
                 return mapping.findForward("success");
             }
         }
         else if (CommandConstant.FB_LOGIN.equals(qform.getCommand())) {
             String code = qform.getCode();
             MResult result = LoginService.logInFacebook(qform.getName(),qform.getEmail(),code);
             if (result.getCode()==MResult.RESULT_OK) {
                 User user = (User)result.getObject();
                 if (user!=null) {
                     request.setAttribute(Constant.REQUEST_MESSAGE, user.getName());
                    PlanService.loadPlans(user,(Plan)request.getSession().getAttribute(Constant.SESSION_PLAN));
                     User suser = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                     if (suser!=null && suser.getStatus()==UserService.TEMP_USER) {
                         user.getPlans().addAll(UserService.deleteTempUser(suser,user));
                     }
                     request.getSession().setAttribute(Constant.SESSION_USER, user);
                 }
             }
         }
         else if (CommandConstant.FB_LOGOUT.equals(qform.getCommand())) {
             User user = (User)request.getSession().getAttribute(Constant.SESSION_USER);
             if (user!=null) {
                 LoginService.logOut(user);
             }
             request.getSession().setAttribute(Constant.SESSION_USER, null);
             request.getSession().setAttribute(Constant.SESSION_PLAN, null);               
         }
         else {
             if (errors.isEmpty()) {
                 if (CommandConstant.GET_USER.equals(qform.getCommand())) {
                     User user = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                     if (user!=null && user.getStatus()!=UserService.TEMP_USER) {
                         request.setAttribute(Constant.REQUEST_MESSAGE, user.getName());
                     }
                 }
                 else if (CommandConstant.LOGOUT_USER.equals(qform.getCommand())) {
                     User user = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                     if (user!=null) {
                         LoginService.logOut(user);
                     }
                     request.getSession().setAttribute(Constant.SESSION_USER, null);
                     request.getSession().setAttribute(Constant.SESSION_PLAN, null);                    
                 }
                 else if (CommandConstant.LOGIN_USER.equals(qform.getCommand())) {
                     String username = qform.getEmail();
                     String password = qform.getPassword();
 
                     MResult result = LoginService.logIn(username, password);
 
                     if (result.getCode()==MResult.RESULT_OK) {
                         User user = (User)result.getObject();
                         if (user!=null) {
                             if (user.getStatus()==UserService.FORGOT_PASSWORD) {
                                 errors.add(CommandConstant.ERROR, new ActionMessage("login.changepassword"));
                                 request.getSession().setAttribute(Constant.SESSION_USER, user);
                                 request.setAttribute(Constant.REQUEST_MESSAGE, "change");
                             }
                             else if (user.getStatus()==UserService.NEED_VERIFY) {
                                 errors.add(CommandConstant.ERROR, new ActionMessage("login.needveriy"));
                                 request.setAttribute(Constant.REQUEST_MESSAGE, "verify");
                             }
                             else {
                                 request.setAttribute(Constant.REQUEST_MESSAGE, user.getName());
                                PlanService.loadPlans(user,(Plan)request.getSession().getAttribute(Constant.SESSION_PLAN));
                                 User suser = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                                 if (suser!=null && suser.getStatus()==UserService.TEMP_USER) {
                                     user.getPlans().addAll(UserService.deleteTempUser(suser,user));
                                 }
                                 request.getSession().setAttribute(Constant.SESSION_USER, user);
                             }
                         }
                     }
                     else {
                         errors.add(CommandConstant.ERROR, new ActionMessage("login.error"));
                         qform.setPassword("");
                     }
                 }
                 else if (CommandConstant.FORGOT_USER.equals(qform.getCommand())) {
                     String email = qform.getEmail();
                     MResult result = UserService.resetUser(email);
                     if (result.getCode()==MResult.RESULT_OK) {
                         errors.add(CommandConstant.MESSAGE, new ActionMessage("forgot.ok"));
                     }
                     else {
                         errors.add(CommandConstant.ERROR, new ActionMessage("forgot.error"));
                     }
                 }
                 else if (CommandConstant.VERIFY_USER.equals(qform.getCommand())) {
                     String email = qform.getEmail();
                     String code = qform.getCode();
                     MResult result = UserService.verifyUser(email, code);
                     if (result.getCode()==MResult.RESULT_OK) {
                         request.getSession().setAttribute(Constant.SESSION_USER, result.getObject());
                         errors.add(CommandConstant.MESSAGE, new ActionMessage("verify.ok"));
                     }
                     else {
                         errors.add(CommandConstant.ERROR, new ActionMessage("verify.error"));
                     }
                 }
                 else if (CommandConstant.NEW_USER.equals(qform.getCommand())) {
                     String email = qform.getEmail();
                     String npassword = qform.getNewpassword();
                     User user = null;
                     User suser = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                     if (suser!=null && suser.getStatus()==UserService.TEMP_USER) {
                         user = new User(suser);
                     }
                     else {
                         user = new User();
                     }
                     if (StringUtil.isEmpty(qform.getName())) {
                         user.setName("User");
                     }
                     else {
                         user.setName(qform.getName());
                     }
                     user.setEmail(email);
                     user.setPassword(npassword);
                     MResult result = UserService.newUser(user);
                     if (result.getCode()==MResult.RESULT_OK) {
                         errors.add(CommandConstant.MESSAGE, new ActionMessage("newuser.ok"));
                     }
                     else {
                         errors.add(CommandConstant.ERROR, new ActionMessage("newuser.error"));
                     }
                 }
                 else if (CommandConstant.CHANGE_PASSWORD.equals(qform.getCommand())) {
                     User sessionDb = (User)request.getSession().getAttribute(Constant.SESSION_USER);
                     String password = qform.getPassword();
 
                     if (password.equals(qform.getNewpassword())) {
                         errors.add(CommandConstant.ERROR, new ActionMessage("changep.same"));
                     }
                     else {
                         MResult result = LoginService.logIn(sessionDb.getEmail(), password);
 
                         if (result.getCode()==MResult.RESULT_OK) {
                             User user = (User)result.getObject();
                             user.setPassword(qform.getNewpassword());
                             UserService.saveUser(user);
                             user.setStatus(UserService.NORMAL_USER);
                             errors.add(CommandConstant.MESSAGE, new ActionMessage("changep.ok"));
                         }
                         else {
                             errors.add(CommandConstant.ERROR, new ActionMessage("login.error"));
                         }
                     }
                 }
             }
         }
         if (!errors.isEmpty()) {
             this.saveMessages(request, errors);
         }
         return mapping.findForward(Constant.MAPPING_MESSAGE);        
     }
 }
