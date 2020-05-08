 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.samcrow.colonydata.experiments;
 
 import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Transient;
 
 /**
  * An experiment in which events with marked ants were recorded
  * @author Sam Crow
  */
@Entity
 public class MarkedAntRecordingExperiment extends Experiment {
 
    @Transient
     @Override
     public List<? extends ExperimentResult> getResults() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void setResults(List<? extends ExperimentResult> newResults) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
