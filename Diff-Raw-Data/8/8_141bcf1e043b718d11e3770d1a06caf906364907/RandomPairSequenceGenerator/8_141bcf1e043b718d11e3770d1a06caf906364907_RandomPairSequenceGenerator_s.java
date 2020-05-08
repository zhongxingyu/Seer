 package com.josephbleau.applications;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.josephbleau.collections.*;
 import com.josephbleau.utilities.parsing.*;
 
 public class RandomPairSequenceGenerator {
 
 	public static Pair<Integer, String> validateArgs(String[] args) {
 		if(args.length != 3) {
 			System.out.println("Usage: randomizer [num_of_groups] [type] [filename]");
 			System.exit(-1);
 		}
 		
 		int num_groups = 0;
 		String type = args[1];
 		
 		try {
 			num_groups = Integer.parseInt(args[0]);
 		} 
 		catch (Exception e) {
 			System.out.println("Invalid number of groups, must be numeric");
 			System.exit(-1);
 		}
 		
 		if(num_groups <= 0) {
 			System.out.println("Invalid number of groups, must be greater than zero.");
 			System.exit(-1);
 		}
 		
 		if(!type.equals("valid") && !type.equals("invalid_grouping") &&
 		   !type.equals("unmatched_left") && !type.equals("unmatched_right")) {
 			System.out.println("Invalid type provided, must be one of: 'valid', " +
 					           "'invalid_grouping', 'unmatched_left', or 'unmatched_right'." +
 					           " You provided " + type);
 			System.exit(-1);
 		}
 		
 		return new Pair<Integer, String>(num_groups, type);
 	}
 	
 	public static String randomPair() {
 		Random generator = new Random();
 		
 		switch(generator.nextInt(4)) {
 			case 0: 
 				return "()";
 			case 1:
 				return "[]";
 			case 2:
 				return "{}";
 			case 3:
 				return "<>";
 			default:
 				return "";
 		}
 	}
 	
 	public static Character swapSingle(Character c) {
 		if (c == '(') return '['; if (c == ')') return ']';
 		if (c == '[') return '('; if (c == ']') return ')';
 		if (c == '{') return '<'; if (c == '}') return '>';
 		if (c == '<') return '{'; if (c == '>') return '}';
 		
 		return ' ';
 	}
 	
 	public static ArrayList<Integer> findPairEmptyStackpoints(String pairs) {
 		Stack<Character> stack = new Stack<Character>();
 		PairSequenceParser parser = new PairSequenceParser();
 		ArrayList<Integer> points = new ArrayList<Integer>();
 		
 		for(int i = 0; i < pairs.length(); i++) {
 			char c = pairs.charAt(i);
 			
 			if(stack.empty()){
 				points.add(i);
 			}
 			
 			if(parser.isLeftToken(c)) {
 				stack.push(c);
 			}
 			else if(parser.isRightToken(c)) {
 				if(stack.empty()) {
 					return points;
 				}
 				
 				if(!parser.isProperMatch(stack.pop(), c)) {
 					return points;
 				}
 			}
 		}
 		
 		return points;
 	}
 	
 	public static String generatePairs(int num, String type) {
 		String pairs = "";
 		
 		PairSequenceParser parser = new PairSequenceParser();
 		Random generator = new Random();
 		
 		for(int i = 0; i < num; i++) {
 			int insert_at = (pairs.length()>0) ? generator.nextInt(pairs.length()) : 0;
 			pairs = pairs.substring(0, insert_at) + randomPair() + pairs.substring(insert_at);
 		}	
 		
 		if(type == "valid") {
 			/* No other work required. */
		} else if(type == "invalid_grouping") {
 			/* Pick a random character and replace it with an equivalent-handed
 			 * character that isn't itself, e.g.: '(' becomes '[' */
 			
 			int replace = generator.nextInt(pairs.length());
 			if(replace > 0)
 				pairs = pairs.substring(0, replace) + swapSingle(pairs.charAt(replace)) + pairs.substring(replace+1);
 			else 
 				pairs = swapSingle(pairs.charAt(0)) + pairs.substring(1);
 			
		} else if(type == "unmatched_left") {
 			/* Iterate through the pairs and discover how many points in the string result in
 			 * an empty stack, these are our points for causing an unmatched left member. */
 			
 			ArrayList<Integer> empty_points = findPairEmptyStackpoints(pairs);
 			int insert = generator.nextInt(empty_points.size());
 			insert = empty_points.get(insert);
 			
 			pairs = pairs.substring(0, insert) + parser.randomLeftHand() + pairs.substring(insert);
		} else if(type == "unmatched_right") {
 			/* Same as unmatched_left, except we're going to insert a right-handed
 			 * character. */
 			
 			ArrayList<Integer> empty_points = findPairEmptyStackpoints(pairs);
 			int insert = generator.nextInt(empty_points.size());
 			insert = empty_points.get(insert);
 			
 			pairs = pairs.substring(0, insert) + parser.randomRightHand() + pairs.substring(insert);
 		}
 		
 		return pairs;
 	}
 	
 	public static void main(String[] args) {
 		Pair<Integer, String> valid_args = validateArgs(args);
 		String pairs = generatePairs(valid_args.mFirst, valid_args.mSecond);
 		
 		try {
 			FileWriter writer = new FileWriter(args[2]);
 			writer.write(pairs);
 			writer.close();
 		} catch (IOException e) {
 			System.out.println("Error, could not write to file " + args[2]);
 		}
 	}
 }
