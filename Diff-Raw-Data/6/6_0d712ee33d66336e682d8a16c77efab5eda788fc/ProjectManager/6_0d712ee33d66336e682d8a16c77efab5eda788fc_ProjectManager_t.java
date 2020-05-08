 /*
  * Copyright 2006-2007, AVANE sarl, and individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *
  * @author jvdrean
  */
 package com.xpn.xwiki.plugin.chronopolys;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Object;
 import com.xpn.xwiki.cache.api.XWikiCache;
 import com.xpn.xwiki.cache.api.XWikiCacheNeedsRefreshException;
 import com.xpn.xwiki.cache.api.XWikiCacheService;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.plugin.PluginException;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class ProjectManager {
     public static final int projectsCacheCapacity = 100;
 
     // Project templates
     public static final String TEMPLATE_DEFAULT_SPACE = "ChronoTemplates";
 
     public static final String TEMPLATE_PROJECT = TEMPLATE_DEFAULT_SPACE + "." + "ProjectTemplate";
 
     public static final String TEMPLATE_PROJECT_LOG =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectLogTemplate";
 
     public static final String TEMPLATE_PROJECT_DOCUMENTS =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectDocumentsTemplate";
 
     public static final String TEMPLATE_PROJECT_MEMBERS =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectMembersTemplate";
 
    public static final String TEMPLATE_PROJECT_GUESTS =
            TEMPLATE_DEFAULT_SPACE + "." + "ProjectMembersTemplate";

     public static final String TEMPLATE_PROJECT_LEADERS =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectLeadersTemplate";
 
     public static final String TEMPLATE_PROJECT_PHASES =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectPhasesTemplate";
 
     public static final String TEMPLATE_PROJECT_WIKI =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectWikiTemplate";
 
     public static final String TEMPLATE_PROJECT_NOTE =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectNoteTemplate";
 
     public static final String TEMPLATE_PROJECT_PREFS =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectWebPreferencesTemplate";
 
     public static final String TEMPLATE_PROJECT_ARTICLE =
             TEMPLATE_DEFAULT_SPACE + "." + "ProjectArticleTemplate";
 
     ChronopolysPlugin plugin;
 
     protected XWikiCache projectsCache;
 
     public ProjectManager(ChronopolysPlugin plugin) {
         this.plugin = plugin;
     }
 
     public XWikiCache getProjectsCache() {
         return projectsCache;
     }
 
     /*
      * Init projects cache
      */
     public synchronized void initProjectsCache(XWikiContext context) throws XWikiException {
         if (projectsCache == null) {
             XWikiCacheService cacheService = context.getWiki().getCacheService();
             projectsCache =
                     cacheService.newCache("xwiki.chronopolys.folders.cache", projectsCacheCapacity);
         }
     }
 
     /*
      * Flush projects cache (contains user's projects lists)
      */
     public void flushProjectsCache() {
         if (projectsCache != null) {
             projectsCache.flushAll();
             projectsCache = null;
         }
     }
 
     /*
      * Get projects the current user is member of
      * (no duplicate with getProjects because of Admins&Managers)
      */
     public List getMyProjects(XWikiContext context) throws XWikiException {
         initProjectsCache(context);
         List list = null;
         String key = context.getDatabase() + ':' + context.getLocalUser() + ":myprojects";
         synchronized (key) {
             try {
                 list = (List) projectsCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 projectsCache.cancelUpdate(key);
                 String hql =
                         "select distinct doc.web, pdate.value from XWikiDocument doc, XWikiDocument pdoc, BaseObject obj, StringProperty prop, " +
                                 "BaseObject pobj, DateProperty pdate where doc.fullName=obj.name " +
                                 "and obj.className='XWiki.XWikiGroups' and obj.id=prop.id.id and prop.id.name='member' " +
                                 "and prop.value='" + context.getLocalUser() +
                                 "' and pdoc.web=doc.web and pdoc.name='WebHome' " +
                                 "and pdoc.fullName=pobj.name and pobj.id=pdate.id.id and pdate.id.name='end' order by pdate.value";
                 // workaround for HSQLDB distinct/order limitation
                 List tmplist = context.getWiki().search(hql, context);
                 list = new ArrayList();
                 for (int i = 0; i < tmplist.size(); i++) {
                     String prj = (String) ((java.lang.Object[]) tmplist.get(i))[0];
                     if (prj != null)
                         list.add(prj);
                 }
                 projectsCache.putInCache(key, list);
             }
         }
         return list;
     }
 
     /*
      * Create a new chronopolys project
      */
     public ProjectApi addProject(String name, XWikiContext context) throws XWikiException {
         String space = getNewProjectUid(context);
 
         context.getWiki()
                 .copyDocument(TEMPLATE_PROJECT, space + "." + Project.PROJECT_HOMEDOC, context);
         context.getWiki()
                 .copyDocument(TEMPLATE_PROJECT_LOG, space + "." + Project.PROJECT_LOGDOC, context);
         context.getWiki().copyDocument(TEMPLATE_PROJECT_DOCUMENTS,
                 space + "." + Project.PROJECT_DOCUMENTSDOC, context);
         context.getWiki().copyDocument(TEMPLATE_PROJECT_MEMBERS,
                 space + "." + Project.PROJECT_MEMBERSDOC, context);
        context.getWiki().copyDocument(TEMPLATE_PROJECT_GUESTS,
                space + "." + Project.PROJECT_GUESTSDOC, context);
         context.getWiki().copyDocument(TEMPLATE_PROJECT_LEADERS,
                 space + "." + Project.PROJECT_LEADERSDOC, context);
         context.getWiki().copyDocument(TEMPLATE_PROJECT_PHASES,
                 space + "." + Project.PROJECT_PHASESDOC, context);
         context.getWiki()
                 .copyDocument(TEMPLATE_PROJECT_WIKI, space + "." + Project.PROJECT_WIKIDOC, context);
         context.getWiki()
                 .copyDocument(TEMPLATE_PROJECT_NOTE, space + "." + Project.PROJECT_NOTEDOC, context);
         context.getWiki()
                 .copyDocument(TEMPLATE_PROJECT_PREFS, space + "." + Project.PROJECT_PREFSDOC, context);
 
         XWikiDocument homeDoc =
                 context.getWiki().getDocument(space, Project.PROJECT_HOMEDOC, context);
 
         BaseObject bobj = homeDoc.getObject(Project.CLASS_PROJECT);
         bobj.set("name", name, context);
         bobj.set("projected_start", "", context);
         bobj.set("projected_end", "", context);
         bobj.set("start", "", context);
         bobj.set("end", "", context);
         homeDoc.setCreator(context.getUser());
         context.getWiki().saveDocument(homeDoc, context);
         ProjectApi newProject = new ProjectApi(new Project(homeDoc, plugin, context), context);
         newProject.addMember(context.getLocalUser());
         newProject.setProjectRights(ProjectMembers.PROJECT_PRIVATE_RIGHTS);
 
         return newProject;
     }
 
     public boolean projectExists(String uid, XWikiContext context) throws XWikiException {
         return isProject(uid, context);
     }
 
     public boolean isProject(String uid, XWikiContext context) throws XWikiException {
         try {
             getProject(uid, context);
         } catch (PluginException e) {
             return false;
         }
         return true;
     }
 
     /*
      * Get the chronopolys project API for the given project
      */
     public ProjectApi getProject(String uid, XWikiContext context) throws XWikiException {
         XWikiDocument doc = context.getWiki().getDocument(uid, Project.PROJECT_HOMEDOC, context);
         return new ProjectApi(new Project(doc, plugin, context), context);
     }
 
     /**
      * Retreive chronopolys projects basic users : search based on the user's memberships admin &
      * managers : retreive all the projects
      *
      * @param context of the request
      * @return ProjectsList
      * @throws XWikiException error on cache creation
      */
     public List getProjects(XWikiContext context) throws XWikiException {
         List list = null;
         String key = context.getDatabase() + ":" + context.getLocalUser();
 
         initProjectsCache(context);
         synchronized (key) {
             try {
                 // retreive projects list from cache
                 list = (List) projectsCache.getFromCache(key);
             } catch (XWikiCacheNeedsRefreshException e) {
                 // update the projects list cache
                 projectsCache.cancelUpdate(key);
                 String hql;
                 list = new ArrayList<Object>();
                 List projectList = new ArrayList<Object>();
                 if (plugin.getUserManager().isAdmin(context) ||
                         plugin.getUserManager().isManager(context)) {
                     // admin & managers
                     // the distinct + order here are not hsqldb not compliant
                     hql =
                             "select distinct doc.web from XWikiDocument doc, BaseObject as obj where obj.name=doc.fullName" +
                                     " and doc.web != '" + TEMPLATE_DEFAULT_SPACE + "'" +
                                     " and obj.className='" + Project.CLASS_PROJECT + "'";
                     projectList = context.getWiki().search(hql, context);
                 } else {
                     // basic users
                     hql =
                             "select distinct doc.web, pdate.value from XWikiDocument doc, XWikiDocument pdoc, BaseObject obj, StringProperty prop, " +
                                     "BaseObject pobj, DateProperty pdate where doc.fullName=obj.name " +
                                     "and doc.web != '" + TEMPLATE_DEFAULT_SPACE + "'" +
                                     "and obj.className='XWiki.XWikiGroups' and obj.id=prop.id.id and prop.id.name='member' " +
                                     "and (prop.value='" + context.getLocalUser() + "' or prop.value='XWiki.XWikiAllGroup')" +
                                     " and pdoc.web=doc.web and pdoc.name='WebHome' " +
                                     "and pdoc.fullName=pobj.name and pobj.id=pdate.id.id and pdate.id.name='end' order by pdate.value";
                     // workaround for HSQLDB distinct/order limitation
                     List tmplist = context.getWiki().search(hql, context);
                     list = new ArrayList();
                     for (int i = 0; i < tmplist.size(); i++) {
                         String prj = (String) ((java.lang.Object[]) tmplist.get(i))[0];
                         if (prj != null)
                             projectList.add(prj);
                     }
                 }
 
                 Iterator it = projectList.iterator();
                 while (it.hasNext()) {
                     String docName = (String) it.next();
                     BaseObject bobj = context.getWiki()
                             .getDocument(docName + "." + Project.PROJECT_HOMEDOC, context)
                             .getObject(Project.CLASS_PROJECT);
                     Object obj = new Object(bobj, context);
                     if (!list.contains(obj))
                         list.add(obj);
                 }
                 projectsCache.putInCache(key, list);
             }
         }
 
         return list;
     }
 
     /*
      * Get members of the given project
      */
     public List getMembers(String projectUid, XWikiContext context) throws XWikiException {
         ProjectApi project = getProject(projectUid, context);
         return project.getMembers();
     }
 
     /*
      * Get a new project Unique ID
      */
     public String getNewProjectUid(XWikiContext context) throws XWikiException {
         String uid;
         String page;
 
         uid = Project.PROJECT_SPACEPREFIX +
                 org.apache.commons.lang.RandomStringUtils.randomNumeric(8);
         page = uid + "." + Project.PROJECT_HOMEDOC;
         while (context.getWiki().exists(page, context)) {
             uid = Project.PROJECT_SPACEPREFIX +
                     org.apache.commons.lang.RandomStringUtils.randomNumeric(8);
             page = uid + "." + Project.PROJECT_HOMEDOC;
         }
 
         return uid;
     }
 }
