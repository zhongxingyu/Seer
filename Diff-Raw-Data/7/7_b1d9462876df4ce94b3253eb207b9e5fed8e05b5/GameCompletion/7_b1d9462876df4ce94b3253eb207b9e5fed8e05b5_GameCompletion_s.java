 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 //Still needs to add an implementation that includes the game completion time display/info
 public class GameCompletion extends JPanel
 {
 	//So I know what to draw to
 	private Frame parent;
 	//The image to display at the top of the screen
 	private BufferedImage titleImg;
 	//Displayed at the bottom of the screen as a "highscore" indicator
 	private int completionTime;
 	//Used to hold all of the things that the credits will play!
 	private int index = 0; //Used to move through the Credits ArrayList
 	private ArrayList<String> credits = new ArrayList<String>();
 	private Point p1 = new Point(100, 540);
 	private String s1;
 	private Point p2 = new Point(100, 690);
 	private String s2;
 	private Point p3 = new Point(100, 840);
 	private String s3;
 	private boolean creditsDone = false;
 	public GameCompletion(Frame fIn, int ctIn)
 	{
 		parent = fIn;
 		initCredits();
 		completionTime = ctIn;
 		loadTitleImage("bin\\images\\MainMenu\\Title.png");
 	}
 	private void loadTitleImage(String path)
 	{//Try to Load the Image from the bin Images
 		try
 		{
 			System.out.println(path);
 			titleImg = ImageIO.read(new File(path));
 		}
 		catch(IOException e)
 		{
 			//Oh no, we didn't find the image (that's evil!)
 			System.out.println("Unable to load Title Image!");
 		}
 	}
 	@Override
 	public void paint(Graphics gIn)
 	{
 		super.paint(gIn);
 		//System.out.println(p1.y + " " + p2.y + " " + p3.y);
 		Graphics2D g2 = (Graphics2D)gIn; //Draw Whatever Needs to Be Drawn!
 		g2.drawImage(titleImg, null, 0, 0); //Draw the title of the Game
 		g2.setFont(new Font("TimesRoman", Font.BOLD, 20));
 		
 		if(creditsDone == false)
 		{ //As long as there are still credits to go
 			if(p1.y <= 150)
 			{ //The top credit has hit the top of the screen! Oh no!
 				s1 = s2; //That's okay, lets swap element 1 and 2
 				p1.y = 300; //and move 3 to the same position as it was
 				s2 = s3; //and lets swap 2 and 3
 				p2.y = 450; //and move 3 to the same position as it was
 				s3 = credits.get(index); //Lets pull the new element to display
 				index++; //Make sure we are ready to get the next element, later
 				p3.y = 600; //and put the new element at the bottom of the screen
 				if(credits.size() <= index)
 				{ //Make sure we stop pulling elements when we've got no more!
 					creditsDone = true; //Thus, return to the mainmenu
 				}
 			}
 			p1.y -= 3; //Move up 3 pixels per pause
 			p2.y -= 3;
 			p3.y -= 3;
 			if(p1.y <= 530) //so we don't overlap with the time of completion
 			{
 				g2.drawString(s1, p1.x, p1.y);
 			}
 			if(p2.y <= 530)
 			{
 				g2.drawString(s2, p2.x, p2.y);
 			}
 			if(p3.y <= 530)
 			{
 				g2.drawString(s3, p3.x, p3.y);
 			}
 		}
 
 		g2.drawString(new String("Completion Time: " + Integer.toString(completionTime)), 340, 550); //Show the player their time
 		Util.sleep(100); //Sleep
 		if(creditsDone)
 		{
			parent.restart(); //Display the mainmenu with everything reset. 
			parent.remove(this);
 		}
 	}
 	private void initCredits()
 	{
 		//Empty Strings are Used as Buffers for the Moving Text
 		credits.add("You're Incredible! You Beat Our Game!!");
 		credits.add("Credits");
 		credits.add("GAME DEVELOPERS");
 		credits.add("Blake Tattoli: Librarian/Client interaction lead (Prophet of the End)");
 		credits.add("Branden Kroske: Project Manager (Lord and master of all he surveys)");
 		credits.add("Daniel Siebert: Gui/Blood artist (Master of Carnage)");
 		credits.add("John Bell: Graphics/lighting engineer (Lord of Darkness)");
 		credits.add("Zachary Easey: Framework Engineer (Voodoo Priest of Code)");
 		credits.add("WORKS CITED");
 		credits.add("Metroid Fusion: Character Sprites");
 		credits.add("");
 		credits.add("");
 		credits.add("");
 		s1 = credits.get(index);
 		index++;
 		s2 = credits.get(index);
 		index++;
 		s3 = credits.get(index);
 		index++;
 	}
 }
