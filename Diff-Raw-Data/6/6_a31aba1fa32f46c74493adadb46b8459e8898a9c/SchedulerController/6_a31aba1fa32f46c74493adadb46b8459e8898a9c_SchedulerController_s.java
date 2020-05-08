 /*
  * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * @created May 19, 2008
  * 
  */
 package org.pentaho.pac.client.scheduler;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.ICallback;
 import org.pentaho.pac.client.common.ui.dialog.ConfirmDialog;
 import org.pentaho.pac.client.common.ui.dialog.MessageDialog;
 import org.pentaho.pac.client.common.util.TimeUtil;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.PushButton;
 
 
 public class SchedulerController {
 
   private SchedulerPanel schedulerPanel = null; // this is the view
   private SchedulesModel schedulesModel = null;   // this is the model
 
   private SchedulesListController schedulesListController = null;
   private ScheduleCreatorDialog scheduleCreatorDialog = null;
   
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   private static final int INVALID_SCROLL_POS = -1;
   
   public SchedulerController( SchedulerPanel schedulerPanel ) {
     assert (null != schedulerPanel ) : "schedulerPanel cannot be null.";
     
     this.schedulerPanel = schedulerPanel;
     this.scheduleCreatorDialog = new ScheduleCreatorDialog();
   }
   
   public void init() {
     
     if ( !isInitialized() ) {
       schedulerPanel.init();
       schedulesListController = new SchedulesListController(this.schedulerPanel.getSchedulesListCtrl() );
       loadJobsTable();
       SchedulerToolbar schedulerToolbar = schedulerPanel.getSchedulerToolbar();
       final SchedulerController localThis = this;
       
       schedulerToolbar.setOnSelectAllListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           localThis.handleSelectAllSchedules();
         }
       });
       
       schedulerToolbar.setOnUnselectAllListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           localThis.handleUnselectAllSchedules();
         }
       });
       
       schedulerToolbar.setOnCreateListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           localThis.handleCreateSchedule();
         }
       });
       
       schedulerToolbar.setOnUpdateListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           localThis.handleUpdateSchedule();
         }
       });
 
       schedulerToolbar.setOnDeleteListener( new ICallback<Object>() {
         public void onHandle(Object o) {
           localThis.handleDeleteSchedules();
         }
       });
       
       schedulerToolbar.setOnResumeListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           localThis.handleResumeSchedules();
         }
       });
       
       schedulerToolbar.setOnPauseListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           localThis.handlePauseSchedules();
         }
       });
       
       schedulerToolbar.setOnRefreshListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           loadJobsTable();
         }
       });
       
       // TODO sbarkdull, uh, ya, this needs some work
       schedulerToolbar.setOnToggleResumePauseAllListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           PushButton b = (PushButton)o;
           // TODO sbarkdull
           b.setText( "toggled" );
           
           b.setTitle( "yep" );
         }
       });
       
       schedulerToolbar.setOnFilterListChangeListener( new ICallback<Object>() { 
         public void onHandle(Object o) {
           updateSchedulesTable();
         }
       });
     } // end isInitialized
   }
   
   private boolean isInitialized() {
     return null != schedulesModel;
   }
   
   private void initFilterList() {
     
     Set<String> groupNames = new HashSet<String>();
     List<Schedule> scheduleList = schedulesModel.getScheduleList();
     for ( int ii=0; ii<scheduleList.size(); ++ii ) {
       Schedule s = scheduleList.get( ii );
       String groupName = s.getJobGroup();
       if ( !groupNames.contains( groupName ) ) {
         groupNames.add( groupName );
       }
     }
     schedulerPanel.getSchedulerToolbar().clearFilters();
     Iterator<String> it = groupNames.iterator();
     
     schedulerPanel.getSchedulerToolbar().addFilterItem( SchedulerToolbar.ALL_FILTER );
     while ( it.hasNext() ) {
       String name = it.next();
       schedulerPanel.getSchedulerToolbar().addFilterItem(name );
     }
   }
   
   private List<Schedule> getFilteredSchedulesList() {
     List<Schedule> filteredList = null;
     String filterVal = schedulerPanel.getSchedulerToolbar().getFilterValue();
     if ( SchedulerToolbar.ALL_FILTER.equals( filterVal ) ) {
       filteredList = schedulesModel.getScheduleList();
     } else {
       filteredList = new ArrayList<Schedule>();
       List<Schedule> scheduleList = schedulesModel.getScheduleList();
       for ( int ii=0; ii<scheduleList.size(); ++ii ) {
         Schedule s = scheduleList.get( ii );
         if ( filterVal.equals( s.getJobGroup() ) ) {
           filteredList.add( s );
         }
       }
     }
     return filteredList;
   }
   
   private void updateSchedulesTable() {
     schedulesListController.updateSchedulesTable( getFilteredSchedulesList() );
   }
   
   private void loadJobsTable()
   {
     final int currScrollPos = schedulerPanel.getSchedulesListCtrl().getScrollPosition();
     
     PacServiceFactory.getSchedulerService().getJobNames(
         new AsyncCallback<List<Schedule>>() {
           public void onSuccess( List<Schedule> pSchedulesList ) {
             schedulesModel = new SchedulesModel();
             schedulesModel.add( pSchedulesList );
             initFilterList();
             updateSchedulesTable();
             if ( INVALID_SCROLL_POS != currScrollPos ) { 
               schedulerPanel.getSchedulesListCtrl().setScrollPosition( currScrollPos );
             }
           }
     
           public void onFailure(Throwable caught) {
             MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
                 caught.getMessage() );
             messageDialog.center();
           }
         }
       );
   }
   
   private void updateSchedule() {
 
     SchedulesListCtrl schedulesListCtrl = schedulerPanel.getSchedulesListCtrl();
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     Schedule oldSchedule = scheduleList.get( 0 );
     
     // TODO, List<Schedule> is probably not what we will get back
     AsyncCallback<List<Schedule>> responseCallback = new AsyncCallback<List<Schedule>>() {
       public void onSuccess( List<Schedule> pSchedulesList ) {
         MessageDialog messageDialog = new MessageDialog( "Kool!", 
             "Success, I guess!" );
         messageDialog.center();
         scheduleCreatorDialog.hide();
         loadJobsTable();
       }
 
       public void onFailure(Throwable caught) {
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     };
 
     ScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
     
     String cronStr = scheduleEditor.getCronString();
     Date startDate = TimeUtil.getDateTime(scheduleEditor.getStartTime(),
           scheduleEditor.getStartDate());
     Date endDate = scheduleEditor.getEndDate();
     if ( null != cronStr ) {
       PacServiceFactory.getSchedulerService().updateCronJob(
           oldSchedule.getJobName(),
           oldSchedule.getJobGroup(),
           scheduleEditor.getName().trim(), 
           scheduleEditor.getGroupName().trim(), 
           scheduleEditor.getDescription().trim(),
           startDate,
           endDate,
           cronStr.trim(), 
           scheduleCreatorDialog.getSolutionRepositoryItemPicker().getSolution().trim(),
           scheduleCreatorDialog.getSolutionRepositoryItemPicker().getPath().trim(),
           scheduleCreatorDialog.getSolutionRepositoryItemPicker().getAction().trim(),
           responseCallback
         );
     } else {
       String repeatTimeMillisecs = Integer.toString( TimeUtil.secsToMillisecs( 
             Integer.parseInt( scheduleEditor.getRepeatInSecs() ) ) );
 
       PacServiceFactory.getSchedulerService().updateRepeatJob(
           oldSchedule.getJobName(),
           oldSchedule.getJobGroup(),
           scheduleEditor.getName().trim(), 
           scheduleEditor.getGroupName().trim(), 
           scheduleEditor.getDescription().trim(), 
           startDate,
           endDate,
           repeatTimeMillisecs.trim(), 
           scheduleCreatorDialog.getSolutionRepositoryItemPicker().getSolution().trim(),
           scheduleCreatorDialog.getSolutionRepositoryItemPicker().getPath().trim(),
           scheduleCreatorDialog.getSolutionRepositoryItemPicker().getAction().trim(),
           responseCallback
         );
     }
   }
   
   private void createSchedule() {
     // TODO, List<Schedule> is probably not what we will get back
     AsyncCallback<List<Schedule>> responseCallback = new AsyncCallback<List<Schedule>>() {
       public void onSuccess( List<Schedule> pSchedulesList ) {
         MessageDialog messageDialog = new MessageDialog( "Kool!", 
             "Success, I guess!" );
         messageDialog.center();
         scheduleCreatorDialog.hide();
         loadJobsTable();
       }
 
       public void onFailure(Throwable caught) {
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     };
     // TODO sbarkdull scheduleCreatorDialog -> scheduleEditorDialog
     ScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
     if ( scheduleEditor.isRecurrenceEditorValid() ) {
       String cronStr = scheduleEditor.getCronString();
       Date startDate = TimeUtil.getDateTime(scheduleEditor.getStartTime(),
             scheduleEditor.getStartDate());
       Date endDate = scheduleEditor.getEndDate();
       if ( null != cronStr ) {
         PacServiceFactory.getSchedulerService().createCronJob(
             scheduleEditor.getName().trim(), 
             scheduleEditor.getGroupName().trim(), 
             scheduleEditor.getDescription().trim(), 
             startDate,
             endDate,
             cronStr.trim(), 
             scheduleCreatorDialog.getSolutionRepositoryItemPicker().getSolution().trim(),
             scheduleCreatorDialog.getSolutionRepositoryItemPicker().getPath().trim(),
             scheduleCreatorDialog.getSolutionRepositoryItemPicker().getAction().trim(),
             responseCallback
           );
       } else {
         String repeatTimeMillisecs = Integer.toString( TimeUtil.secsToMillisecs( 
               Integer.parseInt( scheduleEditor.getRepeatInSecs() ) ) );
         PacServiceFactory.getSchedulerService().createRepeatJob(
             scheduleEditor.getName().trim(), 
             scheduleEditor.getGroupName().trim(), 
             scheduleEditor.getDescription().trim(), 
             startDate,
             endDate,
             repeatTimeMillisecs.trim(), 
             scheduleCreatorDialog.getSolutionRepositoryItemPicker().getSolution().trim(),
             scheduleCreatorDialog.getSolutionRepositoryItemPicker().getPath().trim(),
             scheduleCreatorDialog.getSolutionRepositoryItemPicker().getAction().trim(),
             responseCallback
           );
       }
     } else {
       // recurrence editor is not valid, generate a one-shot schedule
     }
   }
   
   private void deleteSelectedSchedules() {
    
     SchedulesListCtrl schedulesListCtrl = schedulerPanel.getSchedulesListCtrl();
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> callback = new AsyncCallback<Object>() {
       public void onSuccess(Object result) {
         loadJobsTable();
       }
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     };
     PacServiceFactory.getSchedulerService().deleteJobs( scheduleList, callback );
     
   }
   
   private void handleSelectAllSchedules() {
     schedulerPanel.getSchedulesListCtrl().selectAll();
   }
   
   private void handleUnselectAllSchedules() {
     schedulerPanel.getSchedulesListCtrl().unselectAll();
   }
 
   private void handleCreateSchedule() {
     final SchedulerController localThis = this;
     
     scheduleCreatorDialog.getScheduleEditor().reset();
     scheduleCreatorDialog.getSolutionRepositoryItemPicker().reset();
     scheduleCreatorDialog.setOnOkHandler( new ICallback<Object>() {
       public void onHandle(Object o) {
         localThis.createSchedule();
       }
     });
     scheduleCreatorDialog.center();
   }
   
   private void handleUpdateSchedule() {
     final SchedulerController localThis = this;
 
     SchedulesListCtrl schedulesListCtrl = schedulerPanel.getSchedulesListCtrl();
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     scheduleCreatorDialog.setOnOkHandler( new ICallback<Object>() {
       public void onHandle(Object o) {
         localThis.updateSchedule();
       }
     });
     // the update button should be enabled/disabled to guarantee that one and only one schedule is selected
     assert scheduleList.size() == 1 : "When clicking update, exactly one schedule should be seleced.";
     
     Schedule s = scheduleList.get( 0 );
     ScheduleEditor scheduleEditor = scheduleCreatorDialog.getScheduleEditor();
     initScheduleEditor( scheduleEditor, s );
 
     scheduleCreatorDialog.center();
   }
   
   /**
    * initialize the <code>scheduleEditor</code>'s user interface with 
    * the contents of the <code>sched</code>.
    * 
    * @param scheduleEditor
    * @param sched
    */
   private static void initScheduleEditor( ScheduleEditor scheduleEditor, Schedule sched ) {
     scheduleEditor.reset();
 
     scheduleEditor.setName( sched.getJobName() );
     scheduleEditor.setGroupName( sched.getJobGroup() );
     scheduleEditor.setDescription( sched.getDescription() );
     String cronStr = sched.getCronString();
     String repeatInMillisecs;
     if ( null != cronStr ) {
       scheduleEditor.setCronString( sched.getCronString() );
     } else if ( null != ( repeatInMillisecs = sched.getRepeatTimeInMillisecs() ) ) {
       String strRepeatTimeInSecs = Integer.toString( TimeUtil.millsecondsToSecs( Integer.parseInt( repeatInMillisecs ) ) );
       scheduleEditor.setRepeatInSecs( strRepeatTimeInSecs );
     } // else we got a problem.
 
     String strDate = sched.getStartDate();
     if ( null != strDate ) {
      String startTime = TimeUtil.getTime( strDate );
       scheduleEditor.setStartTime( startTime );
     }
     
     strDate = sched.getEndDate();
     if ( null != strDate ) {
       scheduleEditor.setEndBy();
       Date endDate = TimeUtil.getDate( strDate );
       scheduleEditor.setEndDate(endDate);
     } else {
       scheduleEditor.setNoEndDate();
     }
   }
   
   private void handleDeleteSchedules() {
     final SchedulerController localThis = this;
     final ConfirmDialog confirm = new ConfirmDialog( "Confirm Delete",
         "Are you sure you would like to delete all checked schedules?" );
     confirm.setOnOkHandler( new ICallback<Object>() {
       public void onHandle( Object o ) {
         confirm.hide();
         localThis.deleteSelectedSchedules();
       }
     });
     confirm.center();
   }
   
   private void handleResumeSchedules() {
     SchedulesListCtrl schedulesListCtrl = schedulerPanel.getSchedulesListCtrl();
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> callback = new AsyncCallback<Object>() {
       public void onSuccess(Object result) {
         loadJobsTable();
       }
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     };
     PacServiceFactory.getSchedulerService().resumeJobs( scheduleList, callback );
   }
   
   private void handlePauseSchedules() {
     SchedulesListCtrl schedulesListCtrl = schedulerPanel.getSchedulesListCtrl();
     final List<Schedule> scheduleList = schedulesListCtrl.getSelectedSchedules();
     
     AsyncCallback<Object> callback = new AsyncCallback<Object>() {
       public void onSuccess(Object result) {
         loadJobsTable();
       }
       public void onFailure(Throwable caught) {
         // TODO sbarkdull
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
       }
     };
     PacServiceFactory.getSchedulerService().pauseJobs( scheduleList, callback );
   }
 }
