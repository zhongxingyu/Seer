 package org.pentaho.pac.client.scheduler;
 
 import org.pentaho.pac.client.common.util.StringUtils;
 
 public class SolutionRepositoryItemPickerValidator implements IUiValidator {
 
   private SolutionRepositoryItemPicker solRepPicker = null;
 
   public SolutionRepositoryItemPickerValidator(SolutionRepositoryItemPicker solRepPicker) {
     this.solRepPicker = solRepPicker;
   }
 
   public boolean isValid() {
     boolean isValid = true;
     
     String solution = solRepPicker.getSolution();
     if ( StringUtils.isEmpty( solution ) ) {
       isValid = false;
      solRepPicker.setActionError( "Solution name cannot be empty." );
     }
     
     String path = solRepPicker.getPath();
     if ( StringUtils.isEmpty( path ) ) {
       isValid = false;
      solRepPicker.setActionError( "Path cannot be empty." );
     }
     
     String action = solRepPicker.getAction();
     if ( StringUtils.isEmpty( action ) ) {
       isValid = false;
       solRepPicker.setActionError( "Action name cannot be empty." );
     }
     
     return isValid;
   }
 
   public void clear() {
     solRepPicker.setSolutionError( null );
     solRepPicker.setPathError( null );
     solRepPicker.setActionError( null );
   }
 }
