 /*
  * Name: Jelte Fennema
  * Studentnumber: 10183159
  * Study: Informatica
  *
  * Functionality:
  * The program needs be able to determine a winner using a file with the votes.
  * The file must have this exact input format:
  * (n candidates )
  * candidate_1 candidate_2 ... candidate_n
  * (k voters )
  * choice_1 choice_2 ... choice_n
  * choice_1 choice_2 ... choice_n
  * choice_1 choice_2 ... choice_n
  * ... and so on ( in total k times )
  *
  * The program must use this algorithem for determining the winner:
  * Determine which candidate has the lowest amount of first choices. If there
  * are multiple check the second choices and so on.
  * Delete those votes and move the lower votes one up.
  */
 
 import java.io.FileReader;
 import java.io.FileNotFoundException;
 import java.util.*;
 
 public class Assignment5 {
     static Scanner input = new Scanner (System.in);
     static StringTable votes;
     static StringTable candidates;
     static IntTable intCandidates;
     static int longestCandidate;
 
     /*
      * Main method: Receive the input and call the methods that use it.
      */
     public static void main(String[] args) {
         if (args.length != 1) {
             System.err.println("Usage: java Assignment5 'filename'");
             return;
         }
         if(!readVoteFile(args[0])){
             System.out.println("Something went wrong when reading the file. Are"
                     + " you sure you used the correct input format?");
         }
         countVotes();
         while(true){
             if(!chooseLoser()){
                 break;
             }
             countVotes();
             if(intCandidates.getY()== 1){
                 System.out.println("And the winner is: " +candidates.Arr[1][1]);
                 break;
             }
 
         }
     }
 
     /*
      * ReadVoteFile method: Read the vote file and put its contents in tables.
      */
     public static boolean readVoteFile(String filename){
         Scanner file;
         int voter, candidate;
         try{
             file = new Scanner(new FileReader(filename));
         }
         catch (FileNotFoundException fnf) {
             System.err.println("Error: " + fnf.getMessage());
             return false;
         }
 
         /*
          * Create the candidates table from the information from the file.
          */
         if(file.hasNextInt())
             candidate=file.nextInt();
         else
             return false;
         candidates = new StringTable(candidate+2, candidate+2);
         candidates.initialize();
         candidates.Arr[0][0] = "Candidates Table";
         candidates.Arr[0][1] = "                  ";
         candidates.Arr[0][2] = "1st choice votes: ";
         for(int i=2; i<=candidate; i++){
             candidates.Arr[0][i+1]= Integer.toString(i) + "nd choice votes: ";
         }
         for (int i=1; i<=candidate; i++){
             if(file.hasNext())
                 candidates.Arr[i][1]=file.next();
             else
                 return false;
         }
         candidates.Arr[candidate+1][1] = "   Total amount of votes";
 
 
         /*
          * Check what candidate string is the longest. And make all the strings
          * an even length. This is done so that it Still looks like a table when
          * the candidate strings don't have an even length.
          */
         for(int i=1; i<=candidate; i++){
             if (longestCandidate < candidates.Arr[i][1].length()){
                 longestCandidate = candidates.Arr[i][1].length();
             }
         }
         if(longestCandidate < Integer.toString(candidate).length())
             longestCandidate = Integer.toString(candidate).length();
         longestCandidate++;
 
         
         for(int i=1; i<=candidate; i++){
             while(longestCandidate > candidates.Arr[i][1].length()){
                 candidates.Arr[i][1] += " ";
             }
         }
 
         if(file.hasNextInt())
             voter=file.nextInt();
         else
             return false;
         intCandidates = new IntTable(candidate, candidate);
         votes = new StringTable(voter + 1, candidate + 1);
         votes.initialize();
         votes.Arr[0][0] = "Voting Table";
         votes.Arr[0][1] = "1st choices:      ";
         for(int i=2; i<=candidate; i++){
             votes.Arr[0][i]= Integer.toString(i) + "nd choices:      ";
         }
         for (int j=1; j<=voter; j++){
             for (int i=1; i<=candidate; i++){
                 if(file.hasNext()){
                     votes.Arr[j][i]=file.next();
                     while(longestCandidate > votes.Arr[j][i].length()){
                         votes.Arr[j][i] += " ";
                     }
                 }
                 else
                     return false;
             }
         }
         votes.print();
         return true;
     }
 
     /*
      * CountVotes method: Count the votes for every candidate and put them in
      * candidates table.
      */
     public static void countVotes(){
         intCandidates.initialize();
         for(int i=1; i<candidates.getX()-1; i++){
             for(int j=1; j<votes.getY(); j++){
                 for(int k=1; k<votes.getX(); k++){
                     if(candidates.Arr[i][1].equals(votes.Arr[k][j])){
                         intCandidates.Arr[i-1][j-1] += 1;
                     }
                 }
                 candidates.Arr[i][j+1] =
                         Integer.toString(intCandidates.Arr[i-1][j-1]);
                 while(longestCandidate > candidates.Arr[i][j+1].length()){
                     candidates.Arr[i][j+1] += " ";
                 }
             }
         }
         for(int i=0; i<intCandidates.getY(); i++){
             candidates.Arr[candidates.getX()-1][i+2] = "   " +
                     Integer.toString(intCandidates.rowSum(i));
         }
         candidates.print();
     }
 
     /*
      * ChooseLoser method: Find the candidate with the fewest highest votes.
      * Delete that candidate from both tables.
      */
     public static boolean chooseLoser(){
         IntArray losers = new IntArray(intCandidates.getY());
         for(int i=0; i<intCandidates.getX(); i++){
             losers.Arr[i]=i;
         }
         int leastVotes = votes.getX();
         int amntLosers = intCandidates.getX() + 1;
         int whichLoser = 0;
 
         for(int i=0; i < intCandidates.getY(); i++){
             leastVotes = votes.getX() ;
             for(int j=0; j<losers.getLength()-amntLosers; j++){
                losers.deleteLast();
             }
             amntLosers = 0;
             whichLoser = 0;
             for(int j=0; j<intCandidates.getX(); j++){
                 if(losers.Arr[whichLoser]==j){
                    if(intCandidates.Arr[j][0]<leastVotes){
                         leastVotes = intCandidates.Arr[j][0];
                         losers.Arr[0] = j;
                         amntLosers = 1;
                     }
                    else if(intCandidates.Arr[j][0]==leastVotes){
                         losers.Arr[amntLosers] = j;
                         amntLosers++;
                     }
                     whichLoser++;
 
                 }
             }
             if(amntLosers == 1){
                 break;
             }
         }
         /*
          * If there is less more candidates left than the candidates with the
          * lowest amount of votes delete those candidates.
          */
         if(amntLosers < intCandidates.getY()){
             for(int i=amntLosers-1; i>=0; i--){
                 String loser = candidates.Arr[losers.Arr[i] + 1][1];
                 candidates.deleteLastRow();
                 intCandidates.deleteLastRow();
                 candidates.deleteColumn(losers.Arr[i] + 1);
                 intCandidates.deleteColumn(losers.Arr[i]);
                 moveVotes(loser);
                 votes.print();
             }
             return true;
         }
         System.out.println("There are multiple winners.");
         return false;
     }
 
     /*
      * MoveVotes method: Remove the loser and shift the lower votes up.
      */
     public static void moveVotes(String loser){
         for(int i=1; i<votes.getY()-1; i++){
             for(int j=1; j<votes.getX(); j++){
                     if(votes.Arr[j][i].equals(loser)){
 
                     votes.shiftUp(j, i+1);
                 }
             }
         }
         votes.deleteLastRow();
     }
 
 
 }
 
 
 /*
  * StringArray class: A class that contains a string array.
  */
 class StringArray {
     String[] Arr;
     int length;
     boolean created;
     StringArray() {  }
     StringArray(int length) {
         created = true;
         Arr = new String[length];
         this.length = length;
     }
 
     /*
      * StringArray.initialize method: Fill the array with empty strings.
      */
     void initialize(){
         for(int i=0; i<length; i++){
             Arr[i]="";
         }
     }
 
     /*
      * StringArray.print method: Print the array.
      */
     void print(){
         for(int i=0; i<length; i++){
             System.out.print(Arr[i] + " ");
         }
         System.out.println("\n");
     }
 
     /*
      * StringArray.deletLast method: Delete the last value in the array.
      */
     void deleteLast(){
         Arr[length-1] = null;
         length--;
     }
 
     int getLength(){
         return length;
     }
 }
 
 /*
  * IntArray class: The same as the StringArray class, just with arrays of
  * integers instead of strings.
  */
 class IntArray {
     int[] Arr;
     private int length;
     boolean created;
     IntArray() {  }
     IntArray(int length) {
         created = true;
         Arr = new int[length];
         this.length = length;
     }
 
     void initialize(){
         for(int i=0; i<length; i++){
             Arr[i]=-1;
         }
     }
 
     void print(){
         for(int i=0; i<length; i++){
             System.out.print(Arr[i] + " ");
         }
         System.out.println("\n");
     }
     
     void deleteLast(){
         Arr[length-1] =  -1;
         length--;
     }
 
     int getLength(){
         return length;
     }
     
 }
 
 /*
  * StringTable class: A class containing a 2D string array.
  */
 class StringTable {
     String[][] Arr;
     private int x, y;
     boolean created;
     StringTable() { }
     StringTable(int x, int y){
         created = true;
         Arr = new String[x][y];
         this.x=x;
         this.y=y;
     }
 
     /*
      * StringTable.initialize method: Fill the table with empty strings.
      */
     void initialize(){
         for (int i=0; i<y; i++){
             for(int j=0; j<x; j++){
                 Arr[j][i] = "";
             }
         }
     }
 
     /*
      * StringTable.print method: Print the table.
      */
     void print(){
         System.out.println("");
         for (int i=0; i<y; i++){
             for(int j=0; j<x; j++){
                 System.out.print(Arr[j][i] + " ");
             }
             System.out.println("");
         }
         System.out.println("");
     }
 
     /*
      * StringTable.deleteLastRow method: Delete the last row.
      */
     void deleteLastRow(){
         for(int i=0; i<x; i++){
             Arr[i][y-1]=null;
         }
         y--;
     }
 
     /*
      * StringTable.deleteLastColumn method: Delete the last column.
      */
     void deleteLastColumn(){
         for(int i=0; i<y; i++){
             Arr[x-1][i]=null;
         }
         x--;
     }
 
     /*
      * StringTable.shiftUp method: Move the given value and everything below it
      * one up.
      */
     void shiftUp(int xCo, int yCo){
         if(yCo > 0){
             for(int i=yCo; i<y; i++){
                 Arr[xCo][i-1] = Arr[xCo][i];
             }
         }
     }
 
     /*
      * StringTable.shiftLeft method: Move the given value and everything right
      * of it one to the left.
      */
     void shiftLeft(int xCo, int yCo){
         if(xCo > 0){
             for(int i=xCo; i<x; i++){
                 Arr[i-1][yCo] = Arr[i][yCo];
             }
         }
     }
 
     /*
      * StringTable.deleteColumn method: Delete a given column.
      */
     void deleteColumn(int column){
         for(int i=0; i<y; i++)
             shiftLeft(column + 1, i);
         deleteLastColumn();
     }
 
     /*
      * StringTable.deleteRow method: Delete a given row.
      */
     void deleteRow(int row){
         for(int i=0; i<x; i++)
             shiftUp(row + 1, i);
         deleteLastRow();
     }
 
     int getX(){
         return x;
     }
 
     int getY(){
         return y;
     }
 
 }
 
 /*
  * IntTable class: Same as StringTable class, just with integers instead of
  * strings. It also has a couple of math metmods.
  */
 class IntTable {
     int[][] Arr;
     private int x, y;
     boolean created;
     IntTable() { }
     IntTable(int x, int y){
         created = true;
         Arr = new int[x][y];
         this.x=x;
         this.y=y;
     }
 
     void initialize(){
         for (int i=0; i<y; i++){
             for(int j=0; j<x; j++){
                 Arr[j][i] = 0;
             }
         }
     }
 
     void print(){
         System.out.println("");
         for (int i=0; i<y; i++){
             for(int j=0; j<x; j++){
                 System.out.print(Arr[j][i] + " ");
             }
             System.out.println("");
         }
     }
 
     /*
      * IntTable.rowSum method: Add all the values in one row together.
      */
     int rowSum(int row){
         int sum=0;
         for(int i=0; i<x; i++){
             sum += Arr[i][row];
         }
         return sum;
     }
 
     /*
      * IntTable.columnSum method: Add all the values in one column together.
      */
     int columnSum(int column){
         int sum=0;
         for(int i=0; i<y; i++){
             sum += Arr[column][i];
         }
         return sum;
     }
 
     /*
      * IntTable.sum method: Add all the values in the table together.
      */
     int sum(){
         int total=0;
         for (int i=0; i<y; i++){
             total += rowSum(i);
         }
         return total;
     }
 
     void deleteLastRow(){
         for(int i=0; i<x; i++){
             Arr[i][y-1]=-1;
         }
         y--;
     }
 
     void deleteLastColumn(){
         for(int i=0; i<y; i++){
             Arr[x-1][i]=-1;
         }
         x--;
     }
 
     void shiftUp(int xCo, int yCo){
         if(yCo > 0){
             for(int i=yCo; i<=y; i++){
                 Arr[xCo][i-1] = Arr[xCo][i];
             }
         }
     }
 
     void shiftLeft(int xCo, int yCo){
         if(xCo > 0){
             for(int i=xCo; i<x; i++){
                 Arr[i-1][yCo] = Arr[i][yCo];
             }
         }
     }
 
     void deleteColumn(int column){
         for(int i=0; i<y; i++)
             shiftLeft(column + 1, i);
         deleteLastColumn();
     }
 
     void deleteRow(int row){
         for(int i=0; i<x; i++)
             shiftUp(row + 1, i);
         deleteLastRow();
     }
 
     int getX(){
         return x;
     }
 
     int getY(){
         return y;
     }
 
 }
