 package tictactoeai;
 
 public class MinimaxWin implements Player {
 	private Node tree;
 	private Node activeNode;
 	private boolean firstMove;
 
 	
 	MinimaxWin(){
 		System.out.println("minimax made");
 		tree = new Node(1);
 		activeNode=tree;
 		firstMove=true;
 		System.out.println("minimax made");
 	}
 	
 
 	@Override
 	public String getType() {
 		return "minimax-Win";
 	}
 
 	public String toString(){
 		return "";
 	}
 
 	@Override
 	public void makeMove(Board board) {
 		System.out.println("making move");
 		if(firstMove){
 			int score=activeNode.getScore();
 			for(Node n: activeNode.getChildren()){
 				if(n.getScore()==score){
 					activeNode=n;
 					break;
 				}
 			}
 			board.makeMove(1, activeNode.getMovement());
 			this.firstMove = false;
 		}else{
 			System.out.println("not first move");
 			for(Node n: activeNode.getChildren()){
 				System.out.println("checking nodes");
				System.out.println(n.getState().toString());
 				if(n.getState().equals(board)){
					System.out.println("here");
 					activeNode=n;
 					break;
 				}
 			}
 			int score=activeNode.getScore();
 			for(Node n: activeNode.getChildren()){
 				if(n.getScore()==score){
 					activeNode=n;
 					break;
 				}
 			}
 			board.makeMove(1, activeNode.getMovement());
 		}
 		return;
 	}
 }
