 package us.jubat.stat;
 
 import static org.hamcrest.Matchers.closeTo;
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.msgpack.rpc.Client;
 
 import us.jubat.testutil.JubaServer;
 import us.jubat.testutil.JubatusClientTest;
 
 public class StatClientTest extends JubatusClientTest {
 	private StatClient client;
 
 	public StatClientTest() {
 		super(JubaServer.stat);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		server.start(server.getConfigPath());
 		client = new StatClient(server.getHost(), server.getPort(), TIMEOUT_SEC);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		server.stop();
 	}
 
 	@Test
 	public void testGet_config() throws IOException {
 		String config = client.get_config(NAME);
 		assertThat(formatAsJson(config),
 				is(formatAsJson(server.getConfigData())));
 	}
 
 	@Test
 	public void testSum() {
 		for (int i = 1; i <= 10; i++) {
 			String key = "key" + i;
 			for (int val = 1; val <= 50; val++) {
 				assertThat(client.push(NAME, key, val), is(true));
 			}
 			assertThat(client.sum(NAME, key), is(1275.0));
 		}
 	}
 
 	@Test
 	public void testStddev() {
 		for (int i = 1; i <= 10; i++) {
 			String key = "key" + i;
 			for (int val = 1; val <= 50; val++) {
 				client.push(NAME, key, val);
 			}
 			assertThat(client.stddev(NAME, key), is(closeTo(14.43087, 0.00001)));
 		}
 	}
 
 	@Test
 	public void testMax() {
 		for (int i = 1; i <= 10; i++) {
 			String key = "key" + i;
 			for (int val = 1; val <= 50; val++) {
 				client.push(NAME, key, val);
 			}
 			assertThat(client.max(NAME, key), is(50.0));
 		}
 	}
 
 	@Test
 	public void testMin() {
 		for (int i = 1; i <= 10; i++) {
 			String key = "key" + i;
 			for (int val = 1; val <= 50; val++) {
 				client.push(NAME, key, val);
 			}
 			assertThat(client.min(NAME, key), is(1.0));
 		}
 	}
 
 	@Test
 	public void testEntropy() {
 		for (int i = 1; i <= 10; i++) {
 			String key = "key" + i;
 			for (int val = 1; val <= 50; val++) {
 				client.push(NAME, key, val);
 			}
			double prob = 1.0 / i;
			double entropy = -1 * i * prob * Math.log(prob);
			assertThat(client.entropy(NAME, key), is(closeTo(entropy, 0.00001)));
 		}
 	}
 
 	@Test
 	public void testMoment() {
 		for (int i = 1; i <= 10; i++) {
 			String key = "key" + i;
 			for (int val = 1; val <= 50; val++) {
 				client.push(NAME, key, val);
 			}
 			assertThat(client.moment(NAME, key, 0, 0.0), is(1.0));
 			assertThat(client.moment(NAME, key, 1, 0.0), is(25.5));
 		}
 	}
 
 	@Test
 	public void testSave_and_Load() {
 		String id = "stat.test_java-client.model";
 		assertThat(client.save(NAME, id), is(true));
 		assertThat(client.load(NAME, id), is(true));
 	}
 
 	@Test
 	public void testGet_status() {
 		Map<String, Map<String, String>> status = client.get_status(NAME);
 		assertThat(status, is(notNullValue()));
 		assertThat(status.size(), is(1));
 	}
 
 	@Test
 	public void testClear() {
 		client.push(NAME, "key", 1);
 		assertThat(client.max(NAME, "key"), is(1.0));
 
 		client.clear(NAME);
 
 		client.push(NAME, "key", 1);
 		assertThat(client.max(NAME, "key"), is(not(2.0)));
 		assertThat(client.max(NAME, "key"), is(1.0));
 	}
 
 	@Test
 	public void testGet_client() {
 		assertThat(client.get_client(), is(instanceOf(Client.class)));
 		assertThat(client.get_client(), is(notNullValue()));
 	}
 }
