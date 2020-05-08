 /*
  * Copyright (c) 2013 Philippe VIENNE
  *
  * This file is a part of SpeleoGraph
  *
  * SpeleoGraph is free software: you can redistribute
  * it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * SpeleoGraph is distributed in the hope that it will
  * be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with SpeleoGraph.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.cds06.speleograph.graph;
 
 import org.apache.commons.lang3.Validate;
 import org.cds06.speleograph.GraphPanel;
 import org.jetbrains.annotations.NotNull;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Create a Dialog to personalize a {@link org.jfree.chart.JFreeChart}.
  * <p>The user can edit axes, background colors, titles ...</p>
  */
 public class GraphEditor extends JDialog {
 
     /**
      * Attached graph panel where put user choices.
      */
     private final GraphPanel graphPanel;
 
     /**
      * Creates a modal dialog by specifying the attached {@link GraphPanel}.
      * <p>Please look at {@link javax.swing.JDialog#JDialog()} to see defaults params applied to this Dialog.</p>
      */
     public GraphEditor(GraphPanel panel) {
         super((Frame) SwingUtilities.windowForComponent(panel), true);
         Validate.notNull(panel);
         this.graphPanel = panel;
        JPanel configuration = new JPanel(new GridBagLayout());
        JPanel dateAxis = new JPanel(new GridBagLayout());


     }
 
     /**
      * Getter for the linked {@link GraphPanel}
      *
      * @return A non-null instance of GraphPanel
      */
     @SuppressWarnings("UnusedDeclaration")
     @NotNull
     public GraphPanel getGraphPanel() {
         return graphPanel;
     }
 }
