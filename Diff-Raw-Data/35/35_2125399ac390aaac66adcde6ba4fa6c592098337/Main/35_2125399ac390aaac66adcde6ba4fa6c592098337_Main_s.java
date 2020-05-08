 package main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Scanner;
 
 import corpus.Corpus;
 
 import question.Question;
 
 public class Main 
 {
 	public static String readTextFile(String fullPathFilename) throws IOException 
 	{
 		String toReturn;
 		
 		StringBuffer sb = new StringBuffer(1024);
 		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
 				
 		char[] chars = new char[1024];
 		while (reader.read(chars) > -1)
 		{
 			sb.append(String.valueOf(chars));	
 		}
 
 		reader.close();
 		
 		toReturn = sb.toString();
 		
 		//System.out.print(toReturn);
 
 		return toReturn;
 	}
 	
 	public static void main(String[] args)
 	{	
 		Corpus corpus;
 		try 
 		{
 			corpus = new Corpus(readTextFile("corpus.txt"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Scanner scanner = new Scanner(System.in);
 		
 		while (scanner.hasNextLine())
 		{
 			Question question = new Question(scanner.nextLine());
 			
 			// solve(corpus, question)
 		}
 	}
 }
