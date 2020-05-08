 package to.joe.decapitation.datastorage;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 import to.joe.decapitation.Bounty;
 import to.joe.decapitation.Decapitation;
 
 public class MySQLDataStorageImplementation implements DataStorageInterface {
 
     Decapitation plugin;
     private Connection connection;
 
     public MySQLDataStorageImplementation(Decapitation decapitation, String url, String username, String password) throws SQLException {
         plugin = decapitation;
         connection = DriverManager.getConnection(url, username, password);
         
         final ResultSet bansExists = connection.getMetaData().getTables(null, null, "bounties", null);
         if (!bansExists.first()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("mysql.sql")));
             final StringBuilder builder = new StringBuilder();
             String next;
             try {
                 while ((next = reader.readLine()) != null) {
                     builder.append(next);
                 }
                getFreshPreparedStatementColdFromTheRefrigerator(builder.toString()).execute();
             } catch (final IOException e) {
                 throw new SQLException("Could not load default table creation text", e);
             }
         }
        
     }
 
     private PreparedStatement getFreshPreparedStatementColdFromTheRefrigerator(String query) throws SQLException {
         return connection.prepareStatement(query);
     }
 
     private PreparedStatement getFreshPreparedStatementWithGeneratedKeys(String query) throws SQLException {
         return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getNumBounties() throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT count(*) FROM bounties WHERE hunter IS NULL");
             ResultSet rs = ps.executeQuery();
             rs.next();
             return rs.getInt(1);
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ArrayList<Bounty> getBounties(int min, int max) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunter IS NULL ORDER BY bounties.reward DESC LIMIT " + min + "," + max);
             ResultSet rs = ps.executeQuery();
             ArrayList<Bounty> bounties = new ArrayList<Bounty>();
             while (rs.next()) {
                 bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
             }
             return bounties;
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Bounty getBounty(String hunted) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunted LIKE ? AND hunter IS NULL ORDER BY bounties.reward DESC LIMIT 1");
             ps.setString(1, hunted);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 return new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8));
             } else {
                 return null;
             }
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Bounty addBounty(Bounty bounty) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementWithGeneratedKeys("INSERT INTO bounties (issuer, hunted, reward) VALUES (?,?,?)");
             ps.setString(1, bounty.getIssuer());
             ps.setString(2, bounty.getHunted());
             ps.setDouble(3, bounty.getReward());
             ps.execute();
             ResultSet rs = ps.getGeneratedKeys();
             rs.next();
             ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE id = ?");
             ps.setInt(1, rs.getInt(1));
             rs = ps.executeQuery();
             rs.next();
             return new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8));
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateBounty(Bounty bounty) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("UPDATE bounties SET issuer = ?, hunted = ?, reward = ?, created = ?, hunter = ?, turnedin = ?, redeemed = ? WHERE id = ?");
             ps.setString(1, bounty.getIssuer());
             ps.setString(2, bounty.getHunted());
             ps.setDouble(3, bounty.getReward());
             ps.setTimestamp(4, new Timestamp(bounty.getCreated().getTime()));
             if (bounty.getHunter() == null)
                 ps.setNull(5, Types.VARCHAR);
             else
                 ps.setString(5, bounty.getHunter());
 
             if (bounty.getTurnedIn() == null)
                 ps.setNull(6, Types.TIMESTAMP);
             else
                 ps.setTimestamp(6, new Timestamp(bounty.getTurnedIn().getTime()));
 
             if (bounty.getRedeemed() == null)
                 ps.setNull(7, Types.TIMESTAMP);
             else
                 ps.setTimestamp(7, new Timestamp(bounty.getRedeemed().getTime()));
             ps.setInt(8, bounty.getID());
 
             ps.execute();
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteBounty(Bounty bounty) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("DELETE FROM bounties WHERE id = ?");
             ps.setInt(1, bounty.getID());
             ps.execute();
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Bounty> getBounties(String hunted) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunted LIKE ? AND hunter IS NULL ORDER BY bounties.hunted ASC");
             ps.setString(1, "%" + hunted + "%");
             ResultSet rs = ps.executeQuery();
             ArrayList<Bounty> bounties = new ArrayList<Bounty>();
             while (rs.next()) {
                 bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
             }
             return bounties;
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Bounty getBounty(String hunted, String issuer) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunted LIKE ? AND ISSUER LIKE ? AND hunter IS NULL ORDER BY bounties.reward DESC LIMIT 1");
             ps.setString(1, hunted);
             ps.setString(2, issuer);
             ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 return new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8));
             } else {
                 return null;
             }
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Bounty> getOwnBounties(String issuer) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE issuer LIKE ? AND hunter IS NULL ORDER BY bounties.reward DESC");
             ps.setString(1, issuer);
             ResultSet rs = ps.executeQuery();
             ArrayList<Bounty> bounties = new ArrayList<Bounty>();
             while (rs.next()) {
                 bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
             }
             return bounties;
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getNumUnclaimedHeads(String issuer) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT count(*) FROM bounties WHERE issuer LIKE ? AND turnedin IS NOT NULL AND redeemed IS NULL");
             ps.setString(1, issuer);
             ResultSet rs = ps.executeQuery();
             rs.next();
             return rs.getInt(1);
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ArrayList<Bounty> getUnclaimedBounties(String issuer) throws DataStorageException {
         try {
             PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE issuer LIKE ? AND turnedin IS NOT NULL AND redeemed IS NULL");
             ps.setString(1, issuer);
             ResultSet rs = ps.executeQuery();
             ArrayList<Bounty> bounties = new ArrayList<Bounty>();
             while (rs.next()) {
                 bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
             }
             return bounties;
         } catch (SQLException e) {
             throw new DataStorageException(e);
         }
     }
 
 }
