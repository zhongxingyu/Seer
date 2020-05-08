 /**
  * 
  */
 package controller.entity;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.TreeSet;
 
 import model.BaseTable;
 import model.entity.Author;
 import model.entity.Picture;
 import model.enums.TableType;
 import utilities.Utilities;
 import controller.SQLFactory;
 import controller.SQLStamentType;
 
 /**
  * @author kornicameister
  * 
  */
 public class AuthorSQLFactory extends SQLFactory {
 	private final TreeSet<Author> authors = new TreeSet<Author>();
 
 	public AuthorSQLFactory(SQLStamentType type, BaseTable table) {
 		super(type, table);
 	}
 
 	@Override
 	protected void executeByTableAndType(PreparedStatement st)
 			throws SQLException {
 		Author author = (Author) this.table;
 		switch (this.type) {
 		case UPDATE:
 			st.setInt(1, author.getPrimaryKey());
 			this.parseDeleteSet(st.executeUpdate());
 			break;
 		case INSERT:
 			this.insertEntity(author, st);
 			break;
 		case SELECT:
 			this.parseResultSet(st.executeQuery());
 			break;
 		case DELETE:
 			st.setInt(1, author.getPrimaryKey());
 			this.parseDeleteSet(st.executeUpdate());
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	protected void parseResultSet(ResultSet set) throws SQLException {
 		Author author = null;
 		switch (this.type) {
 		case UPDATE:
 			break;
 		case SELECT:
 			while (set.next()) {
 				byte[] buf = set.getBytes("object");
 				if (buf != null) {
 					try {
 						ObjectInputStream objectIn = new ObjectInputStream(
 								new ByteArrayInputStream(buf));
 						author = (Author) objectIn.readObject();
 						author.setPrimaryKey(set.getInt("idAuthor"));
 						this.authors.add(author);
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
 				}
 				author = null;
 			}
 			break;
 		default:
 			break;
 		}
 		set.close();
 	}
 
 	/**
 	 * Metoda umieszcza w {@link PreparedStatement} zserializowany obiekt klasy
 	 * {@link Author}. Dodatkowo zajmuje się umieszczeniem w bazie danych
 	 * informacji o avatarze danego autora.
 	 * 
 	 * @param entity
 	 *            obiekt klasy {@link Author}, który ma być umieszczony w bazie
 	 *            danych
 	 * @param st
 	 *            obiekt klasy {@link PreparedStatement}
 	 * @throws SQLException
 	 */
 	protected void insertEntity(Author entity, PreparedStatement st)
 			throws SQLException {
 		if(!entity.getTableType().equals(TableType.USER)){
 			st.setString(1, entity.getType().toString());
 			st.setInt(2, this.insertAvatar(entity.getPictureFile()));
 			st.setObject(3, entity);
 		}else{
			st.setObject(2, entity);
			st.setInt(1, this.insertAvatar(entity.getPictureFile()));
 		}
 		st.execute();
 		st.clearParameters();
 		this.lastAffactedId = Utilities.lastInsertedId(entity, st);
 	}
 
 	/**
 	 * Metoda umieszcza w bazie danych informacje o avatarze danego autora
 	 * 
 	 * @param picture
 	 * @return
 	 * @throws SQLException
 	 */
 	private Integer insertAvatar(Picture picture) throws SQLException {
 		if (picture != null) {
 			PictureSQLFactory psf = new PictureSQLFactory(SQLStamentType.INSERT, picture);
 			this.lastAffactedId = psf.executeSQL(false);
 			picture.setPrimaryKey(this.lastAffactedId);
 			return lastAffactedId;
 		}else{
 			return 0;
 		}
 	}
 
 	/**
 	 * Zwraca pobranych autorów
 	 * 
 	 * @return {@link TreeSet} z pobranymi aktorami
 	 */
 	public TreeSet<Author> getAuthors() {
 		return this.authors;
 	}
 
 	@Override
 	public Boolean checkIfInserted() throws SQLException {
 		return true;
 	}
 }
