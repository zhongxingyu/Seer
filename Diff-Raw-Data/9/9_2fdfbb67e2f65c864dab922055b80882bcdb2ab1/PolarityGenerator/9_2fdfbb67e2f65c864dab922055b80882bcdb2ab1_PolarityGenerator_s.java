 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.InputMismatchException;
 import java.io.PrintStream;
 import java.io.FileWriter;
 public class PolarityGenerator
 {
 	static File statisticsFile;
 	static final int NUM_STATS = 5;
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) throws IOException, ClassNotFoundException
 	{
 		final String wordsFilename = "words.tst";
 		String[] tokens;
 		String[] tweets;
 		String[] subjects;
 		String tweetData = "";
 		String[] allTweets;
 		double vote = 0;
 		String current;
 		PolarityValue v;
 		Scanner in = null;
 		Scanner stdIn = new Scanner(System.in);
 		scoreSheet userScores;
 		String user;
 		FileWriter voteWriter = null;
 		TST<PolarityValue> words = null;
 		Object obj = null;
 		
 		//Create files if do not exist
 		statisticsFile = new File("statistics.txt");
 		try {
 			statisticsFile.createNewFile();
 		} catch (IOException e1) {
 			System.out.println("Failed to Create statistics.txt");
 			System.exit(0);
 		}
 		
 		// Load TST from file
 		try
 		{
 			obj = ObjectLoader.load(wordsFilename);
 		}
 		catch (IOException e)
 		{
 			System.err.println("IOException: Failed to Load TST from + " + wordsFilename);
 			System.exit(0);
 		}
 		catch (ClassNotFoundException e)
 		{
 			System.err.println("ClassNotFoundException: Failed to Load TST from + " + wordsFilename);
 			System.exit(0);
 		}
 		if (obj instanceof TST<?>)
 			words = (TST<PolarityValue>)obj;
 		else
 		{
 			System.err.println("Class Mismatch: Could not load TST from " + wordsFilename);
 			System.exit(0);
 		}
 		if (words == null)
 		{
 			System.out.println("Failed to load words.tst");
 			System.exit(0);
 		}
 		
 		File voteFile = new File("votes.txt");
 		try {
 			voteWriter = new FileWriter(voteFile, true);
 		} catch (IOException e1) {
 			System.out.println("Failed to open vote write stream");
 			System.exit(0);
 		}
 		
 		
 		// Load Tweets
 		try {
 			in = new Scanner(new File("tweets.txt"));
 		} catch (FileNotFoundException e) {
 			System.out.println("Could not open tweets.txt. Aborting");
 			System.exit(0);
 		}
 		int j = 0;
 		boolean subject = true;
 		while (in.hasNext())
 		{
 			tweetData += in.nextLine() + "\n";
 		}
 		allTweets = tweetData.split("\\n");
 		subjects = new String[allTweets.length / 2];
 		tweets = new String[allTweets.length / 2];
 		for (int i = 0; i < allTweets.length && j < allTweets.length / 2; i++)
 		{
 			if (subject)
 			{
 				subjects[j] = allTweets[i];
 				subject = false;
 			}
 			else
 			{
 				tweets[j] = allTweets[i];
 				subject = true;
 				j++;
 			}
 		}
 		
 		
 		// Print Initial Message and Logon
 		System.out.print("Welcome to the Tweet rating system! Please use the following voting protocol:\n1 - Very Negative" +
 				"\n2 - Somewhat Negative\n3 - Neutral\n4 - SomewhatPositive\n5 - Very Positive\n6 - Skip Tweet\n9 - Save\n0 - Save & Exit\n--------------------\n" +
 				"Please Login: ");
 		user = stdIn.next();
 		userScores = readStats(user);
 		if (userScores == null)
 		{
 			System.out.println("Welcome " + user + "!");
 			userScores = new scoreSheet(user, 0, 0, 0, 0, 0);
 		}
 		else
 		{
 			int totalVotes = userScores.num1 + userScores.num2 + userScores.num3 + userScores.num4 + userScores.num5;
 			System.out.println("Welcome " + user + "!\nYour current statistics are:\nTotal Votes: " + 
 			 totalVotes + "\nVery Negative\tSomewhat Negative\tNeutral\t\tSomewhat Positive\tVery Positive\n" + 
 			(int)((double)userScores.num1 / (double)totalVotes * 100) + "%\t\t" +
 			(int)((double)userScores.num2 / (double)totalVotes * 100) + "%\t\t\t" +
 			(int)((double)userScores.num3 / (double)totalVotes * 100) + "%\t\t" +
 			(int)((double)userScores.num4 / (double)totalVotes * 100) + "%\t\t\t" +
 			(int)((double)userScores.num5 / (double)totalVotes * 100) + "%");
 		}
 		
 		
 		// Vote on Tweets
 		for (int k = 0; k < tweets.length; k++)
 		{
 			System.out.println("SUBJECT: " + subjects[k]);
 			current = tweets[k];
 			current = current.replaceAll("http:\\/\\/t.co\\/........", " ");
 			current = current.toLowerCase();
 			System.out.println("TWEET: " + current);
 			tokens = current.split("(\\W)+");		
 			// Generate Vote
 			
 			
 			do 
 			{
 				System.out.print("Score: ");
 				try
 				{
 					vote = (double)stdIn.nextInt();
 					System.out.println();
 				}
 				catch (InputMismatchException e)
 				{
 					System.out.println("\nIllegal input. Aborting.");
 					vote = 0;
 				}
 				if (vote == 0)
 				{
 					PrintStream writer = null;
 					try {
 						writer = new PrintStream(new File("tweets.txt"));
 					} catch (FileNotFoundException e) {
 						System.out.println("Failed to write remaining tweets");
 					}
 					for (;k < tweets.length; k++)
 					{
 						writer.print(subjects[k] + "\n");
 						writer.print(tweets[k] + "\n");
 					}
 					writer.flush();
 					writer.close();
 					ObjectLoader.save(words, wordsFilename);
 					writeStats(userScores);
 					System.exit(0);
 				} 
 				if (vote == 9)
 				{
 					ObjectLoader.save(words, wordsFilename);
 					writeStats(userScores);
 					System.out.println("Save Successful");
 				}
 			}
 			while (!(vote == 1 || vote == 2 || vote == 3 || vote == 4 || vote == 5 || vote == 6));
 			
 			if (vote == 6)
 				continue;
 			
 			
 			// Update User Scores
 			if (vote == 1)
 				userScores.num1++;
 			else if (vote == 2)
 				userScores.num2++;
 			else if (vote == 3)
 			{
 				userScores.num3++;
 			}
 			else if (vote == 4)
 				userScores.num4++;
 			else if (vote == 5)
 				userScores.num5++;
 			
 			vote -= 3;
 			vote *= 2.5;
 			try {
 				voteWriter.write(tweets[k] + "\n" + vote + "\n");
 				voteWriter.flush();
 			} catch (IOException e) {
 				System.out.println("Unable to write vote");
 			}
 			
 			
 			// Update Word Polarities
 			for (int i = 0; i < tokens.length; i++)
 			{
 				if (tokens[i].length() == 0 || tokens[i] == null)
 					continue;
 				if (!words.contains(tokens[i]))
 					words.put(tokens[i], new PolarityValue(0., 5));
 				v = words.get(tokens[i]);
 				// Change Value Accordingly
 				v.setScore((v.getScore() * v.getOccurrences() + vote) / (v.getOccurrences() + 1));
 				v.incrementOccurrences();
 			}
 		}
 	}
 	
 	// Save User Voting Statistics
 	public static void writeStats(scoreSheet s)
 	{
 		PrintStream stats;
 		Scanner statsReader;
 		String current = "";
 		String currentLine;
 		int i = 0;
 		boolean found = false;
 		try {
 			statsReader = new Scanner(statisticsFile);
 		} catch (FileNotFoundException e) {
 			System.out.println("Unable to save user statistics.");
 			return;
 		}
 		while (statsReader.hasNext())
 		{
 			currentLine = statsReader.nextLine();
 			if (currentLine.equals(s.user))
 			{
 				found = true;
 			}
 			if (i == NUM_STATS + 1)
 					found = false;
 			if (found)
 			{
 				i++;
 				continue;
 			}
 			current += currentLine + "\n";
 		}
 		current += s.user + "\n";
 		current += s.num1 + "\n";
 		current += s.num2 + "\n";
 		current += s.num3 + "\n";
 		current += s.num4 + "\n";
 		current += s.num5 + "\n";
 		try
 		{
 			stats = new PrintStream(statisticsFile);
 		}
 		catch (FileNotFoundException e) {
 			System.out.println("Unable to save user statistics.");
 			return;
 		}
 		System.out.println("Writing stats");
 		stats.print(current);
 		stats.flush();
 		stats.close();
 	}
 
 	// Load User Voting Statistics
 	public static scoreSheet readStats(String user)
 	{
 		Scanner stats;
 		String current;
 		try {
 			stats = new Scanner(statisticsFile);
 		} catch (FileNotFoundException e) {
 			return null;
 		}
 		while (stats.hasNext())
 		{
 			current = stats.nextLine();
 			if (current.equals(user))
 			{
 				return new scoreSheet(user, Integer.parseInt(stats.nextLine()), Integer.parseInt(stats.nextLine()), 
 									Integer.parseInt(stats.nextLine()), Integer.parseInt(stats.nextLine()), 
 									Integer.parseInt(stats.nextLine()));
 			}
 		}
 			return null;
 	}
 	// Stores User Voting Statistics
 	private static class scoreSheet
 	{
 		String user;
 		int num1, num2, num3, num4, num5;
 		public scoreSheet(String user, int num1, int num2, int num3, int num4, int num5)
 		{
 			this.user = user;
 			this.num1 = num1;
 			this.num2 = num2;
 			this.num3 = num3;
 			this.num4 = num4;
 			this.num5 = num5;
 		}
 	}
 
 }
