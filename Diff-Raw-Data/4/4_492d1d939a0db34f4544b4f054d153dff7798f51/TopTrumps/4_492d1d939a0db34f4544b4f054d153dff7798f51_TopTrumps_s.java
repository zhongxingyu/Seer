 
 package net.marcuswhybrow.uni.g52obj.cw1;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author G52OBJ
  */
 public class TopTrumps
 {
 	public TopTrumps(String fileName) throws IOException
 	{
 		BufferedReader reader = null;
 		IDeckBuilder deckBuilder = new DeckBuilder();
 		String line;
 		ArrayList<String> properties = new ArrayList<String>();
 		StringTokenizer tokenizer, lineTokenizer;
 
 		try
 		{
 			reader = new BufferedReader(new FileReader(fileName));
 		}
 		catch(FileNotFoundException ex)
 		{
 			System.err.println("File not found.");
 		}
 
 		// First line contians property names
 		line = reader.readLine();
 		tokenizer = new StringTokenizer(line, ",", false);
 
 		while(tokenizer.hasMoreTokens())
 		{
 			properties.add(tokenizer.nextToken());
 		}
 
		// Second line contains propert types
		line = reader.readLine();
		tokenizer = new StringTokenizer(line, ",", false);

 		while((line = reader.readLine()) != null)
 		{
 			lineTokenizer = new StringTokenizer(line, ",", false);
 			deckBuilder.newCard(lineTokenizer.nextToken());
 
 			for(int i = 1; i < properties.size(); i++)
 			{
 				deckBuilder.addProperty(properties.get(i), Integer.parseInt(lineTokenizer.nextToken()));
 			}
 		}
 
 		// Close the file
 		try
 		{
 			reader.close();
 		}
 		catch(IOException ex)
 		{
 			System.err.println("Unable to close file.");
 		}
 
 		Game game = new Game(deckBuilder.getDeck());
 		game.playGame();
 	}
 
 	public static void main(String[] args)
 	{
 		if(args.length != 1)
 		{
 			System.err.println("Incorrect number of arguments");
 			System.exit(-1);
 		}
 
 		try
 		{
 			new TopTrumps(args[0]);
 		}
 		catch(IOException ex)
 		{
 			System.err.println("Error accessing file");
 			ex.printStackTrace();
 		}
 	}
 }
