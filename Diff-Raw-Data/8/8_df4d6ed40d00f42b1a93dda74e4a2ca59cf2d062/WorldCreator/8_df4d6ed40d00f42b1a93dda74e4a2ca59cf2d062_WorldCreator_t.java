 package welterstellung;
 
 import de.mb.database.SQLAnswerTable;
 import de.mb.database.mysql.MysqlConnectionFactory;
 import de.mb.database.mysql.MysqlSQLExecution;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Diese Klasse kann eine neue Welt erschaffen.
  * @author Andreas
  *
  */
 
 public class WorldCreator{
 	public int fileOutputFlag;
 	public String fileOutputPath;
 	public int debugOutput;
 	public int numberOfArchipelagos;
 	public double sensitivity;
 	public String imagePath;
 	public int mapSection;
 
 	private MysqlConnectionFactory SQLcf;
 	private MysqlSQLExecution SQLexec;
 	private SQLAnswerTable SQLAntwort;
 
 	public String dbhost;
 	public String dbname;
 	public String dbuser;
 	public String dbpass;
 
 
 
 	public WorldCreator(int mapSectionId)
 	{
 		fileOutputFlag = 1;
 		debugOutput = 1;
 		numberOfArchipelagos = 500;
 		sensitivity = 0.5;
 		this.mapSection = mapSectionId;
 
 	}
 
 	/**
 	 * �ffnet einen Kanal in die Datenbank.
 	 * @return gibt 1 zur�ck, wenn es nicht geklappt hat, sonst 0.
 	 */
 	public int OpenConn(){
 		try {
 			SQLcf = new MysqlConnectionFactory(dbhost, dbname);
 			SQLexec = new MysqlSQLExecution(SQLcf.getConnection(dbuser, dbpass));
 
 		} catch (SQLException e1) {
 			//e1.printStackTrace();
 			return 1;
 		}
 		return 0;
 	}
 
 	public void CloseConn(){
 		try {
 			SQLexec.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public void SendQuery(String Query)
 	{
 		try {
 			// one way: Give Query
 			SQLAntwort = SQLexec.executeQuery(Query);
 
 			/*
 			// second way
 			ArrayList fields = new ArrayList();
 			fields.add("id");
 			fields.add("name");
 			ArrayList from = new ArrayList();
 			from.add("island");
 			t = exec.executeQuery(fields, from);
 			System.out.println(t.toString(true));
 			/* */
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Die Methode gibt einen String in eine Textdatei aus.
 	 * @param ausgabe ist der Auszugebende String
 	 * @param pfad ist der Pfad, an den die Datei geschrieben werden soll
 	 * @return 0: Hat funktioniert,
 	 * 1: konnte nicht erstellt werden da die Textdatei nicht gefunden wurde
 	 * 2: Konnte aufgrund einer IOException nicht erstellt werden.
 	 */
 	public int fileOutput(String ausgabe, String pfad)
 	{
 		try {
        		File file = new File(pfad);
         	FileOutputStream fout = new FileOutputStream(file);
         	fout.write(ausgabe.getBytes());
         	fout.close();
         }
         catch (FileNotFoundException e) {
 	            System.out.println("Sorry, die Datei "+pfad+" konnte nicht erstellt werden");
 	            return 1;
         }
         catch (IOException e) {
 	            System.out.println("Sorry, die Datei konnte nicht erstellt werden");
 	            return 2;
         }
         return 0;
 	}
 
 	public int run(){
 		if (imagePath == null) {
 			System.err.println("Bildpfad nicht gesetzt");
 			System.exit(1);
 		}
 
 		File f = new File(imagePath);
 		if (!f.exists()) {
 			System.err.println("Bildpfad '" + imagePath + "' existiert nicht");
 			System.exit(1);
 		}
 
 
 		if (OpenConn()==1){//Datenbankanbindung �ffnen wenn m�glich.
 			if (debugOutput > 0) System.out.println("Die Datenbankverbindung konnte nicht ge�ffnet werden.");
 			return 1;
 		}
 
 		try {
 			SQLexec.startTransaction();
 		} catch (SQLException ex) {
 			Logger.getLogger(WorldCreator.class.getName()).log(Level.SEVERE, null, ex);
 			ex.printStackTrace();
 			System.exit(-1);
 		}
 
 		long zeitstempel = new Date().getTime();
 
 		String createdAt = "'" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "'";
 
 		/* Zunächst wird das neueWelt-Objekt erzeugt, das die Archipelpositionen und -größen enthält.
 		 */
 		SendQuery("SELECT max(id) FROM World;");
 		int welt_id = 1;
 		try
 		{
 			welt_id = Integer.parseInt(SQLAntwort.getDataCell(1, 1));
 
 			SendQuery("SELECT count(*) FROM MapSection WHERE worldID = " + welt_id + ";");
 			int sectionCount = Integer.parseInt(SQLAntwort.getDataCell(1, 1));
 			if (sectionCount > 0) {
 				System.err.println("Die aktuelle Welt '" + welt_id + "' hat bereits generierte Daten. Es muss eine neue leere Welt erstellt werden.");
 				System.exit(1);
 			}
 
 		} catch (NumberFormatException e) {
 			Logger.getAnonymousLogger().log(Level.SEVERE, "Create a world prior to creating islands");
 			System.exit(0);
 		}
 
 		if (debugOutput > 0) System.out.println("Nächster Kartenabschnitt");
 
 		try
 		{
 			SQLexec.executeInsert("INSERT LOW_PRIORITY INTO MapSection (createdAt, worldId) VALUES ("+
 				createdAt +", " + welt_id+ ");");
 
 			SendQuery("SELECT max(id) FROM MapSection;");
 			try
 			{
 				mapSection = Integer.parseInt(SQLAntwort.getDataCell(1,1));
 			}
 			catch (NumberFormatException e)
 			{
 				e.printStackTrace();
 				System.exit(1);
 			}
 
 			// TODO fix the section links
 			/*if (mapSection > 1) {
 				SQLexec.executeUpdate("UPDATE LOW_PRIORITY MapSection SET leftSectionId = "+(mapSection-1)+" WHERE id = " +
 									(mapSection) + ";");
 			}*/
 		}
 		catch(SQLException e)
 		{
 			System.err.println(SQLexec.getLastQuery());
 			e.printStackTrace();
 			System.exit(0);
 		}
 		if (debugOutput>0) System.out.println("Welterstellung gestartet. Archipel werden verteilt...");
 		Archipelverteilung neueWelt;
 		neueWelt = new Archipelverteilung(numberOfArchipelagos, imagePath, sensitivity);
 		neueWelt.run();
 		/* Neue Archipelago in die Datenbank eintragen und mit Namen versehen.
 		 */
 		String archipelQuery="";
 		RandomNames name = new RandomNames();
 		String SqlSyntaxName;
 		for (int i=0; i<neueWelt.Archipelzahl; i++)
 		{
 			SqlSyntaxName = name.setZufallsName().replaceAll("'","\\\\'");
 			archipelQuery = "INSERT LOW_PRIORITY "+
 			"INTO Archipelago (createdAt, xPos, yPos, name, magnitude, mapSectionId) "+
 			"VALUES("+ createdAt + ", " + neueWelt.Orte[i].x+", "+ neueWelt.Orte[i].y +", '"+ SqlSyntaxName +"', "+ (neueWelt.Orte[i].magnitude+1) +", "+ mapSection +");";
 			try
 			{
 				SQLexec.executeInsert(archipelQuery);
 			}
 			catch(SQLException e)
 			{
 				//Jaja... Ein bis zwei Archipelago gehen in die Hose, aber wen st�rts?
 				//Es werden vorher auch zwei bis drei Archipelago zu viel erzeugt (-;
 				System.err.println(SQLexec.getLastQuery());
 				e.printStackTrace();
 				System.exit(0);
 			}
 
 		}
 
 		if (debugOutput>0)
 		{
 			System.out.println("Bisherige Laufzeit: " + ((new Date().getTime())-zeitstempel) + " ms. Pro Archipel sind das " +((new Date().getTime())-zeitstempel)/neueWelt.Archipelzahl + " ms.");
 			System.out.println(neueWelt.Archipelzahl + " Archipel wurden auf der Karte "+mapSection+" verteilt. Inselverteilung wird gestartet...");
 		}
 
 		/* Archipelago sind jetzt auf der Karte verteilt und k�nnen in die Datenbank eingetragen werden.
 		 * Ab hier werden die Inseln in den Archipeln verteilt. Es werden alle Archipele nacheinander
 		 * durchgegangen und zu jedem Archipelago die Inseln erzeugt. Aufgrund der vielen 2-D-Operatoren
 		 * kann dieser Vorgang mehrere Minuten in Anspruch nehmen.
 		 * In die Inseltabelle m�ssen folgende Werte eingetragen werden:
 		 * name, magnitude, xPos, yPos, archipel_id
 		 * Alle Inseln werden mit nur einem Archipelago erzeugt, das aber jeweils aus der Datenbank die
 		 * neuen Eigenschaften bekommt. Der Grund ist, dass die Methode, die unter testen.java
 		 * angewendet wurde und alle Inseln im Hauptspeicher hielt, den Rechner doch arg stark aus-
 		 * gelastet hat. Jetzt entfallen extrem viele Konstruktoraufrufe, was das ganze schon schneller
 		 * machen d�rfte.
 		 */
 		int inselzahl = 0;
 		Archipelago archipel = new Archipelago();
 		SendQuery("SELECT id, xPos, yPos, magnitude "+
 				"FROM Archipelago "+
 				"WHERE mapSectionId = "+ this.mapSection + ";");
 
 		SQLAnswerTable SQLAntwortArchipel;
 		SQLAntwortArchipel = SQLAntwort;
 
 		// SQLAntwort z�hlt beginnend mit der 1! (Schweinerei, wer macht denn sowas? ;-) )
 		for (int i=1; i<=SQLAntwortArchipel.rowCount(); i++)
 		{
 			archipel.archipelagoId = Integer.parseInt(SQLAntwortArchipel.getDataCell(1,i));
 			archipel.x 			= Integer.parseInt(SQLAntwortArchipel.getDataCell(2,i));
 			archipel.y 			= Integer.parseInt(SQLAntwortArchipel.getDataCell(3,i));
 			archipel.magnitude	= Integer.parseInt(SQLAntwortArchipel.getDataCell(4,i));
 			while( archipel.inselnImArchipelVerteilen() == 0);
 			if ((debugOutput>0) && (i%10 ==0)) System.out.println((int)((double)i*100/numberOfArchipelagos) + "% ");
 
 			for (int j=0; j<archipel.numberOfIslands; j++)
 			{
 				SqlSyntaxName = archipel.island[j].getName().replaceAll("'","\\\\'");
 				// Lager f�r die Insel erstellen. Kapazit�t =0. Egal. Ihr wolltet es so.
 				try
 				{
 					SQLexec.executeInsert("INSERT LOW_PRIORITY INTO Storage (createdAt, capacity) VALUES (" + createdAt + ", 0);");
 				}
 				catch (SQLException e)
 				{
 
 					System.out.println ("Es konnte kein Lager für diese Insel erstellt werden: "+ SqlSyntaxName);
 					e.printStackTrace();
 					System.exit(1);
 				}
 				SendQuery("SELECT  max(id) FROM Storage;");
 				archipelQuery = "INSERT LOW_PRIORITY "+
 				"INTO Island (name, size, xPos, yPos, archipelagoId, storageId, createdAt) "+
 				"VALUES('"+SqlSyntaxName+"'"+
				", "+ archipel.island[j].size +
				", "+ archipel.island[j].xPos +
				", "+ archipel.island[j].yPos +
				", "+ archipel.island[j].archipelagosId +
 				", "+ SQLAntwort.getDataCell(1,1) +
 				", "+ createdAt +
 				");";
 				try
 				{
 					SQLexec.executeInsert(archipelQuery);
 					inselzahl++;
 				}
 				catch(SQLException e)
 				{
 					System.out.println("SQL-Insert funktioniert nicht mit folgendem Query: " + archipelQuery);
 					System.err.println(SQLexec.getLastQuery());
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 		}
 		if (debugOutput>0)
 		{
 			System.out.println("Fertig. Dieses Mal wurden " + inselzahl+ " Inseln erzeugt.");
 			System.out.println("gesamte Laufzeit:" + ((new Date().getTime())-zeitstempel) + " ms." );
 		}
 
 		try {
 			SQLexec.commitTransaction();
 		} catch (SQLException ex) {
 			Logger.getLogger(WorldCreator.class.getName()).log(Level.SEVERE, null, ex);
 			System.exit(-1);
 		}
 
 	    CloseConn(); //SQL-Datenbankanbindung wieder schlie�en
 
 		return 0;
 	} //Verteilung
 }
