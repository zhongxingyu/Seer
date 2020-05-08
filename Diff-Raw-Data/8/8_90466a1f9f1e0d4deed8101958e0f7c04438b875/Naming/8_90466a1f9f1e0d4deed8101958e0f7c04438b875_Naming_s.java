 // Released under GPLv2 or later. See http://www.gnu.org/ for details.
 package tags.proto.name;
 
 import tags.proto.LayerService;
 import tags.proto.Query;
 import tags.proto.QueryProcessor;
 import tags.proto.LocalViewFactory;
 import tags.util.ScoreInferer;
 
 import tags.util.exec.MessageReceiver;
 import tags.util.exec.MessageRejectedException;
 import tags.util.exec.Services;
 import tags.util.exec.TaskResult;
 import tags.util.exec.TaskService;
 import java.io.IOException;
 
 import tags.proto.MultiParts;
 import tags.util.Maps;
 import java.util.Collections;
 
 import tags.proto.AddressScheme;
 import tags.proto.DataSources;
 import tags.proto.LocalTGraph;
 import tags.proto.FullTGraph;
 import tags.proto.TGraph.Lookup;
 import tags.proto.TGraph.NodeLookup;
 import tags.util.Maps.U2Map;
 import tags.util.Union.U2;
 import tags.util.Arc;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
 ** DOCUMENT.
 **
 ** @param <T> Type of tag
 ** @param <A> Type of address
 ** @param <U> Type of node-attribute
 ** @param <W> Type of arc-attribute
 ** @param <S> Type of score
 */
 public class Naming<T, A, U, W, S>
 extends LayerService<Query<?, T>, QueryProcessor<?, T, A, U, W, S, ?>, Naming.State, Naming.MRecv> {
 
 	public enum State { NEW, AWAIT_SEEDS, IDLE }
 	public enum MRecv { REQ_MORE_DATA, RECV_SEED_G }
 
 	final protected TGraphComposer<T, A, U, W, S> mod_tgr_cmp;
 	final protected AddressSchemeBuilder<T, A, U, W> mod_asc_bld;
 
 	final protected DataSources<A, LocalTGraph<T, A, U, W>, S> source;
 
 	protected FullTGraph<T, A, U, W> graph;
 	volatile protected AddressScheme<T, A, W> scheme;
 
 	public Naming(
 		Query<?, T> query,
 		QueryProcessor<?, T, A, U, W, S, ?> proc,
 		TGraphComposer<T, A, U, W, S> mod_tgr_cmp,
 		AddressSchemeBuilder<T, A, U, W> mod_asc_bld,
 		LocalViewFactory<A, LocalTGraph<T, A, U, W>> view_fac,
 		ScoreInferer<S> score_inf
 	) {
 		super("N", query, proc, State.NEW);
 		if (mod_tgr_cmp == null) { throw new NullPointerException(); }
 		if (mod_asc_bld == null) { throw new NullPointerException(); }
 		this.mod_tgr_cmp = mod_tgr_cmp;
 		this.mod_asc_bld = mod_asc_bld;
 		this.source = new DataSources<A, LocalTGraph<T, A, U, W>, S>(view_fac, score_inf);
 	}
 
 	@Override public synchronized void recv(MRecv msg) throws MessageRejectedException {
 		super.recv(msg);
 		switch (state) {
 		case NEW:
 			switch (msg) {
 			case REQ_MORE_DATA:
 				sendAtomic(State.AWAIT_SEEDS, Services.defer(proc.contact, tags.proto.cont.Contact.MRecv.REQ_MORE_DATA));
 
 				return;
 			default: throw mismatchMsgRejEx(state, msg);
 			}
 		case AWAIT_SEEDS:
 			switch (msg) {
 			case RECV_SEED_G:
 				// init data structures etc. reset everything
 				source.setSeeds(proc.contact.getSeedTGraphs());
 
 				execute(new Runnable() {
 					@Override public void run() {
 						addSeedTag();
 						updateAddressScheme();
 					}
 				}, State.IDLE,
 				Services.defer(proc.routing, tags.proto.route.Routing.MRecv.RECV_ADDR_SCH));
 
 				return;
 			default: throw mismatchMsgRejEx(state, msg);
 			}
 		case IDLE:
 			switch (msg) {
 			case REQ_MORE_DATA:
 				if (scheme.isIncomplete() /* TODO HIGH or maybe if its distance is above some threshold */) {
 					// complete the next tag in the address scheme
 					execute(new Runnable() {
 						@Override public void run() {
 							addTagAndComplete(scheme.getIncomplete());
 							updateAddressScheme();
 						}
 					}, Services.defer(proc.routing, tags.proto.route.Routing.MRecv.RECV_ADDR_SCH));
 
 				} else if (scheme.getNearestTGraph() != null) {
 					// add a tgraph as a data source
 					execute(new Runnable() {
 						@Override public void run() {
 							addDataSourceAndComplete(scheme.getNearestTGraph());
 							updateAddressScheme();
 						}
 					}, Services.defer(proc.routing, tags.proto.route.Routing.MRecv.RECV_ADDR_SCH));
 
 				} else {
 					// nothing to do, pass request onto contact layer
 					proc.contact.recv(tags.proto.cont.Contact.MRecv.REQ_MORE_DATA);
 				}
 				return;
 			default: throw mismatchMsgRejEx(state, msg);
 			}
 		}
 	}
 
 	public AddressScheme<T, A, W> getAddressScheme() {
 		return scheme;
 	}
 
 	protected void addDataSourceAndComplete(A addr) {
 		LocalTGraph<T, A, U, W> view = source.useSource(addr);
 		Set<T> old_complete = getCompletedTags();
 
 		TaskService<Lookup<T, A>, U2Map<T, A, W>, IOException> srv = proc.newTGraphService();
 		TaskService<NodeLookup<T, A>, U, IOException> srv_node = proc.newTGraphNodeService();
 		Set<NodeLookup<T, A>> submitted = new HashSet<NodeLookup<T, A>>();
 		Map<T, U2Map<T, A, W>> outgoing = new HashMap<T, U2Map<T, A, W>>();
 
 		try {
 			for (T tag: old_complete) {
 				srv.submit(Services.newTask(Lookup.make(addr, tag)));
 				NodeLookup<T, A> lku = NodeLookup.makeT(addr, tag);
 				srv_node.submit(Services.newTask(lku));
 				submitted.add(lku);
 			}
 
 			do {
 				// handle downloaded node-attributes
 				while (srv_node.hasComplete()) {
 					TaskResult<NodeLookup<T, A>, U, IOException> res = srv_node.reclaim();
 					U2<T, A> node = res.getKey().node;
 
 					if (node.isT0()) {
 						T tag = node.getT0();
 						view.setNodeAttrT(tag, res.getValue());
 						if (outgoing.containsKey(tag)) {
 							// pick up the outgoing map too, if it's there
 							view.setOutgoingT(tag, outgoing.remove(tag));
 						}
 					} else {
 						view.setNodeAttrG(node.getT1(), res.getValue());
 					}
 				}
 
 				// handle downloaded arc-maps
 				while (srv.hasComplete()) {
 					TaskResult<Lookup<T, A>, U2Map<T, A, W>, IOException> res = srv.reclaim();
 					T tag = res.getKey().tag;
 					U2Map<T, A, W> out = res.getValue();
 
 					if (view.nodeMap().K0Map().containsKey(tag)) {
 						// if we've already added the node-attribute
 						view.setOutgoingT(tag, out);
 					} else {
 						// otherwise store it and pick it up later, when we have the node-attribute
 						outgoing.put(tag, out);
 					}
 
 					// retrieve node-weights of all out-neighbours
 					for (U2<T, A> u2: out.keySet()) {
 						NodeLookup<T, A> lku = NodeLookup.make(view.addr, u2);
 						// don't submit same node twice
 						if (submitted.contains(lku)) { continue; }
 						submitted.add(lku);
 						srv_node.submit(Services.newTask(lku));
 					}
 				}
 
 				Thread.sleep(proc.interval);
 			} while (srv.hasPending() || srv_node.hasPending());
 
 			assert outgoing.isEmpty();
 			assert getCompletedTags().equals(old_complete);
 
 		} catch (InterruptedException e) {
 			throw new UnsupportedOperationException(e); // FIXME HIGH
 		} catch (IOException e) {
 			throw new RuntimeException(e); // FIXME HIGH
 		} finally {
 			srv.close();
 			srv_node.close();
 		}
 	}
 
 	protected void addTagAndComplete(T tag) {
 		Map<A, LocalTGraph<T, A, U, W>> local = source.localMap();
 
 		TaskService<Lookup<T, A>, U2Map<T, A, W>, IOException> srv = proc.newTGraphService();
 		TaskService<NodeLookup<T, A>, U, IOException> srv_node = proc.newTGraphNodeService();
 		Set<NodeLookup<T, A>> submitted = new HashSet<NodeLookup<T, A>>();
 
 		if (getCompletedTags().contains(tag)) { return; }
 
 		try {
 			// retrieve outgoing arcs of tag, in all sources
 			for (LocalTGraph<T, A, U, W> view: local.values()) {
 				if (view.nodeMap().K0Map().containsKey(tag)) {
 					srv.submit(Services.newTask(Lookup.make(view.addr, tag)));
 				} else {
 					assert view.getCompletedTags().contains(tag);
 				}
 			}
 
 			do {
 				// handle downloaded node-attributes
 				while (srv_node.hasComplete()) {
 					TaskResult<NodeLookup<T, A>, U, IOException> res = srv_node.reclaim();
 					LocalTGraph<T, A, U, W> view = local.get(res.getKey().tgr);
 					view.setNodeAttr(res.getKey().node, res.getValue());
 				}
 
 				// handle downloaded arc-maps
 				while (srv.hasComplete()) {
 					TaskResult<Lookup<T, A>, U2Map<T, A, W>, IOException> res = srv.reclaim();
 					LocalTGraph<T, A, U, W> view = local.get(res.getKey().tgr);
 					U2Map<T, A, W> out = res.getValue();
 
 					assert res.getKey().tag.equals(tag);
 					assert view.nodeMap().K0Map().containsKey(tag);
 					view.setOutgoingT(tag, out);
 
 					// retrieve node-weights of all out-neighbours
 					for (U2<T, A> u2: out.keySet()) {
 						NodeLookup<T, A> lku = NodeLookup.make(view.addr, u2);
 						// don't submit same node twice
 						if (submitted.contains(lku)) { continue; }
 						submitted.add(lku);
 						srv_node.submit(Services.newTask(lku));
 					}
 				}
 
 				Thread.sleep(proc.interval);
 			} while (srv.hasPending() || srv_node.hasPending());
 
 			try{
 				assert getCompletedTags().contains(tag);
 			} catch (AssertionError e) {
 				for (LocalTGraph<T, A, U, W> view: local.values()) {
 					System.out.println(view.addr + " " + view.getCompletedTags() + " " + view.nodeMap() + "\n" + view.arcMap());
 				}
 			}
 
 		} catch (InterruptedException e) {
 			throw new UnsupportedOperationException(e); // FIXME HIGH
 		} catch (IOException e) {
 			throw new RuntimeException(e); // FIXME HIGH
 		} finally {
 			srv.close();
 			srv_node.close();
 		}
 	}
 
 	protected void addSeedTag() {
 		Map<A, LocalTGraph<T, A, U, W>> local = source.localMap();
 		T tag = query.tag;
 
 		TaskService<NodeLookup<T, A>, U, IOException> srv_node = proc.newTGraphNodeService();
 
 		try {
 			// retrieve outgoing arcs of tag, in all sources
 			for (LocalTGraph<T, A, U, W> view: local.values()) {
 				srv_node.submit(Services.newTask(NodeLookup.makeT(view.addr, tag)));
 			}
 
 			do {
 				// handle downloaded node-attributes
 				while (srv_node.hasComplete()) {
 					TaskResult<NodeLookup<T, A>, U, IOException> res = srv_node.reclaim();
 					LocalTGraph<T, A, U, W> view = local.get(res.getKey().tgr);
 					view.setNodeAttr(res.getKey().node, res.getValue());
 				}
 
 				Thread.sleep(proc.interval);
 			} while (srv_node.hasPending());
 
 		} catch (InterruptedException e) {
 			throw new UnsupportedOperationException(e); // FIXME HIGH
 		} catch (IOException e) {
 			throw new RuntimeException(e); // FIXME HIGH
 		} finally {
 			srv_node.close();
 		}
 	}
 
 	protected void updateAddressScheme() {
 		source.calculateScores();
 		graph = composeTGraph();
 		scheme = makeAddressScheme();
 	}
 
 	/**
 	** Get the set of tags which have been completed in all the tgraphs that we
 	** are using as a data source (ie. source.localMap().values()).
 	*/
 	protected Set<T> getCompletedTags() {
 		if (source.localMap().isEmpty()) { return Collections.emptySet(); }
 
 		// return intersection of all sources.getCompletedTags(),
 		Iterator<LocalTGraph<T, A, U, W>> it = source.localMap().values().iterator();
 		assert it.hasNext();
 		Set<T> complete = new HashSet<T>(it.next().getCompletedTags());
 		while (it.hasNext()) { complete.retainAll(it.next().getCompletedTags()); }
 
 		return complete;
 	}
 
 	/**
 	** Make a new {@link #graph} from {@link #source}. To be called whenever
 	** the {@linkplain #getCompletedTags() completed set} changes.
 	*/
 	protected FullTGraph<T, A, U, W> composeTGraph() {
 		// iterates through all nodes present in every source
 		U2Map<T, A, U> node_map = Maps.uniteDisjoint(new HashMap<T, U>(), new HashMap<A, U>());
 		for (U2<T, A> node: Maps.domain(MultiParts.iterTGraphNodeMaps(source.localMap().values()))) {
 			node_map.put(node, mod_tgr_cmp.composeNode(source.localScoreMap(), node));
 		}
 
 		// iterates through all arcs present in every source
 		U2Map<Arc<T, T>, Arc<T, A>, W> arc_map = Maps.uniteDisjoint(new HashMap<Arc<T, T>, W>(), new HashMap<Arc<T, A>, W>());
 		for (U2<Arc<T, T>, Arc<T, A>> arc: Maps.domain(MultiParts.iterTGraphArcMaps(source.localMap().values()))) {
 			// filter out tgraphs that are already in-use as a data source
 			if (arc.isT1() && source.localMap().containsKey(arc.getT1().dst)) { continue; }
 			arc_map.put(arc, mod_tgr_cmp.composeArc(source.localScoreMap(), arc));
 		}
 
 		return new FullTGraph<T, A, U, W>(node_map, arc_map);
 	}
 
 	/**
 	** Make a new {@link #scheme} from {@link #graph}. To be called whenever
 	** the latter changes, ie. after {@link #composeTGraph()}.
 	*/
 	protected AddressScheme<T, A, W> makeAddressScheme() {
 		return mod_asc_bld.buildAddressScheme(graph, getCompletedTags(), query.tag);
 	}
 
 }
