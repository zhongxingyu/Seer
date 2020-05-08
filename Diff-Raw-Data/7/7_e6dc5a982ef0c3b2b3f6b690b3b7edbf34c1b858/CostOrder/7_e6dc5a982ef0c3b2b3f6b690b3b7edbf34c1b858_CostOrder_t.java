 /*
  * To change this template, choose Tools | Templates and open the template in the editor.
  */
 package orders;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import ru.peppers.R;
 
 import model.Order;
 import android.content.Context;
 
 /**
  *
  * @author papas
  */
 public class CostOrder extends Order {
 
     private Date _registrationtime;
     private Date _departuretime;
 
     public CostOrder(Context context,String index, Integer nominalcost, Date registrationtime, String addressdeparture, Integer carClass, String comment, String addressarrival,Integer paymenttype,Date departuretime) {
         super(context,nominalcost,addressdeparture, carClass, comment, addressarrival,paymenttype,index);
         _registrationtime = registrationtime;
         _departuretime  = departuretime;
     }
 
     public String toString() {
     	String pred = "";
     	if(_departuretime!=null)
     		pred = "П "+getTimeString(_departuretime)+", ";
     	else pred = getTimeString(_registrationtime)+", ";
     		
     	String over = "";
     	if(_nominalcost!=null)
     		over = ", "+_nominalcost+" "+_context.getString(R.string.currency);
         return pred + _addressdeparture + over;
     }
 
     public ArrayList<String> toArrayList() {
         ArrayList<String> array = new ArrayList<String>();
         array.addAll(getAbonentArray());
         if(_departuretime!=null)
         array.add(_context.getString(R.string.accepted)+" "+getTimeString(_departuretime));
         array.add(_context.getString(R.string.date) + " " + getTimeString(_registrationtime));
         
         array.add(_context.getString(R.string.adress) + " " + _addressdeparture);
         array.add(_context.getString(R.string.where) + " " + _addressarrival);
         
         array.add(_context.getString(R.string.car_class) + " " + getCarClass());
         array.add(_context.getString(R.string.cost_type) + " " + getPayment());
         if(_nominalcost!=null)
         array.add(_context.getString(R.string.cost_ride) + " " + _nominalcost + " "
                 + _context.getString(R.string.currency));
        if(_comment!=null)
         array.add(_comment);
         return array;
     }
 
 	
 	
     private String getTimeString(Date date) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
         return dateFormat.format(date);
     }
 }
