 /* ==========================================================================
  * Copyright 2002-2005 Cyclops Group Community
  * 
  * Licensed under the Open Software License, Version 2.1 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://opensource.org/licenses/osl-2.1.php
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * =========================================================================
  */
 package com.cyclopsgroup.tornado.hibernate;
 
 import com.cyclopsgroup.waterview.RuntimeData;
 import com.cyclopsgroup.waterview.spi.PipelineContext;
 import com.cyclopsgroup.waterview.spi.Valve;
 
 /**
  * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
  * 
  * Valve to deal with hibernate sessions
  */
 public class HibernateTransactionValve
     implements Valve
 {
     /**
      * Overwrite or implement method invoke()
      *
      * @see com.cyclopsgroup.waterview.spi.Valve#invoke(com.cyclopsgroup.waterview.RuntimeData, com.cyclopsgroup.waterview.spi.PipelineContext)
      */
     public void invoke( RuntimeData data, PipelineContext context )
         throws Exception
     {
         HibernateService hibernate = (HibernateService) data.getServiceManager().lookup( HibernateService.ROLE );
         try
         {
             context.invokeNextValve( data );
             hibernate.commitTransactions();
         }
         catch ( Exception e )
         {
             hibernate.rollbackTransactions();
            throw e;
         }
         finally
         {
             hibernate.closeSessions();
         }
     }
 }
