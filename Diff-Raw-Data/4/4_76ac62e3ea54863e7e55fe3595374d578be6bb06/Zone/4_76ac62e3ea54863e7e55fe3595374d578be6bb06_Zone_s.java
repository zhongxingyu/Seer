 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package InsightLT;
 
 import com.sun.squawk.util.Arrays;
 import java.util.Vector;
 /**
  *
  * @author Wave
  */
 public class Zone {
     Zone(int line, int position, int length, int numOfLines, int scrollTime)
     {
         m_scrollTimer = System.currentTimeMillis();
         m_displayTime = scrollTime;
         m_zoneLine = line;
         m_zonePosition = position;
         m_zoneLength = length;
         m_twoLines = false;
         m_infoItems = new Vector();
         if(numOfLines > 1)
         {
             m_twoLines = true;
         }
     }
     
     void setTime(int time)
     {
         m_displayTime = time;
     }
     
     void registerData(DisplayData data)
     {
         m_infoItems.addElement(data);
     }
     
     void advanceZoneUp()
     {
         m_scrollPosition++;
 	if(m_scrollPosition == (int)m_infoItems.size())
 	{
 		m_scrollPosition = 0;
 	}
     }
     
     void advanceZoneDown()
     {
         if(m_scrollPosition == 0)
 	{
             m_scrollPosition = m_infoItems.size() - 1;
 	}
 	else
 	{
             m_scrollPosition--;
 	}
     }
     
     int getLine()
     {
         return m_zoneLine;
     }
     
     int getPosition()
     {
         return m_zonePosition;
     }
     
     String getLineOne()
     {
         byte[] tmp = new byte[m_zoneLength];
         Arrays.fill(tmp, (byte)'-');
 	String tmpString = new String(tmp);
 	if(!m_infoItems.isEmpty())
 	{
 		if(m_scrollPosition >= (int)m_infoItems.size())
 		{
 			m_scrollPosition = 0;
 		}
 		tmpString = ((DisplayData)m_infoItems.elementAt(m_scrollPosition)).getFormattedString(m_zoneLength);
 	}
 	return tmpString;
     }
     
     String getLineTwo()
     {
         byte[] tmp = new byte[m_zoneLength];
         Arrays.fill(tmp, (byte)'-');        
         String tmpString = new String(tmp);
 		
 	if(m_infoItems.size() > 1)
 	{
 		if(m_scrollPosition + 1 >= (int)m_infoItems.size())
 		{
 			tmpString = ((DisplayData)m_infoItems.elementAt(0)).getFormattedString(m_zoneLength);
 		}
 		else
 		{
 			tmpString = ((DisplayData)m_infoItems.elementAt(m_scrollPosition + 1)).getFormattedString(m_zoneLength);
 		}
 	}
 	return tmpString;
     }
     
     boolean isTwoLines()
     {
         return m_twoLines;
     }
     
     void update()
     {
         if(System.currentTimeMillis() > m_scrollTimer)
         {
             m_scrollTimer += m_displayTime;
             advanceZoneDown();
         }
     }
 		
 	
     private int m_zoneLine;
     private int m_zonePosition;
     private int m_zoneLength;
     private boolean m_twoLines;
     private boolean m_rotate;
     private int m_scrollPosition;
     private long m_scrollTimer;
     // lenght of time each information item is displayed
     private int m_displayTime;
     // container for information items
     private Vector m_infoItems;
 }
