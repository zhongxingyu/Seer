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
 
 package org.eknet.neoswing.view.control;
 
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import org.eknet.neoswing.ComponentFactory;
 import org.eknet.neoswing.GraphModel;
 import org.eknet.neoswing.actions.CollapseNodeAction;
 import org.eknet.neoswing.actions.CreateRelationshipAction;
 import org.eknet.neoswing.actions.DeleteElementAction;
 import org.eknet.neoswing.actions.EditPropertyAction;
 import org.eknet.neoswing.actions.ExpandNodeAction;
 import org.eknet.neoswing.actions.HideElementAction;
 import org.eknet.neoswing.actions.RemoveDefaultLabelAction;
 import org.eknet.neoswing.utils.NeoSwingUtil;
 import org.jetbrains.annotations.NotNull;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 import javax.swing.Action;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 10.01.12 19:22
  */
 public class NodePopupMenu extends AbstractMousePlugin {
 
   private final ComponentFactory factory;
 
   public NodePopupMenu(int modifiers, ComponentFactory factory) {
     super(modifiers);
     this.factory = factory;
   }
 
   public NodePopupMenu(int modifiers) {
     super(modifiers);
     this.factory = NeoSwingUtil.getFactory(true);
   }
 
   @Override
   public void mousePressed(MouseEvent e) {
     if (e.getButton() != modifiers || !e.isPopupTrigger()) {
       return;
     }
     VisualizationViewer<Node, Relationship> viewer = getViewer(e);
 
     down = e.getPoint();
     Node node = viewer.getPickSupport().getVertex(viewer.getGraphLayout(), down.getX(), down.getY());
     if (node != null) {
       JPopupMenu menu = getPopup(node, viewer);
       menu.show(viewer, down.x, down.y);
     }
     Relationship relationship = viewer.getPickSupport().getEdge(viewer.getGraphLayout(), down.getX(), down.getY());
     if (relationship != null) {
       JPopupMenu menu = getPopup(relationship, viewer);
       menu.show(viewer, down.x, down.y);
     }
   }
   
   protected JPopupMenu getPopup(@NotNull Node node, @NotNull VisualizationViewer<Node, Relationship> viewer) {
     JPopupMenu menu = factory.createPopupMenu();
     Window owner = NeoSwingUtil.findOwner(viewer);
 
     JLabel label = new JLabel("Node Actions");
     label.setFont(label.getFont().deriveFont(Font.BOLD));
     label.setHorizontalTextPosition(SwingConstants.CENTER);
     menu.add(label);
     
     menu.addSeparator();
     GraphModel graphModel = NeoSwingUtil.getGraphModel(viewer);
     factory.addMenuItemGroup(menu, getAllExpandActions(node, graphModel));
     
     menu.add(new JPopupMenu.Separator());
     factory.addMenuItemGroup(menu, getAllCollapseActions(node, graphModel));
     menu.add(new JMenuItem(new HideElementAction(graphModel, node)));
 
     menu.addSeparator();
     List<Action> createRelationshipActions = new ArrayList<Action>();
     CreateRelationshipAction createRel = new CreateRelationshipAction(graphModel.getGraph(), node, Direction.INCOMING);
     createRel.setWindow(owner);
     createRelationshipActions.add(createRel);
     
     createRel = new CreateRelationshipAction(graphModel.getGraph(), node, Direction.OUTGOING);
     createRel.setWindow(owner);
     createRelationshipActions.add(createRel);
     
     Set<Node> picked = viewer.getPickedVertexState().getPicked();
     if (picked.size() == 1) {
       Node other = new ArrayList<Node>(picked).get(0);
       createRel = new CreateRelationshipAction(graphModel.getGraph(), node, Direction.INCOMING);
       createRel.setOther(other);
       createRel.setWindow(owner);
       createRelationshipActions.add(createRel);
 
      createRel = new CreateRelationshipAction(graphModel.getGraph(), node, Direction.INCOMING);
       createRel.setOther(other);
       createRel.setWindow(owner);
       createRelationshipActions.add(createRel);
     }
     factory.addMenuItemGroup(menu, createRelationshipActions);
 
     menu.add(new JPopupMenu.Separator());
     EditPropertyAction editAction = new EditPropertyAction(node);
     editAction.setWindow(owner);
     menu.add(new JMenuItem(editAction));
     
     menu.add(new JPopupMenu.Separator());
     DeleteElementAction deleteAction = new DeleteElementAction(node, graphModel);
     deleteAction.setWindow(owner);
     menu.add(new JMenuItem(deleteAction));
 
     menu.addSeparator();
     menu.add(new JMenuItem(new RemoveDefaultLabelAction(node)));
     return menu;
   }
 
   protected JPopupMenu getPopup(Relationship relationship, VisualizationViewer<Node, Relationship> viewer) {
     JPopupMenu menu = factory.createPopupMenu();
     JLabel label = new JLabel("Relationship Actions");
     label.setFont(label.getFont().deriveFont(Font.BOLD));
     label.setHorizontalTextPosition(SwingConstants.CENTER);
     menu.add(label);
     Window owner = NeoSwingUtil.findOwner(viewer);
     GraphModel graphModel = NeoSwingUtil.getGraphModel(viewer);
 
     menu.add(new JPopupMenu.Separator());
     EditPropertyAction editAction = new EditPropertyAction(relationship);
     editAction.setWindow(owner);
     menu.add(new JMenuItem(editAction));
 
     menu.add(new JPopupMenu.Separator());
     DeleteElementAction deleteAction = new DeleteElementAction(relationship, graphModel);
     deleteAction.setWindow(owner);
     menu.add(new JMenuItem(deleteAction));
 
     return menu;
   }
 
   private Iterable<Action> getAllExpandActions(Node node, GraphModel model) {
     List<Action> list = new ArrayList<Action>();
     list.add(new ExpandNodeAction(node, model, Direction.BOTH));
     list.add(new ExpandNodeAction(node, model, Direction.OUTGOING));
     list.add(new ExpandNodeAction(node, model, Direction.INCOMING));
     return list;
   }
 
   private Iterable<Action> getAllCollapseActions(Node node, GraphModel graphModel) {
     List<Action> list = new ArrayList<Action>();
     list.add(new CollapseNodeAction(node, graphModel, Direction.BOTH));
     list.add(new CollapseNodeAction(node, graphModel, Direction.OUTGOING));
     list.add(new CollapseNodeAction(node, graphModel, Direction.INCOMING));
     return list;
   }
 }
