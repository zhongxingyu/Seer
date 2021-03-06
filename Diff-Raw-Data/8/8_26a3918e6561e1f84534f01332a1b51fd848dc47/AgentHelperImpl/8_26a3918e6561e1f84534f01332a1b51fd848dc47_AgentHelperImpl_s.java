 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
  *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
  *
  * Licensed under the Educational Community License Version 1.0 (the "License");
  * By obtaining, using and/or copying this Original Work, you agree that you have read,
  * understand, and will comply with the terms and conditions of the Educational Community License.
  * You may obtain a copy of the License at:
  *
  *      http://cvs.sakaiproject.org/licenses/license_1_0.html
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  **********************************************************************************/
 package org.sakaiproject.tool.assessment.integration.helper.standalone;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
 import org.sakaiproject.tool.assessment.osid.shared.impl.AgentImpl;
 import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
 import org.sakaiproject.tool.assessment.ui.bean.shared.BackingBean;
 import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
 
 /**
  *
  * <p>Description:
  * This is a stub standalone context implementation helper delegate class for
  * the AgentFacade class.  "Standalone" means that Samigo (Tests and Quizzes)
  * is running without the context of the Sakai portal and authentication
  * mechanisms, and therefore we "make up" some of the values returned.</p>
  * <p>Note: To customize behavior you can add your own helper class to the
  * Spring injection via the integrationContext.xml for your context.
  * The particular integrationContext.xml to be used is selected by the
  * build process.
  * </p>
  * <p>Sakai Project Copyright (c) 2005</p>
  * <p> </p>
  * @author Ed Smiley <esmiley@stanford.edu>
 *
  */
 public class AgentHelperImpl implements AgentHelper
 {
   private static Log log = LogFactory.getLog(AgentHelperImpl.class);
   String agentString;
 
   /**
    * Get an osid Agent implementation class instance.
    *
    * @return an AgentImpl: osid Agent implementation class.
    */
   public AgentImpl getAgent(){
     AgentImpl agent = new AgentImpl("Administrator", null, new IdImpl("admin"));
     return agent;
   }
 
   /**
    * Get the agent string.
    * @return the agent string.
    */
 
   public String getAgentString(){
     String agentS = "admin";
     BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
     if (bean != null && !bean.getProp1().equals("prop1"))
       agentS = bean.getProp1();
     return agentS;
   }
 
   /**
    * Get the agent string.
    * @param req the HttpServletRequest
    * @param res the HttpServletResponse
    * @return the agent string.
    */
   public String getAgentString(HttpServletRequest req, HttpServletResponse res){
     String agentS = "admin";
     BackingBean bean = (BackingBean) ContextUtil.lookupBeanFromExternalServlet(
         "backingbean", req, res);
     if (bean != null && !bean.getProp1().equals("prop1"))
       agentS = bean.getProp1();
     return agentS;
   }
 
   /**
    * Get the Agent display name.
    * @param agentS the Agent string.
    * @return the Agent display name.
    */
   public String getDisplayName(String agentS){
     if ("admin".equals(agentS))
       return "Administrator";
     else if (agentS.equals("rachel"))
       return "Rachel Gollub";
     else if (agentS.equals("marith"))
       return "Margaret Petit";
     else
       return "Dr. Who";
   }
 
   /**
    * Get the Agent first name.
    * @return the Agent first name.
    */
   public String getFirstName()
   {
     if ("admin".equals(agentString))
       return "Samigo";
     else if (agentString.equals("rachel"))
       return "Rachel";
     else if (agentString.equals("marith"))
       return "Margaret";
     else
       return "Dr.";
   }
 
   /**
   * Gegt the Agent last name.
    * @return the Agent last name.
    */
   public String getLastName()
   {
     if ("admin".equals(agentString))
       return "Administrator";
     else if (agentString.equals("rachel"))
       return "Gollub";
     else if (agentString.equals("marith"))
       return "Petit";
     else
       return "Who";
   }
 
   /**
    * Get the agent role.
    * @return the agent role.
    */
   public String getRole()
   {
     return "Student";
   }
 
   /**
    * For a specific agent id, get the agent role.
    * @param agentId the agent id
    * @return the agent role.
    */
   public String getRole(String agentId)
   {
     return "Maintain";
   }
 
   /**
    * Get the current site id.
    * @return the site id.
    */
   public String getCurrentSiteId(){
     return "Samigo Site";
   }
 
   /**
    * Get the current site name.
    * @return the site name.
    */
   public String getCurrentSiteName(){
     return "Samigo Site";
   }
 
   /**
    * Get the site name.
    * @param siteId  site id
    * @return the site name.
    */
   public String getSiteName(String siteId){
     return "Samigo Site";
   }
 
   /**
    * Get the id string.
    * @return the id string.
    */
   public String getIdString()
   {
     return this.getAgentString();
   }
 
   /**
    * Get the display name fo ra specific agent id string.
    * @param agentId the agent id string.
    * @return the display name.
    */
   public String getDisplayNameByAgentId(String agentId){
     return "Samigo Administrator";
   }
 
   /**
    * Create anonymous user and return the anonymous user id.
    * @return the anonymous user id.
    */
   public String createAnonymous(){
     BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
     String anonymousId = "anonymous_"+(new java.util.Date()).getTime();
     bean.setProp1(anonymousId);
     return anonymousId;
   }
 
   /**
    * Is this a standalone environment?
    * @return true, always, in this implementation
    */
   public boolean isStandaloneEnvironment(){
     return true;
   }
 
   /**
   * Is this a standalone environment?
    * @return false, in this implementation
    */
   public boolean isIntegratedEnvironment(){
     return false;
   }
 
   /**
    * Get current site id from within an external servlet.
    * @param req the HttpServletRequest
    * @param res the HttpServletResponse
    * @return teh site id.
    */
   public String getCurrentSiteIdFromExternalServlet(HttpServletRequest req,  HttpServletResponse res){
       return "Samigo Site";
   }
 
   /**
    * Get the anonymous user id.
    * @return the anonymous user id.
    */
   public String getAnonymousId(){
     String agentS = "";
     BackingBean bean = (BackingBean) ContextUtil.lookupBean("backingbean");
     if (bean != null && !bean.getProp1().equals("prop1"))
       agentS = bean.getProp1();
     return agentS;
   }
 
   /**
    * Set the agent id string.
    * @param idString the isd string.
    */
   public void setIdString(String idString)
   {
     this.agentString = idString;
   }
 
   /**
    * Set the agent string.
    * @param agentString the agent string.
    */
   public void setAgentString(String agentString)
   {
     this.agentString = agentString;
   }
 
 }
