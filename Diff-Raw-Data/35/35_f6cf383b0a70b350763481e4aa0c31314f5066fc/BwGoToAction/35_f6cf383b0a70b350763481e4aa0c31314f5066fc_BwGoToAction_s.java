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
 
 import org.bedework.appcommon.MyCalendarVO;
 import org.bedework.appcommon.TimeView;
 import org.bedework.appcommon.BedeworkDefs;
 import org.bedework.calfacade.CalFacadeUtil;
 import org.bedework.webcommon.TimeDateComponents;
 
 
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Action to go to a given date and/or set a day/week/month/year view.
  * <p>Request parameter<br>
  *      "date" date to move to.
  * <p>Optional request parameter<br>
  *      "viewType" type of view we want, day, week, etc.
  *
  */
 public class BwGoToAction extends BwCalAbstractAction {
   /* (non-Javadoc)
    * @see org.bedework.webclient.BwCalAbstractAction#doAction(javax.servlet.http.HttpServletRequest, org.bedework.webclient.BwActionForm)
    */
   public String doAction(HttpServletRequest request,
                          BwActionForm form) throws Throwable {
     gotoDateView(this, form,
                   form.getDate(),
                   form.getViewTypeI(),
                   debug);
 
     return "success";
   }
 
   /** Set the current date and/or view. The date may be null indicating we
    * should switch to a new view based on the current date.
    *
    * <p>newViewTypeI may be less than 0 indicating we stay with the current
    * view but switch to a new date.
    *
    * @param action
    * @param form         UWCalActionForm
    * @param date         String yyyymmdd date or null
    * @param newViewTypeI new view index or -1
    * @param debug
    * @throws Throwable
    */
   public static void gotoDateView(BwCalAbstractAction action,
                                   BwActionForm form,
                                   String date,
                                   int newViewTypeI,
                                   boolean debug) throws Throwable {
     /* We get a new view if either the date changed or the view changed.
      */
     boolean newView = false;
 
     if (debug) {
       action.logIt("ViewTypeI=" + newViewTypeI);
     }
 
     MyCalendarVO dt;
     TimeView tv = form.getCurTimeView();
 
     if (newViewTypeI == BedeworkDefs.todayView) {
       // dt = new MyCalendarVO(new Date(System.currentTimeMillis()));
       dt = new MyCalendarVO();
       newView = true;
       newViewTypeI = BedeworkDefs.dayView;
     } else if (date == null) {
       if (newViewTypeI == BedeworkDefs.dayView) {
         // selected specific day to display from personal event entry screen.
         dt = new MyCalendarVO(form.getViewStartDate().getDateTime());
         newView = true;
       } else {
         if (debug) {
           action.logIt("No date supplied: go with current date");
         }
 
         // Just stay here
         dt = tv.getCurDay();
       }
     } else {
       if (debug) {
         action.logIt("Date=" + date + ": go with that");
       }
 
       dt = new MyCalendarVO(CalFacadeUtil.fromISODate(date));
       newView = true;
     }
 
     if ((newViewTypeI >= 0) &&
         (newViewTypeI != form.getCurViewPeriod())) {
       // Change of view
       newView = true;
     }
 
     if (newView && (newViewTypeI < 0)) {
       newViewTypeI = form.getCurViewPeriod();
       if (newViewTypeI < 0) {
         newViewTypeI = BedeworkDefs.defaultView;
       }
     }
 
     TimeDateComponents viewStart = form.getViewStartDate();
 
     if (!newView) {
       /* See if we were given an explicit date as view start date components.
          If so we'll set a new view of the same period as the current.
        */
      String vsdate = viewStart.getDateTime().getDate().substring(0, 8);
 
       if (!(vsdate.equals(form.getCurTimeView().getFirstDay().getDateDigits()))) {
         newView = true;
         newViewTypeI = form.getCurViewPeriod();
         dt = new MyCalendarVO(CalFacadeUtil.fromISODate(vsdate));
       }
     }
 
     if (newView) {
       form.setCurViewPeriod(newViewTypeI);
       form.setViewMcDate(dt);
       form.refreshIsNeeded();
     }
 
     tv = form.getCurTimeView();
     // dt = tv.getCurDay();
 
     /** Set first day, month and year
      */
 
     MyCalendarVO firstDay = tv.getFirstDay();
 
     viewStart.setDay(firstDay.getTwoDigitDay());
     viewStart.setMonth(firstDay.getTwoDigitMonth());
     viewStart.setYear(firstDay.getFourDigitYear());
 
     form.getEventStartDate().setDateTime(tv.getCurDay().getTime());
     form.getEventEndDate().setDateTime(tv.getCurDay().getTime());
   }
 }
