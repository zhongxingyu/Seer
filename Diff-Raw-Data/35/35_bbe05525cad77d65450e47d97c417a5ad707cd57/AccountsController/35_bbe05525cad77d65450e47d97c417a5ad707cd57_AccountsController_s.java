 /*****************************************************************************
  * Copyright  2011 , UT-Battelle, LLC All rights reserved
  *
  * OPEN SOURCE LICENSE
  *
  * Subject to the conditions of this License, UT-Battelle, LLC (the
  * Licensor) hereby grants to any person (the Licensee) obtaining a copy
  * of this software and associated documentation files (the "Software"), a
  * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
  * copy, modify, merge, publish, distribute, and/or sublicense copies of the
  * Software.
  *
  * 1. Redistributions of Software must retain the above open source license
  * grant, copyright and license notices, this list of conditions, and the
  * disclaimer listed below.  Changes or modifications to, or derivative works
  * of the Software must be noted with comments and the contributor and
  * organizations name.  If the Software is protected by a proprietary
  * trademark owned by Licensor or the Department of Energy, then derivative
  * works of the Software may not be distributed using the trademark without
  * the prior written approval of the trademark owner.
  *
  * 2. Neither the names of Licensor nor the Department of Energy may be used
  * to endorse or promote products derived from this Software without their
  * specific prior written permission.
  *
  * 3. The Software, with or without modification, must include the following
  * acknowledgment:
  *
  *    "This product includes software produced by UT-Battelle, LLC under
  *    Contract No. DE-AC05-00OR22725 with the Department of Energy.
  *
  * 4. Licensee is authorized to commercialize its derivative works of the
  * Software.  All derivative works of the Software must include paragraphs 1,
  * 2, and 3 above, and the DISCLAIMER below.
  *
  *
  * DISCLAIMER
  *
  * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
  * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
  * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
  * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
  * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
  * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
  * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
  * liabilities, penalties, fines, claims, causes of action, and costs and
  * expenses, caused by, resulting from or arising out of, in whole or in part
  * the use, storage or disposal of the SOFTWARE.
  *
  *
  ******************************************************************************/
 
 
 /**
  * On request mapping:
  *
  *     url rewrite filter will take over first, then we do regular Spring mapping.
  *     RedirectView is discouraged here as it will mess up the current rewrite
  *     rule, use "redirect:" prefix instead, and it is regarded as a better alternative
  *     anyway.
  *
  * For any redirect trouble, please refers to ROOT/urlrewrite.xml
  *
  * @author Feiyi Wang (fwang2@ornl.gov)
  *
  */
 package org.esgf.adminui;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.esgf.manager.InputManager;
 import org.esgf.manager.InputManagerImpl;
 import org.esgf.manager.OutputManager;
 import org.esgf.manager.OutputManagerImpl;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import org.esgf.commonui.Utils;
 import esg.search.query.api.FacetProfile;
 import esg.search.query.api.SearchOutput;
 import esg.search.query.api.SearchService;
 import esg.search.query.impl.solr.SearchInputImpl;
 
 @Controller
 @RequestMapping(value="/accountsview")
 
 public class AccountsController {
 
     private final static String ACCOUNTS_INPUT = "accounts_input";
     private final static String ACCOUNTS_MODEL = "accounts_input";
     private final static String ACCOUNTS_USERINFO = "accounts_userinfo";
 
     private final static Logger LOG = Logger.getLogger(AccountsController.class);
 
     private final static String USERS_FILE = "C:\\Users\\8xo\\esgProjects\\esgf-6-29\\esgf-web-fe\\esgf-web-fe\\src\\java\\main\\users.file";
 
     private final static boolean debugFlag = true;
     /**
      * List of invalid text characters -
      * anything that is not within square brackets.
      */
     private static Pattern pattern =
         Pattern.compile(".*[^a-zA-Z0-9_\\-\\.\\@\\'\\:\\;\\,\\s/()].*");
 
     public AccountsController() {
         LOG.debug("IN AccountsController Constructor");
     }
 
     /**
      * Method invoked in response to a GET request:
      * -) if invoked directly, a new set of facets is retrieved (but no results)
      * -) if invoked in response to a POST-REDIRECT,
      * @param request
      * @param input
      * @param result
      * @return
      * @throws IOException 
      * @throws JDOMException 
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     @RequestMapping(method=RequestMethod.GET)
     public ModelAndView doGet(final HttpServletRequest request,
             final @ModelAttribute(ACCOUNTS_INPUT) String accountsInput) throws JDOMException, IOException {
 
         LOG.debug("In do get");
 
         //get the userId from the cookie
         String userId = Utils.getIdFromHeaderCookie(request);
         if(debugFlag) {
             LOG.debug("UserId Retrieved: " + userId);
         }
         
         //debug
         if(userId.equals("https://pcmdi3.llnl.gov/esgcet/myopenid/jfharney")) {
             userId = "user1_userName";
         }
         
         //initialize the model sent to the view
         Map<String,Object> model = new HashMap<String,Object>();
 
         //make sure this is a "fresh" model
         if (request.getParameter(ACCOUNTS_MODEL)!=null) {
             LOG.debug("Not null");
             // retrieve model from session
             model = (Map<String,Object>)request.getSession().getAttribute(ACCOUNTS_MODEL);
 
         } 
         else {
             
            User user = Utils.populateUserObjectFromIdXML(userId,new File(USERS_FILE));
             
             // populate model with the UserInfo
            model.put( ACCOUNTS_USERINFO, user);
             model.put(ACCOUNTS_INPUT, accountsInput);
             
             //put the model in the session
             request.getSession().setAttribute(ACCOUNTS_MODEL, model);
             
         }
         return new ModelAndView("accountsview", model);
     }
     
     
     
     /**
      * Method invoked in response to a POST request:
      * both results and facets are retrieved.
      * @param request
      * @param input
      * @param result
      * @return
      * @throws Exception
      */
     @RequestMapping(method=RequestMethod.POST)
     @SuppressWarnings("unchecked")
     protected ModelAndView doPost(final HttpServletRequest request,
             final @ModelAttribute(ACCOUNTS_INPUT) String accountsInput) throws Exception {
         LOG.debug("In do get");
 
         //get the userId from the cookie
         String userId = Utils.getIdFromHeaderCookie(request);
         if(debugFlag) {
             LOG.debug("UserId Retrieved: " + userId);
         }
         
         if(userId.equals("https://pcmdi3.llnl.gov/esgcet/myopenid/jfharney")) {
             userId = "user1_userName";
         }
         
         //initialize the model sent to the view
         Map<String,Object> model = new HashMap<String,Object>();
 
         //make sure this is a "fresh" model
         if (request.getParameter(ACCOUNTS_MODEL)!=null) {
             LOG.debug("Not null");
             // retrieve model from session
             model = (Map<String,Object>)request.getSession().getAttribute(ACCOUNTS_MODEL);
 
         } 
         else {
             
            User user = Utils.populateUserObjectFromIdXML(userId,new File(USERS_FILE));
             
             // populate model with the UserInfo
            model.put( ACCOUNTS_USERINFO, user);
             model.put(ACCOUNTS_INPUT, accountsInput);
             
             //put the model in the session
             request.getSession().setAttribute(ACCOUNTS_MODEL, model);
             
         }
         return new ModelAndView("accountsview", model);
     }
     
     
     
     /**
      *
      * @param request
      * @return
      * @throws Exception
      */
     /*
     @ModelAttribute(ACCOUNTS_MISC)
     public String formAccountsMiscObject(final HttpServletRequest request) throws Exception {
         LOG.debug("formAccountsMiscObject");
         
         
         
         return "ACCOUNTS_MISC here";
     }
     */
     
     /**
      *
      * @param request
      * @return
      * @throws Exception
      */
     @ModelAttribute(ACCOUNTS_INPUT)
     public String formAccountsInputObject(final HttpServletRequest request) throws Exception {
 
         LOG.debug("formAccountsInputObject called");
 
         return "ACCOUNTS_INPUT here";
 
     }
     
     
 
 }
 
