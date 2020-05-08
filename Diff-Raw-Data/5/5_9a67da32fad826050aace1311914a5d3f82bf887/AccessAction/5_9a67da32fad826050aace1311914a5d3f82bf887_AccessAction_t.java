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
 package org.bedework.webcommon.access;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.svc.BwCalSuite;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwSession;
 
 import edu.rpi.cmt.access.Ace;
 import edu.rpi.cmt.access.PrivilegeDefs;
 import edu.rpi.cmt.access.Privileges;
 
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Action to update access rights to an entity. Note this will change as we
  * develop the user interface (and the access methods).
  *
  * Currently this provides a basic level of access control modification.
  *
  * <p>Request parameters:<ul>
  *      <li>  calPath alone:         path (or url) of calendar or...</li>.
  *      <li>  calPath+guid+recurid:  event or ...</li>.
  *      <li>  calSuiteName:          name of calendar suite</li>.
  *      <li>  how:                   concatenated String of desired access rights
  *                               @see edu.rpi.cmt.access.PrivilegeDefs </li>.
  *      <li>  whoType:               user (default), group</li>.
  *      <li>  who:                   name of principal - default to owner</li>.
  * </ul>
  * <p>Forwards to:<ul>
  *      <li>"doNothing"    input error or we want to ignore the request.</li>
  *      <li>"notFound"     entity not found.</li>
  *      <li>"edit"         to edit the event.</li>
  *      <li>"error"        input error - correct and retry.</li>
  *      <li>"success"      OK.</li>
  * </ul>
  *
  * <p>If no period is given return this week. If no interval and intunit is
  * supplied default to 1 hour intervals during the workday.
  *
  *  @author Mike Douglass   douglm@rpi.edu
  */
 public class AccessAction extends BwAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webclient.BwCalAbstractAction#doAction(javax.servlet.http.HttpServletRequest, org.bedework.webclient.BwActionForm)
    */
   public String doAction(HttpServletRequest request,
                          HttpServletResponse response,
                          BwSession sess,
                          BwActionFormBase form) throws Throwable {
     if (form.getGuest()) {
       return "notFound";
     }
 
     CalSvcI svci = form.fetchSvci();
     BwCalendar cal = null;
     BwCalSuite calSuite = null;
     EventInfo ei = null;
     BwEvent ev = null;
 
     String rpar = getReqPar(request, "guid");
     String calPath = getReqPar(request, "calPath");
 
     if (rpar != null) {
       // Assume event
       ei = findEvent(request, form);
       if (ei == null) {
         // Do nothing
         form.getErr().emit("org.bedework.client.error.nosuchevent");
         return "doNothing";
       }
       ev = ei.getEvent();
     } else if (calPath != null) {
       // calendar
       cal = svci.getCalendar(calPath);
       if (cal == null) {
         form.getErr().emit("org.bedework.client.error.nosuchcalendar", calPath);
         return "notFound";
       }
     } else {
       String calSuiteName = getReqPar(request, "calSuiteName");
 
       if (calSuiteName == null) {
         // bogus request
         return "notFound";
       }
 
       calSuite = svci.getCalSuite(calSuiteName);
       if (calSuite == null) {
         form.getErr().emit("org.bedework.client.error.nosuchcalendarsuite", calSuite);
         return "notFound";
       }
     }
 
     String whoTypeStr = request.getParameter("whoType");
     int whoType = -1;
     boolean needWho = false;
 
     if (whoTypeStr == null) {
       whoType = Ace.whoTypeUser;
       needWho = true;
     } else if (whoTypeStr.equals("owner")) {
       whoType = Ace.whoTypeOwner;
     } else if (whoTypeStr.equals("user")) {
       whoType = Ace.whoTypeUser;
       needWho = true;
     } else if (whoTypeStr.equals("group")) {
       whoType = Ace.whoTypeGroup;
       needWho = true;
       //form.getErr().emit("org.bedework.client.error.unimplemented");
       //return "error";
     } else if (whoTypeStr.equals("unauth")) {
       whoType = Ace.whoTypeUnauthenticated;
     } else if (whoTypeStr.equals("other")) {
       whoType = Ace.whoTypeOther;
     } else {
       form.getErr().emit("org.bedework.client.error.badwhotype");
       return "error";
     }
 
     String who = request.getParameter("who");
 
     /*
     if (who != null) {
       BwUser user = svci.findUser(who);
       if (user == null) {
         form.getErr().emit("org.bedework.client.error.usernotfound");
         return "notFound";
       }
     } else {
       who = null;
     }
     */
     if (needWho && (who == null)) {
       form.getErr().emit("org.bedework.client.error.missingwho");
       return "error";
     }
 
     ArrayList aces = new ArrayList();
 
     String how = getReqPar(request, "how");
 
     if (how == null) {
       form.getErr().emit("org.bedework.client.error.nohowaccess");
       return "error";
     }
 
     char[] howchs = how.toCharArray();
 
     for (int hi = 0; hi < howchs.length; hi++) {
       char howch = howchs[hi];
       boolean found = false;
 
       for (int pi = 0; pi <= PrivilegeDefs.privMaxType; pi++) {
         if (howch == PrivilegeDefs.privEncoding[pi]) {
           aces.add(new Ace(who, false, whoType,
                            Privileges.makePriv(pi)));
           found = true;
           break;
         }
       }
 
       if (!found) {
         form.getErr().emit("org.bedework.client.error.badhow");
         return "error";
       }
     }
 
 
     if (ev != null) {
       svci.changeAccess(ev, aces);

      return refreshEvent(fetchEvent(ev, form), request, form);
     } else if (calSuite != null) {
       svci.changeAccess(calSuite, aces);
     } else {
       svci.changeAccess(cal, aces);
       //svci.updateCalendar(cal);
     }
 
    return "success";
   }
 }
