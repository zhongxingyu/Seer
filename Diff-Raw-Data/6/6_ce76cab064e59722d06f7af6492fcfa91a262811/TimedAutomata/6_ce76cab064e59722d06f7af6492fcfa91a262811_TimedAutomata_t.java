 package fr.labri.tima;
 
 //import java.io.ByteArrayOutputStream;
 //import java.io.IOException;
 //import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 //import org.jdom2.Attribute;
 //import org.jdom2.Document;
 //import org.jdom2.Element;
 //import org.jdom2.Namespace;
 //import org.jdom2.output.Format;
 //import org.jdom2.output.XMLOutputter;
 
 public class TimedAutomata<C> implements ITimedAutomata<C> {
 	
 	public static final int INFINITY = -1;
 	public static final int TIMEOUT = 0;
 	
 	State<C> _initial;
 	final Map<State<C>, List<Transition>> _transitions = new HashMap<>();
 	final Set<State<C>> _stateMap = new HashSet<>();
 	final Set<Predicate<C>> _predMap = new HashSet<>();
 
 	public void setInitial(State<C> state) {
 		_initial = state;
 	}
 
 	public void addDefaultTransition(State<C> from, State<C> to) {
 		addTransition(from, TIMEOUT, null, to);
 	}
 	
 	public void addTransition(State<C> from, Predicate<C> pred, State<C> to) {
 		addTransition(from, INFINITY, pred, to);
 	}
 	
 	void addTransition(State<C> from, int timeout, Predicate<C> pred, State<C> to) {
 		List<Transition> t = _transitions.get(from);
 		if(t == null) {
 			t = new ArrayList<Transition>();
 			_transitions.put(from, t);
 		}
 		t.add(new Transition(timeout, pred, to));
 		_predMap.add(pred);
 		_stateMap.add(from);
 		_stateMap.add(to);
 	}
 	
 	public ITimedAutomata<C> compile(State<C> init) {
 		setInitial(init);
 		return compile();
 	}
 
 	public ITimedAutomata<C> compile() {
 		if(_initial == null)
 			throw new RuntimeException("Initial state not set");
 		
 		return new Compiler().compile();
 	}
 	
 	class Compiler {
 		int[] timeouts;
 		int[] timeoutTargets;
 		int[][] transitionPredicates;
 		int[][] transitionTarget;
 		
 		Map<State<C>, List<Next>> newNodes;
 		final Map<State<C>, Integer> nodeIndex = new HashMap<State<C>, Integer>();
 		final Map<Predicate<C>, Integer> transIndex = new HashMap<Predicate<C>, Integer>();
 		
 		
 		int computeStates() {
 			int nb = _stateMap.size();
 			newNodes = new HashMap<State<C>, List<Next>>(nb);
 			for(State<C> node: _transitions.keySet()) {
 				List<Next> n = nextStates(node);
 				nb += Math.max(n.size() - 2, 0); // FIXME make a smarter method in Next
 				newNodes.put(node, n);
 			}
 			
 			return nb;
 		}
 
 		private ITimedAutomata<C> compile() {
 			int nb = computeStates();
 			allocateTables(nb);
 			
 			for(Entry<State<C>, List<Next>> e: newNodes.entrySet()) {
 				List<Next> lst = e.getValue();
 				int size = lst.size() - 1;
 				int node = getIndex(e.getKey(), nodeIndex);
 				noTimeout(node);
 				Next next = null;
 				
 				for(int i = 0; i < size; i ++) {
 					next = lst.get(i);
 					addTransitions(node, next);
 					if(i < (size-1)) {
 						State<C> state = e.getKey();
 						node = addTimeout(node, newState(state, "_" + next.deadline + "_" + i), next.deadline);
 					}
 				}
 				
 				// add what's left
 				Next n = lst.get(size);
 				if(n.deadline == TIMEOUT) {
 					if(n.size() > 0) {
 						if(next == null)
 							System.err.println("States '"+ e.getKey()+"' has only a timeout state !");
 						addTimeout(node, n.states.get(0), next == null ? 0 : next.deadline ); // FIXME does a timeout without alternatives have any sense ?
 					}
 				} else {
 					for(int i = 0; i < n.size(); i++)
 						addTransitions(node, n);
 				}
 			}
 
 			return new CompiledTimedAutomata<>(mapToStates(nodeIndex), mapToPredicates(transIndex), nodeIndex.get(_initial), transitionPredicates, timeouts, transitionTarget, timeoutTargets);
 		}
 		
 		private State<C> newState (final State<C> state, final String suffix) {
 			return new State<C>() {
 				@Override
 				public String getName() {
 					return state.getName() + suffix;
 				}
 
 				@Override
 				public List<Action<C>> getActions() {
 					return state.getActions();
 				}
 
 				@Override
 				public int getModifier() {
 					return state.getModifier();
 				}
 
 				@Override
 				public void preAction(C context, Executor<C> executor, String key) {
 					state.preAction(context, executor, key);
 				}
 
 				@Override
 				public void eachAction(C context, Executor<C> executor, String key) {
 					state.eachAction(context, executor, key);
 				}
 
 				@Override
 				public void postAction(C context, Executor<C> executor, String key) {
 					state.postAction(context, executor, key);
 				}
 
 				@Override
 				public List<ITimedAutomata<C>> getSpawnableAutomatas() {
 					return state.getSpawnableAutomatas();
 				}
 			};
 		}
 		
 		private void allocateTables(int size) {
 			timeouts = new int[size];
 			timeoutTargets = new int[size];
 			transitionPredicates = new int[size][];
 			transitionTarget = new int[size][];
 		}
 		
 		private List<Next> nextStates (State<C> state) {
 			int m;
 			List<Transition> nexts =  new ArrayList<Transition>(_transitions.get(state));
 			List<Next> result = new ArrayList<Next>();
 			int offset = 0;
 			while(true) {
 				m = nextDeadLine(nexts, offset);
 				if(m != Integer.MAX_VALUE) {
 					result.add(selectNextState(m - offset, m, nexts));
 					offset = m;
 				} else {
 					switch(nexts.size()) {
 					case 0:
 						throw new RuntimeException("Automata has no default transition for node: "+ state);
 						//		Next timeout = new Next(TIMEOUT);
 						//		result.add(timeout);
 						//	break;
 					case 1:
 						Transition target = nexts.get(0);
 						Next timeout = new Next(target.timeout);
 						timeout.add(target.state, target.predicate);
 						result.add(timeout);
 						break;
 					default:
 						Next infinites = new Next(INFINITY);
 						for(Transition t: nexts) {
 							if(t.timeout != INFINITY)
 								throw new RuntimeException("Cannot mix timeout alternative and infinite guards, neither having more than a single timeout alternative ('"+state+"': " + nexts + ")");
 							infinites.add(t.state, t.predicate);
 						}
 						result.add(infinites);
 					}
 					break;
 				}
 			}
 			
 			return result;
 		}
 		
 		Next selectNextState(int deadline, int elapsed, List<Transition> nexts) {
 			Next next = new Next(deadline);
 			
 			Iterator<Transition> it = nexts.iterator();
 			while(it.hasNext()) {
 				Transition t = it.next();
 				if(t.timeout != TIMEOUT) {
 					next.add(t.state, t.predicate);
 					if(t.timeout == elapsed)
 						it.remove();
 				}
 			}
 			return next;
 		}
 		
 		private int nextDeadLine(List<Transition> targets, int min) {
 			int m = Integer.MAX_VALUE;
 			for(Transition t: targets) {
 				if(t.timeout == Integer.MAX_VALUE)
 					throw new RuntimeException(Integer.MAX_VALUE+" is a reserved timeout value"); // FIXME rewrite without using MAX_VALUE
 					
 				if(t.timeout > min)
 					m = Math.min(m, t.timeout);
 			}
 			return m;
 		}
 		
 		private void noTimeout(int state) {
 			timeouts[state] = INFINITY;
 			timeoutTargets[state] = -1;
 		}
 		
 		private int addTimeout(int state, State<C> nextState, int deadline) {
 			timeouts[state] = deadline;
 			return timeoutTargets[state] = getIndex(nextState, nodeIndex);
 		}
 		
 		private void addTransitions(int node, Next next) {
 			int ln = next.size();
 			int[] pred = new int[ln];
 			int[] target = new int[ln];
 			transitionPredicates[node] = pred;
 			transitionTarget[node] = target;
 	
 			for(int j = 0; j < ln ; j++) {
 				pred[j] = getIndex(next.trans.get(j), transIndex);
 				target[j] = getIndex(next.states.get(j), nodeIndex);
 			}
 		}
 	}
 	
 	@Override
 	public Cursor<C> start(final ContextProvider<C> context, final String key) {
 		return new Cursor<C>() {
 			State<C> _current;
 			int _currentTime;
 			
 			@Override
 			public ITimedAutomata<C> getAutomata() {
 				return TimedAutomata.this;
 			}
 			
 			@Override
 			final public boolean next(Executor<C> executor) {
 				C ctx = context.getContext();
 
 				boolean allexpired = true;
 				State<C> timeoutTarget = null;
 				for(Transition trans: _transitions.get(_current)) {
 					int timeout = trans.timeout;
 					if(timeout == TIMEOUT)
 						timeoutTarget = trans.state;
 					else if (_currentTime < timeout || timeout == INFINITY) {
 						allexpired = false;
 						if(trans.predicate.isValid(ctx, key))
 							setState(trans.state, executor, ctx);
 					}
 				}
 
 				_currentTime ++;
 				
 				if(allexpired && timeoutTarget != null)
 					setState(timeoutTarget,  executor, ctx);
 				
 				return (_current.getModifier() & TERMINATE) > 0;
 			}
 
 			final public void setState(State<C> target, Executor<C> executor, C context) {
 				if(_current == target) {
 					target.eachAction(context, executor, key); // FIXME this is buggy ! (no self loop)
 				} else {
 					_current.postAction(context, executor, key);
 					_current = target;
 					_currentTime = 0;
 					_current.preAction(context, executor, key);
 				}
 			}
 
 			@Override
 			public String getKey() {
 				return key;
 			}
 		};
 	}
 
 	
 	@SuppressWarnings("unchecked")
 	private State<C>[] mapToStates(Map<State<C>, Integer> map) {
 		return mapTo(map, new State[map.size()]);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Predicate<C>[] mapToPredicates(Map<Predicate<C>, Integer> map) {
 		return mapTo(map, new Predicate[map.size()]);
 	}
 	
 	private <T> T[] mapTo(Map<T, Integer> map, T[] array) {
 		for(Entry<T, Integer> e: map.entrySet())
 			array[e.getValue()] = e.getKey();
 		return array;
 	}
 	
 	private <T> int getIndex(T item, Map<T, Integer> index) {
 		if(index.containsKey(item))
 			return index.get(item);
 		int idx = index.size();
 		index.put(item, idx);
 		return idx;
 	}
 	
 	final private class Transition { 
 		final Predicate<C> predicate;
 		final int timeout;
 		final State<C> state;
 		
 		Transition(int time, Predicate<C> t, State<C> s) {
 			timeout = time;
 			state = s;
 			predicate = t;
 		}
 		
 		@Override
 		public String toString() {
 			return new StringBuilder("{").append(predicate).append("/").append(timeout).append("->").append(state).append("}").toString();
 		}
 	}
 	
 	class Next implements Iterable<Entry<State<C>, Predicate<C>>> {
 		final int deadline;
 		ArrayList<State<C>> states = new ArrayList<State<C>>();
 		ArrayList<Predicate<C>> trans = new ArrayList<Predicate<C>>();
 		
 		Next(int t) {
 			deadline = t;
 		}
 		
 		void add(State<C> s, Predicate<C> t) {
 			states.add(s);
 			trans.add(t);
 		}
 		
 		@Override
 		public String toString() {
 			return new StringBuilder("<").append(deadline).append("::").append(states).append(trans).append(">").toString();
 		}
 
 		int size() {
 			return states.size();
 		}
 		
 		@Override
 		public Iterator<Entry<State<C>, Predicate<C>>> iterator() {
 			return new Iterator<Map.Entry<State<C>,Predicate<C>>>() {
 				int pos = 0;
 				@Override
 				public boolean hasNext() {
 					return pos < states.size();
 				}
 
 				@Override
 				public Entry<State<C>, Predicate<C>> next() {
 					return new Entry<State<C>, Predicate<C>>() {
 						@Override
 						public State<C> getKey() {
 							return states.get(pos);
 						}
 
 						@Override
 						public Predicate<C> getValue() {
 							return trans.get(pos);
 						}
 
 						@Override
 						public Predicate<C> setValue(Predicate<C> value) {
 							return trans.set(pos, value);
 						}
 					};
 				}
 
 				@Override
 				public void remove() {
 					states.remove(pos);
 					trans.remove(pos);
 				}
 			};
 		}
 	}
 	
 //	public Document toXML() {
 //		Element root = new Element(TimedAutomataFactory.AUTOMATA_TAG);
 //		int slen = _stateMap.size();
 //		@SuppressWarnings("unchecked")
 //		Action<C>[] states = new Action[slen];
 //		_stateMap.values().toArray(states);
 //		
 //		Namespace ns = Namespace.getNamespace("xsi", TimedAutomataFactory.XMLNS_XSI);
 //		root.addNamespaceDeclaration(ns);
 //		root.setAttribute(new Attribute("noNamespaceSchemaLocation", TimedAutomataFactory.XSI_LOCATION, ns));
 //		
 //		for(Entry<Action<C>, List<Transition>> e : _transitions.entrySet()) {
 //			Element state = xmlState(e.getKey(), states);
 //			root.addContent(state);
 //			
 //			for(Transition t: e.getValue()) {
 //				state.addContent(xmlTransition(t, states));
 //			}
 //		}
 //		
 //		for(State<C> src: _stateMap.values()) {
 //			if(_transitions.containsKey(src)) continue;
 //			root.addContent(xmlState(src, states));
 //		}
 //		
 //		return new Document(root);
 //	}
 //	
 //	private Element xmlState(State<C> src, Action<C>[] states) {
 //		Element state = new Element(TimedAutomataFactory.STATE_TAG);
 //
 //		state.setAttribute(new Attribute(TimedAutomataFactory.STATE_NAME_TAG, getNodeName(src, states)));
 //		if(src == _initial)
 //			state.setAttribute(new Attribute(TimedAutomataFactory.STATE_INITIAL_TAG, "true"));
 //		
 //		if(src instanceof UrgentState) 
 //			state.setAttribute(new Attribute(TimedAutomataFactory.STATE_URGENT_TAG, "true"));
 //
 //		String type = src.getType();
 //		if(type != null)
 //			state.setAttribute(new Attribute(TimedAutomataFactory.STATE_ACTION_TAG, type));
 //		return state;
 //	}
 //	
 //	private Element xmlTransition(Transition t, Action<C>[] states) {
 //		Element path = new Element(t.predicate == null ? TimedAutomataFactory.TIMEOUT_TAG : TimedAutomataFactory.TRANSITION_TAG );
 //		path.setAttribute(new Attribute(TimedAutomataFactory.TRANSITION_TARGET_TAG, getNodeName(t.state, states)));
 //		if(t.predicate != null) {
 //			if(t.timeout > 0)
 //				path.setAttribute(new Attribute(TimedAutomataFactory.TRANSITION_TIMEOUT_TAG, Integer.toString(t.timeout)));
 //			path.setAttribute(new Attribute(TimedAutomataFactory.TRANSITION_PREDICATE_TAG, t.predicate.getType()));
 //		}
 //		return path;
 //	}
 	
 //	public final <S extends OutputStream> S xmlToStream(S stream) throws IOException {
 //		new XMLOutputter(Format.getPrettyFormat()).output(toXML(), stream);
 //		return stream;
 //	}
 
 //	public final String toString() {
 //		try {
 //			return xmlToStream(new ByteArrayOutputStream()).toString();
 //		} catch (IOException e) {
 //			return new StringBuilder("<error>").append(e.getMessage()).append("</error>").toString();
 //		}
 //	}
 //
 	public String toString() {
 		return DotRenderer.toDot(this, "G");
 	}
 	
 	
 	@Override
 	final public State<C> getInitialState() {
 		return _initial;
 	}
 
 	@Override
 	final public void setInitialState(State<C> initial) {
 		_initial = initial;
 	}
 
 
 	@Override
 	final public State<C>[] getStates() {
 		@SuppressWarnings("unchecked")
 		State<C>[] a = new State[_stateMap.size()]; 
 		return _stateMap.toArray(a);
 	}
 
 	@Override
 	public State<C>[] getFollowers(State<C> src) {
 		List<Transition> t = _transitions.get(src);
		int size = t == null ? 0 : t.size();
 		@SuppressWarnings("unchecked")
		State<C>[] states = new State[size];
		for(int i = 0; i < size; i ++)
 			states[i] = t.get(i).state;
 		return states;
 	}
 
 	@Override
 	public int getTimeout(State<C> src, State<C> dst) {
 		List<Transition> t = _transitions.get(src);
 		for(Transition trans: t)
 			if(trans.state == dst)
 				return trans.timeout;
 		
 		return TimedAutomata.TIMEOUT;
 	}
 
 	@Override
 	public Predicate<C> getPredicate(State<C> src, State<C> dst) {
 		List<Transition> t = _transitions.get(src);
 		for(Transition trans: t)
 			if(trans.state == dst)
 				return trans.predicate;
 		
 		return null;
 	}
 }
