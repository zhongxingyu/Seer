 package it.joshua.crobots.impl;
 
 import it.joshua.crobots.bean.GamesBean;
 import it.joshua.crobots.bean.RobotGameBean;
 import it.joshua.crobots.data.TableName;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import oracle.jdbc.internal.OracleTypes;
 import org.apache.log4j.Logger;
 
 public class SQLManagerOracle extends SQLManager {
 
 	private static Logger logger = Logger.getLogger(SQLManagerOracle.class);
 	private TableName tableName;
 
 	private SQLManagerOracle(TableName tableName) {
 		super(tableName);
 		this.tableName      = tableName;
 	}
 
 	@Override
 	public List<GamesBean> getGames()
 	{
 		Connection         c   = null;
 		CallableStatement cs   = null;
 		List<GamesBean> result = new ArrayList<>();
 		String sql  = "{call pSelect" + tableName + "(?,?)}";
 		ResultSet rs           = null;
 		GamesBean game         = null;
 		try
 		{
 			c  = getConnection(sharedVariables.isLocalDb());
 			cs = c.prepareCall(sql);
 			cs.setInt(1, sharedVariables.getBufferMinSize());
 			cs.registerOutParameter(2, OracleTypes.CURSOR);
 			cs.execute();
 			rs = (ResultSet)cs.getObject(2);
 			while(rs.next())
 			{
 				game = new GamesBean.Builder(rs.getInt(1), tableName, sharedVariables.getNumOfMatch(tableName), "match").build();
 				int n = tableName.getNumOfOpponents()+1;
 				for(int i=2;i<=n;i++)
 				{
 					game.getRobots().add(RobotGameBean.create(rs.getString(i)));
 				}
 				result.add(game);
 			}
 			if (result.isEmpty())
 			{
 				logger.info(tableName + " table empty ...");
 			}
 		}
 		catch(Exception e)
 		{
 			logger.error("", e);
 		}
 		finally
 		{
 			close(rs);
 			close(cs);
 			close(c);
 		}
 		return result;		
 	}
 }
