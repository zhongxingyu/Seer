 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFConstants;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.Variable;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 /*
 LevelTwo.java v12.11.20
 
 Description:
    Creates Level Two CDF files
 
 v12.11.20
    -Changed references to Level_Generator to CDF_Gen
    -Moved file reading process for calibration data 
       out of the main loop so it only happens once 
    -Moved frameGrp, mod and mag_tot variables out of main loop
    -Matched new variable names and types in DataHolder
    -Rebinned Spectra is no longer saved to DataHolder
    -Bug Fix: Spectra is now rebinned as a whole, rather than by frame
    
 v12.11.05
    -Fills level 2 skeleton files instead of copying completed level 1 files
    -Added ability to scale housekeeping data
    -Extracts real GPS, RC, and MAG numbers
    -Uses DataHolder object to get raw values rather than level 1 files
 
 v12.10.16
    -opens all CDF Lvl 2 files at the same time
    -scales mag data according to calibration file and temp (uses dummy id for now)
 
 v12.10.11
    -Changed version numbers to a date format
    -Creates L2 CDF files with no corrections
    -Added file reading function to get calibration data
    -Creates variables for corrected mag data, but does not fill them
 
 v0.0
    -Does nothing, just an empty object for the main program to create
 
 Planned Changes:
    -Add Exact timing
    -Add detector gain corrections
    -Add mag temp corrections
    -Fix mag variable meta data
 */
 
 public class LevelTwo implements CDFConstants{
    File cdfFile;
    CDF mag_cdf, rcnt_cdf, gps_cdf, fspc_cdf, 
       mspc_cdf, sspc_cdf, hkpg_cdf, pps_cdf;
    long mag_rec = -1L, rcnt_rec = -1L, gps_rec = -1L, fspc_rec = -1L, 
       mspc_rec = -1L, sspc_rec = -1L, hkpg_rec = -1L, pps_rec = -1L;
    String outputPath = "",
       date = "",
       payload = "";
    Double[] mag_slope;
    DataHolder data;
    Double[] magCal = getCalDat("magGain.cal", 4, "0219", 0);
    
    //Housekeeping scaling info
    Double[] hkpg_scale = {
          0.0003052, 0.05086, 0.0003052, 0.06104, 0.0006104, 0.06104, 
          0.0001526, 0.01017, 0.0001526, 0.001017, 0.0003052, 0.05086, 
          -0.0001526, -0.0001261, -0.0001526, -0.001017, 
          0.007629, 0.007629, 0.007629, 0.007629, 0.007629, 0.007629,
          0.007629, 0.007629, 0.007629, 0.007629, 0.007629, 0.0003052,
          0.007629, 0.0003052, 0.007629, 0.0001526, 0.0001526, 0.0006104, 
          0.0006104, 0.0006104
    };
    Double[] hkpg_offset = {
       0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
       0.0, 0.0, -273.15, -273.15, -273.15, -273.15, -273.15, -273.15, 
       -273.15, -273.15, -273.15, -273.15, -273.15, 0.0, -273.15, 0.0, -273.15, 
       0.0, 0.0, 0.0, 0.0, 0.0
    };
    
    public LevelTwo(final String d, final String p)
          throws IOException
    {  
       date = d;
       payload = p;
       
       //get the data storage object
       data = CDF_Gen.getDataSet();
       
       System.out.println(
             "Creating Level Two CDF... (" + data.getSize() + " frames)");
       
       //set file output path
       outputPath = CDF_Gen.L2_Dir + "/" + payload + "/";
       File outDir = new File(outputPath);
       if(!outDir.exists()){outDir.mkdirs();}
       
       //copy the CDF skeletons to the new files 
       for(int type_i = 0; type_i < CDF_Gen.fileTypes.length; type_i++){
          String srcName =
             "cdf_skels/l2/" + "barCLL_PP_S_l2_" + 
                CDF_Gen.fileTypes[type_i] + "_YYYYMMDD_v++.cdf";
          String destName = 
             outputPath + "barCLL_" + payload + "_S_l2_" + 
             CDF_Gen.fileTypes[type_i] + "_" + date + "_v++.cdf";
          
          CDF_Gen.copyFile(new File(srcName), new File(destName));
       }
       
       //Make whatever corrections to the data that are needed and save them to the L2 CDFs
       try{
          processData();
       }catch(CDFException ex){
          System.out.println(ex.getMessage());
       }
       
       
    }
    private void processData() throws CDFException{
       
       Variable tempVar;
       String tempID = "";
       long numRecs;
       long lastRec = 0;
       double[][] 
          stdSpectraEdges = SpectrumExtract.getBinEdges(2.44),
          realSpectraEdges;
       double magTemp = -1;
       int mod4, mod32, mod40, frameGrp4, frameGrp32, frameGrp40;
       Double mag_tot = null;
       Integer[] 
          fspc_raw = new Integer[4],
          mspc_raw = new Integer[48],
          sspc_raw = new Integer[256];
       Double[] 
          fspc_rebin = new Double[4], 
          mspc_rebin = new Double[48], 
          sspc_rebin = new Double[256], 
          fspc_error = new Double[4],
          mspc_error = new Double[48],
          sspc_error = new Double[256];
       int
          mspc_first_frm = 0,
          sspc_first_frm = 0;
                   
       //get first mag temp reading
       for(int t_i = 0; t_i < data.getSize(); t_i++){
          //search for next good mag temp
          if( t_i % 40 == DataHolder.T1 && data.hkpg_raw[t_i] != 0){
             //extract the correct housekeeping value
             magTemp = 
                (data.hkpg_raw[t_i] * hkpg_scale[DataHolder.T1]) + 
                hkpg_offset[DataHolder.T1];
             break;
          }
       }
       
       //Open all cdf files
       gps_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_gps-_" + date + "_v++.cdf"
       );
       pps_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_pps-_" + date + "_v++.cdf"
       );
       hkpg_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_hkpg_" + date + "_v++.cdf"
       ); 
       mag_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_magn_" + date + "_v++.cdf" 
       );
       fspc_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_fspc_" + date + "_v++.cdf" 
       );
       mspc_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_mspc_" + date + "_v++.cdf" 
       );
       sspc_cdf = CDF_Gen.openCDF( 
          outputPath + "barCLL_" + payload + "_S_l2_sspc_" + date + "_v++.cdf" 
       );
 
       for(int frm_i = 0; frm_i < data.getSize(); frm_i++){
          System.out.println(
             frm_i + " (" + (100 * frm_i) / data.getSize() + "%)");
          
          mod4 = data.frameNum[frm_i] % 4;
          mod32 = data.frameNum[frm_i] % 32;
          mod40 = data.frameNum[frm_i] % 40;
          frameGrp4 = data.frameNum[frm_i] - mod4;
          frameGrp32 = data.frameNum[frm_i] - mod32;
          frameGrp40 = data.frameNum[frm_i] - mod40;
          
          //Extract GPS
          switch(mod4){
             case 0:
                gps_rec++;
                
                CDF_Gen.putData(
                   gps_cdf, "FrameGroup", gps_rec, 
                   Integer.valueOf(data.frameNum[frm_i]), 0L
                );
                data.gps[frm_i] = (double) data.gps_raw[frm_i];
                CDF_Gen.putData(
                   gps_cdf, "GPS_Alt", gps_rec, 
                   Double.valueOf(data.gps[frm_i]), 0L
                );
                break;
                
             case 1:
                data.gps[frm_i] = (double) data.gps_raw[frm_i];
                CDF_Gen.putData(
                   gps_cdf, "Epoch", gps_rec, 
                   Long.valueOf(data.epoch[frm_i]), 0L
                );
                CDF_Gen.putData(
                   gps_cdf, "ms_of_week", gps_rec, 
                   Long.valueOf(data.ms_of_week[frm_i]), 0L
                );
                break;
                
             case 2:
                if(data.gps_raw[frm_i] > 2147483648L){
                   data.gps[frm_i] = (double)(data.gps_raw[frm_i] - 4294967296L);
                }else{data.gps[frm_i] = (double) data.gps_raw[frm_i];}
                
                data.gps[frm_i] *= 8.38190317154 * Math.pow(10,-8);
                CDF_Gen.putData(
                   gps_cdf, "GPS_Lat", gps_rec, 
                   Double.valueOf(data.gps[frm_i]), 0L
                );
                break;
             
             case 3:
                if(data.gps_raw[frm_i] > 2147483648L){
                   data.gps[frm_i] = (double)(data.gps_raw[frm_i] - 4294967296L);
                }else{data.gps[frm_i] = (double) data.gps_raw[frm_i];}
                
                data.gps[frm_i] *= 8.38190317154 * Math.pow(10,-8);
                CDF_Gen.putData(
                   gps_cdf, "GPS_Lon", gps_rec, 
                   Double.valueOf(data.gps[frm_i]), 0L
                );
                break;
             
             default:
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
          
          
          //Extract Housekeeping
          hkpg_rec++;
          
          switch(mod40){
             case 0:
                data.hkpg[frm_i] = 
                   (data.hkpg_raw[frm_i] * hkpg_scale[mod40]) + 
                   hkpg_offset[mod40];
                
                CDF_Gen.putData(
                   hkpg_cdf, "Epoch", hkpg_rec, 
                   Long.valueOf(data.epoch[frm_i]), 0L
                );
                CDF_Gen.putData(
                   hkpg_cdf, "FrameGroup", hkpg_rec, 
                   Integer.valueOf(data.frameNum[frm_i]), 0L
                );
                CDF_Gen.putData(
                   hkpg_cdf, DataHolder.hkpg_label[mod40], hkpg_rec, 
                   Double.valueOf(data.hkpg[frm_i]), 0L
                );
             break;
             
             case DataHolder.T1:
                //update mag temperature if it is valid
                data.hkpg[frm_i] = 
                   (data.hkpg_raw[frm_i] * hkpg_scale[mod40]) + 
                   hkpg_offset[mod40];
                CDF_Gen.putData(
                   hkpg_cdf, DataHolder.hkpg_label[mod40], hkpg_rec, 
                   Double.valueOf(data.hkpg[frm_i]), 0L
                );
                if(data.hkpg[frm_i] > 0){magTemp = data.hkpg[frm_i];}
                break;
             case 36:
                CDF_Gen.putData(
                   hkpg_cdf, "numOfSats", hkpg_rec, 
                   Short.valueOf(data.sats[frm_i]), 0L
                );
                CDF_Gen.putData(
                   hkpg_cdf, "timeOffset", hkpg_rec, 
                   Short.valueOf(data.offset[frm_i]), 0L
                );
                break;
             
             case 37:
                CDF_Gen.putData(
                   hkpg_cdf, "weeks", hkpg_rec, 
                   Integer.valueOf(data.weeks[frm_i]), 0L
                );
                break;
             
             case 38:
                CDF_Gen.putData(
                   hkpg_cdf, "termStatus", hkpg_rec, 
                   Short.valueOf(data.termStat[frm_i]), 0L
                );
                CDF_Gen.putData(
                   hkpg_cdf, "cmdCounter", hkpg_rec, 
                   Integer.valueOf(data.cmdCnt[frm_i]), 0L
                );
                break;
             
             case 39:
                CDF_Gen.putData(
                   hkpg_cdf, "dcdCounter", hkpg_rec, 
                   Short.valueOf(data.dcdCnt[frm_i]), 0L
                );
                CDF_Gen.putData(
                   hkpg_cdf, "modemCounter", hkpg_rec, 
                   Short.valueOf(data.modemCnt[frm_i]), 0L
                );
                   
                break;
             
             default:
                data.hkpg[frm_i] = 
                   (data.hkpg_raw[frm_i] * hkpg_scale[mod40]) + 
                   hkpg_offset[mod40];
                CDF_Gen.putData(
                   hkpg_cdf, DataHolder.hkpg_label[mod40], 
                   hkpg_rec, data.hkpg[frm_i], 0L
                );
                break;
          }
          
          //B
          //get gain correction
          for(int set_i = 0; set_i < 4; set_i++){
             mag_rec++;
             
             //extract the mag data
             data.magx[frm_i][set_i] = 
                (data.magx_raw[frm_i][set_i] - 8388608) / 83886.070;
             data.magy[frm_i][set_i] = 
                (data.magy_raw[frm_i][set_i] - 8388608) / 83886.070;
             data.magz[frm_i][set_i] = 
                (data.magz_raw[frm_i][set_i] - 8388608) / 83886.070;
             
             //do mag gain correction if we have a valid temperature
             if(magTemp > 0){
                //get the gain correction factors
                data.magx[frm_i][set_i] *= magCal[1] * (magTemp - 20);   
                data.magy[frm_i][set_i] *= magCal[2] * (magTemp - 20);
                data.magx[frm_i][set_i] *= magCal[3] * (magTemp - 20);
             }
             
             //calculate magnitude of field
             mag_tot = 
                Math.sqrt(
                   Math.pow(data.magx[frm_i][set_i], 2) + 
                   Math.pow(data.magy[frm_i][set_i], 2) + 
                   Math.pow(data.magz[frm_i][set_i], 2)
                );
             
             //save data
             CDF_Gen.putData(
                mag_cdf, "MAG_X", mag_rec, 
                Double.valueOf(data.magx[frm_i][set_i]), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "MAG_Y", mag_rec, 
                Double.valueOf(data.magy[frm_i][set_i]), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "MAG_Z", mag_rec, 
                Double.valueOf(data.magx[frm_i][set_i]), 0L
             );
             CDF_Gen.putData(
                mag_cdf, "Total", mag_rec, 
                Double.valueOf(mag_tot), 0L
             );
             
             //Add offset to time and epoch because this is 4Hz data 
             CDF_Gen.putData(
                mag_cdf, "Epoch", mag_rec, 
                Long.valueOf((data.epoch[frm_i]+(250000000 * set_i))),0L
             );
             CDF_Gen.putData(
                mag_cdf, "FrameGroup", mag_rec, 
                Integer.valueOf(data.frameNum[frm_i]), 0L
             );
          }
          
          //Extract Energy Spectra
    
          //get spectra edges based on temperature
          realSpectraEdges = SpectrumExtract.getBinEdges(2.44 * 1.01); //FIX THIS
    
          //Slow Spectrum
         System.out.println((mod32 * 12));
          System.arraycopy(
             data.sspc_raw[frm_i], 0, 
             sspc_raw, (mod32 * 8), 
             data.sspc_raw[frm_i].length
          );
           
          if(mod32 == 0){
             sspc_rec++;
             
             sspc_first_frm = data.frameNum[frm_i];
             
             CDF_Gen.putData(
                sspc_cdf, "Epoch", sspc_rec, 
                Long.valueOf(data.epoch[frm_i]), 0L
             );
             CDF_Gen.putData(
                sspc_cdf, "FrameGroup", sspc_rec, 
                Integer.valueOf(frameGrp32), 0L
             );
          }else if(mod32 == 31){
             //make sure all of the frames are from the same frame group
             if(sspc_first_frm == frameGrp32){
                sspc_first_frm = 0;
                
                try{
                   sspc_rebin =
                      SpectrumExtract.rebin(
                         sspc_raw, realSpectraEdges[0], 
                         stdSpectraEdges[0], true
                      );
                   
                   sspc_error = getCountError(sspc_raw);
                   countsToEnergy(sspc_rebin, stdSpectraEdges[0]);
                   
                   for(int chan_i = 0; chan_i < 8; chan_i ++){
                      CDF_Gen.putData(
                         sspc_cdf, "SSPC", sspc_rec, 
                         Double.valueOf(sspc_rebin[chan_i]), 
                         ((8 * mod32) + chan_i)
                      );
                      CDF_Gen.putData(
                         sspc_cdf, "SSPC_ERROR", sspc_rec, 
                         Double.valueOf(sspc_error[chan_i]), 
                         ((8 * mod32) + chan_i)
                      );
                   }
          
                }catch(NullPointerException ex){
                   //found an incomplete spectrum... ignore it.
                   System.out.println(
                      "Incomplete spectrum in frame group " + 
                      frameGrp32 + "."
                   );
                }
                sspc_raw = new Integer[256];
                sspc_rebin = new Double[256];
                sspc_error = new Double[256];
             }
          }
          
          
          //Medium Spectrum
          System.arraycopy(
             data.mspc_raw[frm_i], 0, 
             mspc_raw, (mod4 * 12), 
             data.mspc_raw[frm_i].length);
          if(mod4 == 0){
             mspc_rec++;
             
             mspc_first_frm = data.frameNum[frm_i];
             
             CDF_Gen.putData(
                mspc_cdf, "Epoch", mspc_rec, data.epoch[frm_i], 0L
             );
             CDF_Gen.putData(
                mspc_cdf, "FrameGroup", mspc_rec, frameGrp4, 0L
             );
          }else if(mod4 == 3){
             //make sure the spectrum is made of 32 consecutive frames
             if(sspc_first_frm == frameGrp4){
                mspc_first_frm = 0;
                try{
                   mspc_rebin = 
                      SpectrumExtract.rebin(
                         mspc_raw, realSpectraEdges[1], 
                         stdSpectraEdges[1], true
                      );
                   
                   mspc_error = getCountError(data.mspc_raw[frm_i]);
                   countsToEnergy(mspc_rebin, stdSpectraEdges[1]);
                   
                   for(int chan_i = 0; chan_i < 12; chan_i ++){
                      CDF_Gen.putData(
                         mspc_cdf, "MSPC", mspc_rec, 
                         Double.valueOf(mspc_rebin[chan_i]), 
                         ((12 * mod4) + chan_i)
                      );
                      CDF_Gen.putData(
                         mspc_cdf, "MSPC_ERROR", mspc_rec, 
                         Double.valueOf(mspc_error[chan_i]), 
                         ((12 * mod4) + chan_i)
                      );
                   }
                }catch(NullPointerException ex){
                   // Incomplete spectrum
                   System.out.println(
                      "Incomplete spectrum in frame group " + 
                      frameGrp4 + "."
                   );
                }
                mspc_raw = new Integer[48];
                mspc_rebin = new Double[48];
                mspc_error = new Double[48];
             }
          }
          
          //Fast Spectrum
          for(int set_i = 0; set_i < 20; set_i++){
             fspc_raw[0] = data.lc1_raw[frm_i][set_i];
             fspc_raw[1] = data.lc2_raw[frm_i][set_i];
             fspc_raw[2] = data.lc3_raw[frm_i][set_i];
             fspc_raw[3] = data.lc4_raw[frm_i][set_i];
             
             fspc_rebin = 
                SpectrumExtract.rebin(
                   fspc_raw, realSpectraEdges[2], stdSpectraEdges[2], true
                );
             
             //get error on counts
             fspc_error = getCountError(fspc_raw);
    
             //convert counts and count error to cnts/kev/sec
             countsToEnergy(fspc_error, stdSpectraEdges[2]);
             countsToEnergy(fspc_rebin, stdSpectraEdges[2]);
    
             fspc_rec++;
             CDF_Gen.putData(
                fspc_cdf, "FrameGroup", fspc_rec, 
                Integer.valueOf(data.frameNum[frm_i]), 0L
             );
          
             //write spectra to cdf files
             //Add epoch and time offsets because data comes at 20Hz
             CDF_Gen.putData(
                fspc_cdf, "Epoch", fspc_rec, 
                Long.valueOf((data.epoch[frm_i]+(50000000 * set_i))), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC1", fspc_rec, 
                Double.valueOf(fspc_rebin[0]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC1_ERROR", fspc_rec, 
                Double.valueOf(fspc_error[0]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC2", fspc_rec, 
                Double.valueOf(fspc_rebin[1]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC2_ERROR", fspc_rec, 
                Double.valueOf(fspc_error[1]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC3", fspc_rec, 
                Double.valueOf(fspc_rebin[2]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC3_ERROR", fspc_rec, 
                Double.valueOf(fspc_error[2]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC4", fspc_rec, 
                Double.valueOf(fspc_rebin[3]), 0L
             );
             CDF_Gen.putData(
                fspc_cdf, "LC4_ERROR", fspc_rec, 
                Double.valueOf(fspc_error[3]), 0L
             );
             
             fspc_raw = new Integer[4];
             fspc_rebin = new Double[4];
             fspc_error = new Double[4];
          
          }
          
          //Rate Counters
          /* FIX THIS */
       }
       
       sspc_cdf.close();
       fspc_cdf.close();
       mag_cdf.close();
       hkpg_cdf.close();
       gps_cdf.close();
       pps_cdf.close();
       
       System.out.println("Created Level Two.");
    }
 
    private Double[] getCountError(Integer[] inBins){
       Double[] outBins = new Double[inBins.length];
 
       for(int bin_i = 0; bin_i < inBins.length; bin_i++){
          try{
             outBins[bin_i] = Math.sqrt(inBins[bin_i]);
          }catch(NullPointerException ex){
             outBins[bin_i] = 0.0;
          }
       }
 
       return outBins;
    }
    
    private void countsToEnergy(Double[] bins, double[] edges){
       for(int bin_i = 0; bin_i < bins.length; bin_i++){
          try{
             bins[bin_i] =
                (bins[bin_i] / Math.abs(edges[bin_i + 1] - edges[bin_i]));
          }catch(NullPointerException ex){
             bins[bin_i] = 0.0;
          }
       }
    }
 
    private Double[] getCalDat(String fileName, int fields, String id, int id_i){
       String[] tempArr = null;
       Double[] outArr = new Double[fields];
       String line;
       FileReader fr = null;
       BufferedReader br = null;
       
       try{
          fr = new FileReader(fileName);
          br = new BufferedReader(fr);
          
          while((line = br.readLine()) != null){
             tempArr = line.split(",");
             if(tempArr[id_i].equals(id)){
                br.close();
                
                for(int el_i = 0; el_i < fields; el_i++){
                   outArr[el_i] = Double.parseDouble(tempArr[el_i]);
                }
                
                return outArr;
             }
          }
          
          br.close();
          fr.close();
       }catch(IOException ex){
          System.out.println(ex.getMessage());
          
       }finally{
          try{
             if(br != null){br.close();}
             if(fr != null){fr.close();}
          }catch(IOException ex){
             System.out.println("Could not close file:" + ex.getMessage());
          }
          
       }
       
       return outArr;
    }
 }
