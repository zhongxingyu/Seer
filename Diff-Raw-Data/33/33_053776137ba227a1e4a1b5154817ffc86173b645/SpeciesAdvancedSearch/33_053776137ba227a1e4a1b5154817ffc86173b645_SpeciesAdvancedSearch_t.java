 package ro.finsiel.eunis.search.species.advanced;
 
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.util.Vector;
 
 /**
  * Class used for species advanced search.
  * @author finsiel
  */
 public class SpeciesAdvancedSearch {
   private String SQL_DRV = "";
   private String SQL_URL = "";
   private String SQL_USR = "";
   private String SQL_PWD = "";
   private int SQL_LIMIT = 1000;
 
   private Vector Tables = new Vector();
   private Vector Operands = new Vector();
   private Vector MinValues = new Vector();
   private Vector MaxValues = new Vector();;
 
   private int resultCount = 0;
 
   /**
    * Creates new SpeciesAdvancedSearch.
    */
   public void SpeciesAdvancedSearch() {
   }
 
   /**
    * Initialization method.
    * @param SQL_DRIVER_NAME JDBC driver.
    * @param SQL_DRIVER_URL JDBC url.
    * @param SQL_DRIVER_USERNAME JDBC username.
    * @param SQL_DRIVER_PASSWORD JDBC password.
    */
   public void Init(String SQL_DRIVER_NAME, String SQL_DRIVER_URL,
                    String SQL_DRIVER_USERNAME, String SQL_DRIVER_PASSWORD) {
     SQL_DRV = SQL_DRIVER_NAME;
     SQL_URL = SQL_DRIVER_URL;
     SQL_USR = SQL_DRIVER_USERNAME;
     SQL_PWD = SQL_DRIVER_PASSWORD;
   }
 
   /**
    * Limit the results output to the specified value.
    * @param SQLLimit Limit of returned rows.
    */
   public void SetSQLLimit(int SQLLimit) {
     SQL_LIMIT = SQLLimit;
   }
 
   private int FindCriteria(String Table, String Operand, String ValueMin, String ValueMax) {
     int pos = -1;
     int i;
     String sTable;
     String sOperand;
     String sValueMin;
     String sValueMax;
 
     if (!this.Tables.isEmpty()) {
       for (i = 0; i < this.Tables.size(); i++) {
         sTable = (String) this.Tables.elementAt(i);
         if (sTable.equalsIgnoreCase(Table)) {
           sOperand = (String) this.Operands.elementAt(i);
           sValueMin = (String) this.MinValues.elementAt(i);
           sValueMax = (String) this.MaxValues.elementAt(i);
           if (sOperand.equalsIgnoreCase(Operand) &&
                   sValueMin.equalsIgnoreCase(ValueMin) &&
                   sValueMax.equalsIgnoreCase(ValueMax)) {
             pos = i;
           }
         }
       }
     }
     return pos;
   }
 
   /**
    * Add new search criteria.
    * @param Table Table name.
    * @param Operand relation operator.
    * @param ValueMin Minimum value
    * @param ValueMax Maximum value.
    */
   public void AddCriteria(String Table, String Operand, String ValueMin, String ValueMax) {
     int pos = -1;
     if (null == Table || null == Operand || null == ValueMin) return;
 
     if (Table.length() > 0 && Operand.length() > 0) {
       if (Operand.equalsIgnoreCase("Equal") && ValueMin.length() == 0) {
         return;
       }
       if (Operand.equalsIgnoreCase("Contains") && ValueMin.length() == 0) {
         return;
       }
       if (Operand.equalsIgnoreCase("Regex") && ValueMin.length() == 0) {
         return;
       }
       if (Operand.equalsIgnoreCase("Between") && ValueMin.length() == 0) {
         return;
       }
       if (Operand.equalsIgnoreCase("Between") && ValueMax.length() == 0) {
         return;
       }
 
       if (!this.Tables.isEmpty()) {
         //System.out.println("Tables contain something");
         pos = this.FindCriteria(Table, Operand, ValueMin, ValueMax);
       }
       if (pos == -1) {
         //System.out.println("add criteria");
         this.Tables.addElement(Table);
         this.Operands.addElement(Operand);
         this.MinValues.addElement(ValueMin);
         this.MaxValues.addElement(ValueMax);
       }
     }
   }
 
   /**
    * Remove an search criteria.
    * @param Table Table name.
    * @param Operand Relation operator.
    * @param ValueMin Minimum value.
    * @param ValueMax Maximum value.
    */
   public void RemoveCriteria(String Table, String Operand, String ValueMin, String ValueMax) {
     int pos;
 
     if (!this.Tables.isEmpty()) {
       pos = this.FindCriteria(Table, Operand, ValueMin, ValueMax);
       if (pos != -1) {
         this.Tables.remove(pos);
         this.Operands.remove(pos);
         this.MinValues.remove(pos);
         this.MaxValues.remove(pos);
       }
     }
   }
 
   /**
    * Remove all search criteria.
    */
   public void ClearCriteria() {
     this.Tables.clear();
     this.Operands.clear();
     this.MinValues.clear();
     this.MaxValues.clear();
   }
 
   private String GetID(String value, String table, String idcolumn, String valuecolumn) {
     String SQL = "";
     Connection con = null;
     PreparedStatement ps = null;
     ResultSet rs = null;
     String result = "-1";
 
     try {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
       if (valuecolumn == null || valuecolumn.length() == 0) {
         valuecolumn = "NAME";
       }
       if (idcolumn == null || idcolumn.length() == 0) {
         idcolumn = "ID_" + table.toUpperCase();
       }
       SQL = "SELECT " + idcolumn + " FROM CHM62EDT_" + table.toUpperCase();
       SQL += " WHERE " + valuecolumn + "='" + value + "'";
 
       if (SQL.length() > 0) {
         ps = con.prepareStatement(SQL);
         rs = ps.executeQuery();
 
         if (rs.next()) {
           result = rs.getString(1);
         }
         rs.close();
         ps.close();
       }
       con.close();
     } catch (Exception e) {
       e.printStackTrace();
       return result;
     }
 
 
     return result;
   }
 
   /**
    * Build the condition for search.
    * @return SQL.
    * @throws ClassNotFoundException From JDBC.
    * @throws SQLException Feom JDBC.
    */
   public String BuildFilter() throws ClassNotFoundException, SQLException {
     String filter = "";
     String attributesFilter = "";
     String speciesFilter = "";
     String speciesSQL = "";
     String sTable;
     String sOperand;
     String sValueMin;
     String sValueMax;
     int i;
 
     if (!this.Tables.isEmpty()) {
       for (i = 0; i < this.Tables.size(); i++) {
         sTable = (String) this.Tables.elementAt(i);
         sOperand = (String) this.Operands.elementAt(i);
         sValueMin = (String) this.MinValues.elementAt(i);
         sValueMax = (String) this.MaxValues.elementAt(i);
 
         if (sTable.equalsIgnoreCase("ScientificName")) {
           speciesSQL = "SELECT DISTINCT ID_NATURE_OBJECT FROM CHM62EDT_SPECIES";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " WHERE SCIENTIFIC_NAME LIKE '%" + sValueMin + "%'";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " WHERE SCIENTIFIC_NAME = '" + sValueMin + "'";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " WHERE SCIENTIFIC_NAME REGEXP '" + sValueMin + "'";
           }
         }
 
         if (sTable.equalsIgnoreCase("VernacularName")) {
           speciesSQL = "SELECT DISTINCT CHM62EDT_REPORTS.ID_NATURE_OBJECT FROM CHM62EDT_REPORTS ";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_ATTRIBUTES` ON (`CHM62EDT_REPORTS`.`ID_REPORT_ATTRIBUTES` = `CHM62EDT_REPORT_ATTRIBUTES`.`ID_REPORT_ATTRIBUTES`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_ATTRIBUTES`.`NAME` = 'VERNACULAR_NAME')";
           speciesSQL += " AND";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " (CHM62EDT_REPORT_ATTRIBUTES.VALUE LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " (CHM62EDT_REPORT_ATTRIBUTES.VALUE = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regexp")) {
             speciesSQL += " (CHM62EDT_REPORT_ATTRIBUTES.VALUE REGEXP '" + sValueMin + "')";
           }
         }
 
         if (sTable.equalsIgnoreCase("Group")) {
           speciesSQL = "SELECT DISTINCT ID_NATURE_OBJECT";
           speciesSQL += " FROM `CHM62EDT_SPECIES`";
           speciesSQL += " INNER JOIN `CHM62EDT_GROUP_SPECIES` ON (`CHM62EDT_SPECIES`.`ID_GROUP_SPECIES` = `CHM62EDT_GROUP_SPECIES`.`ID_GROUP_SPECIES`)";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " WHERE (`CHM62EDT_GROUP_SPECIES`.`COMMON_NAME` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " WHERE (`CHM62EDT_GROUP_SPECIES`.`COMMON_NAME` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " WHERE (`CHM62EDT_GROUP_SPECIES`.`COMMON_NAME` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " WHERE ID_GROUP_SPECIES >= " + GetID(sValueMin, "GROUP_SPECIES", "", "COMMON_NAME");
             speciesSQL += " AND ID_GROUP_SPECIES >= " + GetID(sValueMax, "GROUP_SPECIES", "", "COMMON_NAME");
           }
         }
 
         if (sTable.equalsIgnoreCase("Author")) {
           String SQLWhere = "";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             SQLWhere = " (`DC_SOURCE`.`SOURCE` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             SQLWhere = " (`DC_SOURCE`.`SOURCE` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             SQLWhere = " (`DC_SOURCE`.`SOURCE` LIKE '%" + sValueMin + "%')";
           }
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `CHM62EDT_REPORTS` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_REPORTS`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE";
           speciesSQL += "      (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` IN ('DISTRIBUTION_STATUS','LANGUAGE','CONS_STATUS','SPECIES_GEO','LEGAL_STATUS','SPECIES_STATUS','POPULATION_UNIT','TREND'))";
           speciesSQL += "    AND " + SQLWhere;
           speciesSQL += "    UNION";
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_NATURE_OBJECT`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE " + SQLWhere;
           speciesSQL += "    UNION";
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_NATURE_OBJECT`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE " + SQLWhere;
           speciesSQL += "    UNION";
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `CHM62EDT_TAXONOMY` ON (`CHM62EDT_SPECIES`.`ID_TAXONOMY` = `CHM62EDT_TAXONOMY`.`ID_TAXONOMY`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_TAXONOMY`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE " + SQLWhere;
         }
 
         if (sTable.equalsIgnoreCase("LegalInstrument")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` FROM `CHM62EDT_SPECIES`";
           speciesSQL += " INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORTS` ON (`CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT` = `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `DC_INDEX` ON (`CHM62EDT_REPORTS`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += " INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += " INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += " WHERE `CHM62EDT_REPORT_TYPE`.LOOKUP_TYPE='LEGAL_STATUS'";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`DC_TITLE`.`TITLE` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`DC_TITLE`.`TITLE` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`DC_TITLE`.`TITLE` LIKE '%" + sValueMin + "%')";
           }
         }
 
         if (sTable.equalsIgnoreCase("Title")) {
           String SQLWhere = "";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             SQLWhere = " (`DC_TITLE`.`TITLE` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             SQLWhere = " (`DC_TITLE`.`TITLE` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             SQLWhere = " (`DC_TITLE`.`TITLE` LIKE '%" + sValueMin + "%')";
           }
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `CHM62EDT_REPORTS` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_REPORTS`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE";
           speciesSQL += "      (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` IN ('DISTRIBUTION_STATUS','LANGUAGE','CONS_STATUS','SPECIES_GEO','LEGAL_STATUS','SPECIES_STATUS','POPULATION_UNIT','TREND'))";
           speciesSQL += "    AND " + SQLWhere;
           speciesSQL += "    UNION";
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_NATURE_OBJECT`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE " + SQLWhere;
           speciesSQL += "    UNION";
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_NATURE_OBJECT`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE " + SQLWhere;
           speciesSQL += "    UNION";
           speciesSQL += "    SELECT";
           speciesSQL += "      `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`";
           speciesSQL += "    FROM";
           speciesSQL += "      `CHM62EDT_SPECIES`";
           speciesSQL += "      INNER JOIN `CHM62EDT_NATURE_OBJECT` ON (`CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` = `CHM62EDT_NATURE_OBJECT`.`ID_NATURE_OBJECT`)";
           speciesSQL += "      INNER JOIN `CHM62EDT_TAXONOMY` ON (`CHM62EDT_SPECIES`.`ID_TAXONOMY` = `CHM62EDT_TAXONOMY`.`ID_TAXONOMY`)";
           speciesSQL += "      INNER JOIN `DC_INDEX` ON (`CHM62EDT_TAXONOMY`.`ID_DC` = `DC_INDEX`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_PUBLISHER` ON (`DC_INDEX`.`ID_DC` = `DC_PUBLISHER`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_TITLE` ON (`DC_INDEX`.`ID_DC` = `DC_TITLE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_SOURCE` ON (`DC_INDEX`.`ID_DC` = `DC_SOURCE`.`ID_DC`)";
           speciesSQL += "      INNER JOIN `DC_DATE` ON (`DC_INDEX`.`ID_DC` = `DC_DATE`.`ID_DC`)";
           speciesSQL += "    WHERE " + SQLWhere;
         }
 
         if (sTable.equalsIgnoreCase("Taxonomy")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT` FROM `CHM62EDT_SPECIES`";
           speciesSQL += " INNER JOIN `CHM62EDT_TAXONOMY` ON (`CHM62EDT_SPECIES`.`ID_TAXONOMY` = `CHM62EDT_TAXONOMY`.`ID_TAXONOMY`)";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " WHERE (`CHM62EDT_TAXONOMY`.`NAME` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " WHERE (`CHM62EDT_TAXONOMY`.`NAME` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " WHERE (`CHM62EDT_TAXONOMY`.`NAME` LIKE '%" + sValueMin + "%')";
           }
         }
 
         if (sTable.equalsIgnoreCase("ThreatStatus")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_COUNTRY` ON (`CHM62EDT_REPORTS`.`ID_GEOSCOPE` = `CHM62EDT_COUNTRY`.`ID_GEOSCOPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_CONSERVATION_STATUS` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_CONSERVATION_STATUS`.`ID_CONSERVATION_STATUS`)";
           speciesSQL += " WHERE (`CHM62EDT_COUNTRY`.`SELECTION` <> 0)";
           speciesSQL += " AND (`CHM62EDT_COUNTRY`.`ISO_2L` IS NOT NULL)";
           speciesSQL += " AND (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'CONSERVATION_STATUS')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`NAME` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`NAME` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`NAME` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`ID_CONSERVATION_STATUS` >= " + GetID(sValueMin, "CONSERVATION_STATUS", "", "") + ")";
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`ID_CONSERVATION_STATUS` <= " + GetID(sValueMax, "CONSERVATION_STATUS", "", "") + ")";
           }
         }
         if (sTable.equalsIgnoreCase("InternationalThreatStatus")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_COUNTRY` ON (`CHM62EDT_REPORTS`.`ID_GEOSCOPE` = `CHM62EDT_COUNTRY`.`ID_GEOSCOPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_CONSERVATION_STATUS` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_CONSERVATION_STATUS`.`ID_CONSERVATION_STATUS`)";
           speciesSQL += " WHERE (`CHM62EDT_COUNTRY`.`AREA_NAME_EN` = 'Europe')";
           speciesSQL += " AND (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'CONSERVATION_STATUS')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`NAME` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`NAME` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`NAME` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`ID_CONSERVATION_STATUS` >= " + GetID(sValueMin, "CONSERVATION_STATUS", "", "") + ")";
             speciesSQL += " AND (`CHM62EDT_CONSERVATION_STATUS`.`ID_CONSERVATION_STATUS` <= " + GetID(sValueMax, "CONSERVATION_STATUS", "", "") + ")";
           }
         }
         if (sTable.equalsIgnoreCase("Country")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_COUNTRY` ON (`CHM62EDT_REPORTS`.`ID_GEOSCOPE` = `CHM62EDT_COUNTRY`.`ID_GEOSCOPE`)";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " WHERE (`CHM62EDT_COUNTRY`.`AREA_NAME_EN` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " WHERE (`CHM62EDT_COUNTRY`.`AREA_NAME_EN` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " WHERE (`CHM62EDT_COUNTRY`.`AREA_NAME_EN` REGEXP '" + sValueMin + "')";
           }
         }
         if (sTable.equalsIgnoreCase("Biogeoregion")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
          speciesSQL += " INNER JOIN `CHM62EDT_BIOGEOREGION` ON (`CHM62EDT_REPORTS`.`ID_GEOSCOPE_LINK` = `CHM62EDT_BIOGEOREGION`.`ID_GEOSCOPE`)";
           if (sOperand.equalsIgnoreCase("Between")) {
             sOperand = "Equal";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " WHERE (`CHM62EDT_BIOGEOREGION`.`NAME` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " WHERE (`CHM62EDT_BIOGEOREGION`.`NAME` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " WHERE (`CHM62EDT_BIOGEOREGION`.`NAME` REGEXP '" + sValueMin + "')";
           }
         }
         if (sTable.equalsIgnoreCase("Abundance")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_ABUNDANCE` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_ABUNDANCE`.`ID_ABUNDANCE`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'ABUNDANCE')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_ABUNDANCE`.`DESCRIPTION` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_ABUNDANCE`.`DESCRIPTION` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_ABUNDANCE`.`DESCRIPTION` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_ABUNDANCE`.`ID_ABUNDANCE` >= " + GetID(sValueMin, "ABUNDANCE", "", "DESCRIPTION") + ")";
             speciesSQL += " AND (`CHM62EDT_ABUNDANCE`.`ID_ABUNDANCE` <= " + GetID(sValueMax, "ABUNDANCE", "", "DESCRIPTION") + ")";
           }
         }
 
         if (sTable.equalsIgnoreCase("LegalStatus")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_LEGAL_STATUS` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_LEGAL_STATUS`.`ID_LEGAL_STATUS`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'LEGAL_STATUS')";
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_LEGAL_STATUS`.`COMMENT` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_LEGAL_STATUS`.`COMMENT` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_LEGAL_STATUS`.`COMMENT` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_LEGAL_STATUS`.`ID_LEGAL_STATUS` >= " + GetID(sValueMin, "LEGAL_STATUS", "", "COMMENT") + ")";
             speciesSQL += " AND (`CHM62EDT_LEGAL_STATUS`.`ID_LEGAL_STATUS` <= " + GetID(sValueMax, "LEGAL_STATUS", "", "COMMENT") + ")";
           }
         }
 
         if (sTable.equalsIgnoreCase("Trend")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_TREND` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_TREND`.`ID_TREND`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'TREND')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_TREND`.`STATUS` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_TREND`.`STATUS` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_TREND`.`STATUS` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_TREND`.`ID_TREND` >= " + GetID(sValueMin, "TREND", "", "STATUS") + ")";
             speciesSQL += " AND (`CHM62EDT_TREND`.`ID_TREND` <= " + GetID(sValueMax, "TREND", "", "STATUS") + ")";
           }
         }
 
         if (sTable.equalsIgnoreCase("DistributionStatus")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_DISTRIBUTION_STATUS` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_DISTRIBUTION_STATUS`.`ID_DISTRIBUTION_STATUS`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'DISTRIBUTION_STATUS')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_DISTRIBUTION_STATUS`.`NAME` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_DISTRIBUTION_STATUS`.`NAME` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_DISTRIBUTION_STATUS`.`NAME` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_DISTRIBUTION_STATUS`.`ID_DISTRIBUTION_STATUS` >= " + GetID(sValueMin, "DISTRIBUTION_STATUS", "", "") + ")";
             speciesSQL += " AND (`CHM62EDT_DISTRIBUTION_STATUS`.`ID_DISTRIBUTION_STATUS` <= " + GetID(sValueMax, "DISTRIBUTION_STATUS", "", "") + ")";
           }
         }
 
         if (sTable.equalsIgnoreCase("SpeciesStatus")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES_STATUS` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_SPECIES_STATUS`.`ID_SPECIES_STATUS`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'SPECIES_STATUS')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_SPECIES_STATUS`.`DESCRIPTION` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_SPECIES_STATUS`.`DESCRIPTION` REGEX '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_SPECIES_STATUS`.`DESCRIPTION` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_SPECIES_STATUS`.`ID_SPECIES_STATUS` >= " + GetID(sValueMin, "SPECIES_STATUS", "", "DESCRIPTION") + ")";
             speciesSQL += " AND (`CHM62EDT_SPECIES_STATUS`.`ID_SPECIES_STATUS` <= " + GetID(sValueMax, "SPECIES_STATUS", "", "DESCRIPTION") + ")";
           }
         }
 
         if (sTable.equalsIgnoreCase("InfoQuality")) {
           speciesSQL = "SELECT DISTINCT `CHM62EDT_REPORTS`.`ID_NATURE_OBJECT`";
           speciesSQL += " FROM `CHM62EDT_REPORTS`";
           speciesSQL += " INNER JOIN `CHM62EDT_SPECIES` ON (`CHM62EDT_REPORTS`.`ID_NATURE_OBJECT` = `CHM62EDT_SPECIES`.`ID_NATURE_OBJECT`)";
           speciesSQL += " INNER JOIN `CHM62EDT_REPORT_TYPE` ON (`CHM62EDT_REPORTS`.`ID_REPORT_TYPE` = `CHM62EDT_REPORT_TYPE`.`ID_REPORT_TYPE`)";
           speciesSQL += " INNER JOIN `CHM62EDT_INFO_QUALITY` ON (`CHM62EDT_REPORT_TYPE`.`ID_LOOKUP` = `CHM62EDT_INFO_QUALITY`.`ID_INFO_QUALITY`)";
           speciesSQL += " WHERE (`CHM62EDT_REPORT_TYPE`.`LOOKUP_TYPE` = 'INFO_QUALITY')";
           if (sOperand.equalsIgnoreCase("Equal")) {
             speciesSQL += " AND (`CHM62EDT_INFO_QUALITY`.`STATUS` = '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Regex")) {
             speciesSQL += " AND (`CHM62EDT_INFO_QUALITY`.`STATUS` REGEXP '" + sValueMin + "')";
           }
           if (sOperand.equalsIgnoreCase("Contains")) {
             speciesSQL += " AND (`CHM62EDT_INFO_QUALITY`.`STATUS` LIKE '%" + sValueMin + "%')";
           }
           if (sOperand.equalsIgnoreCase("Between")) {
             speciesSQL += " AND (`CHM62EDT_INFO_QUALITY`.`ID_INFO_QUALITY` >= " + GetID(sValueMin, "INFO_QUALITY", "", "STATUS") + ")";
             speciesSQL += " AND (`CHM62EDT_INFO_QUALITY`.`ID_INFO_QUALITY` <= " + GetID(sValueMin, "INFO_QUALITY", "", "STATUS") + ")";
           }
         }
         //execute every attribute query
         //attributesFilter+=" AND ID_NATURE_OBJECT IN ("+ExecuteSQL(speciesSQL,"")+")";
       } //end for
       //execute final query
 
 //      speciesSQL="SELECT DISTINCT ID_NATURE_OBJECT FROM CHM62EDT_SPECIES";
 //      speciesFilter=" WHERE 1=1 "+attributesFilter;
 //      speciesSQL+=speciesFilter;
 //      filter+=" ID_NATURE_OBJECT IN ("+ExecuteSQL(speciesSQL,"")+")";
       //System.out.println("filter build start");
       filter += " ID_NATURE_OBJECT IN (" + ExecuteSQL(speciesSQL, "") + ")";
       //System.out.println("filter build");
     }
     //System.out.println("filter = " + filter);
     //System.out.println("filter was build ");
     return filter;
   }
 
   private String ExecuteSQL(String SQL, String Delimiter) {
     String result = "";
     int resCount = 0;
     StringBuffer resultbuf = new StringBuffer();
 
     Connection con = null;
     PreparedStatement ps = null;
 
     try {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
       if (SQL.length() > 0) {
         resultCount = 0;
         ResultSet rs = null;
         ps = con.prepareStatement(SQL);
         //System.out.println("Executing: "+SQL);
         rs = ps.executeQuery();
 
         if (rs.isBeforeFirst()) {
           rs.last();
           resCount = rs.getRow();
           if (resCount > 0) {
             rs.beforeFirst();
             resultbuf.ensureCapacity(resCount * 6);
           }
         }
         //System.out.println("resCount = " + resCount);
 
         while (rs.next()) {
           resultCount++;
           if (resultCount <= SQL_LIMIT) {
 //            result+=Delimiter+rs.getString(1)+Delimiter;
 //            result+=",";
             resultbuf.append(Delimiter).append(rs.getString(1)).append(Delimiter);
             resultbuf.append(",");
           }
         }
         if (resultCount >= SQL_LIMIT) {
 //          System.out.println("<<< SQL LIMIT of "+SQL_LIMIT+" reached!. The results might not be concludent! >>>");
         }
 //        System.out.println("finished build delimited criteria");
         result = resultbuf.toString();
 
         if (result.length() > 0) {
           if (result.substring(result.length() - 1).equalsIgnoreCase(",")) {
             result = result.substring(0, result.length() - 1);
           }
         } else {
           result = "-1";
         }
         rs.close();
         ps.close();
       }
       con.close();
     } catch (Exception e) {
       System.out.println("SQL = " + SQL);
       e.printStackTrace();
       return "";
     }
 
 //    System.out.println("resultCount: "+resultCount);
     return result;
   }
 
   /**
    * Number of results for search.
    * @return Found rows.
    */
   public int getResultCount() {
     return resultCount;
   }
 }
