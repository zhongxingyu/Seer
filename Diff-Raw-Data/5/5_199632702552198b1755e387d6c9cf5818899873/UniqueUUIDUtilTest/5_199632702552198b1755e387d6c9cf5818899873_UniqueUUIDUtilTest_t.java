 package edu.teco.dnd.util.tests;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.when;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
import org.junit.rules.TestRule;
 import org.junit.rules.Timeout;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import edu.teco.dnd.util.UUIDFactory;
 import edu.teco.dnd.util.UniqueUUIDUtil;
 
 @RunWith(MockitoJUnitRunner.class)
 public class UniqueUUIDUtilTest {
 	private final UUID uuid0 = UUID.fromString("00000000-0000-0000-0000-000000000000");
 	private final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
 	private final UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
 	private final UUID uuid3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
 	private final UUID extraUUID = UUID.fromString("00000000-0000-0000-0000-000000000042");
 
 	@Rule
	public TestRule globalTimeout = new Timeout(1000);
 
 	@Mock
 	private UUIDFactory factory;
 	private UniqueUUIDUtil util;
 
 	@Before
 	public void setup() {
 		util = new UniqueUUIDUtil(factory);
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testFactoryNull() {
 		// The constructor may already throw NPE, this is intended
 		final UniqueUUIDUtil nullUtil = new UniqueUUIDUtil(null);
 		nullUtil.getNewUUID();
 	}
 
 	@Test
 	public void testUUIDsNotNull() {
 		setFactoryToDefaultSequence();
 
 		assertNotNull(util.getNewUUID());
 		assertNotNull(util.getNewUUID());
 		assertNotNull(util.getNewUUID());
 		assertNotNull(util.getNewUUID());
 	}
 
 	@Test
 	public void testUUIDsDifferent() {
 		setFactoryToDefaultSequence();
 		final Set<UUID> uuids = new HashSet<UUID>();
 
 		for (int i = 0; i < 4; i++) {
 			final UUID newUUID = util.getNewUUID();
 			if (uuids.contains(newUUID)) {
 				fail(newUUID + " was already generated");
 			}
 		}
 	}
 
 	@Test
 	public void testHasGeneratedUUIDFresh() {
 		assertFalse(util.hasGeneratedUUID(uuid0));
 		assertFalse(util.hasGeneratedUUID(uuid1));
 		assertFalse(util.hasGeneratedUUID(uuid2));
 		assertFalse(util.hasGeneratedUUID(uuid3));
 		assertFalse(util.hasGeneratedUUID(extraUUID));
 	}
 
 	@Test
 	public void testHasGeneratedUUID() {
 		setFactoryToDefaultSequence();
 
 		final UUID firstGeneratedUUID = util.getNewUUID();
 		final UUID secondGeneratedUUID = util.getNewUUID();
 
 		assertTrue(util.hasGeneratedUUID(firstGeneratedUUID));
 		assertTrue(util.hasGeneratedUUID(secondGeneratedUUID));
 		assertFalse(util.hasGeneratedUUID(extraUUID));
 	}
 
 	private void setFactoryToDefaultSequence() {
 		when(factory.createUUID()).thenReturn(uuid0, uuid1, uuid2, uuid3);
 	}
 }
