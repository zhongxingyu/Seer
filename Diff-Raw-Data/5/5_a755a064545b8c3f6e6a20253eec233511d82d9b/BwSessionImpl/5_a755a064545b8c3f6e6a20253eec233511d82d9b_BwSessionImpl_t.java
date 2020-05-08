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
 
 import org.bedework.appcommon.BedeworkDefs;
 import org.bedework.appcommon.ClientError;
 import org.bedework.appcommon.CollectionCollator;
 import org.bedework.appcommon.ConfigCommon;
 import org.bedework.appcommon.DayView;
 import org.bedework.appcommon.MonthView;
 import org.bedework.appcommon.MyCalendarVO;
 import org.bedework.appcommon.TimeView;
 import org.bedework.appcommon.WeekView;
 import org.bedework.appcommon.YearView;
 import org.bedework.appcommon.client.Client;
 import org.bedework.caldav.util.filter.FilterBase;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwCategory;
 import org.bedework.calfacade.BwContact;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwFilterDef;
 import org.bedework.calfacade.BwLocation;
 import org.bedework.calfacade.BwPrincipal;
 import org.bedework.calfacade.BwProperty;
 import org.bedework.calfacade.configs.AuthProperties;
 import org.bedework.calfacade.configs.SystemProperties;
 import org.bedework.calfacade.exc.CalFacadeException;
 import org.bedework.calfacade.svc.prefs.BwAuthUserPrefs;
 import org.bedework.util.misc.Util;
 import org.bedework.util.struts.Request;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /** This ought to be made pluggable. We need a session factory which uses
  * CalEnv to figure out which implementation to use.
  *
  * <p>This class represents a session for the Bedework web interface.
  * Some user state will be retained here.
  * We also provide a number of methods which act as the interface between
  * the web world and the calendar world.
  *
  * @author Mike Douglass   douglm     rpi.edu
  */
 public class BwSessionImpl implements BwSession {
   private static final String refreshTimeAttr = "bw_refresh_time";
   private static final long refreshRate = 1 * 60 * 1000;
 
   /** Not completely valid in the j2ee world but it's only used to count sessions.
    */
   private static class Counts {
     long totalSessions = 0;
   }
 
   private final boolean isPortlet;
 
   private final ConfigCommon config;
 
   private boolean publicAdmin;
 
   private BwAuthUserPrefs curAuthUserPrefs;
 
   private static volatile HashMap<String, Counts> countsMap =
     new HashMap<String, Counts>();
   private long sessionNum = 0;
 
   /** The current user - null for guest
    */
   private String user;
 
   /** The application root
    */
   //private String appRoot;
 
   /** The application name
    */
   private String appName;
 
   private AuthProperties authpars;
 
   private SystemProperties syspars;
 
   private transient CollectionCollator<BwContact> contactCollator;
   private transient CollectionCollator<BwCategory> categoryCollator;
   private transient CollectionCollator<BwLocation> locationCollator;
 
   /** Constructor for a Session
    *
    * @param isPortlet
    * @param config
    * @param user       String user id
    * @param appName    String identifying particular application
    * @throws Throwable
    */
   public BwSessionImpl(final boolean isPortlet,
                        final ConfigCommon config,
                        final String user,
                        final String appName) throws Throwable {
     this.isPortlet = isPortlet;
     this.config = config;
     this.user = user;
     this.appName = appName;
 
     publicAdmin = config.getPublicAdmin();
 
     setSessionNum(appName);
   }
 
   @Override
   public void reset(final Request req) {
     req.setSessionAttr(refreshTimeAttr, new Long(0));
   }
 
   /* NOTE: This is NOT intended to turn a relative URL into an
   absolute URL. It is a convenience for development which turns a
   not fully specified url into a url referring to the server.
 
   This will not work if they are treated as relative to the servlet.
 
   In production mode, the appRoot will normally be fully specified to a
   different web server.
 * /
   private String prefixUri(final String schemeHostPort,
                            final String val) {
     if (val.toLowerCase().startsWith("http")) {
       return val;
     }
 
     StringBuilder sb = new StringBuilder(schemeHostPort);
 
     if (!val.startsWith("/")) {
       sb.append("/");
     }
     sb.append(val);
 
     return sb.toString();
   }
   */
 
   /* ======================================================================
    *                     Property methods
    * ====================================================================== */
 
   @Override
   public long getSessionNum() {
     return sessionNum;
   }
 
   /**
    * @param val
    */
   public void setAppName(final String val) {
     appName = val;
   }
 
   /**
    * @return app name
    */
   public String getAppName() {
     return appName;
   }
 
   @Override
   public void setUser(final String val) {
     user = val;
   }
 
   @Override
   public String getUser() {
     return user;
   }
 
   @Override
   public boolean isGuest() {
     return user == null;
   }
 
   @Override
   public void prepareRender(final BwRequest req) {
     BwActionFormBase form = req.getBwForm();
     Client cl = req.getClient();
     BwModuleState mstate = req.getModule().getState();
 
     req.setRequestAttr(BwRequest.bwSearchParamsName,
                        cl.getSearchParams());
 
     req.setRequestAttr(BwRequest.bwSearchResultName,
                        mstate.getSearchResult());
 
     try {
       form.assignCalendarUserAddress(cl.getCurrentCalendarAddress());
 
       if (mstate.getEventDates() == null) {
         mstate.assignEventDates(new EventDates(cl.getCurrentPrincipalHref(),
                                                mstate.getCalInfo(),
                                                form.getHour24(),
                                                form.getEndDateType(),
                                                config.getMinIncrement(),
                                                form.getErr()));
       }
 
       /* This only till we make module state the resuest scope form */
       if (form.getEventDates() == null) {
         form.assignEventDates(new EventDates(cl.getCurrentPrincipalHref(),
                                                mstate.getCalInfo(),
                                                form.getHour24(),
                                                form.getEndDateType(),
                                                config.getMinIncrement(),
                                                form.getErr()));
       }
 
       Long lastRefresh = (Long)req.getSessionAttr(refreshTimeAttr);
       long now = System.currentTimeMillis();
 
       if (!mstate.getRefresh() ||
               (lastRefresh == null) || (now - lastRefresh > refreshRate)) {
         // Implant various objects for the pages.
         embedFilters(req);
         embedCollections(req);
         embedPublicCollections(req);
         embedUserCollections(req);
         embedPrefs(req);
         embedViews(req);
 
         authpars = cl.getAuthProperties().cloneIt();
         form.setAuthPars(authpars);
 
         syspars = cl.getSystemProperties();
         form.setEventRegAdminToken(syspars.getEventregAdminToken());
 
         form.setCurrentGroups(cl.getCurrentPrincipal().getGroups());
 
         req.setSessionAttr(refreshTimeAttr, now);
       }
 
       if (mstate.getRefresh() ||
               mstate.getCurTimeView() == null) {
         refreshView(req);
 //        mstate.setRefresh(false);
       }
     } catch (Throwable t) {
       // Not much we can do here
       form.getErr().emit(t);
       return;
     }
   }
 
   @Override
   public void embedFilters(final BwRequest req) throws Throwable {
     req.setSessionAttr(BwRequest.bwFiltersListName,
                        req.getClient().getAllFilters());
   }
 
   public TimeView getCurTimeView(final BwRequest req) {
     BwModuleState mstate = req.getModule().getState();
 
     if (mstate.getCurTimeView() == null) {
       refreshView(req);
     }
 
     return mstate.getCurTimeView();
   }
 
   public AuthProperties getAuthpars() {
     return authpars;
   }
 
   @Override
   public void embedAddContentCalendarCollections(final BwRequest request) throws Throwable {
     request.setSessionAttr(BwRequest.bwAddContentCollectionListName,
                        request.getClient().getAddContentCollections(publicAdmin));
   }
 
   @Override
   public void embedCollections(final BwRequest request) throws Throwable {
     BwCalendar col = null;
     BwActionFormBase form = request.getBwForm();
     Client cl = request.getClient();
 
     try {
       if (form.getSubmitApp()) {
         // Use submission root
         col = cl.getCollection(
                 form.getConfig().getSubmissionRoot());
       } else {
         // Current owner
         col = cl.getHome();
       }
 
       embedClonedCollection(request, col,
                             BwRequest.bwCollectionListName);
     } catch (Throwable t) {
       request.getErr().emit(t);
     }
   }
 
   protected void embedPublicCollections(final BwRequest request) throws Throwable {
     embedClonedCollection(request,
                           request.getClient().getPublicCalendars(),
                           BwRequest.bwPublicCollectionListName);
   }
 
   @Override
   public void embedUserCollections(final BwRequest request) throws Throwable {
     BwCalendar col = null;
     BwActionFormBase form = request.getBwForm();
     Client cl = request.getClient();
     boolean publicAdmin = form.getConfig().getPublicAdmin();
 
     try {
       BwPrincipal p;
 
       if ((publicAdmin) && (form.getCurrentCalSuite() != null)) {
         // Use calendar suite owner
         p = cl.getPrincipal(
                 form.getCurrentCalSuite().getGroup().getOwnerHref());
       } else {
         p = cl.getCurrentPrincipal();
       }
 
       col = cl.getHome(p, false);
 
       embedClonedCollection(request, col,
                             BwRequest.bwUserCollectionListName);
     } catch (Throwable t) {
       request.getErr().emit(t);
     }
   }
 
   /* ====================================================================
    *                   Categories
    * ==================================================================== */
 
   @Override
   public Collection<BwCategory> embedCategories(final BwRequest request,
                                                 final boolean refresh,
                                                 final int kind) throws Throwable {
     String attrName;
     Collection <BwCategory> vals;
 
     if (kind == ownersEntity) {
       attrName = BwRequest.bwCategoriesListName;
 
       vals = (Collection<BwCategory>)request.getSessionAttr(BwRequest.bwCategoriesListName);
       if (!refresh && vals  != null) {
         return vals;
       }
 
       vals = getCategoryCollection(request, ownersEntity, true);
     } else if (kind == editableEntity) {
       attrName = BwRequest.bwEditableCategoriesListName;
 
       vals = (Collection<BwCategory>)request.getSessionAttr(BwRequest.bwEditableCategoriesListName);
       if (!refresh && vals  != null) {
         return vals;
       }
 
       vals = getCategoryCollection(request, editableEntity, false);
     } else if (kind == preferredEntity) {
       attrName = BwRequest.bwPreferredCategoriesListName;
 
       Client cl = request.getClient();
 
       vals = cl.getCategories(curAuthUserPrefs.getCategoryPrefs().getPreferred());
     } else if (kind == defaultEntity) {
       attrName = BwRequest.bwDefaultCategoriesListName;
 
       vals = (Set<BwCategory>)request.getSessionAttr(BwRequest.bwDefaultCategoriesListName);
       if (!refresh && vals  != null) {
         return vals;
       }
 
       vals = new TreeSet<>();
 
       Client cl = request.getClient();
 
       Set<String> catuids = cl.getPreferences().getDefaultCategoryUids();
 
       for (String uid: catuids) {
         BwCategory cat = cl.getCategory(uid);
 
         if (cat != null) {
           vals.add(cat);
         }
       }
     } else {
       throw new Exception("Software error - bad kind " + kind);
     }
 
     request.setSessionAttr(attrName, vals);
     return vals;
   }
 
   /* ====================================================================
    *                   Contacts
    * ==================================================================== */
 
   @Override
   public void embedContactCollection(BwRequest request,
                                      final int kind) throws Throwable {
     Client cl = request.getClient();
     Collection<BwContact> vals = null;
     String attrName;
 
     if (kind == ownersEntity) {
       attrName = BwRequest.bwContactsListName;
 
       if (cl.getWebSubmit()) {
         // Use public
         vals = cl.getPublicContacts();
       } else {
         // Current owner
         vals = cl.getContacts();
       }
     } else if (kind == editableEntity) {
       attrName = BwRequest.bwEditableContactsListName;
 
       vals = cl.getEditableContacts();
     } else if (kind == preferredEntity) {
       attrName = BwRequest.bwPreferredContactsListName;
 
       vals = curAuthUserPrefs.getContactPrefs().getPreferred();
     } else {
       throw new Exception("Software error - bad kind " + kind);
     }
 
     request.setSessionAttr(attrName,
                            getContactCollator().getCollatedCollection(vals));
   }
 
   /* ====================================================================
    *                   Locations
    * ==================================================================== */
 
   @Override
   public void embedLocations(final BwRequest request,
                              final int kind) throws Throwable {
     Collection<BwLocation> vals = null;
     String attrName;
 
     if (kind == ownersEntity) {
       attrName = BwRequest.bwLocationsListName;
       vals = getLocations(request, ownersEntity, true);
     } else if (kind == editableEntity) {
       attrName = BwRequest.bwEditableLocationsListName;
 
       vals = getLocations(request, editableEntity, false);
     } else if (kind == preferredEntity) {
       attrName = BwRequest.bwPreferredLocationsListName;
 
       vals = curAuthUserPrefs.getLocationPrefs().getPreferred();
     } else {
       throw new Exception("Software error - bad kind " + kind);
     }
 
     request.setSessionAttr(attrName,
                            getLocationCollator().getCollatedCollection(vals));
   }
 
   /* ====================================================================
    *                   Package methods
    * ==================================================================== */
 
   /**
    * @param val
    */
   void setCurAuthUserPrefs(final BwAuthUserPrefs val) {
     curAuthUserPrefs = val;
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private void embedClonedCollection(final BwRequest request,
                                      final BwCalendar col,
                                      final String attrName) throws Throwable {
     BwCalendar cloned = new Cloner(request.getClient(),
                                    request.getBwForm().getCalendarsOpenState()).deepClone(col);
 
     request.setSessionAttr(attrName, cloned);
   }
 
   private void refreshView(final BwRequest req) {
     BwModuleState mstate = req.getModule().getState();
     BwActionFormBase form = req.getBwForm();
     Client cl = req.getClient();
 
     try {
       /* First ensure we have the view period set */
       if (mstate.getCurTimeView() == null) {
         /** Figure out the default from the properties
          */
         String vn;
 
         try {
           vn = cl.getPreferences().getPreferredViewPeriod();
           if (vn == null) {
             vn = "week";
           }
         } catch (Throwable t) {
           System.out.println("Exception setting current view");
           vn = "week";
         }
 
         if (mstate.getCurViewPeriod() < 0) {
           for (int i = 1; i < BedeworkDefs.viewPeriodNames.length; i++) {
             if (BedeworkDefs.viewPeriodNames[i].startsWith(vn)) {
               mstate.setCurViewPeriod(i);
               break;
             }
           }
 
           if (mstate.getCurViewPeriod() < 0) {
             mstate.setCurViewPeriod(BedeworkDefs.weekView);
           }
 
           mstate.setViewMcDate(new MyCalendarVO(new Date(System.currentTimeMillis())));
         }
       }
 
       /* Now get a view object */
 
       if ((mstate.getCurViewPeriod() == BedeworkDefs.todayView) ||
               (mstate.getViewMcDate() == null)) {
         mstate.setViewMcDate(new MyCalendarVO(new Date(
                 System.currentTimeMillis())));
         mstate.setCurViewPeriod(BedeworkDefs.dayView);
       }
 
       FilterBase filter = getFilter(req, null);
       TimeView tv = null;
 
       switch (mstate.getCurViewPeriod()) {
         case BedeworkDefs.todayView:
         case BedeworkDefs.dayView:
           tv = new DayView(form.getErr(),
                            mstate.getViewMcDate(),
                            filter);
           break;
         case BedeworkDefs.weekView:
           tv = new WeekView(form.getErr(),
                             mstate.getViewMcDate(),
                             filter);
           break;
         case BedeworkDefs.monthView:
           tv = new MonthView(form.getErr(),
                              mstate.getViewMcDate(),
                              filter);
           break;
         case BedeworkDefs.yearView:
           tv = new YearView(form.getErr(),
                             mstate.getViewMcDate(),
                             form.getShowYearData(), filter);
           break;
       }
 
       mstate.setCurTimeView(tv);
 
       if (BedeworkDefs.appTypeWebuser.equals(cl.getAppType())) {
         cl.clearSearch();
       }
     } catch (Throwable t) {
       // Not much we can do here
       req.getErr().emit(t);
     }
   }
 
   /** If a name is defined fetch it, or use the current filter if it exists
    *
    * @param filterName
    * @return BwFilter or null
    * @throws Throwable
    */
   private FilterBase getFilter(final BwRequest req,
                                final String filterName) throws Throwable {
     BwActionFormBase form = req.getBwForm();
     Client cl = req.getClient();
 
     BwFilterDef fdef = null;
 
     if (filterName != null) {
       fdef = cl.getFilter(filterName);
 
       if (fdef == null) {
         req.getErr().emit(ClientError.unknownFilter, filterName);
       }
     }
 
     if (fdef == null) {
       fdef = form.getCurrentFilter();
     }
 
     if (fdef == null) {
       return null;
     }
 
     if (fdef.getFilters() == null) {
       try {
         cl.parseFilter(fdef);
       } catch (CalFacadeException cfe) {
         req.getErr().emit(cfe);
       }
     }
 
     return fdef.getFilters();
   }
 
   /**
     Does the given string represent a rootless URI?  A URI is rootless
     if it is not absolute (that is, does not contain a scheme like 'http')
     and does not start with a '/'
     @param uri String to test
     @return Is the string a rootless URI?  If the string is not a valid
       URI at all (for example, it is null), returns false
    */
   private boolean rootlessUri(final String uri) {
     try {
       return !((uri == null) || uri.startsWith("/") || new URI(uri).isAbsolute());
     } catch (URISyntaxException e) {  // not a URI at all
       return false;
     }
   }
 
   private void setSessionNum(final String name) {
     try {
       synchronized (countsMap) {
         Counts c = countsMap.get(name);
 
         if (c == null) {
           c = new Counts();
           countsMap.put(name, c);
         }
 
         sessionNum = c.totalSessions;
         c.totalSessions++;
       }
     } catch (Throwable t) {
     }
   }
 
   private Collection<BwCategory> getCategoryCollection(final BwRequest request,
                                                        final int kind,
                                                        final boolean forEventUpdate) throws Throwable {
     BwActionFormBase form = request.getBwForm();
     Client cl = request.getClient();
     Collection<BwCategory> vals = null;
 
     if (kind == ownersEntity) {
 
       String appType = cl.getAppType();
       if (cl.getWebSubmit() ||
               BedeworkDefs.appTypeWebpublic.equals(appType) ||
               BedeworkDefs.appTypeFeeder.equals(appType)) {
         // Use public
         vals = cl.getPublicCategories();
       } else {
         // Current owner
         vals = cl.getCategories();
 
         BwEvent ev = form.getEvent();
 
         if (!publicAdmin && forEventUpdate &&
                 (ev != null) &&
                 (ev.getCategories() != null)) {
           for (BwCategory cat: ev.getCategories()) {
             if (!cat.getOwnerHref().equals(cl.getCurrentPrincipalHref())) {
               vals.add(cat);
             }
           }
         }
       }
     } else if (kind == editableEntity) {
       vals = cl.getEditableCategories();
     }
 
     if (vals == null) {
       return null;
     }
 
     return getCategoryCollator().getCollatedCollection(vals);
   }
 
   private CollectionCollator<BwCategory> getCategoryCollator() {
     if (categoryCollator == null) {
       categoryCollator = new CollectionCollator<BwCategory>();
     }
 
     return categoryCollator;
   }
 
   private CollectionCollator<BwContact> getContactCollator() {
     if (contactCollator == null) {
       contactCollator = new CollectionCollator<>();
     }
 
     return contactCollator;
   }
 
   private Collection<BwLocation> getLocations(final BwRequest request,
                                               final int kind,
                                               final boolean forEventUpdate) {
     try {
       BwActionFormBase form = request.getBwForm();
       Client cl = request.getClient();
       Collection<BwLocation> vals = null;
 
       if (kind == ownersEntity) {
         String appType = cl.getAppType();
         if (cl.getWebSubmit()) {
           // Use public
           vals = cl.getPublicLocations();
         } else {
           // Current owner
           vals = cl.getLocations();
 
           BwEvent ev = form.getEvent();
 
           if (!publicAdmin && forEventUpdate && (ev != null)) {
             BwLocation loc = ev.getLocation();
 
             if ((loc != null) &&
                     (!loc.getOwnerHref().equals(cl.getCurrentPrincipalHref()))) {
               vals.add(loc);
             }
           }
         }
       } else if (kind == editableEntity) {
         vals = cl.getEditableLocations();
       }
 
       if (vals == null) {
         // Won't need this with 1.5
         throw new Exception("Software error - bad kind " + kind);
       }
 
       return getLocationCollator().getCollatedCollection(vals);
     } catch (Throwable t) {
       t.printStackTrace();
       request.getErr().emit(t);
       return new ArrayList<>();
     }
   }
 
   private CollectionCollator<BwLocation> getLocationCollator() {
     if (locationCollator == null) {
       locationCollator = new CollectionCollator<BwLocation>();
     }
 
     return locationCollator;
   }
 
   protected void embedPrefs(final BwRequest request) throws Throwable {
     request.setSessionAttr(BwRequest.bwPreferencesName,
                            request.getClient().getPreferences());
   }
 
   protected void embedViews(final BwRequest request) throws Throwable {
     request.setSessionAttr(BwRequest.bwViewsListName,
                            request.getClient().getAllViews());
   }
 
   private static class Cloner {
     private Map<String, BwCalendar> clonedCols = new HashMap<>();
 
     private Map<String, BwCategory> clonedCats = new HashMap<>();
 
     private Client cl;
 
     private Set<String> openStates;
 
     private static class CloneResult {
       /* true if we found it in the map */
       boolean alreadyCloned;
 
       BwCalendar col;
 
       CloneResult(final BwCalendar col,
                   final boolean alreadyCloned) {
         this.col = col;
         this.alreadyCloned = alreadyCloned;
       }
     }
 
     Cloner(final Client cl,
            final Set<String> openStates) {
       this.cl = cl;
       this.openStates = openStates;
     }
 
     BwCalendar deepClone(final BwCalendar val) throws Throwable {
       final CloneResult cr = cloneOne(val);
 
       if (cr.alreadyCloned) {
         return cr.col;
       }
 
       cr.col.setChildren(getChildren(val));
 
       return cr.col;
     }
 
     private CloneResult cloneOne(final BwCalendar val) throws Throwable {
       BwCalendar clCol = clonedCols.get(val.getPath());
 
       if (clCol != null) {
         if (openStates != null) {
           clCol.setOpen(openStates.contains(clCol.getPath()));
         }
 
         return new CloneResult(clCol, true);
       }
 
       clCol = val.shallowClone();
 
       clCol.setCategories(cloneCategories(val));
       clCol.setProperties(cloneProperties(val));
 
       if (openStates != null) {
         clCol.setOpen(openStates.contains(clCol.getPath()));
       }
 
      clonedCols.put(val.getPath(), clCol);

       if (val.getAliasUri() != null) {
         final BwCalendar aliased = cl.resolveAlias(val, false, false);
 
         if (aliased != null) {
           clCol.setAliasCalType(aliased.getCalType());
           final BwCalendar clAliased = deepClone(aliased);
           clonedCols.put(clAliased.getPath(), clAliased);
 
           clCol.setAliasTarget(clAliased);
         }
       }
 
       return new CloneResult(clCol, false);
     }
 
     private Collection<BwCalendar> getChildren(final BwCalendar col) throws Throwable {
       final Collection<BwCalendar> children = cl.getChildren(col);
       final Collection<BwCalendar> cloned = new ArrayList<>(children.size());
 
       if (!Util.isEmpty(children)) {
         for (final BwCalendar c:children) {
           final CloneResult cr = cloneOne(c);
           cloned.add(cr.col);
 
           if (!cr.alreadyCloned) {
             // Clone the subtree
             cr.col.setChildren(getChildren(c));
           }
         }
       }
 
       return cloned;
     }
 
     private Set<BwCategory> cloneCategories(final BwCalendar val) {
       if (val.getNumCategories() == 0) {
         return null;
       }
 
       final TreeSet<BwCategory> ts = new TreeSet<>();
 
       for (final BwCategory cat: val.getCategories()) {
         BwCategory clCat = clonedCats.get(cat.getUid());
 
         if (clCat == null) {
           clCat = (BwCategory)cat.clone();
           clonedCats.put(cat.getUid(), clCat);
         }
 
         ts.add(clCat);
       }
 
       return ts;
     }
 
     private Set<BwProperty> cloneProperties(final BwCalendar val) {
       if (val.getNumProperties() == 0) {
         return null;
       }
 
       final TreeSet<BwProperty> ts = new TreeSet<>();
 
       for (final BwProperty p: val.getProperties()) {
         ts.add((BwProperty)p.clone());
       }
 
       return ts;
     }
   }
 }
