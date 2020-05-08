 /**
  * Copyright (C) 2013
  *   Michael Mosmann <michael@mosmann.de>
  *
  * with contributions from
  * 	${lic.developers}
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.flapdoodle.logparser.usecases;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import de.flapdoodle.logparser.ILineProcessor;
 import de.flapdoodle.logparser.IMatcher;
 import de.flapdoodle.logparser.IRewindableReader;
 import de.flapdoodle.logparser.IStreamErrorListener;
 import de.flapdoodle.logparser.IStreamListener;
 import de.flapdoodle.logparser.LogEntry;
 import de.flapdoodle.logparser.StreamProcessException;
 import de.flapdoodle.logparser.StreamProcessor;
 import de.flapdoodle.logparser.io.BufferedReaderAdapter;
 import de.flapdoodle.logparser.io.Streams;
 import de.flapdoodle.logparser.io.WriteToListLineProcessor;
 import de.flapdoodle.logparser.matcher.javalogging.StandardJavaLoggingMatcher;
 import de.flapdoodle.logparser.stacktrace.AbstractStackFrame;
 import de.flapdoodle.logparser.stacktrace.At;
 import de.flapdoodle.logparser.streamlistener.CollectingStreamListener;
 import de.flapdoodle.logparser.streamlistener.DoesNotExpectAnyErrorListener;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static junit.framework.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 public class TestJavaLogging {
 
 	@Test
 	public void readLogfile() throws IOException {
 
         CollectingStreamListener<LogEntry> streamListener = new CollectingStreamListener<LogEntry>();
         WriteToListLineProcessor defaultLineProcessor = new WriteToListLineProcessor();
 
         processLogFile("java-logging-stacktrace-sample.txt.gz", streamListener, defaultLineProcessor, new DoesNotExpectAnyErrorListener());
 
         assertTrue("no lines to console", defaultLineProcessor.lines().isEmpty());
 		ImmutableList<LogEntry> entries = streamListener.entries();
 
 		assertEquals("Log Lines", 8, entries.size());
 
 		AbstractStackFrame rootCause = entries.get(3).stackTrace().get().rootCause();
 		assertEquals("rootCause", "java.lang.NullPointerException", rootCause.exception().exceptionClass());
 
 		At rootCauseAt = rootCause.firstAt().get();
 		assertEquals("firstRootCause", "de.flapdoodle.logparser.usecases.TestJavaLogging", rootCauseAt.classname());
 		assertEquals("firstRootCause", "inner", rootCauseAt.method());

        assertEquals("date","Apr 04, 2013 8:01:44 PM",entries.get(0).attributes().get("date"));
        assertEquals("date","Apr 04, 2013 8:02:45 PM",entries.get(1).attributes().get("date"));
        assertEquals("date","Apr 04, 2013 8:03:45 PM",entries.get(2).attributes().get("date"));
        assertEquals("date","Apr 04, 2013 8:04:45 PM",entries.get(3).attributes().get("date"));

        assertEquals("date","Apr 04, 2013 11:01:21 PM",entries.get(4).attributes().get("date"));
        assertEquals("date","Apr 04, 2013 11:02:21 PM",entries.get(5).attributes().get("date"));
        assertEquals("date","Apr 04, 2013 11:03:21 PM",entries.get(6).attributes().get("date"));
        assertEquals("date","Apr 04, 2013 11:04:21 PM",entries.get(7).attributes().get("date"));
    }
 
     @Test
     public void logfileWithErrorInStackTrace() throws IOException {
 
         CollectingStreamListener<LogEntry> streamListener = new CollectingStreamListener<LogEntry>();
         WriteToListLineProcessor defaultLineProcessor = new WriteToListLineProcessor();
 
         final List<LogEntry> wrongEntries=Lists.newArrayList();
         
         IStreamErrorListener errorListener=new IStreamErrorListener() {
             @Override
             public void error(StreamProcessException sx) {
                 assertNotNull(sx);
                 assertNotNull("Entry",sx.entry(LogEntry.class).isPresent());
                 wrongEntries.add(sx.entry(LogEntry.class).get());
             }
         };
 
         processLogFile("stacktrace-with-error-in-stacktrace-multiline.txt", streamListener, defaultLineProcessor, errorListener);
 
         assertTrue("no lines to console", defaultLineProcessor.lines().isEmpty());
         ImmutableList<LogEntry> entries = streamListener.entries();
 
         assertEquals("Log Lines", 1, entries.size());
         assertEquals("Log Lines", 1, wrongEntries.size());
     }
 
     private void processLogFile(String logFile, IStreamListener<LogEntry> streamListener, ILineProcessor defaultLineProcessor, IStreamErrorListener errorListener) throws IOException {
         try (InputStream stream = streamFromLogFile(logFile)) {
 			IRewindableReader reader = new BufferedReaderAdapter(stream, Charsets.UTF_8, 1024);
 
 			StreamProcessor<LogEntry> streamProcessor = new StreamProcessor<LogEntry>(
 					Lists.<IMatcher<LogEntry>> newArrayList(new StandardJavaLoggingMatcher()), defaultLineProcessor,
 					streamListener,errorListener);
 
 			streamProcessor.process(reader);
 		}
     }
 
     private InputStream streamFromLogFile(String logFile) throws IOException {
         InputStream uncompressedStream = getClass().getResourceAsStream(logFile);
         if (logFile.endsWith(".gz")) {
             return Streams.compressed(uncompressedStream);
         }
         return uncompressedStream;
     }
 
     //	@Test
 	public void createStacktraceWithThreeCauses() {
 
 		Logger logger = Logger.getLogger(getClass().getName());
 
 		try {
 			logger.info("some info");
 			logger.warning("some error\nwith a second line");
 			logger.severe("some error");
 			outer();
 		} catch (Exception ex) {
 			logger.log(Level.WARNING, "outer", ex);
 		}
 	}
 
 	//	@Test
 	public void createStacktraceWithOneCauses() {
 
 		Logger logger = Logger.getLogger(getClass().getName());
 
 		try {
 			logger.info("some info");
 			logger.warning("some error\nwith a second line");
 			logger.severe("some error");
 			inner();
 		} catch (Exception ex) {
 			logger.log(Level.WARNING, "inner", ex);
 		}
 	}
 
 	void outer() {
 		try {
 			middle();
 		} catch (IllegalArgumentException iax) {
 			throw new RuntimeException("middle", iax);
 		}
 	}
 
 	void middle() {
 		try {
 			inner();
 		} catch (NullPointerException npx) {
 			throw new IllegalArgumentException(npx);
 		}
 	}
 
 	void inner() {
 		throw new NullPointerException("without any cause");
 	}
 }
