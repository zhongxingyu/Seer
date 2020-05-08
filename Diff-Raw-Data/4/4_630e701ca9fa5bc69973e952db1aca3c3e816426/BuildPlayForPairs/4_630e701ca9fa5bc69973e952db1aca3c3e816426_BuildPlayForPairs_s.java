 package com.blackjack.strategy;
 
 import com.blackjack.cards.Card;
 import com.blackjack.player.Play;
 
 public class BuildPlayForPairs {
 	
 	public static Play[][] build() {
 		// playForPairs array index is facevalue - 2
 		// Example: Ace = (11-2) = 9
 		// Ten, Jack, Queen, King = (10-2) = 8
 		// Deuce = (2-2) = 0
 		// p[] = {2,3,4,5,6,7,8,9,10,A}
 		
 		Play[][] playForPairs = new Play[10][10];
 
 		{// Aces [9]
 			Play p[] = { Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT };
 			playForPairs[9] = p;
 		}
 		{// Tens [8]
 			Play p[] = { Play.STAND, Play.STAND, Play.STAND, Play.STAND,
 					Play.STAND, Play.STAND, Play.STAND, Play.STAND, Play.STAND,
 					Play.STAND };
 			playForPairs[8] = p;
 		}
 		{// Nines [7]
 			Play p[] = { Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
					Play.SPLIT, Play.STAND, Play.SPLIT, Play.SPLIT, Play.SPLIT,
					Play.SPLIT };
 			playForPairs[7] = p;
 		}
 		{// Eights [6]
 			Play p[] = { Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT };
 			playForPairs[6] = p;
 		}
 		{// Sevens [5]
 			Play p[] = { Play.SPLIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT, Play.SPLIT, Play.HIT, Play.HIT, Play.HIT,
 					Play.HIT };
 			playForPairs[5] = p;
 		}
 		{// Sixes [4]
 			Play p[] = { Play.HIT, Play.SPLIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT, Play.HIT, Play.HIT, Play.HIT, Play.HIT,
 					Play.HIT };
 			playForPairs[4] = p;
 		}
 		{// Fives [3]
 			Play p[] = { Play.DOUBLE, Play.DOUBLE, Play.DOUBLE, Play.DOUBLE,
 					Play.DOUBLE, Play.DOUBLE, Play.DOUBLE, Play.DOUBLE,
 					Play.HIT, Play.HIT };
 			playForPairs[3] = p;
 		}
 		{// Fours [2]
 			Play p[] = { Play.HIT, Play.HIT, Play.HIT, Play.HIT,
 					Play.HIT, Play.HIT, Play.HIT, Play.HIT, Play.HIT,
 					Play.HIT };
 			playForPairs[2] = p;
 		}
 		{// Threes [1]
 			Play p[] = { Play.HIT, Play.HIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT, Play.SPLIT, Play.HIT, Play.HIT, Play.HIT,
 					Play.HIT };
 			playForPairs[1] = p;
 		}
 		{// Deuces [0]
 			Play p[] = { Play.HIT, Play.HIT, Play.SPLIT, Play.SPLIT,
 					Play.SPLIT, Play.SPLIT, Play.HIT, Play.HIT, Play.HIT,
 					Play.HIT };
 			playForPairs[0] = p;
 		}
 		return playForPairs;
 
 	}
 }
