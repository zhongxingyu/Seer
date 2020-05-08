 package QueryHandlers;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class PlotQueryHandler {
 
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rs = null;
     private String sql = "";
     private ArrayList<String> duchyList, qualityList;
 
     public PlotQueryHandler(Connection c) {
         super();
         con = c;
 
         duchyList = new ArrayList();
         qualityList = new ArrayList();
 
         try {
             sql = "SELECT DuchyName FROM Duchy";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 duchyList.add(rs.getString("DuchyName"));
             }
 
             sql = "SELECT QualityDescription FROM Quality";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 qualityList.add(rs.getString("QualityDescription"));
             }
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler Constructor");
             System.out.println(e.getMessage());
         }
     }
 
      /**
      * This function capitalizes the first letter of the word sent in.
      */
     public String capitalizeFirst(String name) {
         String correction = Character.toString(name.charAt(0));
         correction = correction.toUpperCase();
         name = name.replaceFirst(Character.toString(name.charAt(0)), correction);
         return name;
     }
 
     /**
      * This function returns the price of a certain plot, and the amount needed
      * for monthly upkeep - depending on the provided duchy, and quality of the
      * plot.
      */
     public ArrayList<String[]> queryPlotPrice(String duchy, String quality) {
         boolean correctDuchy = false, correctQuality = false;
         int duchyID, qualityID;
         int plotPrice, plotUpkeep;
         ArrayList<String[]> values;
         String[] line;
 
         duchy = duchy.toLowerCase();
         quality = quality.toLowerCase();
         values = new ArrayList();
         line = new String[3];
 
         //Check parameter validity
         for (int a = 0; a < duchyList.size(); a++) {
             if (duchyList.get(a).toLowerCase().equals(duchy)) {
                 correctDuchy = true;
             }
         }
 
         for (int a = 0; a < qualityList.size(); a++) {
             if (qualityList.get(a).toLowerCase().equals(quality)) {
                 correctQuality = true;
             }
         }
 
         if (correctDuchy != true || correctQuality != true) {
             System.out.println("Incorrect parameters supplied to function queryPlotPrice()");
         } else {
             //Capitalize first letter of duchy and quality
             duchy = capitalizeFirst(duchy);
             quality = capitalizeFirst(quality);
 
             try {
                 //Get the unique ID's of both the duchy, and quality
                 sql = "SELECT DuchyID FROM Duchy WHERE DuchyName = '" + duchy + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 rs.next();
                 duchyID = Integer.parseInt(rs.getString("DuchyID"));
 
                 sql = "SELECT QualityID FROM Quality WHERE QualityDescription = '" + quality + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 rs.next();
                 qualityID = Integer.parseInt(rs.getString("QualityID"));
 
                 //Determine price and upkeep costs
                 sql = "SELECT PriceLand, PriceMonthlyUpkeep FROM Price "
                         + "WHERE DuchyID = " + duchyID + " AND QualityID = "
                         + qualityID;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 plotPrice = Integer.parseInt(rs.getString("PriceLand"));
                 plotUpkeep = Integer.parseInt(rs.getString("PriceMonthlyUpkeep"));
 
                 System.out.println(plotPrice + " " + plotUpkeep);
                 //Find price values
                 sql = "SELECT AmountPlatinum, AmountGold, AmountSilver "
                         + "FROM Amount WHERE AmountID = "
                         + plotPrice;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 line[0] = rs.getString("AmountPlatinum");
                 line[1] = rs.getString("AmountGold");
                 line[2] = rs.getString("AmountSilver");
                 values.add(line);
 
                 line = new String[3];
                 sql = "SELECT AmountPlatinum, AmountGold, AmountSilver "
                         + "FROM Amount WHERE AmountID = "
                         + plotUpkeep;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 line[0] = rs.getString("AmountPlatinum");
                 line[1] = rs.getString("AmountGold");
                 line[2] = rs.getString("AmountSilver");
                 values.add(line);
 
                 return values;
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         }
 
         return null;
     }
 
     /**
      * This function will convert a 2D integer array, to make it suitable to
      * store in the database.
      */
     public String convertToArray(int[][] inArray) {
         String result = "";
 
         for (int a = 0; a < inArray.length; a++) {
             for (int b = 0; b < inArray[a].length; b++) {
                 if (b == inArray[a].length - 1) {
                     result += inArray[a][b];
                 } else {
                     result += inArray[a][b] + ",";
                 }
             }
             result += ";";
         }
 
         return result;
     }
 
     /**
      * This function will convert the 1D integer array, that was stored in the
      * database, back to a 2D integer array usable by the grid system.
      */
     public int[][] convertFromArray(String inArray) {
         int[][] result;
         StringTokenizer str, stc;
         int rows, columns;
 
         str = new StringTokenizer(inArray, ";");
         rows = str.countTokens();
         stc = new StringTokenizer(str.nextToken(), ",");
         columns = stc.countTokens();
         result = new int[rows][columns];
 
         for (int a = 0; a < rows; a++) {
             for (int b = 0; b < columns; b++) {
                 result[a][b] = Integer.parseInt(stc.nextToken());
             }
             if (a < rows - 1) {
                 stc = new StringTokenizer(str.nextToken(), ",");
             }
         }
 
         return result;
     }
 
     /**
      * This function will add a certain plot to a character, as each character
      * in the game system can own one or more plots.
      */ 
     public boolean addPlotToCharacter(String characterName, String duchyName, int sizeValue,
             String quality, int[][] groundArray, int[][] buildingArray, double acresUsed,
             double acreMax, int workersUsed, int workerMax, int happiness, double monthlyIncome) {
         int characterID, duchyID, qualityID;
         String ground, building;
 
         //Resolve all ID's
         //*Fault checking
         try {
             sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                     + "LOWER(UserCharacterName) = '" + characterName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             characterID = Integer.parseInt(rs.getString("UserCharacterID"));
 
             sql = "SELECT DuchyID FROM Duchy WHERE "
                     + "LOWER(DuchyName) = '" + duchyName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             duchyID = Integer.parseInt(rs.getString("DuchyID"));
 
             sql = "SELECT QualityID FROM Quality WHERE "
                     + "LOWER(QualityDescription) = '" + quality.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             qualityID = Integer.parseInt(rs.getString("QualityID"));
 
             ground = convertToArray(groundArray);
             building = convertToArray(buildingArray);
 
             //Add the plot to database
             sql = "INSERT INTO Plot (PlotOwnedBy, PlotDuchy, PlotSize, PlotQuality,"
                     + "PlotGroundArray, PlotBuildingArray, PlotAcresUsed, "
                     + "PlotAcreMax, PlotHappiness, PlotMonthlyIncome, PlotWorkersUsed, "
                     + "PlotWorkerMax) VALUES "
                     + "(" + characterID + ", " + duchyID + ", " + sizeValue + ", "
                     + qualityID + ", '" + ground + "', '" + building + "', "
                     + acresUsed + ", " + acreMax + ", " + happiness + ", "
                     + monthlyIncome + ", " + workersUsed + ", " + workerMax + ")";
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Could not execute function addPlotToCharacter()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /**
      * This function will retrieve a list of all the plots owned by the provided
      * character.
      */
     public ArrayList<String[]> retrievePlotsOwnedByCharacter(int characterID) {
         ArrayList<String[]> values;
         String[] line;
 
         values = new ArrayList();
 
         try {
             sql = "SELECT * FROM Plot WHERE "
                     + "PlotOwnedBy = " + characterID;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             while (rs.next()) {
                 line = new String[13];
                 line[0] = rs.getString("PlotID");
                 line[1] = rs.getString("PlotOwnedBy");
                 line[2] = rs.getString("PlotDuchy");
                 line[3] = rs.getString("PlotSize");
                 line[4] = rs.getString("PlotQuality");
                 line[5] = rs.getString("PlotGroundArray");
                 line[6] = rs.getString("PlotBuildingArray");
                 line[7] = rs.getString("PlotAcresUsed");
                 line[8] = rs.getString("PlotAcreMax");
                 line[9] = rs.getString("PlotHappiness");
                 line[10] = rs.getString("PlotMonthlyIncome");
                 line[11] = rs.getString("PlotWorkersUsed");
                 line[12] = rs.getString("PlotWorkerMax");
 
                 values.add(line);
             }
 
             //Exchange ID's with values
             for (int a = 0; a < values.size(); a++) {
                 sql = "SELECT UserCharacterName FROM UserCharacter "
                         + "WHERE UserCharacterID = " + values.get(a)[1];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[1] = rs.getString("UserCharacterName");
 
                 sql = "SELECT DuchyName FROM Duchy WHERE "
                         + "DuchyID = " + values.get(a)[2];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[2] = rs.getString("DuchyName");
 
                 sql = "SELECT QualityDescription FROM Quality WHERE "
                         + "QualityID = " + values.get(a)[4];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[4] = rs.getString("QualityDescription");
             }
 
             return values;
         } catch (Exception e) {
             System.out.println("Could not execute function retrievePlotsOwnedByCharacter()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /**
      * This function returns all details regarding the provided plotID.
      */
     public ArrayList<String> retrievePlotDetails(int plotID) {
         ArrayList<String> value;
 
         value = new ArrayList();
 
         try {
             sql = "SELECT * FROM Plot WHERE PlotID = " + plotID;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             value.add(rs.getString("PlotID"));
             value.add(rs.getString("PlotOwnedBy"));
             value.add(rs.getString("PlotDuchy"));
             value.add(rs.getString("PlotSize"));
             value.add(rs.getString("PlotQuality"));
             value.add(rs.getString("PlotGroundArray"));
             value.add(rs.getString("PlotBuildingArray"));
             value.add(rs.getString("PlotAcresUsed"));
             value.add(rs.getString("PlotAcreMax"));
             value.add(rs.getString("PlotHappiness"));
             value.add(rs.getString("PlotMonthlyIncome"));
             value.add(rs.getString("PlotWorkersUsed"));
             value.add(rs.getString("PlotWorkerMax"));
 
             sql = "SELECT UserCharacterName FROM UserCharacter "
                     + "WHERE UserCharacterID = " + value.get(1);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(1, rs.getString("UserCharacterName"));
 
             sql = "SELECT DuchyName FROM Duchy WHERE "
                     + "DuchyID = " + value.get(2);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(2, rs.getString("DuchyName"));
 
             sql = "SELECT QualityDescription FROM Quality WHERE "
                     + "QualityID = " + value.get(4);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(4, rs.getString("QualityDescription"));
 
             return value;
         } catch (Exception e) {
             System.out.println("Error, could not execute function retrievePlotDetails()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
      /**
      * This function changes plot details to the ones provided.
      */ 
     public boolean modifyPlot(int plotID, String characterName, String duchyName, int sizeValue,
         String quality, int[][] groundArray, int[][] buildingArray, double acresUsed,
         double acreMax, int workersUsed, int workerMax, int happiness, double monthlyIncome) {
         int characterID, duchyID, qualityID;
         String ground, building;
 
         //Exchange ID's with values  
         try {
             sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                     + "LOWER(UserCharacterName) = '" + characterName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             characterID = Integer.parseInt(rs.getString("UserCharacterID"));
 
             sql = "SELECT DuchyID FROM Duchy WHERE "
                     + "LOWER(DuchyName) = '" + duchyName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             duchyID = Integer.parseInt(rs.getString("DuchyID"));
 
             sql = "SELECT QualityID FROM Quality WHERE "
                     + "LOWER(QualityDescription) = '" + quality.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             qualityID = Integer.parseInt(rs.getString("QualityID"));
 
             ground = convertToArray(groundArray);
             building = convertToArray(buildingArray);
             
             System.out.println(happiness);
 
             sql = "UPDATE Plot SET "
                     + "PlotOwnedBy = " + characterID + ", "
                     + "PlotDuchy = " + duchyID + ", "
                     + "PlotSize = " + sizeValue + ", "
                     + "PlotQuality = " + qualityID + ", "
                     + "PlotGroundArray = '" + ground + "', "
                     + "PlotBuildingArray = '" + building + "', "
                     + "PlotAcresUsed = " + acresUsed + ", "
                     + "PlotAcreMax = " + acreMax + ", "
                     + "PlotHappiness = " + happiness + ", "
                     + "PlotMonthlyIncome = " + monthlyIncome + ", "
                     + "PlotWorkersUsed = " + workersUsed + ", "
                     + "PlotWorkerMax = " + workerMax + " "
                     + "WHERE PlotID = " + plotID;
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Could not execute function modifyPlot()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /**
      * This function acts as a search function, with multiple criteria, that
      * returns a certain plot's details depending on the criteria provided.
      * Empty fields will yield all results.
      */
     public ArrayList<String[]> searchPlotBy(String characterName, String duchy, int size, String quality) {
         ArrayList<String[]> values = null;
         int characterID = 0, duchyID = 0, qualityID = 0;
         String[] line;
 
         values = new ArrayList();
 
         try {
             //Get ID's
             if (!characterName.equals("")) {
                 sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                         + "LOWER(UserCharacterName) = '" + characterName.toLowerCase() + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 characterID = Integer.parseInt(rs.getString("UserCharacterID"));
             }
 
             if (!duchy.equals("")) {
                 sql = "SELECT DuchyID FROM Duchy WHERE "
                         + "LOWER(DuchyName) = '" + duchy.toLowerCase() + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 duchyID = Integer.parseInt(rs.getString("DuchyID"));
             }
 
             if (!quality.equals("")) {
                 sql = "SELECT QualityID FROM Quality WHERE "
                         + "LOWER(QualityDescription) = '" + quality.toLowerCase() + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 qualityID = Integer.parseInt(rs.getString("QualityID"));
             }
 
             sql = "SELECT * FROM Plot";
             if (characterID != 0) {
                 sql += " WHERE PlotOwnedBy LIKE " + characterID;
             }
 
             if (duchyID != 0 && characterID != 0) {
                 sql += " AND PlotDuchy LIKE " + duchyID;
             } else if (duchyID != 0) {
                 sql += " WHERE PlotDuchy LIKE " + duchyID;
             }
 
             if (size > 0 && (characterID != 0 || duchyID != 0)) {
                 sql += " AND PlotSize LIKE " + size;
             } else if (size > 0) {
                 sql += " WHERE PlotSize LIKE " + size;
             }
 
             if (qualityID != 0 && (characterID != 0 || duchyID != 0 || size > 0)) {
                 sql += " AND PlotQuality LIKE " + qualityID;
             } else if (qualityID != 0) {
                 sql += " WHERE PlotQuality LIKE " + qualityID;
             }
 
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             while (rs.next()) {
                 line = new String[13];
                 line[0] = rs.getString("PlotID");
                 line[1] = rs.getString("PlotOwnedBy");
                 line[2] = rs.getString("PlotDuchy");
                 line[3] = rs.getString("PlotSize");
                 line[4] = rs.getString("PlotQuality");
                 line[5] = rs.getString("PlotGroundArray");
                 line[6] = rs.getString("PlotBuildingArray");
                 line[7] = rs.getString("PlotAcresUsed");
                 line[8] = rs.getString("PlotAcreMax");
                 line[9] = rs.getString("PlotHappiness");
                 line[10] = rs.getString("PlotMonthlyIncome");
                 line[11] = rs.getString("PlotWorkersUsed");
                 line[12] = rs.getString("PlotWorkerMax");
                 values.add(line);
             }
 
             return values;
         } catch (Exception e) {
             System.out.println("Could not execute function searchPlotBy()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /**
      * This function deletes a plot. Returns true if deleted successfully,
      * and false if not.
      */ 
     public boolean deletePlot(int plotID) {
         try {
             sql = "DELETE FROM Plot WHERE PlotID = " + plotID;
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Could not execute function deletePlot()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
     
     public ArrayList<String[]> retrieveAllPlots()
     {
         String sql = "";
         ArrayList<String[]> result = null;
         String[] line = null;
         
         try
         {
             sql = "SELECT * FROM Plot";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             
             ResultSet lrs;
             result = new ArrayList();
             while(rs.next())
             {
                
                 line = new String[13];
                 line[0] = rs.getString("PlotID");
                 line[1] = rs.getString("PlotOwnedBy");
                 line[2] = rs.getString("PlotDuchy");
                 line[3] = rs.getString("PlotSize");
                 line[4] = rs.getString("PlotQuality");
                 line[5] = rs.getString("PlotGroundArray");
                 line[6] = rs.getString("PlotBuildingArray");
                 line[7] = rs.getString("PlotAcresUsed");
                 line[8] = rs.getString("PlotAcreMax");
                 line[9] = rs.getString("PlotHappiness");
                 line[10] = rs.getString("PlotMonthlyIncome");
                 line[11] = rs.getString("PlotWorkersUsed");
                 line[12] = rs.getString("PlotWorkerMax");
                 
                 sql = "SELECT UserCharacterName FROM UserCharacter "
                         + "WHERE UserCharacterID = " + line[1];
                 stmt = con.createStatement();
                 lrs = stmt.executeQuery(sql);
                 lrs.next();
                 line[1] = lrs.getString("UserCharacterName");
 
                 sql = "SELECT DuchyName FROM Duchy WHERE "
                         + "DuchyID = " + line[2];
                 stmt = con.createStatement();
                 lrs = stmt.executeQuery(sql);
                 lrs.next();
                 line[2] = lrs.getString("DuchyName");
 
                 sql = "SELECT QualityDescription FROM Quality WHERE "
                         + "QualityID = " + line[4];
                 stmt = con.createStatement();
                 lrs = stmt.executeQuery(sql);
                 lrs.next();
                 line[4] = lrs.getString("QualityDescription");
                 
                 result.add(line);
                 
             }
             
            
             return result;
         }
         catch(Exception e) {
             System.out.println("Could not execute function retrieveAllPlots()");
             System.out.println(e.getMessage());
         }
         return null;
     }
 }
