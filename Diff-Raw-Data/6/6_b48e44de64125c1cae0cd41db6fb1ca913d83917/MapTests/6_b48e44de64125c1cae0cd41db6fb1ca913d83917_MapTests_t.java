 package org.jrenner;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.*;
 
 import java.util.HashMap;
 import java.util.Map;
 import static org.jrenner.Master.*;
 
 public class MapTests {
 	public static int loopCount = 1000;
 	private static boolean runningTests = false;
 	public static Thread testRunnerThread = null;
 
 	public static void hashMapTest() {
 		System.gc();
 		Map<DummyObject, Integer> map = new HashMap<>();
 		String testName = map.getClass().getSimpleName();
 		int i = 0;
 		PerformanceCounter perf = new PerformanceCounter(testName);
 		while (i < loopCount) {
 			perf.start();
 			i++;
 			String name = "dummy " + i;
 			DummyObject dummy = new DummyObject(rand.nextFloat() * 10000, rand.nextFloat() * 10000, name);
 			Integer someValue = rand.nextInt(100);
 			map.put(dummy, someValue);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "put test");
 		printHeap();;
 
 		perf.reset();
 		int x;
 		for (DummyObject dummy : map.keySet()) {
 			perf.start();
 			x = map.get(dummy);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "get test");
 		System.gc();
 	}
 
 	public static void arrayMapTest() {
 		System.gc();
 		ArrayMap<DummyObject, Integer> map = new ArrayMap<>();
 		String testName = map.getClass().getSimpleName();
 		int i = 0;
 		PerformanceCounter perf = new PerformanceCounter(testName);
 		while (i < loopCount) {
 			perf.start();
 			i++;
 			String name = "dummy " + i;
 			DummyObject dummy = new DummyObject(rand.nextFloat() * 10000, rand.nextFloat() * 10000, name);
 			Integer someValue = rand.nextInt(100);
 			map.put(dummy, someValue);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "put test");
 		printHeap();;
 
 		perf.reset();
 		int x;
 		for (DummyObject dummy : map.keys()) {
 			perf.start();
 			x = map.get(dummy);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "get test");
 		System.gc();
 	}
 
 
 	public static void objectMapTest() {
 		System.gc();
 		ObjectMap<DummyObject, Integer> map = new ObjectMap<>();
 		String testName = map.getClass().getSimpleName();
 		int i = 0;
 		PerformanceCounter perf = new PerformanceCounter(testName);
 		while (i < loopCount) {
 			perf.start();
 			i++;
 			String name = "dummy " + i;
 			DummyObject dummy = new DummyObject(rand.nextFloat() * 10000, rand.nextFloat() * 10000, name);
 			Integer someValue = rand.nextInt(100);
 			map.put(dummy, someValue);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "put test");
 		printHeap();;
 
 		perf.reset();
 		int x;
 		for (DummyObject dummy : map.keys()) {
 			perf.start();
 			x = map.get(dummy);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "get test");
 		System.gc();
 	}
 
 	public static void identityMapTest() {
 		System.gc();
 		IdentityMap<DummyObject, Integer> map = new IdentityMap<>();
 		String testName = map.getClass().getSimpleName();
 		int i = 0;
 		PerformanceCounter perf = new PerformanceCounter(testName);
 		while (i < loopCount) {
 			perf.start();
 			i++;
 			String name = "dummy " + i;
 			DummyObject dummy = new DummyObject(rand.nextFloat() * 10000, rand.nextFloat() * 10000, name);
 			Integer someValue = rand.nextInt(100);
 			map.put(dummy, someValue);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "put test");
 		printHeap();
 
 		perf.reset();
 		int x;
 		for (DummyObject dummy : map.keys()) {
 			perf.start();
 			x = map.get(dummy);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "get test");
 		System.gc();
 	}
 
 	public static void longMapTest() {
 		System.gc();
 		LongMap<DummyObject> map = new LongMap<>();
 		String testName = map.getClass().getSimpleName();
 		int i = 0;
 		PerformanceCounter perf = new PerformanceCounter(testName);
 		while (i < loopCount) {
 			perf.start();
 			i++;
 			String name = "dummy " + i;
 			DummyObject dummy = new DummyObject(rand.nextFloat() * 10000, rand.nextFloat() * 10000, name);
 			Integer someValue = rand.nextInt(100);
 			map.put(dummy.longCode, dummy);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "put test");
 		printHeap();
 
 		perf.reset();
 		DummyObject dum;
 		for (long j = 0; j < loopCount; j++) {
 			perf.start();
 			dum = map.get(j);
 			perf.stop();
 			perf.tick();
 		}
 		printCounter(perf, "get test");
 		System.gc();
 	}
 
 	public static void printHeap() {
 		boolean disabled = true;
 		if (disabled) return;
 		double heapSize = (double) Gdx.app.getJavaHeap() / 1000000.0;
 		print(String.format("Java heap: %.2fM", heapSize));
 
 		double nativeHeapSize = (double) Gdx.app.getNativeHeap() / 1000000.0;
 		if (Math.abs(heapSize - nativeHeapSize) > 10) {
 			print(String.format("Native heap: %.2fM", nativeHeapSize));
 		}
 	}
 
 	public static void printCounter(PerformanceCounter perf, String testName) {
 		String report = String.format("...%20s, avg: %.3fms, total: %.3fms, min/max: %.3f/%.3fms",
 				perf.name + " " + testName, perf.time.average * 1000, perf.time.total * 1000,
 				perf.time.min * 1000, perf.time.max * 1000);
 		updateStatus(report);
 		print(report);
 	}
 
 	public static void runAllTests() {
 		if (runningTests) {
 			return;
 		}
 		testRunnerThread = new Thread(testRunner);
 		testRunnerThread.start();
 	}
 
 	private static Runnable testRunner = new Runnable() {
 		@Override
 		public void run() {
 			updateStatus("running " + loopCount + " iterations for each test");
 			runningTests = true;
 			PerformanceCounter perf = new PerformanceCounter("Total time taken");
 			perf.start();
 			hashMapTest();
 			arrayMapTest();
 			objectMapTest();
 			identityMapTest();
 			longMapTest();
 			perf.stop();
 			perf.tick();
 
			updateStatus(String.format("Total time taken for all tests: %.3fms", perf.time.total * 1000));
 			runningTests = false;
 		}
 	};
 
 	private static void updateStatus(String status) {
 		Master.addTextLine(status);
 	}
 }
