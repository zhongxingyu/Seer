 /* This code is part of WoT, a plugin for Freenet. It is distributed 
  * under the GNU General Public License, version 2 (or at your option
  * any later version). See http://www.gnu.org/ for details of the GPL. */
 package plugins.WebOfTrust;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.UUID;
 
 import org.junit.Ignore;
 
 import plugins.WebOfTrust.exceptions.InvalidParameterException;
 import plugins.WebOfTrust.ui.fcp.FCPInterface;
 import freenet.node.FSParseException;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 public class SubscriptionManagerFCPTest extends DatabaseBasedTest {
 
 	/**
 	 * From the perspective of this unit test, this does NOT send replies and therefore is called "ReplyRECEIVER" instead of ReplySENDER.
 	 */
 	@Ignore
 	static class ReplyReceiver extends PluginReplySender {
 
 		LinkedList<SimpleFieldSet> results = new LinkedList<SimpleFieldSet>();
 		
 		public ReplyReceiver() {
 			super("SubscriptionManagerTest", "SubscriptionManagerTest");
 		}
 
 		/**
 		 * This is called by the FCP interface to deploy the reply to the sender of the original message.
 		 * So in our case this function actually means "receive()", not send.
 		 */
 		@Override
 		public void send(SimpleFieldSet params, Bucket bucket) throws PluginNotFoundException {
 			results.addLast(params);
 		}
 		
 		/**
 		 * @throws NoSuchElementException If no result is available
 		 */
 		public SimpleFieldSet getNextResult() {
 			return results.removeFirst();
 		}
 		
 		public boolean hasNextResult() {
 			return !results.isEmpty();
 		}
 		
 	}
 	
 	FCPInterface mFCPInterface;
 	ReplyReceiver mReplyReceiver;
 	
 	/**
 	 * Sends the given {@link SimpleFieldSet} to the FCP interface of {@link DatabaseBasedTest#mWoT}
 	 * You can obtain the result(s) by <code>mReplySender.getNextResult();</code>
 	 */
 	void fcpCall(final SimpleFieldSet params) {
 		mFCPInterface.handle(mReplyReceiver, params, null, 0);
 	}
 
 	/**
 	 * The set of all {@link Identity}s as we have received them from the {@link SubscriptionManager.IdentitiesSubscription}.
 	 * Is compared to the actual contents of the {@link WebOfTrust} database at the end of the test.
 	 * 
 	 * Key = {@link Identity#getID()} 
 	 */
 	HashMap<String, Identity> mReceivedIdentities;
 	
 	/**
 	 * The set of all {@link Trust}s as we have received them from the {@link SubscriptionManager.TrustsSubscription}.
 	 * Is compared to the actual contents of the {@link WebOfTrust} database at the end of the test.
 	 * 
 	 * Key = {@link Trust#getID()} 
 	 */
 	HashMap<String, Trust> mReceivedTrusts;
 	
 	/**
 	 * The set of all {@link Score}s as we have received them from the {@link SubscriptionManager.ScoresSubscription}.
 	 * Is compared to the actual contents of the {@link WebOfTrust} database at the end of the test.
 	 * 
 	 * Key = {@link Score#getID()} 
 	 */
 	HashMap<String, Score> mReceivedScores;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		mFCPInterface = mWoT.getFCPInterface();
 		mReplyReceiver = new ReplyReceiver();
 		
 		mReceivedIdentities = new HashMap<String, Identity>();
 		mReceivedTrusts = new HashMap<String, Trust>();
 		mReceivedScores = new HashMap<String, Score>();
 	}
 	
 	public void testSubscribe() throws FSParseException {
 		testSubscribeTo("Identities");
 		testSubscribeTo("Trusts");
 		testSubscribeTo("Scores");
 	}
 	
 	void testSubscribeTo(String type) throws FSParseException {
 		final SimpleFieldSet sfs = new SimpleFieldSet(true);
 		sfs.putOverwrite("Message", "Subscribe");
 		sfs.putOverwrite("To", type);
 		fcpCall(sfs);
 		
 		// First reply message is the full set of all objects of the type we are interested in so the client can synchronize its database
 		final SimpleFieldSet synchronization = mReplyReceiver.getNextResult();
 		assertEquals(type, synchronization.get("Message"));
 		assertEquals("0", synchronization.get("Amount")); // No identities/trusts/scores stored yet
 		
 		// Second reply message is the confirmation of the subscription
 		final SimpleFieldSet subscription = mReplyReceiver.getNextResult();
 		assertEquals("Subscribed", subscription.get("Message"));
 		try {
 			UUID.fromString(subscription.get("Subscription"));
 		} catch(IllegalArgumentException e) {
 			fail("Subscription ID is invalid!");
 			throw e;
 		}
 		
 		assertFalse(mReplyReceiver.hasNextResult());
 		
 		// Try to file the same subscription again - should fail because we already are subscribed
 		fcpCall(sfs);
 		final SimpleFieldSet duplicateSubscriptionMessage = mReplyReceiver.getNextResult();
 		assertEquals("Error", duplicateSubscriptionMessage.get("Message"));
 		assertEquals("Subscribe", duplicateSubscriptionMessage.get("OriginalMessage"));
 		assertEquals("plugins.WebOfTrust.SubscriptionManager$SubscriptionExistsAlreadyException", duplicateSubscriptionMessage.get("Description"));
 
 		assertFalse(mReplyReceiver.hasNextResult());
 	}
 	
 	
 	public void testAllRandomized() throws MalformedURLException, InvalidParameterException, FSParseException {
 		final int ownIdentityCount = 10;
 		final int identityCount = 100;
 		final int trustCount = (identityCount*identityCount) / 5; // A complete graph would be identityCount² trust values.
 
 		// Random trust graph setup...
 	
 		// FIXME: Make sure that this function also adds random contexts, trust values & publish trust list flags
 		// Also adapt addRandomTrustValues() to respect the publish trust list flag
 		final ArrayList<Identity> identities = addRandomIdentities(identityCount / 3);
 		
 		// At least one own identity needs to exist to ensure that scores are computed.
 		for(int i=0; i < Math.max(ownIdentityCount / 3, 1); ++i) {
 			final OwnIdentity ownIdentity = mWoT.createOwnIdentity(getRandomSSKPair()[0], getRandomLatinString(Identity.MAX_NICKNAME_LENGTH), true, "Test");
 			identities.add(ownIdentity); 
 		}
 		
 		// FIXME: Make sure that this function also adds random comments
 		addRandomTrustValues(identities, trustCount / 10);
 
 		/* Initial test data is set up */
 		subscribeAndSynchronize("Identities");
 		subscribeAndSynchronize("Trusts");
 		subscribeAndSynchronize("Scores");
 		
		assertEquals(new HashSet<Identity>(mWoT.getAllIdentities()), mReceivedIdentities);
		assertEquals(new HashSet<Trust>(mWoT.getAllTrusts()), mReceivedTrusts);
		assertEquals(new HashSet<Score>(mWoT.getAllScores()), mReceivedScores);
 		
 		/* FIXME: Actually test event notifications by doing random changes of the WOT now */
 	}
 	
 	void subscribeAndSynchronize(final String type) throws FSParseException, MalformedURLException, InvalidParameterException {
 		final SimpleFieldSet sfs = new SimpleFieldSet(true);
 		sfs.putOverwrite("Message", "Subscribe");
 		sfs.putOverwrite("To", type);
 		fcpCall(sfs);
 		
 		// First reply message is the full set of all objects of the type we are interested in so the client can synchronize its database
 		importSynchronization(type, mReplyReceiver.getNextResult());
 		
 		// Second reply message is the confirmation of the subscription
 		assertEquals("Subscribed", mReplyReceiver.getNextResult().get("Message"));
 		assertFalse(mReplyReceiver.hasNextResult());
 	}
 	
 	void importSynchronization(final String type, SimpleFieldSet synchronization) throws FSParseException, MalformedURLException, InvalidParameterException {
 		/**
 		 * It is necessary to have this class instead of the following hypothetical function:
 		 * <code>void putAllWithDupecheck(final List<Persistent> source, final HashMap<String, Persistent> target)</code>
 		 * 
 		 * We couldn't shove a HashMap<String, Identity> into target of the hypothetical function even though Identity is a subclass of Persistent:
 		 * The reason is that GenericClass<Type> is not a superclass of GenericClass<subtype of Object> in Java:
 		 * "Parameterized types are not covariant."
 		 */
 		@Ignore
 		class ReceivedObjectPutter<T extends Persistent> {
 			
 			void putAllWithDupecheck(final List<T> source, final HashMap<String, T> target) {
 				for(final T p : source) {
 					assertFalse(target.containsKey(p.getID()));
 					target.put(p.getID(), p);
 				}
 			}
 		
 		}
 		
 		assertEquals(type, synchronization.get("Message"));
 		if(type.equals("Identities")) {
 			new ReceivedObjectPutter<Identity>().putAllWithDupecheck(new IdentityParser().parseMultiple(synchronization), mReceivedIdentities);
 		} else if(type.equals("Trusts")) {
 			new ReceivedObjectPutter<Trust>().putAllWithDupecheck(new TrustParser().parseMultiple(synchronization), mReceivedTrusts);
 		} else if(type.equals("Scores")) {
 			new ReceivedObjectPutter<Score>().putAllWithDupecheck(new ScoreParser().parseMultiple(synchronization), mReceivedScores);
 		}
 	}
 	
 
 	@Ignore
 	abstract class FCPParser<T extends Persistent> {
 		
 		public ArrayList<T> parseMultiple(final SimpleFieldSet sfs) throws FSParseException, MalformedURLException, InvalidParameterException {
 			final int amount = sfs.getInt("Amount");
 			final ArrayList<T> result = new ArrayList<T>(amount+1);
 			for(int i=0; i < amount; ++i) {
 				result.add(parseSingle(sfs, i));
 			}
 			return result;
 		}
 		
 		abstract protected T parseSingle(SimpleFieldSet sfs, int index) throws FSParseException, MalformedURLException, InvalidParameterException;
 	
 	}
 	
 	@Ignore
 	final class IdentityParser extends FCPParser<Identity> {
 
 		@Override
 		protected Identity parseSingle(final SimpleFieldSet sfs, final int index) throws FSParseException, MalformedURLException, InvalidParameterException {
 			final String suffix = Integer.toString(index);
 			
 			final String type = sfs.get("Type" + suffix);
 	    	
 			if(type.equals("Inexistent"))
 	    		return null;
 	    	
 	        final String nickname = sfs.get("Nickname" + suffix);
 	        final String requestURI = sfs.get("RequestURI" + suffix);
 	    	final String insertURI = sfs.get("InsertURI" + suffix);
 	    	final boolean doesPublishTrustList = sfs.getBoolean("PublishesTrustList" + suffix);
 	        final String id = sfs.get("ID" + suffix); 
 	 	
 	 		final Identity identity;
 	 		
 	 		if(type.equals("Identity"))
 	 			identity = new Identity(mWoT, requestURI, nickname, doesPublishTrustList);
 	 		else if(type.equals("OwnIdentity"))
 	 			identity = new OwnIdentity(mWoT, insertURI, nickname, doesPublishTrustList);
 	 		else
 	 			throw new RuntimeException("Unknown type: " + type);
 	 		
 	 		assertEquals(identity.getID(), id);
 	 		
 	 		final int contextAmount = sfs.getInt("Contexts" + suffix + ".Amount");
 	        final int propertyAmount = sfs.getInt("Properties" + suffix + ".Amount");
 	 		
 	        for(int i=0; i < contextAmount; ++i) {
 	        	identity.addContext(sfs.get("Contexts" + suffix + ".Context" + i));
 	        }
 
 	        for(int i=0; i < propertyAmount; ++i)  {
 	            identity.setProperty(sfs.get("Properties" + suffix + ".Property" + i + ".Name"),
 	            		sfs.get("Properties" + suffix + ".Property" + i + ".Value"));
 	        }
 
 	        return identity;
 		}
 		
 	}
 	
 	@Ignore
 	final class TrustParser extends FCPParser<Trust> {
 
 		@Override
 		protected Trust parseSingle(final SimpleFieldSet sfs, final int index) throws FSParseException, InvalidParameterException {
 			final String suffix = Integer.toString(index);
 			
 	    	if(sfs.get("Value" + suffix).equals("Inexistent"))
 	    		return null;
 	    	
 			final String trusterID = sfs.get("Truster" + suffix);
 			final String trusteeID = sfs.get("Trustee" + suffix);
 			final byte value = sfs.getByte("Value" + suffix);
 			final String comment = sfs.get("Comment" + suffix);
 			
 			return new Trust(mWoT, mReceivedIdentities.get(trusterID), mReceivedIdentities.get(trusteeID), value, comment);
 		}
 		
 	}
 	
 	@Ignore
 	final class ScoreParser extends FCPParser<Score> {
 
 		@Override
 		protected Score parseSingle(final SimpleFieldSet sfs, final int index) throws FSParseException {
 			final String suffix = Integer.toString(index);
 			
 	    	if(sfs.get("Value" + suffix).equals("Inexistent"))
 	    		return null;
 	    	
 			final String trusterID = sfs.get("Truster" + suffix);
 			final String trusteeID = sfs.get("Trustee" + suffix);
 			final int capacity = sfs.getInt("Capacity" + suffix);
 			final int rank = sfs.getInt("Rank" + suffix);
 			final int value = sfs.getInt("Value" + suffix);
 			
 			return new Score(mWoT, (OwnIdentity)mReceivedIdentities.get(trusterID), mReceivedIdentities.get(trusteeID), value, rank, capacity);
 		}
 		
 	}
 
 
 }
