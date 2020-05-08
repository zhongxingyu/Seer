 package com.joyveb.test.cassandra.use;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
 import me.prettyprint.cassandra.service.CassandraHostConfigurator;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.HConsistencyLevel;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.factory.HFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.joyveb.support.cassandra.CassandraList;
 import com.joyveb.support.cassandra.Example;
 import com.joyveb.test.cassandra.dao.SimGameInfoDAO;
 import com.joyveb.test.cassandra.dao.impl.SimGameInfoDAOImpl;
 import com.joyveb.test.cassandra.domain.SimGameInfo;
 
 /**
  * cassandra 使用 Example FindByPage使用说明：
  * <p>
  * 1、DAO接口类继承BaseCassandraDao类，参考BetDAO
  * <p>
  * 2、具体使用参考本类line53-65
  * <p>
  * 限制：此接口只能实现多个and关联查询，不支持or查询
  * 
  * 
  */
 public class FindByPagesExam {
 
 	final static String hosts = "192.168.3.141:9160";
 
 	final static Cluster cluster = HFactory.getOrCreateCluster("Test Cluster",
 			new CassandraHostConfigurator(hosts));
 
 	private static Keyspace keyspace = null;
 	private static String columnFamily = "T_SIM_GAMEINFO";
 	final static String kp_name = "gxsim";
 	private SimGameInfoDAO simGameInfoDAO;
 
 	@Before
 	public void init() {
 		simGameInfoDAO = new SimGameInfoDAOImpl(keyspace, columnFamily);
 	}
 
 	@Test
 	public void findbypage() {
 		int pagesize = 8;
 		Example<String> example = new Example<String>();
 		example.addEqExpress("GAMENAME", "PCK3").addEqExpress("LTYPE", "PCK3")
 				.addEqExpress("PLAYNAME", "PCK3")
 				.addEqExpress("PRIZEDATE", "2013-06-06");
 		CassandraList<String, SimGameInfo> cassandraList = simGameInfoDAO
 				.findByPages(example, pagesize, "224505", null);
 		for (SimGameInfo info : cassandraList.getResultList()) {
 			System.out.println(info);
 		}
 		while (cassandraList.getStartKey() != null) {
 			cassandraList = simGameInfoDAO.findByPages(example, pagesize,
 					cassandraList.getStartKey(), null);
 			for (SimGameInfo info : cassandraList.getResultList()) {
 				System.out.println(info);
 			}
			System.out.println("------------------"
					+ cassandraList.getResultList().size()
					+ "--------------------------");
 		}
 		System.out.println(cassandraList.getResultList().size());
 	}
 
 	public static void main(String[] args) {
 		FindByPagesExam useBet = new FindByPagesExam();
 
 		ConfigurableConsistencyLevel cl = new ConfigurableConsistencyLevel();
 		Map<String, HConsistencyLevel> clmap = new HashMap<String, HConsistencyLevel>();
 		clmap.put(columnFamily, HConsistencyLevel.ONE);
 		cl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
 		cl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
 		cl.setReadCfConsistencyLevels(clmap);
 		cl.setWriteCfConsistencyLevels(clmap);
 
 		keyspace = HFactory.createKeyspace(kp_name, cluster, cl);
 
 		useBet.init();
 		useBet.findbypage();
 	}
 }
