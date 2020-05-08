 package org.pentaho.pac.client.scheduler;
 
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class SolutionRepositoryItemPicker extends FlexTable {
 
   private TextBox solutionTb = new TextBox();
   private TextBox pathTb = new TextBox();
   private TextBox actionTb = new TextBox();
 
   public SolutionRepositoryItemPicker() {
     super();
 
     setCellPadding( 0 );
     setCellSpacing( 0 );
     
     Label l = new Label( "Solution:" );
     setWidget( 0 , 0, l );
     setWidget( 0 , 1, solutionTb );
     
     l = new Label( "Path:" );
     setWidget( 1 , 0, l );
     setWidget( 1 , 1, pathTb );
     
     l = new Label( "Action:" );
     setWidget( 2 , 0, l );
     setWidget( 2 , 1, actionTb );
 
   }
   
   public void reset() {    
     // TODO sbarkdull tmp
    solutionTb.setText( "" );
    pathTb.setText( "" );
    actionTb.setText( "" );
   }
   
   public String getSolution() {
     return solutionTb.getText();
   }
   
   public void setSolution( String solution ) {
     solutionTb.setText( solution );
   }
   
   public String getPath() {
     return pathTb.getText();
   }
   
   public void setPath( String path ) {
     pathTb.setText( path );
   }
   
   public String getAction() {
     return actionTb.getText();
   }
   
   public void setAction( String action ) {
     actionTb.setText( action );
   }
   
 }
