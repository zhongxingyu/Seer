 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 import gsfc.nssdc.cdf.Variable;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Vector;
 import java.util.Arrays;
 /*
 LevelOne.java v12.11.28
 
 Description:
    Creates level one CDF files
 
 v13.01.18
    -Updated the filename format
    
 v12.11.28
    -Modified terminal output to indicate payload and date
 
 v12.11.26
    -Removed epoch calculations
    -Does not add payload onto directory path 
 
 v12.11.20
    -Changed references to Level_Generator to CDF_Gen
    -Moved the declaration of frameGrp and mod variables outside the main loop. 
    -Moved time calculation code to top of loop
    -Changed epoch to being only stored in DataHolder instead of also being a 
       local variable
    -Removed ms_of_week from all files except GPS
    -Uses DataHolder.rc_label to write rc CDF variables instead of the switch 
       statment
    -Wraps primitive variable types stored in DataHolded in proper objects before
       writing to CDF
    
 v12.11.05
    -Saves ints (or longs) to cur_cdf.files. CDF's are now full of completely raw
       variables (except EPOCH and ms_of_week)
    -Changed "Time" CDF variable to "ms_of_week" to avoid TDAS namespace
       collision
    
 v12.10.11
    -Changed version numbers to a date format
    -No longer has any public members or methods other than the constructor.
    -Constructor calls functions to process all data held in DataHolder object 
       and produce CDF files.
    -Removed HexToBit function
    -Moved functions "copyFile()", "putData()" and "openCDF()" to 
       Level_Generator.java
    -Now gets output path from Level_Generator.L1_Dir
    -Types of CDF files are now pulled from a public member of 
       Level_Generator.java
 
 v0.3
    -Epoch is now calculated from "weeks" and "time" variables rather than 
       filename
    -Improved the way Epoch offsets are added to variables that come faster than 
       1Hz
    
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
    CDF mag_cdf, rcnt_cdf, fspc_cdf, 
       mspc_cdf, sspc_cdf, hkpg_cdf, pps_cdf;
    
    String outputPath;
    int lastFrame = -1;
    long ms_of_week = 0;
    int weeks = 0;
    String
       date = "000000",
       id = "00",
       flt = "00",
       stn = "0",
       revNum = "00";
    Calendar dateObj = Calendar.getInstance();
    
    short INCOMPLETE_GROUP = 8196;
    
    private DataHolder data;
    
    public LevelOne(
       final String d, final String p, final String f, final String s
    ) throws IOException
    {
       //get file revision number
       if(CDF_Gen.getSetting("rev") != null){
          revNum = CDF_Gen.getSetting("rev");
       }
       
       //save input arguments
       id = p;
       flt = f;
       stn = s;
       date = d;
       
       //get the data storage object
       data = CDF_Gen.getDataSet();
       
       //set output path
       outputPath = CDF_Gen.L1_Dir;
       File outDir = new File(outputPath);
       if(!outDir.exists()){outDir.mkdirs();}
       
       //copy the CDF skeletons to the new files 
       for(int type_i = 0; type_i < CDF_Gen.fileTypes.length; type_i++){
          String srcName = 
             "cdf_skels/l1/" + "barCLL_PP_S_l1_" + 
             CDF_Gen.fileTypes[type_i] + "_YYYYMMDD_v++.cdf";
          String destName = 
             outputPath + "bar1" + flt + "_" + id + "_" + stn + "_l1_" +
             CDF_Gen.fileTypes[type_i] + "_20" + date +  "_v" + revNum +
             ".cdf";
          CDF_Gen.copyFile(new File(srcName), new File(destName));
       }
       
       //get data from DataHolder and save them to CDF files
       try{
          writeData();
       }catch(CDFException ex){
          System.out.println(ex.getMessage());
       }
    }
    
    //Pull each value out of the frame and store it in the appropriate CDF.
    private void writeData() throws CDFException{
       //create a holder for the current CDF and Variable
       CDF cur_cdf;
       Variable cur_var;
 
       System.out.println(
          "Creating Level One... (" + data.getSize("1Hz") + " frames)"
       );
       
       //GPS//
          System.out.println("\nSaving GPS CDF...");
          //open GPS CDF and save the reference in the cur_cdf variable
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_gps-_20" + date +  "_v" + revNum + ".cdf"
          );
         
         

 
          //put an entire day's worth of data at once for each CDF variable
          cur_var = cur_cdf.getVariable("GPS_Alt");
          System.out.println("GPS_Alt...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1,
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.gps_raw[0]
          );
 
          cur_var = cur_cdf.getVariable("ms_of_week");
          System.out.println("ms_of_week...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.gps_raw[1]
          );
 
          cur_var = cur_cdf.getVariable("GPS_Lat");
          System.out.println("GPS_Lat...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.gps_raw[2]
          );
 
          cur_var = cur_cdf.getVariable("GPS_Lon");
          System.out.println("GPS_Lon...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.gps_raw[3]
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_mod4
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_mod4
          );
 System.out.println(data.gps_q.length + data.gps_q[data.gps_q.length - 1]);
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.gps_q
          );
 System.out.println("Done with GPS!");
          //close current cdf
          cur_cdf.close();
 
       //PPS//
          System.out.println("\nSaving PPS CDF...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_pps-_20" + date +  "_v" + revNum + ".cdf"
          );
          
          cur_var = cur_cdf.getVariable("GPS_PPS");
          System.out.println("GPS_PPS...");
          cur_var.putHyperData(
             0, data.getSize("1Hz"), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.pps
          );
 
          cur_var = cur_cdf.getVariable("Version");
          System.out.println("Version...");
          cur_var.putHyperData(
             0, data.getSize("1Hz"), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.ver
          );
 
          cur_var = cur_cdf.getVariable("Payload_ID");
          System.out.println("Payload_ID...");
          cur_var.putHyperData(
             0, data.getSize("1Hz"), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.payID
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, data.getSize("1Hz"), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_1Hz
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, data.getSize("1Hz"), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_1Hz
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, data.getSize("1Hz"), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.pps_q
          );
 
          cur_cdf.close();
          
       //B//
          System.out.println("\nSaving Magnetometer CDF...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_magn_20" + date +  "_v" + revNum + ".cdf"
          );
          
          cur_var = cur_cdf.getVariable("MAG_X");
          System.out.println("MAG_X... ");
          cur_var.putHyperData(
             0, (data.getSize("4Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.magx_raw
          );
 
          cur_var = cur_cdf.getVariable("MAG_Y");
          System.out.println("MAG_Y...");
          cur_var.putHyperData(
             0, (data.getSize("4Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.magy_raw
          );
 
          cur_var = cur_cdf.getVariable("MAG_Z");
          System.out.println("MAG_Z...");
          cur_var.putHyperData(
             0, (data.getSize("4Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.magz_raw
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("4Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_4Hz
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("4Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_4Hz
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("4Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.magn_q
          );
 
          cur_cdf.close();
          
       //HKPG
          System.out.println("\nSaving HKPG...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_hkpg_20" + date +  "_v" + revNum + ".cdf"
          );
             
          for(int var_i = 0; var_i < 36; var_i++){
             cur_var = cur_cdf.getVariable(data.hkpg_label[var_i]);
             System.out.println(data.hkpg_label[var_i] + "...");
             cur_var.putHyperData(
                0, (data.getSize("mod40")), 1, 
                new long[] {0}, 
                new long[] {1}, 
                new long[] {1}, 
                data.hkpg_raw[var_i]
             );
          }
 
          cur_var = cur_cdf.getVariable("numOfSats");
          System.out.println("numOfSats...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.sats
          );
 
          cur_var = cur_cdf.getVariable("timeOffset");
          System.out.println("timeOffset...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.offset
          );
          
          cur_var = cur_cdf.getVariable("termStatus");
          System.out.println("termStatus...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.termStat
          );
 
          cur_var = cur_cdf.getVariable("cmdCounter");
          System.out.println("cmdCounter...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.cmdCnt
          );
 
          cur_var = cur_cdf.getVariable("modemCounter");
          System.out.println("modemCounter...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.modemCnt
          );
 
          cur_var = cur_cdf.getVariable("dcdCounter");
          System.out.println("dcdCounter...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.dcdCnt
          );
 
          cur_var = cur_cdf.getVariable("weeks");
          System.out.println("weeks...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.weeks
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_mod40
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_mod40
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("mod40")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.hkpg_q
          );
 
          cur_cdf.close();
          
       //FSPC//
          System.out.println("\nSaving FSPC...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_fspc_20" + date +  "_v" + revNum + ".cdf"
          );
          
          cur_var = cur_cdf.getVariable("LC1");
          System.out.println("LC1...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.lc1_raw
          );
 
          cur_var = cur_cdf.getVariable("LC2");
          System.out.println("LC2...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.lc2_raw
          );
 
          cur_var = cur_cdf.getVariable("LC3");
          System.out.println("LC3...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.lc3_raw
          );
 
          cur_var = cur_cdf.getVariable("LC4");
          System.out.println("LC4...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.lc4_raw
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_20Hz
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_20Hz
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("20Hz")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.fspc_q
          );
 
          cur_cdf.close();
          
       //MSPC//
          System.out.println("\nSaving MSPC...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_mspc_20" + date +  "_v" + revNum + ".cdf"
          );
 
          cur_var = cur_cdf.getVariable("MSPC");
          System.out.println("Spectrum Arrays...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0, 0}, 
             new long[] {48, 1}, 
             new long[] {1, 1}, 
             data.mspc_raw
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_mod4
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_mod4
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.mspc_q
          );
 
          cur_cdf.close();
          
       //SSPC//
          System.out.println("\nSaving SSPC...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_sspc_20" + date +  "_v" + revNum + ".cdf"
          );
 
          cur_var = cur_cdf.getVariable("SSPC");
          System.out.println("Spectrum Arrays...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
            new long[] {1}, 
             new long[] {1}, 
             data.sspc_raw
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_mod32
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.epoch_mod32
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.sspc_q
          );
 
          cur_cdf.close();
          
       //RCNT
          System.out.println("\nSaving RCNT...");
          cur_cdf = CDF_Gen.openCDF( 
             outputPath + "bar1" + flt + "_" + id + "_" + stn +
             "_l1_rcnt_20" + date +  "_v" + revNum + ".cdf"
          );
 
          cur_var = cur_cdf.getVariable("Interrupt");
          System.out.println("Interrupt...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.rcnt_raw[0]
          );
 
          cur_var = cur_cdf.getVariable("LowLevel");
          System.out.println("LowLevel...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.rcnt_raw[1]
          );
 
          cur_var = cur_cdf.getVariable("PeakDet");
          System.out.println("PeakDet...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.rcnt_raw[2]
          );
 
          cur_var = cur_cdf.getVariable("HighLevel");
          System.out.println("HighLevel...");
          cur_var.putHyperData(
             0, (data.getSize("mod4")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.rcnt_raw[3]
          );
 
          cur_var = cur_cdf.getVariable("FrameGroup");
          System.out.println("FrameGroup...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.frame_mod4
          );
 
          cur_var = cur_cdf.getVariable("Epoch");
          System.out.println("Epoch...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1},
             data.epoch_mod4
          );
 
          cur_var = cur_cdf.getVariable("Q");
          System.out.println("Q...");
          cur_var.putHyperData(
             0, (data.getSize("mod32")), 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             data.rcnt_q
          );
 
       cur_cdf.close();
       
       System.out.println("Created Level One.");
    }
  }
