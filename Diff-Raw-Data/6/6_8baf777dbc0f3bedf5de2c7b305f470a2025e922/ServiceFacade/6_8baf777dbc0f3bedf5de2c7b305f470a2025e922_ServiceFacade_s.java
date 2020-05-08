 package org.meteorologaaguascalientes.businesslogic.facade;
 
 import java.text.SimpleDateFormat;
 import java.util.Map.Entry;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.meteorologaaguascalientes.businesslogic.service.AbstractVariableService;
 import org.meteorologaaguascalientes.businesslogic.service.ServicesFactory;
 import org.meteorologaaguascalientes.control.measure.MeasuresFactory;
 import org.meteorologaaguascalientes.da.DataAccessAdapter;
 import org.meteorologaaguascalientes.da.DataAccessException;
 import org.meteorologaaguascalientes.helper.Config;
 import org.meteorologaaguascalientes.vo.VariableVo;
 
 /**
  *
  * @author jdbermeol
  */
 public class ServiceFacade {
 
     public boolean insertValues(Map<String, String> data, String timestring) throws DataAccessException, Exception {
         if (data == null) {
             throw new Exception("Map data cannot be null");
         }
         if (timestring == null) {
             throw new Exception("String timestring cannot be null");
         }
         //check for null keys or null values
         if (checkNullElements(data.keySet().toArray())) {
             throw new Exception("There cannot be null keys in the Map data");
         }
         if (checkNullElements(data.values().toArray())) {
             throw new Exception("There cannot be null values in the Map data");
         }
         //checking time
         Date time;
         try {
             time = (new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa")).parse(timestring);
             Calendar c = Calendar.getInstance();
             c.setTime(time);
         } catch (Exception e) {
             throw e;
         }
         double val;
         DataAccessAdapter da = Config.getInstance().getDataAccessFactory().createDataAccess();
         da.beginTransaction();
         for (Entry<String, String> entry : data.entrySet()) {
             try {
                 val = Double.parseDouble(entry.getValue());
             } catch (NumberFormatException e) {
                 throw new Exception("The value provided for the variable " + entry.getKey() + " was not a valid Double");
             }
             AbstractVariableService avs = ServicesFactory.getInstance().getVariableServiceByKey(entry.getKey());
             VariableVo variableVo = avs.getNewVo();
             variableVo.setTime(time);
             variableVo.setValue(val);
             try {
                 avs.createRecord(da, variableVo);
             }catch (DataAccessException ex) {
                 Logger.getLogger(ServiceFacade.class.getName()).log(Level.SEVERE, null, ex);
                 throw ex;
             }catch (IllegalArgumentException ex) {
                 Logger.getLogger(ServiceFacade.class.getName()).log(Level.SEVERE, null, ex);
                 throw ex;
             }
         }
         da.commit();
         da.close();
         return true;
     }
 
     private boolean checkNullElements(Object[] list) {
         for (Object element : list) {
             if (element == null) {
                 return true;
             }
         }
         return false;
     }
 
     public HashMap<String, VariableVo> getLastValues() {
         HashMap<String, VariableVo> lastValues = new HashMap<String, VariableVo>();
         try {
             DataAccessAdapter da = Config.getInstance().getDataAccessFactory().createDataAccess();
             for (Entry<String, AbstractVariableService> e : ServicesFactory.getInstance().getVariablesServicesMap().entrySet()) {
                 lastValues.put(e.getKey(), e.getValue().getLastValue(da));
             }
         } catch (DataAccessException ex) {
             Logger.getLogger(ServiceFacade.class.getName()).log(Level.SEVERE, null, ex);
         }
         return lastValues;
     }
 
     public List<SortedMap<Date, Double>> getData(String variable, String forecast) {
         List<SortedMap<Date, Double>> data = new ArrayList<SortedMap<Date, Double>>();
         try {
             DataAccessAdapter da = Config.getInstance().getDataAccessFactory().createDataAccess();
            data = ServicesFactory.getInstance().getVariableServiceByKey(variable).getDataAndPrediction(da);
         } catch (DataAccessException ex) {
             Logger.getLogger(ServiceFacade.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoSuchElementException ex) {
             Logger.getLogger(ServiceFacade.class.getName()).log(Level.SEVERE, null, ex);
         }
         return data;
     }
 
     public Map<String, Double> getReport(String measure) {
 
         Map<String, Double> Values = new HashMap<String, Double>(); // Will store the value returned by measure.calculate() for each variable in the list
         HashMap<String, AbstractVariableService> VariablesList = ServicesFactory.getInstance().getVariablesServicesMap(); // Stores the list of variables returned by the ServicesFactory
         double result; // Will store the result returned by measure.calculate()
         /*
          * Go through the VariableList, getting all the values of each Dao, Then
          * calculate the measure and add it to the map
          */
         for (String i : VariablesList.keySet()) {
             try {
                 DataAccessAdapter da = Config.getInstance().getDataAccessFactory().createDataAccess();
                 VariablesList.get(i).setMeasure(MeasuresFactory.getInstance().getMeasure(measure));
                 result = VariablesList.get(i).getReport(da);
                 Values.put(i, result);
             } catch (DataAccessException ex) {
                 Logger.getLogger(ServiceFacade.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         return Values;
 
     }
 }
