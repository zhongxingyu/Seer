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
 package org.bedework.webcommon;
 
import org.bedework.appcommon.CheckData;
 import org.bedework.appcommon.ClientError;
import org.bedework.appcommon.MyCalendarVO;
 import org.bedework.appcommon.client.Client;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwFilterDef;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.exc.ValidationError;
 import org.bedework.calfacade.svc.EventInfo;
 import org.bedework.calfacade.util.ChangeTable;
 import org.bedework.util.calendar.IcalDefs;
 import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
 import org.bedework.util.calendar.ScheduleMethods;
 import org.bedework.util.misc.Util;
 import org.bedework.util.struts.Request;
 import org.bedework.util.timezones.Timezones;
 
 import org.apache.struts.action.Action;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 /**
  * @author douglm
  *
  */
 public class BwRequest extends Request {
   private BwSession sess;
 
   private Request req;
 
   private BwModule module;
 
   private Client cl;
 
   /** client stored in request */
   public final static String embeddedClientName = "bw_embedded_client";
 
   /** event state stored in request */
   public final static String eventStateName = "bw_event_state";
 
   /** module state stored in request */
   public final static String moduleStateName = "bw_module_state";
 
   /** auth user list stored in session */
   public final static String bwAuthUsersListName = "bw_auth_users";
 
   /** admin group info list stored in session */
   public final static String bwAdminGroupsInfoName = "bw_admin_groups";
 
   /** Search pars stored in session */
   public final static String bwSearchParamsName = "bw_search_params";
 
   /** Search result stored in session */
   public final static String bwSearchResultName = "bw_search_result";
 
   /** Formatted entity list stored in session */
   public final static String bwSearchListName = "bw_search_list";
 
   /** filter list stored in session */
   public final static String bwFiltersListName = "bw_filters_list";
 
   /** collection list stored in session */
   public final static String bwCollectionListName = "bw_collection_list";
 
   /** public collection list stored in session */
   public final static String bwPublicCollectionListName = "bw_public_collection_list";
 
   /** user collection list stored in session */
   public final static String bwUserCollectionListName = "bw_user_collection_list";
 
   /** add content collection list stored in session */
   public final static String bwAddContentCollectionListName = "bw_addcontent_collection_list";
 
   /** preferences stored in session */
   public final static String bwPreferencesName = "bw_preferences";
 
   /** view list stored in session */
   public final static String bwViewsListName = "bw_views_list";
 
   /** category list stored in session */
   public final static String bwCategoriesListName = "bw_categories_list";
 
   /** default category list stored in session */
   public final static String bwDefaultCategoriesListName = "bw_default_categories_list";
 
   /** editable category list stored in session */
   public final static String bwEditableCategoriesListName = "bw_editable_categories_list";
 
   /** preferred category list stored in session */
   public final static String bwPreferredCategoriesListName = "bw_preferred_categories_list";
 
   /** contact list stored in session */
   public final static String bwContactsListName = "bw_contacts_list";
 
   /** editable contact list stored in session */
   public final static String bwEditableContactsListName = "bw_editable_contacts_list";
 
   /** preferred contact list stored in session */
   public final static String bwPreferredContactsListName = "bw_preferred_contacts_list";
 
   /** location list stored in session */
   public final static String bwLocationsListName = "bw_locations_list";
 
   /** preferred location list stored in session */
   public final static String bwPreferredLocationsListName = "bw_preferred_locations_list";
 
   /** editable location list stored in session */
   public final static String bwEditableLocationsListName = "bw_editable_locations_list";
 
   /**
    * @param request
    * @param sess
    * @param action
    */
   public BwRequest(final Request request,
                    final BwSession sess,
                    final Action action) {
     super(request.getRequest(), request.getResponse(), request.getForm(),
           action, request.getMapping());
     this.req = request;
     this.sess = sess;
   }
 
   /**
    * @return BwSession
    */
   public BwSession getSess() {
     return sess;
   }
 
   public BwModule getModule() {
     if (module == null) {
       module = getBwForm().fetchModule(req.getModuleName());
       request.setAttribute(moduleStateName, module.getState());
     }
 
     return module;
   }
 
   public Client getClient() {
     if (cl == null) {
       cl = getBwForm().fetchClient(req.getModuleName());
       request.setAttribute(embeddedClientName, cl);
     }
 
     return cl;
   }
 
   /**
    * @return BwActionFormBase
    */
   public BwActionFormBase getBwForm() {
     return (BwActionFormBase)getForm();
   }
 
   /**
    * Signal we need to refresh the module state
    */
   public void refresh() {
     getModule().getState().setRefresh(true);
   }
 
   /**
    * @return true for "copy" present
    * @throws Throwable
    */
   public boolean hasCopy() throws Throwable {
     return present("copy");
   }
 
   /**
    * @return true for "delete" present
    * @throws Throwable
    */
   public boolean hasDelete() throws Throwable {
     return present("delete");
   }
 
   /**
    * @return true for "export" present
    * @throws Throwable
    */
   public boolean hasExport() throws Throwable {
     return present("export");
   }
 
   /**
    * <p>Request parameters are:<ul>
    *      <li>"entityType"              Absent for an event, otherwise
    *                                    event | task | journal</li>
    * </ul>
    *
    * @return int
    * @throws Throwable
    */
   public int getEntityType() throws Throwable {
     String s = getReqPar("entityType");
 
     if ((s == null) || "event".equals(s)) {
       return IcalDefs.entityTypeEvent;
     }
 
     if ("task".equals(s)) {
       return IcalDefs.entityTypeTodo;
     }
 
     if ("journal".equals(s)) {
       return IcalDefs.entityTypeJournal;
     }
 
     if ("freebusy".equals(s)) {
       return IcalDefs.entityTypeFreeAndBusy;
     }
 
     getErr().emit(ValidationError.invalidEntityType, s);
     return 0;
   }
 
   /** If filterName or fexpr is specified will return a parsed filter definition
    *
    * @return BwFilterDef or null
    * @throws Throwable
    */
   public BwFilterDef getFilterDef() throws Throwable {
     Client cl = getClient();
     String name = getReqPar("filterName");
     String fexpr = getReqPar("fexpr");
 
     BwFilterDef fd = null;
 
     if ((name == null) && (fexpr == null)) {
       return null;
     }
 
     if (name != null) {
       fd = cl.getFilter(name);
       if (fd == null) {
         form.getErr().emit(ClientError.unknownFilter, name);
         return null;
       }
     } else {
       fd = new BwFilterDef();
       fd.setDefinition(fexpr);
     }
 
     if (fd.getFilters() == null) {
       try {
         cl.parseFilter(fd);
       } catch (CalFacadeException cfe) {
         getErr().emit(cfe.getMessage(), cfe.getExtra());
         return null;
       }
     }
 
     return fd;
   }
 
   /**
    * @param evDateOnly  true if event says date only values
    * @return Collection of BwDateTime or null
    * @throws Throwable
    */
   public Collection<BwDateTime> getRdates(final boolean evDateOnly) throws Throwable {
     return getRExdates(true, evDateOnly);
   }
 
   /**
    * @param evDateOnly  true if event says date only values
    * @return (possibly empty) Collection of BwDateTime, never null
    * @throws Throwable
    */
   public Collection<BwDateTime> getExdates(final boolean evDateOnly) throws Throwable {
     return getRExdates(false, evDateOnly);
   }
 
   private Collection<BwDateTime> getRExdates(final boolean rdates,
                                              final boolean evDateOnly) throws Throwable {
     String reqPar;
     String token = "DATE\t";
     Collection<BwDateTime> bwdts = new ArrayList<BwDateTime>();
 
     if (rdates) {
       reqPar = "rdates";
     } else {
       reqPar = "exdates";
     }
 
     String dtsPar = getReqPar(reqPar);
     if (dtsPar == null) {
       return bwdts;
     }
 
     String[] dts = dtsPar.split(token);
 
     for (String dtVal: dts) {
       if ((dtVal == null) || (dtVal.length() == 0)) {
         continue;
       }
 
       String[] dtParts = dtVal.split("\t");
 
       /* date, time, tzid */
 
       StringBuilder dtm = new StringBuilder(dtParts[0]);
       boolean dateOnly = true;
 
       if (!evDateOnly && (Util.checkNull(dtParts[1]) != null)) {
         dtm.append("T");
         dtm.append(dtParts[1]);
         dtm.append("00");  // Seconds
 
         /*
          * if (UTC) {
              dtm.append("Z");
            }
          *
          */
 
         dateOnly = false;
       }
 
       String tzid = null;
 
       if (!evDateOnly && (dtParts.length > 2)) {
         tzid = Util.checkNull(dtParts[2]);
       }
 
       bwdts.add(BwDateTime.makeBwDateTime(dateOnly, dtm.toString(), tzid));
     }
 
     return bwdts;
   }
 
   /** This often appears as the request parameter specifying the date for an
    * action. Always YYYYMMDD format. Returns current date if not in request
    *
    * @return String date in YYYYMMDD format
    */
   public String getDate() {
     String date = getReqPar("date");
 
    if (!CheckData.checkDateString(date)) {
      return new MyCalendarVO(new Date(System.currentTimeMillis())).getDateDigits();
    }
 
     return date;
   }
 
   /**
    * @return index
    * @throws Throwable
    */
   public int getViewTypeI() throws Throwable {
     return getIntReqPar("viewTypeI", -1);
   }
 
   /** Get date/time object
    *
    * @param dtPar
    * @param tzidPar
    * @return date/time
    * @throws Throwable
    */
   public BwDateTime getDateTime(final String dtPar,
                                 final String tzidPar) throws Throwable {
     String dt = getReqPar(dtPar);
     if (dt == null) {
       return null;
     }
 
     String tzid  = getReqPar(tzidPar);
     if (tzid == null) {
       tzid = Timezones.getThreadDefaultTzid();
     }
 
     return BwDateTime.makeBwDateTime(dt.indexOf("T") < 0, dt, tzid);
   }
 
   /** Get calendar identified by newCalPath. Emits error message for invalid path
    * Returns null for bad or missing calendar.
    *
    * @param required boolean true if we require a calendar.
    * @return calendar or null for invalid path.
    * @throws Throwable
    */
   public BwCalendar getNewCal(final boolean required) throws Throwable {
     String newCalPath = getReqPar("newCalPath");
     BwCalendar newCal = null;
 
     if (newCalPath == null) {
       if (required) {
         getErr().emit(ValidationError.missingCalendar);
       }
       return null;
     }
 
     newCal = getClient().getCollection(newCalPath);
     if (newCal == null) {
       getErr().emit(ClientError.unknownCalendar, newCalPath);
       return null;
     }
 
     return newCal;
   }
 
   /** Set the event calendar based on request parameters. If newCalPath
    * is specified we use the named calendar as the event calendar.
    *
    * <p>If newCalPath is not specified and no calendar was already set in the
    * event, we fail with an error.
    *
    * <p>If a calendar was already set in the event, this action will only
    * change that calendar if newCalPath is specified. It will not
    * reset the calendar to the default.
    *
    * @param ei
    * @param changes
    * @return boolean false if none or errors
    * @throws Throwable
    */
   public boolean setEventCalendar(final EventInfo ei,
                                   final ChangeTable changes) throws Throwable {
     BwEvent ev = ei.getEvent();
     BwCalendar cal = getNewCal(false);
 
     if (form.getErrorsEmitted()) {
       return false;
     }
 
     if ((cal == null) && (ev.getColPath() == null)) {
       getErr().emit(ValidationError.missingCalendar);
       return false;
     }
 
     if (cal != null) {
       changes.changed(PropertyInfoIndex.COLLECTION,
                       ev.getColPath(),
                       cal.getPath());
       ev.setColPath(cal.getPath());
     }
 
     return true;
   }
 
   /** Get calendars identified by multi-valued parameter calPath.
    *
    * @return Collection<BwCalendar> (possibly empty).
    * @throws Throwable
    */
   public Collection<BwCalendar> getCalendars() throws Throwable {
     Collection<String> calPaths = getReqPars("calPath");
     Collection<BwCalendar> cals = new ArrayList<>();
 
     if (calPaths == null) {
       return cals;
     }
 
     for (String calPath: calPaths) {
       BwCalendar cal = getClient().getCollection(calPath);
 
       if (cal != null) {
         cals.add(cal);
       }
     }
 
     return cals;
   }
 
   /** Get collection identified by single-valued parameter colHref.
    *
    * @param required
    * @return BwCalendar or null.
    * @throws Throwable
    */
   public BwCalendar getCollection(final boolean required) throws Throwable {
     return getCalendar("colHref", required);
   }
 
   /** Get calendar user address identified by single-valued parameter cua.
    *
    * @param required
    * @return String or null.
    * @throws Throwable
    */
   public String getCua(final boolean required) throws Throwable {
     String cua = getReqPar("cua");
 
     if (cua == null) {
       getErr().emit(ClientError.badRequest, "Missing cua");
       return null;
     }
 
     return cua;
   }
 
   /** Get calendar identified by single-valued parameter calPath.
    *
    * @param required
    * @return BwCalendar or null.
    * @throws Throwable
    */
   public BwCalendar getCalendar(final boolean required) throws Throwable {
     if (present("calPath")) {               // TODO - drop this
       return getCalendar("calPath", required);
     }
     return getCalendar("colPath", required);
   }
 
   /** Get calendar identified by single-valued request parameter specified.
    *
    * @param reqParName
    * @param required
    * @return BwCalendar or null.
    * @throws Throwable
    */
   public BwCalendar getCalendar(final String reqParName,
                                 final boolean required) throws Throwable {
     String calPath = getReqPar(reqParName);
 
     if (calPath == null) {
       if (required) {
         // bogus request
         getErr().emit(ValidationError.missingCalendarPath);
       }
       return null;
     }
 
     BwCalendar cal = getClient().getCollection(calPath);
 
     if (cal == null) {
       getErr().emit(ClientError.unknownCalendar, calPath);
       return null;
     }
 
     return cal;
   }
 
   /**
    * <p>Request parameters are:<ul>
    *      <li>"schedule=none"           Not a scheduling request</li>
    *      <li>"schedule=request"        Send a scheduling request</li>
    *      <li>"schedule=reconfirm"      Ask for reconfirmation</li>
    *      <li>"schedule=publish"        Publish an event</li>
    * </ul>
    *
    * @return int schedule method
    * @throws Throwable
    */
   public int getSchedule() throws Throwable {
     String schedStr = getReqPar("schedule");
 
     if (schedStr == null) {
       return ScheduleMethods.methodTypeNone;
     }
 
     if ("none".equals(schedStr)) {
       return ScheduleMethods.methodTypeNone;
     }
 
     if ("request".equals(schedStr)) {
       return ScheduleMethods.methodTypeRequest;
     }
 
     if ("reconfirm".equals(schedStr)) {
       // Use refresh to indicate we want to reconfirm
       return ScheduleMethods.methodTypeRefresh;
     }
 
     if ("publish".equals(schedStr)) {
       return ScheduleMethods.methodTypePublish;
     }
 
     getErr().emit(ValidationError.invalidSchedMethod, schedStr);
     return ScheduleMethods.methodTypeNone;
   }
 
   /**
    * @param forExport
    * @return EventKey
    * @throws Throwable
    */
   public EventKey makeEventKey(boolean forExport) throws Throwable {
     String calPath = getReqPar("calPath");
 
     if (calPath == null) {
       // bogus request
       getErr().emit(ValidationError.missingCalendarPath);
       return null;
     }
 
     BwCalendar cal = getClient().getCollection(calPath);
 
     if (cal == null) {
       // Assume no access
       getErr().emit(ClientError.noAccess);
       return null;
     }
 
     String guid = getReqPar("guid");
     String rid = null;
     String eventName = getReqPar("eventName");
     EventKey ekey;
 
     if (guid != null) {
       if (getReqPar("master") != null) {
         forExport = true;
       }
 
       if (!forExport) {
         rid = getReqPar("recurrenceId");
       }
 
       ekey = new EventKey(calPath, guid, rid, forExport);
     } else if (eventName != null) {
       ekey = new EventKey(calPath, eventName, forExport);
     } else {
       getErr().emit(ClientError.missingEventKeyFields);
       return null;
     }
 
     return ekey;
   }
 }
