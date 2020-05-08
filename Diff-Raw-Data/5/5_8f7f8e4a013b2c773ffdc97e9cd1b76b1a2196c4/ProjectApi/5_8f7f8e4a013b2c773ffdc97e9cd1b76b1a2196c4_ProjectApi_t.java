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
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ProjectApi
 {
     private Project project;
 
     private XWikiContext context;
 
     public ProjectApi(Project project, XWikiContext context)
     {
         this.context = context;
         this.project = project;
     }
 
     public String getLanguage()
     {
         return project.getLanguage();
     }
 
     public String getURL() throws XWikiException
     {
         return project.getURL(context);
     }
 
     public String getSpace() throws XWikiException
     {
         return project.getSpace();
     }
 
    public Document getProjectHomeDoc() throws XWikiException
     {
        return project.getProjectHomeDoc(context).newDocument(context);
     }
 
     public String getNewPlogUid() throws XWikiException
     {
         return project.getLog().getNewPlogUid(context);
     }
 
     public List getWikiPages() throws XWikiException
     {
         return project.getWiki().getWikiPages(context);
     }
 
     public boolean isWikiPage(String page) throws XWikiException
     {
         return project.getWiki().isWikiPage(page, context);
     }
 
     public List getPlogPages() throws XWikiException
     {
         return project.getLog().getPlogPages(context);
     }
 
     public boolean isPlogPage(String page) throws XWikiException
     {
         return project.getLog().isPlogPage(page, context);
     }
 
     public boolean isPlogTask(XWikiDocument plogdoc)
     {
         return project.getLog().isPlogTask(plogdoc);
     }
 
     public boolean isPlogMeeting(XWikiDocument plogdoc)
     {
         return project.getLog().isPlogMeeting(plogdoc);
     }
 
     public boolean isPlogNew(XWikiDocument plogdoc)
     {
         return project.getLog().isPlogNew(plogdoc);
     }
 
     public List getMembersToNotifyForPlog(XWikiDocument plogdoc) throws XWikiException
     {
         return project.getNotifications().getMembersToNotifyForPlog(plogdoc, context);
     }
 
     /* public List getSubscribers(String docname)
     {
         return project.getNotifications().getSubscribers(docname, context);
     } */
 
     public void setProjectRights(int mode) throws XWikiException
     {
         project.getMembers().setProjectRights(mode, context);
     }
 
     public void delete() throws XWikiException
     {
         project.delete(context);
     }
 
     public List getLastModifications(int limit, int start, XWikiContext context)
         throws XWikiException
     {
         return project.getLastModifications(limit, start, context);
     }
 
     public List getLastModifications() throws XWikiException
     {
         return project.getLastModifications(context);
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Note
 
     public float getNote() throws XWikiException
     {
         return project.getNote(context);
     }
 
     public List getNotes() throws XWikiException
     {
         return project.getNotes(context);
     }
 
     public void addNote(String note) throws XWikiException
     {
         project.addNote(note, context);
     }
 
     public void resetNotes() throws XWikiException
     {
         project.resetNotes(context);
     }
 
     /*
     public void hasGivenNote(String user) throws XWikiException {
         project.hasGivenNote(user, context);
     }
     */
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Get / set
 
     public java.lang.Object get(String name) throws XWikiException
     {
         return project.get(name);
     }
 
     public void set(String name, String value) throws XWikiException
     {
         project.set(name, value);
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Member management
 
     public String getProjectCreator() throws XWikiException
     {
         return project.getMembers().getProjectCreator(context);
     }
 
     public String getProjectLeader() throws XWikiException
     {
         return project.getMembers().getProjectLeader(context);
     }
 
     public boolean addMember(String memberName) throws XWikiException
     {
         return project.getMembers().addMember(memberName, context);
     }
 
     public boolean removeMember(String memberName) throws XWikiException
     {
         return project.getMembers().removeMember(memberName, context);
     }
 
     public boolean isMember(String memberName) throws XWikiException
     {
         return project.getMembers().isMember(memberName, context);
     }
 
     public List getMembers() throws XWikiException
     {
         return project.getMembers().getMembers(context);
     }
 
     /* public List getNotificationsSubscribers() throws XWikiException
     {
         // restricted to project leader & creator
         if (getProjectLeader().equals(context.getUser()) ||
             getProjectCreator().equals(context.getUser()))
         {
             return project.getNotifications().getNotificationsSubscribers(context);
         } else {
             return new ArrayList();
         }
     }
 
     public Object getUserNotificationsObj() throws XWikiException
     {
         return project.getNotifications().getUserNotificationsObj(context);
     }
 
     public List getUserNotifications(String user) throws XWikiException
     {
         if (project.getNotifications().getUserNotifications(user, context) != null)
         // return java.util.Arrays.asList(project.getUserNotifications(user, context).split(","));
         {
             return project.getNotifications().getUserNotifications(user, context);
         } else {
             return null;
         }
     }
 
     public void setUserNotifications(String user, String items) throws XWikiException
     {
         project.getNotifications().setUserNotifications(user, items, context);
     } */
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Guest management    
 
     public boolean addGuest(String guestName) throws XWikiException
     {
         return project.getGuests().addGuest(guestName, context);
     }
 
     public boolean removeGuest(String guestName) throws XWikiException
     {
         return project.getGuests().removeGuest(guestName, context);
     }
 
     public boolean isGuest(String guestName) throws XWikiException
     {
         return project.getGuests().isGuest(guestName, context);
     }
 
     public List getGuests() throws XWikiException
     {
         return project.getGuests().getGuests(context);
     }
 
     public boolean isPublic() throws XWikiException
     {
         return project.getGuests().isPublic(context);
     }
 
     public boolean setPublic(boolean makepublic) throws XWikiException
     {
         return project.getGuests().setPublic(makepublic, context);
     }
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Phase management
 
     /*public boolean addPhase(String name) throws XWikiException {
         return project.addPhase(name, context);
     }*/
 
     public boolean isPhase(String name) throws XWikiException
     {
         return project.getPhases().isPhase(name, context);
     }
 
     /* public void updatePhase(String name) throws XWikiException {
         project.updatePhase(name, context);
     }
 
     public Object getPhaseu(String name) throws XWikiException {
         return project.getPhaseu(name, context);
     }*/
 
     public List getPhasesNames() throws XWikiException
     {
         return project.getPhases().getPhasesNames(context);
     }
 
     /* public int getPhaseDuration(com.xpn.xwiki.api.Object phase) throws XWikiException {
         return project.getPhaseDuration(phase);
     }
 
     public String getHTMLPhasesTimeline(int fromYear, int fromMonth, int fromDay, int scale) throws XWikiException {
         return project.getHTMLPhasesTimeline(fromYear, fromMonth, fromDay, scale, context);
     } */
 
     public List getPhases() throws XWikiException
     {
         return project.getPhases().getPhases(context);
     }
 
     /* public boolean removePhase(String name) throws XWikiException {
         return project.removePhase(name, context);
     } */
 
     public void sendNotification(String user, Document doc, String template)
     {
         List rcpt = new ArrayList();
         rcpt.add(user);
         try {
             project.getPlugin().getNotificationManager().sendNotification(rcpt, this,
                 doc, template, context);
         } catch (XWikiException e) {
         }
     }
 
     public void resetProjectRights() throws XWikiException
     {
         project.getMembers().setProjectRights(0, context);
     }
 }
