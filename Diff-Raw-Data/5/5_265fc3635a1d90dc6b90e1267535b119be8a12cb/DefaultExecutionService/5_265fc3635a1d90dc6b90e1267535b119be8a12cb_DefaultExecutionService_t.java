 package org.cotrix.common.async;
 
 import static java.lang.Thread.*;
 import static org.cotrix.common.CommonUtils.*;
 import static org.cotrix.common.async.TaskUpdate.*;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.RejectedExecutionException;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import org.cotrix.common.async.TaskManagerProvider.TaskManager;
 import org.cotrix.common.tx.Transaction;
 import org.cotrix.common.tx.Transactions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ApplicationScoped
 public class DefaultExecutionService implements ExecutionService {
 
 	private static ExecutorService service = Executors.newCachedThreadPool();
 	private Logger logger = LoggerFactory.getLogger(DefaultExecutionService.class);
 
 	@Inject
 	private TaskContext context;
 
 	@Inject
 	private TaskManagerProvider managers;
 
 	@Inject
 	Transactions txs;
 
 	private static class Closure {	
 		Task t; 
 	}
 
 
 	@Override
 	public <T> ReportingFuture<T> execute(final Callable<T> task) throws RejectedExecutionException {
 
 		notNull("task", task);
 
 		try {
 			final CountDownLatch started = new CountDownLatch(1);
 
 			final Closure closure = new Closure();
 
 			final TaskManager manager = managers.get();
 			
 			final Object cancelMonitor = new Object();
 
 			Callable<T> wrap = new Callable<T>() {
 
 
 				@Override
 				public T call() throws Exception {
 
 					manager.started();
 
 					try {
 
 						closure.t = context.thisTask();
 
 						started.countDown();
 
 						Transaction tx = txs.open();
 						
 						try {
 
 							logger.trace("started transaction for async task {}",tx);
 
 							T result = task.call();
 
							synchronized(cancelMonitor) {
 								
								if (!currentThread().isInterrupted()){
 									tx.commit();
 									context.save(update(1f, "task completed"));
 									logger.trace("committed transaction {}",tx);
 								}
 							}
 
 							return result;
 						}
 						
 						finally {
 							
 							synchronized (cancelMonitor) {
 							
 								//clears interrupt thread, if any.
 								Thread.interrupted();
 								
 								tx.close();
 							}
 						}
 
 					}
 					catch (CancelledTaskException e) {
 						
 						throw new InterruptedException(e.getMessage());
 						
 					}
 					catch(Exception e) {
 
 						context.thisTask().failed(e);
 
 						throw e;
 					}
 
 					finally {
 
 						context.reset();
 
 						manager.finished();
 					}
 				}
 			};
 
 			Future<T> future = service.submit(wrap);
 
 			started.await();
 
 			return new DefaultReportingFuture<T>(future,closure.t,cancelMonitor);
 
 		}
 		catch(Exception e) {
 			throw unchecked(e);
 		}
 	}
 }
