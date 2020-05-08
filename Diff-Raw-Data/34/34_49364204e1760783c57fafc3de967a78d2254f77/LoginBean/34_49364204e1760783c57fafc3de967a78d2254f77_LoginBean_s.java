 
 package gov.nih.nci.evs.reportwriter.bean;
 
 import gov.nih.nci.evs.reportwriter.utils.DataUtils;
 import gov.nih.nci.security.AuthenticationManager;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.SecurityServiceProvider;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.User;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.naming.InitialContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import gov.nih.nci.evs.reportwriter.utils.*;
 
 /**
   * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2009 NGIT. This software was developed in conjunction with the National Cancer Institute,
 * and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 * "This product includes software developed by NGIT and the National Cancer Institute."
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
 * wherever such third-party acknowledgments normally appear.
 * 3. The names "The National Cancer Institute", "NCI" and "NGIT" must not be used to endorse or promote products derived from this software.
 * 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
 * the recipient to use any trademarks owned by either NCI or NGIT
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 * NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   * <!-- LICENSE_TEXT_END -->
   */
 
 /**
   * @author EVS Team
   * @version 1.0
   *
   * Modification history
   *     Initial implementation kim.ong@ngc.com
   *
  */
 
 public class LoginBean extends Object
 {
 
   private static Logger logger = Logger.getLogger(LoginBean.class);
 
   String userid;
   String password;
 
   long roleGroupId;
   String selectedTask = null;
   Boolean isAdmin = null;
 
   private InitialContext ctx = null;
 
 
   public void setSelectedTask(String selectedTask)
   {
     this.selectedTask = selectedTask;;
   }
 
   public String getUserid()
   {
     return userid;
   }
 
   public void setUserid(String newUserid)
   {
     this.userid = newUserid;
   }
 
   public String getPassword()
   {
     return this.password;
   }
 
   public void setPassword(String newPassword)
   {
     this.password = newPassword;
   }
 
   public long getRoleGroupId()
   {
 	  return this.roleGroupId;
   }
 
   public void setRoleGroupId(long roleGroupId)
   {
     this.roleGroupId = roleGroupId;
   }
 
   public Boolean hasAdminPrivilege() {
 
 	  long rid = -1;
 	  AuthorizationManager ami = null;
 
 	  try {
 		  ami = SecurityServiceProvider.getAuthorizationManager("ncireportwriter");
 		  if (ami == null) {
 				throw new Exception();
 		  }
 		  ami = (AuthorizationManager) ami;
 
 		  User user = ami.getUser(userid);
 		  //KLO_log.warn("LoginBean: User ID: " + user.getUserId() + " User paswd: " + user.getPassword());
 
 		  Set<Group> groups = ami.getGroups(user.getUserId().toString());
 
 		  if(null != groups) {
 			  //KLO_log.warn("LoginBean findRoleGroupId: Groups set is not null. User is part of : " + groups.size() + " groups");
 			  Iterator iter = groups.iterator();
 			  while(iter.hasNext()) {
 				    Group grp = (Group) iter.next();
 				    if(null != grp) {
 				    	rid = grp.getGroupId();
 				    	if(grp.getGroupName().startsWith("admin")) {
 				    		//KLO_log.warn("LoginBean: User is associated with an admin group");
 				    		return Boolean.TRUE;
 				    	}
 				    }
 			  }
 		  }
 	  }
 	  catch(Exception e) {
 		  e.printStackTrace();
 	  }
 
 	  return Boolean.FALSE;
   }
 
 
   public List getTaskList()
   {
 	  //KLO_log.warn("LoginBean: isAdmin: " + isAdmin);
 	  return DataUtils.getTaskList(isAdmin);
   }
 
 
   public gov.nih.nci.evs.reportwriter.bean.User getUser(String loginName) {
 	  try{
 		  SDKClientUtil sdkclientutil = new SDKClientUtil();
 		  String FQName = "gov.nih.nci.evs.reportwriter.bean.User";
 		  String methodName = "setLoginName";
 		  Object obj = sdkclientutil.search(FQName, methodName, loginName);
 		  if (obj == null) return null;
 		  gov.nih.nci.evs.reportwriter.bean.User user = (gov.nih.nci.evs.reportwriter.bean.User) obj;
 		  return user;
 	  } catch(Exception e) {
 		  //e.printStackTrace();
 	  }
 	  return null;
   }
 
 
   public String loginAction()
   {
 		String applicationName = "ncireportwriter";
 		isAdmin = false;
 
 		//Get the user credentials from the database and login
 		try {
			AuthenticationManager authenticationManager = SecurityServiceProvider.getAuthenticationManager(applicationName);
 			boolean loginOK = authenticationManager.login(userid, password);
 			if (loginOK)
 			{
 				FacesContext context = FacesContext.getCurrentInstance();
 			    HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
 			    HttpSession session = request.getSession(); // true
 			    if (session != null) {
 					 session.setAttribute("uid", userid);
 					 //session.setAttribute("password", password);
 				}
 				isAdmin = hasAdminPrivilege();
 				session.setAttribute("isAdmin", isAdmin);
 
                 gov.nih.nci.evs.reportwriter.bean.User user = getUser(userid);
                 if (user == null)
                 {
                      // Synchronize with CSM User table
 					 try{
 						  SDKClientUtil sdkclientutil = new SDKClientUtil();
 						  gov.nih.nci.evs.reportwriter.bean.User newuser = (gov.nih.nci.evs.reportwriter.bean.User) sdkclientutil.createUser(userid);
 						  sdkclientutil.insertUser(newuser);
 
 					 } catch(Exception e) {
 						  e.printStackTrace();
 					 }
 				}
 				/*
 				else
 				{
 					KLO_log.warn("User object found -- " + userid);
 					System.out.println("User object found -- " + userid);
 				}
 				*/
                 
                 session.setAttribute("isSessionValid", Boolean.TRUE);
 				return "success";
 			}
 			else
 			{
 				return "failure";
 			}
 		}
		catch (Exception cse) {
 			System.out.println(">>>>>>>>>>>>> ERROR IN LOGIN by <<<<<<<<< " + userid);
 		}
 
 		return "failure";
 	  }
 
 
 	   public String logoutAction() {
 		  SessionUtil.getSession().invalidate();
 		  return "logout";
 	   }
 
 
 
 	  public void changeTaskSelection(ValueChangeEvent vce) {
 		 String newValue = (String)vce.getNewValue();
 		 this.selectedTask = newValue;
 	  }
 
 	  public Object getService(String serviceBeanName) throws javax.naming.NamingException {
 		return ctx.lookup(serviceBeanName);
 	  }
 
       public String logout() {
 		 if (SessionUtil.getSession() != null)
 		 {
          	SessionUtil.getSession().invalidate();
 		 }
          return "logout";
       }
 
   }
