 package one.examples.features;
 
 import one.client.jre.OneJre;
 import one.core.domain.OneClient;
 import one.core.dsl.CoreDsl;
 import one.core.dsl.callbacks.WhenChildrenSelected;
 import one.core.dsl.callbacks.WhenSeeded;
 import one.core.dsl.callbacks.WhenShutdown;
 import one.core.dsl.callbacks.results.WithChildrenSelectedResult;
 import one.core.dsl.callbacks.results.WithSeedResult;
 import one.core.nodes.OneNode;
 import one.core.nodes.OneTypedReference;
 
 public class VersatileDataRepresentation_NodeList {
 
 	public static void main(final String[] args) {
 		createListAsNodeList();
 	}
 
 	private static void createListAsNodeList() {
 		// initialize engine
 		final CoreDsl dsl = OneJre.init();
 
 		// create a client to hold local data replication
 		final OneClient c = dsl.createClient();
 
 		// create a new 'seed' data node in the cloud
 		dsl.seed(c, new WhenSeeded() {
 
 			@Override
 			public void thenDo(final WithSeedResult sr) {
 
 				// create nodes for friends and attach to new seed node
 				final Object peter = dsl.append("Peter").to(sr.seedNode())
 						.in(c);
 				final Object paul = dsl.append("Paul").to(sr.seedNode()).in(c);
 				final Object petra = dsl.append("Petra").to(sr.seedNode())
 						.in(c);
 
 				// define a 'type' to distinguish `friend` nodes from other
 				// nodes
 				final OneNode friendType = dsl.append("aFriend")
 						.to(sr.seedNode()).atAddress("./aFriend").in(c);
 
 				// annotate all 'friend' nodes with the appropriate type
 				dsl.append(friendType).to(peter).in(c);
 				dsl.append(friendType).to(paul).in(c);
 				dsl.append(friendType).to(petra).in(c);
 
 				System.out.println("Friend data written. Wait for load ...");
 
 				// finalize local client context & commit all changes
 				dsl.shutdown(c).and(new WhenShutdown() {
 
 					@Override
 					public void thenDo() {
 						loadListAsNodeList(friendType, sr.seedNode().getId(),
 								sr.accessToken());
 					}
 				});
 
 			}
 
 		});
 
 	}
 
 	private static void loadListAsNodeList(final OneNode friendType,
 			final String friendsNodeUri, final String friendsNodeSecret) {
 		// reinitialize engine
 		final CoreDsl dsl = OneJre.init();
 
 		// instantiate another local client context
 		final OneClient c = dsl.createClient();
 
		// select from the seed node create in the previous step all nodes,
		// which contain a link to the tyoe node `aFriend`.
 		dsl.selectFrom(dsl.reference(friendsNodeUri)).theChildren()
 				.withSecret(friendsNodeSecret)
 				.linkingTo(dsl.reference(friendType)).in(c)
 				.and(new WhenChildrenSelected<OneTypedReference<Object>>() {
 
 					@Override
 					public void thenDo(
 							final WithChildrenSelectedResult<OneTypedReference<Object>> cr) {
 						System.out.println("My friends as a node list:");
 
 						// iterate through call selected children
 						for (final OneTypedReference<?> child : cr.children()) {
 							// resolve the reference into the values of the
 							// nodes
 							System.out.println(dsl.dereference(child).in(c));
 
 						}
 
 						System.out.println("As stored in: " + friendsNodeUri);
 
 						dsl.shutdown(c).and(WhenShutdown.DO_NOTHING);
 
 					}
 				});
 
 	}
 }
