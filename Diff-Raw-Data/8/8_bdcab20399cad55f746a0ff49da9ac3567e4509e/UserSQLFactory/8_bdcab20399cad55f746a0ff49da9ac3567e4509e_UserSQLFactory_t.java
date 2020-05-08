 package controller.entity;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import model.entity.User;
 import utilities.Utilities;
 import controller.SQLFactory;
 import controller.SQLStamentType;
 
 /**
  * This is the wrapper that allows to perform database specific operation to
  * User table content
  * 
  * @author kornicameister
  * 
  */
 public class UserSQLFactory extends SQLFactory {
 	private final HashMap<Integer, User> users = new HashMap<Integer, User>();
 
 	public UserSQLFactory(SQLStamentType type, User table) {
 		super(type, table);
 	}
 
 	@Override
 	final protected void executeByTableAndType(PreparedStatement st)
 			throws SQLException {
 		User u = (User) this.table;
 		switch (this.type) {
 		case UPDATE:
 			break;
 		case INSERT:
 			int picturePK = this.insertAvatar();
 			u.getPictureFile().setPrimaryKey(picturePK);
 			st.setInt(1, picturePK);
 			st.setObject(2, u);
 			st.execute();
 			st.clearParameters();
 			this.lastAffactedId = Utilities.lastInsertedId(u, st);
 			break;
 		case SELECT:
 			this.parseResultSet(st.executeQuery());
 			break;
 		case DELETE:
 			st.setInt(1, u.getPrimaryKey());
 			this.parseDeleteSet(st.executeUpdate());
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	final protected void parseResultSet(ResultSet set) throws SQLException {
 		User user = null;
 		switch (this.type) {
 		case SELECT:
 			while (set.next()) {
 				byte[] buf = set.getBytes("object");
 				if (buf != null) {
 					try {
 						ObjectInputStream objectIn = new ObjectInputStream(
 								new ByteArrayInputStream(buf));
 						user = (User) objectIn.readObject();
						user.setPrimaryKey(set.getInt("idUser"));
 						this.users.put(user.getPrimaryKey(), user);
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
 				}
 				user = null;
 			}
 			break;
 		case UPDATE:
 			break;
 		default:
 			break;
 		}
 	}
 
 	private Integer insertAvatar() throws SQLException {
 		PictureSQLFactory psf = new PictureSQLFactory(SQLStamentType.INSERT,
 				((User) table).getPictureFile());
 		this.lastAffactedId = psf.executeSQL(localDatabase);
 		return lastAffactedId;
 	}
 
 	public HashMap<Integer, User> getUsers() {
 		return this.users;
 	}
 }
