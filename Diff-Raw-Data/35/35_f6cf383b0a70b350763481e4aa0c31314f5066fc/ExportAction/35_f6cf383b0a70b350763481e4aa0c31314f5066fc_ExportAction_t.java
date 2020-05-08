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
 
 package org.bedework.webcommon.misc;
 
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.CalFacadeDefs;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.icalendar.IcalTranslator;
 import org.bedework.webcommon.BwAbstractAction;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwSession;
 import org.bedework.webcommon.EventDates;
 
 import edu.rpi.sss.util.Util;
 
 import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Method;
 
 import java.util.Collection;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Action to export an icalendar file. This might be better done as a custom tag which
  * could write directly to the response. We could be generating something big here.
  *
  * <p>Request parameters<ul>
  *      <li>"guid"           guid of event.</li>
  *      <li>"recurrenceId"   recurrence id of event (optional)... or</li>
  *      <li>"calid"          Id of calendar to export.</li>
  *      <li>"sresult"        Any value - export last search result.</li>
  *      <li>"expand"         true/false - default is to not expand recurrences.</li>
  * </ul>
  * <p>Forwards to:<ul>
  *      <li>"notFound"     no event was found.</li>
  *      <li>"success"      exported ok.</li>
  * </ul>
  */
 public class ExportAction extends BwAbstractAction {
   public String doAction(HttpServletRequest request,
                          HttpServletResponse response,
                          BwSession sess,
                          BwActionFormBase form) throws Throwable {
     String guid = Util.checkNull(request.getParameter("guid"));
     int calid = getIntReqPar(request, "calid", -1);
     int subid = getIntReqPar(request, "subid", -1);
     CalSvcI svci = form.fetchSvci();
 
     EventInfo ev = null;
     Collection evs = null;
 
     if (guid != null) {
       if (debug) {
         debugMsg("Export event by guid");
       }
 
       ev = findEvent(request, form);
 
       if (ev == null) {
         return "doNothing";
       }
       if (debug) {
         debugMsg("Got event by guid");
       }
     } else {
       BwSubscription sub = null;
       if (calid >= 0) {
         BwCalendar cal = svci.getCalendar(calid);
         if (cal == null) {
           return "notFound";
         }
 
         sub = new BwSubscription();
         sub.setName(cal.getName());
         sub.setDisplay(true);
         sub.setInternalSubscription(true);
         sub.setCalendar(cal);
       } else if (subid >= 0) {
         sub = svci.getSubscription(subid);
 
         if (sub == null) {
           return "notFound";
         }
       }
 
       EventDates edates = form.getEventDates();
 
       String expandStr = request.getParameter("expand");
       int expand = CalFacadeDefs.retrieveRecurMaster;
       if (expandStr != null) {
         if ("true".equals(expandStr)) {
           expand = CalFacadeDefs.retrieveRecurExpanded;
         }
       }
 
       evs = svci.getEvents(sub, null,
                            edates.getStartDate().getDateTime(),
                            edates.getEndDate().getDateTime(),
                            expand);
 
       if (evs == null) {
         return "notFound";
       }
     }
 
     if (ev != null) {
       evs = new Vector();
 
       evs.add(ev.getEvent());
       evs.addAll(ev.getOverrides());
     }
 
     IcalTranslator ical = new IcalTranslator(svci.getIcalCallback());
 
     Calendar vcal = ical.toIcal(evs);
 
    vcal.getProperties().add(Method.PUBLISH);

     form.setVcal(vcal);
 
     return "success";
   }
 }
