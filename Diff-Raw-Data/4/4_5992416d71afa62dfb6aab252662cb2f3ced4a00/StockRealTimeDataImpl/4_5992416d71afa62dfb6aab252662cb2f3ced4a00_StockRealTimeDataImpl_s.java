 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.brown.cs32.atian.crassus.backend;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author lyzhang
  */
 public class StockRealTimeDataImpl implements StockRealTimeData {
 
     private String _ticker;
     private ArrayList<StockTimeFrameData> _realtimeData;
     private Date latestRecordDate = null;
     private String _high = "N/A";
     private String _low = "N/A";
     private String _open = "N/A";
     private String _curr = "N/A";
     private String _high52Week = "N/A";
     private String _low52Week = "N/A";
     private String _changeAndPtgChange = "N/A";
 
     public StockRealTimeDataImpl(String ticker) {
         _ticker = ticker;
         _realtimeData = new ArrayList<StockTimeFrameData>();
     }
 
     private void getStockTableData() {
         //http://www.gummy-stuff.org/Yahoo-data.htm
         String urlString = "http://finance.yahoo.com/d/quotes.csv?s=" + _ticker + "&f=sghjkol1c";
         //MSFT	N/A	N/A	26.26	32.52	N/A	33.1
 //  ticker	Day's Low	high	52 low	52 high	open	price
 
         // http://finance.yahoo.com/d/quotes.csv?s=MSFT&f=sghjkol1      
         HttpURLConnection connection = null;
         URL serverAddress = null;
         BufferedReader reader = null;
         String line = null;
 
         try {
             serverAddress = new URL(urlString);
             //set up out communications stuff
             connection = null;
 
             //Set up the initial connection
             connection = (HttpURLConnection) serverAddress.openConnection();
             connection.setRequestMethod("GET");
             connection.setDoOutput(true);
            connection.setReadTimeout(2000);
 
             connection.connect();
 
             reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 
             line = reader.readLine();  // skip the header line
             String[] splitted = line.split(",");
             if (splitted.length != 8) {
                 return;
             }
             _high = splitted[2];
             _low = splitted[1];
             _open = splitted[5];
             _curr = splitted[6];
             _high52Week = splitted[4];
             _low52Week = splitted[3];
             _changeAndPtgChange  = splitted[7];
             latestRecordDate = new Date();
         } catch (SocketTimeoutException e) {
             //e.printStackTrace();
         }        
         catch (MalformedURLException e) {
             e.printStackTrace();
 
         } catch (ProtocolException e) {
             e.printStackTrace();
 
         } catch (IOException e) {
             e.printStackTrace();
 
         } catch (NumberFormatException e) {
             e.printStackTrace();
         } 
         
         //System.err.println("Update Real Time data at " + (new Date()).toString() + " price= " + _curr);
     }
 
     @Override
     public boolean Init() {
         getStockTableData();
         _realtimeData.clear();
         
         try {
             Long time = latestRecordDate.getTime()/1000;
             boolean isHist = false;
             double open = Double.parseDouble(_open);
             double high = Double.parseDouble(_high);
             double low = Double.parseDouble(_low);  
             double close = Double.parseDouble(_curr);         
             int volume = 0;  // no support yet
             double adjustedClose = close;
 
             StockTimeFrameData newTFDate = new StockTimeFrameData(time.toString(), open, high, low, close,  volume, adjustedClose, isHist);
             _realtimeData.add(newTFDate);
         } catch(NumberFormatException e) {
             return false;
         }
         return true;
 //        _realtimeData.clear();
 //
 //        String urlString = "http://chartapi.finance.yahoo.com/instrument/1.0/" + _ticker + "/chartdata;type=quote;range=1d/csv/";
 //        // http://chartapi.finance.yahoo.com/instrument/1.0/msft/chartdata;type=quote;range=1d/csv/        
 //        HttpURLConnection connection = null;
 //        URL serverAddress = null;
 //        BufferedReader reader = null;
 //        String line = null;
 //
 //        try {
 //            serverAddress = new URL(urlString);
 //            //set up out communications stuff
 //            connection = null;
 //
 //            //Set up the initial connection
 //            connection = (HttpURLConnection) serverAddress.openConnection();
 //            connection.setRequestMethod("GET");
 //            connection.setDoOutput(true);
 //            connection.setReadTimeout(10000);
 //
 //            connection.connect();
 //            //read the result from the server
 //            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 //            //sb = new StringBuilder();
 //
 //            String[] sep_list = {",", ":"};
 //            StringBuilder regexp = new StringBuilder("");
 //            regexp.append("[");
 //            for (String s : sep_list) {
 //                regexp.append(Pattern.quote(s));;
 //            }
 //            regexp.append("]");
 //
 //            line = reader.readLine();  // skip the header line
 //            while ((line = reader.readLine()) != null) {
 //
 //                String[] splitted = line.split(regexp.toString());
 //
 //                if (splitted.length != 6) {
 //                    continue;
 //                }
 //                if (splitted[5].equals("volume") || splitted[0].equals("labels")) {
 //                    continue;
 //                }
 //
 //                StockTimeFrameData newTFData = new StockTimeFrameData(splitted[0], //Time
 //                        Double.parseDouble(splitted[4]), //Open				
 //                        Double.parseDouble(splitted[2]), //High				
 //                        Double.parseDouble(splitted[3]), //Low
 //                        Double.parseDouble(splitted[1]), //Close	
 //                        Integer.parseInt(splitted[5]), //Volume
 //                        Double.parseDouble(splitted[1]), // realtime data from yahoo has not adjusted close, we set it equal to Close
 //                        false);
 //
 //                _realtimeData.add(newTFData);   // from the earliest to the latest
 //            }
 //
 //            return true;
 //        } catch (MalformedURLException e) {
 //            e.printStackTrace();
 //            return false;
 //        } catch (ProtocolException e) {
 //            e.printStackTrace();
 //            return false;
 //        } catch (IOException e) {
 //            e.printStackTrace();
 //            return false;
 //        } catch (NumberFormatException e) {
 //            e.printStackTrace();
 //            return false;
 //        }
     }
 
     @Override
     public List<StockTimeFrameData> getRealTimeData() {
         return _realtimeData;
     }
     
     @Override
     public String getChgAndPertChg() {
         return _changeAndPtgChange;
     }
     
     private String format(String in) {
         try {
             Double tmp = Double.parseDouble(in);
             return String.format("%1$,.2f", tmp);
         } catch (NumberFormatException e) {
             return "N/A";
         }
     }
     
     @Override
     public String getOpenPrice() {
 
         return format(this._open);
     }
     
 
     
     @Override
     public String getCurrPrice() {
         return format(this._curr);
     }
 
     @Override
     public String getTodayLow() {
 
         return format(this._low);
     }
 
     @Override
     public String getTodayHigh() {
         return format(this._high);
     }
 
     @Override
     public String getWeek52Low() {
         return format(this._low52Week);
     }
 
     @Override
     public String getWeek52High() {
         return format(this._high52Week);
     }
 
     @Override
     public void refresh() {
         this.Init();
     }
 }
