 /*
 ExactTiming.java
 
 Description:
    Uses a block of gps time info to create a more exact time variable.
    Ported from MPM's C code.
 
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
 */
 
 package edu.ucsc.barrel.cdf_gen;
 
 import org.apache.commons.math3.stat.regression.SimpleRegression;
 import java.util.Arrays;
 
 public class ExtractTiming {
    //Set some constant values
    private static final int MAX_RECS = 500;// max number of mod4 recs for model
    private static final double NOM_RATE = 999.89;// nominal ms per frame
    private static final int MSPERWEEK = 604800000;// mseconds in a week
    private static final short PPSFILL = -32768;// fill value for PPS
    private static final int MSFILL = -2147483648;// fill value for ms_of_week
    private static final int FCFILL = -2147483648;// fill value for frame counter
    private static final short WKFILL = -32768;// fill value for week
    private static final short MINWK = 1200;
    private static final byte MINPPS = 0;
    private static final byte MINMS = 1;
    private static final byte MINFC = 0;
    private static final short MAXWK = 1880;
    private static final short MAXPPS = 1000;
    private static final int MAXMS = 604800000;
    private static final int MAXFC = 2097152;
    
    //quality flags
    private static final short FILLED = 1;
    private static final short NOINFO = 2;
    private static final short BADMOEL = 4;
    private static final short BADFC = 8;
    private static final short BADMS = 16;
    private static final short BADWK = 32;
    private static final short BADPPS = 64;
 
    private long today;
 
    private class TimeRec{
       private long ms;//frame timestamp
       private long frame;//frame counter
       private long GPS_EPOCH = -630763148816L;//number of ms from Jan 6, 1980 to J2000
 
       public TimeRec(long fc, long msw, short weeks, short pps){
          //figure out if we need to add an extra second based on the PPS
          int extra_ms = (pps < 241) ? 0 : 1000;
          
          //get the number of ms between GPS_START_TIME and start of this week
          long weeks_in_ms = (long)weeks * (long)MSPERWEEK;
 
          //save the frame number
          frame = fc;
 
          //calculate the number of milliseconds since J2000 
          ms = 
             weeks_in_ms + msw + extra_ms - pps + GPS_EPOCH;
       }
       
       public long getMS(){return ms;}
       public long getFrame(){return frame;}
    }
    
    private class LinModel{
       private long first_frame, last_frame;
       private double slope, intercept;
 
       public void setFirst(long fc){first_frame = fc;}
       public void setLast(long fc){last_frame = fc;}
       public void setSlope(double s){slope = s;}
       public void setIntercept(double i){intercept = i;}
 
       public long getFirst(){return first_frame;}
       public long getLast(){return last_frame;}
       public double getSlope(){return slope;}
       public double getIntercept(){return intercept;}
    }
 
    //declare an array of time pairs
    private TimeRec[] time_recs;
    private int time_rec_cnt = 0;
 
    //declare array of linear models
    private LinModel[] models;
    private int model_cnt = 0;
 
    //holder for external data set
    private DataHolder data;
    
    public ExtractTiming(String d){
       //save today's date
       today = Long.valueOf(d);
       
       //get DataHolder storage object
       data = CDF_Gen.getDataSet();
       
       int temp, day, fc, week, ms, pps, cnt, mod40;
       int rec_i = 0, frame_i = 0;
       
       time_recs = new TimeRec[data.getSize("mod4")];
       models = new LinModel[(data.getSize("1Hz") / MAX_RECS) + 1];
    }
 
    public void getTimeRecs(){
       int fc_offset, rec_mod40_i, rec_1Hz_i;
       short week, pps;
       long ms, fc;
 
       //cycle through the entire data set and create an array of time records
       for(int rec_mod4_i = 0; rec_mod4_i < time_recs.length; rec_mod4_i++){
          //make sure we have a valid ms
          ms = data.ms_of_week[rec_mod4_i];
          if((ms <= MINMS) || (ms >= MAXMS)){continue;}
          
          //get initial fc from the mod4 framegroup
          fc = data.frame_mod4[rec_mod4_i]; //last good fc from this mod4 group
          if((fc <= MINFC) || (fc >= MAXFC)){continue;}
 
          //figure out the offset from mod4 fc and 1Hz fc
          fc -= ((fc % 4) - DataHolder.TIME); 
          
          //get the indices of other cadence data
          rec_1Hz_i = data.convertIndex(rec_mod4_i, fc, "mod4", "1Hz");
          rec_mod40_i = data.convertIndex(rec_mod4_i, fc, "mod4", "mod40");
 
          //figure out if pps is valid 
          pps = (short)data.pps[rec_1Hz_i];
          if((pps <= MINPPS) || (pps >= MAXPPS)){continue;}
 
          //get number of weeks since GPS_START_TIME
          week = (short)data.weeks[rec_mod40_i];
          if((week <= MINWK) || (week >= MAXWK)){continue;}
          
          time_recs[time_rec_cnt] = new TimeRec(fc, ms, week, pps);
          time_rec_cnt++;
       }
    }
    
    public void fillModels(){
       int last_rec = 0, frame_i = 0, size_1Hz;
       long last_frame;
       SimpleRegression fit = null, new_fit = null;
       
       size_1Hz = data.getSize("1Hz");
       //create a model for each batch of time records
       for(int first_rec = 0; first_rec < time_rec_cnt; first_rec = last_rec+1){
          //incriment the last_rec by the max, or however many recs are left
          last_rec += Math.min(MAX_RECS, (time_rec_cnt - first_rec));
          
          //try to generate a model
          new_fit = genModel(first_rec, last_rec);
 
          //Need to add better criteria than this for accepting a new model
          if(new_fit != null){
             fit = new_fit;
             models[model_cnt] = new LinModel();
             models[model_cnt].setSlope(fit.getSlope()); 
             models[model_cnt].setIntercept(fit.getIntercept()); 
             models[model_cnt].setFirst(time_recs[first_rec].getFrame()); 
             models[model_cnt].setLast(time_recs[last_rec].getFrame()); 
             model_cnt++;
 
             System.out.println(
                "Frames " + time_recs[first_rec].getFrame() + " - " +
                time_recs[last_rec].getFrame()); 
             System.out.println(
                "\tm = " + fit.getSlope() + ", b = " + fit.getIntercept() + 
                " slope error = " + fit.getSlopeStdErr() + " n = " + fit.getN()
             );
          }else{
             System.out.println(
                "Failed to get model using " + (last_rec-first_rec) + "records"
             );
          }
       }
 
       if(fit == null){
          //no timing model was ever created. 
          //Use slope=1000 and intercept=0 to use frame number epoch.
          //this will clearly not give a good result for time, but will
          //allow the data to be plotted as a time series.
          //This will be a place to add a quality flag
          models[model_cnt] = new LinModel();
          models[model_cnt].setSlope(1000); 
          models[model_cnt].setIntercept(0); 
          models[model_cnt].setFirst(0); 
          models[model_cnt].setLast(data.frame_1Hz[size_1Hz]); 
          model_cnt = 1;
       }
    }
 
    public void fillEpoch(){
       long fc; 
       int date_offset, size;
       double m, b;
       
       //fill the 1Hz and faster timestamps
       size = data.getSize("1Hz");
       for(int data_i = 0, model_i = 0; data_i < size; data_i++){
          fc = data.frame_1Hz[data_i];
          data.epoch_1Hz[data_i] = calcEpoch(fc, model_i);
 
          //save epoch to the various time scales
          //fill the >1Hz times 
          for(int fill_i = 0; fill_i < 4; fill_i++){
             data.epoch_4Hz[(data_i * 4) + fill_i] = 
                data.epoch_1Hz[data_i] + (fill_i * 250000000);
          }
          for(int fill_i = 0; fill_i < 20; fill_i++){
             data.epoch_20Hz[(data_i * 20) + fill_i] = 
                data.epoch_1Hz[data_i] + (fill_i * 50000000);
          }
       }
 
       //fill mod4 timestamps
       size = data.getSize("mod4");
       for(int data_i = 0, model_i = 0; data_i < size; data_i++){
          fc = data.frame_mod4[data_i];
          data.epoch_mod4[data_i] = calcEpoch(fc, model_i);
       }
 
       //fill mod32 timestamps
       size = data.getSize("mod32");
       for(int data_i = 0, model_i = 0; data_i < size; data_i++){
          fc = data.frame_mod32[data_i];
          data.epoch_mod32[data_i] = calcEpoch(fc, model_i);
       }
 
       //fill mod40 timestamps
       size = data.getSize("mod40");
       for(int data_i = 0, model_i = 0; data_i < size; data_i++){
          fc = data.frame_mod40[data_i];
          data.epoch_mod40[data_i] = calcEpoch(fc, model_i);
       }
    }
 
    public void fixWeekOffset(){
       /*
       Because the each "day" of data most likely contains some portion of a 
       call that started before 00:00 and/or lasted until after 23:59 of the 
       current day, we might have a data set that spans the Sat/Sun week boundry.
       The week variable is only transmitted once every 40 frames, so there is 
       the potential for the epoch variable to jump back when the ms_of_week 
       rolls over.
       */
       
       int initial_week = 0, initial_ms = 0;
       
       //start looking for rollover
       for(int ms_i = 0; ms_i < data.getSize("mod4"); ms_i++){
          //try to find and initial set of 
          //timestamps and week variables if needed.
         if(initial_week == 0){initial_week = data.weeks[ms_i];}
         if(initial_ms == 0){initial_ms = data.ms_of_week[ms_i/10];}
 
          //check to see if the ms_of_week rolled over
          //the value given by the gps might jump around a bit, so make sure 
          //the roll back is significant (>1min)
          if((data.ms_of_week[ms_i] - initial_ms) < -60000){
             //check if the week variable was updated
             if(data.weeks[ms_i/10] != 0 && data.weeks[ms_i/10] == initial_week){
                //the week variable has not yet updated,
                // add 1 week of ms to the ms_of_week variable
                data.ms_of_week[ms_i] += 604800000;
             }
          }
       }
    }
    
    private long calcEpoch(long fc, int model_i){
       double m, b;
       //select a model for this frame
       if(fc > models[model_i].getLast()){
          //frame came after the last valid fc for the current model
          for(int new_i = 0; new_i < model_cnt; new_i++){
             //loop through the remaining models
             if(fc <= models[new_i].getLast()){
                //stop looping when we find a model that has a 
                //fc range containing this frame
                model_i = new_i;
                break;
             }
          }
       }
 
       m = models[model_i].getSlope();
       b = models[model_i].getIntercept();
       
       return (long)((m * fc) + b) * 1000000L;
    }
    private SimpleRegression genModel(int first, int last){
       int cnt = last - first;
       double[] offsets = new double[cnt];
       double med;
       SimpleRegression fit = new SimpleRegression();
 
       //not enough pairs to continue
       if(cnt < 3){return null;}
       
       //get offsets from set of time pairs
       //"offsets" are the difference between the nominal time guess and
       //the time that was transmitted
       for(int rec_i = first, offset_i = 0; rec_i < last; rec_i++, offset_i++){
          offsets[offset_i] = 
             time_recs[rec_i].getMS() - 
             (NOM_RATE * time_recs[rec_i].getFrame());
       }
       
       //find the median offset value
       med = median(offsets);
 
       //Find all the points that are within 200ms from the median offset
       //and add them to the model
       for (int rec_i = 0; rec_i < cnt; rec_i++){
          if(Math.abs(offsets[rec_i] - med) < 200){
             fit.addData(time_recs[rec_i].getFrame(), time_recs[rec_i].getMS());
          }
       }
       return fit;
    }
    
    private double median(double[] list){
       double[] sortedList = new double[list.length];
       
       //copy the input list and sort it
       System.arraycopy(list, 0, sortedList, 0, list.length);
       
       Arrays.sort(sortedList);
       
       if(list.length > 2){
          return sortedList[(int) (sortedList.length / 2) + 1];
       }else{
          return sortedList[0];
       }
    }
 }
