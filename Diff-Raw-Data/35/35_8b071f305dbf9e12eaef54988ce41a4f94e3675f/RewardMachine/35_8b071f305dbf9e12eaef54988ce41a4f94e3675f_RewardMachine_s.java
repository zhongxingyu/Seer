 package com.caramelcode.mcstudent;
 
 import com.caramelcode.mcstudent.util.NumberHelper;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 /**
  * State machine that gives players rewards based on their reward streak state.
  * 
  * @author jsprague
  * 
  */
 public class RewardMachine {
 	private static int correctCount = 0;
 	private static String rewardString = "";
 
 	public static void resetCorrectCount() {
 		RewardMachine.correctCount = 0;
 	}
 
 	public static int incrementCorrectCount() {
 		return ++RewardMachine.correctCount;
 	}
 
 	public static int getCorrectCount() {
 		return RewardMachine.correctCount;
 	}
 
 	public static void setCorrectCount(int correctCount) {
 		RewardMachine.correctCount = correctCount;
 	}
 
 	public static ItemStack getRewardItemStack() {
 		Item rewardItem = Item.ingotIron;
 		int qty = 1;
 
 		if (RewardMachine.correctCount < 0) {
 			RewardMachine.correctCount = 0;
 		}
 
 		switch (RewardMachine.correctCount) {
 			case 0:
 			case 1:
 				rewardItem = Item.ingotIron;
 				break;
 			case 2:
 				rewardItem = Item.ingotIron;
 				qty = 2;
 				break;
 			case 3:
 				rewardItem = Item.ingotGold;
 				break;
 			case 4:
 				rewardItem = Item.ingotGold;
 				qty = 2;
 				break;
 			case 5:
 				rewardItem = Item.emerald;
 				break;
 			default:
 				rewardItem = Item.diamond;
 				break;
 		}
 
 		ItemStack itemStack = new ItemStack(rewardItem, qty);
 		rewardString = qty + " x " + itemStack.getDisplayName();
 		return itemStack;
 	}
 
 	public static String getRewardString() {
 		return rewardString;
 	}
 
 	public static String getPraiseMessage() {
 		String[] praise = { "Well done!", 
 				"Hooray!", 
 				"You are awesome!", 
 				"Yay! Yay!! Yay!!!",
 				"Super-dee-duper!", 
 				"You're a Minecraft Pro!", 
 				"Definitely worth a reward!",
 				"You got it right! :D", 
 				"Yippee!", 
 				"Clap Clap Clap!", 
 				"Good Answer!",
 				"You're Brilliant!", 
 				"Fantastic!", 
 				"Amazing!", 
 				"Super Smart!", 
 				"Great job!",
 				"Right On!", 
 				"Perfect!", 
 				"Excellent!", 
 				"Very intelligent!",
 				"Yipee yay yay!",
 				"Super cool!",
 				"Meow!"};
 		
		return praise[NumberHelper.rand(praise.length - 1)];
 	}
 }
