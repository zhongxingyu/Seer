 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.webcommon.event;
 
 import org.bedework.appcommon.ClientError;
 import org.bedework.appcommon.client.Client;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwXproperty;
 import org.bedework.calfacade.RecurringRetrievalMode.Rmode;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.util.misc.Util;
 import org.bedework.webcommon.BwActionFormBase;
 import org.bedework.webcommon.BwRequest;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Action to update suggest status for an event
  * <p>Request parameters:<ul>
  *      <li>  colPath    - collection href</li>.
  *      <li>  eventName  - name for event</li>.
  *      <li>  accept | reject  - only one must be present </li>.
  * </ul>
  * <p>Errors:<ul>
  *      <li>org.bedework.error.noaccess </li>
  *      <li>org.bedework.error.not.suggested - when
  *            not a suggested event</li>
  * </ul>
  */
 public class SuggestAction extends EventActionBase {
   @Override
   public int doAction(final BwRequest request,
                       final BwActionFormBase form) throws Throwable {
     final Client cl = request.getClient();
     final HttpServletResponse response = request.getResponse();
 
     /** Check access
      */
     final boolean publicAdmin = cl.getPublicAdmin();
     if (cl.isGuest() || !publicAdmin || !form.getCurUserApproverUser()) {
       response.setStatus(HttpServletResponse.SC_FORBIDDEN);
       return forwardNull;
     }
 
    final EventInfo ei = findEvent(request, Rmode.overrides);
 
     if (ei == null) {
       // Do nothing
       response.setStatus(HttpServletResponse.SC_NOT_FOUND);
       return forwardNull;
     }
 
     final BwEvent ev = ei.getEvent();
 
     final boolean accept = request.present("accept");
     final boolean reject = request.present("reject");
 
     if ((reject && accept) || (!reject && !accept)) {
       form.getErr().emit(ClientError.badRequest);
       response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
       return forwardNull;
     }
 
     final String csHref = form.getCurrentCalSuite().getGroup().getPrincipalRef();
 
     final List<BwXproperty> props =
             ev.getXproperties(BwXproperty.bedeworkSuggestedTo);
 
     if (Util.isEmpty(props)) {
       form.getErr().emit(ClientError.notSuggested);
       response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
       return forwardNull;
     }
 
     BwXproperty theProp = null;
 
     for (final BwXproperty prop: props) {
       if (prop.getValue().substring(2).equals(csHref)) {
         theProp = prop;
         break;
       }
     }
 
     if (theProp == null) {
       form.getErr().emit(ClientError.notSuggested);
       response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
       return forwardNull;
     }
 
     String newStatus;
 
     if (accept) {
       newStatus = "A";
     } else {
       newStatus = "R";
     }
 
     newStatus+= ":" + csHref;
 
     if (newStatus.equals(theProp.getValue())) {
       response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
       return forwardNull;
     }
 
     theProp.setValue(newStatus);
 
     final Set<String> catuids = cl.getPreferences().getDefaultCategoryUids();
 
     for (final String uid: catuids) {
      final BwCategory cat = cl.getPersistentCategory(uid);
 
       if (cat != null) {
         if (accept) {
           ev.addCategory(cat);
         } else {
           ev.removeCategory(cat);
         }
       }
     }
 
     try {
       cl.updateEvent(ei, true, null);
     } catch (final CalFacadeException cfe) {
       cl.rollback();
       throw cfe;
     }
 
     response.setStatus(HttpServletResponse.SC_OK);
     return forwardNull;
   }
 }
