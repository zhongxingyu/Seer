 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fantasydrafter;
 
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author cbachich
  */
 public class CustomModel extends AbstractTableModel {
   
   /******************
    * Static Values  *
    * ****************/
   public static int ROWS            = 12;
   public static int COLUMNS         = 7;
   public static int TEAM_NAME_COL   = 0;
   public static int FIRST_PICK_COL  = 1;
   public static int LAST_PICK_COL   = 5;
   public static int DRAFT_ORDER_COL = 6;
   public static String PICKED = "!";
   
   /******************
    * Table Contents *
    * ****************/
   private String[] columnNames = new String[] {
     "Team", "Pick 1", "Pick 2", "Pick 3", "Pick4", "Pick 5", "Draft Order"
   };
 
   private Object[][] data = new Object[][] {
     {"8-Bit Warriors",  new Integer(1),  new Integer(2),  new Integer(3),  new Integer(4),  new Integer(5), null},
     {"Victorious Secret",  new Integer(2),  new Integer(5), new Integer(4),  new Integer(5),  new Integer(3), null},
     {"Someone",  new Integer(2),  new Integer(3),  new Integer(1),  new Integer(8),  new Integer(9), null},
     {"Kyle's Gay Warriors",  new Integer(2),  new Integer(5),  new Integer(6),  new Integer(7),  new Integer(8), null},
     {"Smack Talkers",  new Integer(8),  new Integer(1),  new Integer(5),  new Integer(1),  new Integer(8), null},
     {null, null, null, null, null, null, null},
     {null, null, null, null, null, null, null},
     {null, null, null, null, null, null, null},
     {null, null, null, null, null, null, null},
     {null, null, null, null, null, null, null},
     {null, null, null, null, null, null, null},
     {null, null, null, null, null, null, null}
   };
   
   // Keeps track of which rows have picks available
   private boolean[] active;
   private int pickActive[];
   private int draftOrder;
   private int teamsActive;
      
   /******************
    * Global Values  *
    * ****************/
   private boolean locked;
   private Console console;
   
   public CustomModel(Console console) {
     this.locked = false;
     this.console = console;
     draftOrder = 1;
     teamsActive = 0;
   }
   
   /******************
    * Procedures     *
    * ****************/
   @Override
   public int getColumnCount() {
       return columnNames.length;
   }
 
   @Override
   public int getRowCount() {
       return data.length;
   }
 
   @Override
   public String getColumnName(int col) {
       return columnNames[col];
   }
 
   @Override
   public Object getValueAt(int row, int col) {
       return data[row][col];
   }
 
 //  @Override
 //  public Class getColumnClass(int c) {
 //      return types[c];
 //  }
 
   /*
    * Don't need to implement this method unless your table's
    * editable.
    */
   @Override
   public boolean isCellEditable(int row, int col) {
       //Note that the data/cell address is constant,
       //no matter where the cell appears onscreen.
       if ((col == DRAFT_ORDER_COL) || locked) {
           return false;
       } else {
           return true;
       }
   }
   
   /*
    * Don't need to implement this method unless your table's
    * data can change.
    */
   @Override
   public void setValueAt(Object value, int row, int col) {
     String valueS = value.toString();
     
     // If this is the "!" that indicates a pick number has been picked, don't
     // convert it to Integer
     if( (col == TEAM_NAME_COL) || valueS.equals(PICKED) ) {
       data[row][col] = valueS;
     }
     // Otherwise this needs to be converted to an Integer
     else {
       data[row][col] = Integer.parseInt(valueS);
     }
     fireTableCellUpdated(row, col);
     
   }
   
   // Lock the cells from editing
   public void lockCells() {
     locked = true;
   }
   
   // Unlock the cells for editing
   public void unlockCells() {
     locked = false;
   }
   
   // Check the table contains good values
   public boolean isTableGood() {
     boolean passed = true;
     active = new boolean[ROWS];
     pickActive = new int[ROWS];
     
     // Step through each row for error checking
     for (int row = 0; row < ROWS; row++) {
       // Start by checking if the team name is empty. If it is, then don't check
       // the draft numbers
       String name = getTeamName(row);
       
       // If there is no team name then we'll move onto the next row
       if (name.isEmpty()) {
         continue;
       } 
       
       // Check that each of the pick number is between the set values
       int count = 1;
      for(int pickCol = FIRST_PICK_COL; pickCol <= LAST_PICK_COL; pickCol++) {
         if(!isPickGood(row,pickCol)) {
           console.write("Pick #" + count + " for Team " + name + 
                   " is not valid");
           passed = false;
         } else {
           // Indicate that this row is good by activating it
           active[row] = true;
           
           // Also set the first pick as the active one
           pickActive[row] = FIRST_PICK_COL;
           
           // Add the team to the teams active for tracking
           teamsActive++;
         }
         count++;
       }
     }
     
     return passed;
   }
   
   // Takes a pick and determines which teams have it
   public void checkPick(int pick) {
     // Loop through each active row to determine if it's pick should be checked
     for(int row = 0; row < ROWS; row++) {
       // Move onto the next row if this row is not active
       if(!active[row]) {
         continue;
       }
       
       // Check if the pick matches the pick value
       int pickCol = pickActive[row];
       if(pick == getPickValue(row,pickCol)) {
         // If this was the final pick, assign a draft spot
         if(pickCol == LAST_PICK_COL) {
           active[row] = false;
           setValueAt(PICKED, row, pickCol);
           setValueAt(draftOrder++, row, DRAFT_ORDER_COL);
           teamsActive--;
           continue;
         }
         
         // Set the pick to an exclamation to indicate it's been picked
         setValueAt(PICKED, row, pickCol);
         
         // Change the active pick column to the next
         pickActive[row]++;
       }
     }
   }
   
   // Returns whether their are any active teams remaining
   public boolean areTeamsActive() {
     if(teamsActive > 0) {
       return true;
     }
     
     return false;
   }
   
   // Return the team name in the passed in row
   private String getTeamName(int row) {
     try {
       return getValueAt(row, TEAM_NAME_COL).
               toString().trim();
     } catch(Exception ex) {
       return "";
     }
   }
   
   // Checks if the pick cell is good
   private boolean isPickGood(int row, int col) {
     try {
      int pick = getPickValue(row, col);
       if( (pick >= DraftoMachine.MIN) && (pick <= DraftoMachine.MAX)) {
         return true;
       }
       else {
         return false;
       }
     } catch(Exception ex) {
       return false;
     }
   }
    
   // Return the pick value in the row and col
   private int getPickValue(int row, int col) {
     return (Integer)getValueAt(row, col);
   }
   
 }
