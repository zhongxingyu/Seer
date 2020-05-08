 package pl.remadag.madzia.pat.database.procedure;
 
 import pl.remadag.madzia.pat.data.ComplexTriple;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * PostgreSQL procedures
  * User: marcin
  * Date: 2010-08-24
  */
 public class PostgreSQLProcedures implements SpiderProcedures {
 
     /**
      * SQL connection.
      */
     private Connection sqlConn = null;
 
     public PostgreSQLProcedures(final Connection connection) {
         this.sqlConn = connection;
     }
 
     @Override
     public void callInsert(String statementString) throws SQLException {
 /*        String sqlString = statementString;
         CallableStatement statement = sqlConn.prepareCall(sqlString);
         statement.execute();
         int iStatus = statement.getInt(1);
         sqlConn.commit();*/
         Statement statement = sqlConn.createStatement();
 //        statement.executeU(statementString);
         Statement s = null;
         try {
             s = sqlConn.createStatement();
         } catch (SQLException se) {
             System.out.println("We got an exception while creating a statement:" +
                     "that probably means we're no longer connected.");
             se.printStackTrace();
             System.exit(1);
         }
 
         int m = 0;
 
         try {
             m = s.executeUpdate(statementString);
         } catch (SQLException se) {
             System.out.println("We got an exception while executing our query:" +
                     "that probably means our SQL is invalid");
             se.printStackTrace();
             System.exit(1);
         }
 
         System.out.println("Successfully modified " + m + " rows.\n");
 
     }
 
     @Override
     public void callSelectP(String question) throws SQLException {
         CallableStatement statement = sqlConn.prepareCall("COPY ( select p_" + question + "," +
                 "sum(case when code like '%K%' then 1 else 0 end) as k,''," +
                 "sum(case when code like '%M%' then 1 else 0 end) as m,''," +
                 "sum(case when null is null then 1 else 0 end) as all,''," +
 
                 "sum(case when m_mz = 'a' then 1 else 0 end) as m_mza,''," +
                 "sum(case when m_mz = 'b' then 1 else 0 end) as m_mzb,''," +
                 "sum(case when m_mz = 'c' then 1 else 0 end) as m_mzc,''," +
                 "sum(case when m_mz = 'd' then 1 else 0 end) as m_mzd,''," +
                 "sum(case when m_mz = 'e' then 1 else 0 end) as m_mze,''," +
 
                 "sum(case when m_lr = '0' then 1 else 0 end) as lr_0, ''," +
                 "sum(case when m_lr = '1' then 1 else 0 end) as lr_1, ''," +
                 "sum(case when m_lr = '2' then 1 else 0 end) as lr_2, ''," +
                 "sum(case when m_lr = '3' then 1 else 0 end) as lr_3, ''," +
                 "sum(case when m_lr in ('4', '5', '6', '7') then 1 else 0 end) as lr_4_wiecej, ''," +
 
                 "sum(case when m_wr LIKE '%a%' then 1 else 0 end) as wr_a, ''," +
                 "sum(case when m_wr LIKE '%b%' then 1 else 0 end) as wr_b, ''," +
                 "sum(case when m_wr LIKE '%c%' then 1 else 0 end) as wr_c, ''," +
 
                 "sum(case when m_wm = 'c' then 1 else 0 end) as wmc,''," +
                 "sum(case when m_wm = 'd' then 1 else 0 end) as wmd,''," +
                 "sum(case when m_wm = 'e' then 1 else 0 end) as wme,''," +
                 "sum(case when m_wm = 'f' then 1 else 0 end) as wmf,''," +
                 "sum(case when m_wm = 'g' then 1 else 0 end) as wmg,''," +
                 "sum(case when m_wm not in ('a','b','c','d','e', 'f', 'g') then 1 else 0 end) as wmnic,''," +
 
                 "sum(case when m_wo = 'c' then 1 else 0 end) as woc,''," +
                 "sum(case when m_wo = 'd' then 1 else 0 end) as wod,''," +
                 "sum(case when m_wo = 'e' then 1 else 0 end) as woe,''," +
                 "sum(case when m_wo = 'f' then 1 else 0 end) as wof,''," +
                 "sum(case when m_wo = 'g' then 1 else 0 end) as wog,''," +
 
 
                 "sum(case when m_wykm = 'a' then 1 else 0 end) as wykma,''," +
                 "sum(case when m_wykm = 'b' then 1 else 0 end) as wykmb,''," +
                 "sum(case when m_wykm = 'c' then 1 else 0 end) as wykmc,''," +
                 "sum(case when m_wykm = 'd' then 1 else 0 end) as wykmd,''," +
                 "sum(case when m_wykm not in ('a','b','c','d') then 1 else 0 end) as wykmnic,''," +
 
                 "sum(case when m_wyko = 'a' then 1 else 0 end) as wykoa,''," +
                 "sum(case when m_wyko = 'b' then 1 else 0 end) as wykob,''," +
                 "sum(case when m_wyko = 'c' then 1 else 0 end) as wykoc,''," +
                 "sum(case when m_wyko = 'd' then 1 else 0 end) as wykod, ''" +
 
                 "    from ankieta_pat group by p_" + question + " order by p_" + question + " " +
 
                 ") TO '/tmp/p_a" + question + ".csv' WITH CSV;");
         statement.execute();
         sqlConn.commit();
     }
 
     public ComplexTriple callSelectComplex(String question, String letter, String where) throws SQLException {
 
         String sqlString = "SELECT p_" + question + letter + ", count(*) from ankieta_pat " + where + " group by p_"
                 + question + letter + " order by count(*) desc limit 1;";
 //        System.out.println("Executing: " + sqlString);
 
         Statement sql = sqlConn.createStatement();
         ResultSet results = sql.executeQuery(sqlString);
         int answer = 0;
         int answerCount = 0;
         if (results != null) {
             while (results.next()) {
                 answer = results.getInt(1);
                 answerCount = results.getInt(2);
             }
             results.close();
         }
         return new ComplexTriple(letter, answer, answerCount);
 
     }
 
     public String makeSelectForUnion(String question, String letter) {
         return "select '" + letter + "'," +
                 "sum(case when code like '%K%' then 1 else 0 end) as k,''," +
                 "sum(case when code like '%M%' then 1 else 0 end) as m,''," +
                 "sum(case when null is null then 1 else 0 end) as all,''," +
 
                 "sum(case when m_mz = 'a' then 1 else 0 end) as m_mza,''," +
                 "sum(case when m_mz = 'b' then 1 else 0 end) as m_mzb,''," +
                 "sum(case when m_mz = 'c' then 1 else 0 end) as m_mzc,''," +
                 "sum(case when m_mz = 'd' then 1 else 0 end) as m_mzd,''," +
                 "sum(case when m_mz = 'e' then 1 else 0 end) as m_mze,''," +
 
                 "sum(case when m_lr = '0' then 1 else 0 end) as lr_0, ''," +
                 "sum(case when m_lr = '1' then 1 else 0 end) as lr_1, ''," +
                 "sum(case when m_lr = '2' then 1 else 0 end) as lr_2, ''," +
                 "sum(case when m_lr = '3' then 1 else 0 end) as lr_3, ''," +
                 "sum(case when m_lr in ('4', '5', '6', '7') then 1 else 0 end) as lr_4_wiecej, ''," +
 
                 "sum(case when m_wr LIKE '%a%' then 1 else 0 end) as wr_a, ''," +
                 "sum(case when m_wr LIKE '%b%' then 1 else 0 end) as wr_b, ''," +
                 "sum(case when m_wr LIKE '%c%' then 1 else 0 end) as wr_c, ''," +
 
                 "sum(case when m_wm = 'c' then 1 else 0 end) as wmc,''," +
                 "sum(case when m_wm = 'd' then 1 else 0 end) as wmd,''," +
                 "sum(case when m_wm = 'e' then 1 else 0 end) as wme,''," +
                 "sum(case when m_wm = 'f' then 1 else 0 end) as wmf,''," +
                 "sum(case when m_wm = 'g' then 1 else 0 end) as wmg,''," +
                 "sum(case when m_wm not in ('a','b','c','d','e', 'f', 'g') then 1 else 0 end) as wmnic,''," +
 
                 "sum(case when m_wo = 'c' then 1 else 0 end) as woc,''," +
                 "sum(case when m_wo = 'd' then 1 else 0 end) as wod,''," +
                 "sum(case when m_wo = 'e' then 1 else 0 end) as woe,''," +
                 "sum(case when m_wo = 'f' then 1 else 0 end) as wof,''," +
                 "sum(case when m_wo = 'g' then 1 else 0 end) as wog,''," +
 
 
                 "sum(case when m_wykm = 'a' then 1 else 0 end) as wykma,''," +
                 "sum(case when m_wykm = 'b' then 1 else 0 end) as wykmb,''," +
                 "sum(case when m_wykm = 'c' then 1 else 0 end) as wykmc,''," +
                 "sum(case when m_wykm = 'd' then 1 else 0 end) as wykmd,''," +
                 "sum(case when m_wykm not in ('a','b','c','d') then 1 else 0 end) as wykmnic,''," +
 
                 "sum(case when m_wyko = 'a' then 1 else 0 end) as wykoa,''," +
                 "sum(case when m_wyko = 'b' then 1 else 0 end) as wykob,''," +
                 "sum(case when m_wyko = 'c' then 1 else 0 end) as wykoc,''," +
                 "sum(case when m_wyko = 'd' then 1 else 0 end) as wykod, ''" +
 
                 " from ankieta_pat where p_" + question + " LIKE '%" + letter + "%'";
     }
 
 
     public String makeWholeUnionCall(String question) {
         List<String> letters = new ArrayList<String>();
         letters.add("a");
         letters.add("b");
         letters.add("c");
         letters.add("d");
         letters.add("e");
         letters.add("f");
 
         StringBuilder wholeSelect = new StringBuilder();
         wholeSelect.append("COPY (");
         for (String letter : letters) {
             wholeSelect.append(makeSelectForUnion(question, letter));
             wholeSelect.append(" UNION ");
         }
         wholeSelect.delete(wholeSelect.length() - 6, wholeSelect.length());
        wholeSelect.append(") TO '/tmp/p_"+question+".csv' WITH CSV;");
         return wholeSelect.toString();
     }
 
     public void callUnionSelectP(String question) throws SQLException {
         makeWholeUnionCall(question);
         CallableStatement statement = sqlConn.prepareCall(makeWholeUnionCall(question));
         statement.execute();
         sqlConn.commit();
     }
 
     public void callSelectPWykOA(String question) throws SQLException {
                CallableStatement statement = sqlConn.prepareCall("COPY ( select p_" + question + "," +
                "sum(case when m_wyko = 'a' then 1 else 0 end) as wykoa,''," +
                 "    from ankieta_pat group by p_" + question + " order by p_" + question + " " +
 
                 ") TO '/tmp/p_a" + question + ".csv' WITH CSV;");
         statement.execute();
         sqlConn.commit();
     }
 }
