 package com.techtalk4geeks.blogspot;
 
 import java.util.ArrayList;
 
import android.content.Loader.ForceLoadContentObserver;
 
 public class User
 {
 	String myName = "User.getName()";
 	int myLevel = 1;
 	int myRank;
 //	Date myBirthYear;
 	protected int myHP;
 	protected int mySP;
 	
 	protected int myPOW;
 	protected int myDEF;
 	protected int mySPEED;
 	int myAge;
 	String[] rankStrings = new String[] {
 			"I Do Not Care",
 			"Dork",
 			"Geek",
 			"Nerd",
 			"Jock",
 			"Blonde",
 			"Teacher",
 			"Hippie",
 			"Animal Lover",
 			"Shortie",
 			"Stretch",
 			"Hobbit",
 			"Dwarf",
 			"Vampire",
 			"Ninja",
 			"Gangster",
 			"Emo",
 			"Professor",
 			"Coder",
 			"Fox",
 			"Werewolf",
 			"Zombie",
 			"Narwhal",
 			"Alien",
 			"Swag Master",
 			"Princess",
 			"Crafter",
 			"Gamer",
 			"Vlogger",
 			"Predator",
 			"Wrestler",
 			"Super Hero",
 			"Rich Person",
 			"Baby",
 			"Grandparent",
 			"Biker",
 			};
 
 	ArrayList<Item> inventory = new ArrayList<Item>();
 	public static ArrayList<String> rank_display = new ArrayList<String>();
 	public User(String name, String rank, int age)
 	{
 		for (int i = 0; i < rankStrings.length; i++)
 		{
 			rank_display.add(rankStrings[i]);
 		}
 //		myBirthYear = birthYear;
 //		Date date = new Date(System.currentTimeMillis()) + 2100;
 		myAge = age;
 		myName = name;
 		myRank = rank_display.indexOf(rank);
 //		myLevel = (int)(age * 2);
 		mySP = (int)(15); //myLevel * 1.5 / 5
 		myHP = (int)(17);
 		myPOW = (int)(mySP * 0.4);
 		myDEF = (int)(mySP * 0.6);
 		mySPEED = 5;
 	}
 
 	public String getName()
 	{
 		return myName;
 	}
 
 	public void setName(String name)
 	{
 		myName = name;
 	}
 
 	public int getLevel()
 	{
 		return myLevel;
 	}
 
 	int getHP()
 	{
 		return myHP;
 	}
 
 	void setHP(int myHP)
 	{
 		this.myHP = myHP;
 	}
 
 	int getSP()
 	{
 		return mySP;
 	}
 
 	void setSP(int mySP)
 	{
 		this.mySP = mySP;
 	}
 
 	int getPOW()
 	{
 		return myPOW;
 	}
 
 	void setPOW(int myPOW)
 	{
 		this.myPOW = myPOW;
 	}
 	
 	int getDEF()
 	{
 		return myDEF;
 	}
 
 	void setDEF(int myDEF)
 	{
 		this.myDEF = myDEF;
 	}
 
 	int getSPEED()
 	{
 		return mySPEED;
 	}
 	
 	public void changeSPEEDby(int changedBy)
 	{
 		mySPEED = mySPEED + changedBy;
 	}
 
 	void setSPEED(int mySPEED)
 	{
 		this.mySPEED = mySPEED;
 	}
 }
