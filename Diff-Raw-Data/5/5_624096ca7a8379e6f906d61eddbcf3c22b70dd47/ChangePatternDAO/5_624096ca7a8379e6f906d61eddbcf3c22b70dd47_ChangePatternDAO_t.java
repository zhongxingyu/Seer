 package yoshikihigo.cpanalyzer.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import yoshikihigo.cpanalyzer.Config;
 
 public class ChangePatternDAO {
 
 	static public final String PATTERNS_SCHEMA = "id integer primary key autoincrement, beforeHash blob, afterHash blob, type integer, support integer, confidence real, nos integer, adr string";
 
 	private Connection connector;
 
 	public ChangePatternDAO() {
 
 		try {
 
 			Class.forName("org.sqlite.JDBC");
 			final String database = Config.getInstance().getDATABASE();
 			this.connector = DriverManager.getConnection("jdbc:sqlite:"
 					+ database);
 			final Statement statement = connector.createStatement();
 			statement.executeUpdate("drop table if exists patterns");
 			statement.executeUpdate("create table patterns (" + PATTERNS_SCHEMA
 					+ ")");
 			statement.close();
 
 		} catch (ClassNotFoundException | SQLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	public void makeIndicesOnCODES() {
 
 		try {
 			final Statement statement = this.connector.createStatement();
 			statement
					.executeUpdate("create index index_hash_codes on codes(hash)");
 			statement
					.executeUpdate("create index index_text_codes on codes(text)");
 			statement.close();
 		}
 
 		catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	public void makeIndicesOnCHANGES() {
 
 		try {
 			final Statement statement = this.connector.createStatement();
 			statement
 					.executeUpdate("create index index_beforeHash_changes on changes(beforeHash)");
 			statement
 					.executeUpdate("create index index_afterHash_changes on changes(afterHash)");
 			statement
 					.executeUpdate("create index index_beforeHash_afterHash_changes on changes(beforeHash, afterHash)");
 			statement.close();
 		}
 
 		catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	public void makeChangePatterns() {
 
 		try {
 
 			System.out.print("making change patterns ...");
 			final List<byte[]> hashs = new ArrayList<>();
 			{
 				final Statement statement = this.connector.createStatement();
 				final StringBuilder text = new StringBuilder();
 				text.append("select beforeHash from changes ");
 				text.append("group by beforeHash having count(beforeHash) <> 1");
 				final ResultSet result = statement
 						.executeQuery(text.toString());
 				while (result.next()) {
 					hashs.add(result.getBytes(1));
 				}
 				statement.close();
 			}
 
 			{
 				final StringBuilder text = new StringBuilder();
 				text.append("insert into patterns (beforeHash, afterHash, type, support, confidence) ");
 				text.append("select A.beforeHash, A.afterHash, A.type, A.times, ");
 				text.append("CAST(A.times AS REAL)/(select count(*) from changes where beforeHash=?)");
 				text.append("from (select beforeHash, afterHash, type, count(*) times ");
 				text.append("from changes where beforeHash=? group by afterHash) A");
 				final PreparedStatement statement = this.connector
 						.prepareStatement(text.toString());
 
 				int number = 1;
 				for (final byte[] beforeHash : hashs) {
 					if (0 == number % 500) {
 						System.out.print(number);
 					} else if (0 == number % 100) {
 						System.out.print(".");
 					}
 					if (0 == number % 5000) {
 						System.out.println();
 					}
 					statement.setBytes(1, beforeHash);
 					statement.setBytes(2, beforeHash);
 					statement.executeUpdate();
 					number++;
 				}
 				statement.close();
 			}
 			{
 				final Statement statement = this.connector.createStatement();
 				statement
 						.executeUpdate("create index index_beforeHash_patterns on patterns(beforeHash)");
 				statement
 						.executeUpdate("create index index_afterHash_patterns on patterns(afterHash)");
 				statement
 						.executeUpdate("create index index_beforeHash_afterHash_patterns on patterns(beforeHash, afterHash)");
 				statement.close();
 			}
 			System.out.println(" done.");
 
 			System.out.print("calculating metrics ...");
 			final List<byte[][]> hashpairs = new ArrayList<>();
 			{
 				final Statement statement = this.connector.createStatement();
 				final ResultSet result = statement
 						.executeQuery("select beforeHash, afterHash from patterns");
 				while (result.next()) {
 					final byte[][] hashpair = new byte[2][];
 					hashpair[0] = result.getBytes(1);
 					hashpair[1] = result.getBytes(2);
 					hashpairs.add(hashpair);
 				}
 				statement.close();
 			}
 
 			{
 				final StringBuilder text = new StringBuilder();
 				text.append("update patterns set nos = (select count(distinct software) ");
 				text.append("from changes C where C.beforeHash = ? and C.afterHash = ?) ");
 				text.append("where beforeHash = ? and afterHash = ?");
 				final PreparedStatement statement = this.connector
 						.prepareStatement(text.toString());
 
 				int number = 1;
 				for (final byte[][] hashpair : hashpairs) {
 					if (0 == number % 1000) {
 						System.out.print(number);
 					} else if (0 == number % 100) {
 						System.out.print(".");
 					}
 					if (0 == number % 5000) {
 						System.out.println();
 					}
 					statement.setBytes(1, hashpair[0]);
 					statement.setBytes(2, hashpair[1]);
 					statement.setBytes(3, hashpair[0]);
 					statement.setBytes(4, hashpair[1]);
 					statement.executeUpdate();
 					number++;
 				}
 				statement.close();
 			}
 			{
 				final Statement statement = this.connector.createStatement();
 				statement
 						.executeUpdate("create index index_support_patterns on patterns(support)");
 				statement
 						.executeUpdate("create index index_confidence_patterns on patterns(confidence)");
 				statement
 						.executeUpdate("create index index_nos_patterns on patterns(nos)");
 				statement.close();
 			}
 			System.out.println(" done.");
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	public void close() {
 		try {
 			this.connector.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 }
