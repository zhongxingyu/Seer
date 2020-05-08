 package db;
 
import models.City;
 import models.Client;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class DBClient implements IFDBClient
 {
 
     private DataAccess _da;
 	public DBClient()
 	{
 		_da = DataAccess.getInstance();
 	}
 	
 	/**
 	 * Retrieve all clients information from database
 	 *
 	 * @return ArrayList<Client>
 	 */
     @Override
 	public ArrayList<Client> getAllClients() throws Exception
 	{
 		ArrayList<Client> returnList = new ArrayList<Client>();
 
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM Clients");
         _da.setSqlCommandText(query);
         ResultSet clients = _da.callCommandGetResultSet();
 
         while(clients.next())
         {
             Client client = buildClient(clients);
             returnList.add(client);
         }
 
 		return returnList;
 	}
 	
 	/**
 	 *  Retrieve specific client record by id
 	 *  
 	 *  @param id					the id of the record you wish to return
 	 *  @return Client
 	 */
     @Override
 	public Client getClientById(int id) throws Exception
 	{
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM Clients WHERE clientId = ?");
         query.setInt(1, id);
         _da.setSqlCommandText(query);
         ResultSet clientResult = _da.callCommandGetRow();
         if(clientResult.next())
             return buildClient(clientResult);
 
         return null;
 	}
 	
 	/**
 	 * Retrieve specific client by name
 	 * 
 	 * @param name					the name of the record you wish to return
 	 * @return Client
 	 */
     @Override
 	public Client getClientByName(String name) throws Exception
 	{
         PreparedStatement query = _da.getCon().prepareStatement("SELECT * FROM Clients WHERE name = ?");
         query.setString(1, name);
         _da.setSqlCommandText(query);
         ResultSet clientResult = _da.callCommandGetRow();
         if(clientResult.next())
             return buildClient(clientResult);
 
         return null;
 	}
 
     /**
      * Inserts a new client in the database
      *
      * @param client				the object containing the information you want stored
      * @return						returns the number of rows affected
      */
     @Override
 	public int insertClient(Client client) throws Exception
 	{
         if(client == null)
             return 0;
 
         PreparedStatement query = _da.getCon().prepareStatement("INSERT INTO Clients (cityId, name, address, phoneNo, eMail, createdDate, editedDate) VALUES (?, ?, ?, ?, ?, ?, ?)");
 
         query.setString(1, client.getName());
         query.setString(2, client.getAddress());
         query.setInt(3, client.getCity().getCityId());
         query.setLong(4, client.getPhoneNo());
         query.setString(5, client.getEmail());
         query.setDate(6, (java.sql.Date)client.getCreatedDate());
         query.setDate(7, (java.sql.Date)client.getEditedDate());
         _da.setSqlCommandText(query);
 
         return _da.callCommand();
 	}
 
     /**
      * Update a existing client in database
      *
      * @param client 				the object containing the updated information you want stored
      * @return						returns the number of rows affected
      */
     @Override
 	public int updateClient(Client client) throws Exception
 	{
 		if(client == null)
             return 0;
 
         PreparedStatement query = _da.getCon().prepareStatement("UPDATE Clients SET cityId = ?, name = ?, address = ?, phoneNo = ?, eMail = ?, createdDate = ?, editedDate = ? WHERE clientId = ?");
         query.setInt(1, client.getCity().getCityId());
         query.setString(2, client.getName());
         query.setString(3, client.getAddress());
         query.setLong(4, client.getPhoneNo());
         query.setString(5, client.getEmail());
         query.setDate(6, (java.sql.Date)client.getCreatedDate());
         query.setDate(7, (java.sql.Date)client.getEditedDate());
         query.setInt(8, client.getClientId());
         _da.setSqlCommandText(query);
 
         return _da.callCommand();
 	}
 
     /**
      * Delete an existing client from the database
      *
      * @param client 		the object containing the client which should be deleted from the database
      * @return int 			returns the number of rows affected
      */
     @Override
     public int deleteClient(Client client) throws Exception
     {
         if(client == null)
             return 0;
         
         if(getClientById(client.getClientId()) == null)
             return 0;
 
         int rowsAffected = 0;
         PreparedStatement query = _da.getCon().prepareStatement("DELETE FROM Clients WHERE clientId = ?");
         query.setLong(1, client.getClientId());
         _da.setSqlCommandText(query);
         rowsAffected += _da.callCommand();
 
         return rowsAffected;
     }
 
 	private Client buildClient(ResultSet row) throws Exception
 	{
 		if(row == null)
 			return null;
 		
         int clientId = row.getInt("clientId");
        DBCity dbc = new DBCity();
        City city = dbc.getCityById(row.getInt("cityId"));
         String name = row.getString("name");
         String address = row.getString("address");
         long phoneNo = row.getLong("phoneNo");
         String eMail = row.getString("eMail");
         Date createdDate = row.getDate("createdDate");
         Date editedDate = row.getDate("editedDate");
 
         return new Client(clientId, name, address, city, phoneNo, eMail, createdDate, editedDate);
 	}
 }
