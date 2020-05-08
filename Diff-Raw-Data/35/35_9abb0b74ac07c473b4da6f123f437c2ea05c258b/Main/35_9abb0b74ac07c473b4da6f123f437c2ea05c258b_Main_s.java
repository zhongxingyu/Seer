 /**
  * Floor Plan Marker Project
  * Copyright (C) 2013  Vy Thuy Nguyen
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  * 
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 
 package main;
 
 import gui.view.MainFrame;
 import java.util.*;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Jan 10, 2013
  * Last modified:       
  */
 public class Main 
 {
     public static void main (String[] args)
     {
         MainFrame window = new MainFrame();
         
//        PriorityQueue<Collection<Integer>> q = new PriorityQueue(2, new Comparator() {
//
//            @Override
//            public int compare(Object o1, Object o2)
//            {
//                List<Integer> list1 = (List<Integer>)o1;
//                List<Integer> list2 = (List<Integer>)o2;
//                return (list1.size() > list2.size()
//                        ? -1
//                        : (list1.size() == list2.size() ? 0 : 1));
//            }
//        });
//        
//        
//        List<Integer> l1 = new ArrayList<>();
//        l1.add(3);
//        l1.add(2);
//        
//        List<Integer> l2 = new ArrayList<>();
//        l2.add(3);
//        l2.add(2);
//        l2.add(4);
//        
//        List<Integer> l3 = new ArrayList<>();
//        l3.add(3);
//        
//        
//        q.add(l1);
//        q.add(l2);
//        q.add(l3);
//        
//        for (Collection<Integer> l : q)
//            System.out.println("Q's size = " + l.size());
//        
//        System.out.println("popped: " + q.remove().size());
     }
 }
