 package edu.ucsc.barrel.cdf_gen;
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 import gsfc.nssdc.cdf.Variable;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.Calendar;
 import java.util.Vector;
 import java.util.Arrays;
 
 /*
 LevelTwo.java v13.02.28
 
 Description:
    LevelTwo.java pulls data from the DataHolder.java object and processes it 
    into physical units (when needed) and outputs CDF files in the L2 directory.
 
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    This file is part of The BARREL CDF Generator.
 
    The BARREL CDF Generator is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    The BARREL CDF Generator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License along with 
    The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 Change Log:
    v13.02.28
       -Now outputs correct L2 values for all variables except spectra (Still
          needs rebin)
    v13.02.15
       -Updated to match the current version of Level One
    
    v13.02.06
       -New version of Level Two. An exact copy of Level One for now...
 */
 
 
 public class LevelTwo{
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
       revNum = "00",
       mag_id = "";
    Calendar dateObj = Calendar.getInstance();
    
    short INCOMPLETE_GROUP = 8196;
    
    private DataHolder data;
    
    public LevelTwo(
       final String d, final String p, 
       final String f, final String s, final String m
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
       mag_id = m;
 
       //get the data storage object
       data = CDF_Gen.getDataSet();
       
       //set output path
       outputPath = CDF_Gen.L2_Dir;
       File outDir = new File(outputPath);
       if(!outDir.exists()){outDir.mkdirs();}
       
       //copy the CDF skeletons to the new files 
       for(int type_i = 0; type_i < CDF_Gen.fileTypes.length; type_i++){
          String srcName = 
             "cdf_skels/l2/" + "barCLL_PP_S_l2_" + 
             CDF_Gen.fileTypes[type_i] + "_YYYYMMDD_v++.cdf";
          String destName = 
             outputPath + "bar1" + flt + "_" + id + "_" + stn + "_l2_" +
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
    
    //Convert the GPS data and save it to CDF files
    public void doGpsCdf() throws CDFException{
       int numOfRecs = data.getSize("mod4");
       CDF cdf;
       Variable var;
 
       float[] 
          lat = new float[numOfRecs], 
          lon = new float[numOfRecs], 
          alt = new float[numOfRecs];
       
       System.out.println("\nSaving GPS Level 2 CDF...");
 
       //convert lat, lon, and alt values
       for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
         //convert mm to km
         alt[rec_i] = (float)data.gps_raw[0][rec_i] / 1000000;
 
         //convert lat and lon to physical units
         lat[rec_i] = (float)data.gps_raw[2][rec_i];
         if((data.gps_raw[2][rec_i] >> 31) > 0){
            lat[rec_i] -=  0x100000000L;
         }
         lat[rec_i] *= 
            Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());
 
         lon[rec_i] = (float)data.gps_raw[3][rec_i];
         if((data.gps_raw[3][rec_i] >> 31) > 0){
            lon[rec_i] -=  0x100000000L;
         }
         lon[rec_i] *= 
            Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());
       }
 
       //open GPS CDF and save the reference in the cdf variable
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_gps-_20" + date +  "_v" + revNum + ".cdf"
       );
 
       var = cdf.getVariable("GPS_Alt");
       System.out.println("GPS_Alt...");
       var.putHyperData(
          0, numOfRecs, 1,
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          alt
       );
 
       var = cdf.getVariable("ms_of_week");
       System.out.println("ms_of_week...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.gps_raw[1]
       );
 
       var = cdf.getVariable("GPS_Lat");
       System.out.println("GPS_Lat...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lat 
       );
 
       var = cdf.getVariable("GPS_Lon");
       System.out.println("GPS_Lon...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lon
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_mod4
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_mod4
       );
       
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.gps_q
       );
 
       System.out.println("Done with GPS!");
       //close current cdf
       cdf.close();
    }
    
    //write the pps file, no processing needed
    public void doPpsCdf() throws CDFException{
       int numOfRecs = data.getSize("1Hz");
       CDF cdf;
       Variable var;
       
       System.out.println("\nSaving PPS Level Two CDF...");
 
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_pps-_20" + date +  "_v" + revNum + ".cdf"
       );
       
       var = cdf.getVariable("GPS_PPS");
       System.out.println("GPS_PPS...");
       var.putHyperData(
          0L, numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.pps
       );
 
       var = cdf.getVariable("Version");
       System.out.println("Version...");
       var.putHyperData(
          0L, numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.ver
       );
 
       var = cdf.getVariable("Payload_ID");
       System.out.println("Payload_ID...");
       var.putHyperData(
          0L, numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.payID
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0L, numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_1Hz
       );
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0L, numOfRecs, 1L,
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_1Hz
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0L, numOfRecs, 1L, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.pps_q
       );
 
       cdf.close();
    }
    
    public void doMagCdf() throws CDFException{
       int numOfRecs = data.getSize("4Hz");
 
       CDF cdf;
       Variable var;
       
       double[] 
          magx = new double[numOfRecs],
          magy = new double[numOfRecs],
          magz = new double[numOfRecs],
          magTot = new double[numOfRecs];
 
       float slopex = 0.0f, slopey = 0.0f, slopez = 0.0f;
 
       System.out.println("\nSaving Magnetometer Level Two CDF...");
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_magn_20" + date +  "_v" + revNum + ".cdf"
       );
      
       //get gain correction slope for this payload
 	   try{
          FileReader fr = new FileReader("magGain.cal");
          BufferedReader iniFile = new BufferedReader(fr);
          String line;
 
          while((line = iniFile.readLine()) != null){
             String[] fields = line.split(",");
             if(fields[0].equals(mag_id)){
                slopex = Float.parseFloat(fields[1]);
                slopey = Float.parseFloat(fields[2]);
                slopez = Float.parseFloat(fields[3]);
                break;
             }
          }      
       }catch(IOException ex){
          System.out.println(
             "Could not read config file: " + ex.getMessage()
          );
       }
 
       //extract the nominal magnetometer value and calculate |B|
       for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
          magx[rec_i] = (data.magx_raw[rec_i] - 8388608.0) / 83886.070;
          magy[rec_i] = (data.magy_raw[rec_i] - 8388608.0) / 83886.070;
          magz[rec_i] = (data.magz_raw[rec_i] - 8388608.0) / 83886.070;
 
          magTot[rec_i] = 
             Math.sqrt(
                (magx[rec_i] * magx[rec_i]) + 
                (magy[rec_i] * magy[rec_i]) +
                (magz[rec_i] * magz[rec_i]) 
             );
       }
 
       //store the nominal mag values
       var = cdf.getVariable("MAG_X");
       System.out.println("MAG_X... ");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magx 
       );
 
       var = cdf.getVariable("MAG_Y");
       System.out.println("MAG_Y...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magy
       );
 
       var = cdf.getVariable("MAG_Z");
       System.out.println("MAG_Z...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magz
       );
 
       var = cdf.getVariable("Total");
       System.out.println("Field Magnitude...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magTot 
       );
 
       //do gain correction on nominal values
       for(int mag_rec = 0; mag_rec < numOfRecs; mag_rec++){
          int hkpg_rec = mag_rec / 160; //convert from 4Hz to mod40
          float magTemp = data.hkpg_raw[data.T1][hkpg_rec];
 
          magTemp = (magTemp != 0) ? magTemp * data.hkpg_scale[data.T1] : 20;
          magx[mag_rec] = magx[mag_rec] * (slopex * (magTemp - 20) + 1);
          magy[mag_rec] = magy[mag_rec] * (slopey * (magTemp - 20) + 1);
          magz[mag_rec] = magz[mag_rec] * (slopez * (magTemp - 20) + 1);
          magTot[mag_rec] = 
             Math.sqrt(
                (magx[mag_rec] * magx[mag_rec]) + 
                (magy[mag_rec] * magy[mag_rec]) +
                (magz[mag_rec] * magz[mag_rec]) 
             );
       }
 
       //store the gain adjusted values
       var = cdf.getVariable("MAG_X_ADJ");
       System.out.println("Gain Adjusted MAG_X... ");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magx 
       );
 
       var = cdf.getVariable("MAG_Y_ADJ");
       System.out.println("Gain Adjusted MAG_Y...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magy
       );
 
       var = cdf.getVariable("MAG_Z_ADJ");
       System.out.println("Gain Adjusted MAG_Z...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magz
       );
 
       var = cdf.getVariable("Total_ADJ");
       System.out.println("Gain Adjusted Field Magnitude...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          magTot 
       );
 
 
       //save the rest of the file
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_4Hz
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_4Hz
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.magn_q
       );
 
       cdf.close();
 
    }
    
    public void doHkpgCdf() throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = data.getSize("mod40");
 
       System.out.println("\nSaving HKPG...");
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_hkpg_20" + date +  "_v" + revNum + ".cdf"
       );
          
       for(int var_i = 0; var_i < data.hkpg_scale.length; var_i++){
          //scale all the records for this variable
          double[] hkpg_scaled = new double[numOfRecs];
          for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
             hkpg_scaled[rec_i] = 
                data.hkpg_raw[var_i][rec_i] * data.hkpg_scale[var_i];
          }
 
          var = cdf.getVariable(data.hkpg_label[var_i]);
          System.out.println(data.hkpg_label[var_i] + "...");
          var.putHyperData(
             0, numOfRecs, 1, 
             new long[] {0}, 
             new long[] {1}, 
             new long[] {1}, 
             hkpg_scaled
          );
       }
 
       var = cdf.getVariable("numOfSats");
       System.out.println("numOfSats...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.sats
       );
 
       var = cdf.getVariable("timeOffset");
       System.out.println("timeOffset...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.offset
       );
       
       var = cdf.getVariable("termStatus");
       System.out.println("termStatus...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.termStat
       );
 
       var = cdf.getVariable("cmdCounter");
       System.out.println("cmdCounter...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.cmdCnt
       );
 
       var = cdf.getVariable("modemCounter");
       System.out.println("modemCounter...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.modemCnt
       );
 
       var = cdf.getVariable("dcdCounter");
       System.out.println("dcdCounter...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.dcdCnt
       );
 
       var = cdf.getVariable("weeks");
       System.out.println("weeks...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.weeks
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_mod40
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_mod40
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.hkpg_q
       );
 
       cdf.close();
    }
 
    public void doFspcCdf() throws CDFException{
       CDF cdf;
       Variable var;
       int numOfRecs = data.getSize("20Hz");
       double[][] lc_rebin = new double[4][numOfRecs];
 
       System.out.println("\nSaving FSPC...");
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_fspc_20" + date +  "_v" + revNum + ".cdf"
       );
     
       //rebin and save the light curves
       for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
          //rebin each spectra created from the 4 light curves
          for(int spc_i = 0; spc_i < 20; spc_i++){
             //create the spectrum
             float[] lc_spec = {
                data.lc1_raw[rec_i],
                data.lc2_raw[rec_i],
                data.lc3_raw[rec_i],
                data.lc4_raw[rec_i]
             };
 
             /* do something to actually rebin the spectrum here...*/
 
             //write the spectrum to the new array
             lc_rebin[0][rec_i] = lc_spec[0];
             lc_rebin[1][rec_i] = lc_spec[1];
             lc_rebin[2][rec_i] = lc_spec[2];
             lc_rebin[3][rec_i] = lc_spec[3];
          }
       }
       var = cdf.getVariable("LC1");
       System.out.println("LC1...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_rebin[0]
       );
       
       var = cdf.getVariable("LC2");
       System.out.println("LC2...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_rebin[1]
       );
 
       var = cdf.getVariable("LC3");
       System.out.println("LC3...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_rebin[2]
       );
 
       var = cdf.getVariable("LC4");
       System.out.println("LC4...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          lc_rebin[3]
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_20Hz
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_20Hz
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.fspc_q
       );
 
       cdf.close();
 
    }
 
    public void doMspcCdf() throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = data.getSize("mod4");
       double[][] mspc_rebin = new double[numOfRecs][48];
 
       //rebin the mspc spectra
       for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
          /* need to do something other than just copy the spectra*/
          for(int val_i = 0; val_i < 48; val_i++){
             mspc_rebin[rec_i][val_i] = data.mspc_raw[rec_i][val_i];
          }
       }
 
       System.out.println("\nSaving MSPC...");
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_mspc_20" + date +  "_v" + revNum + ".cdf"
       );
 
       var = cdf.getVariable("MSPC");
       System.out.println("Spectrum Arrays...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0, 0}, 
          new long[] {48, 1}, 
          new long[] {1, 1}, 
          mspc_rebin
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_mod4
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_mod4
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.mspc_q
       );
 
       cdf.close();
    }
 
    public void doSspcCdf() throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = data.getSize("mod32");
       double[][] sspc_rebin = new double[numOfRecs][256];
 
      //rebin the mspc spectra
       for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
          /* need to do something other than just copy the spectra*/
         for(int val_i = 0; val_i < 48; val_i++){
             sspc_rebin[rec_i][val_i] = data.sspc_raw[rec_i][val_i];
          }
       }
 
       System.out.println("\nSaving SSPC...");
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_sspc_20" + date +  "_v" + revNum + ".cdf"
       );
 
       var = cdf.getVariable("SSPC");
       System.out.println("Spectrum Arrays...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {256, 1}, 
          new long[] {1}, 
          sspc_rebin
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_mod32
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.epoch_mod32
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.sspc_q
       );
 
       cdf.close();
    }
 
    public void doRcntCdf() throws CDFException{
       CDF cdf;
       Variable var;
       
       int numOfRecs = data.getSize("mod4");
       double[][] rc_timeScaled = new double[4][numOfRecs];
 
       //change all the units from cnts/4sec to cnts/sec
       for(int var_i = 0; var_i < 4; var_i++){
          for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
             rc_timeScaled[var_i][rec_i] = data.rcnt_raw[var_i][rec_i] / 4;
          }
       }
 
       System.out.println("\nSaving RCNT...");
       cdf = CDF_Gen.openCDF( 
          outputPath + "bar1" + flt + "_" + id + "_" + stn +
          "_l2_rcnt_20" + date +  "_v" + revNum + ".cdf"
       );
 
       var = cdf.getVariable("Interrupt");
       System.out.println("Interrupt...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[0]
       );
 
       var = cdf.getVariable("LowLevel");
       System.out.println("LowLevel...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[1]
       );
 
       var = cdf.getVariable("PeakDet");
       System.out.println("PeakDet...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[2]
       );
 
       var = cdf.getVariable("HighLevel");
       System.out.println("HighLevel...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          rc_timeScaled[3]
       );
 
       var = cdf.getVariable("FrameGroup");
       System.out.println("FrameGroup...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.frame_mod4
       );
 
       var = cdf.getVariable("Epoch");
       System.out.println("Epoch...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1},
          data.epoch_mod4
       );
 
       var = cdf.getVariable("Q");
       System.out.println("Q...");
       var.putHyperData(
          0, numOfRecs, 1, 
          new long[] {0}, 
          new long[] {1}, 
          new long[] {1}, 
          data.rcnt_q
       );
 
       cdf.close();
    }
 
    //Pull each value out of the frame and store it in the appropriate CDF.
    private void writeData() throws CDFException{
       System.out.println(
          "Creating Level Two... (" + data.getSize("1Hz") + " frames)"
       );
       
       doGpsCdf();
       doPpsCdf();
       doMagCdf();
       doHkpgCdf();   
       doFspcCdf();   
       doMspcCdf();   
       doSspcCdf();   
       doRcntCdf();   
          
       System.out.println("Created Level Two.");
    }
  }
