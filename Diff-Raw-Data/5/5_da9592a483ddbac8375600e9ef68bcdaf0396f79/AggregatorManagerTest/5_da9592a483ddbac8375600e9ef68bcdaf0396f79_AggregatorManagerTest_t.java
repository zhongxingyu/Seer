 package ch.uzh.ddis.katts.bolts.aggregate;
 
 import static ch.uzh.ddis.katts.util.MockDataUtils.ISO_FORMAT;
 import static ch.uzh.ddis.katts.util.MockDataUtils.parseString;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.DatatypeFactory;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.uzh.ddis.katts.bolts.aggregate.AggregatorManager.Callback;
 import ch.uzh.ddis.katts.query.processor.aggregate.AggregatorConfiguration;
 import ch.uzh.ddis.katts.query.processor.aggregate.MinAggregatorConfiguration;
 import ch.uzh.ddis.katts.query.processor.aggregate.SumAggregatorConfiguration;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Table;
 
 public class AggregatorManagerTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testSimpleSum() throws Exception {
 		AggregatorManager manager;
 		AggregatorManager.Callback callback;
 		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
 		SumAggregatorConfiguration sumConfig;
 		List<AggregatorConfiguration<?>> aggregatorConfigList;
 		ImmutableList<String> groupByKey;
 		final Table<ImmutableList<Object>, String, Object> resultTable = HashBasedTable.create();
 
 		sumConfig = new SumAggregatorConfiguration();
 		sumConfig.setOf("price");
 		sumConfig.setAs("total_price");
 		aggregatorConfigList = new ArrayList<AggregatorConfiguration<?>>();
 		aggregatorConfigList.add(sumConfig);
 
 		callback = new Callback() {
 
 			@Override
 			public void callback(Table<ImmutableList<Object>, String, Object> aggregateValues, Date startDate,
 					Date endDate) {
 				synchronized (resultTable) {
 					resultTable.putAll(aggregateValues);
 				}
 			}
 		};
 
 		manager = new AggregatorManager(datatypeFactory.newDuration("PT1S"), // window size
 				datatypeFactory.newDuration("PT1S"), // update interval
 				callback, // this method will be called when there are
 				true, // onlyIfChanged
 				sumConfig // aggregator list
 		);
 
 		groupByKey = ImmutableList.of("HHH", "sales");
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:01,ticker=HHH,department=sales,price=3.0D"));
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:02,ticker=HHH,department=sales,price=4.0D"));
 		// We should have gotten the sum for HHH-sales during the first second back (written in the result table)
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 		}
 	}
 
 	@Test
 	public void testOnlyUpdateOnChangeTrue() throws Exception {
 		AggregatorManager manager;
 		AggregatorManager.Callback callback;
 		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
 		SumAggregatorConfiguration sumConfig;
 		List<AggregatorConfiguration<?>> aggregatorConfigList;
 		final ImmutableList<String> groupByKey;
 		final Table<ImmutableList<Object>, String, Object> resultTable = HashBasedTable.create();
 		final Date[] lastUpdateDates = new Date[2]; // start- and end date
 		Date lastUpdate;
 
 		sumConfig = new SumAggregatorConfiguration();
 		sumConfig.setOf("price");
 		sumConfig.setAs("total_price");
 		aggregatorConfigList = new ArrayList<AggregatorConfiguration<?>>();
 		aggregatorConfigList.add(sumConfig);
 
 		groupByKey = ImmutableList.of("HHH", "sales");
 
 		callback = new Callback() {
 
 			@Override
 			public void callback(Table<ImmutableList<Object>, String, Object> aggregateValues, Date startDate,
 					Date endDate) {
 				synchronized (resultTable) {
 					if (aggregateValues.contains(groupByKey, "total_price")) {
 						resultTable.putAll(aggregateValues);
 						lastUpdateDates[0] = startDate;
 						lastUpdateDates[1] = endDate;
 					}
 				}
 			}
 		};
 
 		manager = new AggregatorManager(datatypeFactory.newDuration("PT1S"), // window size
 				datatypeFactory.newDuration("PT1S"), // update interval
 				callback, // this method will be called when there are
 				true, // onlyIfChanged
 				sumConfig // aggregator list
 		);
 
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:01,ticker=HHH,department=sales,price=3.0D"));
 		manager.advanceInTime(ISO_FORMAT.parseMillis("2001-01-01T00:00:02"));
 		// We should have gotten the sum for HHH-sales during the first second back (written in the result table)
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 			lastUpdate = lastUpdateDates[1];
 		}
 		// Add 3.0 to the sum. since we're only looking at a 1 second window, this did not change the value
 		// of the sum (it is still 3), so this should not trigger an update -> the lastUpdate should not have changed.
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:02,ticker=HHH,department=sales,price=3.0D"));
 		manager.advanceInTime(ISO_FORMAT.parseMillis("2001-01-01T00:00:03"));
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 			Assert.assertEquals(lastUpdate, lastUpdateDates[1]);
 		}
 	}
 
 	@Test
 	public void testOnlyUpdateOnChangeFalse() throws Exception {
 		AggregatorManager manager;
 		AggregatorManager.Callback callback;
 		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
 		SumAggregatorConfiguration sumConfig;
 		List<AggregatorConfiguration<?>> aggregatorConfigList;
 		final ImmutableList<String> groupByKey;
 		final Table<ImmutableList<Object>, String, Object> resultTable = HashBasedTable.create();
 		final Date[] lastUpdateDates = new Date[2]; // start- and end date
 		Date lastUpdate;
 
 		sumConfig = new SumAggregatorConfiguration();
 		sumConfig.setOf("price");
 		sumConfig.setAs("total_price");
 		aggregatorConfigList = new ArrayList<AggregatorConfiguration<?>>();
 		aggregatorConfigList.add(sumConfig);
 
 		groupByKey = ImmutableList.of("HHH", "sales");
 
 		callback = new Callback() {
 
 			@Override
 			public void callback(Table<ImmutableList<Object>, String, Object> aggregateValues, Date startDate,
 					Date endDate) {
 				synchronized (resultTable) {
 					if (aggregateValues.contains(groupByKey, "total_price")) {
 						resultTable.putAll(aggregateValues);
 						lastUpdateDates[0] = startDate;
 						lastUpdateDates[1] = endDate;
 					}
 				}
 			}
 		};
 
 		manager = new AggregatorManager(datatypeFactory.newDuration("PT1S"), // window size
 				datatypeFactory.newDuration("PT1S"), // update interval
 				callback, // this method will be called when there are
 				false, // onlyIfChanged
 				sumConfig // aggregator list
 		);
 
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:01,ticker=HHH,department=sales,price=3.0D"));
 		manager.advanceInTime(ISO_FORMAT.parseMillis("2001-01-01T00:00:02"));
 		// We should have gotten the sum for HHH-sales during the first second back (written in the result table)
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 			lastUpdate = lastUpdateDates[1];
 		}
 		// Add 3.0 to the sum. since we're only looking at a 1 second window, this did not change the value
 		// of the sum (it is still 3), so this should not trigger an update -> the lastUpdate should not have changed.
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:03,ticker=HHH,department=sales,price=3.0D"));
 		manager.advanceInTime(ISO_FORMAT.parseMillis("2001-01-01T00:00:04"));
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 			Assert.assertFalse(lastUpdate.equals(lastUpdateDates[1]));
 		}
 	}
 
 	@Test
 	public void testNoWindow() throws Exception {
 		AggregatorManager manager;
 		AggregatorManager.Callback callback;
 		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
 		SumAggregatorConfiguration sumConfig;
 		List<AggregatorConfiguration<?>> aggregatorConfigList;
 		final ImmutableList<String> groupByKey;
 		final Table<ImmutableList<Object>, String, Object> resultTable = HashBasedTable.create();
 		final Date[] lastUpdateDates = new Date[2]; // start- and end date
 		Date lastUpdate;
 		
 		sumConfig = new SumAggregatorConfiguration();
 		sumConfig.setOf("price");
 		sumConfig.setAs("total_price");
 		aggregatorConfigList = new ArrayList<AggregatorConfiguration<?>>();
 		aggregatorConfigList.add(sumConfig);
 
 		groupByKey = ImmutableList.of("HHH", "sales");
 
 		callback = new Callback() {
 
 			@Override
 			public void callback(Table<ImmutableList<Object>, String, Object> aggregateValues, Date startDate,
 					Date endDate) {
 				synchronized (resultTable) {
 					if (aggregateValues.contains(groupByKey, "total_price")) {
 						resultTable.putAll(aggregateValues);
 						lastUpdateDates[0] = startDate;
 						lastUpdateDates[1] = endDate;
 					}
 				}
 			}
 		};
 
 		manager = new AggregatorManager(null, // window size
 				datatypeFactory.newDuration("PT1S"), // update interval
 				callback, // this method will be called when there are
 				true, // onlyIfChanged
 				sumConfig // aggregator list
 		);
 
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:01,ticker=HHH,department=sales,price=3.0D"));
 		manager.advanceInTime(ISO_FORMAT.parseMillis("2001-01-01T00:00:02")); // simulate heartbeat
 		// We should have gotten the sum for HHH-sales during the first second back (written in the result table)
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 			lastUpdate = lastUpdateDates[1];
 		}
 		// Add 3.0 to the sum
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:02,ticker=HHH,department=sales,price=3.0D"));
 		manager.advanceInTime(ISO_FORMAT.parseMillis("2001-01-01T00:00:03")); // simulate heartbeat
 		// since we don't have a window, the sum should now be 6
 		synchronized (resultTable) {
 			Assert.assertEquals(6.0D, ((Double) resultTable.get(groupByKey, "total_price")).doubleValue(), 0.01D);
 		}
 	}
 	
 	@Test
 	public void testMin() throws Exception {
 		AggregatorManager manager;
 		AggregatorManager.Callback callback;
 		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
 		MinAggregatorConfiguration minConfig;
 		List<AggregatorConfiguration<?>> aggregatorConfigList;
 		ImmutableList<String> groupByKey;
 		final Table<ImmutableList<Object>, String, Object> resultTable = HashBasedTable.create();
 
 		minConfig = new MinAggregatorConfiguration();
 		minConfig.setOf("price");
 		minConfig.setAs("min_price");
 		aggregatorConfigList = new ArrayList<AggregatorConfiguration<?>>();
 		aggregatorConfigList.add(minConfig);
 
 		callback = new Callback() {
 
 			@Override
 			public void callback(Table<ImmutableList<Object>, String, Object> aggregateValues, Date startDate,
 					Date endDate) {
 				synchronized (resultTable) {
 					resultTable.putAll(aggregateValues);
 				}
 			}
 		};
 
		manager = new AggregatorManager(datatypeFactory.newDuration("PT2S"), // window size
 				datatypeFactory.newDuration("PT1S"), // update interval
 				callback, // this method will be called when there are
 				true, // onlyIfChanged
 				minConfig // aggregator list
 		);
 
 		groupByKey = ImmutableList.of("HHH", "sales");
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:01,ticker=HHH,department=sales,price=3.0D"));
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:02,ticker=HHH,department=sales,price=1.0D"));
 		// We should have gotten the min for HHH-sales during the first second back (written in the result table)
 		synchronized (resultTable) {
 			Assert.assertEquals(3.0D, ((Double) resultTable.get(groupByKey, "min_price")).doubleValue(), 0.01D);
 		}
		// TODO this test is somehow croocked..
 		// move one second more and now 1.0D should be the new minimum value
 		manager.incorporateValue(groupByKey,
 				parseString("startDate=2001-01-01T00:00:03,ticker=HHH,department=sales,price=2.0D"));
 		synchronized (resultTable) {
 			Assert.assertEquals(1.0D, ((Double) resultTable.get(groupByKey, "min_price")).doubleValue(), 0.01D);
 		}
 	}
 
 }
