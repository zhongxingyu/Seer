 package org.meteorologaaguascalientes.control.report;
 
import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import org.meteorologaaguascalientes.control.measure.Measure;
 import org.meteorologaaguascalientes.dao.AbstractVariableDao;
 import org.meteorologaaguascalientes.dao.DaoList;
 import org.meteorologaaguascalientes.model.Variable;
 
 /**
  *
  * @author Diego Gerena (SNIPERCAT) <dagerenaq@gmail.com>
  */
 public class ReportsCotrol {
     
     public Map<AbstractVariableDao,Double> getReport(Measure measure){
         
         Map Values = new HashMap(); // Will store the value returned by measure.calculate() for each variable in the list
         List<AbstractVariableDao> VariablesList = DaoList.getVariables(); // Will store the list of variables returned by the DaoList
        ArrayList<Variable> DaoData; // Will store the data returned by each Dao with the method getAllValues();
         double result; // Will store the result returned by measure.calculate()
         
         
         /* Go through the VariableList, getting all the values of each Dao, 
          * Then calculate the measure
          * and add it to the map
          */
         for(int i = 0 ;i< VariablesList.size() ;i++){
            DaoData = (ArrayList<Variable>) VariablesList.get(i).getAllValues();
             result = measure.calculate(DaoData);
             Values.put(VariablesList.get(i).getVisibleName(), result);
         }
         
         return Values;
         
     }
     
 }
