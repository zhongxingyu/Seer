 package data;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import search.MoveValidator;
 
 public class GameBoardNode {
 	private GameBoardTree tree;
 	GameBoardNode parent;
 	Set<GameBoardNode> children;
 	
 	GameBoard gameBoard;
 	
 	public GameBoardNode(GameBoardNode parent, GameBoard gb) {
 		gameBoard = gb;
 		
		children = new HashSet<GameBoardNode>();
 	}
 	
 	public void setTree(GameBoardTree tree) {
 		if(parent == null)
 			this.tree = tree;
 	}
 	
 	private GameBoardTree getTree() {
 		return getRoot().tree;
 	}
 		
 	public Set<GameBoardNode> getChildren() {
 		return children;
 	}
 	
 	public Set<GameBoardNode> spawnChildren() {
 		Set<GameBoard> newChildren = MoveValidator.getValidPermutations(gameBoard);
 		
 		Iterator<GameBoard> it = newChildren.iterator();
 		
 		while(it.hasNext()) {
 			GameBoardNode newNode = new GameBoardNode(this, it.next());
 			if(!tree.nodeSet.contains(newNode)) {
 				children.add(newNode);
 				newNode.setTree(tree);
 				tree.nodeSet.add(newNode);
 			}
 		}
 		
 		return getChildren();
 	}
 	
 	@Override
 	public int hashCode() {
 		return gameBoard.hashCode();
 	}
 	
 	private GameBoardNode getRoot() {
 		GameBoardNode root = null;
 		
 		while(parent != null)
 			root = root.parent;
 		
 		return root;
 	}
 	
 	public boolean equals(Object obj) {
 		return hashCode() == obj.hashCode();
 		/*
 		if(this == obj)
 			return true;
 		if((obj == null) || (obj.getClass() != this.getClass()))
 			return false;
 		// object must be GameBoardNode at this point
 		GameBoardNode test = (GameBoardNode)obj;
 		return num == test.num && (data == test.data || (data != null && data.equals(test.data)));
 		*/
 	}
 }
