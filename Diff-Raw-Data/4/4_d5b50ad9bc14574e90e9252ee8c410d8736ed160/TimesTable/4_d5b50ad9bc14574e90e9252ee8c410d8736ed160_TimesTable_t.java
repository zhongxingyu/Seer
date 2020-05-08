 /**
  *  A class that holds times tables 1 through 12
  *
  *  Author: Jason Angus
  *  File:   TimesTable.java
  *  Last Edit: 10/10/2010
  */
 
 import java.util.*;
 
 class TimesTable {
 
    public TimesTable () {
       setupTimesTablesArray();
    } // close no-args contructor
 
    public TimesTable (int table) {
       this.table = table;
       setupTimesTablesArray();
    } // close constructor
 
    // setup a two dimensional array containing 1 to 12 times tables
    private void setupTimesTablesArray() {
       for (int i=0; i < ARRAY_SIZE; ++i)
          for (int j=0; j < ARRAY_SIZE; ++j)
             timesTables[i][j] = i * j;
    } // close setupTimesTablesArray method
 
    // set a value for a times table to work with
    public int setTable(int table) {
       if (table >= TABLE_MIN && table <= TABLE_MAX) {
          this.table = table;
          return this.table;
       }
       else {
          System.out.println(table + " is not between " +
                         TABLE_MIN + " and " + TABLE_MAX);
          return -1;
       }
    } // close setTable method
 
    public int getTable() {
       return this.table;
    } // close getTable method
 
    // display the whole of currently set table
    public void displayTimesTable() {
       if (table >= TABLE_MIN && table <= TABLE_MAX) {
          System.out.println(table + " times table:");
          for (int i=1; i <= TABLE_MAX; ++i) {
             System.out.printf("%2d  X %2d  = %3d\n",
                            i, table, timesTables[table][i]);
          } // close for
       }
       else
          System.out.println(table + " is not between " +
                         TABLE_MIN + " and " + TABLE_MAX);
    } // close displayTimesTable method
 
    // display the whole of a user selected table
    public void displayTimesTable(int table) {
       if (table >= TABLE_MIN && table <= TABLE_MAX) {
          System.out.println(table + " times table:");
          for (int i=1; i<=TABLE_MAX; ++i) {
             System.out.printf("%2d  X %2d  = %3d\n",
                            i, table, timesTables[table][i]);
          } // close for
       } else
          System.out.println(table + " is not between 1 and 12");
    } // close displayTimesTable method
 
    // display a single sum using currently selected table
    public void displaySum(int index) {
       if (index >= TABLE_MIN && index <= TABLE_MAX)
          System.out.printf("%2d  X %2d  = ",
                         index, table, timesTables[table][index]);
       else
          System.out.println(table + " is not between " +
                         TABLE_MIN + " and " + TABLE_MAX);
    } // close displaySum method
 
    // display a single sum
    public void displaySum(int table, int index) {
       if (table >= TABLE_MIN && table <= TABLE_MAX) {
          if (index >= TABLE_MIN && index <= TABLE_MAX) {
             System.out.printf("%2d  X %2d  = ",
                         index, table, timesTables[table][index]);
          }
          else
          System.out.println(index + " is not between " +
                               TABLE_MIN + " and " + TABLE_MAX);
       }
       else
          System.out.println(table + " is not between " +
                               TABLE_MIN + " and " + TABLE_MAX);
    } // close displaySum method
 
    // return result of table sum using currently selected table
    public int getSum(int index) {
       if (index >= TABLE_MIN && index <= TABLE_MAX)
          return timesTables[table][index];
       else {
          System.out.println(table + " is not between " +
                               TABLE_MIN + " and " + TABLE_MAX);
          return -1;
       }
 
    } // close getSum method
 
    // return result of table sum using user selected table
    public int getSum(int table, int index) {
       if (table >= TABLE_MIN && table <= TABLE_MAX) {
         if (index >= TABLE_MIN && index <= TABLE_MAX) {
            return timesTables[table][index];
       }
       else {
          System.out.println(index + " is not between " +
                               TABLE_MIN + " and " + TABLE_MAX);
          return -1;
          }
       }
       else {
          System.out.println(table + " is not between " +
                               TABLE_MIN + " and " + TABLE_MAX);
          return -1;
       }
    } // close getSum method
 
    public String toString() {
       return table + " times table" + Arrays.toString(timesTables[table]);
    } // close toString method
 
    private int table = 1; // default to 1 times table
    public final static int TABLE_MAX = 12;
    public final static int TABLE_MIN =  1;
    private final static int ARRAY_SIZE = TABLE_MAX + 1;
    private int[][] timesTables = new int[ARRAY_SIZE][ARRAY_SIZE];
 
 } // close TimesTable class
