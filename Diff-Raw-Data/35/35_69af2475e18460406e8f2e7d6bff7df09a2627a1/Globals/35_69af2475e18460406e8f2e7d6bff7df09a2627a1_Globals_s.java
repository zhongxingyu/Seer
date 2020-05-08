 /**
  * @author Yixin Zhu
  */
 package com.comic.globals;
 
 import android.view.Menu;
 
 public class Globals {
 	public static final String VolZeroImage="http://samandfuzzy.com/imgint/volume0.gif";
 	public static final String VolZeroInfo="The first Sam and Fuzzy strips are a little rough " +
 			"around the edges, but are not without their hardcore advocates.";
 	public static final String VolZeroName="Volume 0: The Early Years";
 	public static final String VolOneImage="http://samandfuzzy.com/imgint/volume1.gif";
 	public static final String VolOneInfo="Enter Sam and Fuzzy's surreal world of dating disasters, " +
 			"taxi-driving action superstars, time-travelling bears and possessed" +
 			" refridgerators. Meet many of the series' strangest and most beloved " +
 			"characters in these early strips.";
 	public static final String VolOneName="Volume 1: Taxi-cab of Dreams";
 	public static final String VolTwoImage="http://samandfuzzy.com/imgint/volume2.gif";
 	public static final String VolTwoInfo="After botching a high-profile job and destroying his " +
 			"own taxi-cab, Sam is forced to search for a new career and face some old " +
 			"demons.";
 	public static final String VolTwoName="Volume 2: Growing Pains";
 	public static final String VolThreeImage="http://samandfuzzy.com/imgint/volume3.gif";
 	public static final String VolThreeInfo="Working as a bookstore clerk, Sam falls hard for a " +
 			"delivery woman with a terrible secret. Meanwhile, Fuzzy adjusts to his " +
 			"own secret life as a member of the Ninja Mafia crime syndicate's most " +
 			"inept squadran.";
 	public static final String VolThreeName="Volume 3: Love and War";
 	public static final String VolFourImage="http://samandfuzzy.com/imgint/volume4.gif";
 	public static final String VolFourInfo="What happens when the world's most famous hardcore heavy " +
 			"metal vocalist decides he'd rather sing about kittens than corpses? And what " +
 			"does he have to do with our titular duo, anyway? A tale of action, romance, " +
 			"comedy, tragedy, and ninjas.";
 	public static final String VolFourName="Volume 4: Noosehead";
 	public static final String VolFiveImage="http://samandfuzzy.com/images/vol5.gif";
 	public static final String VolFiveInfo="Troubled by ninja mafiosos? Gangster gerbils? Vampire stalkers? " +
 			"Don't worry! Sam and Fuzzy can fix your problem. Now, if only someone could fix " +
 			"theirs...";
 	public static final String VolFiveName="Volume 5: Sam and Fuzzy Fix Your Problem!";
 	public static final String VolSixName="Volume 6: Sam and Fuzzy Are Very Famous";
 	public static final String VolSixInfo="The most recent volume";
 	
 	public static final String StartImageURL = "http://samandfuzzy.com/comics/";
 	public static final String EndImageURL = ".gif";
 	
 	public static final String ZeroRange = "1-98";
 	public static final String OneRange = "99-229";
 	public static final String TwoRange = "230-374";
 	public static final String ThreeRange = "375-565";
 	public static final String FourRange = "566-1045";
 	public static final String FiveRange = "1046-1236";
 	public static final String SixRange = "1237";
 	
 	public static final String EndorseVolFive = "The start of a brand new epic, this volume is the " +
 			"perfect starting place for new readers!";
 	
 	public static final String[] VOL_RANGES = {ZeroRange, OneRange, TwoRange,
 		ThreeRange, FourRange, FiveRange, SixRange};
 	
 	public static final String CopyrightTitle = "Copyright";
 	public static final String CopyrightMessage =
 		"All the comics featured in this app are retrieved from Sam Logan's website www.samandfuzzy.com. " +
 		"The developers of this app do not claim any rights or ownership over the content. " + 
 		"We have the explicit approval from the owner to display the owners content. " +
 		"If you have any questions or concerns over copyright, feel free to contact us.";
 	public static final String HelpTitle = "SamAndFuzzyViewer-Help Menu";
 	
 	public static final int numZeros = 8;
 	public static final int MAX_VOLUMES = 6;
 	
 	/** Ids for guest comics which have a different image extension than gif */
	public static final int[] guest_img_ids = {198, 202, 206, 366, 367, 371, 374, 481, 550, 552, 553,
		554, 557, 710, 715, 718, 894, 897, 898, 899, 900, 1202, 1203, 1205, 1207, 1208, 1209, 1211};
 	
 	public static final int HELP_ID = Menu.FIRST;
 	public static final int COPYRIGHT_ID = Menu.FIRST + 1;
 }
