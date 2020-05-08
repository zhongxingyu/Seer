 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package autonomous;
 
 import utilities.JoyEmulator;
 import utilities.FileReader;
 import utilities.Robot;
 import utilities.Vars;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  *
  * @author fauzi
  */
 public class Replayer {
     
     //Constants
     private final double m_dMaxReplay = 14.75;
     
     private int m_iMax = 0;
     private int m_iCounter = 0;
     private boolean m_bRepStarted = false;
     private boolean m_bDoneReplay = false;
     private Timer m_tmReplay = new Timer();
     private JoyEmulator m_joyAuto = new JoyEmulator();
     private String m_sReplayStatus = null;
     private FileReader m_fileReader;
     private Robot m_bot;
     
     public Replayer(Robot robot)
     {
         m_bot = robot;
     }
     
     public String replay(String sFileName)
     {
         if(!m_bRepStarted)
         {
             Vars.disableDrive();
             m_fileReader = new FileReader(sFileName);
             m_iMax = m_fileReader.readInt();
             m_tmReplay.start();
             m_bRepStarted = true;
         }
 
         if(!m_bDoneReplay)
         {        
             if(getNewData())
            {    
                 m_joyAuto.setValues(m_fileReader.readAll());
                m_iCounter++;
            }
             
             m_sReplayStatus = "Replaying: " + Vars.setPrecision(m_tmReplay.get());
             m_bot.setSpeed(m_joyAuto.getMtLeft(), m_joyAuto.getMtRight());
             m_bot.setRetrieve(m_joyAuto.getRetrieve());
             
             if(overTimeLimit(sFileName) || EndOfFile())
                 m_bDoneReplay = true;
         }
 
         else
         {
             m_sReplayStatus = "Done Replaying";
             m_bot.setSpeed(0, 0);
             m_tmReplay.stop();
         }
         
         return m_sReplayStatus;
     }
     
     public void reset()
     {
         if(m_bRepStarted)
         {
             Vars.enableDrive();
             m_fileReader.close();
             m_tmReplay.stop();
             m_tmReplay.reset();
             m_iCounter = 0;
             m_iMax = 0;
             m_bDoneReplay = false;
             m_bRepStarted = false;
         }
     }
     
     private boolean getNewData()
     {
         if(m_joyAuto.getTime() >= m_tmReplay.get())
             return true;
         
         else 
             return false;
     }
     
     private boolean EndOfFile()
     {
         if(m_iCounter >= 0 && m_iCounter < m_iMax)
             return false;
         
         else 
             return true;
     }
     
     private boolean overTimeLimit(String sFileName)
     {
         if(m_tmReplay.get() >= m_dMaxReplay && !sFileName.equalsIgnoreCase(Vars.sRegOutput))
             return true;
         
         else
             return false;
     }
 }
