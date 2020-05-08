 package chordest.transform;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import chordest.util.DataUtil;
 import chordest.wave.Buffer;
 import chordest.wave.ITaskProvider;
 
 
 public class PooledTransformer {
 
 	private static final Logger LOG = LoggerFactory.getLogger(PooledTransformer.class);
 	private static final int SLEEP_MS = 100;
 
 	private final ITaskProvider provider;
 	private final ITransformProvider transProvider;
 	
 	private final int transformsInTotal;
 	private final CountDownLatch latch;
 	private final ThreadPoolExecutor threadPool;
 	private final int threadPoolQueueSize;
 	
 	private boolean isCancelRequested = false;
 
 	public PooledTransformer(ITaskProvider provider, int threadPoolSize, int transforms,
 			ITransformProvider transProvider) {
 		if (provider == null) {
 			throw new NullPointerException("Task provider is null");
 		}
 		this.provider = provider;
 		this.transProvider = transProvider;
 		this.transformsInTotal = transforms;
 		this.latch = new CountDownLatch(transforms);
 		this.threadPoolQueueSize = 3 * threadPoolSize;
 		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
 	}
 
 	public double[][] run() throws InterruptedException {
 		final List<Future<Buffer>> futures = new ArrayList<Future<Buffer>>(transformsInTotal);
 		new Thread(provider).start(); // start provider in a separate thread
 		LOG.debug("File processing started...");
 		while (!isCancelRequested && latch.getCount() > 0) {
 			final Buffer buffer = provider.poll();
 			if (buffer != null) {
 				final ITransform transform = transProvider.getTransform(buffer, latch);
 				// simplest way to limit the queue size of the thread pool
 				while (threadPool.getQueue().size() > threadPoolQueueSize) {
 					Thread.sleep(SLEEP_MS);
 				}
 				futures.add(threadPool.submit(transform));
 			}
 		}
 		threadPool.shutdownNow();
 		if (isCancelRequested && latch.getCount() > 0) {
 			throw new InterruptedException("Cancelled");
 		}
 		final List<Buffer> buffers = PooledTransformer.toBufferList(futures);
 		DataUtil.sortByTimestamp(buffers);
 		final double[][] result = new double[buffers.size()][];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = buffers.get(i).getData();
 		}
 		LOG.debug("... finished transforming wave data");
 		return result;
 	}
 
 	public void cancel() {
 		isCancelRequested = true;
 	}
 
 	private static List<Buffer> toBufferList(List<Future<Buffer>> futures) {
 		if (futures == null) {
 			throw new NullPointerException("futures is null");
 		}
 		List<Buffer> result = new ArrayList<Buffer>(futures.size());
 		try {
 			for (Future<Buffer> future : futures) {
 				if (future == null){
 					throw new NullPointerException("Future is null");
				} else if (future.isCancelled()) {
					throw new IllegalStateException("Future is cancelled");
				} else if (!future.isDone()) {
					Thread.sleep(100);
					if (! future.isDone()) {
						throw new IllegalStateException("Future is not done");
					}
 				}
 				result.add(future.get());
 			}
 		} catch (InterruptedException e) {
 			LOG.error("Interrupted when getting data from Futures", e);
 		} catch (ExecutionException e) {
 			LOG.error("Error when getting data from Futures", e);
 		}
 		return result;
 	}
 
 	public static interface ITransformProvider {
 		ITransform getTransform(Buffer buffer, CountDownLatch latch);
 	}
 
 }
