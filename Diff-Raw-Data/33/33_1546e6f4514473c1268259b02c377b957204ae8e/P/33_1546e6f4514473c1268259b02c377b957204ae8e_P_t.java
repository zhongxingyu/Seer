//import java.util.*;
 public class P implements Comparable<P>{// Represent Point or Tuple
 	public int x,y;
 	public int l,r;
 	public int color;
 	int direct;
 	int score;
 	public P(int a,int b)
 	{
 		x = a;
 		y = b;
 		l = r = 0;
 		color = 0;
 		direct = -1;
 		score = -1;
 	}
 	public P(P p)
 	{
 		x = p.x;
 		y = p.y;
 		l = p.l;
 		r = p.r;
 		color = p.color;
 		direct = p.direct;
 		score = p.score ;
 	}
 	public P(int a,int b,int c)
 	{
 		x=a;y=b;direct=-1;
 		l=r=0;
 		color = c;
 		score = -1;
 	}
 	public P(int a,int b,int c,int d)
 	{
 		x=a;y=b;direct=d;
 		l=r=0;
 		color = c;
 		score = -1;
 	}
 	public P setScore(int s)
 	{
 		score = s;
 		return this;
 	}
 	public P setColor(int c)
 	{
 		color = c;
 		return this;
 	}
 	public P setAvailableLength(int left,int right)
 	{
 		l = left;
 		r = right;
 		return this;
 	}
 	public P setDirect(int d)
 	{
 		direct = d;
 		l=r=0;
 		return this;
 	}
 	public P inc(int num)
 	{
 		switch(direct)
 		{
 		case Five_Game.WEST:
 			return new P(x-num,y,color,direct);
 		case Five_Game.NORTH_WEST:
 			return new P(x-num,y-num,color,direct);
 		case Five_Game.NORTH:
 			return new P(x,y-num,color,direct);
 		case Five_Game.NORTH_EAST:
 			return new P(x+num,y-num,color,direct);
 		default:
 			System.err.println("ERROR in P.inc():direct out of range");
 			return new P(-1,-1,0,0);
 		}
 	}
 	public P inc()
 	{
 		return inc(1);
 	}
 //	@Override
 //	public int compare(P arg0, P arg1) {
 //		// TODO Auto-generated method stub
 //		if(arg0.score>arg1.score)return 1;
 //		if(arg0.score<arg1.score)return -1;
 //		return 0;
 //	}
 //	public boolean equals(P arg0) {
 //		if(arg0.x == x &&
 //			arg0.y == y &&
 //			arg0.score == score &&
 //			arg0.color == color)return true;
 //		return false;
 //	}
 	@Override
 	public int compareTo(P arg0) {
 		if(arg0.score > score)return 1;
 		if(arg0.score == score)return 0;
 		// TODO Auto-generated method stub
 		return -1;
 	}
 	
 }
