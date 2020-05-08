 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Peak implements Comparable<Peak>{
     private double value;
     private double mass;
     private double intensity;
     private int charge;
     private List<Peak> copies = new ArrayList<Peak>();
 
     private int componentId;
 
     private List<Peak> next = new ArrayList<Peak>();
     private List<Peak> prev = new ArrayList<Peak>();
 
     private int maxPrefix = 0;
 
     private PeakType peakType;
 
     public Peak(double value, double intensity, int charge) {
         this.value = value;
         this.mass = value;
         this.intensity = intensity;
         this.charge = charge;
         peakType = PeakType.B;
     }
 
     public Peak(double value, double mass, double intensity, int charge) {
         this.value = value;
         this.mass = mass;
         this.intensity = intensity;
         this.charge = charge;
         peakType = PeakType.Y;
     }
 
     private Peak yPeak = null;
 
     public Peak getYPeak(double precursorMass){
         if (peakType != PeakType.B) {
             return null;
         }
         if (yPeak == null) {
             yPeak = new Peak(precursorMass - value, value, intensity, charge);
         }
         return yPeak;
     }
 
     public Peak getYPeak() {
         return yPeak;
     }
 
     public PeakType getPeakType() {
         return peakType;
     }
 
     public double getValue() {
         return value;
     }
 
     public double getMass() {
         return mass;
     }
 
     public double getIntensity() {
         return intensity;
     }
 
     public int getCharge() {
         return charge;
     }
 
     public int getMaxPrefix() {
         return maxPrefix;
     }
 
     public void setMaxPrefix(int maxPrefix) {
         this.maxPrefix = maxPrefix;
     }
 
     public int getComponentId() {
         return componentId;
     }
 
     public void setComponentId(int componentId) {
         if (componentId != this.componentId) {
             this.componentId = componentId;
             for (Peak peak : next) {
                 peak.setComponentId(componentId);
             }
         }
     }
 
     public void addNext(Peak peak) {
         next.add(peak);
         if (peak.getComponentId() != componentId) {
             doUpdateComponentId(peak);
         }
     }
 
     public void removeNext(Peak peak) {
         next.remove(peak);
         peak.removePrev(this);
     }
 
     public void removePrev(Peak peak) {
         prev.remove(peak);
     }
 
     public void clearEdges() {
         next.clear();
         prev.clear();
     }
 
     public List<Peak> getNext() {
         return next;
     }
 
     public List<Peak> getPrev() {
         return prev;
     }
 
     public boolean isParent(Peak peak) {
         return next.containsAll(peak.getNext()) && prev.containsAll(peak.getPrev());
     }
 
     public void populatePrev() {
         for (Peak peak : next) {
             peak.addCopy(this);
         }
     }
 
     public void addCopy(Peak peak) {
         copies.add(peak);
         for (Peak p : peak.getNext()) {
             p.removePrev(peak);
         }
         for (Peak p : peak.getPrev()) {
             p.removeNext(peak);
         }
 
     }
 
     private void doUpdateComponentId(Peak peak) {
         int minComponentId = Math.min(componentId, peak.getComponentId());
         setComponentId(minComponentId);
         peak.setComponentId(minComponentId);
     }
 
     public boolean updateComponentId() {
         for (Peak peak : next) {
             if (peak.getComponentId() != componentId) {
                 doUpdateComponentId(peak);
                 return true;
             }
         }
         return false;
     }
 
     public double diff(Peak prev) {
         return value - prev.getValue();
     }
 
     public int compareTo(Peak other) {
         if (other.getValue() < value) {
             return 1;
         } else if (other.getValue() > value) {
             return -1;
         }
         return 0;
     }
 }
