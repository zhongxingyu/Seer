 package us.jubat.regression;
 
 import static org.hamcrest.Matchers.closeTo;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import us.jubat.testutil.JubaServer;
 import us.jubat.testutil.JubatusClientTest;
 
 public class RegressionClientTest extends JubatusClientTest {
 	private RegressionClient client;
 
 	public RegressionClientTest() {
 		super(JubaServer.regression);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		server.start(server.getConfigPath());
 		client = new RegressionClient(server.getHost(), server.getPort(),
 				TIMEOUT_SEC);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		server.stop();
 	}
 
 	@Test
	public void testGet_config() {
 		String config = client.get_config(NAME);
 		assertThat(formatAsJson(config),
 				is(formatAsJson(server.getConfigData())));
 	}
 
 	@Test
 	public void testTrain_and_Estimate() {
 		Datum datum = new Datum();
 
 		List<TupleStringString> string_values = new ArrayList<TupleStringString>();
 		for (int i = 1; i <= 10; i++) {
 			TupleStringString string_value = new TupleStringString();
 			string_value.first = "key/str" + Integer.toString(i);
 			string_value.second = "val/str" + Integer.toString(i);
 			string_values.add(string_value);
 		}
 		datum.string_values = string_values;
 
 		List<TupleStringDouble> num_values = new ArrayList<TupleStringDouble>();
 		for (int i = 1; i <= 10; i++) {
 			TupleStringDouble num_value = new TupleStringDouble();
 			num_value.first = "key/num" + Integer.toString(i);
 			num_value.second = i;
 			num_values.add(num_value);
 		}
 		datum.num_values = num_values;
 
 		TupleFloatDatum train_datum = new TupleFloatDatum();
 		train_datum.first = 1f;
 		train_datum.second = datum;
 
 		List<TupleFloatDatum> train_data = new ArrayList<TupleFloatDatum>();
 		train_data.add(train_datum);
 
 		for (int i = 1; i <= 100; i++) {
 			assertThat(client.train(NAME, train_data), is(1));
 		}
 
 		List<Datum> estimate_data = new ArrayList<Datum>();
 		estimate_data.add(datum);
 		List<Float> result = client.estimate(NAME, estimate_data);
 
 		assertThat(result, is(notNullValue()));
 		assertThat(result.size(), is(1));
 		assertThat(result.get(0).doubleValue(), is(closeTo(1.0, 0.00001)));
 	}
 
 	@Test
 	public void testSave_and_Load() {
 		String id = "regression.test_java-client.model";
 		assertThat(client.save(NAME, id), is(true));
 		assertThat(client.load(NAME, id), is(true));
 	}
 
 	@Test
 	public void testGet_status() {
 		Map<String, Map<String, String>> status = client.get_status(NAME);
 		assertThat(status, is(notNullValue()));
 		assertThat(status.size(), is(1));
 	}
 }
