 /*
  * Copyright 2012 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.jamgotchian.jabat.context;
 
 import fr.jamgotchian.jabat.job.Step;
 import fr.jamgotchian.jabat.repository.JabatStepExecution;
 import java.io.Externalizable;
 import java.util.Properties;
 import javax.batch.runtime.Metric;
 import javax.batch.runtime.context.StepContext;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class JabatStepContext<T, P extends Externalizable> extends JabatBatchContext<Step, T>
                                                            implements StepContext<T, P> {
 
     private final JabatStepExecution stepExecution;
 
     private Properties properties;
 
     private T transientUserData;
 
     public JabatStepContext(Step step, JabatStepExecution stepExecution) {
         super(step);
         this.stepExecution = stepExecution;
     }
 
     @Override
     public long getStepExecutionId() {
         return stepExecution.getId();
     }
 
     @Override
     public Properties getProperties() {
         return properties;
     }
 
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 
     @Override
     public T getTransientUserData() {
         return transientUserData;
     }
 
     @Override
     public void setTransientUserData(T data) {
         this.transientUserData = data;
     }
 
     @Override
     public P getPersistentUserData() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setPersistentUserData(P data) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getBatchStatus() {
         return stepExecution.getStatus();
     }
 
     @Override
     public String getExitStatus() {
         return stepExecution.getExitStatus();
     }
 
     @Override
     public void setExitStatus(String status) {
         stepExecution.setExitStatus(status);
     }
 
     @Override
     public Exception getException() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Metric[] getMetrics() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
