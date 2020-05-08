 package net.chunk64.french;
 
 import java.io.*;
 
 public class French
 {
 	public static File file;
 
 	public static void main(String[] args)
 	{
 		loadVocab();
 
 		Game game = new Game();
 		game.start();
 
 	}
 
 	public static void loadVocab()
 	{
 		System.out.print("Enter the absolute path to where your vocab file will be created.\n >");
 
 		String input;
 
 		while (true)
 		{
 			input = Game.scanner.nextLine();
 			file = new File(input);
 			if (file.exists())
 				break;
 			else
 				System.out.println("File not found!");
 		}
 
 		System.out.println(file.getAbsolutePath());
 		try
 		{
 			if (!file.exists())
 			{
 				file.createNewFile();
 				write("# Please enter vocab in format 'm/f:french:english', or just french:english if it is not a noun", "# Add as many as you want!", "m:chien:dog", "f:table:table");
 				log("Could not find vocab file! Creating...");
 
 			}
 
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			String line;
 			while ((line = reader.readLine()) != null)
 			{
 				if (line.startsWith("#"))
 					continue;
 
 				Vocab.createVocab(line);
 			}
 
 			if (Vocab.vocabList.isEmpty())
 				exit("No vocab was found!");
 
 			log("Loaded " + Vocab.vocabList.size() + " items of vocab.");
 
 		} catch (IOException e)
 		{
 			System.out.println("Could not load vocab: " + e.getMessage());
 			System.exit(0);
 
 		}
 
 
 	}
 
 	public static void write(String... messages)
 	{
 		try
 		{
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
 			for (String msg : messages)
 			{
 				writer.write(msg);
 				writer.newLine();
 			}
 			writer.close();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void log(String msg)
 	{
 		System.out.println("[|] " + msg);
 	}
 
 	public static void print(String msg)
 	{
 		System.out.print("[->] " + msg);
 	}
 
 	public static void exit(String msg)
 	{
 		log(msg);
 		System.exit(0);
 	}
 
}
