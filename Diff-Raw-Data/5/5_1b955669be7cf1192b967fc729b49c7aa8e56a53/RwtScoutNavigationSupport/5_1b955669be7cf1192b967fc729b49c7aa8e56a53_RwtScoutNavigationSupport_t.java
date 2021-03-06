 /*******************************************************************************
  * Copyright (c) 2011 BSI Business Systems Integration AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BSI Business Systems Integration AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.scout.rt.ui.rap.window.desktop.navigation;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.rwt.IBrowserHistory;
 import org.eclipse.rwt.RWT;
 import org.eclipse.rwt.events.BrowserHistoryEvent;
 import org.eclipse.rwt.events.BrowserHistoryListener;
 import org.eclipse.scout.commons.StringUtility;
 import org.eclipse.scout.commons.exception.ProcessingException;
 import org.eclipse.scout.rt.client.ClientSyncJob;
 import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
 import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
 import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
 import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
 import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
 import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
 import org.eclipse.scout.service.SERVICES;
 
 public class RwtScoutNavigationSupport {
 
   private final IRwtEnvironment m_uiEnvironment;
   private IBrowserHistory m_uiHistory;
   private INavigationHistoryService m_historyService;
   private P_NavigationHistoryListener m_scoutListener;
   private BrowserHistoryListener m_uiListener = new BrowserHistoryListener() {
     private static final long serialVersionUID = 1L;
 
     @Override
     public void navigated(BrowserHistoryEvent event) {
       handleNavigationFromUi(event.entryId);
     }
   };
 
   public RwtScoutNavigationSupport(IRwtEnvironment uiEnvironment) {
     m_uiEnvironment = uiEnvironment;
   }
 
   public void install() {
     if (m_uiHistory == null) {
       m_uiHistory = RWT.getBrowserHistory();
       m_uiHistory.addBrowserHistoryListener(m_uiListener);
     }
 
     m_uiEnvironment.invokeScoutLater(new Runnable() {
 
       @Override
       public void run() {
         m_historyService = SERVICES.getService(INavigationHistoryService.class);
         if (m_scoutListener == null) {
           m_scoutListener = new P_NavigationHistoryListener();
           m_historyService.addNavigationHistoryListener(m_scoutListener);
         }
       }
 
     }, 0);
   }
 
   public void uninstall() {
     new ClientSyncJob("", getUiEnvironment().getClientSession()) {
 
       @Override
       protected void runVoid(IProgressMonitor monitor) {
         if (m_historyService != null && m_scoutListener != null) {
           m_historyService.removeNavigationHistoryListener(m_scoutListener);
         }
       }
 
       //It seems that jobs aren't reliably executed on shutdown, explicitly calling Job.getJobManager().resume() doesn't work either.
       //RunNow should be save here because the job just removes a listener
     }.runNow(new NullProgressMonitor());
     if (m_uiHistory != null) {
       m_uiHistory.removeBrowserHistoryListener(m_uiListener);
     }
   }
 
   protected void handleNavigationFromUi(final String entryId) {
     Runnable t = new Runnable() {
       @Override
       public void run() {
         try {
           for (Bookmark b : m_historyService.getBookmarks()) {
             if (getId(b).equals(entryId)) {
               m_historyService.stepTo(b);
               break;
             }
           }
         }
         catch (ProcessingException e) {
           //nop
         }
       }
     };
     getUiEnvironment().invokeScoutLater(t, 0);
 
   }
 
   private IRwtEnvironment getUiEnvironment() {
     return m_uiEnvironment;
   }
 
   protected void handleBookmarkAddedFromScout(Bookmark bookmark) {
     String id = getId(bookmark);
    //Title is set to null because it doesn't work properly, see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=396400
    m_uiHistory.createEntry(id, null);
   }
 
   private String cleanNl(String s) {
     s = s.replaceAll("(\r\n)|(\n\r)|(\n)|(\r)", " -");
     return s;
   }
 
   private String cleanBrowserSpecialChars(String s) {
     s = s.replaceAll("\\s*\\-\\s*", "-");
     s = s.replaceAll("\\s+", "-");
     s = s.replaceAll(",", "");
     return s;
   }
 
   private String getId(Bookmark b) {
     StringBuilder key = new StringBuilder();
     if (!StringUtility.isNullOrEmpty(b.getOutlineClassName())) {
       key.append(b.getOutlineClassName());
     }
     List<AbstractPageState> path = b.getPath();
     if (!path.isEmpty()) {
       for (int i = 0; i < path.size(); i++) {
         if (!StringUtility.isNullOrEmpty(path.get(i).getLabel())) {
           key.append("-" + path.get(i).getLabel());
         }
       }
     }
     return cleanBrowserSpecialChars(cleanNl(key.toString()));
   }
 
   private class P_NavigationHistoryListener implements NavigationHistoryListener {
     @Override
     public void navigationChanged(NavigationHistoryEvent e) {
       if (e.getType() == NavigationHistoryEvent.TYPE_BOOKMARK_ADDED) {
         final Bookmark bookmark = e.getBookmark();
         Runnable r = new Runnable() {
           @Override
           public void run() {
             handleBookmarkAddedFromScout(bookmark);
           }
         };
         getUiEnvironment().invokeUiLater(r);
       }
     }
   }
 }
