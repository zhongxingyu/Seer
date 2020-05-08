 /*
  *	File: @(#)DimSortComparator.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Sep 10, 2005  		By: jim
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
  Date			Author			Version			Changes
  xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.data;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.pace.base.SortOrder;
 
 /**
  * This sorts a collection of Intersection objects
  * based upon the order of the members passed in
  * and the axis the members belong to.
  *
  * @version	x.xx
  * @author jim
  *
  */
 public class DimSortComparator implements Comparator<Intersection> {
     
     private Map<String, Map<String, Integer>> memberSeqs;
     private String[] axisSequence;
     private SortOrder sortOrder;
     
     public DimSortComparator(Map <String, Map<String, Integer>>memberSequences, String[] axisSequence, SortOrder sortOrder) {
         memberSeqs = memberSequences;
         this.axisSequence = axisSequence;
         this.sortOrder = sortOrder;
     }
     
     public int compare2(Intersection o1, Intersection o2) { return 1; }
     
     public int compare(Intersection i1, Intersection i2) {
            
         // sorts like an alphabetical order. If 1st axis determines priority consider sorted,
         // else continue until tie is resolved.
         
     	
         Map <String, Integer> axisSeq;
         String axis, o1Coord, o2Coord;
         int axisVal1, axisVal2;
         for (int i = 0; i < axisSequence.length; i++) {
             axis = axisSequence[i];
             axisSeq = memberSeqs.get(axis);
             o1Coord = i1.getCoordinate(axis);
             o2Coord = i2.getCoordinate(axis);
 
 
             
             // Allow for this comparator to use the same axis sequence for both attribute
             // and non-attribute intersections during attribute evaluation. (TTN-1506) 
             if (o1Coord == null || o2Coord == null) {
             	// Just skip to the next axis, if one or both intersections don't contain
             	// the current axis. Hopefully it's both, since the intent is to compare
             	// like intersections to each other.
             	continue;
             }
 
            
             axisVal1 = axisSeq.get(o1Coord);
             axisVal2 = axisSeq.get(o2Coord);
             
             if (sortOrder == SortOrder.Ascending) {
 //              	 if (true) return 1;             	
                if (axisVal1 < axisVal2) return 1;
                else if (axisVal1 > axisVal2) return -1;         
             }
             else {
                if (axisVal1 < axisVal2) return -1;
                else if (axisVal1 > axisVal2) return 1;      
             }
         }
         
         // assumes all tests resulted in tie so must be equal.
 //        if (!o1.equals(o2))
 //            throw new ArithmeticException("Failure in intersection sorting algorthithm.\n" + "\nI1: " + i1.toString() + " -- I2: " + i2.toString() );
         
         return 0;
         
     }
 }
