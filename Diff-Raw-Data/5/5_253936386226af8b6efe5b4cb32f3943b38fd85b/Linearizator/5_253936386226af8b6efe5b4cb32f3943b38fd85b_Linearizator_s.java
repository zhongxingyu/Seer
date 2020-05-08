 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: Linearizator.java,v 1.6 2005-01-24 16:53:02 aracic Exp $
  */
 
 package org.caesarj.mixer;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * C3 Linearization Algorithm (&)
  * 
  * @author Ivica Aracic
  */
 public class Linearizator {
     private static Linearizator singleton = new Linearizator();
    
     public static Linearizator instance() {
         return singleton;
     }
     
     private Linearizator() {
     }
     
     public List mixFromLeftToRight(List[] mixinLists) throws MixerException {
         if(mixinLists.length < 1)
             throw new MixerException("mixinLists array is empty");
         
         List merged = mixinLists[0];
         for (int i = 1; i < mixinLists.length; i++) {
             merged = mix(merged, mixinLists[i]);
         }
         
         return merged;
     }
     
     public List mixFromRightToLeft(List[] mixinLists) throws MixerException {
         if(mixinLists.length < 1)
             throw new MixerException("mixinLists array is empty");
         
         List merged = mixinLists[mixinLists.length-1];
         for (int i = mixinLists.length-2; i >= 0; i++) {
             merged = mix(mixinLists[i], merged);
         }
         
         return merged;
     }
     
     public List mix(List mixinList1, List mixinList2) throws MixerException {
         List res = new LinkedList();
         
         int i1 = mixinList1.size() - 1;
         int i2 = mixinList2.size() - 1;
     
         while(i1>=0 && i2>=0) {
             
             Object a = mixinList1.get(i1);
             Object b = mixinList2.get(i2);
                         
             if(!mixinList1.contains(b)) {
                 res.add(0, b);
                 i2--;
             }
             else if(mixinList1.contains(b) && !mixinList2.contains(a)) {
                 res.add(0, a);
                 i1--;
             }
             else if(a.equals(b)) {
                 res.add(0, a);
                 i1--;
                 i2--;
             }
             else {
                throw new MixerException("bad merge");
             }
         }
         
         while(i1 >= 0)
             res.add(0, mixinList1.get(i1--));
 
         while(i2 >= 0)
             res.add(0, mixinList2.get(i2--));        
         
         return res;
     }
 }
 
