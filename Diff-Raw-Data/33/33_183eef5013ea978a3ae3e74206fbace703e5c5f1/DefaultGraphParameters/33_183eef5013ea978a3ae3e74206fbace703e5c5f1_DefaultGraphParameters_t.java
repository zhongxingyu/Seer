 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.otto.source.config;
 
 import com.github.dbourdette.otto.data.DataTablePeriod;
 import com.google.code.morphia.annotations.Property;
 
 /**
  * Default values for graph form for a given source.
  *
  * @author damien bourdette
  * @version \$Revision$
  */
 public class DefaultGraphParameters {
     @Property
    public DataTablePeriod period = DataTablePeriod.RECENT;
 
     public DataTablePeriod getPeriod() {
         return period;
     }
 
     public void setPeriod(DataTablePeriod period) {
         this.period = period;
     }
 }
