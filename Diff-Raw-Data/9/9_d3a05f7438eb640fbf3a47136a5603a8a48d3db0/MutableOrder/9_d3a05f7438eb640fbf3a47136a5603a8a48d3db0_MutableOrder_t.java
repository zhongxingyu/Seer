 package com.akavrt.csp.solver.sequential;
 
 import com.akavrt.csp.core.Order;
 
 /**
  * <p>Utility wrapper class used to link order with current production.</p>
  *
  * @author Victor Balabanov <akavrt@gmail.com>
  */
 public class MutableOrder {
     private final Order order;
     private double producedLength;
 
     /**
      * <p>Create wrapper for order with mutable field to control produced length of the strip.</p>
      *
      * @param order Order to be linked width production.
      */
     public MutableOrder(Order order) {
         this.order = order;
     }
 
     /**
      * <p>The immutable order linked with production.</p>
      */
     public Order getOrder() {
         return order;
     }
 
     /**
      * <p>Currently produced length of the strip.</p>
      *
      * @return Length of the produced strip, measured in abstract units.
      */
     public double getProducedLength() {
         return producedLength;
     }
 
     /**
      * <p>Change produced length by the value specified.</p>
      *
      * @param length Produced length to be added to previously registered production of strip.
      *               Measured in abstract units.
      */
     public void addProducedLength(double length) {
         this.producedLength += length;
     }
 
     /**
      * <p>Compare current production with demand.</p>
      *
      * @return true if produced length is sufficient, false otherwise.
      */
     public boolean isFulfilled() {
         return producedLength >= order.getLength();
     }
 
     /**
      * <p>Compare current production with demand.</p>
      *
      * @return Length of the missing strip or zero, if order is already fulfilled. Measured in
      *         abstract units.
      */
     public double getUnfulfilledLength() {
        return isFulfilled() ? 0 : (order.getLength() - producedLength);
     }
 
     /**
      * <p>Compare current production with demand.</p>
      *
      * @return Area of the missing strip or zero, if order is already fulfilled.
      *         Measured in abstract square units.
      */
     public double getUnfulfilledArea() {
         return isFulfilled() ? 0 : (order.getLength() - producedLength) * order.getWidth();
     }
 }
