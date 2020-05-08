 package de.uzl.decentsparqle;
 
 import com.google.common.base.Stopwatch;
 import com.google.common.io.Files;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.inject.Guice;
 import de.uniluebeck.itm.tr.util.Logging;
 import lupos.datastructures.items.Triple;
 import lupos.datastructures.items.literal.string.StringLiteral;
 import org.apache.log4j.Level;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
 
 @Ignore
 @RunWith(Parameterized.class)
 public abstract class DataStoreLoadTest {
 
 	static {
 		Logging.setLoggingDefaults(Level.WARN);
 	}
 
 	private static final Logger log = LoggerFactory.getLogger(DataStoreLoadTest.class);
 
 	private int peerCount;
 
 	private int parallelPuts;
 
 	private int requestCount;
 
 	private DecentSparqle[] peers;
 
 	public DataStoreLoadTest(final int peerCount, final int parallelPuts, final int requestCount) {
 		this.peerCount = peerCount;
 		this.parallelPuts = parallelPuts;
 		this.requestCount = requestCount;
 	}
 
 	@Parameterized.Parameters
 	public static Collection<Object[]> data() {
 		return Arrays.asList(new Object[][]{
 
 				//{ 1, 1, 10000 },
 				//{ 1, 1, 20000 },
 				//{ 1, 1, 30000 },
 
 				//{ 3, 1, 10000 },
 				//{ 3, 1, 20000 },
 				//{ 3, 1, 30000 },
 
 				//{ 5, 1, 10000 },
 				//{ 5, 1, 20000 },
 				//{ 5, 1, 30000 },
 
 				//{ 5, 2, 10000 },
 				//{ 5, 2, 20000 },
 				//{ 5, 2, 30000 },
 
 				//{ 5, 3, 10000 },
 				//{ 5, 3, 20000 },
 				//{ 5, 3, 30000 },
 
 				//{ 5, 4, 10000 },
 				//{ 5, 4, 20000 },
 				//{ 5, 4, 30000 },
 
 				{5, 5, 10000},
 				{5, 5, 10000},
 				{5, 5, 20000},
 				{5, 5, 30000},
 
 				{5, 10, 10000},
 				{5, 10, 20000},
 				{5, 10, 30000},
 
 				{5, 20, 10000},
 				{5, 20, 20000},
 				{5, 20, 30000},
 
 				/*{ 10, 1, 10000 },
 				{ 10, 1, 20000 },
 				{ 10, 1, 30000 },
 
 				{ 10, 2, 10000 },
 				{ 10, 2, 20000 },
 				{ 10, 2, 30000 },
 
 				{ 10, 3, 10000 },
 				{ 10, 3, 20000 },
 				{ 10, 3, 30000 },
 
 				{ 10, 4, 10000 },
 				{ 10, 4, 20000 },
 				{ 10, 4, 30000 },
 
 				{ 10, 5, 10000 },
 				{ 10, 5, 20000 },
 				{ 10, 5, 30000 },
 
 				{ 10, 10, 10000 },
 				{ 10, 10, 20000 },
 				{ 10, 10, 30000 },*/
 		}
 		);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		peers = new DecentSparqle[peerCount];
 		for (int i = 0; i < peerCount; i++) {
 
 			final DecentSparqleConfig config = new DecentSparqleConfig(
 					4000 + i,
 					(i == 0 ? null : "localhost"),
 					(i == 0 ? null : 4000)
 			);
 
 			peers[i] = Guice.createInjector(createDecentSparqleModule(config)).getInstance(DecentSparqle.class);
 			peers[i].startAndWait();
 		}
 	}
 
 	protected abstract DecentSparqleModule createDecentSparqleModule(DecentSparqleConfig config);
 
 	@After
 	public void tearDown() throws Exception {
 		for (DecentSparqle peer : peers) {
 			peer.stopAndWait();
 		}
 		System.gc();
 	}
 
 	@Test
 	public void testHighLoadOnPut() throws Exception {
 
 		System.out.println(
 				">>>> testHighLoadOnPut(peerCount=" + peerCount + ", parallelPuts=" + parallelPuts + ", requestCount=" + requestCount + ") <<<<"
 		);
 
 		final Random random = new Random();
 		final Semaphore semaphore = new Semaphore(parallelPuts);
 
 		final long beforeAll = System.currentTimeMillis();
 		final Map<Integer, Long> executionTimes = Collections.synchronizedMap(new TreeMap<Integer, Long>());
 		final Stopwatch stopwatch = new Stopwatch();
 
 		stopwatch.start();
 
 		for (int i = 0; i < requestCount; i++) {
 
 			semaphore.acquire();
 
 			if (i % 100 == 0 && i != 0) {
 				log.info(stopwatch.elapsedMillis() + " ms");
 				stopwatch.reset();
 				stopwatch.start();
 			}
 
 			final int id = i;
 			final long before = System.currentTimeMillis();
 			Long data = random.nextLong();
 
 			//final ListenableFuture<Iterable<Triple>> future = peers[0].getDataStoreAdapter().get(Long.toString(data));
 			final ListenableFuture<Void> future = peers[0].getDataStoreAdapter().add(
 					Long.toString(data),
 					new Triple(
 							new StringLiteral(Long.toString(random.nextLong())),
 							new StringLiteral(Long.toString(random.nextLong())),
 							new StringLiteral(Long.toString(random.nextLong()))
 					)
 			);
 			future.addListener(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						log.debug("operationComplete({}, {})", id, future);
 						executionTimes.put(id, System.currentTimeMillis() - before);
 					} catch (NullPointerException e) {
 						log.error("", e);
 					} finally {
 						semaphore.release();
 					}
 				}
 			}, sameThreadExecutor());
 		}
 
 		semaphore.acquire(parallelPuts);
 
 		final long afterAll = System.currentTimeMillis();
 
 		final File csvFile = new File(peerCount + "peers_" + parallelPuts + "puts_" + requestCount + "requests.csv");
 		final File datFile = new File(peerCount + "peers_" + parallelPuts + "puts_" + requestCount + "requests.dat");
 
 		for (Map.Entry<Integer, Long> entry : executionTimes.entrySet()) {
			Files.append(entry.getKey() + "," + entry.getValue(), csvFile, Charset.defaultCharset());
			Files.append(entry.getKey() + " " + entry.getValue(), datFile, Charset.defaultCharset());
 		}
 
 		//Files.write(Joiner.on('\n').withKeyValueSeparator(",").join(executionTimes), csvFile, Charset.defaultCharset());
 		//Files.write(Joiner.on('\n').withKeyValueSeparator(" ").join(executionTimes), datFile, Charset.defaultCharset());
 
 		System.out.println(
 				"Duration (" + peerCount + " peers, " + parallelPuts + " parallel puts, " + requestCount + " requests): " + (afterAll - beforeAll)
 		);
 	}
 }
