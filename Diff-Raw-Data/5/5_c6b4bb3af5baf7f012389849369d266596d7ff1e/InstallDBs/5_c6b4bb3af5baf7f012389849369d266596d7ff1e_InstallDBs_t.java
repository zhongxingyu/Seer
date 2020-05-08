 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Scanner;
 import java.io.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: andre
  * Date: 18.06.13
  * Time: 16:13
  * To change this template use File | Settings | File Templates.
  */
 public class InstallDBs {
     public void exec(SQLConnection connection,
         String path,
         String serverURL,
         String serverPort,
         String databaseName,
         String userName,
         String password
     )
     {
 		PrintStream out = System.out;
 		Scanner in = new Scanner(System.in).useDelimiter("\n");
 
         if (!connection.tableExist("WETTERSTATION") || !connection.tableExist("WETTERMESSUNG"))
         {
         	out.println("Die Wetterdaten sind nicht eingelesen jetzt einlesen ( = 1 Tasse Kaffee)? (y/n)");
         	int answer = -1;
         	do {
         		String userInput = in.nextLine();
         		if( userInput.trim().equals("y"))
         			answer = 1;
         		else if( userInput.trim().equals("n"))
         			answer = 0;
         		else
         			out.println("invalid input!");
         	}
         	while(answer == -1);
         	if( answer == 0)
         		System.exit(0);
         	
         	System.out.println("INFO: Lese Wetterdaten ein!");
             dynamicimport(connection,path, serverURL,serverPort,databaseName,userName,password,"wetterdaten/dd.sql");
             dynamicimport(connection,path, serverURL,serverPort,databaseName,userName,password,"wetterdaten/dm.sql");
         }
 
         if (   !connection.tableExist("geodb_type_names") || !connection.tableExist("geodb_locations")
             || !connection.tableExist("geodb_hierarchies") || !connection.tableExist("geodb_coordinates")
             || !connection.tableExist("geodb_textdata") || !connection.tableExist("geodb_intdata")
             || !connection.tableExist("geodb_floatdata") || !connection.tableExist("geodb_changelog"))
         {
         	out.println("Die Geodaten sind nicht eingelesen. Jetzt einlesen ( = sehr viele Tasse Kaffee)? (y/n)");
         	int answer = -1;
         	do {
         		String userInput = in.nextLine();
         		if( userInput.trim().equals("y"))
         			answer = 1;
         		else if( userInput.trim().equals("n"))
         			answer = 0;
         		else
         			out.println("invalid input!");
         	}
         	while( answer == -1);
         	if( answer == 0)
         		System.exit(0);
         	
             System.out.println("INFO: Lese Geodaten ein!");
             dynamicimport(connection,path, serverURL,serverPort,databaseName,userName,password,"opengeodb/opengeodb-begin2.sql");
             dynamicimport(connection,path, serverURL,serverPort,databaseName,userName,password,"opengeodb/DE2.sql");
             dynamicimport(connection,path, serverURL,serverPort,databaseName,userName,password,"opengeodb/opengeodb-end.sql");
         }
         //erstelle den wetterstationview
         try {
             connection.update("create or replace view dbsp_wetterstation_view\n" +
                             "as\n" +
                             "select s_id as station_id, geo_laenge as laenge, geo_breite as breite\n" +
                             "from wetterstation\n" +
                             ";");
         }
         catch(SQLException e){
             System.out.println("dbsp_wetterstation view konnte nicht erzeugt werden: "+e.getMessage());
         }
         //erstelle den wettervermessungview
         try {
             connection.update("create or replace view dbsp_wettermessung_view\n" +
                     "\t\tas\n" +
                     "\t\tselect stations_id as station_id, datum, qualitaet, min_5cm, min_2m, mittel_2m, max_2m, relative_feuchte, mittel_windstaerke, max_windgeschwindigkeit, sonnenscheindauer, mittel_bedeckungsgrad, niederschlagshoehe, mittel_luftdruck\n" +
                     "\t\tfrom wettermessung\n" +
                     "\t\t;");
         }
         catch(SQLException e){
             System.out.println("dbsp_wettermessung view konnte nicht erzeugt werden: "+e.getMessage());
         }
         //erstelle den stadtview
         try {
             connection.update("create or replace view dbsp_stadt_view\n" +
                     "\t\t\tas\n" +
                     "\t\t\tselect loc.loc_id as stadt_id, text.text_val as name, coord.lon as laenge, coord.lat as breite\n" +
                     "\t\t\tfrom geodb_locations as loc\n" +
                     "\t\t\tjoin geodb_textdata as text on text.loc_id = loc.loc_id\n" +
                     "\t\t\tjoin geodb_coordinates as coord on coord.loc_id = loc.loc_id\n" +
                     "\t\t\twhere loc.loc_type = 100600000 /* typ: Politische Gliederung */\n" +
                     "\t\t\tand text.text_type = 500100000 /* der name der location*/\n" +
                     "\t\t\t;");
         }
         catch(SQLException e){
             System.out.println("dbsp_stadt view konnte nicht erzeugt werden: "+e.getMessage());
         }
 
         try {
             connection.update("select * into dbsp_wetterstation from dbsp_wetterstation_view;");
             connection.update("select * into dbsp_wettermessung from dbsp_wettermessung_view;");
             connection.update("select * into dbsp_stadt from dbsp_stadt_view;");
         }
         catch(SQLException e){
             System.out.println("tables aus views konnten nicht erstellt werden:"+e.getMessage());
         }
 
 
 
         //erstelle die Kreuzprodukttabelle
         try {
             connection.update("create table dbsp_relevantfor(" +
                     "station_id int NOT NULL," +
                     "stadt_id int NOT NULL," +
                    "distance double precision," +
                     "PRIMARY KEY(station_id,stadt_id)" +
                     //"FOREIGN KEY(station_id)," +
                     //"FOREIGN KEY(stadt_id)" +
                     ")" +
                     ";");
         }
         catch(SQLException e){
             System.out.println("dbsp_relevantfor konnte nicht erstellt werden! "+e.getMessage());
         }


     }
 
 
     public void dynamicimport(SQLConnection connection,
                               String path,
                               String serverURL,
                               String serverPort,
                               String databaseName,
                               String userName,
                               String password,
                               String fileName
     )
     {
         try {
             String line;
             Process p = Runtime.getRuntime().exec
                     ("psql -U "+userName+" -d "+databaseName+" -h "+serverURL+" -f "+path+fileName);
             BufferedReader input =
                     new BufferedReader
                             (new InputStreamReader(p.getErrorStream()));
             while ((line = input.readLine()) != null) {
                 System.out.println(line);
             }
             input.close();
         }
         catch (Exception err) {
             err.printStackTrace();
         }
     }
 }
