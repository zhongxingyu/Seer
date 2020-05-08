 package org.faur.model;
 
 import org.faur.api.model.IPlace;
 
 /**
  * (C) Copyright 2013 Faur Ioan-Aurel.
  * <p/>
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * <p/>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: faur
  * Date: 10/19/13
  * Time: 5:35 PM
  */
 public class Place implements IPlace {
     private int tokens;
     private String name;
 
     /**
      * Creates an empty place.
      * @param name a unique name for the place.
      */
     public Place(String name) {
         this(name, 0);
     }
 
     /**
      * Creates a place with the specified number of tokens.
      * @param name a unique name for the place.
      * @param numberOfTokens the number of tokens this place initially contains.
      */
     public Place(String name, int numberOfTokens) {
         this.name = name;
         this.tokens = numberOfTokens;
     }
     @Override
     public String getName() {
         return this.name;
     }
 
     @Override
    public void setName() {
         this.name = name;
     }
 
     public int getNrOfTokens() {
         return this.tokens;
     }
 
     @Override
     public void setNrOfTokens(int tokens) {
         this.tokens = tokens;
     }
 
     @Override
     public boolean removeTokens(int nrOfTokens) {
         if (this.tokens >= nrOfTokens) {
             this.tokens -= nrOfTokens;
             return true;
         }
         return false;
     }
 
     @Override
     public void addTokens(int nrOfTokens) {
         this.tokens += nrOfTokens;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(this.name).append("( ").append(String.valueOf(this.tokens)).append(" )");
         return builder.toString();
     }
 }
