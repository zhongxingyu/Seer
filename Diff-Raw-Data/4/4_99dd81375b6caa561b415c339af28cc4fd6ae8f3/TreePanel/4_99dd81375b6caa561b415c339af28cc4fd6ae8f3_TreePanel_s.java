 package webApplication.grafica;
 
 import java.awt.event.ActionEvent;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.DropMode;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.TransferHandler;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import javax.swing.undo.UndoManager;
 import javax.swing.undo.UndoableEdit;
 import javax.swing.undo.UndoableEditSupport;
 
 import webApplication.business.Componente;
 import webApplication.business.ComponenteMolteplice;
 import webApplication.business.ComponenteSemplice;
 import webApplication.business.Immagine;
 import webApplication.business.Link;
 import webApplication.business.Testo;
 
 /**
  * Il pannello contenente l'albero
  * 
  * @author Andrea
  */
 public class TreePanel extends JPanel implements TreeWillExpandListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8834919285623008305L;
 
 	private static final String ROOT = "";
 	private static final int LIMITUNDO = 100;
 
 	private DisabledNode rootNode;
 	private DefaultTreeModel model;
 	private JTree tree;
 	private TreeTransferHandler th;
 	
 	private UndoableEditSupport undoSupport;
 	private UndoManager undoManager;
 
 	/**
 	 * Il costruttore del pannello contenente l'albero.
 	 */
 	public TreePanel() {
 		init();
 	}
 
 	/**
 	 * Inizializza il pannello contenente l'albero
 	 */
 	private void init() {
 		th = new TreeTransferHandler(this);
 		rootNode = new DisabledNode(ROOT);
 		model = new DefaultTreeModel(rootNode);
 		tree = new JTree(model);
 		tree.setShowsRootHandles(false); // rende visibile il nodo root
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); // solo un nodo alla volta e selezionabile
 //		tree.setCellRenderer(new CustomCellRenderer());
 		tree.setDragEnabled(true);
 		tree.setDropMode(DropMode.ON_OR_INSERT);
 		tree.setTransferHandler(th);
 
 		// NOTA: TreeWillExpandListener ascolta la volont� di
 		// collassare/espandere prima che l'azione sia realmente compiuta e
 		// posso quindi fermarla
 		//TODO vedere perch� a volte non funziona
 		tree.addTreeWillExpandListener(this); // il listener per catturare la volont� di collassare/espandere un elemento
 		
 		tree.setEditable(false); // fa in modo che l'albero non sia editabile
 		tree.setAutoscrolls(true);
 		javax.swing.ToolTipManager.sharedInstance().registerComponent(tree);
 		setMappings(tree);
 
 		// pannello contenente il tree
 		JScrollPane scrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		add(scrollPane);
 		
 		// inizializzo il gestore delle azioni di undo/redo
 		undoManager = new UndoManager();
 		undoManager.setLimit(LIMITUNDO);
 		undoSupport = new UndoableEditSupport();
 		undoSupport.addUndoableEditListener(new UndoAdapter());
 	}
 
 	/**
 	 * Get the undomanager used in TreePanel
 	 * 
 	 * @return The UndoManager
 	 */
 	protected UndoManager getUndoManager() {
 		return undoManager;
 	}
 
 	/**
 	 * Cancella tutti gli elementi inseriti nell'albero
 	 */
 	protected void clear() {
 		rootNode.removeAllChildren();
 		model.nodeStructureChanged(rootNode);
 		model.reload();
 	}
 
 	/**
 	 * Rimuove un nodo dall'albero. Per geneare la corretta azione di undo/redo
 	 * passare attraverso il listener RemoveAction
 	 * 
 	 * @param node
 	 *            Il nodo da rimuovere
 	 */
 	private void removeNode(DisabledNode node) {
 		DisabledNode parent = (DisabledNode) node.getParent();
 		if (!parent.isRoot()) {
 			ComponenteMolteplice compParent = (ComponenteMolteplice) parent.getUserObject();
 			compParent.cancellaOpzione(parent.getIndex(node));
 		}
 		model.removeNodeFromParent(node);
 	}
 
 	/**
 	 * Aggiunge un nodo all'albero. Per generare la corretta azione di undo/redo
 	 * passare attraverso il listener AddAction
 	 * 
 	 * @param parent
 	 *            Il genitore del nodo che si vuole inserire
 	 * @param node
 	 *            Il nodo da inserire
 	 */
 	private void addNode(DisabledNode parent, DisabledNode node) {
 		if (!parent.isRoot()) {
 			Componente compElem = (Componente) parent.getUserObject();
 			if (!compElem.isSimple()) {
 				((ComponenteMolteplice) compElem).aggiungiOpzione((ComponenteSemplice) node.getUserObject(), parent.getChildCount());
 			}
 		}
 		model.insertNodeInto(node, parent, parent.getChildCount());
 		if (!((Componente) node.getUserObject()).isSimple()) {
 			parent = node;
 			ComponenteMolteplice comps = (ComponenteMolteplice) node.getUserObject();
 			for (int i = 0; i < comps.getOpzioni().size(); i++) {
 				DisabledNode newNode = new DisabledNode((ComponenteSemplice) comps.getOpzione(i));
 				model.insertNodeInto(newNode, parent, i);
 				newNode.setAllowsChildren(false);
 			}
 		} else {
 			node.setAllowsChildren(false);
 		}
 	}
 
 	/**
 	 * Aggiunge le azioni di copia/taglia/incolla all'albero
 	 * 
 	 * @param tree	L'albero
 	 */
 	private void setMappings(JTree tree) {
 		// NOTA: non ho pi� bisogno di settare la mappa dato che � codata nello
 		// shortcut degli elementi del menu.
 		// Dovrei farlo solo se non ho acceleratori settati.
 		// Passo ridondante visto che sono azioni settate nei menuitem 
 		ActionMap map = tree.getActionMap();
 		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
 		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
 		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
 
 		// disabilito le azioni di default di ctrl+x ctrl+c ctrl+v per avere un
 		// controllo sugli eventi
 		tree.getInputMap().put(KeyStroke.getKeyStroke("control C"), "none");
 		tree.getInputMap().put(KeyStroke.getKeyStroke("control X"), "none");
 		tree.getInputMap().put(KeyStroke.getKeyStroke("control V"), "none");
 	}
 
 	/**
 	 * Metodo per ottenere l'oggetto JTree interno al pannello
 	 * 
 	 * @return L'albero
 	 */
 	public JTree getTree() {
 		return tree;
 	}
 
 	/**
 	 * Verifica se l'albero � vuoto
 	 * 
 	 * @return True o False a seconda se l'albero � vuoto o no
 	 */
 	public boolean isEmpty() {
 		if ((((DisabledNode) model.getRoot()).getChildCount()) != 0) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Verifica se un nome � gi� usato nell'albero
 	 * 
 	 * @param name
 	 *            Il nome da verificare
 	 * @return True o False a seconda se il nome � gi� stato usato o meno
 	 */
 	public boolean nameExists(String name) {
 		for (int i = 0; i < rootNode.getChildCount(); i++) {
 			if (nameExists(name, (DisabledNode) rootNode.getChildAt(i))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Verifica se un nome � gi� stato usato in un nodo specifico
 	 * 
 	 * @param name
 	 *            Il nome da verificare
 	 * @param node
 	 *            Il nodo in cui verificare
 	 * @return True o False a seconda se il nome � gi� stato usato o meno
 	 */
 	private boolean nameExists(String name, DisabledNode node) {
 		Componente comp = (Componente) node.getUserObject();
 		if (comp.getNome().equalsIgnoreCase(name)) {
 			return true;
 		}
 		if (node.getAllowsChildren()) {
 			for (int i = 0; i < node.getChildCount(); i++) {
 				if (nameExists(name, (DisabledNode) node.getChildAt(i))) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Ritorna i path dei nodi aventi il nome indicato
 	 * 
 	 * @param name
 	 *            Il nome da ricercare
 	 * @return Il vettore dei path degli elementi con quel nome
 	 */
 	public Vector<TreePath> getPathForName(String name) {
 		Vector<TreePath> elementsPath = new Vector<TreePath>();
 		TreePath path = null;
 		for (int i = 0; i < rootNode.getChildCount(); i++) {
 			Vector<TreeNode[]> nodePath = getPathForName(name, (DisabledNode) rootNode.getChildAt(i));
 			if (nodePath != null) {
 				for (int j = 0; j < nodePath.size(); j++) {
 					path = new TreePath(nodePath.elementAt(j));
 					elementsPath.add(path);
 				}
 			}
 		}
 		return elementsPath;
 	}
 
 	/**
 	 * Ritorna i path dei nodi figli del nodo specificato aventi il nome
 	 * indicato
 	 * 
 	 * @param name
 	 *            Il nome da cercare
 	 * @param node
 	 *            Il node da cui cominciare la ricerca
 	 * @return Il vettore dei path degli elementi con quel nome figli del nodo
 	 *         indicato
 	 */
 	private Vector<TreeNode[]> getPathForName(String name,
 			DisabledNode node) {
 		Vector<TreeNode[]> elPath = new Vector<TreeNode[]>();
 		Componente comp = (Componente) node.getUserObject();
 		if (comp.getNome().equalsIgnoreCase(name)) {
 			elPath.add(node.getPath());
 		}
 		if (node.getAllowsChildren()) {
 			for (int i = 0; i < node.getChildCount(); i++) {
 				elPath.addAll(getPathForName(name, (DisabledNode) node.getChildAt(i)));
 			}
 		}
 		return elPath;
 	}
 
 	/**
 	 * Fornisce la lista dei Componenti Semplici di primo livello dell'albero ed
 	 * il relativo indice
 	 * 
 	 * @return Il vettore contenente tali nodi
 	 */
 	protected HashMap<Integer, DisabledNode> getFirstLevelSimpleComponent() {
 		// NOTA: la costruisco dinamicamente per evitare di doverla aggiornare
 		// in caso di spostamento/copia di nodi
 		HashMap<Integer, DisabledNode> hm = new HashMap<Integer, DisabledNode>();
 		for (int i = 0; i < rootNode.getChildCount(); i++) {
 			Componente comp = (Componente) ((DisabledNode) rootNode
 					.getChildAt(i)).getUserObject();
 			if (comp.isSimple()) {
 				// NOTA: clono il nodo altrimenti viene rimosso dall'albero
 				// originale
 				hm.put(i, (DisabledNode) ((DisabledNode) rootNode.getChildAt(i)).clone());
 			}
 		}
 		return hm;
 	}
 
 	/**
 	 * Fornisce la lista dei Componenti di primo livello dell'albero
 	 * 
 	 * @return Il vettore contenente i nodi
 	 */
 	protected Vector<Componente> getComponenti() {
 		Vector<Componente> comps = new Vector<Componente>();
 		for (int i = 0; i < rootNode.getChildCount(); i++) {
 			comps.add((Componente) ((DisabledNode) rootNode
 					.getChildAt(i)).getUserObject());
 		}
 		return comps;
 	}
 
 	/**
 	 * Permette il caricamento di una lista di Componenti nell'albero
 	 * 
 	 * @param comps
 	 *            Il vettore dei componenti da inserire
 	 */
 	protected void setComponenti(Vector<Componente> comps) {
 		for (int i = 0; i < comps.size(); i++) {
 			addNode(rootNode, new DisabledNode((Componente) comps.get(i)));
 		}
 		tree.expandPath(new TreePath(rootNode.getPath()));
 	}
 	
 	/**
 	 * Verifica se l'albero contiene nodi corretti
 	 * @return	True se l'albero non contiene nodi con errori, False altrimenti
 	 */
 	protected boolean isCorrect() {
 		return isCorrect(rootNode);
 
 	}
 
 	private boolean isCorrectChilds(DisabledNode node) {
 		for (int i=0; i<node.getChildCount(); i++) {
 			if (!isCorrect((DisabledNode)node.getChildAt(i))) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private boolean isCorrect(DisabledNode node) {
 		if (!node.isCorrect) {
 			return false;
 		}
 		if (node.isRoot() || !((Componente)node.getUserObject()).isSimple()) {
 			if (!isCorrectChilds(node)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
 		// Faccio in modo che la root non sia collassabile
 		if (e.getPath().getPathCount() == 1) {
 			// Il path � un "array" contenente gli elementi a partire dalla
 			// root: se c'� un solo elemento l'elemento � solo la root
 			throw new ExpandVetoException(e); // questa eccezione fa si che non avvenga il collasso
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void treeWillExpand(TreeExpansionEvent arg0)
 			throws ExpandVetoException {
 		// Non mi interessa
 	}
 
 	/**
 	 * Ascolta gli eventi per il reset dell'albero e delle azioni di Undo/Redo
 	 * 
 	 * @author Andrea
 	 */
 	public class NewAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -4567445415314285870L;
 		protected static final String NEWCOMMAND = "New";
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			clear();
 			undoManager.discardAllEdits();
 			((MainWindow) ((JButton) e.getSource()).getTopLevelAncestor()).newProject();
 			// resetto la clipboard interna
 			th.getClipboard().removeFlavorListener(MainWindow.eventDispatcher);
 			th.getClipboard().setContents(null, null);
 			th.getClipboard().addFlavorListener(MainWindow.eventDispatcher);
 			MainWindow.pasteState(false);
 		}
 	}
 
 	/**
 	 * Ascolta gli eventi di aggiunta di un nodo all'albero
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class AddAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -4513876659563150305L;
 
 		public static final String ADDCOMMAND = "Add";
 
 		public static final String COMPONENTE = "Componente";
 		public static final String PARENTINDEX = "ParentIndex";
 		public static final String ORIGINALINDEXES = "OriginalIndexes";
 		public static final String NEWINDEXES = "NewIndexes";
 
 		HashMap<Integer, Integer> movingNodes;
 
 		public AddAction() {
 			super();
 			this.putValue(ORIGINALINDEXES, null);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent evt) {
 			if (getValue(COMPONENTE) != null) {
 				DisabledNode parent = null;
 				DisabledNode compNode = new DisabledNode((Componente) getValue(COMPONENTE));
 				if ((Integer) this.getValue(PARENTINDEX) == -1) {
 					parent = rootNode;
 				} else {
 					parent = (DisabledNode) rootNode.getChildAt((Integer) getValue(PARENTINDEX));
 
 				}
 				// resetto il contenuto di movingNodes
 				movingNodes = null;
 				if (getValue(ORIGINALINDEXES) != null) {
 					movingNodes = new HashMap<Integer, Integer>();
 					@SuppressWarnings("unchecked")
 					Vector<Integer> originalIndexes = (Vector<Integer>) getValue(ORIGINALINDEXES);
 					@SuppressWarnings("unchecked")
 					Vector<Integer> newIndexes = (Vector<Integer>) getValue(NEWINDEXES);
 					for (int i = (originalIndexes.size()-1); i >=0 ; i--) {
 						movingNodes.put(newIndexes.get(i), originalIndexes.get(i));
 						model.removeNodeFromParent((DisabledNode) rootNode.getChildAt(originalIndexes.get(i)));
 					}
 				}
 				addNode(parent, compNode);
 				// creo l'undoable dell'add
 				UndoableEdit edit = new UndoableAddNode(tree, (Componente) compNode.getUserObject(), (Integer) getValue(PARENTINDEX), parent.getIndex(compNode), movingNodes);
 				undoSupport.postEdit(edit);
 
 				// setto il focus sul nuovo oggetto
 				if (!evt.getActionCommand().equals(PannelloComp.ADDNEWACTION)) {
 					// se non sto aggiungendo un elemento dal pannello delle
 					// proprieta sposto il focus ed espando il nodo
 					tree.setSelectionPath(new TreePath(compNode.getPath()));
 					tree.expandPath(new TreePath(compNode.getPath()));
 				} else {
 					// se sto aggiungendo un elemento dal pannello delle
 					// proprieta espando il padre
 					tree.expandPath(new TreePath(parent.getPath()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Ascolta gli eventi di rimozione di un elemento dall'albero
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class RemoveAction extends AbstractAction {
 
 		public static final String INDEXES = "Indexes";
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3159160087603892781L;
 		public static final String REMOVECOMMAND = "Remove";
 
 		/**
 		 * Il costruttore setta l'azione di default NOTA: una remove pu� avere
 		 * come target o il nodo selezionato sull'albero o, per i nodi
 		 * molteplici un nodo selezionato nel pannello di riepilogo. Per default
 		 * il nodo da eliminare � quello selezionato sull'albero
 		 */
 		public RemoveAction() {
 			super();
 			this.putValue(INDEXES, null);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			TreePath currentSelection = tree.getSelectionPath();
 			DisabledNode currentNode;
 			if (getValue(INDEXES) == null) {
 				// indici = null indica che il nodo da rimuovere �
 				// quello selezionato sull'albero
 				currentNode = (DisabledNode) (currentSelection.getLastPathComponent());
 				delete(currentSelection, currentNode);
 			} else {
 				DisabledNode parent = (DisabledNode) (currentSelection.getLastPathComponent());
 				int[] indexes = (int[]) getValue(INDEXES);
 				System.out.println("indexes length: "+indexes.length);
 				for (int i = 0; i < indexes.length; i++) {
 					currentNode = (DisabledNode) parent.getChildAt(indexes[i]);
 					delete(currentSelection, currentNode);
 				}
 			}
 		}
 
 		/**
 		 * Rimuove un nodo dall'albero e gestisce la creazione dell'oggetto per
 		 * l'undo
 		 * 
 		 * @param currentSelection	Il nodo selezionato sull'albero
 		 * @param nodeToRemove		Il nodo che si intende rimuovere
 		 */
 		private void delete(TreePath currentSelection, DisabledNode nodeToRemove) {
 			/*
 			 * NOTA: il nodo selezionato sull'albero NON � sempre il nodo da
 			 * rimuovere se si considera ad esempio il pannello delle propriet�
 			 * di un elemento molteplice, ad esempio, il nodo selezionato
 			 * nell'albero � l'elemento molteplice ma � possibile rimuovere uno
 			 * dei suoi figli selezionandolo nella lista delle propriet�. In
 			 * questo caso, la currentSelection � l'elemento molteplice, ma il
 			 * currentNode sar� il nodo figlio, e sar� quest'ultimo da rimuovere
 			 */
 			DisabledNode parent = (DisabledNode) nodeToRemove.getParent();
 			int parentIndex;
 			if (parent.isRoot()) {
 				parentIndex = -1;
 			} else {
 				DisabledNode root = (DisabledNode) model.getRoot();
 				parentIndex = root.getIndex(parent);
 			}
 			// Creo l'oggetto per l'undo action
 			UndoableEdit edit = new UndoableRemoveNode(tree, (Componente) nodeToRemove.getUserObject(), parentIndex, parent.getIndex(nodeToRemove));
 			undoSupport.postEdit(edit);
 			// rimuovo effettivamente il nodo
 			removeNode(nodeToRemove);
 		}
 	}
 
 	/**
 	 * Ascolta la copia di un nodo dell'albero
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class CopyAction extends AbstractAction {
 		// NOTA: una CopyAction � fondamentalmente una AddAction ma l'elemento
 		// inserito � una copia di quello esistente
 		// infatti le tengo separate per "comprensione" ma in pratica uso sempre
 		// la AddAction
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -4612100142468392225L;
 		protected static final String PARENTINDEX = "ParentIndex";
 		protected static final String INDEX = "Index";
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			DisabledNode parent;
 			if (((Integer) this.getValue(PARENTINDEX)) == -1) {
 				parent = rootNode;
 			} else {
 				parent = (DisabledNode) rootNode.getChildAt((Integer) this.getValue(PARENTINDEX));
 			}
 			DisabledNode compNode = (DisabledNode) parent.getChildAt((Integer) this.getValue(INDEX));
 			UndoableEdit edit = new UndoableAddNode(tree, (Componente) compNode.getUserObject(), (Integer) getValue(PARENTINDEX), parent.getIndex(compNode), null);
 			undoSupport.postEdit(edit);
 		}
 	}
 
 	/**
 	 * Ascolta lo spostamento di un nodo nell'albero
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class MoveAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3099984963394542917L;
 		protected static final String OLDPARENTINDEX = "OldParentIndex";
 		protected static final String NEWPARENTINDEX = "NewParentIndex";
 		protected static final String UPDATENEWPARENTINDEX = "UpdatedNewParentIndex";
 		protected static final String NEWINDEX = "NewIndex";
 		protected static final String OLDINDEX = "OldIndex";
 
 		protected void moveNode() {
 			DisabledNode oldParent = rootNode;
 			if ((Integer) getValue(OLDPARENTINDEX) != -1) {
 				oldParent = (DisabledNode) rootNode.getChildAt((Integer) getValue(OLDPARENTINDEX));
 			}
 			DisabledNode newParent = (DisabledNode) rootNode.getChildAt((Integer) getValue(NEWPARENTINDEX));
 			ComponenteMolteplice parentComp = (ComponenteMolteplice) newParent.getUserObject();
 			DisabledNode node = (DisabledNode) oldParent.getChildAt((Integer) getValue(OLDINDEX));
 			ComponenteSemplice comp = (ComponenteSemplice) (node).getUserObject();
 			parentComp.aggiungiOpzione(comp, (Integer)getValue(NEWINDEX));
			model.insertNodeInto(new DisabledNode(comp), newParent, newParent.getChildCount());
 			model.removeNodeFromParent(node);
 			// richiamo la creazione dell'undo apposito
 			actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, ""));
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			// prima di creare l'evento di undo devo aggiornare gli indici new
 			// Es. sto spostando un nodo da una posizione con indice basso ad
 			// una con indice alto.
 			// Dato che il drag/drop prima importa poi esporta il nuovo indici
 			// risulta maggiorato di 1
 			int newParentIndex = (Integer) getValue(NEWPARENTINDEX);
 			int newIndex = (Integer) getValue(NEWINDEX);
 			if ((Integer) getValue(OLDPARENTINDEX) == -1) {
 				if ((Integer) getValue(NEWPARENTINDEX) == -1) {
 					if (((Integer) getValue(OLDINDEX)) < ((Integer) getValue(NEWINDEX))) {
 						newIndex = newIndex - 1;
 					}
 				} else {
 					if (((Integer) getValue(OLDINDEX)) < ((Integer) getValue(NEWPARENTINDEX))) {
 						newParentIndex = newParentIndex - 1;
 					}
 				}
 			} else if (((Integer) getValue(OLDPARENTINDEX)) == ((Integer) getValue(NEWPARENTINDEX))) {
 				if (((Integer) getValue(OLDINDEX)) < ((Integer) getValue(NEWINDEX))) {
 					newIndex = newIndex - 1;
 				}
 			}
 			UndoableEdit edit = new UndoableMoveNode(tree, (Integer) getValue(OLDPARENTINDEX), newParentIndex, (Integer) getValue(OLDINDEX), newIndex);
 			undoSupport.postEdit(edit);
 		}
 
 	}
 
 	/**
 	 * Ascolta la modifica della priorit� degli elementi all'interno di un
 	 * Alternative
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class ChangePriorityAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 5836454808095990928L;
 		protected static final String OLDINDEXES = "OldIndexes";
 		protected static final String PARENTINDEX = "ParentIndex";
 		protected static final String GAP = "gap";
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent arg0) {
 			// prendo la selezione, prendo il parent e swappo
 			int parentIndex = (Integer) getValue(PARENTINDEX);
 			DisabledNode parent = (DisabledNode) ((DisabledNode) model.getRoot()).getChildAt((Integer) getValue(PARENTINDEX));
 			int[] oldIndexes = (int[]) getValue(OLDINDEXES);
 			int gap = (Integer) getValue(GAP);
 			if (gap < 0) {
 				for (int i = 0; i < oldIndexes.length; i++) {
 					// sposta i nodi e aggiorna la lista
 					spostaNodo(parent, oldIndexes[i], gap);
 				}
 			} else {
 				for (int i = (oldIndexes.length - 1); i >= 0; i--) {
 					// sposta i nodi e aggiorna la lista
 					spostaNodo(parent, oldIndexes[i], gap);
 				}
 			}
 			UndoableChangePriority edit = new UndoableChangePriority(tree, parentIndex, oldIndexes, gap);
 			undoSupport.postEdit(edit);
 		}
 
 		/**
 		 * Sposta un nodo all'interno del proprio genitore di un determinato
 		 * numero di posti
 		 * 
 		 * @param p	Il genitore
 		 * @param i	L'indice del nodo da spostare
 		 * @param g	Il numero di posti di cui spostarlo
 		 */
 		private void spostaNodo(DisabledNode p, int i, int g) {
 			ComponenteMolteplice comp = (ComponenteMolteplice) p.getUserObject();
 			// aggiorno l'elenco delle opzioni
 			Vector<ComponenteSemplice> opzioni = comp.getOpzioni();
 			Collections.swap(opzioni, i, i+g);
 			
 			// sposto i nodi
 			DisabledNode node = (DisabledNode) p.getChildAt(i);
 			model.removeNodeFromParent(node);
 			model.insertNodeInto(node, p, i + g);
 		}
 	}
 
 	/**
 	 * Ascolta la modifica di un campo di un Elemento
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class ChangeFieldAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 8756265766454125789L;
 		protected static final String FIELD = "Field";
 		protected static final String OLDVALUE = "OldValue";
 		protected static final String NEWVALUE = "NewValue";
 
 		public static final String NAME = "Nome";
 		public static final String CATEGORY = "Categoria";
 		public static final String EMPHASIS = "Enfasi";
 		public static final String VISIBILITY = "Visibilita";
 		public static final String TEXT = "Testo";
 		public static final String IMAGE = "Image path";
 		public static final String URLPATH = "URL path";
 		public static final String URLTEXT = "URL text";
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			DisabledNode node = (DisabledNode) tree.getSelectionPath().getLastPathComponent();
 			DisabledNode parent = (DisabledNode) node.getParent();
 			int parentIndex = -1;
 			if (!parent.isRoot()) {
 				parentIndex = rootNode.getIndex(parent);
 			}
 			Componente comp = (Componente) node.getUserObject();
 			String field = (String) getValue(FIELD);
 			if (field == NAME) {
 				comp.setNome((String) getValue(NEWVALUE));
 			} else if (field == CATEGORY) {
 				comp.setCategoria((String) getValue(NEWVALUE));
 			} else if (field == EMPHASIS) {
 				comp.setEnfasi((Integer) getValue(NEWVALUE));
 			} else if (field == VISIBILITY) {
 				comp.setVisibilita((Integer) getValue(NEWVALUE));
 			} else if (field == IMAGE) {
 				((Immagine) comp).setPath((String) getValue(NEWVALUE));
 			} else if (field == TEXT) {
 				((Testo) comp).setTesto((String) getValue(NEWVALUE));
 			} else if (field == URLPATH) {
 				((Link) comp).setUri((String) getValue(NEWVALUE));
 			} else if (field == URLTEXT) {
 				((Link) comp).setTesto((String) getValue(NEWVALUE));
 			}
 			UndoableChangeField edit = new UndoableChangeField(tree, parentIndex, parent.getIndex(node), (String) getValue(FIELD), (Object) getValue(OLDVALUE), (Object) getValue(NEWVALUE));
 			undoSupport.postEdit(edit);
 		}
 
 	}
 
 	/**
 	 * Ascolta una richiesta di Undo
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class UndoAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3952201139907413522L;
 		public static final String UNDOCOMMAND = "Undo";
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			undoManager.undo();
 		}
 
 	}
 
 	/**
 	 * Ascolta una richiesta di Redo
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	public class RedoAction extends AbstractAction {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 2568562509253946673L;
 		public static final String REDOCOMMAND = "Redo";
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e) {
 			undoManager.redo();
 		}
 
 	}
 
 	/**
 	 * Listener per gli eventi di Undo/Redo
 	 * 
 	 * @author Andrea
 	 * 
 	 */
 	private class UndoAdapter implements UndoableEditListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void undoableEditHappened(UndoableEditEvent evt) {
 			UndoableEdit edit = evt.getEdit();
 			undoManager.addEdit(edit);
 		}
 	}
 
 }
