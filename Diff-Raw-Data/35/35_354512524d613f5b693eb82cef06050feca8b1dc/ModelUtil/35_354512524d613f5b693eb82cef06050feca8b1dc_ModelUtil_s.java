 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.model;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import littlesmarttool2.comm.AutoServoPuller;
 import littlesmarttool2.comm.SerialController;
 import static littlesmarttool2.model.ConnectionType.*;
 import littlesmarttool2.util.JSON;
 
 /**
  *
  * @author marcher89
  */
 public class ModelUtil {
     
     static CameraBrand[] cameraBrands;
     static IRCommand[] irCommands;
     static WireCommand[] wireCommands;
     static LANCCommand[] lancCommands;
     
     public static void LoadData() {
         cameraBrands = JSON.readObjectFromFile("cameraList.json", CameraBrand[].class);
         
         HashMap<String, CameraModel> map = new HashMap<>();
         
         //Mapping identifiers to camera models
         for (CameraBrand cameraBrand : cameraBrands) {
             for (CameraModel cameraModel : cameraBrand.getModels()) {
                 map.put(cameraModel.getIdentifier(), cameraModel);
             }
         }
         
         CameraModel[] models;
         int i;
         
         irCommands = JSON.readObjectFromFile("IRCommandList.json", IRCommand[].class);
         wireCommands = JSON.readObjectFromFile("WireCommandList.json", WireCommand[].class);
         lancCommands = JSON.readObjectFromFile("LANCCommandList.json", LANCCommand[].class);
         
         for (IRCommand command : irCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addIRCommand(command);
             }
             command.setCameraModels(models);
         }
         
         for (WireCommand command : wireCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addWireCommand(command);
             }
             command.setCameraModels(models);
         }
         
         for (LANCCommand command : lancCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addLANCCommand(command);
             }
             command.setCameraModels(models);
         }
     }
     
     public static void SendConfigurationToSnapper(Configuration conf, SerialController comm) throws IOException {
         try{
             AutoServoPuller.Stop(comm);
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {/*Cough*/}
             comm.setSyncTimeout(1000);
             String response = comm.sendSync('F', new String[]{"1"});
             if(!response.equals("F;1"))//--------------------------------"F" Clear EEPROM
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to clear the EEPROM. Response: "+response);
             switch(conf.getOutputType()){
                 case IR:
                     if(!comm.sendSync('O', new String[]{"1"}).equals("O;1"))//------------------------"O" Output mode: IR
                         throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the output mode to IR.");
                     break;
                 case Wire:
                     if(!comm.sendSync('O', new String[]{"2"}).equals("O;1"))//------------------------"O" Output mode: Wire
                         throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the output mode to wire.");
                     break;
                 case LANC:
                     if(!comm.sendSync('O', new String[]{"3"}).equals("O;1"))//------------------------"O" Output mode: LANC
                         throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the output mode to LANC.");
                     break;
             }
             for (Channel channel : conf.getChannels()) {
                 int servo = channel.getId();
                 Setting setting = channel.getSetting();
 
                 //Thresholds / Triggers
                 int cmdId = 1, number = 1;
                 for (Threshold threshold : setting.getThresholds()) {
                     if(threshold.getUpCommand() != Command.getNothingCommand()) {
                        sendCommandToSnapper(comm, threshold.getUpCommand(), cmdId);
                         response = comm.sendSync('T', new String[]{//------------------------------"T" Trigger point
                             number+"",//number
                             servo+"",//servo
                             channel.convertPromilleToValue(threshold.getValuePromille())+"",//trig point
                             "1",//going high
                             "0",//going low
                             "10",//hysteresis
                             cmdId+""//command
                         });
                         if(!response.equals("T;1"))
                             throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to send a trigger point with an up command. Response: "+response);
                         cmdId++;number++;
                     }
                     if(threshold.getDownCommand() == Command.getNothingCommand()) 
                         continue;
                    sendCommandToSnapper(comm, threshold.getDownCommand(), cmdId);
                     response = comm.sendSync('T', new String[]{//------------------------------"T" Trigger point
                         number+"",//number
                         servo+"",//servo
                         channel.convertPromilleToValue(threshold.getValuePromille())+"",//trig point
                         "0",//going high
                         "1",//going low
                         "50",//hysteresis
                         cmdId+""//command
                     });
                     if(!response.equals("T;1"))
                         throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to a trigger point with a down command. Response: "+response);
                     cmdId++;number++;
                 }
 
                 //Blocks / Ranges
                 for (Block block : setting.getBlocks()) {
                     if(block.getCommand() == Command.getNothingCommand())
                         continue;
                     int minPromille = block.getLowerThreshold() != null ? block.getLowerThreshold().getValuePromille() : 0;
                     //Max is exclusive, except for the highest block
                     int maxPromille = block.getUpperThreshold() != null ? block.getUpperThreshold().getValuePromille()-1 : 1000;
 
                    sendCommandToSnapper(comm, block.getCommand(), cmdId);
                     response = comm.sendSync('R', new String[]{//------------------------------"R" Range trigger
                         number+"",//number
                         servo+"",//servo
                         channel.convertPromilleToValue(maxPromille)+"",//max point
                         channel.convertPromilleToValue(minPromille)+"",//min point
                         block.getInterval()+"",//timing range high
                         block.getInterval()+"",//timing range low
                         "1",//expo TODO: What's this?
                         cmdId+""//command
                     });
                     if(!response.equals("R;1"))
                         throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to a range trigger. Response: "+response);
                     cmdId++;number++;
                 }
             }
         }
         finally{
             AutoServoPuller.Start(comm);
         }
     }
     
     private static void sendCommandToSnapper(SerialController comm, Command command, int commandId) throws IOException{
         if(command.getClass() == IRCommand.class){
             IRCommand ir = (IRCommand) command;
             int[] pulsdata = ir.getPulsedata();
             for (int puls = 1; puls <= (pulsdata.length/2); puls++) {
                 String[] cmds = new String[]{//----------------------------------------------------------------------"I" IR puls
                     commandId+"",//command
                     puls+"",//puls number
                     pulsdata[puls*2-2]+"",//timing high
                     pulsdata[puls*2-1]+""//timing low
                 };
                 String response = comm.sendSync('I', cmds);
                 if(!response.equals("I;1")) //TODO: what about repeats??
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set an IR pulse.\n"
                             + "Sent: "+Arrays.deepToString(cmds)+"\n"
                 + "Response: "+response);
             }
             if(!comm.sendSync("K;"+ir.getFrequency()).equals("K;1")){//----------------------------------------------"K" IR Frequency
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the IR frequency.");
             }else return;
         }
        if(command.getClass() == WireCommand.class){
            if(!comm.sendSync(commandId+";").equals("???")){ //TODO: What to do?-------------------------------------"??" Wire Command
                throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to send a wire command.");
            }else return;
        }
         if(command.getClass() == LANCCommand.class){
             LANCCommand lanc = (LANCCommand)command;
             String response = comm.sendSync('L', new String[]{//------------------------------------------------------------"L" LANC Command
                 commandId+"",//command
                 lanc.getCommandByte0()+"",//byte 0
                 lanc.getCommandByte1()+"" //byte 1
             });
             if(!response.equals("L;1")){ 
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to send a LANC command. Response: "+response);
             } else return;
         }
     }
 }
