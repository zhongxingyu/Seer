 package br.unb.tr2.harmonic.entity;
 
 import java.io.Serializable;
 
 /**
  * Copyright (C) 2013 Loop EC - All Rights Reserved
  * Created by sandoval for harmonic-server
  */
 public class CalculationInterval implements Serializable {
 
     private static final long serialVersionUID = 236271456999483720L;
 
     private Long start;
 
     private Long end;
 
     private Double result = null;
 
     private Long executionTime = null;
 
     private Long sent = null;
 
     public CalculationInterval(Long start, Long end) {
         this.start = start;
         this.end = end;
     }
 
     public void calculate() {
         Long start = System.currentTimeMillis();
         result = 0d;
        for (Long i = start; i <= end; i++)
            result += 1d / i;
         executionTime = System.currentTimeMillis() - start;
     }
 
     public Long getStart() {
         return start;
     }
 
     public Long getEnd() {
         return end;
     }
 
     public Double getResult() {
         return result;
     }
 
     public Long getExecutionTime() {
         return executionTime;
     }
 
     public Long getSent() {
         return sent;
     }
 
     public void setSent(Long sent) {
         this.sent = sent;
     }
 
     public void wasSent(Long sent) {
         this.sent = sent;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         CalculationInterval that = (CalculationInterval) o;
 
         if (end != null ? !end.equals(that.end) : that.end != null) return false;
         if (executionTime != null ? !executionTime.equals(that.executionTime) : that.executionTime != null)
             return false;
         if (result != null ? !result.equals(that.result) : that.result != null) return false;
         if (start != null ? !start.equals(that.start) : that.start != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result1 = start != null ? start.hashCode() : 0;
         result1 = 31 * result1 + (end != null ? end.hashCode() : 0);
         result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
         result1 = 31 * result1 + (executionTime != null ? executionTime.hashCode() : 0);
         return result1;
     }
 }
