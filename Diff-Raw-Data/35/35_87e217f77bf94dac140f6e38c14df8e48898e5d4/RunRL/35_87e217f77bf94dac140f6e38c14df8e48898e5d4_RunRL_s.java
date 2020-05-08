 package edu.columbia.stat.wood.pdia;
 
 import java.io.*;
 import java.util.HashMap;
 
 public class RunRL {
 
 	public static void main(String[] args) throws FileNotFoundException, IOException {
 
 		File objs = new File(args[0] + "results/PDIA_DMMs.gz");
 
 		ObjectOutputStream oos = null;
 		HashMap<Integer,Integer> actions      = new HashMap<Integer,Integer>();
 		HashMap<Integer,Integer> observations = new HashMap<Integer,Integer>();
 		HashMap<Integer,Integer> rewards      = new HashMap<Integer,Integer>();
 		int[][] train_a = Util.loadText(args[0] + "tiger_action_set.txt", actions);
 		int[][] test_a = Util.loadText(args[0] + "tiger_action_set.txt", actions);
 
 		int[][] train_o = Util.loadText(args[0] + "tiger_obs_set.txt", observations);
 		int[][] test_o = Util.loadText(args[0] + "tiger_obs_set.txt", observations);
 
 		int[][] train_r = Util.loadText(args[0] + "tiger_reward_set.txt", rewards);
 		int[][] test_r = Util.loadText(args[0] + "tiger_reward_set.txt", rewards);
 
 
		PDIA_DMM[] pdias = PDIA_DMM.sample(Integer.parseInt(args[1]), 100, Integer.parseInt(args[2]), new int[]{actions.size()-1,observations.size()-1,rewards.size()-1}, train_a, train_o, train_r);
 		for (PDIA_DMM pdia : pdias) {
 			double[] score = PDIA_DMM.score(new PDIA_DMM[]{pdia}, 0, test_a, test_o, test_r);
 			System.out.println("SingleMachinePrediction = " + Util.scoreToLogLoss(score));
 		}
 	}
 
 }
