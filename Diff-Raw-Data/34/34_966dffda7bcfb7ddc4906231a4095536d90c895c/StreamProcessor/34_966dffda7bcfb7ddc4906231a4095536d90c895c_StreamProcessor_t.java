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
 package de.flapdoodle.logparser;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import de.flapdoodle.logparser.streamlistener.DoesNotExpectAnyErrorListener;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 public class StreamProcessor<T> implements IStreamProcessor<T> {
 
 	private final ImmutableList<? extends IMatcher<T>> _matcher;
 	private final ILineProcessor _defaultLineProcessor;
 	private final IStreamListener<T> _listener;
     private final IStreamErrorListener _errorListener;
 
     private static final int OFFSET = 1000;
 
     public StreamProcessor(Collection<? extends IMatcher<T>> matcher, ILineProcessor defaultLineProcessor,
 			IStreamListener<T> listener,IStreamErrorListener errorListener) {
         _errorListener = errorListener;
         _matcher = ImmutableList.copyOf(matcher);
 		_defaultLineProcessor = defaultLineProcessor;
 		_listener = listener;
 	}
     public StreamProcessor(Collection<? extends IMatcher<T>> matcher, ILineProcessor defaultLineProcessor,
                            IStreamListener<T> listener) {
         this(matcher,defaultLineProcessor,listener,new DoesNotExpectAnyErrorListener());
     }
 
         @Override
 	public void process(IRewindableReader reader) throws IOException {
 		boolean eof = false;
 
        do {
            Optional<IMatch<T>> match = firstMatch(reader,new EmtpyBackBuffer());
            if (match.isPresent()) {
 				process(reader, match);
 			} else {
 				Optional<String> nextLine = reader.nextLine();
 				if (nextLine.isPresent()) {
 					_defaultLineProcessor.processLine(nextLine.get());
 				} else {
 					eof = true;
 				}
 			}
 		} while (!eof);
 	}
 
 	private void process(IRewindableReader reader, Optional<IMatch<T>> match) throws IOException {
 		List<String> nonMatchingLines = Lists.newArrayList();
 		IBackBuffer backBuffer=new BackBufferFromList(nonMatchingLines);
 
 		boolean readDone = false;
 		int lines = 0;
 		int showNumberAt = lines + OFFSET;
 
 		do {
 			Optional<IMatch<T>> nextMatch = firstMatch(reader,backBuffer);
 			if (nextMatch.isPresent()) {
                 processMatch(match, nonMatchingLines);
                match=nextMatch;
 				nonMatchingLines = Lists.newArrayList();
 			} else {
 				Optional<String> nextLine = reader.nextLine();
 				if (nextLine.isPresent()) {
 					nonMatchingLines.add(nextLine.get());
 				} else {
                     processMatch(match, nonMatchingLines);
                    match=nextMatch;
 					nonMatchingLines = Lists.newArrayList();
 					readDone = true;
 				}
 			}
 			lines++;
 			if (lines >= showNumberAt) {
 				System.out.print("\r" + lines);
 				showNumberAt = lines + OFFSET;
 			}
 		} while (!readDone);
 	}
 
     private void processMatch(Optional<IMatch<T>> match, List<String> nonMatchingLines) throws IOException {
         try {
             T result = match.get().process(nonMatchingLines);
             _listener.entry(result);
         } catch (StreamProcessException stp) {
             _errorListener.error(stp);
         }
     }
 
     private Optional<IMatch<T>> firstMatch(IRewindableReader reader,IBackBuffer backBuffer) throws IOException {
 		reader.setMarker();
 		for (IMatcher<T> matcher : _matcher) {
 			Optional<IMatch<T>> match = matcher.match(reader,backBuffer);
 			if (match.isPresent()) {
 				return match;
 			} else {
 				reader.jumpToMarker();
 			}
 		}
 		return Optional.absent();
 	}
 	
 	static class BackBufferFromList implements IBackBuffer {
 
 		private final List<String> _lines;
 
 		public BackBufferFromList(List<String> lines) {
 			_lines = lines;
 		}
 		
 		@Override
 		public ImmutableList<String> lastLines() {
 			return ImmutableList.copyOf(Lists.reverse(_lines));
 		}
 		
 	}
 	
 	static class EmtpyBackBuffer implements IBackBuffer {
 
 		@Override
 		public ImmutableList<String> lastLines() {
 			return ImmutableList.of();
 		}
 		
 	}
 }
