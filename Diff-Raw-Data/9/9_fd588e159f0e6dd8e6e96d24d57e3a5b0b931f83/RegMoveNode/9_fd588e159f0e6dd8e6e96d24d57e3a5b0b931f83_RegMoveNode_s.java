 package com.chess.genesis;
 
 class RegMoveNode implements Comparable<RegMoveNode>
 {
 	public RegMove move;
 	public int score;
 	public boolean check;
 
 	public RegMoveNode()
 	{
 		score = 0;
 		check = false;
 		move = new RegMove();
 	}
 
 	public RegMoveNode(final RegMoveNode node)
 	{
 		score = node.score;
 		check = node.check;
 		move = new RegMove(node.move);
 	}
  
 	public int compareTo(final RegMoveNode a)
 	{
		return a.score - score;
 	}
 }
