 package com.macrohuang.fileq;
 
 import java.util.Collections;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.macrohuang.fileq.conf.Config;
 import com.macrohuang.fileq.impl.ThreadLockFileQueueImpl;
 
 public class FileQueueTest {
 	int max = 10000;
     int threads = 20;
     ExecutorService executorService = Executors.newFixedThreadPool(threads);
 	Config config = new Config();
	static int index = 0;
 
     @Before
     public void init(){
		config.setQueueFilePath("D:\\tmp\\fileq" + (index++));
 		config.setInit(true);
 		config.setSizePerFile(1024 * 1024 * 100);
     }
     
     @Test
     public void testAdd() {
 		FileQueue<MyObject> fileQueue = new ThreadLockFileQueueImpl<MyObject>(config);
         for (int i = 0; i < max; i++) {
             fileQueue.add(new MyObject());
         }
         Assert.assertEquals(max, fileQueue.size());
 		fileQueue.delete();
     }
 
     @Test
     public void testAddMultThread() throws InterruptedException {
 		final FileQueue<MyObject> fileQueue = new ThreadLockFileQueueImpl<MyObject>(config);
     	for (int i=0;i<threads;i++){
     		executorService.submit(new Runnable() {
 				@Override
 				public void run() {
 					for (int i = 0; i < max / threads; i++) {
 						fileQueue.add(new MyObject());
 					}
 				}
 			});
     	}
     	executorService.shutdown();
     	executorService.awaitTermination(100, TimeUnit.SECONDS);
     	Assert.assertEquals(max, fileQueue.size());
 		fileQueue.delete();
     }
 
     @Test
     public void testPeek() {
 		final FileQueue<MyObject> fileQueue = new ThreadLockFileQueueImpl<MyObject>(config);
         fileQueue.add(new MyObject());
         MyObject myObject = fileQueue.peek();
         MyObject myObject2 = fileQueue.peek();
         Assert.assertNotNull(myObject);
         Assert.assertNotNull(myObject2);
         org.junit.Assert.assertEquals(myObject, myObject2);
 		fileQueue.delete();
     }
 
 	@Test
 	public void testAddMultiFiles() throws Exception {
 		config.setSizePerFile(1024);
 		FileQueue<Integer> fq = new ThreadLockFileQueueImpl<Integer>(config);
 		int times = 2000;
 		for (int i = 0; i < times; i++) {
 			fq.add(i);
 		}
 		System.out.println("Add finished, queue size: " + fq.size());
 		for (int i = 0; i < times; i++) {
			Assert.assertEquals(i, fq.take().intValue());
 		}
 		fq.delete();
 	}
 
 	@Test
 	public void testGetTimeout() throws Exception {
 		final FileQueue<MyObject> fileQueue = new ThreadLockFileQueueImpl<MyObject>(config);
 		long start = System.currentTimeMillis();
 		MyObject res = fileQueue.take(1, TimeUnit.SECONDS);
 		Assert.assertEquals(1, (System.currentTimeMillis() - start) / 1000);
 		Assert.assertNull(res);
 		fileQueue.delete();
 	}
 
 	@Test
 	public void testQueueRestart() throws Exception {
 		int times = 10000;
 		FileQueue<Integer> fq = new ThreadLockFileQueueImpl<Integer>(config);
 		for (int i = 0; i < times; i++) {
 			fq.add(i);
 		}
 
 		for (int i = 0; i < times / 2; i++) {
 			Assert.assertEquals(Integer.valueOf(i), fq.take());
 		}
 		fq.close();
 
 		config.setInit(false);
 		fq = new ThreadLockFileQueueImpl<Integer>(config);
 		for (int i = times / 2; i < times; i++) {
 			Assert.assertEquals(Integer.valueOf(i), fq.take());
 		}
 		fq.delete();
 	}
 
 	@Test
 	public void testQueueRestart2() throws Exception {
 		int times = 1000;
 		config.setSizePerFile(1024);
 		FileQueue<Integer> fq = new ThreadLockFileQueueImpl<Integer>(config);
 		for (int i = 0; i < times; i++) {
 			fq.add(i);
 		}
 
 		for (int i = 0; i < times / 2; i++) {
 			Assert.assertEquals(Integer.valueOf(i), fq.take());
 		}
 
 		fq.close();
 		Thread.sleep(5000);
 
 		config.setInit(false);
 		fq = new ThreadLockFileQueueImpl<Integer>(config);
 		for (int i = times / 2; i < times; i++) {
 			Assert.assertEquals(Integer.valueOf(i), fq.take());
 		}
 		fq.delete();
 	}
 
 	@Test
 	public void testWriteSpeed() throws Exception {
 		FileQueue<byte[]> fq = new ThreadLockFileQueueImpl<byte[]>(config);
 		byte[] content = new byte[1024];
 		for (int i = 0; i < 1024; i++) {
 			content[i] = 0x55;
 		}
 		int times = 100000;
 		long start = System.currentTimeMillis();
 		for (int i = 0; i < times; i++) {
 			fq.add(content);
 		}
 		System.out.printf("[Write]Time spend %d ms for %d times. Avg msg length 1024bytes, each data file %d bytes.\n",
 				(System.currentTimeMillis() - start), times, config.getSizePerFile());
 		fq.delete();
 
 	}
 
 	@Test
 	public void testReadSpeed() throws Exception {
 		config.setSizePerFile(1024 * 1024 * 500);
 		FileQueue<byte[]> fq = new ThreadLockFileQueueImpl<byte[]>(config);
 		byte[] content = new byte[1024];
 		for (int i = 0; i < 1024; i++) {
 			content[i] = 0x55;
 		}
 
 		int times = 100000;
 		for (int i = 0; i < times; i++) {
 			fq.add(content);
 		}
 		System.out.println("Add finished.");
 		long start = System.currentTimeMillis();
 		Thread.sleep(1000);
 		for (int i = 0; i < times; i++) {
 			// System.out.println(i);
 			fq.take();
 		}
 
 		System.out.printf("[Read]Time spend %d ms for %d times. Avg msg length 1024bytes, each data file %d bytes.\n",
 				(System.currentTimeMillis() - start), times, config.getSizePerFile());
 	}
 
 	@Test
 	public void testReadWriteSpeed() throws Exception {
 		FileQueue<byte[]> fq = new ThreadLockFileQueueImpl<byte[]>(config);
 		byte[] content = new byte[1024];
 		for (int i = 0; i < 1024; i++) {
 			content[i] = 0x55;
 		}
 
 		int times = 100000;
 
 		long start = System.currentTimeMillis();
 		for (int i = 0; i < times; i++) {
 			fq.add(content);
 			fq.take();
 		}
 		System.out.printf("[ReadWrite]Time spend %d ms for %d times. Avg msg length 1024bytes, each data file %d bytes.\n",
 				(System.currentTimeMillis() - start), times, config.getSizePerFile());
 	}
 
 	@Test
 	public void concurrentTestReadFasterThanWrite() throws Exception {
		final int totalTimes = 100;
 		final int writerCount = 10;
 		final int readerCount = 20;
 
 		final FileQueue<MyObject> fq = new ThreadLockFileQueueImpl<MyObject>(config);
 		final Set<MyObject> results = Collections.synchronizedSet(new TreeSet<MyObject>());
 
 		final Set<MyObject> expected = Collections.synchronizedSet(new TreeSet<MyObject>());
 
 		final CountDownLatch startLatch = new CountDownLatch(1);
 		final CountDownLatch endLatch = new CountDownLatch(writerCount + readerCount);
 
 		for (int i = 0; i < writerCount; i++) {
 			Thread writerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					for (int j = 0; j < totalTimes / writerCount; j++) {
 						try {
 							MyObject m = new MyObject();
 							fq.add(m);
 							expected.add(m);
 							Thread.sleep(5);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					endLatch.countDown();
 				}
 			});
 
 			writerThread.start();
 		}
 		for (int i = 0; i < readerCount; i++) {
 			Thread readerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					for (int j = 0; j < totalTimes / readerCount; j++) {
 						try {
 							MyObject m = fq.take();
							if (m != null) {
								results.add(m);
							}
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 					endLatch.countDown();
 				}
 			});
 			readerThread.start();
 		}
 		startLatch.countDown();
 		endLatch.await();
 		Assert.assertTrue(expected.equals(results));
 	}
 
 	@Test
 	public void concurrentTestWriterFasterThanReader() throws Exception {
 		final int totalTimes = 10000;
 		final int writerCount = 20;
 		final int readerCount = 10;
 
 		final FileQueue<MyObject> fq = new ThreadLockFileQueueImpl<MyObject>(config);
 		final Set<MyObject> results = Collections.synchronizedSet(new TreeSet<MyObject>());
 
 		final Set<MyObject> expected = Collections.synchronizedSet(new TreeSet<MyObject>());
 
 		final CountDownLatch startLatch = new CountDownLatch(1);
 		final CountDownLatch endLatch = new CountDownLatch(writerCount + readerCount);
 
 		for (int i = 0; i < writerCount; i++) {
 			Thread writerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					for (int j = 0; j < totalTimes / writerCount; j++) {
 						try {
 							MyObject m = new MyObject();
 							fq.add(m);
 							expected.add(m);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					endLatch.countDown();
 				}
 			});
 
 			writerThread.start();
 		}
 
 		for (int i = 0; i < readerCount; i++) {
 			Thread readerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					for (int j = 0; j < totalTimes / readerCount; j++) {
 						try {
 							MyObject m = fq.take();
 							results.add(m);
 							Thread.sleep(5);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					endLatch.countDown();
 				}
 			});
 
 			readerThread.start();
 		}
 
 		startLatch.countDown();
 		endLatch.await();
 		Assert.assertTrue(expected.equals(results));
 	}
 
 	@Test
 	public void concurrentTestWriterReaderWithSameSpeed() throws Exception {
 		final int totalTimes = 100000;
 		final int writerCount = 20;
 		final int readerCount = 20;
 
 		final FileQueue<MyObject> fq = new ThreadLockFileQueueImpl<MyObject>(config);
 		final Set<MyObject> results = Collections.synchronizedSet(new TreeSet<MyObject>());
 
 		final Set<MyObject> expected = Collections.synchronizedSet(new TreeSet<MyObject>());
 
 		final CountDownLatch startLatch = new CountDownLatch(1);
 		final CountDownLatch endLatch = new CountDownLatch(writerCount + readerCount);
 
 		for (int i = 0; i < writerCount; i++) {
 			Thread writerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					for (int j = 0; j < totalTimes / writerCount; j++) {
 						try {
 							MyObject m = new MyObject();
 							fq.add(m);
 							expected.add(m);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					endLatch.countDown();
 				}
 			});
 
 			writerThread.start();
 		}
 
 		for (int i = 0; i < readerCount; i++) {
 			Thread readerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					for (int j = 0; j < totalTimes / readerCount; j++) {
 						try {
 							MyObject m = fq.take();
 							results.add(m);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					endLatch.countDown();
 				}
 			});
 
 			readerThread.start();
 		}
 
 		startLatch.countDown();
 		endLatch.await();
 		Assert.assertTrue(expected.equals(results));
 	}
 
	// @Test
 	public void stressTest() throws Exception {
 		final int writerCount = 20;
 		final int readerCount = 20;
 
 		final FileQueue<MyObject> fq = new ThreadLockFileQueueImpl<MyObject>(config);
 
 		final CountDownLatch startLatch = new CountDownLatch(1);
 		final AtomicBoolean exceptionOccur = new AtomicBoolean(false);
 
 		for (int i = 0; i < writerCount; i++) {
 			Thread writerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					Random random = new Random(System.currentTimeMillis());
 
 					while (!exceptionOccur.get()) {
 						try {
 							MyObject m = new MyObject();
 							fq.add(m);
 							System.out.println("[Write]" + m);
 							Thread.sleep(random.nextInt(100));
 						} catch (Exception e) {
 							exceptionOccur.set(true);
 						}
 					}
 
 				}
 			});
 
 			writerThread.start();
 		}
 
 		for (int i = 0; i < readerCount; i++) {
 			Thread readerThread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						startLatch.await();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 					Random random = new Random(System.currentTimeMillis());
 
 					while (!exceptionOccur.get()) {
 						try {
 							MyObject m = fq.take();
 							System.out.println("[Read]" + m);
 							Thread.sleep(random.nextInt(100));
 						} catch (Exception e) {
 							exceptionOccur.set(true);
 						}
 					}
 
 				}
 			});
 
 			readerThread.start();
 		}
 
 		startLatch.countDown();
 		System.in.read();
 	}
 }
