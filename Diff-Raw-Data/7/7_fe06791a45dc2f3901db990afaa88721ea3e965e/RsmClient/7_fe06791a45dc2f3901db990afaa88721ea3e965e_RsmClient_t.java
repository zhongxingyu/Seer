 package com.appjangle.rsm.client;
 
 import io.nextweb.Link;
 import io.nextweb.LinkList;
 import io.nextweb.ListQuery;
 import io.nextweb.Node;
 import io.nextweb.NodeList;
 import io.nextweb.Query;
 import io.nextweb.Session;
 import io.nextweb.common.Interval;
 import io.nextweb.common.Monitor;
 import io.nextweb.common.MonitorContext;
 import io.nextweb.fn.Closure;
 import io.nextweb.fn.ExceptionListener;
 import io.nextweb.fn.ExceptionResult;
 import io.nextweb.fn.Result;
 import io.nextweb.fn.Success;
 import io.nextweb.jre.Nextweb;
 import io.nextweb.operations.exceptions.ImpossibleListener;
 import io.nextweb.operations.exceptions.ImpossibleResult;
 
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.appjangle.rsm.client.commands.ComponentOperation;
 import com.appjangle.rsm.client.commands.OperationCallback;
 import com.appjangle.rsm.client.commands.v01.ComponentCommandData;
 import com.appjangle.rsm.client.commands.v01.FailureResponse;
 import com.appjangle.rsm.client.commands.v01.SuccessResponse;
 
 public class RsmClient {
 
 	/**
 	 * Run an operation on the server for a specific component.
 	 * 
 	 * @param operation
 	 * @param forId
 	 * @param conf
 	 * @param callback
 	 */
 	public static void performCommand(final ComponentOperation operation,
 			final ClientConfiguration conf, final OperationCallback callback) {
 
 		assert operation != null;
 
 		final Session session = Nextweb.createSession();
 
 		// prepare response node
 		final Link responsesLink = session.node(conf.getResponsesNode(),
 				conf.getResponseNodeSecret());
 
 		responsesLink.selectAllLinks().get(new Closure<LinkList>() {
 
 			@Override
 			public void apply(final LinkList ll) {
 				createResponsesNode(operation, conf, callback, session,
 						responsesLink, ll);
 			}
 		});
 
 	}
 
 	private static void createResponsesNode(final ComponentOperation operation,
 			final ClientConfiguration conf, final OperationCallback callback,
 			final Session session, final Link responsesLink, final LinkList ll) {
 		final Query responseQuery = responsesLink.appendSafe("r"
 				+ (new Random().nextInt()));
 
 		responseQuery.catchImpossible(new ImpossibleListener() {
 
 			@Override
 			public void onImpossible(final ImpossibleResult ir) {
 
 				// just try again
 				createResponsesNode(operation, conf, callback, session,
 						responsesLink, ll);
 
 			}
 		});
 
 		responseQuery.get(new Closure<Node>() {
 
 			@Override
 			public void apply(final Node response) {
 
 				submitCommand(operation, conf, callback, session,
 						responsesLink, response);
 			}
 		});
 
 	}
 
 	private static void submitCommand(final ComponentOperation operation,
 			final ClientConfiguration conf, final OperationCallback callback,
 			final Session session, final Link responsesLink, final Node response) {
 		// preparing command
 		final ComponentCommandData command = new ComponentCommandData();
 
 		// System.out.println("submitting to: " + conf.getCommandsNode());
 
 		command.setOperation(operation);
 		command.setPort(Nextweb
 				.getEngine()
 				.getFactory()
 				.createPort(session, response.uri(),
 						conf.getResponseNodeSecret()));
 
 		final AtomicBoolean responseReceived = new AtomicBoolean(false);
 
 		// monitor node for response from server
 		final Result<Monitor> monitor = response.monitor(Interval.FAST,
 				new Closure<MonitorContext>() {
 
 					@Override
 					public void apply(final MonitorContext ctx) {
 
 						final ListQuery allQuery = ctx.node().selectAll();
 
 						allQuery.get(new Closure<NodeList>() {
 
 							@Override
 							public void apply(final NodeList o) {
 
 								for (final Node n : o.nodes()) {
 
 									final Object obj = n.value();
 									if (obj instanceof SuccessResponse) {
 										responseReceived.set(true);
 
 										response.removeSafe(n);
 										responsesLink.removeSafe(response);
 
 										ctx.monitor().stop()
 												.get(new Closure<Success>() {
 
 													@Override
 													public void apply(
 															final Success o) {
 														session.close()
 																.get(new Closure<Success>() {
 
 																	@Override
 																	public void apply(
 																			final Success o) {
 
 																		callback.onSuccess();
 																	}
 																});
 
 													}
 												});
 
 										return;
 									}
 
 									if (obj instanceof FailureResponse) {
 										responseReceived.set(true);
 
 										response.remove(n);
 										responsesLink.remove(response);
 										ctx.monitor().stop()
 												.get(new Closure<Success>() {
 
 													@Override
 													public void apply(
 															final Success o) {
 														session.close()
 																.get(new Closure<Success>() {
 
 																	@Override
 																	public void apply(
 																			final Success o) {
 																		final FailureResponse failureResponse = (FailureResponse) obj;
 																		callback.onFailure(failureResponse
 																				.getException());
 																	}
 																});
 													}
 												});
 
 										return;
 									}
 
 								}
 
 							}
 						});
 
 					}
 				});
 
 		new Thread() {
 
 			@Override
 			public void run() {
 
 				try {
					int roundsLeft = 60 * 5;

					while (!responseReceived.get() && roundsLeft > 0) {
						Thread.sleep(1000);
						roundsLeft--;
					}
 
 					if (responseReceived.get()) {
 						return;
 					}
 
 					final ExceptionListener el = new ExceptionListener() {
 
 						@Override
 						public void onFailure(final ExceptionResult r) {
 							callback.onFailure(r.exception());
 						}
 					};
 
 					assert monitor.get() != null;
 					final Result<Success> stop = monitor.get().stop();
 					stop.catchExceptions(el);
 
 					final Result<Success> removeSafe = responsesLink
 							.removeSafe(response);
 					removeSafe.catchExceptions(el);
 
 					final Result<Success> close = session.close();
 
 					close.catchExceptions(el);
 
 					monitor.get(new Closure<Monitor>() {
 
 						@Override
 						public void apply(final Monitor o) {
 
 							removeSafe.get(new Closure<Success>() {
 
 								@Override
 								public void apply(final Success o) {
 
 									close.get(new Closure<Success>() {
 
 										@Override
 										public void apply(final Success o) {
 											callback.onFailure(new Exception(
 													"No response from server received in timeout (5 min)."));
 										}
 									});
 
 								}
 							});
 
 						}
 					});
 
 				} catch (final Exception e) {
 					callback.onFailure(e);
 				}
 
 			}
 
 		}.start();
 
 		// synchronizing all changes with server
 		session.commit().get(new Closure<Success>() {
 
 			@Override
 			public void apply(final Success o) {
 
 				// add to commands node
 				session.post(command, conf.getCommandsNode(),
 						conf.getCommandsNodeSecret());
 
 				session.commit().get(new Closure<Success>() {
 
 					@Override
 					public void apply(final Success o) {
 
 					}
 				});
 			}
 		});
 
 	}
 }
