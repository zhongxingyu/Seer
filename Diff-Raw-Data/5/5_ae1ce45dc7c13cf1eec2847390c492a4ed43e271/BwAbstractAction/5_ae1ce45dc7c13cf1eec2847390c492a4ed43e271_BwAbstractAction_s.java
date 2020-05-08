 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package org.bedework.webcommon;
 
 import org.bedework.appcommon.BedeworkDefs;
 import org.bedework.calenv.CalEnv;
 import org.bedework.calenv.CalOptions;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwEventProxy;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwSponsor;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.CalFacadeAccessException;
 import org.bedework.calfacade.CalFacadeDefs;
 import org.bedework.calfacade.CalFacadeException;
 import org.bedework.calfacade.CalFacadeUtil;
 import org.bedework.calfacade.ifs.Groups;
 import org.bedework.calfacade.svc.AdminGroups;
 import org.bedework.calfacade.svc.BwAdminGroup;
 import org.bedework.calfacade.svc.BwAuthUser;
 import org.bedework.calfacade.svc.BwAuthUserPrefs;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.svc.UserAuth;
 import org.bedework.calfacade.svc.wrappers.BwCalSuiteWrapper;
 import org.bedework.calsvc.CalSvc;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.calsvci.CalSvcIPars;
 
 import edu.rpi.sss.util.Util;
 import edu.rpi.sss.util.jsp.UtilAbstractAction;
 import edu.rpi.sss.util.jsp.UtilActionForm;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.struts.util.MessageResources;
 
 /** This abstract action performs common setup actions before the real
  * action method is called.
  *
  * <p>Errors:<ul>
  *      <li>org.bedework.error.noaccess - when user has insufficient
  *            access</li>
  * </ul>
  *
  * @author  Mike Douglass  douglm@rpi.edu
  */
 public abstract class BwAbstractAction extends UtilAbstractAction
                                        implements ForwardDefs {
   /** Name of the init parameter holding our name */
   private static final String appNameInitParameter = "rpiappname";
 
   /*
    *  (non-Javadoc)
    * @see edu.rpi.sss.util.jsp.UtilAbstractAction#getId()
    */
   public String getId() {
     return getClass().getName();
   }
 
   /** This is the routine which does the work.
    *
    * @param request   Needed to locate session
    * @param response
    * @param sess      UWCalSession calendar session object
    * @param frm       Action form
    * @return String   forward name
    * @throws Throwable
    */
   public abstract String doAction(HttpServletRequest request,
                                   HttpServletResponse response,
                                   BwSession sess,
                                   BwActionFormBase frm) throws Throwable;
 
   public String performAction(HttpServletRequest request,
                               HttpServletResponse response,
                               UtilActionForm frm,
                               MessageResources messages) throws Throwable {
     String forward = "success";
     BwActionFormBase form = (BwActionFormBase)frm;
     String adminUserId = null;
 
     CalEnv env = getEnv(request, form);
 
     setConfig(request, form);
 
     setSessionAttr(request, "org.bedework.logprefix",
                    form.retrieveConfig().getLogPrefix());
 
     boolean guestMode = env.getAppBoolProperty("guestmode");
 
     if (guestMode) {
       form.assignCurrentUser(null);
     } else {
       adminUserId = form.getCurrentAdminUser();
       if (adminUserId == null) {
         adminUserId = form.getCurrentUser();
       }
     }
 
     if (getPublicAdmin(form)) {
       /** We may want to masquerade as a different user
        */
 
       String temp = getReqPar(request, "adminUserId");
 
       if (temp != null) {
         adminUserId = temp;
       }
     }
 
     BwSession s = getState(request, form, messages, adminUserId,
                            getPublicAdmin(form));
 
     if (s == null) {
       /* An error should have been emitted. The superclass will return an
          error forward.*/
       return forward;
     }
 
     form.setSession(s);
     form.setGuest(s.isGuest());
 
     if (form.getGuest()) {
       // force public view on - off by default
       form.setPublicView(true);
     }
 
     String appBase = form.getAppBase();
 
     if (appBase != null) {
       // Embed in request for pages that cannot access the form (loggedOut)
       request.setAttribute("org.bedework.action.appbase", appBase);
     }
 
     if (form.getNewSession()) {
       // Set to default view
       setView(null, form);
     }
 
     /* Set up ready for the action */
 
     int temp = actionSetup(request, response, form);
     if (temp != forwardNoAction) {
       return forwards[temp];
     }
 
     if (form.getNewSession()) {
       // Set the default skin
       BwPreferences prefs = form.fetchSvci().getUserPrefs();
 
       String skinName = prefs.getSkinName();
 
       form.getPresentationState().setSkinName(skinName);
       form.getPresentationState().setSkinNameSticky(true);
     }
 
     /* see if we got cancelled */
 
     String reqpar = request.getParameter("cancelled");
 
     if (reqpar != null) {
       /** Set the objects to null so we get new ones.
        */
       initFields(form);
 
       form.getMsg().emit("org.bedework.client.message.cancelled");
       return "cancelled";
     }
 
     if (!getPublicAdmin(form)) {
       /* Set up or refresh frequently used information,
        */
       getSubscriptions(form);
     }
 
     try {
       forward = doAction(request, response, s, form);
 
       if (!getPublicAdmin(form)) {
         /* See if we need to refresh */
         checkRefresh(form);
       }
     } catch (CalFacadeAccessException cfae) {
       form.getErr().emit("org.bedework.client.error.noaccess", "for that action");
       forward="noAccess";
     } catch (Throwable t) {
       form.getErr().emit("org.bedework.client.error.exc", t.getMessage());
       form.getErr().emit(t);
     }
 
     return forward;
   }
 
   /** Called just before action.
    *
    * @param request
    * @param response
    * @param form
    * @return int foward index
    * @throws Throwable
    */
   public int actionSetup(HttpServletRequest request,
                          HttpServletResponse response,
                          BwActionFormBase form) throws Throwable {
     if (getPublicAdmin(form)) {
       CalSvcI svc = form.fetchSvci();
 
       UserAuth ua = svc.getUserAuth();
       BwAuthUser au = ua.getUser(form.getCurrentUser());
 
       // Refresh current auth user prefs.
       BwAuthUserPrefs prefs = au.getPrefs();
       if (prefs == null) {
         prefs = new BwAuthUserPrefs();
       }
 
       form.setCurAuthUserPrefs(prefs);
       if (form.getNewSession()) {
         // First time through here for this session. svci is still set up for the
         // authenticated user. Set access rights.
 
         int rights = au.getUsertype();
 
         form.assignCurUserAlerts((rights & UserAuth.alertUser) != 0);
         form.assignCurUserPublicEvents((rights & UserAuth.publicEventUser) != 0);
         form.assignCurUserContentAdminUser((rights & UserAuth.contentAdminUser) != 0);
         form.assignCurUserSuperUser((rights & UserAuth.superUser) != 0);
 
         form.assignAuthorisedUser(rights != UserAuth.noPrivileges);
         svc.setSuperUser((rights & UserAuth.superUser) != 0);
       }
 
       if (debug) {
         logIt("form.getGroupSet()=" + form.getGroupSet());
       }
 
       /** Show the owner we are administering */
       form.setAdminUserId(form.fetchSvci().getUser().getAccount());
 
       if (debug) {
         logIt("-------- isSuperUser: " + form.getCurUserSuperUser());
       }
 
       int temp = checkGroup(request, form, true);
 
       if (temp != forwardNoAction) {
         if (debug) {
           logIt("form.getGroupSet()=" + form.getGroupSet());
         }
         return temp;
       }
 
       if (!form.getAuthorisedUser()) {
         return forwardNoAccess;
       }
 
       return forwardNoAction;
     }
 
     // Not public admin.
 
     ConfigBase conf = form.retrieveConfig();
 
     String refreshAction = conf.getRefreshAction();
 
     if (refreshAction == null) {
       refreshAction = form.getActionPath();
     }
 
     if (refreshAction != null) {
       setRefreshInterval(request, response,
                          conf.getRefreshInterval(),
                          refreshAction, form);
     }
 
     if (debug) {
       log.debug("curTimeView=" + form.getCurTimeView());
     }
 
     return forwardNoAction;
   }
 
   /** Set the config object.
    *
    * @param request
    * @param form
    * @throws Throwable
    */
   public void setConfig(HttpServletRequest request,
                         BwActionFormBase form) throws Throwable {
     if (!form.configSet()) {
       ConfigBase conf = (ConfigBase)getConfigOption(request, null);
 
       form.setConfig(conf);
     }
   }
 
   protected void initFields(BwActionFormBase form) {
   }
 
   /* Reset the current selection. Called after updates to views, subscriptions
    * or calendars.
    */
   protected void resetSelection(BwActionFormBase form) throws CalFacadeException {
     String seltype = form.getSelectionType();
     CalSvcI svci = form.fetchSvci();
     String name = null;
 
     if (seltype.equals(BedeworkDefs.selectionTypeView)) {
       BwView v = svci.getCurrentView();
       if (v != null) {
         name = v.getName();
       }
       setView(name, form);
     } else if (seltype.equals(BedeworkDefs.selectionTypeSubscription)) {
       // No refresh needed?
     } else if (seltype.equals(BedeworkDefs.selectionTypeCalendar)) {
       // No refresh needed?
     } else {
       // Set to default
       setView(null, form);
     }
   }
 
   /* Set the view to the given name or the default if null.
    *
    * @return false for not found
    */
   protected boolean setView(String name,
                             BwActionFormBase form) throws CalFacadeException {
     CalSvcI svci = form.fetchSvci();
 
     if (name == null) {
       name = svci.getUserPrefs().getPreferredView();
     }
 
     if (name == null) {
       form.getErr().emit("org.bedework.client.error.nodefaultview");
       return false;
     }
 
     if (!svci.setCurrentView(name)) {
       form.getErr().emit("org.bedework.client.error.unknownview");
       return false;
     }
 
     form.setSelectionType(BedeworkDefs.selectionTypeView);
     form.refreshIsNeeded();
     return true;
   }
 
   /* Set the subscription to the given name or the default if null.
    *
    * @return int result code
    */
   protected int setSubscription(String name,
                                 BwActionFormBase form) throws CalFacadeException {
     CalSvcI svci = form.fetchSvci();
 
     if (name == null) {
       return forwardNoParameter;
     }
 
     BwSubscription sub = svci.findSubscription(name);
 
     if (sub == null) {
       form.getErr().emit("org.bedework.client.error.unknownsubscription");
       return forwardNotFound;
     }
 
     Collection c = new ArrayList();
     c.add(sub.clone());
     svci.setCurrentSubscriptions(c);
     form.assignCurrentSubscriptions(c);
     form.setSelectionType(BedeworkDefs.selectionTypeSubscription);
 
     form.refreshIsNeeded();
     return forwardSuccess;
   }
 
   /** Method to find a subscription given its name. Expects a request parameter
    * "subname". Returns with the subscription or null set in the form.
    *
    * @param request     HttpServletRequest for parameters
    * @param form
    * @param emitError   Emit error messages for missing request par.
    * @return boolean    true there was a subname parameter
    * @param cloneIt     true if we should clone the result
    * @throws Throwable
    */
   protected boolean findSubscription(HttpServletRequest request,
                                      BwActionFormBase form,
                                      boolean emitError,
                                      boolean cloneIt) throws Throwable {
     CalSvcI svci = form.fetchSvci();
 
     String name = getReqPar(request, "subname");
     if (name == null) {
       if (emitError) {
         form.getErr().emit("org.bedework.client.error.missingfield", "subname");
       }
       return false;
     }
 
     BwSubscription sub = svci.findSubscription(name);
 
     if (debug) {
       if (sub == null) {
         logIt("No subscription with name " + name);
       } else {
         logIt("Retrieved subscription " + sub.getId());
       }
     }
 
     if (sub == null) {
       form.setSubscription(null);
       form.getErr().emit("org.bedework.client.error.nosuchsubscription", name);
     }
 
     if (cloneIt) {
       sub = (BwSubscription)sub.clone();
     }
     form.setSubscription(sub);
     return true;
   }
 
   protected boolean validateSub(BwSubscription sub,
                                 BwActionFormBase form) {
     sub.setName(Util.checkNull(sub.getName()));
 
     if (sub.getName() == null) {
       form.getErr().emit("org.bedework.validation.error.missingfield", "name");
       return false;
     }
 
     sub.setUri(Util.checkNull(sub.getUri()));
 
     if (!sub.getInternalSubscription() && (sub.getUri() == null)) {
       form.getErr().emit("org.bedework.validation.error.missingfield", "uri");
       return false;
     }
 
     return true;
   }
 
   /** Called to finish off adding or updating a subscription
    *
    * @param request
    * @param sub
    * @param form
    * @return int   result code
    * @throws Throwable
    */
   protected int finishSubscribe(HttpServletRequest request,
                                    BwSubscription sub,
                                    BwActionFormBase form) throws Throwable {
     CalSvcI svc = form.fetchSvci();
     int result;
 
     String viewName = getReqPar(request, "view");
     boolean addToDefaultView = false;
 
     if (viewName == null) {
       addToDefaultView = true;
       String str = getReqPar(request, "addtodefaultview");
       if (str != null) {
         addToDefaultView = str.equals("y");
       }
     }
 
     Boolean bool = getBooleanReqPar(request, "unremoveable");
     if (bool != null) {
       if (!form.getCurUserSuperUser()) {
         return forwardNoAccess; // Only super user for that flag
       }
 
       sub.setUnremoveable(bool.booleanValue());
     }
 
     if (!validateSub(sub, form)) {
       return forwardRetry;
     }
 
     if (getReqPar(request, "addSubscription") != null) {
       try {
         svc.addSubscription(sub);
         result = forwardAdded;
       } catch (CalFacadeException cfe) {
         if (CalFacadeException.duplicateSubscription.equals(cfe.getMessage())) {
           form.getErr().emit(cfe.getMessage());
           return forwardSuccess; // User will see message and we'll stay on page
         }
 
         throw cfe;
       }
     } else if (getReqPar(request, "updateSubscription") != null) {
       svc.updateSubscription(sub);
       result = forwardUpdated;
     } else {
       result = forwardNoAction;
     }
 
     if ((viewName == null) && !addToDefaultView) {
       // We're done - not adding to a view
       return result;
     }
 
     if (sub != null) {
       svc.addViewSubscription(viewName, sub);
     }
 
     getSubscriptions(form);
 
     return result;
   }
 
   /** Implant current subscriptions in the form.
    *
    * @param form
    * @throws Throwable
    */
   public void getSubscriptions(BwActionFormBase form) throws Throwable {
     CalSvcI svc = form.fetchSvci();
     Collection subs = svc.getSubscriptions();
     form.setSubscriptions(subs);
   }
 
   /** Find a user object given a "user" request parameter.
    *
    * @param request     HttpServletRequest for parameters
    * @param form
    * @return BwUser     null if not found. Messages emitted
    * @throws Throwable
    */
   protected BwUser findUser(HttpServletRequest request,
                              BwActionFormBase form) throws Throwable {
     CalSvcI svci = form.fetchSvci();
 
     String str = getReqPar(request, "user");
     if (str == null) {
       form.getErr().emit("org.bedework.client.error.usernotfound");
       return null;
     }
 
     BwUser user = svci.findUser(str);
     if (user == null) {
       form.getErr().emit("org.bedework.client.error.usernotfound", str);
       return null;
     }
 
     return user;
   }
 
   /** Method to find a calendar given the subscription name. Expects a request
    * parameter "subname". Returns with the subscription or null set in the form
    * and the associated calendar embedded in the subscription object.
    *
    * @param request     HttpServletRequest for parameters
    * @param form
    * @param emitError   Emit error messages for missing request par.
    * @return boolean    true there was a subname parameter
    * @throws Throwable
    */
   protected boolean findSubscribedCalendar(HttpServletRequest request,
                                            BwActionFormBase form,
                                            boolean emitError) throws Throwable {
     CalSvcI svci = form.fetchSvci();
 
     if (!findSubscription(request, form, emitError, false)) {
       return false;
     }
 
     BwSubscription sub = form.getSubscription();
     if (sub == null) {
       return true;
     }
 
     svci.getSubCalendar(sub);
 
     return true;
   }
 
   /** Set the event calendar based on request parameters. If newCalPath
    * is specified we use the named calendar as the event calendar.
    *
    * <p>If newCalPath is not speciifed and subname is specified it should refer to
    * an external calendar. We will use teh dummy calendar object for the event.
    *
    * <p>If neither newCalPath or subname is specified we use the default.
    *
    * <p>If a calendar was already set in the event, this action will only
    * change that calendar if subname or newCalPath are specified. It will not
    * reset the calendar to the default.
    *
    * @param request
    * @param form
    * @param ev
    * @return String error forward or null for OK
    * @throws Throwable
    */
   protected String setEventCalendar(HttpServletRequest request,
                                     BwActionFormBase form,
                                     BwEvent ev) throws Throwable {
     CalSvcI svci = form.fetchSvci();
     BwSubscription sub = null;
     BwCalendar cal = null;
 
     String newCalPath = request.getParameter("newCalPath");
 
     if (newCalPath != null) {
       cal = svci.getCalendar(newCalPath);
       if (cal == null) {
         form.getErr().emit("org.bedework.client.error.nosuchcalendar", newCalPath);
         return "notFound";
       }
     } else if (findSubscribedCalendar(request, form, false)) {
       sub = form.getSubscription();
       cal = sub.getCalendar();
     } else if (ev.getCalendar() == null) {
       // Use the default.
       cal = svci.getPreferredCalendar();
     }
 
     if (cal != null) {
       ev.setCalendar(cal);
     } else if (ev.getCalendar() == null) {
       // Just a validity check
       return "error";
     }
 
     return null;  // OK return
   }
 
   /** Method to retrieve an event. An event is identified by the calendar +
    * guid + recurrence id. We also take the subscription id as a parameter so
    * we can pass it along in the result for display purposes.
    *
    * <p>We cannot just take the calendar from the subscription, because the
    * calendar has to be the actual collection containing the event. A
    * subscription may be to higher up the tree (i.e. a folder).
    *
    * <p>We need to also allow the calendar path instead of the id. External
    * calendars don't have an id. This means changing the api (again) and
    * changing the urls (again).
    *
    * <p>It may be more appropriate to simply encode a url to the event.
    *
    * <p>Request parameters<ul>
    *      <li>"subid"    subscription id for event. < 0 if there is none
    *                     e.g. displayed directly from calendar.</li>
    *      <li>"calPath"  Path of calendar to search.</li>
    *      <li>"guid"     guid of event.</li>
    *      <li>"recurrenceId"   recurrence-id of event instance - possibly null.</li>
    * </ul>
    * <p>If the recurrenceId is null and the event is a recurring event we
    * should return the master event only,
    *
    * @param request   HttpServletRequest for parameters
    * @param form
    * @return EventInfo or null if not found
    * @throws Throwable
    */
   protected EventInfo findEvent(HttpServletRequest request,
                                 BwActionFormBase form) throws Throwable {
     CalSvcI svci = form.fetchSvci();
     EventInfo ev = null;
     BwSubscription sub = null;
 
     int subid = getIntReqPar(request, "subid", -1);
     if (subid >= 0) {
       sub = svci.getSubscription(subid);
 
       if (sub == null) {
         form.getErr().emit("org.bedework.client.error.missingsubscriptionid");
         return null;
       }
     }
 
     BwCalendar cal = null;
 
     String calPath = getReqPar(request, "calPath");
 
     if (calPath == null) {
       // bogus request
       form.getErr().emit("org.bedework.client.error.missingcalendarpath");
       return null;
     }
 
     cal = svci.getCalendar(calPath);
 
     if (cal == null) {
       // Assume no access
       form.getErr().emit("org.bedework.client.error.noaccess");
       return null;
     }
 
     String guid = getReqPar(request, "guid");
 
     if (guid != null) {
       if (debug) {
         debugMsg("Get event by guid");
       }
       String rid = getReqPar(request, "recurrenceId");
       int retMethod = CalFacadeDefs.retrieveRecurMaster;
       Collection evs = svci.getEvent(sub, cal, guid, rid, retMethod);
       if (debug) {
         debugMsg("Get event by guid found " + evs.size());
       }
       if (evs.size() == 1) {
         ev = (EventInfo)evs.iterator().next();
       } else {
         // XXX this needs dealing with
       }
     }
 
     if (ev == null) {
       form.getErr().emit("org.bedework.client.error.nosuchevent", /*eid*/guid);
       return null;
     } else if (debug) {
       debugMsg("Get event by guid found " + ev.getEvent());
     }
 
     return ev;
   }
 
   /** Refetch an event given a copy of that event. Calnedar guid and possibly
    * recurrence id must be set.
    *
    * @param event   BwEvent to refetch
    * @param form
    * @return EventInfo or null if not found
    * @throws Throwable
    */
   protected EventInfo fetchEvent(BwEvent event,
                                  BwActionFormBase form) throws Throwable {
     CalSvcI svci = form.fetchSvci();
     EventInfo ev = null;
     BwSubscription sub = null;
 
     BwCalendar cal = event.getCalendar();
 
     if (cal == null) {
       // Assume no access
       form.getErr().emit("org.bedework.client.error.noaccess");
       return null;
     }
 
     String guid = event.getGuid();
 
     if (guid == null) {
       // Assume no access
       form.getErr().emit("org.bedework.client.error.noaccess");
       return null;
     }
 
     String rid = event.getRecurrence().getRecurrenceId();
 
     // XXX is this right?
     int retMethod = CalFacadeDefs.retrieveRecurMaster;
     Collection evs = svci.getEvent(sub, cal, guid, rid, retMethod);
     if (debug) {
       debugMsg("Get event by guid found " + evs.size());
     }
     if (evs.size() == 1) {
       ev = (EventInfo)evs.iterator().next();
     } else {
       // XXX this needs dealing with
     }
 
     if (ev == null) {
       form.getErr().emit("org.bedework.client.error.nosuchevent", /*eid*/guid);
       return null;
     } else if (debug) {
       debugMsg("Fetch event found " + ev.getEvent());
     }
 
     return ev;
   }
 
   /** Add an event ref. The calendar to add it to is defined by the request
    * parameter newCalPath.
    *
    * @param request
    * @param form
    * @return String forward for an errot or null for OK.
    * @throws Throwable
    */
   protected String addEventRef(HttpServletRequest request,
                                BwActionFormBase form) throws Throwable {
     if (form.getGuest()) {
       return "doNothing";
     }
 
     CalSvcI svci = form.fetchSvci();
 
     EventInfo ei = findEvent(request, form);
 
     if (ei == null) {
       // Do nothing
       return "eventNotFound";
     }
 
     /* Create an event to act as a reference to the targeted event and copy
      * the appropriate fields from the target
      */
     BwEventProxy proxy = BwEventProxy.makeAnnotation(ei.getEvent(),
                                                      ei.getEvent().getOwner());
     form.setEditEvent(proxy); // Make it available
 
     String path = getReqPar(request, "newCalPath");
     BwCalendar cal;
 
     if (path == null) {
       cal = svci.getPreferredCalendar();
     } else {
       cal = svci.getCalendar(path);
       if (cal == null) {
         form.getErr().emit("org.bedework.client.error.nosuchcalendar", path);
         return "calendarNotFound";
       }
     }
     proxy.setOwner(svci.getUser());
 
     String transparency = getReqPar(request, "transparency");
     if (transparency != null) {
       if (!CalFacadeUtil.validTransparency(transparency)) {
         form.getErr().emit("org.bedework.client.error.badvalue",
                            "transparency");
         return "badValue";
       }
 
       proxy.setTransparency(transparency);
     }
 
     try {
       svci.addEvent(cal, proxy, null);
       form.getMsg().emit("org.bedework.client.message.added.eventrefs", 1);
     } catch (CalFacadeException cfe) {
       if (CalFacadeException.duplicateGuid.equals(cfe.getMessage())) {
         form.getErr().emit("org.bedework.client.error.duplicate.guid");
         return "duplicate";
       }
 
       throw cfe;
     }
 
     return null;
   }
 
   protected BwCalendar findCalendar(String url,
                              BwActionFormBase form) throws CalFacadeException {
     if (url == null) {
       return null;
     }
 
     CalSvcI svci = form.fetchSvci();
 
     return svci.getCalendar(url);
   }
 
   /** Return no action if group is chosen else return a forward index.
    *
    * @param request   Needed to locate session
    * @param form      Action form
    * @param initCheck true if this is a check to see if we're initialised,
    *                  otherwise this is an explicit request to change group.
    * @return int   forward index
    * @throws Throwable
    */
   protected int checkGroup(HttpServletRequest request,
                            BwActionFormBase form,
                            boolean initCheck) throws Throwable {
     if (form.getGroupSet()) {
       return forwardNoAction;
     }
 
     CalSvcI svci = form.fetchSvci();
 
     try {
       Groups adgrps = svci.getGroups();
 
       if (form.retrieveChoosingGroup()) {
         /** This should be the response to presenting a list of groups.
             We handle it here rather than in a separate action to ensure our
             client is not trying to bypass the group setting.
          */
 
         String reqpar = getReqPar(request, "adminGroupName");
         if (reqpar == null) {
           // Make them do it again.
 
           return forwardChooseGroup;
         }
 
         BwAdminGroup adg = (BwAdminGroup)adgrps.findGroup(reqpar);
         if (adg == null) {
           if (debug) {
             logIt("No user admin group with name " + reqpar);
           }
           // We require a group
           return forwardChooseGroup;
         }
 
         return setGroup(request, form, adgrps, adg);
       }
 
       /** If the user is in no group or in one group we just go with that,
           otherwise we ask them to select the group
        */
 
       Collection adgs;
 
       BwUser user = svci.findUser(form.getCurrentUser());
       if (user == null) {
         return forwardNoAccess;
       }
 
       if (initCheck || !form.getCurUserSuperUser()) {
         // Always restrict to groups of which we are a member
         adgs = adgrps.getGroups(user);
       } else {
         adgs = adgrps.getAll(false);
       }
 
       if (adgs.isEmpty()) {
         /** If we require that all users be in a group we return to an error
             page. The only exception will be superUser.
          */
 
         boolean noGroupAllowed =
             form.getEnv().getAppBoolProperty("nogroupallowed");
         if (svci.getUserAuth().isSuperUser() || noGroupAllowed) {
           form.assignAdminGroup(null);
           return forwardNoAction;
         }
 
         return forwardNoGroupAssigned;
       }
 
       if (adgs.size() == 1) {
         Iterator adgsit = adgs.iterator();
 
         BwAdminGroup adg = (BwAdminGroup)adgsit.next();
 
         return setGroup(request, form, adgrps, adg);
       }
 
       /** Go ahead and present the possible groups
        */
       form.setUserAdminGroups(adgs);
       form.assignChoosingGroup(true); // reset
 
       return forwardChooseGroup;
     } catch (Throwable t) {
       form.getErr().emit(t);
       return forwardError;
     }
   }
 
   /** Override to return true if this is an admin client
    *
    * @param frm
    * @return boolean  true for a public events admin client
    * @throws Throwable
    */
   public boolean getPublicAdmin(BwActionFormBase frm) throws Throwable {
     return frm.getEnv().getAppBoolProperty("publicadmin");
   }
 
   /** get an options property object. If name is null uses the application
    * name.
    *
    * @param request    HttpServletRequest
    * @param name
    * @return Object .
    * @throws Throwable
    */
   protected Object getConfigOption(HttpServletRequest request,
                                    String name) throws Throwable {
     if (name == null) {
       HttpSession session = request.getSession();
       ServletContext sc = session.getServletContext();
 
       name = sc.getInitParameter("bwappname");
 
       if ((name == null) || (name.length() == 0)) {
         name = "unknown-app-name";
       }
     }
 
     String appPrefix = "org.bedework.app.";
     return CalOptions.getProperty(appPrefix + name);
   }
 
   /** Get a prefix for the loggers.
    *
    * @param request    HttpServletRequest
    * @return  String    log prefix
    */
   protected String getLogPrefix(HttpServletRequest request) {
     try {
       String pfx = (String)getSessionAttr(request, "org.bedework.logprefix");
 
       if (pfx == null) {
         return "NOT-SET";
       }
 
       return pfx;
     } catch (Throwable t) {
       error(t);
       return "LOG-PREFIX-EXCEPTION";
     }
   }
 
   /* We should probably return false for a portlet
    *  (non-Javadoc)
    * @see edu.rpi.sss.util.jsp.UtilAbstractAction#logOutCleanup(javax.servlet.http.HttpServletRequest)
    */
   protected boolean logOutCleanup(HttpServletRequest request,
                                   UtilActionForm form) {
     HttpSession hsess = request.getSession();
     BwCallback cb = (BwCallback)hsess.getAttribute(BwCallback.cbAttrName);
 
     if (cb == null) {
       if (form.getDebug()) {
         debugMsg("No cb object for logout");
       }
     } else {
       if (form.getDebug()) {
         debugMsg("cb object found for logout");
       }
       try {
         cb.out();
       } catch (Throwable t) {}
 
       try {
         cb.close();
       } catch (Throwable t) {}
     }
 
     return true;
   }
 
   /** Update an authorised users preferences to reflect usage.
    *
    * @param form
    * @param categories Collection of CategoryVO objects
    * @param sp       SponsorVO object
    * @param loc      LocationVO object
    * @param cal
    * @throws Throwable
    */
   protected void updateAuthPrefs(BwActionFormBase form,
                                  Collection categories, BwSponsor sp, BwLocation loc,
                                  BwCalendar cal) throws Throwable {
     if (!getPublicAdmin(form)) {
       return;
     }
 
     UserAuth ua = form.fetchSvci().getUserAuth();
     BwAuthUser au = ua.getUser(form.getCurrentUser());
     BwAuthUserPrefs prefs = au.getPrefs();
     if (prefs == null) {
       prefs = new BwAuthUserPrefs();
 
       au.setPrefs(prefs);
     }
 
     /** If we have auto add on, add to the users preferred list(s)
      */
 
     if ((categories != null) && (prefs.getAutoAddCategories())) {
       Iterator it = categories.iterator();
       while (it.hasNext()) {
         BwCategory k = (BwCategory)it.next();
         ua.addCategory(au, k);
       }
     }
 
     if ((sp != null) && (prefs.getAutoAddSponsors())) {
       ua.addSponsor(au, sp);
     }
 
     if ((loc != null) && (prefs.getAutoAddLocations())) {
       ua.addLocation(au, loc);
     }
 
     if ((cal != null) && (prefs.getAutoAddCalendars())) {
       ua.addCalendar(au, cal);
     }
   }
 
   /** Check for logout request. Overridden so we can close anything we
    * need to before the session is invalidated.
    *
    * @param request    HttpServletRequest
    * @return null for continue, forwardLoggedOut to end session.
    * @throws Throwable
    */
   protected String checkLogOut(HttpServletRequest request)
                throws Throwable {
     String temp = request.getParameter(requestLogout);
     if (temp != null) {
       HttpSession sess = request.getSession(false);
 
       if (sess != null) {
         /* I don't think I need this - we didn't come through the svci filter on the
            way in?
         UWCalCallback cb = (UWCalCallback)sess.getAttribute(UWCalCallback.cbAttrName);
 
         try {
           if (cb != null) {
             cb.out();
           }
         } catch (Throwable t) {
           getLogger().error("Callback exception: ", t);
           /* Probably no point in throwing it. We're leaving anyway. * /
         } finally {
           if (cb != null) {
             try {
               cb.close();
             } catch (Throwable t) {
               getLogger().error("Callback exception: ", t);
               /* Probably no point in throwing it. We're leaving anyway. * /
             }
           }
         }
         */
 
         sess.invalidate();
       }
       return forwardLoggedOut;
     }
 
     return null;
   }
 
   /** Callback for filter
    *
    */
   public static class Callback extends BwCallback {
     BwActionFormBase form;
 
     Callback(BwActionFormBase form) {
       this.form = form;
     }
 
     /** Called when the response is on its way in. The parameter indicates if
      * this is an action rather than a render url.
      *
      * @param actionUrl   boolean true if it's an action (as against render)
      * @throws Throwable
      */
     public void in(boolean actionUrl) throws Throwable {
       synchronized (form) {
         CalSvcI svci = form.fetchSvci();
         if (svci != null) {
           if (svci.isOpen()) {
             // double-clicking on our links eh?
             form.incWaiters();
             form.wait();
           }
           form.decWaiters();
 
           if (actionUrl) {
             svci.flushAll();
           }
           svci.open();
           svci.beginTransaction();
         }
       }
     }
 
     /** Called when the response is on its way out.
      *
      * @throws Throwable
      */
     public void out() throws Throwable {
       CalSvcI svci = form.fetchSvci();
       if (svci != null) {
         svci.endTransaction();
       }
     }
 
     /** Close the session.
     *
     * @throws Throwable
     */
     public void close() throws Throwable {
       Throwable t = null;
 
       try {
         CalSvcI svci = form.fetchSvci();
         if (svci != null) {
           svci.close();
         }
       } catch (Throwable t1) {
         t = t1;
       } finally {
         synchronized (form) {
           form.notify();
         }
       }
 
       if (t != null) {
         throw t;
       }
     }
   }
 
   /* ********************************************************************
                              private methods
      ******************************************************************** */
 
   /** Get the session state object for a web session. If we've already been
    * here it's embedded in the current session. Otherwise create a new one.
    *
    * <p>We also carry out a number of web related operations.
    *
    * @param request       HttpServletRequest Needed to locate session
    * @param form          Action form
    * @param messages      MessageResources needed for the resources
    * @param adminUserId   id we want to administer
    * @param admin         Get this for the admin client
    * @return UWCalSession null on failure
    * @throws Throwable
    */
   private synchronized BwSession getState(HttpServletRequest request,
                                           BwActionFormBase form,
                                           MessageResources messages,
                                           String adminUserId,
                                           boolean admin) throws Throwable {
     BwSession s = BwWebUtil.getState(request);
     HttpSession sess = request.getSession(false);
     String appName = getAppName(sess);
 
     if (s != null) {
       if (debug) {
         debugMsg("getState-- obtainedfrom session");
         debugMsg("getState-- timeout interval = " +
                  sess.getMaxInactiveInterval());
       }
 
       form.assignNewSession(false);
     } else {
       if (debug) {
         debugMsg("getState-- get new object");
       }
 
       form.assignNewSession(true);
 
       String appRoot = form.retrieveConfig().getAppRoot();
 
       /* If we're running as a portlet change the app root to point to a
        * portlet specific directory.
        */
       String portalPlatform = form.retrieveConfig().getPortalPlatform();
       if (isPortlet && (portalPlatform != null)) {
         appRoot += "." + portalPlatform;
       }
 
       /* If calendar suite is non-null append that. */
       String calSuite = form.retrieveConfig().getCalSuite();
       if (calSuite != null) {
         appRoot += "." + calSuite;
       }
 
       /** The actual session class used is possibly site dependent
        */
       s = new BwSessionImpl(form.getCurrentUser(), appRoot, appName,
                             form.getPresentationState(), messages,
                             form.getSchemeHostPort(), debug);
 
       BwWebUtil.setState(request, s);
 
       setSessionAttr(request, "cal.pubevents.client.uri",
                      messages.getMessage("org.bedework.public.calendar.uri"));
 
       setSessionAttr(request, "cal.personal.client.uri",
                      messages.getMessage("org.bedework.personal.calendar.uri"));
 
       setSessionAttr(request, "cal.admin.client.uri",
                      messages.getMessage("org.bedework.public.admin.uri"));
 
       String temp = messages.getMessage("org.bedework.host");
       if (temp == null) {
         temp = form.getSchemeHostPort();
       }
 
       setSessionAttr(request, "cal.server.host", temp);
 
       String raddr = request.getRemoteAddr();
       String rhost = request.getRemoteHost();
       info("===============" + appName + ": New session (" +
                        s.getSessionNum() + ") from " +
                        rhost + "(" + raddr + ")");
 
       if (!admin) {
         /** Ensure the session timeout interval is longer than our refresh period
          */
         //  Should come from db -- int refInt = s.getRefreshInterval();
         int refInt = 60; // 1 min refresh?
 
         if (refInt > 0) {
           int timeout = sess.getMaxInactiveInterval();
 
           if (timeout <= refInt) {
             // An extra minute should do it.
             debugMsg("@+@+@+@+@+ set timeout to " + (refInt + 60));
             sess.setMaxInactiveInterval(refInt + 60);
           }
         }
       }
     }
 
     //int access = getAccess(request, messages);
     //if (debug) {
     //  debugMsg("Container says that current user has the type: " + access);
     //}
 
     /** Ensure we have a CalAdminSvcI object
      */
     String calSuite = form.retrieveConfig().getCalSuite();
     checkSvci(request, form, s, adminUserId, calSuite,
               getPublicAdmin(form), false, debug);
 
     /*
     UserAuth ua = null;
     UserAuthPar par = new UserAuthPar();
     par.svlt = servlet;
     par.req = request;
 
     try {
       ua = form.fetchSvci().getUserAuth(s.getUser(), par);
 
       form.assignAuthorisedUser(ua.getUsertype() != UserAuth.noPrivileges);
 
       if (debug) {
         debugMsg("UserAuth says that current user has the type: " +
                  ua.getUsertype());
       }
     } catch (Throwable t) {
       form.getErr().emit("org.bedework.client.error.exc", t.getMessage());
       form.getErr().emit(t);
       return null;
     }
     */
 
     return s;
   }
 
   private String getAppName(HttpSession sess) {
     ServletContext sc = sess.getServletContext();
 
     String appname = sc.getInitParameter(appNameInitParameter);
     if (appname == null) {
       appname = "?";
     }
 
     return appname;
   }
 
   private int setGroup(HttpServletRequest request,
                        BwActionFormBase form,
                        Groups adgrps,
                        BwAdminGroup adg) throws Throwable {
     CalSvcI svci = form.fetchSvci();
 
     adgrps.getMembers(adg);
 
     if (debug) {
       logIt("Set admin group to " + adg);
     }
 
     /* Determine which calsuite they are administering by finding the
      * first admin group on the path to the roo which has an associated
      * suite
      */
 
     BwCalSuiteWrapper cs = findCalSuite(svci, adg, adgrps);
     String calSuiteName = null;
     if (cs != null) {
       calSuiteName = cs.getName();
     }
 
     if (debug) {
       if (cs != null) {
         debugMsg("Found calSuite " + cs);
       } else {
         debugMsg("No calsuite found");
       }
     }
 
     form.setCurrentCalSuite(cs);
     form.assignAdminGroup(adg);
 
     //int access = getAccess(request, getMessages());
 
     if (!checkSvci(request, form, form.getSession(),
                    adg.getOwner().getAccount(),
                    calSuiteName, true, isMember(adg, form), debug)) {
       return forwardNoAccess;
     }
 
     form.setAdminUserId(form.fetchSvci().getUser().getAccount());
 
     return forwardNoAction;
   }
 
   private boolean isMember(BwAdminGroup ag,
                            BwActionFormBase form) throws Throwable {
     return ag.isMember(String.valueOf(form.getCurrentUser()), false);
   }
 
   /** Ensure we have a CalAdminSvcI object for the given user.
    *
    * <p>For an admin client with a super user we may switch to a different
    * user to administer their events.
    *
    * @param request       Needed to locate session
    * @param form          Action form
    * @param sess          Session object for global parameters
    * @param user          String user we want to be
    * @param calSuite      Name of calendar suite we are administering
    * @param publicAdmin   true if this is an administrative client
    * @param canSwitch     true if we should definitely allow user to switch
    *                      this allows a user to switch between and into
    *                      groups of which they are a member
    * @param debug         true for all that debugging stuff
    * @return boolean      false for problems.
    * @throws CalFacadeException
    */
   private boolean checkSvci(HttpServletRequest request,
                             BwActionFormBase form,
                             BwSession sess,
                             String user,
                             String calSuite,
                             boolean publicAdmin,
                             boolean canSwitch,
                             boolean debug) throws CalFacadeException {
     /** Do some checks first
      */
     String authUser = String.valueOf(form.getCurrentUser());
 
     if (!publicAdmin) {
       /* We're never allowed to switch identity as a user client.
        */
       if (!authUser.equals(String.valueOf(user))) {
         return false;
       }
     } else if (user == null) {
       throw new CalFacadeException("Null user parameter for public admin.");
     }
 
     CalSvcI svci = BwWebUtil.getCalSvcI(request);
 
     /** Make some checks to see if this is an old - restarted session.
         If so discard the svc interface
      */
     if (svci != null) {
       /* Not the first time through here so for a public admin client we
        * already have the authorised user's rights set in the form.
        */
 
       if (!svci.isOpen()) {
         svci.flushAll();
         svci = null;
         info(".Svci interface discarded from old session");
       } else if (publicAdmin) {
 
         BwUser u = svci.getUser();
         if (u == null) {
           throw new CalFacadeException("Null user for public admin.");
         }
 
         canSwitch = canSwitch || form.getCurUserContentAdminUser() ||
                     form.getCurUserSuperUser();
 
         String curUser = u.getAccount();
 
         if (!canSwitch && !user.equals(curUser)) {
           /** Trying to switch but not allowed */
           return false;
         }
 
         if (!user.equals(curUser)) {
           /** Switching user */
           svci.endTransaction();
           svci.close();
           svci.flushAll();
           svci = null;
         }
       }
     }
 
     if (svci != null) {
       /* Already there and already opened */
       if (debug) {
         debugMsg("CalSvcI-- Obtained from session for user " +
                           svci.getUser());
       }
     } else {
       if (debug) {
         debugMsg(".CalSvcI-- get new object for user " + user);
       }
 
       /* create a call back object so the filter can open the service
          interface */
       BwCallback cb = new Callback(form);
       HttpSession hsess = request.getSession();
       hsess.setAttribute(BwCallback.cbAttrName, cb);
 
       String runAsUser = user;
 
       try {
         svci = new CalSvc();
         if ((user == null) && (calSuite == null)) {
           runAsUser = form.getEnv().getAppProperty("run.as.user");
         }
 
         CalSvcIPars pars = new CalSvcIPars(user, //access,
                                            runAsUser,
                                            calSuite,
                                            form.getEnv().getAppPrefix(),
                                            publicAdmin,
                                            false,    // caldav
                                            null, // synchId
                                            debug);
         svci.init(pars);
         svci.setSuperUser(form.getCurUserSuperUser());
 
         BwWebUtil.setCalSvcI(request, svci);
 
         form.setCalSvcI(svci);
 
         cb.in(true);
 
 
       } catch (CalFacadeException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new CalFacadeException(t);
       }
     }
 
     BwUser u = svci.getUser();
 
     form.assignUserVO((BwUser)u.clone());
 
     if (publicAdmin) {
       form.assignCurrentAdminUser(u.getAccount());
     }
 
     return true;
   }
 
   /* * This method determines the access rights of the current user based on
    * their assigned roles. There are two sections to this which appear to do
    * the same thing.
    *
    * <p>They are there because at some time servlet containers (jetty for one)
    * appeared to be broken. Role mapping did not appear to work reliably.
    * This seems to have something to do with jetty doing internal redirects
    * to handle login. In the process it seems to lose the appropriate servlet
    * context and with it the mapping of roles.
    *
    * @param req        HttpServletRequest
    * @param messages   MessageResources
    * @return int access
    * @throws CalFacadeException
    * /
   private int getAccess(HttpServletRequest req,
                         MessageResources messages) throws CalFacadeException {
     int access = 0;
 
     /** This form works with broken containers.
      * /
     if (req.isUserInRole(
           getMessages().getMessage("org.bedework.role.admin"))) {
       access += UserAuth.superUser;
     }
 
     if (req.isUserInRole(
           getMessages().getMessage("org.bedework.role.contentadmin"))) {
       access += UserAuth.contentAdminUser;
     }
 
     if (req.isUserInRole(
           getMessages().getMessage("org.bedework.role.alert"))) {
       access += UserAuth.alertUser;
     }
 
     if (req.isUserInRole(
           getMessages().getMessage("org.bedework.role.owner"))) {
       access += UserAuth.publicEventUser;
     }
 
     /** This is how it ought to look
     if (req.isUserInRole("admin")) {
       access += UserAuth.superUser;
     }
 
     if (req.isUserInRole("contentadmin")) {
       access += UserAuth.contentAdminUser;
     }
 
     if (req.isUserInRole("alert")) {
       access += UserAuth.alertUser;
     }
 
     if (req.isUserInRole("owner")) {
       access += UserAuth.publicEventUser;
     } * /
 
     return access;
   }*/
 
   private BwCalSuiteWrapper findCalSuite(CalSvcI svc,
                                          BwAdminGroup adg,
                                          Groups adgrps) throws Throwable {
     BwCalSuiteWrapper cs = svc.getCalSuite(adg);
     if (cs != null) {
       return cs;
     }
 
     Iterator parents = ((AdminGroups)adgrps).findGroupParents(adg).iterator();
 
     while (parents.hasNext()) {
       cs = findCalSuite(svc, (BwAdminGroup)parents.next(), adgrps);
       if (cs != null) {
         return cs;
       }
     }
 
     return null;
   }
 
   private void checkRefresh(BwActionFormBase form) {
     if (!form.isRefreshNeeded()){
       try {
         if (!form.fetchSvci().refreshNeeded()) {
           return;
         }
       } catch (Throwable t) {
         // Not much we can do here
         form.getErr().emit(t);
         return;
       }
     }
 
     form.refreshView();
     form.setRefreshNeeded(false);
   }
 
   /** get an env object initialised appropriately for our usage.
    *
    * @param request    HttpServletRequest
    * @param frm
    * @return CalEnv object - also implanted in form.
    * @throws Throwable
    */
   private CalEnv getEnv(HttpServletRequest request,
                         BwActionFormBase frm) throws Throwable {
     CalEnv env = frm.getEnv();
     if (env != null) {
       return env;
     }
 
     HttpSession session = request.getSession();
     ServletContext sc = session.getServletContext();
 
     String appName = sc.getInitParameter("bwappname");
 
     if ((appName == null) || (appName.length() == 0)) {
       appName = "unknown-app-name";
     }
 
     String envPrefix = "org.bedework.app." + appName + ".";
 
     env = new CalEnv(envPrefix, debug);
     frm.assignEnv(env);
 
     return env;
   }
 }
