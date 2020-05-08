 /*
  * This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  * 
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package taberystwyth;
 
 import javax.swing.*;
 
 import taberystwyth.controller.DebugMenuListener;
 import taberystwyth.view.OverviewFrame;
 
 public class Main {
     
     public static void main(String[] args) {
         try {
             UIManager
                     .setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             Class.forName("taberystwyth.db.SQLConnection");
            Class.forName("taberystwyth.prelim.Allocator");
             Class.forName("taberystwyth.view.OverviewFrame");
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         /*
          * If the debug argument is passed, add the debug menu
          */
         if (args.length > 0) {
             if (args[0].equals("--debug")) {
                 System.out.println("Entering Debug Mode");
                 
                 JMenu debugMenu = new JMenu("Debug");
                 debugMenu.addActionListener(new DebugMenuListener());
                 debugMenu.add(new JMenuItem("Load Example Data"));
                 
                 OverviewFrame.getInstance().getMenu().add(debugMenu);
             }
         }
     }
     
 }
