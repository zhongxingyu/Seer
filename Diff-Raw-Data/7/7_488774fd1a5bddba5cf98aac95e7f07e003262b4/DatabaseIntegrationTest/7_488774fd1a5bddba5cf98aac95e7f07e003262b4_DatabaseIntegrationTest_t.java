 package nz.ac.vuw.ecs.rprofs.server.db;
 
 import com.google.common.collect.Lists;
 import com.mongodb.Mongo;
 import nz.ac.vuw.ecs.rprofs.server.context.Context;
 import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
 import nz.ac.vuw.ecs.rprofs.server.domain.*;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
 import org.junit.*;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 import static org.junit.Assert.*;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 9/09/11
  */
 public class DatabaseIntegrationTest {
 
 	private static final org.slf4j.Logger log
 			= LoggerFactory.getLogger(DatabaseIntegrationTest.class);
 
 	private static Process process;
 	private static Thread sysout;
 	private static Thread syserr;
 
 	@BeforeClass
 	public static void createDatabase() throws Exception {
 		ProcessBuilder pb = new ProcessBuilder();
 		pb.environment().put("PATH", "/bin:/usr/bin:/usr/local/bin:~/bin");
 		pb.command("/usr/bin/which", "mongod");
 		Scanner in = new Scanner(pb.start().getInputStream());
 		Assert.assertTrue(in.hasNextLine());
 		String path = in.nextLine();
 
 		pb.command().clear();
 		pb.command("/bin/mkdir", "-p", "/tmp/rprof/mongo");
 		pb.start();
 
 		pb.command().clear();
 		pb.command(path, "--dbpath", "/tmp/rprof/mongo",
 				"--bind_ip", "127.0.0.1", "--port", "27018", "--logpath", "/tmp/rprof/mongod.log");
 		process = pb.start();
 
 		sysout = new Thread() {
 			public void run() {
 				for (Scanner sc = new Scanner(process.getInputStream()); sc.hasNextLine(); ) {
 					log.info(sc.nextLine());
 				}
 			}
 		};
 		syserr = new Thread() {
 			public void run() {
 				for (Scanner sc = new Scanner(process.getErrorStream()); sc.hasNextLine(); ) {
 					log.error(sc.nextLine());
 				}
 			}
 		};
 
 		sysout.start();
 		syserr.start();
 
 		Thread.sleep(2000);
 	}
 
 	private Database database;
 	private Mongo mongo;
 
 	@Before
 	public void createMongo() throws Exception {
 		mongo = new Mongo("127.0.0.1", 27018);
 		database = new Database(mongo);
 		database.context = new Context(database);
 	}
 
 	@Test
 	public void testCreateDataset() throws Exception {
 		List<String> dbs = mongo.getDatabaseNames();
 		dbs.remove("admin");
 		dbs.remove("local");
 		dbs.remove("test");
 		assertEquals(Lists.<String>newArrayList(), dbs);
 
 		DatasetId id = database.getDatasetCreator()
 				.setHandle("foobar")
 				.setStarted(new Date())
 				.store();
 		Dataset dataset = database.findEntity(id);
 
 		assertNotNull(dataset);
 		assertNotNull(id);
 		assertEquals(id, dataset.getId());
 		assertTrue(id.getDatasetIndex() > 0);
 		assertNotNull(dataset.getHandle());
 		assertFalse(dataset.getHandle().isEmpty());
 		assertNotNull(dataset.getId());
 		assertEquals(dataset.getId().getDatasetIndex(), dataset.getId().getDatasetIndex());
 
 		dbs = mongo.getDatabaseNames();
 		dbs.remove("admin");
 		dbs.remove("local");
 		dbs.remove("test");
 		assertEquals(1, dbs.size());
 
 		String dbname = dbs.get(0);
 		assertEquals(dbname, database.getDBName(dataset));
 	}
 
 	@Test
 	public void testGetName() throws Exception {
 		assertEquals("rprof_foo_0", database.getDBName(new Dataset(new DatasetId((short) 0), "foo", new Date())));
 		assertEquals("rprof_20110101_1", database.getDBName(new Dataset(new DatasetId((short) 1), "20110101", new Date())));
 	}
 
 	@Test
 	public void testGetDatasets() throws Exception {
 		assertEquals(new ArrayList<Dataset>(), database.getDatasetQuery().find());
 		DatasetId id1 = database.getDatasetCreator().setHandle("foo").setStarted(new Date()).store();
 		assertEquals(id1, database.getDatasetQuery().find().get(0).getId());
 		DatasetId id2 = database.getDatasetCreator().setHandle("foo").setStarted(new Date()).store();
 
 		List<? extends Dataset> result = database.getDatasetQuery().find();
 		Collections.sort(result);
 		assertEquals(id1, result.get(0).getId());
 		assertEquals(id2, result.get(1).getId());
 	}
 
 	@Test
 	public void testGetDatasetDatasetId() throws Exception {
 		DatasetId in = database.getDatasetCreator().setHandle("foobar").setStarted(new Date()).store();
 		assertEquals("foobar", database.findEntity(in).getHandle());
 
 		assertNull(database.findEntity(new DatasetId((short) 0)));
 	}
 
 	@Test
 	public void testDropDataset() throws Exception {
 		DatasetId in = database.getDatasetCreator().setHandle("foobar").setStarted(new Date()).store();
 		List<? extends Dataset> datasets = database.getDatasetQuery().find();
 		assertEquals(1, datasets.size());
 		assertEquals(in, datasets.get(0).getId());
 		database.deleteEntity(database.findEntity(in));
 		assertEquals(new ArrayList<Dataset>(), database.getDatasetQuery().find());
 
 		assertNull(database.findEntity(in));
 	}
 
 	@Test
 	public void testDatasets() throws Exception {
 		Date now = new Date();
 		String handle = "foobar";
 		String program = "baz";
 		DatasetId id = database.getDatasetCreator()
 				.setHandle(handle)
 				.setStarted(now)
 				.store();
 
 		Dataset ds = database.findEntity(id);
 
 		assertEquals(handle, ds.getHandle());
 		assertEquals(now, ds.getStarted());
		assertEquals(1, ds.getVersion().intValue());
 
 		database.getDatasetUpdater()
 				.setProgram(program)
 				.setStopped(now)
 				.update(id);
 
 		ds = database.findEntity(id);
 
 		assertEquals(handle, ds.getHandle());
 		assertEquals(now, ds.getStarted());
 		assertEquals(now, ds.getStopped());
 		assertEquals(program, ds.getProgram());
 
 		assertEquals(1,
 				database.getDatasetQuery().setProgram("baz").count());
 		assertEquals(Lists.newArrayList(ds),
 				database.getDatasetQuery().setProgram("baz").find());
 	}
 
 	@Test
 	public void testClasses() throws Exception {
 		DatasetId id = database.getDatasetCreator()
 				.setStarted(new Date())
 				.setHandle("foobar")
 				.store();
 		Dataset ds = database.findEntity(id);
 		database.context.setDataset(ds);
 
 		ClazzCreator<?> c = database.getClazzCreator()
 				.setName("org/foo/Bar")
 				.setParentName("org/Foo");
 
 		c.addField().setName("a").setDescription("I").setAccess(1).store();
 		c.addMethod().setName("b").setDescription("()V").setAccess(4).store();
 
 		ClazzId barId = c.store();
 
 		Clazz bar = database.findEntity(barId);
 		assertEquals("org/foo/Bar", bar.getName());
 		assertEquals("org/Foo", bar.getParentName());
 		assertEquals(null, bar.getParent());
 		assertEquals(0, bar.getProperties());
 
 		ClazzId fooId = database.getClazzCreator().setName("org/Foo").store();
 
 		bar = database.getClazzQuery().setPackageName("org.foo").find().get(0);
 		assertEquals(barId, bar.getId());
 		assertEquals(fooId, bar.getParent());
 
 		database.getClazzUpdater().setProperties(15).update(barId);
 
 		bar = database.findEntity(barId);
 
 		assertEquals(15, bar.getProperties());
 
 		assertEquals(2, database.getClazzQuery().count());
 
 		Field f = database.getFieldQuery().find().get(0);
 		assertEquals("a", f.getName());
 		assertEquals("I", f.getDescription());
 		assertEquals(1, f.getAccess());
 		assertEquals(barId, f.getOwner());
 		assertEquals("org/foo/Bar", f.getOwnerName());
 		assertEquals(f.getId(), database.findEntity(f.getId()).getId());
 
 		Method m = database.getMethodQuery().find().get(0);
 		assertEquals("b", m.getName());
 		assertEquals("()V", m.getDescription());
 		assertEquals(4, m.getAccess());
 		assertEquals(barId, m.getOwner());
 		assertEquals("org/foo/Bar", m.getOwnerName());
 		assertEquals(m.getId(), database.findEntity(m.getId()).getId());
 
 
 		assertEquals(2, database.countPackages());
 		List<String> packages = database.findPackages();
 		Collections.sort(packages);
 		assertEquals(Lists.newArrayList("org", "org.foo"), packages);
 
 		database.deleteEntity(bar);
 
 		assertEquals(1, database.countPackages());
 
 		database.context.clear();
 	}
 
 	@Test
 	public void testEvents() throws Exception {
 		DatasetId id = database.getDatasetCreator()
 				.setStarted(new Date())
 				.setHandle("foobar")
 				.store();
 		Dataset ds = database.findEntity(id);
 		database.context.setDataset(ds);
 
 		EventId eventId = new EventId(1);
 		ClazzId clazzId = new ClazzId(2);
 		FieldId fieldId = new FieldId(3);
 		InstanceId instance = new InstanceId(4);
 		EventId eid = database.getEventCreater()
 				.setId(eventId)
 				.setThread(null)
 				.setEvent(Event.FIELD_READ)
 				.setClazz(clazzId)
 				.setField(fieldId)
 				.addArg(instance)
 				.addArg(null)
 				.store();
 
 		assertEquals(eventId, eid);
 
 		Event e = database.getEventQuery().setEvent(Event.FIELD_READ).find().get(0);
 
 		assertEquals(clazzId, e.getClazz());
 		assertEquals(fieldId, e.getField());
 		assertEquals(eventId, e.getId());
 		assertEquals(Event.FIELD_READ, e.getEvent());
 
 		// todo second arg should be null but gwt has a bug
 		assertEquals(Lists.newArrayList(instance, new InstanceId(0)), e.getArgs());
 
 		database.context.clear();
 	}
 
 	@After
 	public void destroyMongo() throws Exception {
 		for (String db : mongo.getDatabaseNames()) {
 			if (db.equals("test")) continue;
 			if (db.equals("admin")) continue;
 			if (db.equals("local")) continue;
 			mongo.dropDatabase(db);
 		}
 		mongo.close();
 	}
 
 	@AfterClass
 	public static void destroyDatabase() throws Exception {
 		process.destroy();
 		sysout.join();
 		syserr.join();
 
 		ProcessBuilder pb = new ProcessBuilder();
 		pb.command("/bin/rm", "-rf", "/tmp/rprof");
 		pb.start();
 	}
 }
