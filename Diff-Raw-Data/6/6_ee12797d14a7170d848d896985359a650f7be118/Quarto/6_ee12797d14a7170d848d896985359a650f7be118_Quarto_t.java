 /**
  * @author Adam Chappell, Joel Lyles, Taylor Phebus, Forrest Stallings
  */
 
 import java.util.*;
 import java.util.concurrent.*;
 
 public class Quarto
 {
 	public static void main(String args[])
 	{
 		// This is the only place we want to declare an executor so that
 		// we can control the number of threads in the entire system
 		ExecutorService e = Executors.newCachedThreadPool();
 		BlockingQueue<Integer> q = new ArrayBlockingQueue<Integer>(1);
 		if (args.length > 0)
 		{
 			byte pieces[] = new byte[16];
 			for (int i = 0; i < 16; i++)
 			{
 				pieces[i] = (byte) Integer.parseInt(args[i]);
 			}
 			Runnable worker = new Game(pieces, q, e);
 			e.execute(worker);
 			int winner = Utils.waitAndGetResult(q);
 			System.out.println(winner);
 			//Game g=new Game(pieces);
 			//g.print();
 			//System.out.println(g.winner());
 			e.shutdown();
 		} else
 		{
 			Runnable worker = new Game(q, e);
 			e.execute(worker);
 			int winner = Utils.waitAndGetResult(q);
 			System.out.println(winner);
 			//System.out.println(new Game().winner());
 			e.shutdown();
 		}
 	}
 }
 
 class Game implements Runnable
 {
 	/*Possible draw state (For testing):
 		0|1|2|c 
 		3|4|5|8
 		9|a|f|6
 		7|d|0|b
 		0000|0001|0010|1100
 		0011|0100|0101|1000
 		1001|1010|1111|0110
 		0111|1101|0000|1011
 	*/
 	boolean player; //, set; set was a variable for the twist.
 	ExecutorService exec;
 	BlockingQueue<Integer> parentQueue;
 
 	/**
 	 * These are all the sets we are keeping for whether we have already solved a given state. 
 	 * We are mostly keeping these so we can write it out to the disk
 	 * We are only keeping the states with 4,8,or 12 pieces on the board
 	 * as a balance between the work and memory of keeping these sets, and 
 	 * the desire to have data to analyze and be able to exit a branch quickly. 	
 	 * We can actually keep the result in the same set as the game state itself
 	 * by utilizing the fact that the first piece in the upper left corner is 
 	 * always 0000. So, to store that player 1 won the game, we start the int
 	 * with 10. To store that player 2 won, we store 01. To store a tie, 11.
 	 * We are doing this because I'm incredible scared of the amount of memory 
 	 * we need to store these things. 
 	*/
 
 	public void run()
 	{
 		// TODO Review this
 		Utils.putAndWaitForSpace(this.parentQueue, this.winner());
 	}
 
 	/*Optomization, add later.
 	public static Set<Long> FourPiece=new HashSet<Long>();
 	public static Set<Long> EightPiece=new HashSet<Long>();
 	public static Set<Long> TwelvePiece=new HashSet<Long>();*/
 	public Set<Byte> pieces;
 	public byte[][] board;
 	protected byte CalculatedResult = -2;
 	final boolean DEBUG = true;
 
 	public Long toLong()
 	{
 		long r = (long) 0;
 		for (int i = 0; i < 4; i++)
 		{
 			for (int j = 0; j < 4; j++)
 			{
 				r += board[i][j];
 				r = r << 4;
 			}
 		}
 		r = r >> 4;
 		return r;
 	}
 
 	void debugp(String s)
 	{
 		if (DEBUG)
 		{
 			System.out.println(s);
 		}
 	}
 
 	void print()
 	{
 		debugp("" + Long.toHexString(toLong()));
 		System.out.println(player ? 2 : 1);
 		for (int i = 0; i < 4; i++)
 		{
 			for (int j = 0; j < 4; j++)
 			{
 				System.out.print(Integer.toHexString(((Byte) board[i][j]).intValue()) + "|");
 			}
 			System.out.println("");
 		}
 		System.out.flush();
 	}
 
 	public void transform()
 	{//Transform this gamestate so that it abides by rules that guarantee no states that are equivilant are represented differently.
 		//TODO: This
 	}
 
 	Game(byte[] piecesArr, BlockingQueue<Integer> queue, ExecutorService e)
 	{//Pass in 16 bytes. The first better be 0.
 		this.parentQueue = queue;
 		this.exec = e;
 		board = new byte[4][4];
 		int num0 = 0;
 
 		if (piecesArr[0] != 0)
 		{
 			System.err.println("Incorrect game state constructor");
 		}
 		int k = 0;
 		pieces = new LinkedHashSet<Byte>();
 		for (byte i = 0; i < 16; i++)
 		{
 			pieces.add(i);
 		}
 		for (int i = 0; i < 4; i++)
 		{
 			for (int j = 0; j < 4; j++)
 			{
 				if (piecesArr[k] == 0)
 				{
 					num0++;//Used to determine the player number
 				}
 				pieces.remove(piecesArr[k]);
 				board[i][j] = piecesArr[k];
 				k++;
 			}
 		}
 		if ((num0 % 2) == 1)
 		{
 			player = false;
 		}
 		transform();
 	}
 
 	//public byte chosen; chosen was a variable for the twist
 	Game(byte piece, int i, int j, Game previous, BlockingQueue<Integer> queue, ExecutorService e)
 	{
 		this.parentQueue = queue;
 		this.exec = e;
 		//Generates a new game state based on the placement.
 		board = new byte[4][4];
 		for (int I = 0; I < 4; I++)
 		{
 			for (int J = 0; J < 4; J++)
 			{
 				board[I][J] = previous.board[I][J];
 			}
 		}
 		board[i][j] = piece;
 		player = !previous.player;
 		pieces = new LinkedHashSet<Byte>();
 		pieces.addAll(previous.pieces);
 		pieces.remove(piece);
 		//TODO: Transform
 		transform();
 	}
 
 	/**
 	 * Instantiation of a new Game.
 	 * Creates the pieces and board.
 	 */
 	Game(BlockingQueue<Integer> queue, ExecutorService e)
 	{
 		this.parentQueue = queue;
 		this.exec = e;
 		player = false;//Start with P2, since the 0 piece was placed by P1
 		pieces = new LinkedHashSet<Byte>();
 		board = new byte[4][4];
 		for (byte i = 1; i < 16; i++)
 		{
 			pieces.add(i);
 		}
 		transform();
 	}
 
 	/**
 	 * Decides the winner.
 	 * @return The ID of the winner, or 0 for tie.
 	 */
 	int winner()
 	{
 		if (done())
 		{
 			if (DEBUG)
 			{
 				print();
 				System.out.println("Player = " + (player ? 2 : 1) + " Winner = " + CalculatedResult);
 			}
 			return CalculatedResult;
 		}
 		//TODO: Look up the winner in the hashMaps.
 		//TODO: With some propability p, divide into threads.
 		int winner = 3;
 		byte nextPiece;
 		Iterator<Byte> i = pieces.iterator();
 		nextMoves: while (i.hasNext())
 		{
 			nextPiece = i.next();
 			debugp("Next piece = " + nextPiece);
 			//For every piece
 			//For every placement
 			BlockingQueue<Integer> newQueue = new ArrayBlockingQueue<Integer>(16);
 			for (int j = 0; j < 4; j++)
 			{
 				for (int k = 0; k < 4; k++)
 				{
 					if (!(j == 0 && k == 0) && board[j][k] == 0)
 					{//Don't overwrite the top left.
 
 						if (Math.random() > 0.98)
 						{
 							Runnable worker = new Game(nextPiece, j, k, this, newQueue, this.exec);
 							this.exec.execute(worker);
 						} else
 						{
 							new Game(nextPiece, j, k, this, newQueue, this.exec).run();
 						}
 						/*
 						int winTemp = new Game(nextPiece, j, k, this).winner();
 						//winner=best of(winner, new Game(piece,placement,this).winner())
 						//TODO: Store result.
 						if (winTemp == 1 && !player)
 						{
 							debugp("Breaking for P1Win");
 							winner = 1;
 							break nextMoves;
 						}
 						if (winTemp == 2 && player)
 						{
 							debugp("Breaking for P2Win");
 							winner = 2;
 							break nextMoves;
 						}
 						winner = Math.min(winner, winTemp);
 						//Take the tie if that's an option.
 						 */
 					}
 				}
 			}
 
 			int winTemp = Utils.waitAndGetResult(newQueue);
			int j = 1;
			while ((winTemp == 3) && (j < 15))
			{
				winTemp = Utils.waitAndGetResult(newQueue);
				j++;
			}
 			//winner=best of(winner, new Game(piece,placement,this).winner())
 			//TODO: Store result.
 			if (winTemp == 1 && !player)
 			{
 				debugp("Breaking for P1Win");
 				winner = 1;
 				break nextMoves;
 			}
 			if (winTemp == 2 && player)
 			{
 				debugp("Breaking for P2Win");
 				winner = 2;
 				break nextMoves;
 			}
 			winner = Math.min(winner, winTemp);
 		}
 		if (winner == 3)
 		{
 			System.err.println("Incorrect Winner Assigned");
 		}
 		//TODO: Store result.
 		if (DEBUG)
 		{
 			print();
 		}
 		debugp("Player = " + (player ? 2 : 1) + " Winner = " + winner);
 		return winner;
 	}
 
 	/**
 	 * Figures out if the algorithm is done.
 	 * @return TRUE if done, FALSE otherwise.
 	 */
 	boolean done()
 	{
 		//If there is a winner
 		//Return true
 		CalculatedResult = result();
 		if (CalculatedResult > 0)
 			return true;
 		if (pieces.size() == 0)
 			return true;
 		//If out of pieces
 		//Return true
 		//Return false
 		return false;
 
 	}
 
 	/**
 	 * The result of the current gamestate?
 	 * @return 0 for tie, 1 for player 1 wins, 2 for player 2 wins
 	 * -1 for inconsistent gamestate
 	 */
 	byte result()
 	{
 		byte mask1 = 0x01;
 		byte mask2 = 0x02;
 		byte mask3 = 0x04;
 		byte mask4 = 0x08;
 		//For all rows
 		rows: for (int i = 0; i < 4; i++)
 		{
 			//Are all the pieces in this row placed
 			for (int j = 0; j < 4; j++)
 			{
 				if (board[i][j] == 0 && !(j == 0 && i == 0))
 				{
 					continue rows;
 				}
 			}
 			//For all attributes
 			//Are the cols all 0?
 			byte temp1 = 0;
 			byte temp2 = 0;
 			byte temp3 = 0;
 			byte temp4 = 0;
 			for (int j = 0; j < 4; j++)
 			{
 				temp1 += board[i][j] & mask1;
 				temp2 += board[i][j] & mask2;
 				temp3 += board[i][j] & mask3;
 				temp4 += board[i][j] & mask4;
 			}
 			if (temp1 == 0 || temp2 == 0 || temp3 == 0 || temp4 == 0)
 			{
 				debugp("Returning Winner-0-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
 				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
 			}
 			temp1 = mask1;
 			temp2 = mask2;
 			temp3 = mask3;
 			temp4 = mask4;
 			//Are the cols all 1?
 			for (int j = 0; j < 4; j++)
 			{
 				temp1 = (byte) (temp1 & board[i][j]);
 				temp2 = (byte) (temp2 & board[i][j]);
 				temp3 = (byte) (temp3 & board[i][j]);
 				temp4 = (byte) (temp4 & board[i][j]);
 			}
 			if (temp1 == 0x01 || temp2 == 0x02 || temp3 == 0x04 || temp4 == 0x08)
 			{
 				debugp("Returning Winner-1-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
 				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
 			}
 		}
 		//For all cols
 		cols: for (int i = 0; i < 4; i++)
 		{
 			//Are all the pieces in this row placed
 			for (int j = 0; j < 4; j++)
 			{
 				if (board[j][i] == 0 && !(j == 0 && i == 0))
 				{
 					continue cols;
 				}
 			}
 			//For all attributes
 			//Are the cols all 0?
 			byte temp1 = 0;
 			byte temp2 = 0;
 			byte temp3 = 0;
 			byte temp4 = 0;
 			for (int j = 0; j < 4; j++)
 			{
 				temp1 += board[j][i] & mask1;
 				temp2 += board[j][i] & mask2;
 				temp3 += board[j][i] & mask3;
 				temp4 += board[j][i] & mask4;
 			}
 			if (temp1 == 0 || temp2 == 0 || temp3 == 0 || temp4 == 0)
 			{
 				debugp("Returning ColWinner-0-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
 				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
 			}
 			temp1 = mask1;
 			temp2 = mask2;
 			temp3 = mask3;
 			temp4 = mask4;
 			//Are the cols all 1?
 			for (int j = 0; j < 4; j++)
 			{
 				temp1 = (byte) (temp1 & board[j][i]);
 				temp2 = (byte) (temp2 & board[j][i]);
 				temp3 = (byte) (temp3 & board[j][i]);
 				temp4 = (byte) (temp4 & board[j][i]);
 			}
 			if (temp1 == 0x01 || temp2 == 0x02 || temp3 == 0x04 || temp4 == 0x08)
 			{
 				debugp("Returning ColWinner-1-" + temp4 + "|" + temp3 + "|" + temp2 + "|" + temp1);
 				return player ? (byte) 2 : (byte) 1;//The person who played the piece wins. If player==true, it was player 2. Otherwise it was player 1.
 			}
 		}
 
 		//Otherwise, return 0 if we are out of pieces.
 		if (pieces.size() == 0)
 		{
 			//System.err.println("Draw");
 			return 0;
 		}
 		return -1;
 	}
 }
 
 // Various wrappers for concurrency
 class Utils
 {
 	public static Integer waitAndGetResult(BlockingQueue<Integer> q)
 	{
 		try
 		{
 			return q.take();
 		} catch (InterruptedException e)
 		{
 			System.err.println("InterruptedException in Utils.waitAndGetResult: " + e.toString());
 		}
 		return -1;
 	}
 
 	public static void putAndWaitForSpace(BlockingQueue<Integer> q, Integer e)
 	{
 		try
 		{
 			q.put(e);
 		} catch (InterruptedException e1)
 		{
 			System.err.println("InterruptedException in Utils.putAndWaitForSpace: " + e.toString());
 		}
 	}
 }
