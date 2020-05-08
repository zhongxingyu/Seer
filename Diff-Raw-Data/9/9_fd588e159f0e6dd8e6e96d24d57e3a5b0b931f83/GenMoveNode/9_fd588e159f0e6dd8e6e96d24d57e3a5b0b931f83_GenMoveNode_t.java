 package com.chess.genesis;
 
 class GenMoveNode implements Comparable<GenMoveNode>
 {
 	public GenMove move;
 	public int score;
 	public boolean check;
 
 	public GenMoveNode()
 	{
 		score = 0;
 		check = false;
 		move = new GenMove();
 	}
  
 	public int compareTo(final GenMoveNode a)
 	{
		return (a.score > score)? 1 : ((a.score < score)? -1 : 0);
 	}
 }
