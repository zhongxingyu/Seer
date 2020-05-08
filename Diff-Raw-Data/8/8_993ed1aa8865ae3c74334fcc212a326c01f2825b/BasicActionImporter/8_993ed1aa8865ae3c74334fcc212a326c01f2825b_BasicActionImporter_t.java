 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2013, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package io.narayana.txmsc;
 
 import javax.transaction.xa.XAException;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author paul.robinson@redhat.com 07/08/2013
  */
 public class BasicActionImporter {
 
     private static Map<Integer, SubordinateBasicAction> transactions = new ConcurrentHashMap<Integer, SubordinateBasicAction>();
 
     private static BasicActionImporter instance;
 
     public static BasicActionImporter getInstance() {
         if (instance == null) {
             instance = new BasicActionImporter();
         }
         return instance;
     }
 
     public SubordinateBasicAction importTransaction(Integer id)
             throws XAException {
 
         if (id == null)
             throw new IllegalArgumentException();
 
         SubordinateBasicAction imported = getImportedTransaction(id);
 
         if (imported == null) {
             imported = new SubordinateBasicAction(id);
 
             transactions.put(id, imported);
         }
 
        //todo right place to begin?
        imported.Begin(null);
         return imported;
     }
 
     public SubordinateBasicAction getImportedTransaction(Integer id)
             throws XAException {
 
         if (id == null)
             throw new IllegalArgumentException();
 
         SubordinateBasicAction tx = transactions.get(id);
 
         if (tx == null)
             return null;
 
         //todo: consider https://issues.jboss.org/browse/JBTM-927
 
         //todo: consider this...
            /*if (!tx.activated())
    		{
    			tx.recover();
 
    			return tx;
    		}
    		else
    			return tx;*/
 
         return tx;
     }
 
     public void removeImportedTransaction(Integer id) throws XAException {
 
         if (id == null)
             throw new IllegalArgumentException();
 
         transactions.remove(id);
     }
 
 }
