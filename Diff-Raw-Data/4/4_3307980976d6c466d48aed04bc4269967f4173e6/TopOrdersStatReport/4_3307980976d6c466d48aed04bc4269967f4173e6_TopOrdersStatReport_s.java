 /*
  * investovator, Stock Market Gaming framework
  * Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.agentsimulation.multiasset.report.statistics;
 
 import net.sourceforge.jabm.event.SimEvent;
 import net.sourceforge.jabm.util.Resetable;
 import net.sourceforge.jasa.event.OrderPlacedEvent;
 import net.sourceforge.jasa.market.Order;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * @author rajith
  * @version ${Revision}
  */
 public class TopOrdersStatReport implements MultiStatReport, Serializable, Cloneable, Resetable {
 
     protected List<Number> topBuy;
     protected List<Number> topSell;
 
     @Override
     public void eventOccurred(SimEvent event) {
         if(event instanceof OrderPlacedEvent){
             Order order = ((OrderPlacedEvent) event).getOrder();
             if (order.isBid()) {
                 addToList(topSell, order.getPrice());
             } else {
                 addToList(topBuy, order.getPrice());
             }
         }
     }
 
     @Override
     public void initialise() {
         topBuy = new ArrayList<Number>() {
             public boolean add(Number order) {
                 super.add(order);
                 Collections.sort(topBuy, Collections.reverseOrder());
                 return true;
             }
         };
 
         topSell = new ArrayList<Number>() {
             public boolean add(Number order) {
                 super.add(order);
                 Collections.sort(topSell, Collections.reverseOrder());
                 return true;
             }
         };
     }
 
     @Override
     public Map<Object, Number> getVariableBindings() {
         return null;  //Do nothing
     }
 
     @Override
     public Map<String, List<Number>> getStatValues() {
         Map<String, List<Number>> values = new HashMap<>();
         values.put("BUY", topBuy);
         values.put("SELL", topSell);
         return values;
     }
 
     @Override
     public String getName() {
         return "top order report";
     }
 
     @Override
     public void reset() {
        initialise();
     }
 
     protected List<Number> addToList(List<Number> list, Number order) {
         list.add(order);
         Collections.sort(list, Collections.reverseOrder());
         return list.size() > 4 ?  list.subList(0, 5) : list;
     }
 }
