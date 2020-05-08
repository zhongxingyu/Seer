 package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MPDAO extends DAO {
 
 	public MPDAO() throws Exception {
 		super(false, false, false, true, false);
 	}
 
 	public void makeModificationPatterns() throws Exception {
 
 		final Statement statement = this.connector.createStatement();
 		final StringBuilder text1 = new StringBuilder();
 		text1.append("select beforeHash from modification ");
 		text1.append("group by beforeHash having count(beforeHash) <> 1");
 		final ResultSet result1 = statement.executeQuery(text1.toString());
 		final List<Integer> hashs = new ArrayList<Integer>();
 		while (result1.next()) {
 			hashs.add(result1.getInt(1));
 		}
 		
 		final StringBuilder text2 = new StringBuilder();
 		text2.append("insert into pattern (beforeHash, afterHash, type, support, confidence) ");
 		text2.append("select A.beforeHash, A.afterHash, A.type, A.a, CAST(A.a AS REAL)/(select count(*) from modification where beforeHash=?) ");
 		text2.append("from (select beforeHash, afterHash, type, count(afterHash) a from modification where beforeHash=? group by afterHash) A");
 		final PreparedStatement pStatement = this.connector
 				.prepareStatement(text2.toString());
 		
		System.out.print("making modification pattern. ");
 		int number = 1;
 		for(final Integer beforeHash : hashs){
 			if(0 == number % 500){
 				System.out.print(number);
 			}else if(0 == number % 100){
 				System.out.print(".");
 			}
 			pStatement.setInt(1, beforeHash);
 			pStatement.setInt(2, beforeHash);
 			pStatement.executeUpdate();
 		}
 		System.out.println(" done.");
 
 		// final Statement statement = this.connector.createStatement();
 		// final StringBuilder insert = new StringBuilder();
 		// insert.append("insert into pattern");
 		// insert.append(" (beforeHash, afterHash, type, support, confidence) ");
 		// insert.append("select A.beforeHash, A.afterHash, A.type, A.a, CAST(A.a AS REAL)/CAST(B.b AS REAL)");
 		// insert.append("from (select beforeHash, afterHash, type, count(afterHash) a ");
 		// insert.append("from modification group by beforeHash, afterHash) A, ");
 		// insert.append("(select beforeHash, count(beforeHash) b ");
 		// insert.append("from modification group by beforeHash) B ");
 		// insert.append("where A.beforeHash = B.beforeHash");
 		// statement.executeUpdate(insert.toString());
 
 		statement.close();
 	}
 }
