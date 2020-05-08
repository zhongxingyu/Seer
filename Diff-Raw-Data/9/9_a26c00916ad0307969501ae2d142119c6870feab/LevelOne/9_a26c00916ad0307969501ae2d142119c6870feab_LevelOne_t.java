 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 
 /*
 LevelOne.java v12.11.20
 
 Description:
    Creates level one CDF files
 
 v12.11.20
    -Changed references to Level_Generator to CDF_Gen
    -Moved the declaration of frameGrp and mod variables outside the main loop.   
    -Moved time calculation code to top of loop
    -Changed epoch to being only stored in DataHolder instead of also being a local variable
    -Removed ms_of_week from all files except GPS
    -Uses DataHolder.rc_label to write rc CDF variables instead of the switch statment
    -Wraps primative variable types stored in DataHolded in proper objects before writing to CDF
    
 v12.11.05
    -Saves ints (or longs) to cdf files. CDF's are now full of completely raw variables (except EPOCH and ms_of_week)
    -Changed "Time" CDF variable to "ms_of_week" to avoid TDAS namespace collision
    
 v12.10.11
    -Changed version numbers to a date format
    -No longer has any public members or methods other than the constructor.
    -Constructor calls functions to process all data held in DataHolder object and produce CDF files.
    -Removed HexToBit function
    -Moved functions "copyFile()", "putData()" and "openCDF()" to Level_Generator.java
    -Now gets output path from Level_Generator.L1_Dir
    -Types of CDF files are now pulled from a public member of Level_Generator.java
 
 v0.3
    -Epoch is now calculated from "weeks" and "time" variables rather than filename
    -Improved the way Epoch offsets are added to variables that come faster than 1Hz
    
 v0.2
    -added CDF libraries
    -copies the skeleton CDF files to date stamped files in constructor 
    -extracts all data types and writes them to the correct CDF files
 
 v0.1
    -added empty function that will convert the hex data eventually
 
 v0.0
    -Does nothing, just an empty object for the main program to create
 
 Planned Changes: 
    -Clean up/ Documentation
    -Add logging
    -Change the way frames are extracted
 */
 
 public class LevelOne{
    File cdfFile;
    CDF mag_cdf, rcnt_cdf, gps_cdf, fspc_cdf, 
       mspc_cdf, sspc_cdf, hkpg_cdf, pps_cdf;
    long mag_rec = -1, rcnt_rec = -1, gps_rec = -1, fspc_rec = -1, 
       mspc_rec = -1, sspc_rec = -1, hkpg_rec = -1, pps_rec = -1;
    String outputPath;
    int lastFrame = -1;
    long ms_of_week = 0;
    int weeks = 0;
    String date;
    Calendar dateObj = Calendar.getInstance();
 
    private DataHolder data;
    
    public LevelOne(final String d, final String payload)
       throws IOException
    {
       date = d;
       
       //get the data storage object
       data = CDF_Gen.getDataSet();
       
       //set output path
       outputPath = CDF_Gen.L1_Dir + "/" + payload + "/";
       File outDir = new File(outputPath);
       if(!outDir.exists()){outDir.mkdirs();}
       
       //copy the CDF skeletons to the new files 
       for(int type_i = 0; type_i < CDF_Gen.fileTypes.length; type_i++){
          String srcName = 
             "cdf_skels/l1/" + "barCLL_PP_S_l1_" + 
             CDF_Gen.fileTypes[type_i] + "_YYYYMMDD_v++.cdf";
          String destName = 
             outputPath + "barCLL_" + payload + "_S_l1_" + 
             CDF_Gen.fileTypes[type_i] + "_" + date + "_v++.cdf";
          
          CDF_Gen.copyFile(new File(srcName), new File(destName));
       }
 
       //open each CDF file and save the id
       gps_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_gps-_" + date + "_v++.cdf" );
       mag_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_magn_" + date + "_v++.cdf" );
       pps_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_pps-_" + date + "_v++.cdf" );
       hkpg_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_hkpg_" + date + "_v++.cdf" ); 
       fspc_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_fspc_" + date + "_v++.cdf" );
       mspc_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_mspc_" + date + "_v++.cdf" );
       sspc_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_sspc_" + date + "_v++.cdf" );
       rcnt_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l1_rcnt_" + date + "_v++.cdf" );
    
       //get data from DataHolder and save them to CDF files
       try{
          saveFrames();
       }catch(CDFException ex){
          System.out.println(ex.getMessage());
       }
    }
    
    /*
     * Pull each value out of the frame and store it in the appropriate CDF.
     */
    private void saveFrames() throws CDFException{
       System.out.println(
          "Creating Level One... (" + data.getSize() + " frames)");
       
       long epoch = 0;
       int mod4, mod32, mod40, frameGrp4, frameGrp32, frameGrp40;
    
       
       for( int frm_i = 0; frm_i < data.getSize(); frm_i++ ){
          System.out.println(
             frm_i + " (" + (100 * frm_i) / data.getSize() + "%)");
          
          epoch = 0L;
          
          //get all the frame groups and mux index
          mod4 = data.frameNum[frm_i] % 4;
          mod32 = data.frameNum[frm_i] % 32;
          mod40 = data.frameNum[frm_i] % 40;
          frameGrp4 = data.frameNum[frm_i] - mod4;
          frameGrp32 = data.frameNum[frm_i] - mod32;
          frameGrp40 = data.frameNum[frm_i] - mod40;
       
          //Do time simple correction
          if(frm_i > 0 && mod4 != 1){
             //Increment time 
             ms_of_week += ((data.frameNum[frm_i] - data.frameNum[frm_i - 1]) * 1000);
 	     }else{
 	        ms_of_week = data.gps_raw[frm_i];
 		 }
          data.ms_of_week[frm_i] = ms_of_week;
          
          //set date and time
          if(
             weeks >= 0 && weeks <= 65535 && 
             ms_of_week >= 0 && ms_of_week < 4294967295L
          ){
             dateObj.setTimeInMillis(0L);
             dateObj.set(1980, 00, 06);
             dateObj.add(Calendar.WEEK_OF_YEAR, weeks);
             dateObj.add(
                Calendar.MILLISECOND, (int) ms_of_week
             );
             data.epoch[frm_i] = CDFTT2000.fromUTCparts(
                (double) dateObj.get(Calendar.YEAR), 
                (double) (dateObj.get(Calendar.MONTH) +1 ), 
                (double) dateObj.get(Calendar.DAY_OF_MONTH), 
                (double) dateObj.get(Calendar.HOUR), 
                (double) dateObj.get(Calendar.MINUTE),
                (double) dateObj.get(Calendar.SECOND),
                (double) dateObj.get(Calendar.MILLISECOND)
             );
          }
          
          //GPS
          switch(mod4){
             case 0:
                gps_rec++;
                CDF_Gen.putData(
                   gps_cdf, "FrameGroup", gps_rec, 
                   Integer.valueOf(data.frameNum[frm_i]), 0L
                );
                CDF_Gen.putData(
                   gps_cdf, "GPS_Alt", gps_rec, 
                   Long.valueOf(data.gps_raw[frm_i]), 0L
                );
                break;
             case 1:
                CDF_Gen.putData(
                   gps_cdf, "Epoch", gps_rec, 
                   Long.valueOf(data.epoch[frm_i]), 0L
                );
                CDF_Gen.putData(
                   gps_cdf, "ms_of_week", gps_rec, 
                   Long.valueOf(ms_of_week), 0L
                );
                break;
             case 2:
                CDF_Gen.putData(
                   gps_cdf, "GPS_Lat", gps_rec, 
                   Long.valueOf(data.gps_raw[frm_i]), 0L
                );
                break;
             default:  
                CDF_Gen.putData(
                   gps_cdf, "GPS_Lon", gps_rec, 
                   Long.valueOf(data.gps_raw[frm_i]), 0L
                );
                
                break;
          }
          
          //PPS
          pps_rec++;
          CDF_Gen.putData(
             pps_cdf, "Epoch", pps_rec, 
             Long.valueOf(data.epoch[frm_i]), 0L
          );
          CDF_Gen.putData(
             pps_cdf, "FrameGroup", pps_rec, 
             Integer.valueOf(data.frameNum[frm_i]), 0L
          );
          CDF_Gen.putData(
             pps_cdf, "GPS_PPS", pps_rec, 
             Integer.valueOf(data.pps[frm_i]), 0L
          );
          CDF_Gen.putData(
             pps_cdf, "Payload_ID", pps_rec, 
             Short.valueOf(data.payID[frm_i]), 0L
          );
          CDF_Gen.putData(
             pps_cdf, "Version", pps_rec, 
             Short.valueOf(data.ver[frm_i]), 0L
          );
 
          //B
          for(int set_i = 0; set_i < 4; set_i++){
             mag_rec++;
             
             //Add offset to time and epoch because this is 4Hz data 
             CDF_Gen.putData(
                mag_cdf, "Epoch", mag_rec, 
                Long.valueOf(data.epoch[frm_i] + (250000000 * set_i)), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "FrameGroup", mag_rec, 
                Integer.valueOf(data.frameNum[frm_i]), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "MAG_X", mag_rec, 
                Long.valueOf(data.magx_raw[frm_i][set_i]), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "MAG_Y", mag_rec, 
                Long.valueOf(data.magy_raw[frm_i][set_i]), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "MAG_Z", mag_rec, 
                Long.valueOf(data.magx_raw[frm_i][set_i]), 0L
             );
          }
          
          //HKPG
          if(mod40 == 0){ //start a new record for each new group of frames
             hkpg_rec++;
             CDF_Gen.putData(
                hkpg_cdf, "Epoch", hkpg_rec, 
                Long.valueOf(data.epoch[frm_i]), 0L
             );
             CDF_Gen.putData(
                hkpg_cdf, "FrameGroup", hkpg_rec, 
                Integer.valueOf(frameGrp40), 0L
             );
          }
          if(mod40 < 36){
             CDF_Gen.putData(
                hkpg_cdf, DataHolder.hkpg_label[mod40], hkpg_rec, 
                Long.valueOf(data.hkpg_raw[frm_i]), 0L
             );
          }
          else if(mod40 == 36){
             data.sats[frm_i] = (short)(data.hkpg_raw[frm_i] >> 8);
             data.offset[frm_i] = (short)(data.hkpg_raw[frm_i] & 255);
             
             CDF_Gen.putData(
                hkpg_cdf, "numOfSats", hkpg_rec, 
                Short.valueOf(data.sats[frm_i]), 0L
             );
             CDF_Gen.putData(
                hkpg_cdf, "timeOffset", hkpg_rec, 
                Short.valueOf(data.offset[frm_i]), 0L
             );
          }
          else if(mod40 == 37){
             weeks = (int) data.hkpg_raw[frm_i];
             data.weeks[frm_i] = weeks;
             
             CDF_Gen.putData(hkpg_cdf, "weeks", hkpg_rec, 
                Integer.valueOf(data.weeks[frm_i]), 0L);
          }
          else if(mod40 == 38){
             data.termStat[frm_i] = 
                   (short)(data.hkpg_raw[frm_i] >> 15);
             data.cmdCnt[frm_i] = 
                   (int)(data.hkpg_raw[frm_i] & 32768);
             
             CDF_Gen.putData(
                hkpg_cdf, "termStatus", hkpg_rec, 
                Short.valueOf(data.termStat[frm_i]), 0L
             );
             CDF_Gen.putData(
                hkpg_cdf, "cmdCounter", hkpg_rec, 
                Integer.valueOf(data.cmdCnt[frm_i]), 0L
             );
             
          }else if(mod40 == 39){
             data.dcdCnt[frm_i] = 
                (short)(data.hkpg_raw[frm_i] >> 8);
             data.modemCnt[frm_i] = 
                (short)(data.hkpg_raw[frm_i] & 255);
             
             CDF_Gen.putData(
                hkpg_cdf, "dcdCounter", hkpg_rec, 
                Short.valueOf(data.dcdCnt[frm_i]), 0L
             );
             CDF_Gen.putData(
                hkpg_cdf, "modemCounter", hkpg_rec, 
                Short.valueOf(data.modemCnt[frm_i]), 0L
             );
          }
          
          //FSPC
          for(int set_i = 0; set_i < 20; set_i++){
             fspc_rec++;
             CDF_Gen.putData(
                fspc_cdf, "FrameGroup", fspc_rec, 
                Integer.valueOf(data.frameNum[frm_i]), 0L
             );
             
             //Add epoch and time offsets because data comes at 20Hz
             CDF_Gen.putData(
                fspc_cdf, "Epoch", fspc_rec, 
                Long.valueOf(data.epoch[frm_i] + (50000000 * set_i)), 0L);
             CDF_Gen.putData(
                fspc_cdf, "LC1", fspc_rec, 
                data.lc1_raw[frm_i][set_i], 0L);
             CDF_Gen.putData(
                fspc_cdf, "LC2", fspc_rec, 
                data.lc2_raw[frm_i][set_i], 0L);
             CDF_Gen.putData(
                fspc_cdf, "LC3", fspc_rec, 
                data.lc3_raw[frm_i][set_i], 0L);
             CDF_Gen.putData(
                fspc_cdf, "LC4", fspc_rec, 
                data.lc4_raw[frm_i][set_i], 0L);
          }
          
          //MSPC
          if(mod4 == 0){
             mspc_rec++;
             CDF_Gen.putData(
                mspc_cdf, "Epoch", mspc_rec, 
                Long.valueOf(data.epoch[frm_i]), 0L
             );
             CDF_Gen.putData(
                mspc_cdf, "FrameGroup", mspc_rec, 
                Integer.valueOf(frameGrp4), 0L
             );
          }
          
          for(int chan_i = 0; chan_i < 12; chan_i ++){
             CDF_Gen.putData(mspc_cdf, "MSPC", mspc_rec, 
                data.mspc_raw[frm_i][chan_i], ((12 * mod4) + chan_i)
             );
          }
          
          //SSPC
          if(mod32 == 0){
             sspc_rec++;
             CDF_Gen.putData(
                sspc_cdf, "Epoch", sspc_rec, 
                Long.valueOf(data.epoch[frm_i]), 0L
             );
             CDF_Gen.putData(
                sspc_cdf, "FrameGroup", sspc_rec, 
                Integer.valueOf(frameGrp32), 0L
             );
          }
          
          for(int chan_i = 0; chan_i < 8; chan_i ++){
             CDF_Gen.putData(sspc_cdf, "SSPC", sspc_rec, 
                data.sspc_raw[frm_i][chan_i], ((8 * mod32) + chan_i)
             );
          }
 
          //RC
          if(mod4 == 0){
             rcnt_rec++;
             CDF_Gen.putData(
                rcnt_cdf, "Epoch", rcnt_rec, 
                Long.valueOf(data.epoch[frm_i]), 0L
             );
             CDF_Gen.putData(
                rcnt_cdf, "FrameGroup", rcnt_rec, 
                Integer.valueOf(frameGrp4), 0L
             );
          }
             CDF_Gen.putData(
                rcnt_cdf, DataHolder.rc_label[mod4], rcnt_rec, 
                Long.valueOf(data.rc_raw[frm_i]), 0L
             );
           
 
          lastFrame = data.frameNum[frm_i];
          
       }
       
       gps_cdf.close();
       mag_cdf.close();
       rcnt_cdf.close();
       fspc_cdf.close();
       mspc_cdf.close();
       sspc_cdf.close();
       hkpg_cdf.close();
       pps_cdf.close();
       
       System.out.println("Created Level One.");
    }
  }
