 package org.pillarone.riskanalytics.core.packets;
 
import org.pillarone.riskanalytics.core.packets.Packet;

 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class SingleValuePacket extends Packet {
     public double value;
 
     public SingleValuePacket() {
     }
 
     public SingleValuePacket(double value) {
         this.value = value;
     }
 
     public SingleValuePacket(SingleValuePacket original) {
         super(original);
         this.setValue(original.value);
     }
 
     public double getValue() {
         return value;
     }
 
     public void setValue(double value) {
         this.value = value;
     }
 
     public String toString() {
         return String.valueOf(value);
     }
 
     @Override
     public Map<String, Number> getValuesToSave() throws IllegalAccessException {
         Map<String, Number> map = new HashMap<String, Number>();
         map.put(getValueLabel(), value);
         return map;
     }
 
     public String getValueLabel() {
         return "value";
     }
 }
