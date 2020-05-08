 package obr;
 
 import moca.graphs.BipartedGraph;
 import moca.graphs.vertices.VertexCollection;
 import moca.graphs.vertices.Vertex;
 import moca.graphs.vertices.VertexArrayList;
 import moca.graphs.edges.Edge;
 import moca.graphs.edges.NeighbourEdge;
 import moca.graphs.edges.UndirectedNeighboursLists;
 import moca.graphs.edges.IllegalEdgeException;
 
 import java.lang.Iterable;
 import java.lang.String;
 import java.lang.Integer;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 
 /**
  * Represents a conjunction of atoms.<br />
  *
  * <p>
  * <b>Internal structure :</b><br />
  * The internal structure is a biparted graph where edges are implemented by neighbours lists.<br />
  * The first partition of vertices represents the atoms. Only their predicates are stored into this partition.
  * But the vertex class provides a global ID which will can be used to identify an atom.<br />
  * The second partition represents the terms. A term may be either a variable or a constant.
  * Since terms are also stored into vertices, they have a global ID.<br />
  * The edges are used to connect atoms with terms. And their value is the position of the term inside the atom.
  * A well-built atom conjunction will ensure that there is one edge for each position of each atom predicate and that 
  * they are added in their position ordering.<br/>
  * </p>
  *
  * <p>
  * <b>About IDs :</b><br />
  * Each vertex (thus, each atom and each term) has a different ID, which can be get by Vertex.getID() method.
  * But since the internal structure provides a bipartition, atoms can be enumerated from 0 to getNbAtoms()-1, and terms
  * from 0 to getNbTerms()-1.<br />
  * To convert a global ID to a local one apply the following computation : 
  * <ul><li>if atom : local ID = global ID</li>
  * <li>if term : local ID = global ID - getNbAtoms()</li></ul>
  * </p>
  *
  */
 public class AtomConjunction implements Iterable<Atom> {
 
 	/** 
 	 * Represents the separator between atoms when the conjunction is under a String format. 
 	 */
 	public static final String ATOM_SEPARATOR	=	new String(";");
 
 	/** 
 	 * Represents the separator between terms when an atom is under a String format.
 	 * @see Atom#TERM_SEPARATOR
 	 */
 	public static final String TERM_SEPARATOR	=	Atom.TERM_SEPARATOR;	
 	
 	/**
 	 * Represents the begin of a term list when an atom is under a String format.
 	 * @see Atom#BEGIN_TERM_LIST
 	 */
 	public static final String BEGIN_TERM_LIST	=	Atom.BEGIN_TERM_LIST;
 
 	/**
 	 * Represents the end of a term list when an atom is under a String format.
 	 * @see Atom#END_TERM_LIST
 	 */
 	public static final char END_TERM_LIST		=	Atom.END_TERM_LIST.charAt(Atom.END_TERM_LIST.length()-1);
 
 	/**
 	 * Default constructor.<br />
 	 * The atom conjunction will be empty.
 	 */
 	public AtomConjunction() {
 		try {
 			_graph = new BipartedGraph<Object,Integer>(
 				new VertexArrayList<Object>(),
 				new VertexArrayList<Object>(), 
 				new UndirectedNeighboursLists<Integer>());
 		}
 		catch (Exception e) { }
 	}
 
 	/** 
 	 * Constructor from a string representation of an atom conjunction.
 	 * @param stringRepresentation The atom conjunction under a String format.
 	 * @see #fromString(String) 
 	 */
 	public AtomConjunction(String stringRepresentation) {
 		try {
 			_graph = new BipartedGraph<Object,Integer>(
 				new VertexArrayList<Object>(),
 				new VertexArrayList<Object>(), 
 				new UndirectedNeighboursLists<Integer>());
 			fromString(stringRepresentation);
 		}
 		catch (Exception e) { }
 	}
 
 	/**
 	 * Copy constructor.
 	 * @param toCopy The atom conjunction to be copied.
 	 */
 	public AtomConjunction(AtomConjunction toCopy) {
 		try {
 			_graph = new BipartedGraph<Object,Integer>(toCopy._graph);
 		}
 		catch (Exception e) { }
 		
 	}
 
 	/**
 	 * Number of atoms getter.
 	 * @return The current number of atoms.
 	 */
 	public int getNbAtoms() {
 		return _graph.getNbVerticesInFirstSet();
 	}
 
 	/**
 	 * Number of terms getter.
 	 * @return The current number of terms.
 	 */
 	public int getNbTerms() {
 		return _graph.getNbVerticesInSecondSet();
 	}
 
 	/**
 	 * Allows to know if a vertex is an atom.
 	 * @param v The vertex to be checked.
 	 * @return True if v is an atom, false otherwise.
 	 */
 	public boolean isAtom(Vertex<Object> v) {
 		if (v == null)
 			return false;
 		if (v.getID() < getNbAtoms())
 			return true;
 		return false;
 	}
 
 	/**
 	 * Allows to know if a vertex is a term.
 	 * @param v The vertex to be checked.
 	 * @return True if v is a term, false otherwise.
 	 */
 	public boolean isTerm(Vertex<Object> v) {
 		if (v == null)
 			return false;
 		if (v.getID() < getNbAtoms())
 			return false;
 		return true;
 	}
 
 	/**
 	 * Creates a new atom instance for the atom whose id matches i.<br />
 	 * The structure does not contain atom instances, that is why only a new instance can be generated.
 	 * Furthermore, any modifications on this atom instance will <b>NOT</b> be applied on the atom representation in the
 	 * conjunction.
 	 * @param i The id of the atom.
 	 * @return A new instance of atom class.
 	 * @throws NoSuchElementException If the id does not match in the atom conjunction.
 	 */
 	public Atom getAtom(int i) throws NoSuchElementException {
 		if ((i >= getNbAtoms()) || (i < 0))
 			throw new NoSuchElementException();
 		Atom atom = new Atom((Predicate)(_graph.getVertex(i).getValue()));
 		for (Iterator<NeighbourEdge<Integer> > iterator = _graph.neighbourIterator(i) ; iterator.hasNext() ; ) {
 			NeighbourEdge<Integer> edge = iterator.next();
 			atom.set(edge.getValue(),(Term)(_graph.getVertex(edge.getIDV()).getValue()));
 		}
 		return atom;
 	}
 
 	/**
 	 * Fullfills the atom conjunction from its string representation.<br />
 	 * The string must be under the following format (otherwise an exception will be thrown) :
 	 * <code>&lt;atom 0&gt;ATOM_SEPARATOR&lt;atom 1&gt;ATOM_SEPARATOR...ATOM_SEPARATOR&lt;atom n&gt;</code>
 	 * Where atoms must be under the following format :
 	 * <code>&lt;predicate label&gt;BEGIN_TERM_LIST&lt;term 0&gt;TERM_SEPARATOR&lt;term 1&gt;TERM_SEPARATOR...TERM_SEPARATOR
 	 * &lt;term m&gt;END_TERM_LIST</code><br />
 	 * The predicate labels and arity are used to check their equality, and only labels for terms equality.
 	 * @param str The atom conjunction under a string format.
 	 * @throws UnrecognizedStringFormatException Whenever the string passed as parameter does not follow a good format.
 	 */
 	protected void fromString(String str) throws UnrecognizedStringFormatException {
 		String[] sub1 = null;
 		String[] sub2 = null;
 		String[] sub3 = null;
 		sub1 = str.split(ATOM_SEPARATOR);
 		Predicate predicate = null;
 		int arity = 0;
 		int atomID = -1;
 		int termID = -1;
 		for (int i = 0 ; i < sub1.length ; i++) {
 			sub2 = sub1[i].split(BEGIN_TERM_LIST);
 			switch (sub2.length) {
 				case 1 :	// if there is no begin term list char we suppose the predicate arity to be null
 					arity = 0;
 					break;
 				case 2 :
 					if (sub2[1].charAt(sub2[1].length()-1) != END_TERM_LIST)
 						throw new UnrecognizedStringFormatException();
 					sub2[1] = sub2[1].substring(0,sub2[1].length()-1);	// the last char is to be removed
 					sub3 = sub2[1].split(TERM_SEPARATOR);
 					arity = sub3.length;
 					break;
 				default :
 					throw new UnrecognizedStringFormatException();
 			}
 			atomID = addAtom(new Predicate(sub2[0],arity));
 			for (int j = 0 ; j < arity ; j++) {
 				termID = addTerm(sub3[j]);
 				try {
 					_graph.addEdge(atomID,termID,new Integer(j));
 				}
 				catch (Exception e) {
 					throw new UnrecognizedStringFormatException();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Converts the atom conjunction into a String format.<br />
 	 * Note that this is just a convenient method for toString(getNbAtoms()).
 	 * @return A well-formatted string representation.
 	 * @see #toString(int)
 	 */
 	public String toString() {
 		return toString(getNbAtoms());
 	}
 
 	/**
 	 * Converts a subset of the atom conjunction into a String format.<br />
 	 * Only atoms whose id is between 0 and the parameter will be converted.
 	 * This method is usefull for inheritance which specializes some of atoms.
 	 * @param nbAtoms The number of atoms to be converted.
 	 */
 	public String toString(int nbAtoms) {
 		StringBuilder string = new StringBuilder();
 		if (nbAtoms > getNbAtoms())
 			nbAtoms = getNbAtoms();
 		for (int i = 0 ; i < nbAtoms -1; i++) {
 			string.append(getAtom(i));
 			string.append(ATOM_SEPARATOR);
 		}
 		string.append(getAtom(nbAtoms-1));
 		return string.toString();
 	}
 	public String toStringExcluding(int exclude) {
 		StringBuilder string = new StringBuilder();
 		final int nbAtoms = getNbAtoms();
		for (int i = 0 ; i < nbAtoms; i++) {
 			if (i != exclude) {
 				string.append(getAtom(i));
 				string.append(ATOM_SEPARATOR);
 			}
 		}
		string.deleteCharAt(string.length()-1);
 		return string.toString();
 	}
 
 
 
 	/**
 	 * Adds an atom into the atom conjunction.<br />
 	 * The internal representation of an atom is a graph vertex with predicate value, that is why only a predicate is
 	 * needed.
 	 * @param predicate The predicate of the new atom.
 	 * @return The global index of the new atom in the conjunction.
 	 */
 	public int addAtom(Predicate predicate) {
 		_graph.addInFirstSet(predicate);
 		return _graph.getNbVerticesInFirstSet() - 1;
 	}
 
 	public void removeCleanlyAtom(int atomID) throws NoSuchElementException {
 		final Vertex<Object> atom = getVertexAtom(atomID);
 		int neighbourAtomID;
 		int currentTerm;
 		final int nbTerms = _graph.getNbNeighbours(atomID);
 		for (currentTerm = 0 ; currentTerm < nbTerms ; currentTerm++) {
 			neighbourAtomID = getVertexTermFromAtom(atomID,currentTerm).getID();
 			if (_graph.getNbNeighbours(neighbourAtomID) == 1) {
 				_graph.removeVertex(neighbourAtomID);
 				currentTerm--;
 			}
 		}
 		removeAtom(atomID);
 	}
 
 	protected void removeAtom(int atomID) throws NoSuchElementException {
 		_graph.removeVertex(atomID);
 	}
 
 	public int getNbConstants() {
 		int result = 0;
 		final int nbTerms = getNbTerms();
 		for (int i = 0 ; i < nbTerms ; i++)
 			if (getTerm(i).isConstant())
 				result++;
 		return result;
 	}
 
 	public int getNbVariables() {
 		int result = 0;
 		final int nbTerms = getNbTerms();
 		for (int i = 0 ; i < nbTerms ; i++)
 			if (getTerm(i).isVariable())
 				result++;
 		return result;
 	}
 
 	public VertexCollection<Object> constants() {
 		VertexArrayList<Object> result = new VertexArrayList<Object>();
 		final int nbTerms = getNbTerms();
 		for (int i = 0 ; i < nbTerms ; i++)
 			if (getTerm(i).isConstant())
 				result.add(getVertexTerm(i));
 		return result;
 	}
 
 	public VertexCollection<Object> variables() {
 		VertexArrayList<Object> result = new VertexArrayList<Object>();
 		final int nbTerms = getNbTerms();
 		for (int i = 0 ; i < nbTerms ; i++)
 			if (getTerm(i).isVariable())
 				result.add(getVertexTerm(i));
 		return result;
 	}
 
 	public VertexCollection<Object> domain(){
 		return _graph.getSecondSet();
 	}
 
 	public VertexCollection<Object> constants(int atomID) {
 		if ((atomID > getNbAtoms()) || (atomID < 0))
 			return null;
 		VertexArrayList<Object> result = new VertexArrayList<Object>();
 		Vertex<Object> term;
 		for (NeighbourIterator iterator = vertexTermIteratorFromAtom(atomID) ; iterator.hasNext() ; ) {
 			term = iterator.next();
 			if ((((Term)(term.getValue())).isConstant())
 			&& (!result.contains(term)))
 				result.add(term);
 		}
 		return result;
 	}
 
 	public VertexCollection<Object> variables(int atomID) {
 		if ((atomID > getNbAtoms()) || (atomID < 0))
 			return null;
 		VertexArrayList<Object> result = new VertexArrayList<Object>();
 		Vertex<Object> term;
 		for (NeighbourIterator iterator = vertexTermIteratorFromAtom(atomID) ; iterator.hasNext() ; ) {
 			term = iterator.next();
 			if ((((Term)(term.getValue())).isVariable())
 			&& (!result.contains(term)))
 				result.add(term);
 		}
 		return result;
 	}
 
 	
 	/**
 	 * Adds a term into the atom conjunction.<br />
 	 * Note that the term will not be connected to any atom. Use addEdge method to make these connections.<br />
 	 * Furthermore, if the term is already in the conjunction, no new term will be added.
 	 * @param term The new term to add.
 	 * @return The term index.
 	 */
 	public int addTerm(Term term) {
 		for (int i = getNbAtoms() ; i < _graph.getNbVertices() ; i++) {
 			if (((Term)(getVertex(i).getValue())).getLabel().compareTo(term.getLabel()) == 0) {
 				return i;
 			}
 		}
 		_graph.addInSecondSet(term);
 		return _graph.getNbVertices() - 1;
 	}
 
 	/**
 	 * Adds a term into the atom conjunction from its label.
 	 * @param termLabel The new term label to add.
 	 * @return The term index.
 	 * @see #addTerm(Term)
 	 */
 	public int addTerm(String termLabel) {
 		for (int i = getNbAtoms() ; i < _graph.getNbVertices() ; i++) {
 			if (((Term)(getVertex(i).getValue())).getLabel().compareTo(termLabel) == 0) {
 				return i;
 			}
 		}
 		_graph.addInSecondSet(new Term(termLabel));
 		return _graph.getNbVertices() - 1;
 	}
 	
 	/**
 	 * Adds an edge between an atom and a term.<br />
 	 * If the arity of the atom id is n, then the position must be in [0;n-1].
 	 * @param atomID The id of the atom to connect.
 	 * @param termID The id of the term to connect.
 	 * @param position The position of the term in the atom.
 	 * @return True if success, false otherwise.
 	 */
 	public boolean addEdge(int atomID, int termID, Integer position) {
 		try {
 			_graph.addEdge(atomID,termID,position);
 			return true;
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Add an atom from another atom conjunction.<br />
 	 * If the first parameter is not really an atom vertex, this method will silently fail.
 	 * @param atom The atom vertex to add.
 	 * @param source The atom conjunction where to find atom vertex connections.
 	 */
 	public void addAtom(Vertex<Object> atom, AtomConjunction source) {
 		if (source.isAtom(atom)) {
 			Iterator<NeighbourEdge<Integer> > termIterator = source.neighbourIterator(atom.getID());
 			NeighbourEdge<Integer> edge = null;
 			int termID = -1;
 			addAtom((Predicate)(atom.getValue()));
 			while (termIterator.hasNext()) {
 				edge = termIterator.next();
 				termID = addTerm(source.getTerm(edge.getIDV()-source.getNbAtoms()));
 				addEdge(getNbAtoms()-1,termID,new Integer(edge.getValue()));
 			}
 		}
 	}
 
 	/** 
 	 * Access to the value of a vertex from its global ID.
 	 * @param i The vertex global id.
 	 * @return The value of the vertex.
 	 * @throws NoSuchElementException If i does not match in the atom conjunction.
 	 */
 	public Object get(int i) throws NoSuchElementException {
 		return _graph.get(i);
 	}
 
 	/**
 	 * Access to the predicate at the specified index.<br />
 	 * Convenient method for getVertexAtom(int).
 	 * @param i The predicate vertex local id.
 	 * @return The predicate.
 	 * @throws NoSuchElementException If i does not match in the atom conjunction.
 	 * @see #getVertexAtom(int)
 	 */
 	public Predicate getPredicate(int i) throws NoSuchElementException {
 		return (Predicate)(getVertexAtom(i).getValue());
 	}
 
 	/** 
 	 * Access to a vertex from its global ID.
 	 * @param i The vertex global id.
 	 * @return The vertex whose id matches i.
 	 * @throws NoSuchElementException If i does not match in the atom conjunction.
 	 */
 	public Vertex<Object> getVertex(int i) throws NoSuchElementException {
 		return _graph.getVertex(i);
 	}
 
 	/**
 	 * Access to a vertex term from its local ID.
 	 * @param i The vertex term local id.
 	 * @return The vertex term whose local id matches i.
 	 * @throws NoSuchElementException If i does not match any vertex term local id.
 	 */
 	public Vertex<Object> getVertexTerm(int i) throws NoSuchElementException {
 		return _graph.getInSecondSet(i);
 	}
 	
 	/** 
 	 * Access to a vertex atom from its local ID.
 	 * @param i The vertex atom local id.
 	 * @return The vertex atom whose local id matches i.
 	 * @throws NoSuchElementException If i does not match any vertex atom local id.
 	 */
 	public Vertex<Object> getVertexAtom(int i) throws NoSuchElementException {
 		return _graph.getInFirstSet(i);
 	}
 	
 	/** 
 	 * Access to a term from its local ID. <br />
 	 * Convenient method for getVertexTerm(int).
 	 * @param i The vertex term local id.
 	 * @return The term whose vertex local id matches i.
 	 * @throws NoSuchElementException If i does not match any vertex term local id.
 	 */
 	public Term getTerm(int i) throws NoSuchElementException {
 		return (Term)getVertexTerm(i).getValue();
 	}
 
 	/** 
 	 * Access to a term from an atom and its position into this atom.<br />
 	 * @param atomID The id of the atom where to get the term.
 	 * @param termIndex The position of the term inside the atom.
 	 * @return The term corresponding.
 	 * @throws NoSuchElementException If atomID does not match, or if termIndex is greater than or equals to the
 	 * corresponding predicate arity.
 	 */
 public Term getTermFromAtom(int atomID, int termIndex) throws NoSuchElementException {
 		return (Term)_graph.getNeighbourValue(atomID,termIndex);
 	}
 
 	/**
 	 * Access to a vertex term from an atom and its position into this atom.<br />
 	 * @param atomID The id of the atom where to get the term.
 	 * @param termIndex The position of the term inside the atom.
 	 * @return The vertex term corresponding.
 	 * @throws NoSuchElementException If atomID does not match, or if termIndex is greater than or equals to the
 	 * corresponding predicate arity.
 	 */
 	public Vertex<Object> getVertexTermFromAtom(int atomID, int termIndex) throws NoSuchElementException {
 		return _graph.getNeighbour(atomID,termIndex);
 	}
 
 	/**
 	 * Returns a new atom conjunction which is a clone of a subset of the current atom conjunction.<br />
 	 * The two parameters will be used as the range of atoms to be used.
 	 * All terms connected to one of these atoms will also be added into the clone.
 	 * Thus, all edges which were connected to the atoms in the current atom conjunction will also be present in the
 	 * clone.<br />
 	 * Note that the first atom will be included but not the last. I.e., if this method is used on 0,getNbAtom(), all
 	 * the atom conjunction will be cloned.
 	 * @param beginAtomID The ID of the first atom (included).
 	 * @param endAtomID The ID of the last atom (not included).
 	 * @return The new atom conjunction if the parameters are not absurds, false otherwise.
 	 */
 	public AtomConjunction subAtomConjunction(int beginAtomID, int endAtomID) {
 		if ((beginAtomID > endAtomID) || (endAtomID > getNbAtoms()) || (beginAtomID < 0))
 			return null;
 		AtomConjunction result = new AtomConjunction();
 		NeighbourEdge<Integer> edge = null;
 		int termID = -1;
 		for (int i = beginAtomID ; i < endAtomID ; i++) {
 			result._graph.addInFirstSet(getPredicate(i).clone());
 		}
 		for (int i = beginAtomID ; i < endAtomID ; i++) {
 			for (Iterator<NeighbourEdge<Integer> > iterator = _graph.neighbourIterator(i) ; iterator.hasNext() ; ) {
 				edge = iterator.next();
 				termID = result.addTerm(getTerm(edge.getIDV()-getNbAtoms()).clone());
 				try { result._graph.addEdge(i-beginAtomID,termID,new Integer(edge.getValue())); }
 				catch (IllegalEdgeException e) { }
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Clones the atom conjunction into a copy instance.
 	 * @param copy The instance where to clone.
 	 * @return The clone.
 	 */
 	public AtomConjunction cloneIn(AtomConjunction copy) {
 		for (Iterator<Object> iterator = _graph.firstIterator() ; iterator.hasNext() ; ) {
 			copy.addAtom(((Predicate)(iterator.next())).clone());
 		}
 		for (Iterator<Object> iterator = _graph.secondIterator() ; iterator.hasNext() ; ) {
 			copy.addTerm(((Term)(iterator.next())).clone());
 		}
 		Edge<Integer> edge = null;
 		for (Iterator<Edge<Integer> > iterator = _graph.edgeIterator() ; iterator.hasNext() ; ) {
 			edge = iterator.next();
 			copy.addEdge(edge.getIDU(),edge.getIDV(),new Integer(edge.getValue()));
 		}
 		return copy;
 	}
 
 	/**
 	 * Clones the atom conjunction.<br />
 	 * Convenient method for cloneIn(new AtomConjunction()).
 	 * @return A clone of the atom conjunction.
 	 */
 	@Override
 	public AtomConjunction clone() {
 		return cloneIn(new AtomConjunction());
 	}
 
 	/**
 	 * Access to the iterator over the atoms.
 	 * @return An itertor over the atoms.
 	 * @see .AtomIterator
 	 */
 	public AtomIterator iterator() {
 		return new AtomIterator();
 	}
 
 	/**
 	 * Access to the iterator over the vertex atoms.
 	 * @return An iterator over the vertices representing atoms.
 	 */
 	public Iterator<Vertex<Object> > vertexAtomIterator() {
 		return _graph.firstVertexIterator();
 	}
 
 	/**
 	 * Access to the iterator over the vertex terms.
 	 * @return An iterator over the vertices representing terms.
 	 */
 	public Iterator<Vertex<Object> > vertexTermIterator() {
 		return _graph.secondVertexIterator();
 	}
 
 	/**
 	 * Access to an iterator over the vertex terms which are connected to the specified atom.<br />
 	 * If the atom conjunction is well built, the terms will be iterated in their positions ordering.
 	 * @param atomID The global id of the atom.
 	 * @return An iterator over the vertex terms connected to the atom.
 	 * @see .NeighbourIterator
 	 */
 	public NeighbourIterator vertexTermIteratorFromAtom(int atomID) {
 		return new NeighbourIterator(_graph.neighbourIterator(atomID));
 	}
 
 	/**
 	 * Access to an iterator over the vertex atoms which are connected to the specified term.
 	 * @param termID The global id of the term.
 	 * @return An iterator over the vertex atoms connected to the term.
 	 * @see .NeighbourIterator
 	 */
 	public NeighbourIterator vertexAtomIteratorFromTerm(int termID) {
 		return new NeighbourIterator(_graph.neighbourIterator(termID));
 	}
 
 	/**
 	 * Access to an iterator over the edges from a specific vertex.
 	 * @param vertexID The global id of the vertex which is the source of edges.
 	 * @return An iterator over the edges connected to the vertex.
 	 */
 	public Iterator<NeighbourEdge<Integer> > neighbourIterator(int vertexID) {
 		return _graph.neighbourIterator(vertexID);
 	}
 
 	/**
 	 * The structure of the atom conjunction.<br />
 	 * The first vertices partition is used to store atoms (the vertices contain only predicates).
 	 * And the second one to store terms.
 	 * An edge represents a connection between an atom and a term, and its value the position of the term in the atom.
 	 */
 	protected BipartedGraph<Object,Integer> _graph = null;
 
 
 	/**
 	 * Iterator over the atoms of the conjunction.<br />
 	 * Since atoms are not stored into the internal structure, the remove operation is not supported.
 	 */
 	public class AtomIterator implements Iterator<Atom> {
 
 		/**
 		 * Allows to know if the next call to next() will fail.
 		 * @return True if the next call to next() will not fail, false otherwise.
 		 */
 		public boolean hasNext() {
 			return _current < getNbAtoms();
 		}
 
 		/**
 		 * Access to the next atom.
 		 * @return The next atom.
 		 * @throws NoSuchElementException If there is no more atom to be iterated over.
 		 */ 
 		public Atom next() throws NoSuchElementException {
 			if (!hasNext())
 				throw new NoSuchElementException();
 			Atom result = getAtom(_current);
 			_current++;
 			return result;
 		}
 
 		/**
 		 * This operation is not supported.
 		 * @throws UnsupportedOperationException Always.
 		 */
 		public void remove() throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
 	
 		/** Current atom index. */
 		protected int _current = 0;
 		
 	}
 
 
 	/**
 	 * Iterator over the vertices connected to a specific one.
 	 */
 	protected class NeighbourIterator implements Iterator<Vertex<Object> > {
 		
 		/**
 		 * Constructor.
 		 * @param iterator The iterator over the neighbour edges.
 		 */
 		public NeighbourIterator(Iterator<NeighbourEdge<Integer> > iterator) {
 			_iterator = iterator;
 		}
 
 		/**
 		 * Allows to know if the next call to next() method will not fail.
 		 * @return True if the next call to next() will not fail, false otherwise.
 		 */
 		@Override
 		public boolean hasNext() {
 			return _iterator.hasNext();
 		}
 		
 		/**
 		 * Iterates and returns the next vertex.
 		 * @return The next vertex.
 		 * @throws NoSuchElementException If there is no more vertex to be iterated.
 		 */
 		@Override
 		public Vertex<Object> next() throws NoSuchElementException {
 			return getVertex(_iterator.next().getIDV());
 		}
 
 		/**
 		 * This operation is not supported.
 		 * @throws UnsupportedOperationException Always.
 		 */
 		@Override
 		public void remove() throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
 
 		/** An iterator over the neighbour edges. */
 		private Iterator<NeighbourEdge<Integer> > _iterator;
 
 	};
 
 
 };
 
