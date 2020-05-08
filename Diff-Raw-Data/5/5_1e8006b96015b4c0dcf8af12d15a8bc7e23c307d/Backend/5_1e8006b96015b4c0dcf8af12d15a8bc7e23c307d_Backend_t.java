 package whiteboard;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Stack;
 import java.util.concurrent.ConcurrentHashMap;
 
 import networking.Networking;
 import GUI.ViewportDragScrollListener;
 import GUI.WhiteboardPanel;
 import boardnodes.BoardElt;
 import boardnodes.BoardEltType;
 import boardnodes.BoardPath;
 import boardnodes.ScribbleNode;
 import boardnodes.SerializedBoardElt;
 import boardnodes.SerializedBoardPath;
 import boardnodes.SerializedInUse;
 import boardnodes.SerializedProjectSaveInfo;
 import boardnodes.SerializedStyledNode;
 import boardnodes.StyledNode;
 //import boardnodes.SerializedScribbleNode;
 import boardnodes.SerializedScribbleNode;
 
 public class Backend {
 	private GUI.WhiteboardPanel panel;
 	private ConcurrentHashMap<Integer, BoardElt> boardElts;
 	private ArrayList<boardnodes.BoardPath> paths;
 	public Stack<BoardAction> pastActions;
 	private Stack<BoardAction> futureActions;
 	private Networking networking;
 	public BoardElt clipboard;
 	public ViewportDragScrollListener _mouseListener;
 	private String _projectName;
 
 	public Backend(String projectName, GUI.WhiteboardPanel _panel) {
 		panel = _panel;
 		_projectName = projectName;
 		pastActions = new Stack<BoardAction>();
 		futureActions = new Stack<BoardAction>();
 		boardElts = new ConcurrentHashMap<Integer, BoardElt>(50);
 		paths = new ArrayList<boardnodes.BoardPath>();
 		networking = new Networking();
 		networking.setBackend(this);
 	}
 
 	public void clear() {
 		pastActions.clear();
 		futureActions.clear();
 		boardElts.clear();
 		paths.clear();
 	}
 
 	public void save(File f) {
 		System.out.println("backend: saving to file"); 
 		/* WHAT ELSE NEEDS TO BE SAVED?? */
 				LinkedList<Object> toWriteOut = new LinkedList<Object>();
 				try {
 					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
 					for (BoardElt be : this.boardElts.values()) {
 						System.out.println("backend: writing out boardelt of type " + be.type);
 						toWriteOut.add(be.toSerialized());
 					}
 					toWriteOut.add(new SerializedProjectSaveInfo(_projectName, WhiteboardPanel.UIDCounter));
 					oos.writeObject(toWriteOut);
 					oos.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 	}
 
 	public LinkedList<Object> saveForNetwork() {
 		System.out.println("backend: saving to network"); 
 		/* WHAT ELSE NEEDS TO BE SAVED?? */
 		LinkedList<Object> toWriteOut = new LinkedList<Object>();
 		for (BoardElt be : this.boardElts.values()) {
 			System.out.println("backend: writing out boardelt of type " + be.type);
 			toWriteOut.add(be.toSerialized());
 		}
 		toWriteOut.add(new SerializedProjectSaveInfo(_projectName, WhiteboardPanel.UIDCounter));
 		return toWriteOut;
 	}
 
 	public void loadFromNetwork(Object project) {
 		System.out.println("backend: loading project from network");
 		/* Make new whiteboard for opening */
 		clear();
 		panel.clearBoard();
 		SerializedBoardElt be;
 		for (Object o : (LinkedList<Object>) project) {
 			be = (SerializedBoardElt) o;
 			if (be.type == BoardEltType.INFO) {
 				setProjectName(((SerializedProjectSaveInfo) be).projectName);
 			} else {
 				addActionFromNetwork(new CreationAction(receiveNetworkCreationObject(be)));
 			}
 		}
 		/* Clear stack upon loading */
 		pastActions.clear();
 		futureActions.clear();
 	}
 
 	public void load(File f) {
 		System.out.println("backend: loading file");
 		try {
 			/* Make new whiteboard for opening */
 			clear();
 			panel.clearBoard();
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
 			LinkedList<Object> save = (LinkedList<Object>) ois.readObject();
 			SerializedBoardElt be;
 			for (Object o : save) {
 				be = (SerializedBoardElt) o;
 				if (be.type == BoardEltType.INFO) {
 					setProjectName(((SerializedProjectSaveInfo) be).projectName);
 					panel.setStartUID(((SerializedProjectSaveInfo) be).startUID);
 				} else {
 					addActionFromNetwork(new CreationAction(receiveNetworkCreationObject(be)));
 				}
 			}
 			/* Clear stack upon loading */
 			pastActions.clear();
 			futureActions.clear();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setProjectName(String projectName) {
 		_projectName = projectName;
 		panel._mainFrame.setTitle(_projectName);
 	}
 
 	//Adds the given board elt and adds the "addition" action to the stack
 	public void add(BoardElt b) {
 		b._mouseListener = _mouseListener;
 		boardElts.put(b.getUID(), b);
 		BoardAction committed = new CreationAction(b);
 		addAction(committed);
 		if(b.getType() == BoardEltType.PATH) {
 			paths.add((boardnodes.BoardPath)b);
 		}
 	}
 	public void addFromNetwork(BoardElt b) {
 		b._mouseListener = _mouseListener;
 		boardElts.put(b.getUID(), b);
 		if(b.getType() == BoardEltType.PATH)
 			paths.add((boardnodes.BoardPath)b);
 
 	}
 
 	//Returns the board elt with given UID. Returns null if no elt with that UID exists
 	public BoardElt lookup(int UID) {
 		return boardElts.get(UID);
 	}
 
 	//Returns and removes the board elt with given UID. Returns null if no elt with that UID exists
 	//If the remove happens, then add a "removal" action to the stack
 	public BoardElt remove(int UID) {
 		BoardElt toReturn = boardElts.remove(UID);
 		if(toReturn!=null) {
 			if(toReturn.getType()!=BoardEltType.PATH) {
 				panel.remove(toReturn);
 			}
 			BoardAction committed = new DeletionAction(toReturn);
 			addAction(committed);
 			if (lookup(UID) != null) {
 			} else  {
 				System.err.println("Couldn't find UID of remove and tried to send " + UID);
 			}
 		}
 		panel.repaint();
 		return toReturn;
 	}
 
 	public BoardElt removeFromNetwork(int UID) {
 		BoardElt toReturn = boardElts.remove(UID);
 		if(toReturn!=null) {
 			if(toReturn.getType()!=BoardEltType.PATH) {
 				panel.remove(toReturn);
 			}
 			if (lookup(UID) != null) {
 			} else  {
 				System.err.println("Couldn't find UID of remove and tried to send " + UID);
 			}
 		}
 		panel.repaint();
 		return toReturn;
 	}
 
 	//Keeps track of the event that the board element with given UID was just modified, so the whiteboard knows which
 	//	one to ask to undo when necessary
 	public void modifyBoardElt(int UID) {
 		BoardElt b = (BoardElt) boardElts.get(UID);
 		if(b!=null) {
 			BoardAction committed = new ModificationAction(b);
 			addAction(committed);
 		}
 	}
 
 	//Adds the given action to the stack, and erases all future actions because we've started a new "branch"
 	public void addAction(BoardAction ba) {
 		addActionHelper(ba, false);
 	}
 
 	//does same thing as above except doesnt send it as a message across
 	public void addActionFromNetwork(BoardAction ba) {
 		addActionHelper(ba, true);
 	}
 
 	private void addActionHelper(BoardAction ba, boolean fromNetwork) {
 		pastActions.push(ba);
 		futureActions.clear();
 		if(!fromNetwork) {
 			/* VERY IMPORTANT EFFECT WHICK NODES GET SENT ACROSS */
 			if(ba.target.getType()==BoardEltType.PATH || ba.target.getType() == BoardEltType.SCRIBBLE || ba.target.getType() == BoardEltType.STYLED) {
 				networking.sendAction(new BoardEltExchange(ba.getTarget().toSerialized(), ba.getType()));
 			}
 		}
 	}
 
 	//Copies the given element (i.e. sets the clipboard)
 	public void copy(BoardElt b) {
 		clipboard = b.clone();
 	}
 
 	public void paste(Point pos) {
 		clipboard.paste(pos);
 	}
 
 	public Object render() {
 		//TODO: will render everything in each list. Discuss with brandon exactly how I should return this/do this
 		return null;
 	}
 
 	public void undo() {
 		undoHelper(false);
 	}
 
 	public void undoFromNetwork() {
 		undoHelper(true);
 	}
 
 	private void undoHelper(boolean fromNetwork) {
 
 		//get the top of the action stack, handle it, and push it to the future actions stack for redo
 		if(pastActions.empty()) {
 			System.out.println("no actions to undo!");
 			return;
 		}
 		if(!fromNetwork)
 			networking.sendAction(new BoardEltExchange(null, BoardActionType.UNDO));
 		BoardAction b = pastActions.pop();
 		BoardElt be;
 		switch(b.getType()) {
 		case ELT_MOD:
 			//			boardElts   
 			//			System.out.println("undoing a modification on node "+b.getTarget());
 			b.getTarget().undo();
 			b.getTarget().repaint();
 			futureActions.push(b);
 			break;
 		case CREATION:
 			//undoing a creation means doing a deletion
 			be = b.getTarget();
 			if(be==null)
 				return;
 			boardElts.remove(be.getUID());
 			if(be.getType()==BoardEltType.PATH) {
 				paths.remove(be);
 			} else {
 				panel.remove(be);
 			}
 			futureActions.push(new CreationAction(be));
 			break;
 		case DELETION:
 			//undoing a deletion means doing a creation
 			be = b.getTarget();
 			if(be==null)
 				return;
 			boardElts.put(be.getUID(), be);
 			if(be.getType()==BoardEltType.PATH) {
 				paths.add((boardnodes.BoardPath)be);
 			} else {
 				panel.add(be);
 			}
 			futureActions.push(new DeletionAction(be));
 			break;
 		}
 		panel.repaint();
 	}
 
 	public void redo() {
 		redoHelper(false);
 	}
 
 	public void redoFromNetwork() {
 		redoHelper(true);
 	}
 
 	private void redoHelper(boolean fromNetwork) {
 		//get the top of the action stack, handle it, and push it to the past actions stack for undo
 		if(futureActions.empty()) {
 			System.out.println("no actions to redo!");
 			return;
 		}
 		if(!fromNetwork)
 			networking.sendAction(new BoardEltExchange(null, BoardActionType.REDO));
 		BoardElt be;
 		BoardAction b = futureActions.pop();
 		switch(b.getType()) {
 		case ELT_MOD:
 			b.getTarget().redo();
 			b.getTarget().repaint();
 			pastActions.push(b);
 			break;
 		case CREATION:
 			//redoing a creation means doing a creation
 			be = b.getTarget();
 			if(be==null)
 				return;
 			boardElts.put(be.getUID(), be);
 			if(be.getType()==BoardEltType.PATH) {
 				paths.add((boardnodes.BoardPath)be);
 			} else {
 				panel.add(be);
 			}
 			pastActions.push(new CreationAction(be));
 			break;			
 		case DELETION:
 			//redoing a deletion means doing a deletion
 			be = b.getTarget();
 			if(be==null)
 				return;
 			boardElts.remove(be.getUID());
 			if(be.getType()==BoardEltType.PATH) {
 				paths.remove(be);
 			} else {
 				panel.remove(be);
 			}
 			pastActions.push(new DeletionAction(be));
 			break;
 		}
 		panel.repaint();
 	}
 
 	public Networking getNetworking() {
 		return networking;
 	}
 
 	public GUI.WhiteboardPanel getPanel() {
 		return panel;
 	}
 
 	public ArrayList<boardnodes.BoardPath> getPaths() {
 		return paths;
 	}
 	/**
 	 * @author aabeshou
 	 * call boardelt.encode on all of the elements in the board, and concatenate them all into one XML string that this will return
 	 * @return
 	 * An XML string encoding the whiteboard.
 	 */
 	public String encode() {
 		return null;
 	}
 
 	public static Backend decode() {
 		return null;
 		//TODO: take in XML/JSON and create a whiteboard object out of it, compelte with all the elements that are in it
 	}
 
 	//returns a map from elements that contain the term to the first index of that term in the element's text field
 	public ArrayList<SearchResult> search(String query) {
 		ArrayList<SearchResult> toReturn = new ArrayList<SearchResult>();
 		for(BoardElt b: boardElts.values()) {
 			toReturn.addAll(b.search(query));
 		}
 		return toReturn;
 	}
 
 	/**callback for when the networking object associated with
 	 * this backend has received a new Object reflecting a change 
 	 * in the state of the hosted Whiteboard.
 	 * We simply replace our outdated object with the one
 	 * contained in this BoardEltExchange object
 	 * 
 	 * @param receivedAction
 	 */
 	public void receiveNetworkedObject(Object receivedAction) {
 		System.out.println("backend: received callback from networking");
 		BoardEltExchange bex = (BoardEltExchange) receivedAction;
 		SerializedBoardElt serializedElt = bex.getNode();
 		BoardActionType type = bex.getAction();
 		BoardAction action = null;
 		switch (type) {
 		case IN_USE:
 			SerializedInUse siu = (SerializedInUse) serializedElt;
 			boolean isBeingUsed = siu.isInUse;
 			int node = siu.getUID();
 			BoardElt boardNode = boardElts.get(node);
 			System.out.println(" UID is " + node + " object null? " + (boardNode == null) + ", used? " + isBeingUsed);
 			if (boardNode != null)
 				boardNode.setBeingEditedStatus(isBeingUsed, siu.sender);
 			break;
 		case CREATION:
 			action = new CreationAction(receiveNetworkCreationObject(serializedElt));
 			addActionFromNetwork(action);
 			break;
 		case ELT_MOD:
 			action = new ModificationAction(receiveNetworkModificationObject(serializedElt));
 			addActionFromNetwork(action);
 			break;
 		case DELETION:
 			action = new DeletionAction(receiveNetworkDeletionObject(serializedElt));
 			addActionFromNetwork(action);
 			break;
 		case REDO:
 			redoFromNetwork();
 			break;
 		case UNDO:
 			undoFromNetwork();
 			break;
 		}
 
 		/*
 		if (nodeToReplace.getType() != BoardEltType.PATH) {
 			//panel.updateMember(nodeToReplace);
 		}
 		else {
 			for (int i = 0 ; i < paths.size(); i++) {
 				if (paths.get(i).getUID() == nodeToReplace.getUID()) {
 					paths.set(i, (BoardPath)nodeToReplace);
 					break;
 				}
 			}
 		}
 		 */
 		this.getPanel().repaint();
 	}
 
 
 	private BoardElt receiveNetworkCreationObject(SerializedBoardElt e) {
 		BoardElt toReturn = null;
 		switch (e.getType()) {
 		case PATH:
 			if(!boardElts.containsKey(e.getUID())) {
 				toReturn = new BoardPath(((SerializedBoardPath) e).getUID(), this);
 				toReturn.ofSerialized(((SerializedBoardPath) e));
 				toReturn._mouseListener = _mouseListener;
 				boardElts.put(toReturn.getUID(), toReturn);
 				System.out.println("adding "+toReturn.getUID());
 				paths.add((BoardPath) toReturn);
 			} else {
 				boardElts.get(((SerializedBoardPath) e).getUID()).ofSerialized(((SerializedBoardPath) e));
 			}
 			break;
 		case SCRIBBLE:
 			if(!boardElts.containsKey(e.getUID())) {
 				toReturn = new ScribbleNode(e.getUID(), this);
 				toReturn.ofSerialized(((SerializedScribbleNode) e));
 				toReturn._mouseListener = _mouseListener;
 				boardElts.put(toReturn.getUID(), toReturn);
				panel.add(toReturn,0);
 				System.out.println("adding "+toReturn.getUID());
 			} else {
 				boardElts.get(e.getUID()).ofSerialized(((SerializedScribbleNode) e));
 			}
 			break;
 		case STYLED:
 			if(!boardElts.containsKey(e.getUID())) {
 				toReturn = new StyledNode(e.getUID(), this);
 				toReturn.ofSerialized(((SerializedStyledNode) e));
 				toReturn._mouseListener = _mouseListener;
 				boardElts.put(toReturn.getUID(), toReturn);
				panel.add(toReturn,0);
 				System.out.println("adding "+toReturn.getUID());
 			} else {
 				boardElts.get(e.getUID()).ofSerialized(((SerializedStyledNode) e));
 			}
 			break;
 		}
 		panel.extendPanel(boardElts.get(e.getUID()).getBounds());
 		panel.repaint();
 		return toReturn;
 	}
 
 	private BoardElt receiveNetworkDeletionObject(SerializedBoardElt e) {
 		BoardElt toReturn = null;
 		if(boardElts.containsKey(e.getUID())) {
 			toReturn = boardElts.remove(e.getUID());
 			if(e.getType()==BoardEltType.PATH)
 				paths.remove(toReturn);
 			else
 				panel.remove(toReturn);
 		}
 		panel.repaint();
 		return toReturn;
 	}
 
 	private BoardElt receiveNetworkModificationObject(SerializedBoardElt e) {
 		if(boardElts.containsKey(e.getUID())) {
 			boardElts.get(e.getUID()).ofSerialized(e);
 		}
 		if (boardElts.get(e.getUID()) == null)
 			return null;
 		panel.extendPanel(boardElts.get(e.getUID()).getBounds());
 		panel.setListFront(e.getUID());
 		panel.repaint();
 		return boardElts.get(e.getUID());
 	}
 
 	public void setStartUID(int id) {
 		System.out.println("setting start id to "+id);
 		panel.setStartUID(id);
 	}
 	public ArrayList<BoardElt> getElts() {
 		return new ArrayList<BoardElt>(boardElts.values());
 	}
 
 	public void centerNode(BoardElt boardElt) {
 		Rectangle elementBounds = boardElt.getBounds();
 		Rectangle visibleRect = panel.getVisibleRect();
 		visibleRect.setLocation(elementBounds.x-(visibleRect.width/2)+(elementBounds.width)/2, elementBounds.y-(visibleRect.height)/2+(elementBounds.height)/2);
 		panel.scrollRectToVisible(visibleRect);
 	}
 
 	public void alertEditingStatus(BoardElt b, boolean isInUse) {
 		SerializedInUse toSend = new SerializedInUse(b.getUID(), b.type, isInUse, networking.getMyClientInfo());
 		BoardEltExchange bex = new BoardEltExchange(toSend, BoardActionType.IN_USE);
 		networking.sendAction(bex);
 	}
 }
