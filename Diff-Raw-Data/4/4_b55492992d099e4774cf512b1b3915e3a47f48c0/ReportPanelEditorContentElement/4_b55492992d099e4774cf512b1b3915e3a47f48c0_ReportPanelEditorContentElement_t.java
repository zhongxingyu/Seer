 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Aye Thu
  */
 
 /**
 * $Id: ReportPanelEditorContentElement.java,v 1.10 2004-08-01 00:47:28 shahid.shah Exp $
  */
 
 package com.netspective.sparx.panel.editor;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.util.StringTokenizer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.commons.report.tabular.TabularReport;
 import com.netspective.commons.value.source.RedirectValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.sparx.command.DialogCommand;
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogExecuteException;
 import com.netspective.sparx.form.DialogState;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.panel.HtmlPanel;
 import com.netspective.sparx.panel.HtmlPanelAction;
 import com.netspective.sparx.panel.HtmlPanelActionStates;
 import com.netspective.sparx.panel.HtmlPanelActions;
 import com.netspective.sparx.panel.HtmlPanelBanner;
 import com.netspective.sparx.panel.HtmlPanelFrame;
 import com.netspective.sparx.panel.HtmlPanelValueContext;
 import com.netspective.sparx.panel.QueryReportPanel;
 import com.netspective.sparx.report.tabular.BasicHtmlTabularReport;
 import com.netspective.sparx.report.tabular.HtmlReportAction;
 import com.netspective.sparx.report.tabular.HtmlReportActions;
 import com.netspective.sparx.report.tabular.HtmlTabularReportSkin;
 import com.netspective.sparx.report.tabular.HtmlTabularReportValueContext;
 import com.netspective.sparx.sql.Query;
 import com.netspective.sparx.sql.QueryResultSetDataSource;
 
 /**
  * Content item for report type panel editors
  */
 public class ReportPanelEditorContentElement extends PanelEditorContentElement
 {
     private static final Log log = LogFactory.getLog(ReportPanelEditorContentElement.class);
 
     public static final String RECORD_EDIT_ACTION = "Edit";
     public static final String RECORD_ADD_ACTION = "Add";
     public static final String RECORD_DELETE_ACTION = "Delete";
     public static final String RECORD_MANAGE_ACTION = "Manage";
 
 
     public static final String PANEL_CONTENT_MANAGE_ACTION = "Manage";
     public static final String PANEL_RECORD_DONE_ACTION = "Done";
 
     /* default skin to use to display query report panel */
     public static final String DEFAULT_EDITOR_SKIN = "panel-editor";
 
     /**
      * default name assigned to the dialog defined in the panel editor
      */
     public static final String DEFAULT_DIALOG_NAME = "com.netspective.sparx.panel.editor.PanelEditorContentElement.defaultDialogName";
 
     /**
      * default name assigned to the query defined in the panel editor item
      */
     public static final String DEFAULT_QUERY_NAME = "com.netspective.sparx.panel.editor.PanelEditorContentElement.defaultQueryName";
 
     // name of the referenced query
     private String queryRef;
     // name of the referenced dialog
     private String dialogRef;
     // query associated with this panel
     private Query query;
     // dialog used for editing the report content
     private Dialog dialog;
     // whether or the element has been initialized
     private boolean initialized;
 
     private int pkColumnIndex;
 
 
     public ReportPanelEditorContentElement()
     {
     }
 
     /**
      * Gets the dialog associated with the panel editor
      *
      * @return
      */
     public Dialog getDialog()
     {
         return dialog;
     }
 
     /**
      * Adds a dialog to the panel editor.
      *
      * @param dialog
      */
     public void addDialog(Dialog dialog)
     {
         this.dialog = dialog;
         this.dialog.setName(getParent().getName() + "." + getName());
         this.dialog.setNameSpace(getParent().getNameSpace().getDialogsNameSpace());
         getParent().getProject().getDialogs().add(dialog);
     }
 
     public String getDialogRef()
     {
         return dialogRef;
     }
 
     public void setDialogRef(String dialogRef)
     {
         this.dialogRef = dialogRef;
     }
 
     /**
      * Sets the query defined for the display mode of the record manager
      *
      * @param queryName query name
      */
     public void setQueryRef(String queryName)
     {
         this.queryRef = queryName;
     }
 
     public String getQueryRef()
     {
         return queryRef;
     }
 
     /**
      * Creates a query object
      *
      * @return query
      */
     public Query createQuery()
     {
         return new com.netspective.sparx.sql.Query(getParent().getProject());
     }
 
     /**
      * Creates a dialog object. Mainly used by XDM to construct a child element dialog.
      *
      * @return
      */
     public Dialog createDialog()
     {
         return getParent().getNameSpace().getDialogsNameSpace().createDialog();
     }
 
     /**
      * Creates a dialog object. Mainly used by XDM to construct a child element dialog.
      *
      * @param cls
      *
      * @return
      *
      * @throws NoSuchMethodException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws InvocationTargetException
      */
     public Dialog createDialog(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         return getParent().getNameSpace().getDialogsNameSpace().createDialog(cls);
     }
 
     /**
      * Adds a query object
      *
      * @param query query
      */
     public void addQuery(Query query)
     {
         this.query = query;
         this.query.setName(getName());
         this.query.setNameSpace(getParent().getNameSpace().getQueriesNameSpace());
         //this.project.getQueries().add(query);
     }
 
     /**
      * Gets the column index to use as the primary key when a record is to be edited
      *
      * @return report column index
      */
     public int getPkColumnIndex()
     {
         return pkColumnIndex;
     }
 
     /**
      * Sets the column index to use as the primary key when a record is to be edited
      *
      * @param pkColumnIndex
      */
     public void setPkColumnIndex(int pkColumnIndex)
     {
         this.pkColumnIndex = pkColumnIndex;
     }
 
     /**
      * Gets the query defined for the default display mode of the panel editor
      *
      * @return query name (NULL if query definition is defined instead)
      */
     public Query getQuery()
     {
         return query;
     }
 
     public static String getPkValueFromState(PanelEditorState state)
     {
         if (state.getActiveElementInfo() != null)
         {
             StringTokenizer st = new StringTokenizer(state.getActiveElementInfo());
             if (st.hasMoreTokens())
                 return st.nextToken();
         }
         return null;
     }
 
     /**
      * Creates all the panel editor actions for the  panel editor. This method SHOULD only be called once to populate the
      * panel editor.
      */
     public void initialize()
     {
         if (dialog == null)
         {
             dialog = getParent().getProject().getDialog(dialogRef);
             if (dialog == null)
             {
                 throw new RuntimeException("No valid dialog defined");
             }
         }
         QueryReportPanel qrp = getQuery().getPresentation().getDefaultPanel();
         if (qrp == null)
         {
             qrp = new QueryReportPanel();
             qrp.setQuery(getQuery());
             qrp.setFrame(new HtmlPanelFrame());
             qrp.setDefault(true);
             getQuery().getPresentation().setDefaultPanel(qrp);
         }
         createPanelBannerActions(qrp);
         createPanelFrameActions(qrp);
         createPanelContentActions(qrp);
         setInitialized(true);
 
         BasicHtmlTabularReport report = (BasicHtmlTabularReport) qrp.getReport();
         if (report == null)
         {
             report = new BasicHtmlTabularReport();
             qrp.addReport(report);
         }
         // HIDE THE HEADING
         qrp.getReport().getFlags().setFlag(TabularReport.Flags.HIDE_HEADING);
         qrp.setReportSkin(DEFAULT_EDITOR_SKIN);
     }
 
     /**
      * Creates actions for the report banner.  NO IMPLEMENTATION CURRENTLY.
      *
      * @param qrp
      */
     public void createPanelFrameActions(QueryReportPanel qrp)
     {
 
     }
 
     /**
      * Creates actions for the report banner
      *
      * @param qrp
      */
     public void createPanelBannerActions(QueryReportPanel qrp)
     {
         // Calculate what to display in the banner
         if (qrp.getBanner() == null)
             qrp.setBanner(new HtmlPanelBanner());
 
         HtmlPanelBanner banner = qrp.getBanner();
         HtmlPanelActions actions = new HtmlPanelActions();
         HtmlPanelAction addAction = banner.createAction();
 
         String addUrl = getParent().generatePanelActionUrl(PanelEditor.MODE_ADD);
         addUrl = appendElementInfoToActionUrl(addUrl, PanelEditor.MODE_ADD);
         addAction.setCaption(new StaticValueSource("Add " + getCaption()));
         addAction.setRedirect(new RedirectValueSource(addUrl));
         actions.add(addAction);
         banner.setActions(actions);
     }
 
     /**
      * Creates actions (EDIT and DELETE) for the report content
      *
      * @param qrp the query report panel
      */
     public void createPanelContentActions(QueryReportPanel qrp)
     {
         String editUrl = getParent().generatePanelActionUrl(PanelEditor.MODE_EDIT);
         editUrl = appendElementInfoToActionUrl(editUrl, PanelEditor.MODE_EDIT);
         String deleteUrl = getParent().generatePanelActionUrl(PanelEditor.MODE_DELETE);
         deleteUrl = appendElementInfoToActionUrl(deleteUrl, PanelEditor.MODE_DELETE);
 
         BasicHtmlTabularReport report = (BasicHtmlTabularReport) qrp.getReport();
         HtmlReportActions actions = new HtmlReportActions();
         HtmlReportAction editAction = actions.createAction();
         editAction.setCaption(new StaticValueSource(RECORD_EDIT_ACTION));
         editAction.setHint(new StaticValueSource(RECORD_EDIT_ACTION));
         editAction.setRedirect(new RedirectValueSource(editUrl));
         editAction.setType(new HtmlReportAction.Type(HtmlReportAction.Type.RECORD_EDIT));
 
         HtmlReportAction deleteAction = actions.createAction();
         deleteAction.setCaption(new StaticValueSource(RECORD_DELETE_ACTION));
         deleteAction.setHint(new StaticValueSource(RECORD_DELETE_ACTION));
         deleteAction.setRedirect(new RedirectValueSource(deleteUrl));
         deleteAction.setType(new HtmlReportAction.Type(HtmlReportAction.Type.RECORD_DELETE));
 
         actions.addAction(editAction);
         actions.addAction(deleteAction);
         report.addActions(actions);
     }
 
     /**
      * Calculate and process the state of the all the panel actions based on current context
      *
      * @param nc               current navigation context
      * @param vc               current report panel context
      * @param panelRecordCount total number of records being displayed
      * @param mode             panel mode
      */
     public void prepareQueryReportState(NavigationContext nc, HtmlPanelValueContext vc, int panelRecordCount, int mode)
     {
         HtmlPanelActionStates actionStates = vc.getPanelActionStates();
 
         // currently only two actions are registered within the report content element and they are EDIT and DELETE
         if (mode == PanelEditor.MODE_DISPLAY)
         {
             actionStates.getState("Delete").getStateFlags().setFlag(HtmlPanelAction.Flags.HIDDEN);
         }
     }
 
     /**
      * Appends additional 'information' to the URL specific to this content element type
      *
      * @param url        URL generated by the parent panel editor
      * @param actionMode the mode of the panel editor
      *
      * @return the appended URL
      */
     public String appendElementInfoToActionUrl(String url, int actionMode)
     {
         url = super.appendElementInfoToActionUrl(url, actionMode);
         if (actionMode == PanelEditor.MODE_EDIT || actionMode == PanelEditor.MODE_DELETE)
             url = url + ",${" + pkColumnIndex + "}";
         return url;
     }
 
     /**
      * Renders the element's editor context content.
      *
      * @param writer
      * @param nc
      * @param state
      *
      * @throws IOException
      */
     public void renderEditorContent(Writer writer, NavigationContext nc, PanelEditorState state) throws IOException
     {
         int mode = state.getCurrentMode();
         // set the dialog perspective using the requested mode.
         if (mode == PanelEditor.MODE_ADD)
             nc.getRequest().setAttribute(DialogState.PARAMNAME_PERSPECTIVE, "add");
         else if (mode == PanelEditor.MODE_EDIT)
             nc.getRequest().setAttribute(DialogState.PARAMNAME_PERSPECTIVE, "edit");
         else if (mode == PanelEditor.MODE_DELETE)
             nc.getRequest().setAttribute(DialogState.PARAMNAME_PERSPECTIVE, "delete");
         if (dialogRef != null)
         {
             dialog = getParent().getProject().getDialog(dialogRef);
             if (dialog == null)
             {
                 log.error("Failed to find dialog '" + dialogRef + "' of element '" + getName() + "' in panel editor '" + getParent().getQualifiedName() + "'.");
                 throw new RuntimeException("Failed to find dialog '" + dialogRef + "' of element '" + getName() + "' in panel editor '" + getParent().getQualifiedName() + "'.");
             }
         }
         DialogContext dc = dialog.createContext(nc, nc.getActiveTheme().getDefaultDialogSkin());
         dc.addRetainRequestParams(DialogCommand.DIALOG_COMMAND_RETAIN_PARAMS);
         dc.getHttpRequest().setAttribute(PanelEditor.PANEL_EDITOR_REQ_ATTRIBUTE_PREFIX, state);
 
         dialog.prepareContext(dc);
         try
         {
             dialog.render(writer, dc, true);
         }
         catch (DialogExecuteException dee)
         {
             log.error("Failed to render dialog of element '" + getName() + "' in panel editor '" + getParent().getQualifiedName() + "'.");
             throw new RuntimeException("Failed to render dialog of element '" + getName() + "' in panel editor '" + getParent().getQualifiedName() + "'.");
         }
     }
 
     /**
      * Renders the element's display context content.
      *
      * @param writer
      * @param nc
      * @param state
      *
      * @throws IOException
      */
     public void renderDisplayContent(Writer writer, NavigationContext nc, PanelEditorState state) throws IOException
     {
         int mode = state.getCurrentMode();
 
         HtmlTabularReportSkin skin = null;
         if (mode == PanelEditor.MODE_MANAGE)
             skin = nc.getActiveTheme().getReportSkin("panel-editor-compressed");
         else
             skin = nc.getActiveTheme().getReportSkin("panel-editor");
 
         if (!isInitialized())
             initialize();
         QueryReportPanel qrp = getQuery().getPresentation().getDefaultPanel();
         qrp.setScrollable(true);
         HtmlTabularReportValueContext context = qrp.createContext(nc, skin);
         String activeElement = state.getActiveElement();
         QueryResultSetDataSource dataRoot = (QueryResultSetDataSource) qrp.createDataSource(nc);
 
         if (activeElement != null && activeElement.equals(getName()))
         {
             if (getPkColumnIndex() != -1)
                 dataRoot.setSelectedRowRule(getPkColumnIndex(), ReportPanelEditorContentElement.getPkValueFromState(state));
 
             context.setPanelRenderFlags(HtmlPanel.RENDERFLAG_NOFRAME | PanelEditorContentElement.HIGHLIGHT_ACTIVE_ITEM);
         }
         else
             context.setPanelRenderFlags(HtmlPanel.RENDERFLAG_NOFRAME);
 
         int totalRows = dataRoot.getTotalRows();
         PanelEditorContentState elementState = constructStateObject();
         if (totalRows == 0)
             elementState.setEmptyContent(true);
         state.addElementState(elementState);
         // process the context to calculate the states of the panel actions
         prepareQueryReportState(nc, context, totalRows, mode);
         context.produceReport(writer, dataRoot);
        dataRoot.close();
     }
 }
