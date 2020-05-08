 package ch.unibe.scg.cells;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.Iterables;
 import com.google.protobuf.ByteString;
 
 @SuppressWarnings("javadoc")
 public final class InMemorySourceTest {
 	private InMemorySource<Void> s;
 
 	@Before
 	public void setUp() {
 		s =  InMemorySource.make(Arrays.asList(
 			Arrays.asList(
 				new Cell<Void>(ByteString.copyFromUtf8("aa0b"), ByteString.copyFromUtf8("1"), ByteString.EMPTY),
 				new Cell<Void>(ByteString.copyFromUtf8("aaab"), ByteString.copyFromUtf8("1"), ByteString.EMPTY),
 				new Cell<Void>(ByteString.copyFromUtf8("aaac"), ByteString.copyFromUtf8("1"), ByteString.EMPTY),
 				new Cell<Void>(ByteString.copyFromUtf8("aaac"), ByteString.copyFromUtf8("2"), ByteString.EMPTY)),
 			Arrays.asList(
 				new Cell<Void>(ByteString.copyFromUtf8("aaad"), ByteString.copyFromUtf8("0"), ByteString.EMPTY)),
 			Collections.<Cell<Void>> emptyList(),
 			Arrays.asList(
 				new Cell<Void>(ByteString.copyFromUtf8("aaae"), ByteString.copyFromUtf8("0"), ByteString.EMPTY),
 				new Cell<Void>(ByteString.copyFromUtf8("aab"), ByteString.copyFromUtf8("0"), ByteString.EMPTY)),
 			Arrays.asList(
 				new Cell<Void>(ByteString.copyFrom(new byte[] {-1, 'a', 'd'}), ByteString.copyFromUtf8("0"), ByteString.EMPTY))));
 	}
 
 	@Test
 	public void testIterator() {
 		Iterable<OneShotIterable<Cell<Void>>> rows = Cells.breakIntoRows(s);
 		StringBuilder b = new StringBuilder();
		b.append("[");
 		for (OneShotIterable<Cell<Void>> row : rows) {
 			b.append(Iterables.toString(row));
 			b.append(", ");
 		}
		b.replace(b.length() - 2, b.length(), ""); // to remove a trailing space
		b.append("]");
 		assertThat(b.toString(),
 				is("[[[[97, 97, 48, 98]]{[49]}], "
 					+ "[[[97, 97, 97, 98]]{[49]}], "
 					+ "[[[97, 97, 97, 99]]{[49]}, [[97, 97, 97, 99]]{[50]}], "
 					+ "[[[97, 97, 97, 100]]{[48]}], "
 					+ "[[[97, 97, 97, 101]]{[48]}], "
 					+ "[[[97, 97, 98]]{[48]}], "
 					+ "[[[-1, 97, 100]]{[48]}]]"));
 	}
 
 	@Test
 	public void testReadRowFF() {
 		Iterable<Cell<Void>> row = s.readRow(ByteString.copyFrom(new byte[] {-1}));
 		assertThat(Iterables.toString(row), is("[[[-1, 97, 100]]{[48]}]"));
 
 		row = s.readRow(ByteString.copyFromUtf8("aaa"));
 		assertThat(Iterables.toString(row),
 				is("[[[97, 97, 97, 98]]{[49]}, [[97, 97, 97, 99]]{[49]}, "
 					+ "[[97, 97, 97, 99]]{[50]}, [[97, 97, 97, 100]]{[48]}, [[97, 97, 97, 101]]{[48]}]"));
 	}
 
 	@Test
 	public void testReadRowBeyondShards() {
 		Iterable<Cell<Void>> row = s.readRow(ByteString.copyFrom(new byte[] {-1, -1}));
 		assertThat(Iterables.isEmpty(row), is(true));
 
 		row = s.readRow(ByteString.copyFrom(new byte[] {-1, -1, 0}));
 		assertThat(Iterables.isEmpty(row), is(true));
 	}
 
 	@Test
 	public void emptyStore() {
 		InMemorySource<Void> source = InMemorySource.make(Collections.<List<Cell<Void>>> emptyList());
 		assertThat(Iterables.isEmpty(source), is(true));
 		assertThat(Iterables.isEmpty(source.readRow(ByteString.copyFromUtf8("bla"))), is(true));
 
 		source = InMemorySource.make(Arrays.<List<Cell<Void>>> asList(
 				Collections.<Cell<Void>> emptyList(), Collections.<Cell<Void>> emptyList()));
 		assertThat(Iterables.isEmpty(source), is(true));
 		assertThat(Iterables.isEmpty(source.readRow(ByteString.copyFromUtf8("bla"))), is(true));
 	}
 
 	@Test
 	public void readFromSameShard() {
 		Iterable<Cell<Void>> row = s.readRow(ByteString.copyFromUtf8("aaab"));
 		assertThat(Iterables.toString(row), is("[[[97, 97, 97, 98]]{[49]}]"));
 
 		row = s.readRow(ByteString.copyFromUtf8("aa0b"));
 		assertThat(Iterables.toString(row), is("[[[97, 97, 48, 98]]{[49]}]"));
 	}
 
 	@Test
 	public void testAbsent() {
 		Iterable<Cell<Void>> row = s.readRow(ByteString.copyFromUtf8("aa0ba"));
 		assertThat(Iterables.isEmpty(row), is(true));
 
 		row = s.readRow(ByteString.copyFromUtf8("qq"));
 		assertThat(Iterables.isEmpty(row), is(true));
 
 		row = s.readRow(ByteString.copyFromUtf8("00"));
 		assertThat(Iterables.isEmpty(row), is(true));
 
 		row = s.readRow(ByteString.copyFromUtf8("aaba"));
 		assertThat(Iterables.isEmpty(row), is(true));
 	}
 
 	@Test
 	public void testReadColumn() {
 		assertThat(Iterables.getOnlyElement(s.readColumn(ByteString.copyFromUtf8("2"))).getRowKey().toStringUtf8(),
 				is("aaac"));
 		assertThat(Iterables.size(s.readColumn(ByteString.EMPTY)), is(8));
 
 		Iterable<Cell<Void>> col0 = s.readColumn(ByteString.copyFromUtf8("0"));
 		assertThat(Iterables.get(col0, 0).getRowKey().toStringUtf8(), is("aaad"));
 		assertThat(Iterables.get(col0, 1).getRowKey().toStringUtf8(), is("aaae"));
 		assertThat(Iterables.get(col0, 2).getRowKey().toStringUtf8(), is("aab"));
 		assertThat(Iterables.get(col0, 3).getRowKey(), is(ByteString.copyFrom(new byte[] {-1, 'a', 'd'})));
 		assertThat(col0.toString(), Iterables.size(col0), is(4));
 
 		assertTrue(Iterables.isEmpty(s.readColumn(ByteString.copyFromUtf8("3"))));
 		assertTrue(Iterables.isEmpty(s.readColumn(ByteString.copyFromUtf8("/"))));
 	}
 }
