 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.store;
 
 import tags.proto.PTable;
 import tags.util.Union.U2;
 import tags.util.Maps.U2Map;
 import java.util.Map;
 
 import java.io.IOException;
 
 /**
 ** DOCUMENT.
 **
 ** @param <I> Type of identity
 ** @param <T> Type of tag
 ** @param <A> Type of address
 ** @param <U> Type of node-attribute
 ** @param <W> Type of arc-attribute
 ** @param <S> Type of score
 ** @param <Z> Type of identity-score
 */
 public interface StoreControl<I, T, A, U, W, S, Z> {
 
 	/**
 	** Retrieves the immediate trusted neighbours of a given identity, each
 	** mapped to their score rating.
 	**
 	** This method blocks until the operation is complete.
 	*/
 	public Map<I, Z> getFriends(I id) throws IOException;
 
 	/**
 	** Retrieves the {@link PTable} for a given identity.
 	**
 	** This method blocks until the operation is complete.
 	*/
 	public PTable<A, S> getPTable(I id) throws IOException;
 
 	/**
 	** Retrieves the out-neighbours (and the weights of the out-arcs) for a
 	** given source tag, in the given {@link TGraph}.
 	**
 	** This method blocks until the operation is complete. It returns {@code
 	** null} if the given node is not defined in the data structure.
 	**
 	** @param addr Address of the {@link TGraph}.
 	*/
 	public U2Map<T, A, W> getTGraphOutgoing(A addr, T src) throws IOException;
 
 	/**
 	** Retrieves the node-attribute for a given tag or tgraph, in the given
 	** {@link TGraph}.
 	**
 	** This method blocks until the operation is complete. It returns {@code
 	** null} if the given node is not defined in the data structure.
 	**
 	** @param addr Address of the {@link TGraph}.
 	*/
 	public U getTGraphNodeAttr(A addr, U2<T, A> node) throws IOException;
 
 	/**
 	** Retrieves the out-neighbours (and the weights of the out-arcs) for a
 	** given source tag, in the given {@link Index}.
 	**
 	** This method blocks until the operation is complete. It returns {@code
 	** null} if the given node is not defined in the data structure.
 	**
 	** @param addr Address of the {@link Index}.
 	*/
 	public U2Map<A, A, W> getIndexOutgoing(A addr, T src) throws IOException;
 
 }
