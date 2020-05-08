 package org.faur.api.model;
 
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
  * Time: 5:37 PM
  */
 public interface IPlace {
     /**
      * Gets place name.
      * @return a string containing the name of the place.
      */
     String getName();
 
     /**
      * Sets the place name.
      */
    void setName(String name);
 
     /**
      * Gets the number of discrete marks that a place may contain.
      * @return a number of discrete marks.
      */
     int getNrOfTokens();
 
     /**
      * Sets the number of discrete marks that a place may contain.
      * @param tokens number of discrete marks.
      */
     void setNrOfTokens(int tokens);
 
     /**
      * Removes the specified number of tokens.
      * @param nrOfTokens tokens to be removed.
      * @return <code>true</code> if the number of tokens could be removed,
      * <code>false</code> otherwise.
      */
     boolean removeTokens(int nrOfTokens);
 
     /**
      * Adds the specified number of tokens.
      * @param nrOfTokens tokens to be added.
      */
     void addTokens(int nrOfTokens);
 }
