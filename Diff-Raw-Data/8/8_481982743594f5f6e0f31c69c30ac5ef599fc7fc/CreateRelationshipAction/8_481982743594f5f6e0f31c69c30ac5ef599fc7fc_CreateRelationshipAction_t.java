 /*
  * Copyright (c) 2012 Eike Kettner
  *
  * This file is part of NeoSwing.
  *
  * NeoSwing is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * NeoSwing is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with NeoSwing.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.eknet.neoswing.actions;
 
 import edu.uci.ics.jung.graph.Graph;
 import org.eknet.neoswing.utils.Dialog;
 import org.eknet.neoswing.utils.NeoSwingUtil;
 import org.eknet.neoswing.view.SelectRelationshipTypePanel;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 
 import java.awt.event.ActionEvent;
 
 /**
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 12.01.12 14:56
  */
 public class CreateRelationshipAction extends AbstractSwingAction {
 
   private final Graph<Node, Relationship> graph;
   private Node node;
   private Node other;
   private Direction direction;
 
   public CreateRelationshipAction(@NotNull Graph<Node, Relationship> graph, @Nullable Node node, @Nullable Direction direction) {
     this.node = node;
     this.direction = direction;
     this.graph = graph;
     setNode(node);
     setDirection(direction);
     updateName();
 
     putValue(SMALL_ICON, NeoSwingUtil.icon("connect"));
     putValue(SHORT_DESCRIPTION, "Create a new relationship");
 
   }
   
   private void updateName() {
     if (direction == Direction.INCOMING) {
       if (this.other == null) {
         putValue(NAME, "Create Relationship to this");
       } else {
         putValue(NAME, "Create Relationship from picked Node to this");
       }
     }
     if (direction == Direction.OUTGOING) {
       if (this.other == null) {
         putValue(NAME, "Create Relationship from this");
       } else {
         putValue(NAME, "Create Relationship from this to picked Node");
       }
     }
   }
 
   public CreateRelationshipAction(Graph<Node, Relationship> graph) {
     this(graph, null, null);
   }
 
   public Node getNode() {
     return node;
   }
 
   public void setNode(Node node) {
     this.node = node;
     setEnabled(this.node != null && this.direction != null);
   }
 
   public Direction getDirection() {
     return direction;
   }
 
   public void setDirection(Direction direction) {
     if (direction == Direction.BOTH) {
       throw new IllegalArgumentException(direction + " not supported");
     }
     this.direction = direction;
     setEnabled(this.node != null && this.direction != null);
    updateName();
   }
 
   public Node getOther() {
     return other;
   }
 
   public void setOther(Node other) {
     this.other = other;
     updateName();
   }
 
   @Override
   public void actionPerformed(ActionEvent e) {
     if (node == null || direction == null) {
       return;
     }
     GraphDatabaseService db = node.getGraphDatabase();
     Dialog dialog = new Dialog("Relationship Type");
     SelectRelationshipTypePanel selectPanel = new SelectRelationshipTypePanel(db, NeoSwingUtil.getFactory(true));
     dialog.setContent(selectPanel);
     Dialog.Option option = dialog.show(getWindow(e), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
     if (option != Dialog.Option.OK) {
       return;
     }
     RelationshipType type = selectPanel.getType();
     if (type == null) {
       return;
     }
     Transaction tx = db.beginTx();
     try {
       if (other == null) {
         other = db.createNode();
       }
 
       Relationship relationship0 = null;
       Relationship relationship1 = null;
       if (direction == Direction.INCOMING) {
         relationship0 = other.createRelationshipTo(node, type);
       }
       if (direction == Direction.OUTGOING) {
         relationship1 = node.createRelationshipTo(other, type);
       }
       tx.success();
       if (relationship0 != null) {
         NeoSwingUtil.addEdge(graph, relationship0);
       }
       if (relationship1 != null) {
         NeoSwingUtil.addEdge(graph, relationship1);
       }
     } finally {
       tx.finish();
     }
 
 
   }
 }
