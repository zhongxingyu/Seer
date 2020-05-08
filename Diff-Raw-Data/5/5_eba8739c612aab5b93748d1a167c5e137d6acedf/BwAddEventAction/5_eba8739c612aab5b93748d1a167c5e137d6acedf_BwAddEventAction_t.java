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
 
 package org.bedework.webclient;
 
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.CalFacadeDefs;
 import org.bedework.calsvci.CalSvcI;
 import org.bedework.webcommon.BwWebUtil;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Action to add an Event.
  * <p>No specific request parameters. Form should contain an initialised
  * BwEvent object.
  * <p>Forwards to:<ul>
  *      <li>"doNothing"    input error or we want to ignore the request.</li>
  *      <li>"success"      added ok.</li>
  * </ul>
  */
 public class BwAddEventAction extends BwCalAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webclient.BwCalAbstractAction#doAction(javax.servlet.http.HttpServletRequest, org.bedework.webclient.BwActionForm)
    */
   public String doAction(HttpServletRequest request,
                          BwActionForm form) throws Throwable {
     if (form.getGuest()) {
       // Just ignore this
       return "doNothing";
     }
 
     BwEvent ev = form.getNewEvent();
 
     if (ev == null) {
       return "doNothing";
     }
 
     CalSvcI svci = form.fetchSvci();
 
    if (ev.getCalendar() == null) {
      // Set the default calendar
      ev.setCalendar(svci.getPreferredCalendar());
    }

     if (!form.getEventDates().updateEvent(ev, svci.getTimezones()) ||
         !BwWebUtil.validateEvent(svci, ev, false, //  descriptionRequired
                                  form.getErr())) {
       return "doNothing";
     }
 
     /** We might also need to add a location - for private events we don't
      * require a location
      */
 
     BwLocation loc = null;
 
     if (form.getLocationId() != CalFacadeDefs.defaultLocationId) {
       loc = svci.getLocation(form.getLocationId());
     }
 
     if (loc == null) {
       loc = form.getNewLocation();
     }
 
     if (loc.getAddress() != null) {
       /* Location assigned */
 
       BwLocation l = svci.ensureLocationExists(loc);
 
       boolean added = true;
       if (l != null) {
         loc = l;
         added = false;
       }
 
       ev.setLocation(loc);
 
       if (added) {
         form.getMsg().emit("org.bedework.client.message.locations.added", 1);
       }
     } else {
       ev.setLocation(null);
     }
 
     svci.addEvent(ev.getCalendar(), ev, null);
 
     form.resetNewEvent();
     form.resetNewLocation();
     form.getEventDates().setNewEvent(form.getNewEvent(), svci.getTimezones());
 
     form.getMsg().emit("org.bedework.message.added.events", 1);
 
     form.refreshIsNeeded();
 
     return "success";
   }
 }
