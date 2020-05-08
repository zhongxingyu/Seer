 package com.hxdcml.wrapper;
 
 import com.hxdcml.card.Card;
 import com.hxdcml.card.CardFactory;
 import com.hxdcml.card.Creature;
 import com.hxdcml.card.Planeswalker;
 import com.hxdcml.sql.QueryMap;
 import com.hxdcml.sql.QueryNode;
 import com.hxdcml.sql.SQLBinder;
 import com.hxdcml.sql.SQLEntities;
 import com.hxdcml.sql.SQLMap;
 import com.hxdcml.sql.SQLite;
 import com.hxdcml.sql.UpdateMap;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import static com.hxdcml.lang.Constant.ABILITY;
 import static com.hxdcml.lang.Constant.ID;
 import static com.hxdcml.lang.Constant.LINK;
 import static com.hxdcml.lang.Constant.LOYALTY;
 import static com.hxdcml.lang.Constant.MANA;
 import static com.hxdcml.lang.Constant.NAME;
 import static com.hxdcml.lang.Constant.POWER;
 import static com.hxdcml.lang.Constant.TOUGHNESS;
 import static com.hxdcml.lang.Constant.TYPE;
 
 /**
  * User: Souleiman Ayoub
  * Date: 12/24/12
  * Time: 2:22 PM
  */
 public class MagicWrapper extends SQLWrapper implements SQLProcedure {
     protected static SQLite lite;
 
     //Table Name
     public static final String TABLE_NAME = "MAGIC";
 
     public MagicWrapper(SQLite lite) throws SQLException {
         MagicWrapper.lite = lite;
         createTable();
     }
 
     /**
      * Creates a Table
      */
     @Override
     protected void createTable() throws SQLException {
 
         PreparedStatement statement = lite.getConnection().prepareStatement(
                 "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                         ID + " INTEGER," + //Multiverse ID
                         NAME + " STRING NOT NULL UNIQUE COLLATE NOCASE," + // NAME
                         TYPE + " STRING NOT NULL COLLATE NOCASE," + // Card Type
                         ABILITY + " STRING COLLATE NOCASE," + //Ability
                         MANA + " STRING COLLATE NOCASE," + //MANA
                         POWER + " STRING," + //POWER
                         TOUGHNESS + " STRING," + //TOUGHNESS
                         LOYALTY + " STRING," + //LOYALTY
                         LINK + " STRING COLLATE NOCASE" + //Link
                         ")"
         );
         statement.executeUpdate();
         statement.close();
     }
 
     /**
      * Creates an INSERT SQL Operation based on the Wrapper
      *
      * @param entities are Objects that contains the data that will be inserted into the
      *                 database.
      */
     @Override
     protected synchronized PreparedStatement inject(SQLEntities entities) throws SQLException {
         String column =
                 String.format(" (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
                         ID, NAME, TYPE, ABILITY, MANA, POWER, TOUGHNESS, LOYALTY, LINK);
         return lite.getConnection().prepareStatement(
                 "INSERT INTO " + TABLE_NAME + column +
                         " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
         );
     }
 
     /**
      * Injects the data into the database.
      *
      * @param entities the SQLEntities that contain information necessary to be inserted to
      *                 the database.
      */
     @Override
     public synchronized void insert(SQLEntities entities) throws SQLException {
         PreparedStatement statement = inject(entities);
 
         SQLMap map = entities.getEntities();
         String name = (String) map.get(NAME);
         statement.setInt(1, (Integer) map.get(ID));
         statement.setString(2, name);
         statement.setString(3, (String) map.get(TYPE));
         statement.setString(4, (String) map.get(ABILITY));
         statement.setString(5, (String) map.get(MANA));
         statement.setString(6, (String) map.get(POWER));
         statement.setString(7, (String) map.get(TOUGHNESS));
         statement.setString(8, (String) map.get(LOYALTY));
         statement.setString(9, (String) map.get(LINK));
         statement.executeUpdate();
         statement.close();
     }
 
     /**
      * Deletes a row based on the primary/reference key
      *
      * @param name the row that will be deleted that is associated with this value.
      */
     @Override
     public synchronized boolean delete(String name) throws SQLException {
         String delete = String.format("DELETE FROM %s WHERE %s LIKE %s",
                 TABLE_NAME, NAME, SQLBinder.format(name));
         return lite.executeUpdate(delete) == 1;
     }
 
     /**
      * Updates the SQL operation where it is equal to the key/r_key and replaced based on
      * the SQLMap values.
      *
      * @param name the row that will be modified that is associated with this value.
      * @param map  values to be changed
      */
     @Override
     public synchronized boolean update(String name, UpdateMap map) throws SQLException {
         String set = map.makeToString();
         String update = String.format("UPDATE %s SET %s WHERE %s LIKE %s",
                 TABLE_NAME, set, NAME, SQLBinder.format(name));
         return lite.executeUpdate(update) == 1;
     }
 
     /**
      * Looks for the data in the database based on the results in the SQLMap
      *
      * @param map query entities that we will use to search in the database
      * @return a ResultSet
      */
     @Override
     public Card[] query(QueryMap map) throws SQLException {
         String result = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s",
                 ID, NAME, TYPE, ABILITY, MANA, POWER, TOUGHNESS, LOYALTY, LINK);
         String query = String.format("SELECT %s FROM %s ", result, TABLE_NAME);
         String condition = "WHERE ";
 
         String end = "";
         for (String value : map.keySet()) {
             QueryNode node = map.get(value);
             String search = node.getValue();
 
             end += value;
             if (value.equals(ID)) {
                 end += " = " + search;
             } else {
                 end += " LIKE ";
                 if (node.isExact())
                     search = (String) SQLBinder.format(search);
                 else if (node.isContains())
                     search = SQLBinder.containsFormat(search);
                else if (node.isExhaust())
                     search = SQLBinder.exhaustFormat(search);
                 end += search;
             }
         }
         String sql = query + condition + end;
         ResultSet set = lite.executeQuery(sql);
         return makeList(set);
     }
 
     /**
      * Parses each value in the ResultSet and should give us an Array of Card.
      *
      * @param set that contains all the data and cursor values
      * @return an Array of Cards
      * @throws SQLException when something goes wrong.
      */
     private Card[] makeList(ResultSet set) throws SQLException {
         ArrayList<Card> list = new ArrayList<Card>();
         Card card;
         while (set.next()) {
             card = new Card();
             card.setId(set.getInt(ID));
             card.setName(set.getString(NAME));
             card.setType(set.getString(TYPE));
             card.setAbility(set.getString(ABILITY));
             card.setMana(set.getString(MANA));
             card.setLink(set.getString(LINK));
             String power = set.getString(POWER);
             String loyalty = set.getString(LOYALTY);
             if (power != null) {
                 Creature creature = CardFactory.create(card, Creature.class);
                 creature.setPower(power);
                 creature.setToughness(set.getString(TOUGHNESS));
                 card = creature;
             } else if (loyalty != null || card.isPlaneswalker()) {
                 Planeswalker walker = CardFactory.create(card, Planeswalker.class);
                 walker.setLoyalty(loyalty == null ? "" : loyalty);
                 card = walker;
             }
             list.add(card);
         }
 
         int size = list.size();
         return list.toArray(new Card[size]);
     }
 
     /**
      * @return an integer value.
      */
     @Override
     public int size() {
         int size = -1;
         try {
             PreparedStatement statement = lite.getConnection().prepareStatement(
                     "SELECT COUNT(*) FROM " + TABLE_NAME
             );
             ResultSet set = statement.executeQuery();
             if (set.next())
                 size = set.getInt("COUNT(*)");
             set.close();
             statement.close();
         } catch (SQLException e) {
             return size;
         }
         return size;
     }
 
     /**
      * Closes the SQLite class from locking the database
      */
     @Override
     public void close() {
         lite.close();
     }
 
     public static void main(String[] args) throws SQLException {
         SQLProcedure procedure = new MagicWrapper(new SQLite());
         //procedure.delete("Fungal Reaches");
         /*QueryMap map = new QueryMap();
         map.put(NAME, new QueryNode("Sleep", QueryNode.EXACT));
         Card[] cards = procedure.query(map);
         for (Card card : cards)
             System.out.println(card.getAbility());*/
         /*UpdateMap map = new UpdateMap();
         map.put(Constant.NAME, "Alpha Myr");
         procedure.update("Bald Myr", map);*/
         procedure.close();
     }
 }
