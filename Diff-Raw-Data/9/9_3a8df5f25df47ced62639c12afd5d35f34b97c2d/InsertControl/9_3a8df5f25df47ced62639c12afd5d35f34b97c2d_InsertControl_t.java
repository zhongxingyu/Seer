 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.meteorologaaguascalientes.control;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import org.meteorologaaguascalientes.dao.Dao;
 import org.meteorologaaguascalientes.dao.DaoList;
 import org.meteorologaaguascalientes.model.AtmosphericPressure;
 import org.meteorologaaguascalientes.model.Pluviosity;
 import org.meteorologaaguascalientes.model.Sample;
 import org.meteorologaaguascalientes.model.Temperature;
 
 /**
  *
  * @author josebermeo
  */
 public class InsertControl {
 
     
     public InsertControl() {}
     
     public boolean insertValues(Map<String,String> data){
         //check for null keys or null values
         if(checkNullElements(data.keySet().toArray()))
            return false;
         if(checkNullElements(data.values().toArray()))
            return false;
         
         //Load data base
         HashMap<String, Dao> database = DaoList.getInstance().getDaoMap();
         
         //checking time
         String timestring = data.get("sample");
         Date time;
         try{
             time = (new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa")).parse(timestring);
            Calendar c = Calendar.getInstance();
            c.setTime(time);
            if(c.get(Calendar.YEAR) < 1000){
                throw new Exception();
            }
        }catch(Exception e){
             return false;
         }
 	
         Sample sample = new Sample(time);
         
         double val;
         
         //checking temperature
         String temperaturaString = data.get("temperature");
         try{
             val = Double.parseDouble(temperaturaString);
         }catch(NumberFormatException e){
             return false;
         }
         Temperature temperature = new Temperature(val);
         temperature.setTime(time);
         
         //checking pluviosity
         String pluviosityString = data.get("pluviosity");
         try{
             val = Double.parseDouble(pluviosityString);
         }catch(NumberFormatException e){
             return false;
         }
         Pluviosity pluviosity = new Pluviosity(val);
         pluviosity.setTime(time);
         
         //checking atmosphericPressure
         String atmosphericPressureString = data.get("atmosphericPressure");
         try{
             val = Double.parseDouble(atmosphericPressureString);
         }catch(NumberFormatException e){
             return false;
         }
         AtmosphericPressure atmosphericPressure = new AtmosphericPressure(val);
         atmosphericPressure.setTime(time);
         
         //Saving
         if (!database.get("sample").createRecord(sample))
 		return false;
         database.get("temperature").createRecord(temperature);
         database.get("pluviosity").createRecord(pluviosity);
         database.get("atmosphericPressure").createRecord(atmosphericPressure);
         return true; 
     }
     
     private boolean checkNullElements(Object[] list){
         for (Object element: list)
             if(element == null)
                 return true;
         return false;
     }
     
 }
