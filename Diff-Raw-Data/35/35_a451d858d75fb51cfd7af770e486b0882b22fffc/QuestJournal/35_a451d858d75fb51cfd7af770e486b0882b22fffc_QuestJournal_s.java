 package quest;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import com.golden.gamedev.object.font.SystemFont;
 
 public class QuestJournal
 {
 	private ArrayList<Quest> myQuests;
 	private boolean showJournal = false;
 	public static final int INDENT = 10;
 	public static final Color journalColor = Color.darkGray;
 	
 	public QuestJournal()
 	{
 		myQuests = new ArrayList<Quest>();
 	}
 	
 	public void addQuest(Quest qu, QuestGiver qg)
 	{
 		myQuests.add(qu);
 		qg.addQuest(qu);
 	}
 	
 	public void removeQuest(Quest qu)
 	{
 		myQuests.remove(qu);
 	}
 
 	public void update() 
 	{
 		for (Quest qu: myQuests)
 		{
 			qu.update();
 		}
 	}
 	
 	public void showJournal(Graphics2D g)
 	{
 		if (!showJournal)
 			return;
 
 		 SystemFont font = new SystemFont(new Font("Arial", Font.BOLD, 12), new Color(255, 255, 255));
 		 
 		 drawQuestSlots(g);
 		 
 		 font.drawString(g, "Quest Log", SystemFont.LEFT, INDENT, 40, 70);
 		 
 		 for (int i = 0; i < 9; i++)
 		 {
 			 if (myQuests.size() > i)
 			 font.drawString(g, myQuests.get(i).getDescription(), SystemFont.LEFT, INDENT, +65+25*i, 70);
 		 }
 	}
 	
     private void drawQuestSlots (Graphics2D g) 
     {	
 
         g.setColor(journalColor);
         
         for (int i = 0; i < 10; i++)
         {
             g.drawRect(5, 40+25*i, 70, 20); 
             g.fillRect(5, 40+25*i, 70, 20);
         }
         
     }
     
     public void toggleShow()
     {
     	if (showJournal == true)
     		showJournal = false;
     	else
     		showJournal = true;
     }
 
 }
