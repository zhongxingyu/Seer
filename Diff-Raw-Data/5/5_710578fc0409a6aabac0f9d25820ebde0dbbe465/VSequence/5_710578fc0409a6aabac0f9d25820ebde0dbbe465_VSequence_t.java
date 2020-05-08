 package code.model.recorder;
 
 import code.controllers.VInstrumentPanelController;
 import code.gui.VMainWindow;
 import code.model.datastructures.VQueue;
 import code.model.instruments.VInstrument;
 import code.model.instruments.VKey;
 import java.io.Serializable;
 
 /**
  *
  * @author Jose Carlos
  */
 public class VSequence implements Serializable, Runnable
 {
     private VInstrument instrument;
     private VQueue<VRecord> records;
     private String name;
     
     private long lastPlayedTime;
     private long newPlayedTime;
     
     public VSequence(VInstrument instrument, long startTime)
     {
         this.instrument = instrument;
         this.records = new VQueue<VRecord>();
         this.lastPlayedTime = startTime;
     }
 
     public VInstrument getInstrument() 
     {
         return instrument;
     }
 
     public VQueue<VRecord> getRecords() 
     {
         return records;
     }
 
     public String getName() 
     {
         return name;
     }
 
     public void setName(String name) 
     {
         this.name = name;
     }
     
     public void record(VKey key)
     {
         this.newPlayedTime = System.currentTimeMillis();
         long startTime = this.newPlayedTime - this.lastPlayedTime;
         this.lastPlayedTime = this.newPlayedTime;
         VRecord newRecord = new VRecord(key, startTime);
         this.records.add(newRecord);
     }
 
     @Override
     public void run() 
     {
         VInstrumentPanelController player = VMainWindow.window.getRailBoard().getInstrumentPanel().getController();
        VQueue<VRecord> recovery = new VQueue<VRecord>();
         while (!this.records.isEmpty())
         {
             VRecord record = records.remove();
             try
             {
                 Thread.sleep(record.getStartTime());
             }
             catch(Exception e){}
             player.playInstrument(record.getKey());
            recovery.add(record);
         }
        this.records = recovery;
     }
 }
