 package ch.unibe.scg.cells.hadoop;
 
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.nio.ByteBuffer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.inject.Qualifier;
 
 import org.apache.hadoop.conf.Configuration;
 import org.junit.Test;
 
 import ch.unibe.scg.cells.Cell;
 import ch.unibe.scg.cells.Cells;
 import ch.unibe.scg.cells.CellsModule;
 import ch.unibe.scg.cells.Codec;
 import ch.unibe.scg.cells.InMemoryPipeline;
import ch.unibe.scg.cells.InMemoryShuffler;
 import ch.unibe.scg.cells.LocalExecutionModule;
 import ch.unibe.scg.cells.Mapper;
 import ch.unibe.scg.cells.OneShotIterable;
 import ch.unibe.scg.cells.Pipeline;
 import ch.unibe.scg.cells.Sink;
 import ch.unibe.scg.cells.Source;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.io.CharStreams;
 import com.google.common.primitives.Ints;
 import com.google.common.primitives.Longs;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.TypeLiteral;
 import com.google.protobuf.ByteString;
 
 @SuppressWarnings("javadoc")
 public final class HadoopPipelineTest {
 	private final static ByteString FAMILY = ByteString.copyFromUtf8("f");
 
 	static class Act {
 		final int number;
 		final String content;
 
 		Act(int number, String content) {
 			this.number = number;
 			this.content = content;
 		}
 	}
 
 	static class ActCodec implements Codec<Act> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<Act> encode(Act act) {
 			return Cell.make(ByteString.copyFrom(Ints.toByteArray(act.number)), ByteString.copyFromUtf8("t"),
 					ByteString.copyFromUtf8(act.content));
 		}
 
 		@Override
 		public Act decode(Cell<Act> encoded) throws IOException {
 			return new Act(Ints.fromByteArray(encoded.getRowKey().toByteArray()), encoded.getCellContents()
 					.toStringUtf8());
 		}
 	}
 
 	private static class Word {
 		final String word;
 		final int act;
 		final int pos;
 
 		Word(String word, int act, int pos) {
 			this.word = word;
 			this.act = act;
 			this.pos = pos;
 		}
 	}
 
 	private static class WordCodec implements Codec<Word> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<Word> encode(Word s) {
 			ByteBuffer col = ByteBuffer.allocate(2 * Ints.BYTES);
 			col.mark();
 			col.putInt(s.act);
 			col.putInt(s.pos);
 			col.reset();
 			return Cell.make(
 					ByteString.copyFromUtf8(s.word),
 					ByteString.copyFrom(col),
 					ByteString.EMPTY);
 		}
 
 		@Override
 		public Word decode(Cell<Word> encoded) throws IOException {
 			ByteBuffer col = encoded.getColumnKey().asReadOnlyByteBuffer();
 			int nAct = col.getInt();
 			int pos = col.getInt();
 			return new Word(encoded.getRowKey().toStringUtf8(), nAct, pos);
 		}
 	}
 
 	private static class WordCount {
 		final String word;
 		final long count;
 
 		WordCount(String word, long count) {
 			this.word = word;
 			this.count = count;
 		}
 
 		@Override
 		public String toString() {
 			return word + " " + count;
 		}
 	}
 
 	private static class WordCountCodec implements Codec<WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<WordCount> encode(WordCount s) {
 			return Cell.make(ByteString.copyFromUtf8(s.word),
 					ByteString.copyFromUtf8("1"),
 					ByteString.copyFrom(Longs.toByteArray(s.count)));
 		}
 
 		@Override
 		public WordCount decode(Cell<WordCount> encoded) throws IOException {
 			return new WordCount(encoded.getRowKey().toStringUtf8(),
 					Longs.fromByteArray(encoded.getCellContents().toByteArray()));
 		}
 	}
 
 	private static class WordParseMapper implements Mapper<Act, Word> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() { }
 
 		@Override
 		public void map(Act first, OneShotIterable<Act> row, Sink<Word> sink) throws IOException,
 				InterruptedException {
 			for(Act act : row) {
 				Matcher matcher = Pattern.compile("\\w+").matcher(act.content);
 				while (matcher.find()) {
 					sink.write(new Word(matcher.group(), first.number, matcher.start()));
 				}
 			}
 		}
 	}
 
 	private static class WordAdderMapper implements Mapper<Word, WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() { }
 
 		@Override
 		public void map(Word first, OneShotIterable<Word> row, Sink<WordCount> sink)
 				throws IOException, InterruptedException {
 			int len = Iterables.size(row);
 			sink.write(new WordCount(first.word, len));
 		}
 	}
 
 	private static class WordFilter implements Mapper<WordCount, WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() throws IOException { }
 
 		@Override
 		public void map(WordCount first, OneShotIterable<WordCount> row, Sink<WordCount> sink) throws IOException,
 				InterruptedException {
 			for (WordCount wc : row) {
 				if (wc.word.equals("your")) {
 					sink.write(wc);
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testHadoopWordcount() throws IOException, InterruptedException {
 		TableAdmin tableAdmin = Guice.createInjector(new UnibeModule()).getInstance(TableAdmin.class);
 
 		try(Table<Act> in = tableAdmin.createTemporaryTable(FAMILY);
 				Table<WordCount> eff = tableAdmin.createTemporaryTable(FAMILY)) {
 			Module tab = new CellsModule() {
 				@Override
 				protected void configure() {
 					installTable(
 							In.class,
 							new TypeLiteral<Act>() {},
 							ActCodec.class,
 							new HBaseStorage(), new HBaseTableModule<>(in));
 					installTable(
 							Eff.class,
 							new TypeLiteral<WordCount>() {},
 							WordCountCodec.class,
 							new HBaseStorage(), new HBaseTableModule<>(eff));
 				}
 			};
 
 			Injector injector = Guice.createInjector(tab, new UnibeModule());
 			try (Sink<Act> sink = injector.getInstance(Key.get(new TypeLiteral<Sink<Act>>() {}, In.class))) {
 				Iterable<Act> acts = readActsFromDisk();
 				for (Act act : acts) {
 					sink.write(act);
 				}
 			}
 
 			run(HadoopPipeline.fromTableToTable(injector.getInstance(Configuration.class), in, eff));
 
 			try(Source<WordCount> src = injector.getInstance(Key.get(new TypeLiteral<Source<WordCount>>() {}, Eff.class))) {
 				WordCount wc = Iterables.getOnlyElement(Iterables.getOnlyElement(src));
 				assertThat(wc.word, is("your"));
 				assertThat(wc.count, is(239L));
 			}
 		}
 	}
 
 	@Test
 	public void testInMemoryWordCount() throws IOException, InterruptedException {
 			try (InMemoryPipeline<Act, WordCount> pipe
 					= Guice.createInjector(new LocalExecutionModule())
 							.getInstance(InMemoryPipeline.Builder.class)
						.make(InMemoryShuffler.copyFrom(readActsFromDisk(), new ActCodec()))) {
 			run(pipe);
 			for (Iterable<WordCount> wcs : Cells.decodeSource(pipe.lastEfflux(), new WordCountCodec())) {
 				for (WordCount wc : wcs) {
 					if (wc.word.equals("your")) {
 						assertThat(wc.count, is(239L));
 					}
 				}
 			}
 		}
 	}
 
 	void run(Pipeline<Act, WordCount> pipeline) throws IOException, InterruptedException {
 		pipeline.influx(new ActCodec())
 			.map(new WordParseMapper())
 			.shuffle(new WordCodec())
 			.map(new WordAdderMapper())
 			.shuffle(new WordCountCodec())
 			.mapAndEfflux(new WordFilter(), new WordCountCodec());
 	}
 
 	@Qualifier
 	@Target({ FIELD, PARAMETER, METHOD })
 	@Retention(RUNTIME)
 	public static @interface In {}
 
 	@Qualifier
 	@Target({ FIELD, PARAMETER, METHOD })
 	@Retention(RUNTIME)
 	public static @interface Eff {}
 
 	static Iterable<Act> readActsFromDisk() throws IOException {
 		ImmutableList.Builder<Act> ret = ImmutableList.builder();
 		String richard = CharStreams.toString(new InputStreamReader(HadoopPipelineTest.class.getResourceAsStream(
 				"richard-iii.txt"), Charsets.UTF_8));
 
 		String[] actStrings = richard.split("\\bACT\\s[IVX]+");
 		for (int i = 0; i < actStrings.length; i++) {
 			ret.add(new Act(i, actStrings[i]));
 		}
 		return ret.build();
 	}
 }
