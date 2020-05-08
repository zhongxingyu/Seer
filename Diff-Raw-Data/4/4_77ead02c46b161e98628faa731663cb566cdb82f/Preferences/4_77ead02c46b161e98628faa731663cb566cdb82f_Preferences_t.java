 package com.nigorojr.typebest;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedHashMap;
 
 public class Preferences extends Database {
     private long id;
     private String username;
     private String keyboardLayout;
     private Color toBeTyped;
     private Color alreadyTyped;
     private Color backgroundColor;
     private Color missTypeColor;
     private Font font;
     // The number of digits to show after decimal point
     private int speedFractionDigit;
     private int timeFractionDigit;
 
     // Force shuffle for now
     // private boolean shuffled;
 
     @SuppressWarnings("serial")
     private static LinkedHashMap<String, String> columnNamesAndTypes = new LinkedHashMap<String, String>() {
         {
             put("ID",
                     "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)");
             put("USERNAME", "VARCHAR(100) NOT NULL");
             put("KEYBOARD_LAYOUT", "VARCHAR(100)");
             put("TO_BE_TYPED", "INT");
             put("ALREADY_TYPED", "INT");
             put("MISS_TYPE_COLOR", "INT");
             put("BACKGROUND_COLOR", "INT");
             put("FONT_FAMILY", "VARCHAR(30)");
             put("FONT_STYLE", "SMALLINT");
             put("FONT_SIZE", "SMALLINT");
             put("SPEED_FRACTION_DIGIT", "SMALLINT");
             put("TIME_FRACTION_DIGIT", "SMALLINT");
         }
     };
 
     final String defaultUsername = "Unknown Username";
 
     public static final String tableName = "PREFERENCES";
 
     /**
      * When neither ID nor the username is provided, a new set of preferences
      * with the default username will be created.
      */
     public Preferences() {
         addPreferencesForUser(defaultUsername);
     }
 
     /**
      * Attempts to retrieve a set of preference from the given ID. If the ID
      * does not match, it'll create a new set of preference with the default
      * username.
      * 
      * @param id
      *            The user ID that will be used to retrieve the preference set
      *            for that user.
      * @throws SQLException
      *             When it failed to establish connection with the database
      *             file.
      */
     public Preferences(long id) throws SQLException {
         super(tableName, columnNamesAndTypes, "ID");
         if (!isIDExist(id)) {
             addPreferencesForUser(defaultUsername);
         }
         // Read from database and update current preferences
         else {
             readPreferencesForID(id);
         }
     }
 
     /**
      * First attempts to find an existing ID for the given ID. If there is no
      * such ID in the database, it will then create a new set of preferences
      * with the given username.
      * 
      * @param id
      *            The user ID.
      * @param username
      *            The username for the new set of preferences when the user ID
      *            is not found.
      * @throws SQLException
      */
     public Preferences(long id, String username) throws SQLException {
         super(tableName, columnNamesAndTypes, "ID");
         if (!isIDExist(id))
             addPreferencesForUser(username);
         else
             readPreferencesForID(id);
     }
 
     /**
      * Creates a new set of preference using the given username. Note that the
      * String parameter is given only when a new set of preferences are to be
      * created. Also, the ID will be determined automatically by the database
      * manager.
      * 
      * @param username
      *            The username that the user chose.
      * @throws SQLException
      *             When it failed to establish a connection with the database
      *             file. Note that it has nothing to do with the table name.
      */
     public Preferences(String username) throws SQLException {
         super(tableName, columnNamesAndTypes, "ID");
         addPreferencesForUser(username);
     }
 
     /**
      * Queries the user preferences database to see if there are any preferences
      * for that user ID. Note that this method also updates the variables if
      * there is at least one match for that ID.
      * 
      * @param id
      *            The user ID that will be the identifier of the user.
      * @return True if there is at least one preference for the given ID, false
      *         if there is none. Note that there should be only one preference
      *         for each ID, however, no check is done in this method to ensure
      *         if that is the case.
      */
     public boolean isIDExist(long id) {
        String condition = String.format("WHERE ID = %d", id);
        String selectColumns = "ID";
         ResultSet result = super.select(selectColumns, condition);
         boolean ret = false;
         try {
             ret = result.next();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return ret;
     }
 
     /**
      * Adds a new set of preferences for the given user to the database. This
      * method assumes that the check for whether or not the table exists has
      * been done prior to the calling of this method.
      * 
      * @param username
      *            The human-readable identifier for the user. However, ID is
      *            used in the database to identify in case there is duplicate of
      *            username.
      */
     public void addPreferencesForUser(String username) {
         init(username);
 
         String[] values = {
                 String.format("'%s'", username),
                 String.format("'%s'", keyboardLayout),
                 Integer.toString(toBeTyped.getRGB()),
                 Integer.toString(alreadyTyped.getRGB()),
                 Integer.toString(missTypeColor.getRGB()),
                 Integer.toString(backgroundColor.getRGB()),
                 String.format("'%s'", font.getFamily()),
                 Integer.toString(font.getStyle()),
                 Integer.toString(font.getSize()),
                 Integer.toString(speedFractionDigit),
                 Integer.toString(timeFractionDigit),
         };
 
         @SuppressWarnings("unchecked")
         LinkedHashMap<String, String> columnNames =
                 (LinkedHashMap<String, String>) columnNamesAndTypes.clone();
         columnNames.remove("ID");
         LinkedHashMap<String, String> columnNamesAndValues =
                 super.createLinkedHashMap(columnNames, values);
 
         super.insert(columnNamesAndValues);
     }
 
     /**
      * Initializes all the variables to the default value.
      */
     public void init(String username) {
         this.username = username;
         keyboardLayout = "QWERTY";
         toBeTyped = Color.BLUE;
         alreadyTyped = Color.RED;
         missTypeColor = Color.CYAN;
         backgroundColor = Color.GRAY;
         font = new Font("Arial", Font.PLAIN, 30);
         // The number of digits to show after decimal point
         speedFractionDigit = 8;
         timeFractionDigit = 9;
     }
 
     /**
      * Queries the database for the given ID and updates the preferences. This
      * method does check if there is an ID match, however, it does not check
      * whether the ID is unique or not.
      * 
      * @param id
      *            The identifier of the user.
      */
     public void readPreferencesForID(long id) {
         String[] selectColumns = { "*" };
         ResultSet result = super.select(selectColumns,
                 String.format("WHERE ID = %d", id));
         try {
             if (result.next()) {
                 this.id = result.getLong("ID");
                 username = result.getString("USERNAME");
                 keyboardLayout = result.getString("KEYBOARD_LAYOUT");
                 toBeTyped = new Color(result.getInt("TO_BE_TYPED"));
                 alreadyTyped = new Color(result.getInt("ALREADY_TYPED"));
                 missTypeColor = new Color(result.getInt("MISS_TYPE_COLOR"));
                 backgroundColor = new Color(result.getInt("BACKGROUND_COLOR"));
                 font = new Font(result.getString("FONT_FAMILY"),
                         result.getInt("FONT_STYLE"), result.getInt("FONT_SIZE"));
                 speedFractionDigit = result.getInt("SPEED_FRACTION_DIGIT");
                 timeFractionDigit = result.getInt("TIME_FRACTION_DIGIT");
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Executes and updates the database for the current preferences.
      */
     public void update() {
         String[] values = {
                 String.format("'%s'", username),
                 String.format("'%s'", keyboardLayout),
                 Integer.toString(toBeTyped.getRGB()),
                 Integer.toString(alreadyTyped.getRGB()),
                 Integer.toString(missTypeColor.getRGB()),
                 Integer.toString(backgroundColor.getRGB()),
                 String.format("'%s'", font.getFamily()),
                 Integer.toString(font.getStyle()),
                 Integer.toString(font.getSize()),
                 Integer.toString(speedFractionDigit),
                 Integer.toString(timeFractionDigit),
         };
 
         // Remove the ID column
         @SuppressWarnings("unchecked")
         LinkedHashMap<String, String> columnNames =
                 (LinkedHashMap<String, String>) columnNamesAndTypes.clone();
         columnNames.remove("ID");
         super.update(createLinkedHashMap(columnNames, values),
                 String.format("WHERE ID = %d", id));
     }
 
     public long getID() {
         return id;
     }
 
     public String getUsername() {
         return username;
     }
 
     /**
      * Because keyboardLayout is a comma-separated String of all the existing
      * keyboard layouts, this method returns the current layout, which is the
      * first layout in the list.
      * 
      * @return The current keyboard layout, which is the first layout in the
      *         comma-separated String.
      */
     public String getKeyboardLayout() {
         int firstComma = keyboardLayout.indexOf(',');
         if (firstComma == -1)
             return keyboardLayout;
         else
             return keyboardLayout.substring(0, firstComma - 1);
     }
 
     /**
      * Due to the fact that keyboardLayout is a comma-separated String, simply
      * returning that variable is the same as returning all the existing
      * keyboard layouts. The first layout is the current layout.
      * 
      * @return A comma-separated list of existing keyboard layouts in String.
      */
     public String getAllKeyboardLayouts() {
         return keyboardLayout;
     }
 
     public Color getToBeTyped() {
         return toBeTyped;
     }
 
     public Color getAlreadyTyped() {
         return alreadyTyped;
     }
 
     public Color getMissTypeColor() {
         return missTypeColor;
     }
 
     public Color getBackgroundColor() {
         return backgroundColor;
     }
 
     public Font getFont() {
         return font;
     }
 
     public int getSpeedFractionDigit() {
         return speedFractionDigit;
     }
 
     public int getTimeFractionDigit() {
         return timeFractionDigit;
     }
 
     public static LinkedHashMap<String, String> getColumnNamesAndTypes() {
         return columnNamesAndTypes;
     }
 
     public String getDefaultUsername() {
         return defaultUsername;
     }
 
     public static String getTablename() {
         return tableName;
     }
 
 }
