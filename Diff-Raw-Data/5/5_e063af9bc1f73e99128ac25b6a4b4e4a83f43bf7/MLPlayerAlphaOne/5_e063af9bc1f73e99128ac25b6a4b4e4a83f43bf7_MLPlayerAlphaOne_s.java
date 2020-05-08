 import java.io.*;
 import java.net.*;
 
 public class MLPlayerAlphaOne
 {
 	// An internal board representation.
 	private int internal_board[][] = null;
 
 	// The player.
 	Player thePlayer;
 
 	// The player id of this player.
 	int player_id = 0;
 
 	// Constructor.
 	public MLPlayerAlphaOne(Player p)
 	{
 		thePlayer=p;
 	}
 
 	// The play function, which connects to the socket and plays.
 	public void play(int my_id) throws IOException,ClassNotFoundException
 	{
 		// Open socket and in/out streams.
 		Socket sock = new Socket("localhost", Player.getSocketNumber(thePlayer));
 		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
 		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
 		BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
 		
 		Weights weights = new Weights("weights.txt");
 		
 		/*
 		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		//ONLY USE FOR INITAL WEIGHTS! (FIRST RUN)
 		double[] initialWeights = new double[FeatureExplorer.getNumFeatures()];
 		for(int x = 0; x < FeatureExplorer.getNumFeatures(); x++)
 		{
 			initialWeights[x] = 1/((double)FeatureExplorer.getNumFeatures());
 		}
 		weights.setWeights(initialWeights);
 		weights.saveWeights();
 		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		//*/
 		
 		
 		// Set the id.
 		player_id = my_id;
 
 		// Get the game rules.
 		Rules gameRules = (Rules)in.readObject();
 		System.out.printf("Num Rows: %d, Num Cols: %d, Num Connect: %d\n", gameRules.numRows, gameRules.numCols, gameRules.numConnect);
 
 		// Create the internal board.
 		internal_board = new int[gameRules.numRows][gameRules.numCols];
 		for (int r = 0; r < gameRules.numRows; r++)
 			for (int c = 0; c < gameRules.numCols; c++)
 				internal_board[r][c] = 0;
 
 		// Start playing the game, first by waiting fo the initial message.
 		System.out.println("Waiting...");
 		GameMessage mess = (GameMessage)in.readObject();
 
 		double Qsa = -1;
 		boolean beginning = true;
 		// The main game loop.
 		int move = 0;
 		int selected_column = 0;
 		while(mess.win == Player.EMPTY)
 		{
 			// weights.printWeights();
 			// If the first message is not the begin message (-1), then record what the other player did.
 			if(mess.move != -1)
 			{
 				int r = 0;
 				for (r = 0; r < gameRules.numRows; r++)
 				{
 					if (internal_board[r][mess.move] != 0)
 					{
 						internal_board[r - 1][mess.move] = player_id % 2 + 1;
 						break;
 					}
 				}
 				if (r == gameRules.numRows) internal_board[r - 1][mess.move] = player_id % 2 + 1;
 			}
 			else
 			{
 				mess.move = (int)((float)gameRules.numCols * Math.random());
 				out.writeObject(mess);
 				mess = (GameMessage)in.readObject();
 				internal_board[gameRules.numRows-1][mess.move] = player_id;
 				continue;
 			}
 			
 			if(beginning == false)
 			{
 				System.out.println("Qsa: " + Qsa + "\tplayer_id: " + player_id);
 				
 				sarsa(0,weights,Qsa,internal_board,gameRules,player_id);
 			}
 
 			// Create features based on the current board layout.
 			FeatureExplorer[] ff = new FeatureExplorer[gameRules.numCols];
 			boolean[] ff_use = new boolean[gameRules.numCols];
 			int numFeatures = FeatureExplorer.getNumFeatures();
 			double[][] features = new double[gameRules.numCols][numFeatures];
 			double[] wx = new double[gameRules.numCols];
 			double[] sig = new double[gameRules.numCols];
 			double[] w = weights.getWeights();
 			double max = 0;
 			int action = 0;
 			double temp;
 			for(int x = 0; x < gameRules.numCols; x++)
 			{
 				ff[x] = new FeatureExplorer();
 				ff_use[x] = ff[x].initialize(internal_board, gameRules.numRows, gameRules.numCols, x, player_id);
 				
 				
 				if(ff_use[x])
 					features[x] = ff[x].getFeatures();
 				
 				//System.out.println("features:");
 				//printD(features[x]);
 				for(int y = 0; y < numFeatures; y++)
 				{
 					//System.out.println("ff_use: " + ff_use[x]);
 					if(ff_use[x])
 					{
 						//if(beginning)
 						//{
 						//	if(mess.move == x)
 						//		wx[x] += ((double)features[x][y])*w[y];
 						//	else
 						//		wx[x] = 0;
 						//}
 						//else
 						wx[x] += ((double)features[x][y])*w[y];
 					}
 					else
 						wx[x] = 0;
 				}
 				if(ff_use[x])
 					sig[x] = sigmoid(wx[x]);
 				else
 					sig[x] = 0;
 					
				//System.out.printf("wx[%d]: %f sig[%d]: %f\n",x,wx[x],x,sig[x]);
 				if(x == 0)
 				{
 					max = sig[0];
 					action = 0;
 				}
 				else if(sig[x] > max)
 				{
 					action = x;
 					max = sig[x];
 				}
 			}
 			
 			Qsa = max;
 			
 			double epsilon = 0.1;
 			if(Math.random() < epsilon)
 				selected_column = action;
 			else
 				selected_column = (int)(Math.random()*gameRules.numCols);
 				
 			// Update the internal representation for where this player put his token.
 			int r = 0;
 			for (r = 0; r < gameRules.numRows; r++)
 			{
 				if (internal_board[r][selected_column] != 0)
 				{
 					internal_board[r - 1][selected_column] = player_id;
 					break;
 				}
 			}
 			if (r == gameRules.numRows) internal_board[r - 1][selected_column] = player_id;
 
 			// Write the game message and read the next one.
 			mess.move = selected_column;
 			out.writeObject(mess);
 			mess = (GameMessage)in.readObject();
 			
 			beginning = false;
 		}
 		
 		System.out.println("Qsa: " + Qsa + "\tplayer_id: " + player_id);
 		//if we win, reward 1, else reward 0
 		if((mess.win).equals(thePlayer))
 			sarsa(1, weights, Qsa,internal_board,gameRules,player_id);	//reward 1 for win
 		else
 			sarsa(0, weights, Qsa,internal_board,gameRules,player_id);	//reward 0 for loss
 		
 		//if(player_id==1)
 		weights.saveWeights();
 
 		// Close the socket.
 		sock.close();
 	}
 	
 	private double sigmoid(double t)
 	{
 		return 1/(1+Math.exp(-t));
 	}
 	
 	private void sarsa(int reward, Weights weights, double Qsa, int[][] internal_board, Rules gameRules, int player_id)
 	{
 		double eta = 0.9;
 		double gamma = 0.9;
 		FeatureExplorer f = new FeatureExplorer();
 		double[] features = new double[FeatureExplorer.getNumFeatures()];
 		double[] w = weights.getWeights();
 		double wx = 0;
 		double sig = 0;
 		double xx = 0;
 		double Qsa_new = 0;
 		
 		f.initialize(internal_board, gameRules.numRows, gameRules.numCols, -1, player_id);
 		features = f.getFeatures();
 		
 		//debug printing
 		//System.out.println("features:");
 		//printD(features);
 		//weights.printWeights();
 		for(int x = 0; x < f.getNumFeatures(); x++)
 		{
 			wx += w[x]*features[x];
 		}
 		//System.out.println("wx: " + wx);
 		
 		sig = sigmoid(wx);
 		
 		//System.out.println("sig: " + sig);
 		
 		Qsa_new = Qsa + eta*((reward+gamma*sig)-Qsa);
 		
 		//System.out.println("Qsa: " + Qsa + "\nQsa_new: " + Qsa_new);
 		
 		
 		for(int x = 0; x < features.length; x++)
 		{
 			xx+=features[x]*features[x];
 		}
 		for(int x = 0; x < features.length; x++)
 		{
			w[x] = w[x]-eta*(Qsa-gamma*Qsa_new)*((Qsa*(1-Qsa)*features[x]));
 		}
 		weights.setWeights(w);
 		
 	}
 	
 	private void printD(double[] array)
 	{
 		for(int i = 0; i < array.length; i++)
 		{
 			System.out.println(array[i]);
 		}
 	}
 	
 	// The main function.
 	public static void main(String[] args)
 	{
 		// If no argument is specified, throw an error.
 		if(args.length != 1)
 		{
 	    		System.out.println("Usage:\n java MPLayerAlphaOne [1|2]");
 	    		System.exit(-1);
 		}
 
 		// Get the player.
 		int my_id = Integer.parseInt(args[0]);
 
 		// Set the player object.
 		Player p = null;
 		if (my_id == 1) p = Player.ONE;
 		else if (my_id == 2) p = Player.TWO;
 		else
 		{
 			System.out.println("Usage:\n java MPLayerAlphaOne [1|2]");
 			System.exit(-1);
 		}
 
 		// Create the MLPlayer object, and begin play.
 		MLPlayerAlphaOne me = new MLPlayerAlphaOne(p);
 		try
 		{
 			me.play(my_id);
 		}
 		catch (IOException ioe)
 		{
 			ioe.printStackTrace();
 		}
 		catch (ClassNotFoundException cnfe)
 		{
 			cnfe.printStackTrace();
 		}
 	}
 }
 
