 package Databank;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.List;
 import javax.swing.JOptionPane;
 
 
 public class Connectie_Databank
 {
     //Eigenschappen databank
     private String connectieString = "";
     private Connection connectie = null;
     private PreparedStatement prepStatement = null;
     private Statement statement = null;
     private ResultSet inhoudQuery = null;
     
     //Inloggegevens PhpMyAdmin
     private String gebruikersnaam, wachtwoord;
     
     //Constructor met standaardinstellingen
     public Connectie_Databank()
     {
         this.connectieString = "jdbc:mysql://localhost/groep2_festivals";
         this.gebruikersnaam = "root";
         this.wachtwoord = "";
     }
     
     //Constructor met nieuwe data
     public Connectie_Databank(String connectionString, String gebruikersnaam, String wachtwoord)
     {
         this.connectieString = connectionString;
         this.gebruikersnaam = gebruikersnaam;
         this.wachtwoord = wachtwoord;
     }
     
     /**
      * Deze methode zorgt ervoor dat er verbinding gemaakt wordt me de databank
      */
     public void maakConnectie()
     {
         try
         {
             Class.forName("com.mysql.jdbc.Driver");
             connectie = DriverManager.getConnection(connectieString, gebruikersnaam, wachtwoord);
         }
         catch(Exception e)
         {
             System.err.println("FOUTMELDING: " + e.getMessage());
         }
     }
     
     /**
      * Deze functie haalt gegevens op uit de database aan de hand van de opgegeven query.
      * @param query
      * @param parameters 
      */
     public void voerQueryUit(String query, List<String> parameters) throws Exception
     {
         try
         {
             if(parameters.size() > 0)
             {
                 //Reden preparedStatement: geen SQL-Injectie!
                 prepStatement = connectie.prepareStatement(query);
 
                 //Lijst met parameters uitlezen om de preparedStatement op te vullen
                 for(int i=1; i<=parameters.size(); i++)
                 {
                    prepStatement.setString(i, parameters.get(i-1));
                 }
                 inhoudQuery = prepStatement.executeQuery();
             }
             else
             {
                 statement = connectie.createStatement();
                 inhoudQuery = statement.executeQuery(query);
             }
         }
         catch(Exception e)
         {
             System.err.println("FOUT BIJ UITVOEREN QUERY: " + e.getMessage());
             throw e;
         }
     }
     
     public void voerUpdateUit(String query, List<String> parameters) throws Exception
     {
         try
         {
             if(parameters.size() > 0)
             {
                 //Reden preparedStatement: geen SQL-Injectie!
                 prepStatement = connectie.prepareStatement(query);
 
                 //Lijst met parameters uitlezen om de preparedStatement op te vullen
                 for(int i=1; i<=parameters.size(); i++)
                 {
                    prepStatement.setString(i, parameters.get(i-1));
                 }
                 prepStatement.executeUpdate();
             }
             else
             {
                 statement = connectie.createStatement();
                 statement.executeUpdate(query);
             }
         }
         catch(Exception e)
         {
             System.err.println("FOUT BIJ UPDATEN: " + e.getMessage());
             throw e;
         }
     }
     
     public ResultSet haalResultSetOp()
     {
         return inhoudQuery;
     }
     
     /**
      * Deze functie sluit alle connectiegegevens af zodat deze uit het geheugen verdwijnen.
      */
     public void sluitConnectie()
     {
         //ConnectieString leegmaken en alle objecten die te maken hebben met de connectie sluiten
         try
         {
             if(connectie != null)
             {
                 connectie.close();
             }
             if(prepStatement != null)
             {
                 prepStatement.close();
             }
             if(inhoudQuery != null)
             {
                 inhoudQuery.close();
             }
             connectie = null;
             statement = null;
             prepStatement = null;
             inhoudQuery = null;
         }
         catch(Exception e)
         {}
     }
     
    public int updateQuery(String query, List<String> parameters)
     {
         int aantalUpdates = 0;
         try
         {
             if(parameters.size() > 0)
             {
                 
                 //Reden preparedStatement: geen SQL-Injectie!
                 prepStatement = connectie.prepareStatement(query);
 
                 //Lijst met parameters uitlezen om de preparedStatement op te vullen
                 for(int i=1; i<=parameters.size(); i++)
                 {
                    prepStatement.setString(i, parameters.get(i-1));
                 }
                 aantalUpdates = prepStatement.executeUpdate();
                 
             }
         }
         catch(Exception e)
         {
             
            System.out.println(e.getMessage()+"");
         }
         return aantalUpdates;
     }
 }
