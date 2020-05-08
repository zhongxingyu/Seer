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
 package org.pentaho.pac.client.scheduler.view;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.TableListCtrl;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.client.scheduler.model.Schedule;
 
 public class SchedulesListCtrl extends TableListCtrl<Schedule> {
 
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   // requirement: Name, Group Name, Schedule State, Next Fire, Previous Fire, Subscriber Count 
   public static final String[] COLUMN_HEADER_TITLE = {
     MSGS.scheduleGroupName(),
     MSGS.state(),
    MSGS.fireTimeLastNext(),
    MSGS.subscriberCount(),
    MSGS.description()
   };
 //  private static final String[] COLUMN_HEADER_TITLE = {
 //    MSGS.scheduleGroupName(),
 //    MSGS.description(),
 //    MSGS.fireTimeLastNext(),
 //    MSGS.state()
 //  };
   
   public SchedulesListCtrl()
   {
     super( COLUMN_HEADER_TITLE );
     
     setHeight( "230px" ); //$NON-NLS-1$
     setTableStyleName( "schedulesTable" ); //$NON-NLS-1$
     setTableHeaderStyleName( "schedulesTableHeader" ); //$NON-NLS-1$
   }
   
   public List<Schedule> getSelectedSchedules() {
     List<Integer> selectedIdxs = getSelectedIndexes();
     final List<Schedule> scheduleList = new LinkedList<Schedule>();
     
     for ( int rowNum : selectedIdxs ) {
       Schedule schedule = getRowData( rowNum );
       scheduleList.add( schedule );
     }
     return scheduleList;
   }
   
   public void setStateToLoading() {
     setTempMessage( "Loading..." );
   }
   
   public void clearStateLoading() {
     clearTempMessage();
   }
 }
