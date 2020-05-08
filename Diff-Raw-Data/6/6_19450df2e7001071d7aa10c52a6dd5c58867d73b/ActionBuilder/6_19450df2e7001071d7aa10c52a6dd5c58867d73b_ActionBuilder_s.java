 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.core;
 
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.ICoolBarManager;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.commands.ActionHandler;
 import org.eclipse.jubula.app.i18n.Messages;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 import org.eclipse.ui.actions.ContributionItemFactory;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.ide.IDEActionFactory;
 import org.eclipse.ui.menus.CommandContributionItem;
 
 
 /**
  * Builds the actions and populates the menubar and toolbar when a new window is
  * opened. This work is factored into a separate class to avoid cluttering
  * <code>BrowserAdvisor</code>
  * <p>
  * This adds several actions to the menus and toolbar that are typical for web
  * browsers (e.g. Back, Forward, Stop, Refresh). These are defined as
  * retargetable actions, for which the <code>BrowserView</code> registers
  * handling actions.
  * 
  * @author BREDEX GmbH
  * @created 20.09.2004
  */
 public class ActionBuilder {
     
     /** WorkbenchWindow */
     private IWorkbenchWindow m_window;
 
     /** Action: quit application */
     private ActionFactory.IWorkbenchAction m_quitAction;
 
     /** Action: preferences */
     private ActionFactory.IWorkbenchAction m_prefAction;
 
     /** Action: help */
     private ActionFactory.IWorkbenchAction m_helpCont;
     
     /** Action: welcome screen */
     private ActionFactory.IWorkbenchAction m_intro;
 
     /** Action: save */
     private ActionFactory.IWorkbenchAction m_fileSave;
     
     /** Action: refresh */
     private ActionFactory.IWorkbenchAction m_refresh;
     
     /** Action: save_all */
     private ActionFactory.IWorkbenchAction m_fileSaveAll;
     
     /** Action: reset perspective */
     private ActionFactory.IWorkbenchAction m_resetPersp;
     
     /** Action: cut */
     private ActionFactory.IWorkbenchAction m_cut;
     
     /** Action: paste */
     private ActionFactory.IWorkbenchAction m_paste;
 
     /** Action: redo */
     private IWorkbenchAction m_redo;
 
     /** Action: undo */
     private IWorkbenchAction m_undo;
 
     /** Action: choose workspace */
     private IWorkbenchAction m_openWorkspaceAction;
 
     /**
      * 
      * @param window
      *            IWorkbenchWindow
      */
     public ActionBuilder(IWorkbenchWindow window) {
         this.m_window = window;
     }
 
     /**
      * 
      * @param configurer
      *            IactionBarConfigurer
      * @param flags
      *            int
      */
     public void fillActionBars(IActionBarConfigurer configurer, int flags) {
         if ((flags & ActionBarAdvisor.FILL_PROXY) == 0) {
             makeActions(configurer);
         }
         if ((flags & ActionBarAdvisor.FILL_MENU_BAR) != 0) {
             fillMenuBar(configurer.getMenuManager());
         }
         if ((flags & ActionBarAdvisor.FILL_COOL_BAR) != 0) {
             fillCoolBar(configurer.getCoolBarManager());
         }
     }
 
     /**
      * actions of the menu entries
      * @param configurer
      *            IactionBarConfigurer 
      */
     private void makeActions(IActionBarConfigurer configurer) {
         IHandlerService handlerService = (IHandlerService) m_window
                 .getService(IHandlerService.class);
         String refreshCommandId = CommandIDs.REFRESH_COMMAND_ID;
         String cutCommandId = "org.eclipse.ui.edit.cut"; //$NON-NLS-1$
         String pasteCommandId = "org.eclipse.ui.edit.paste"; //$NON-NLS-1$
         
         m_intro = ActionFactory.INTRO.create(m_window);        
         m_helpCont = ActionFactory.HELP_CONTENTS.create(m_window);
         m_helpCont.setToolTipText(Messages.ActionBuilderHelpContentItem);
         m_helpCont.setText(Messages.ActionBuilderHelpContentToolTip);
         m_quitAction = ActionFactory.QUIT.create(m_window);
         m_quitAction.setText(Messages.ActionBuilderExitItem);
         m_prefAction = ActionFactory.PREFERENCES.create(m_window);
         m_prefAction.setText(Messages.ActionBuilderPreferencesItem);
         m_fileSaveAll = ActionFactory.SAVE_ALL.create(m_window);
         m_fileSave = ActionFactory.SAVE.create(m_window);
         m_refresh = ActionFactory.REFRESH.create(m_window);
         m_refresh.setText(Messages.ActionBuilderrefreshItem);
         m_refresh.setActionDefinitionId(refreshCommandId);
         handlerService.activateHandler(refreshCommandId, new ActionHandler(
             m_refresh));
         m_refresh.setImageDescriptor(IconConstants.REFRESH_IMAGE_DESCRIPTOR);
         m_refresh.setDisabledImageDescriptor(IconConstants.
                 REFRESH_DISABLED_IMAGE_DESCRIPTOR);
         configurer.registerGlobalAction(m_refresh);
         
         m_resetPersp = ActionFactory.RESET_PERSPECTIVE.create(m_window);
         m_resetPersp.setText(Messages.ActionBuilderresetPerspective);
         m_cut = createCutAction(cutCommandId);
         configurer.registerGlobalAction(m_cut);
         m_paste = ActionFactory.PASTE.create(m_window);
         m_paste.setToolTipText(Messages.ActionBuilderPasteToolTip);
         m_paste.setText(Messages.ActionBuilderPasteItem);
         m_paste.setActionDefinitionId(pasteCommandId);
         configurer.registerGlobalAction(m_paste);
         
         m_openWorkspaceAction = IDEActionFactory.OPEN_WORKSPACE
                 .create(m_window);
         configurer.registerGlobalAction(m_openWorkspaceAction);
         
         addMBTspecificActions(configurer);
     }
 
     /**
      * @param configurer 
      *      the configurer
      */
     private void addMBTspecificActions(IActionBarConfigurer configurer) {
         m_redo = ActionFactory.REDO.create(m_window);
         configurer.registerGlobalAction(m_redo);
         m_undo = ActionFactory.UNDO.create(m_window);
         configurer.registerGlobalAction(m_undo);
     }
     
     /**
      * Creates and returns a "Cut" action.
      *  
      * @param commandId The id for the "Cut" command.
      * @return a "Cut" action.
      */
     private IWorkbenchAction createCutAction(String commandId) {
         IWorkbenchAction cutAction = ActionFactory.CUT.create(m_window);
         cutAction.setToolTipText(Messages.ActionBuilderCutToolTip);
         cutAction.setText(Messages.ActionBuilderCutItem);
         cutAction.setActionDefinitionId(commandId);
         
         return cutAction;
     }
     
     /**
      * designs the menubar
      * 
      * @param menuBar
      *            IMenuManager
      */
     private void fillMenuBar(IMenuManager menuBar) {
         m_resetPersp.setEnabled(true);        
         // setRemoveAllWhenShow(true) is _not_ called to show the declarative main menu "Test" in Juno
         menuBar.add(createFileMenu());
         menuBar.add(createEditMenu());
         menuBar.add(createSearchMenu());
         menuBar.add(createRunMenu());
         menuBar.add(createWindowMenu());
         menuBar.add(createHelpMenu()); 
         
     }
     
     /**
      * Creates Edit menu.
      * 
      * @return IMenuManager.
      */
     private IMenuManager createEditMenu() {
         IMenuManager editMenu = new MenuManager(Messages.ActionBuilderEdit,
                 IWorkbenchActionConstants.M_EDIT);
         /* 
          * JubulaEditSeparator1 = Create>
          * JubulaEditSeparator1 = Add>
          * JubulaEditSeparator3 = Rename/Delete
          */
         editMenu.add(m_redo);
         editMenu.add(m_undo);
         editMenu.add(new Separator()); 
         editMenu.add(m_cut);
         editMenu.add(m_paste);
         editMenu.add(new Separator()); 
         editMenu.add(new GroupMarker("JubulaEditSeparator1")); //$NON-NLS-1$
         editMenu.add(new Separator()); 
         editMenu.add(new GroupMarker("JubulaEditSeparator2")); //$NON-NLS-1$
         editMenu.add(new Separator()); 
         editMenu.add(new GroupMarker("JubulaEditSeparator3")); //$NON-NLS-1$
         return editMenu;
     }
 
     /**
      * Creates a Search menu.
      * 
      * @return the menu manager for the created menu.
      */
     private IMenuManager createSearchMenu() {
         IMenuManager searchMenu = new MenuManager(Messages.ActionBuilderSearch,
                 "org.eclipse.search.menu"); //$NON-NLS-1$
         return searchMenu;
     }
 
     /**
      * Creates a Run menu.
      * 
      * @return the menu manager for the created menu.
      */
     private IMenuManager createRunMenu() {
         IMenuManager runMenu = new MenuManager(Messages.ActionBuilderRun,
                 "org.eclipse.ui.run"); //$NON-NLS-1$
         return runMenu;
     }
 
     /**
      * Creates a File menu.
      * 
      * @return the menu manager for the created menu.
      */
     private IMenuManager createFileMenu() {
         IMenuManager fileMenu = new MenuManager(Messages
                 .ActionBuilderMyFileEntry,
                 IWorkbenchActionConstants.M_FILE);
         fileMenu.add(m_fileSave);
         fileMenu.add(m_fileSaveAll);
         fileMenu.add(new Separator());
         CommandHelper.createContributionPushItem(fileMenu,
                 IWorkbenchCommandConstants.FILE_RENAME);
         CommandHelper.createContributionPushItem(fileMenu,
                 IWorkbenchCommandConstants.FILE_REFRESH);
         fileMenu.add(new Separator());
         fileMenu.add(m_openWorkspaceAction);
         CommandHelper.createContributionPushItem(fileMenu,
                 IWorkbenchCommandConstants.FILE_RESTART);
         fileMenu.add(new Separator());
         CommandHelper.createContributionPushItem(fileMenu,
                 ActionFactory.IMPORT.getCommandId());
         CommandHelper.createContributionPushItem(fileMenu,
                 ActionFactory.EXPORT.getCommandId());
         fileMenu.add(new Separator()); 
         fileMenu.add(m_quitAction);
         return fileMenu;
     }
 
     /**
      * Creates the Window menu.
      * @return IMenuManager.
      */
     private IMenuManager createWindowMenu() {
         IMenuManager windowMenu = new MenuManager(Messages
                 .ActionBuilderWindowEntry,
                 IWorkbenchActionConstants.M_WINDOW);
         windowMenu.add(createOpenPersp());
         windowMenu.add(createShowView());
         windowMenu.add(new Separator());
         windowMenu.add(m_resetPersp);
         windowMenu.add(new Separator());
         windowMenu.add(m_prefAction);
         return windowMenu;
     }
 
     /**
      * Creates Show View menu.
      * @return IMenuManager.
      */
     private IMenuManager createShowView() {
         IMenuManager showView = new MenuManager(Messages.ActionBuildershowView);
         showView.add(
                 ContributionItemFactory.VIEWS_SHORTLIST.create(m_window));
         return showView;
     }
         
     /**
      * Creates Open Perspective
      * @return IMenuManager.
      */
     private IMenuManager createOpenPersp() {
         IMenuManager openPersp = new MenuManager(Messages
                 .ActionBuilderopenPerspective);
         openPersp.add(
             ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(m_window));
         return openPersp;
     }    
     
     /**
      * Creates the help menu.
      * @return an IMenuManager.
      */
     private IMenuManager createHelpMenu() {
         IMenuManager helpMenu = new MenuManager(Messages
                 .ActionBuilderMyHelpEntry, 
                 IWorkbenchActionConstants.M_HELP);
         helpMenu.add(m_intro);
         helpMenu.add(m_helpCont);
         helpMenu.add(new Separator("helpEnd")); //$NON-NLS-1$
 
         CommandHelper.createContributionPushItem(
                 helpMenu, IWorkbenchCommandConstants.HELP_ABOUT);
 
         helpMenu.add(new Separator());
         helpMenu.add(CommandHelper.createContributionItem(
                 "org.eclipse.ui.cheatsheets.openCheatSheet", null, //$NON-NLS-1$ 
                 Messages.ActionBuilderHelpCheatSheetsItem, 
                 CommandContributionItem.STYLE_PUSH));
         
         return helpMenu;
     }
     
     
     /**
      * designs the toolbar
      * 
      * @param coolBar
      *            ICoolBarManager
      */
     private void fillCoolBar(ICoolBarManager coolBar) {
         IToolBarManager toolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolBar)); 
        ActionContributionItem save = new ActionContributionItem(m_fileSave);
        toolBar.add(save);    
     }
 
     /**
      * 
      * disposes the actions
      */
     public void dispose() {
         if (m_cut != null) {
             m_cut.dispose();
         }
         if (m_fileSave != null) {
             m_fileSave.dispose();
         }
         if (m_helpCont != null) {
             m_helpCont.dispose();
         } 
         if (m_paste != null) {
             m_paste.dispose();
         }
         if (m_prefAction != null) {
             m_prefAction.dispose();
         }   
         if (m_quitAction != null) {
             m_quitAction.dispose();
         }
         if (m_resetPersp != null) {
             m_resetPersp.dispose();
         }
         if (m_openWorkspaceAction != null) {
             m_openWorkspaceAction.dispose();
         }
     }
 }
