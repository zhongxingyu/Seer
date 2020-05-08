 /**
  * Copyright (c) 2011 Loganalysis team and contributors
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Raffael Schmid - initial API and implementation
  */
 package com.trivadis.loganalysis.jrockit.analyzer;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Test;
 
 import com.trivadis.loganalysis.core.IAnalyzer;
 import com.trivadis.loganalysis.core.ModuleResult;
 import com.trivadis.loganalysis.core.common.progress.EmptyProgress;
 import com.trivadis.loganalysis.core.domain.IFileDescriptor;
 import com.trivadis.loganalysis.jrockit.domain.JRockitJvmRun;
 import com.trivadis.loganalysis.jrockit.internal.analyzer.IModuleProcessor;
 import com.trivadis.loganalysis.jrockit.internal.analyzer.JRockitAnalyzer;
 
 public class JRockitAnalyzerTest {
 	private final AtomicInteger count = new AtomicInteger();
	private final IAnalyzer<JRockitJvmRun> analyzer = new JRockitAnalyzer(null, new IModuleProcessor() {
 		public ModuleResult process(final JRockitJvmRun logFile, final String line) {
 			count.incrementAndGet();
 			return ModuleResult.PROCEED;
 		}
 	});
 
 	private final IFileDescriptor jrockitLogR28 = new DummyDescriptor(JROCKIT_R28);
 	private final IFileDescriptor jrockitLogR27 = new DummyDescriptor(JROCKIT_R27);
 	private final IFileDescriptor hotSpotLog = new DummyDescriptor(HOT_SPOT);
 
 	@Test
 	public void test_canHandleLogFile_hotspot() throws Exception {
 		assertFalse(analyzer.canHandleLogFile(hotSpotLog));
 	}
 
 	@Test
 	public void test_isResponsible_jrockit_r28() {
 		assertTrue(analyzer.canHandleLogFile(jrockitLogR28));
 	}
 	
 	@Test
 	public void test_isResponsible_jrockit_r27() {
 		assertFalse(analyzer.canHandleLogFile(jrockitLogR27));
 	}
 
 	@Test
 	public void test_isResponsible_hotSpot() {
 		assertFalse(analyzer.canHandleLogFile(hotSpotLog));
 	}
 
 	@Test
 	public void test_process() {
 		final JRockitJvmRun logFile = analyzer.process(jrockitLogR28, new EmptyProgress());
 		assertNotNull(logFile);
 		assertEquals(JROCKIT_R28.length, count.get());
 	}
 
 	@Test
 	public void test_getEditorId() {
 		assertEquals(JRockitAnalyzer.ANALYZER_EDITOR_ID, analyzer.getEditorId());
 	}
 
 	//@formatter:off
 	private static final String[] JROCKIT_R28 = new String[] {
 		/* 00 */"[INFO ][memory ] GC mode: Garbage collection optimized for throughput, strategy: Generational Parallel Mark & Sweep.",
 		/* 01 */"[INFO ][memory ] Heap size: 65536KB, maximal heap size: 1048576KB, nursery size: 32768KB.",
 		/* 02 */"[INFO ][memory ] <start>-<end>: <type> <before>KB-><after>KB (<heap>KB), <time> ms, sum of pauses <pause> ms.",
 		/* 03 */"[INFO ][memory ] <start>  - start time of collection (seconds since jvm start).",
 		/* 04 */"[INFO ][memory ] <type>   - OC (old collection) or YC (young collection).",
 		/* 05 */"[INFO ][memory ] <end>    - end time of collection (seconds since jvm start).",
 		/* 06 */"[INFO ][memory ] <before> - memory used by objects before collection (KB).",
 		/* 07 */"[INFO ][memory ] <after>  - memory used by objects after collection (KB).",
 		/* 08 */"[INFO ][memory ] <heap>   - size of heap after collection (KB).",
 		/* 09 */"[INFO ][memory ] <time>   - total time of collection (milliseconds).",
 		/* 10 */"[INFO ][memory ] <pause>  - total sum of pauses during collection (milliseconds).",
 		/* 11 */"[INFO ][memory ]            Run with -Xverbose:gcpause to see individual phases.",
 		/* 12 */"[INFO ][memory ] [OC#1] 0.830-0.833: OC 428KB->78423KB (117108KB), 0.003 s, sum of pauses 1.753 ms, longest pause 1.753 ms.",
 		/* 13 */"[INFO ][memory ] [OC#2] 0.892-0.982: OC 78450KB->156488KB (233624KB), 0.090 s, sum of pauses 88.419 ms, longest pause 88.419 ms.",
 		/* 14 */"[INFO ][memory ] [YC#1] 1.530-1.531: YC 156524KB->156628KB (233624KB), 0.001 s, sum of pauses 1.275 ms, longest pause 1.275 ms.",
 		/* 15 */"[INFO ][memory ] [YC#2] 1.531-1.532: YC 156652KB->156691KB (233624KB), 0.001 s, sum of pauses 0.564 ms, longest pause 0.564 ms." 
 	};
 
 	private static final String[] JROCKIT_R27 = new String[]{
 		/* 00 */"[INFO ][memory ] Running with 32 bit heap and compressed references.",
 		/* 01 */"[INFO ][memory ] GC mode: Garbage collection optimized for throughput, initial strategy: Generational Parallel Mark & Sweep",
 		/* 02 */"[INFO ][memory ] heap size: 3276800K, maximal heap size: 3276800K, nursery size: 1638400K",
 		/* 03 */"[INFO ][memory ] <s>-<end>: GC <before>K-><after>K (<heap>K), <pause> ms",
 		/* 04 */"[INFO ][memory ] <s/start> - start time of collection (seconds since jvm start)",
 		/* 05 */"[INFO ][memory ] <end>     - end time of collection (seconds since jvm start)",
 		/* 06 */"[INFO ][memory ] <before>  - memory used by objects before collection (KB)",
 		/* 07 */"[INFO ][memory ] <after>   - memory used by objects after collection (KB)",
 		/* 08 */"[INFO ][memory ] <heap>    - size of heap after collection (KB)",
 		/* 09 */"[INFO ][memory ] <pause>   - total sum of pauses during collection (milliseconds)",
 		/* 10 */"[INFO ][memory ]             run with -Xverbose:gcpause to see individual pauses",
 		/* 11 */"[INFO ][memory ] 362.410: parallel nursery GC 1811917K->619093K (3276800K), 168.904 ms",
 		/* 12 */"[INFO ][memory ] 652.793: parallel nursery GC 1992014K->762767K (3276800K), 53.391 ms",
 		/* 13 */"[INFO ][memory ] 661.220: parallel nursery GC 1991567K->762575K (3276800K), 40.372 ms"
 	};  
 	    
 	private static final String[] HOT_SPOT = new String[] {
 		/* 00 */"0.618: [GC 250439K->250447K(295744K), 0.0863349 secs]",
 		/* 01 */"0.704: [Full GC 250447K->250426K(328768K), 0.1010896 secs]",
 		/* 02 */"0.816: [GC 266490K->266554K(339776K), 0.0806770 secs]",
 		/* 03 */"0.917: [GC 293626K->293738K(344832K), 0.1496144 secs]",
 		/* 04 */"1.067: [Full GC 293738K->293735K(410496K), 0.1766740 secs]",
 		/* 05 */"1.266: [GC 325863K->325991K(426816K), 0.1364323 secs]"
 	};
 	
 	
 
 }
