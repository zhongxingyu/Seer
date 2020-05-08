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
 
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.pentaho.pac.client.common.EnumException;
 import org.pentaho.pac.client.common.ui.SimpleGroupBox;
 import org.pentaho.pac.client.common.ui.widget.ValidationLabel;
 import org.pentaho.pac.client.common.util.TimeUtil;
 import org.pentaho.pac.client.scheduler.RecurrenceEditor.TemporalValue;
 
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * 
  * @author Steven Barkdull
  *
  */
 public class ScheduleEditor extends FlexTable {
 
   enum ScheduleType {
     RUN_ONCE(0, "Run Once"), 
     SECONDS(1, "Seconds"), 
     MINUTES(2, "Minutes"), 
     HOURS(3, "Hours"), 
     DAILY(4, "Daily"), 
     WEEKLY(5, "Weekly"), 
     MONTHLY(6, "Monthly"), 
     YEARLY(7, "Yearly"),
     CRON(8, "Cron");
 
     private ScheduleType(int value, String name) {
       this.value = value;
       this.name = name;
     }
 
     private final int value;
 
     private final String name;
 
     private static ScheduleType[] scheduleValue = {
       RUN_ONCE,
       SECONDS, 
       MINUTES, 
       HOURS,
       DAILY, 
       WEEKLY, 
       MONTHLY, 
       YEARLY,
       CRON
     };
 
     public int value() {
       return value;
     }
 
     public String toString() {
       return name;
     }
 
     public static ScheduleType get(int idx) {
       return scheduleValue[idx];
     }
 
     public static int length() {
       return scheduleValue.length;
     }
     
     public static ScheduleType stringToScheduleType( String strSchedule ) throws EnumException {
       for (ScheduleType v : EnumSet.range(ScheduleType.RUN_ONCE, ScheduleType.CRON)) {
         if ( v.toString().equals( strSchedule ) ) {
           return v;
         }
       }
       throw new EnumException( "Invalid String for temporal value: " + scheduleValue );
     }
   } /* end enum */
 
   private RunOnceEditor runOnceEditor = null;
   private RecurrenceEditor recurrenceEditor = null;
   private CronEditor cronEditor = null;
   // TODO sbarkdull, can this be static?
   private Map<ScheduleType, Panel> scheduleTypeMap = new HashMap<ScheduleType, Panel>();
   private static Map<TemporalValue, ScheduleType> temporalValueToScheduleTypeMap = createTemporalValueToScheduleTypeMap();
   private static Map<ScheduleType, TemporalValue> scheduleTypeToTemporalValueMap = createScheduleTypeMapToTemporalValue();
   
   private TextBox nameTb = new TextBox();
   private TextBox groupNameTb = new TextBox();
   private TextBox descriptionTb = new TextBox();
   private ListBox scheduleCombo = null;
   private SimpleGroupBox scheduleGB = null;
   
   // validation labels
   private ValidationLabel nameLabel;
   private ValidationLabel groupNameLabel;
 
 //  private String cronStr = null;
 //  private String repeatInSecs = null;
   
   private static final String DEFAULT_NAME = ""; //$NON-NLS-1$
   private static final String DEFAULT_GROUP_NAME = ""; //$NON-NLS-1$
   private static final String DEFAULT_DESCRIPTION = ""; //$NON-NLS-1$
 
   public ScheduleEditor() {
     super();
 
     setCellPadding( 0 );
     setCellSpacing( 0 );
     
     int rowNum = 0;
     nameLabel = new ValidationLabel( "Name:" );
     setWidget( rowNum , 0, nameLabel );
     setWidget( rowNum , 1, nameTb );
     
     rowNum++;
     groupNameLabel = new ValidationLabel( "Group:" );
     setWidget( rowNum, 0, groupNameLabel );
     setWidget( rowNum, 1, groupNameTb );
 
     rowNum++;
     Label l = new Label( "Description:" );
     setWidget( rowNum, 0, l );
     setWidget( rowNum, 1, descriptionTb );
 
     rowNum++;
     scheduleCombo = createScheduleCombo();
     l = new Label( "Schedule Type:" );
     setWidget( rowNum, 0, l );
     setWidget( rowNum, 1, scheduleCombo );
 
     rowNum++;
     scheduleGB = new SimpleGroupBox( ScheduleType.RUN_ONCE.toString() + " Editor" );
     VerticalPanel vp = new VerticalPanel();
     scheduleGB.add( vp );
     this.getFlexCellFormatter().setColSpan( 4, 0, 3 );
     setWidget( rowNum, 0, scheduleGB );
 
     runOnceEditor = new RunOnceEditor();
     vp.add( runOnceEditor );
     scheduleTypeMap.put( ScheduleType.RUN_ONCE, runOnceEditor );
     runOnceEditor.setVisible( true );
     
     recurrenceEditor = new RecurrenceEditor();
     vp.add( recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.SECONDS, recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.MINUTES, recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.HOURS, recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.DAILY, recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.WEEKLY, recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.MONTHLY, recurrenceEditor );
     scheduleTypeMap.put( ScheduleType.YEARLY, recurrenceEditor );
     recurrenceEditor.setVisible( false );
     
     cronEditor = new CronEditor();
     vp.add( cronEditor );
     scheduleTypeMap.put( ScheduleType.CRON, cronEditor );
     cronEditor.setVisible( false );
   }
 
  public void reset() {
     setName( DEFAULT_NAME );
     setGroupName( DEFAULT_GROUP_NAME );
     setDescription( DEFAULT_DESCRIPTION );
   }
   
   public String getName() {
     return nameTb.getText();
   }
   
   public void setName( String name ) {
     nameTb.setText( name );
   }
   
   public String getGroupName() {
     return groupNameTb.getText();
   }
   
   public void setGroupName( String groupName ) {
     groupNameTb.setText( groupName );
   }
   
   public String getDescription() {
     return descriptionTb.getText();
   }
   
   public void setDescription( String description ) {
     descriptionTb.setText( description );
   }
 
   public String getCronString() {
     switch ( getScheduleType() ) {
       case RUN_ONCE:
         return null;
       case SECONDS: // fall through
       case MINUTES: // fall through
       case HOURS: // fall through
       case DAILY: // fall through
       case WEEKLY: // fall through
       case MONTHLY: // fall through
       case YEARLY:
         return recurrenceEditor.getCronString();
       case CRON:
         return cronEditor.getCronString();
       default:
         throw new RuntimeException( "Invalid Run Type: " + getScheduleType().toString() );
     }
   }
   /**
    * 
    * @param cronStr
    * @throws CronParseException if cronStr is not a valid CRON string.
    */
   public void setCronString( String cronStr ) throws CronParseException {
 
     CronParser cp = new CronParser( cronStr );
     String recurrenceStr = null;
     recurrenceStr = cp.parseToRecurrenceString(); // throws CronParseException
 
     if ( null != recurrenceStr ) {
       recurrenceEditor.inititalizeWithRecurrenceString( recurrenceStr );
       TemporalValue tv = recurrenceEditor.getTemporalState();
       ScheduleType rt = temporalValueToScheduleType( tv );
       setScheduleType( rt );
     } else {
       // its a cron string that cannot be parsed into a recurrence string, switch to cron string editor.
       setScheduleType( ScheduleType.CRON );
     }
     
     cronEditor.setCronString( cronStr );
   }
 
   
   /**
    * 
    * @return null if the selected schedule does not support repeat-in-seconds, otherwise
    * return the number of seconds between schedule execution.
    * @throws RuntimeException if the temporal value is invalid. This
    * condition occurs as a result of programmer error.
    */
   public Integer getRepeatInSecs() throws RuntimeException {
     return recurrenceEditor.getRepeatInSecs();
   }
 
   public void setRepeatInSecs( Integer repeatInSecs ) {
     recurrenceEditor.inititalizeWithRepeatInSecs( repeatInSecs );
     TemporalValue tv = recurrenceEditor.getTemporalState();
     ScheduleType rt = temporalValueToScheduleType( tv );
     setScheduleType( rt );
   }
   
   private ListBox createScheduleCombo() {
     final ScheduleEditor localThis = this;
     ListBox lb = new ListBox();
     lb.setVisibleItemCount( 1 );
     lb.setStyleName("scheduleCombo"); //$NON-NLS-1$
     lb.addChangeListener( new ChangeListener() {
       public void onChange(Widget sender) {
         localThis.handleScheduleChange();
       }
     });
     // add all schedule types to the combobox
     for (ScheduleType schedType : EnumSet.range(ScheduleType.RUN_ONCE, ScheduleType.CRON)) {
       lb.addItem( schedType.toString() );
     }
     lb.setItemSelected( 0, true );
 
     return lb;
   }
   
   public ScheduleType getScheduleType() {
     String selectedValue = scheduleCombo.getValue( scheduleCombo.getSelectedIndex() );
     return ScheduleType.stringToScheduleType( selectedValue );
   }
   
   public void setScheduleType( ScheduleType scheduleType ) {
     scheduleCombo.setSelectedIndex( scheduleType.value() );
     selectScheduleTypeEditor( scheduleType );
   }
   
   public void setStartTime( String startTime ) {
     runOnceEditor.setStartTime( startTime );
     recurrenceEditor.setStartTime( startTime );
   }
   
   public String getStartTime() {
     switch ( getScheduleType() ) {
       case RUN_ONCE:
         return runOnceEditor.getStartTime();
       case SECONDS: // fall through
       case MINUTES: // fall through
       case HOURS: // fall through
       case DAILY: // fall through
       case WEEKLY: // fall through
       case MONTHLY: // fall through
       case YEARLY:
         return recurrenceEditor.getStartTime();
       case CRON:
         return cronEditor.getStartTime();
       default:
         throw new RuntimeException( "Invalid Run Type: " + getScheduleType().toString() );
     }
   }
 
   public void setStartDate( Date startDate ) {
     runOnceEditor.setStartDate( startDate );
     recurrenceEditor.setStartDate( startDate );
     cronEditor.setStartDate( startDate );
   }
   
   public Date getStartDate() {
     switch ( getScheduleType() ) {
       case RUN_ONCE:
         Date startDate = runOnceEditor.getStartDate();
         String startTime  = runOnceEditor.getStartTime();
         Date startDateTime = TimeUtil.getDateTime( startTime, startDate );
         return startDateTime;
       case SECONDS: // fall through
       case MINUTES: // fall through
       case HOURS: // fall through
       case DAILY: // fall through
       case WEEKLY: // fall through
       case MONTHLY: // fall through
       case YEARLY:
         return recurrenceEditor.getStartDate();
       case CRON:
         return cronEditor.getStartDate();
       default:
         throw new RuntimeException( "Invalid Run Type: " + getScheduleType().toString() );
     }
   }
 
   public void setEndDate( Date endDate ) {
     recurrenceEditor.setEndDate( endDate );
     cronEditor.setEndDate( endDate );
   }
 
   public Date getEndDate() {
     switch ( getScheduleType() ) {
       case RUN_ONCE:
         return null;
       case SECONDS: // fall through
       case MINUTES: // fall through
       case HOURS: // fall through
       case DAILY: // fall through
       case WEEKLY: // fall through
       case MONTHLY: // fall through
       case YEARLY:
         return recurrenceEditor.getEndDate();
       case CRON:
         return cronEditor.getEndDate();
       default:
         throw new RuntimeException( "Invalid Run Type: " + getScheduleType().toString() );
     }
   }
 
   public void setNoEndDate() {
     recurrenceEditor.setNoEndDate();
     cronEditor.setNoEndDate();
   }
 
   public void setEndBy() {
     recurrenceEditor.setEndBy();
     cronEditor.setEndBy();
   }
 
   private void handleScheduleChange() throws EnumException {
     ScheduleType schedType = getScheduleType();
     selectScheduleTypeEditor( schedType );
   }
 
 
   private void selectScheduleTypeEditor( ScheduleType scheduleType ) {
     // hide all panels
     for ( Map.Entry<ScheduleType, Panel> me : scheduleTypeMap.entrySet() ) {
       me.getValue().setVisible( false );
     }
     // show the selected panel
     Panel p = scheduleTypeMap.get( scheduleType );
     p.setVisible( true );
     TemporalValue tv = scheduleTypeToTemporalValue( scheduleType );
     if ( null != tv ) {
       // force the recurrence editor to display the appropriate ui
       recurrenceEditor.setTemporalState( tv );
     }
     // set the caption in the group box
     scheduleGB.setCaption( scheduleType.toString() + " Editor"  );
   }
   
   /**
    * @param errorMsg String null or "" to clear the error msg, else
    * set the error msg to <param>errorMsg</param>.
    */
   public void setNameError( String errorMsg ) {
     nameLabel.setErrorMsg( errorMsg );
   }
   
   public void setGroupNameError( String errorMsg ) {
     groupNameLabel.setErrorMsg( errorMsg );
   }
   
   private static Map<TemporalValue, ScheduleType> createTemporalValueToScheduleTypeMap() {
     Map<TemporalValue, ScheduleType> m = new HashMap<TemporalValue, ScheduleType>();
 
     m.put( TemporalValue.SECONDS, ScheduleType.SECONDS );
     m.put( TemporalValue.MINUTES, ScheduleType.MINUTES );
     m.put( TemporalValue.HOURS, ScheduleType.HOURS );
     m.put( TemporalValue.DAILY, ScheduleType.DAILY );
     m.put( TemporalValue.WEEKLY, ScheduleType.WEEKLY );
     m.put( TemporalValue.MONTHLY, ScheduleType.MONTHLY );
     m.put( TemporalValue.YEARLY, ScheduleType.YEARLY );
 
     return m;
   }
   
   private static Map<ScheduleType, TemporalValue> createScheduleTypeMapToTemporalValue() {
     Map<ScheduleType, TemporalValue> m = new HashMap<ScheduleType, TemporalValue>();
 
     m.put( ScheduleType.SECONDS, TemporalValue.SECONDS );
     m.put( ScheduleType.MINUTES, TemporalValue.MINUTES );
     m.put( ScheduleType.HOURS, TemporalValue.HOURS );
     m.put( ScheduleType.DAILY, TemporalValue.DAILY );
     m.put( ScheduleType.WEEKLY, TemporalValue.WEEKLY );
     m.put( ScheduleType.MONTHLY, TemporalValue.MONTHLY );
     m.put( ScheduleType.YEARLY, TemporalValue.YEARLY );
 
     return m;
   }
   
   private static ScheduleType temporalValueToScheduleType( TemporalValue tv ) {
     return temporalValueToScheduleTypeMap.get( tv );
   }
   
   private static TemporalValue scheduleTypeToTemporalValue( ScheduleType st ) {
     return scheduleTypeToTemporalValueMap.get( st );
   }
 }
