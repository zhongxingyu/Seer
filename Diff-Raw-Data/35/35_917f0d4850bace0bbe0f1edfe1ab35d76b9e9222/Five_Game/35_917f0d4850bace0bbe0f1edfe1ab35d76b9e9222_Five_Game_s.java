 import java.util.*;
 public class Five_Game {
 	int players[];
 	int count;
 	public int status[][];
 	public int scores[][][];
 	int row;
 	final int DefaultRow = 20;
 	final int ChessBlack=2;
 	final int ChessWhite=1;
 	int first;
 	boolean started;
 	ArrayDeque<P> stack;
 	int curr_player;
 	Five_UI ui;
 	public static final int WEST=0;
 	public static final int NORTH_WEST=1;
 	public static final int NORTH=2;
 	public static final int NORTH_EAST=3;
 	public static final int SCORE_LEVEL_1 = 8;
 	
 	public static final int WIN_FLAG = 100000;
 	public static final int WARN_FLAG = 1500;
 	boolean enableMouse;
 	int dict[][] = new int[5][1<<9];
	int mode[][] = {
 					{1,0,1,0,1},
 					{1,1,0,1},
 					{1,1,1},
 					{1,0,1,1},
 					{1,1,1,1},
 					{1,1,0,1,1},
 					{1,0,1,1,1},
 					{1,1,1,0,1},
 					{1,1,1,1,1}
 					};
 	int modeScore[] = {
 					100,
 					750,
 					1000,
 					750,
 					3000,
 					750,
 					750,
 					750,
 					100000
 	};
 	int modePunish[] = {
 			0,
			650,
			900,
			650,
 			2100,
 			0,
 			0,
 			0,
 			0
 };
 	public int[][] getStat()
 	{
 		return status;
 	}
 	public P getLastStep()
 	{
 		if(stack.size()>0)
 			return stack.getFirst();
 		else
 			return null;
 	}
 	public int[][][] getScore()
 	{
 		return scores;
 	}
 	public Five_Game()
 	{
 		//test
 		row=DefaultRow+1;
 		started=false;
 		curr_player =0;
 		first=0;
 		players = new int[2];
 		status = new int[row][row];
 		scores = new int [2][row][row];
 		enableMouse = true;
 		count =0;
 		stack = new ArrayDeque<P>();
 		dict();
 		printDict();
 		ui = new Five_UI(this);
 		
 	}
 	public void setPlayer(int pos,int fp)
 	{
 		if(pos>1||pos<0)return;
 		players[pos]=fp;
 	}
 	public void Regret()
 	{
 		if(stack.size()<2)return;
 		for(int i=0;i<2;i++)
 		{	
 			P tmp = stack.pop();
 			status[tmp.x][tmp.y] = 0;
 			refreshScore(tmp);
 		}
 		count-=2;
 		ui.fb.updateUI();
 	}
 	public P selectPointToGo(int pn)
 	{
 		int opp = 1 - pn;
 		ArrayList<P> a[] = new ArrayList[3];
 		a[0] = new ArrayList<P>();
 		a[1] = new ArrayList<P>();
 		a[2] = new ArrayList<P>();
 		for(int i=0;i<this.row;i++)
 		{
 			for(int j=0;j<this.row;j++)
 			{
 				a[0].add(new P(i,j).setScore(scores[0][i][j]));
 				a[1].add(new P(i,j).setScore(scores[1][i][j]));
 				a[2].add(new P(i,j).setScore(scores[1][i][j]+scores[0][i][j]));
 			}
 		}
 		Collections.sort(a[0]);
 		Collections.sort(a[1]);
 		Collections.sort(a[2]);
 		if(a[pn].get(0).score >= WIN_FLAG)
 		{
 			return a[pn].get(0);
 		}
 		if(a[opp].get(0).score >= WIN_FLAG)
 		{
 			return a[opp].get(0);
 		}
 		if(a[opp].get(0).score >= WARN_FLAG)
 		{
 			return a[opp].get(0);
 		}
 		System.out.println("NOT EMERGENCY SELECT MAX_SCORE_SUM");
 		return a[2].get(0);
 	
 		
 	}
 	public void move(int x,int y)
 	{
 		
 		if ((!inBoard(x,y))||status[x][y] != 0)
 			return;
 		status[x][y] = curr_player + 1;
 		P step = new P(x,y,curr_player+1);
 		this.stack.push(step);
 		this.refreshScore(step);
 		
 		ui.fb.updateUI();
 		if(checkWin(x,y))
 		{
 			ui.LTurns.setText("Player"+(1+curr_player)+" WIN!!");
 			this.NewGame();
 			return;
 		}
 		
 		count++;
 		curr_player = 1 - curr_player;
 		ui.LTurns.setText("Player"+(1+curr_player)+"'s Turn...");
 		
 		this.AIHelp();
 	}
 	public boolean checkWin(int x, int y )
 	{
 		for(int i =0;i<4;i++)
 		{
 			for(int j=0;j<5;j++)
 			{
 				if(checkWin(x,y,i,j))
 				{
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public boolean checkWin(int x,int y ,int direct,int offset)
 	{
 		int color = status[x][y];
 		switch(direct)
 		{
 		case WEST:
 			if(!(inBoard(x-offset,y)&&inBoard(x-offset+4,y)))
 			{
 				return false;
 			}
 			
 			for(int i=0;i<5;i++)
 			{
 				if(status[x-offset+i][y]!=color)
 				{
 					return false;
 				}
 			}
 			break;
 		case NORTH_WEST:
 			if(!(inBoard(x-offset,y-offset)&&inBoard(x-offset+4,y-offset+4)))
 			{
 				return false;
 			}
 			for(int i=0;i<5;i++)
 			{
 				if(status[x-offset+i][y-offset+i]!=color)
 				{
 					return false;
 				}
 			}
 			break;
 		case NORTH:
 			if(!(inBoard(x,y-offset)&&inBoard(x,y-offset+4)))
 			{
 				return false;
 			}
 			for(int i=0;i<5;i++)
 			{
 				if(status[x][y-offset+i]!=color)
 				{
 					return false;
 				}
 			}
 			break;
 		case NORTH_EAST:
 			if(!(inBoard(x+offset,y-offset)&&inBoard(x+offset-4,y-offset+4)))
 			{
 				return false;
 			}
 			for(int i=0;i<5;i++)
 			{
 				if(status[x+offset-i][y-offset+i]!=color)
 				{
 					return false;
 				}
 			}
 			break;
 		default:
 			System.err.println("Error !");
 			return false;
 			
 		}
 		return true;
 	}
 	public void StartGame()
 	{
 		curr_player = first;
 		status = new int[21][21];
 		scores = new int [2][row][row];
 		scores[0][10][10] = 100;
 		scores[1][10][10] = 100;
 		started = true;
 		stack.clear();
 		count=0;
 		ui.fb.updateUI();
 	}
 	public void AIHelp()
 	{
 		P next = this.selectPointToGo(curr_player);
 		System.out.println("AI select "+next.x+","+next.y +" for player"+curr_player);
 		status[next.x][next.y] = curr_player+1;
 		P step2 = new P(next.x,next.y,curr_player+1);
 		this.stack.push(step2);
 		this.refreshScore(step2);
		count++;curr_player = 1 - curr_player;
 		ui.fb.updateUI();
 		if(checkWin(next.x,next.y))
 		{
 			ui.LTurns.setText("Player"+(1+curr_player)+" WIN!!");
 			this.NewGame();
 			return;
 		}
		
 		ui.LTurns.setText("Player"+(1+curr_player)+"'s Turn...");
 	}
 	public void refreshScore(P pp)
 	{
 		P p = new P(pp);
 		for(int k=0;k<2;k++)
 		{
 			p.setColor(k+1);
 			if(this.isAvailable(p))
 			{
 				scores[k][p.x][p.y]=getScore(p);
 			}else{
 				scores[k][p.x][p.y] = 0;
 			}
 			for( int i=0;i<4;i++)
 			{
 				p.setDirect(i);
 				for( int j=1;j<5;j++)
 				{
 					P tmp = p.inc(j);
 					if(this.isAvailable(tmp))
 					{
 						scores[k][tmp.x][tmp.y]=getScore(tmp);
 					}
 				}
 				for( int j=1;j<5;j++)
 				{
 					P tmp = p.inc(-j);
 					if(this.isAvailable(tmp))
 					{
 						scores[k][tmp.x][tmp.y]=getScore(tmp);
 					}
 				}
 			}
 		}
 	}
 	public void NewGame()
 	{
 		//curr_player = first;
 		ui.initStatus();
 		started = false;
 	}
 	public P getAvailableLength(P p,int direct)
 	{
 		p.setDirect(direct);
 		int i1=0,i2=0;
 		P tmp;
 		tmp = p.inc();
 		for(;i1<4;i1++)
 		{
 			if(!this.inBoard(tmp)||((status[tmp.x][tmp.y]!=0)&&!(status[tmp.x][tmp.y]==p.color)))
 			{
 				break;
 			}
 			tmp = tmp.inc();
 		}
 		tmp = p.inc(-1);
 		for(;i2<4;i2++)
 		{
 			if(!this.inBoard(tmp)||((status[tmp.x][tmp.y]!=0)&&!(status[tmp.x][tmp.y]==p.color)))
 			{
 				break;
 			}
 			tmp = tmp.inc(-1);
 		}
 		return p.setAvailableLength(i1,i2);
 	}
 	public P getAvailableLength(P p)
 	{
 		return getAvailableLength(p,p.direct);
 	}
 	public int getScore(P p)
 	{
 		int s=0;
 		s+=getScoreLv1(p);
 		s+=getScoreLv2(p);
 		return s;
 	}
 	public int getScoreLv2(P p)
 	{
 		int s=0;
 		for(int i=0;i<4;i++)
 		{
 			this.getAvailableLength(p.setDirect(i));
 			s+=this.getLineScoreLv2(p);
 		}
 		return s;
 	}
 	public int getScoreLv1(P p)
 	{
 		int s=0;
 		for(int i=0;i<4;i++)
 		{
 			this.getAvailableLength(p.setDirect(i));
 			s+=this.getLineScoreLv1(p);
 		}
 		return s;
 	}
 	public int getLineScoreLv2(P p)
 	{
 		int len = p.l+p.r+1;
 		if(len<5)return 0;
 		int s=0;
 		int bs[] = new int[len];
 		int count = 0;
 		for( int i=p.l;i>0;i--)
 		{
 			P tmp = p.inc(i);
 			if(status[tmp.x][tmp.y]==p.color)
 			{
 				bs[count++]=1;
 			}else
 			{
 				bs[count++]=0;
 			}
 		}
 		bs[count++]=1;
 		for( int i=1;i<=p.r;i++)
 		{
 			P tmp = p.inc(-i);
 			if(status[tmp.x][tmp.y]==p.color)
 			{
 				bs[count++]=1;
 			}else
 			{
 				bs[count++]=0;
 			}
 		}
 		int n= getBSValue(bs);
 		s = dict[len-5][n];
 		return s;
 	}
 	public int getLineScoreLv1(P p)
 	{
 		int s = 0;
 		P tmp = p.inc();
 		for(int i=1;i<=p.l;i++)
 		{
 			if(status[tmp.x][tmp.y] == p.color)
 			{
 				s+= (int)(1<<(4-i));
 				break;
 			}
 			tmp = tmp.inc();
 		}
 		tmp = p.inc(-1);
 		for(int i=1;i<=p.r;i++)
 		{
 			if(status[tmp.x][tmp.y] == p.color)
 			{
 				s+= (int)(1<<(4-i));
 				break;
 			}
 			tmp = tmp.inc(-1);
 		}
 		return s;
 	}
 	public boolean inBoard(P p)
 	{
 		return (p.x>=0&&p.x<21)&&(p.y>=0&&p.y<21);
 	}
 	public boolean inBoard(int x,int y)
 	{
 		return (x>=0&&x<21)&&(y>=0&&y<21);
 	}
 	public boolean isAvailable(P p)
 	{
 		return inBoard(p)&&(status[p.x][p.y]==0);
 	}
 	public boolean isAvailable(int x,int y)
 	{
 		return inBoard(x,y)&&(status[x][y]==0);
 	}
 	public String getBSofValue(int BS,int length)
 	{
 		String t = "";
 		for(int k=0;k<length+5;k++)
 		{
 			if((BS&(1<<k))!=0)
 			{
 				t+="1";
 			}else
 			{
 				t+="0";
 			}
 		}
 		return t;
 	}
 	public void printDict()
 	{
 		for(int i=0;i<5;i++)
 		{
 			int l = 5+i;
 			for(int j=0;j<dict[i].length;j++)
 			{
 				if(dict[i][j]!=0)
 				{
 					String t = "";
 					for(int k=0;k<i+5;k++)
 					{
 						if((j&(1<<k))!=0)
 						{
 							t+="1";
 						}else
 						{
 							t+="0";
 						}
 					}
 					t+=" : "+dict[i][j];
 					System.out.println(t);
 				}
 			}
 		}
 	}
 	public void dict()
 	{
 		for(int i=5;i<10;i++)
 		{
 			for(int j=0;j<mode.length;j++)
 			{
 				writeDict(j,i);
 			}
 		}
		
 	}
 
 	public void writeDict(int modeNum,int dictNum)
 	{
		for(int i=0;i<dictNum-mode[modeNum].length;i++)
 		{
 			int curr_array[] = new int[dictNum];
 			writeDict(modeNum,curr_array,dictNum,i,0);
 		}
 	}
 	public void writeDict(int modeNum,int curr_array[],int dictNum,int pos,int curr)
 	{
 		int d[] = dict[dictNum-5];
 		if(curr >= dictNum)
 		{
 			int n = getBSValue(curr_array);
 			int score = modeScore[modeNum];//FIXME
 			if(pos==0||pos+mode[modeNum].length==dictNum)
 			{
 				score -= modePunish[modeNum];
 			}
 			if(d[n]<score)d[n]=score;
 			return;
 		}
 		if(curr == pos)
 		{
 			for(int i=0;i<mode[modeNum].length;i++)
 			{
 				curr_array[curr+i] = mode[modeNum][i];
 			}
 			curr = pos+mode[modeNum].length;
 			writeDict(modeNum,curr_array,dictNum,pos,curr);
 			return;
 		}
 		curr_array[curr] = 0;
 		writeDict(modeNum,curr_array,dictNum,pos,curr+1);
 		curr_array[curr] = 1;
 		writeDict(modeNum,curr_array,dictNum,pos,curr+1);
 	}
 	public int getBSValue(int b[])
 	{
 		int ret=0;
 		for(int i=0;i<b.length;i++)	{
 			if(b[i]==1)
 			{
 				ret+=1<<i;
 			}
 		}
 		return ret;
 	}
 
 }
