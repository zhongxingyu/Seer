 /*
  *  Freeplane - mind map editor
  *  Copyright (C) 2008 Dimitry Polivaev
  *
  *  This file author is Dimitry Polivaev
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.freeplane.view.swing.map;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Point;
 
 import javax.swing.JComponent;
 
 import org.freeplane.core.ui.components.UITools;
 import org.freeplane.features.cloud.CloudModel;
 import org.freeplane.features.map.NodeModel;
 import org.freeplane.features.mode.Controller;
 import org.freeplane.features.nodelocation.LocationModel;
 import org.freeplane.features.styles.MapStyleModel;
 import org.freeplane.view.swing.map.cloud.CloudView;
 
 abstract public class NodeViewLayoutAdapter implements INodeViewLayout {
     protected static class LayoutData{
             final int[] lx;
             final int[] ly;
             final boolean[] free;
             final boolean[] summary;
             int left;
             int childContentHeight;
             int top;
             boolean rightDataSet;
             boolean leftDataSet;
             public LayoutData(int childCount) {
                 super();
                 this.lx = new int[childCount];
                 this.ly = new int[childCount];
                 this.free = new boolean[childCount];
                 this.summary = new boolean[childCount];
                 this.left = 0;
                 this.childContentHeight = 0;
                 this.top = 0;
                 rightDataSet = false;
                 leftDataSet = false;
             }
         }
 
     private static Dimension minDimension;
     private int childCount;
     private JComponent content;
     protected final int LISTENER_VIEW_WIDTH = 10;
     protected Point location = new Point();
     private NodeModel model;
     private int spaceAround;
     private int vGap;
     private NodeView view;
     private int contentWidth;
     private int contentHeight;
 
     public void addLayoutComponent(final String arg0, final Component arg1) {
     }
     /**
      * @return Returns the childCount.
      */
     protected int getChildCount() {
         return childCount;
     }
 
     /**
      * @return Returns the content.
      */
     protected JComponent getContent() {
         return content;
     }
 
     /**
      * @return Returns the model.
      */
     protected NodeModel getModel() {
         return model;
     }
 
     /**
      * @return Returns the spaceAround.
      */
     int getSpaceAround() {
         return spaceAround;
     }
 
     /**
      * @return Returns the vGap.
      */
     int getVGap() {
         return vGap;
     }
 
     /**
      * @return Returns the view.
      */
     protected NodeView getView() {
         return view;
     }
 
     abstract protected void layout();
 
     public void layoutContainer(final Container c) {
         if(setUp(c)){
         	layout();
         }
     	shutDown();
     }
 
     /*
      * (non-Javadoc)
      * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
      */
     public Dimension minimumLayoutSize(final Container arg0) {
         if (NodeViewLayoutAdapter.minDimension == null) {
             NodeViewLayoutAdapter.minDimension = new Dimension(0, 0);
         }
         return NodeViewLayoutAdapter.minDimension;
     }
 
     /*
      * (non-Javadoc)
      * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
      */
     public Dimension preferredLayoutSize(final Container c) {
         if (!c.isValid()) {
             c.validate();
         }
         return c.getSize();
     }
 
     /*
      * (non-Javadoc)
      * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
      */
     public void removeLayoutComponent(final Component arg0) {
     }
 
     protected boolean setUp(final Container c) {
         final NodeView localView = (NodeView) c;
         JComponent content = localView.getContent();
         if(content == null)
         	return false;
         final int localChildCount = localView.getComponentCount() - 1;
         for (int i = 0; i < localChildCount; i++) {
             final Component component = localView.getComponent(i);
             if (component instanceof NodeView) {
                 ((NodeView) component).validateTree();
             }
             else {
                 component.validate();
             }
         }
         this.content = content;
         view = localView;
         model = localView.getModel();
         childCount = localChildCount;
          if (getModel().isVisible()) {
             setVGap(getView().getVGap());
         }
         else {
             setVGap(getView().getVisibleParentView().getVGap());
         }
         spaceAround = view.getSpaceAround();
 		if (view.isContentVisible()) {
 	        Dimension contentPreferredSize;
             contentPreferredSize = getContent().getPreferredSize();
             final MapStyleModel mapStyleModel = MapStyleModel.getExtension(Controller.getCurrentController().getMap());
            contentWidth = Math.max(view.getZoomed(mapStyleModel.getMinNodeWidth()+ 8),contentPreferredSize.width);
             contentHeight = contentPreferredSize.height;
         }
         else {
         	contentHeight = 0;
         	contentWidth = 0;
         }
 		return true;
     }
 
     protected void shutDown() {
         view = null;
         model = null;
         content = null;
         childCount = 0;
         setVGap(0);
         spaceAround = 0;
     }
 
     public void setVGap(final int vGap) {
         this.vGap = vGap;
     }
 
     /**
      * Calculates the tree height increment because of the clouds.
      */
     public int getAdditionalCloudHeigth(final NodeView node) {
         if (!node.isContentVisible()) {
             return 0;
         }
         final CloudModel cloud = node.getCloudModel();
         if (cloud != null) {
             return CloudView.getAdditionalHeigth(cloud, node);
         }
         else {
             return 0;
         }
     }
 
     protected void calcLayout(final boolean isLeft, final LayoutData data) {
         int highestSummaryLevel = 1;
         int level = 1;
         for (int i = 0; i < getChildCount(); i++) {
             final NodeView child = (NodeView) getView().getComponent(i);
             if (child.isLeft() != isLeft) {
                 continue;
             }
             if(child.isSummary()){
                 level++;
                 highestSummaryLevel = Math.max(highestSummaryLevel, level);
             }
             else{
                 level = 1;
             }
         }
         int left = 0;
         int y = 0;
         
         int childContentHeightSum = 0;
         int visibleChildCounter = 0;
         int summedChildCounter = 0;
         int top = 0;
         
         final int[] groupStart = new int[highestSummaryLevel];
         final int[] groupStartContentHeightSum = new int[highestSummaryLevel];
         final int[] groupStartY = new int[highestSummaryLevel];
         final int[] groupEndY = new int[highestSummaryLevel];
         
         final int summaryBaseX[] = new int[highestSummaryLevel];
         
         level = highestSummaryLevel;
         for (int i = 0; i < getChildCount(); i++) {
             final NodeView child = (NodeView) getView().getComponent(i);
             if (child.isLeft() != isLeft) {
                 continue;
             }
             
             final boolean isSummary = child.isSummary();
             final boolean isItem = !isSummary || visibleChildCounter <= summedChildCounter;
             final int oldLevel = level;
             if(isItem){
                 level = 0;
             }
             else{
                 level++;
             }
                 
             
             final int childCloudHeigth = getAdditionalCloudHeigth(child);
             final int childContentHeight = child.getContent().getHeight();
             final int childShiftY = child.isContentVisible() ? child.getShift() : 0;
             final int childContentShift = child.getContent().getY() - getSpaceAround();
             int childHGap;
             if(child.isContentVisible())
             	childHGap =  child.getHGap(); 
             else if(child.isSummary())
             	childHGap = child.getZoomed(LocationModel.HGAP);
             else
             	childHGap = 0;
             final int childHeight = child.getHeight() - 2 * getSpaceAround();
             
             boolean isFreeNode = child.isFree();
 			data.free[i] = isFreeNode;
 			data.summary[i] = ! isItem;
 			if(isItem && isFreeNode){
 				data.ly[i] = childShiftY - childContentShift - getSpaceAround();
             }
             else if(isItem){
                 if (childShiftY < 0 || visibleChildCounter == 0) {
                     top += childShiftY;
                 }
                 top -= childContentShift;
 
                 y += childCloudHeigth/2;
                 top += child.getTopOverlap();
                 y -= child.getTopOverlap();
                 if (childShiftY < 0) {
                     data.ly[i] = y;
                     y -= childShiftY;
                 }
                 else {
                     if(visibleChildCounter > 0)
                         y += childShiftY;
                     data.ly[i] = y;
                 }
                 if (childHeight != 0) {
                     y += childHeight + getVGap() + childCloudHeigth/2;
                     y -= child.getBottomOverlap();
                 }
                 
                 childContentHeightSum += childContentHeight + childCloudHeigth;
                 if(oldLevel > 0){
                     summaryBaseX[0] = 0;
                     for(int j = 0; j < oldLevel; j++){
                         groupStart[j] = i;
                         groupStartY[j] = Integer.MAX_VALUE;
                         groupEndY[j] = Integer.MIN_VALUE;
                         groupStartContentHeightSum[j] = childContentHeightSum;
                     }
                     summedChildCounter = visibleChildCounter;
                 }
                 else if(child.isFirstGroupNode()){
                     groupStartContentHeightSum[0] = childContentHeightSum;
                     summaryBaseX[0] = 0;
                     groupStart[0] = i;
                 }
                 if (child.getHeight() - 2 * getSpaceAround() != 0) {
                     if (visibleChildCounter > 0)
                         childContentHeightSum +=  getVGap();
                     visibleChildCounter++;
                 }
                 if(isSummary)
                     summedChildCounter = visibleChildCounter;
            }
             else{
                 final int itemLevel = level - 1;
                 if(child.isFirstGroupNode()){
                     groupStartContentHeightSum[level] = groupStartContentHeightSum[itemLevel];
                     summaryBaseX[level] = 0;
                     groupStart[level] = groupStart[itemLevel];
                 }
                 int summaryY = (groupStartY[itemLevel] + groupEndY[itemLevel] ) / 2 - childContentHeight / 2 + childShiftY - (child.getContent().getY() - getSpaceAround());
                 data.ly[i] = summaryY;
                 if(!isFreeNode ){
                 	final int deltaY = summaryY -childCloudHeigth/2 - groupStartY[itemLevel] + child.getTopOverlap();
                 	if(deltaY < 0){
                 		top += deltaY;
                 		y -= deltaY;
                 		summaryY -= deltaY;
                 		for(int j = groupStart[itemLevel]; j <= i; j++){
                 			NodeView groupItem = (NodeView) getView().getComponent(j);
                 			if(groupItem.isLeft() == isLeft && (data.summary[j] || !data.free[j]))
                 				data.ly[j]-=deltaY;
                 		}
                 	}
                 	if (childHeight != 0) {
                 		summaryY += childHeight + getVGap() + childCloudHeigth/2 - child.getBottomOverlap();
                 	}
                 	y = Math.max(y, summaryY);
                 	final int summaryContentHeight = groupStartContentHeightSum[itemLevel] + childContentHeight;
                 	if(childContentHeightSum  < summaryContentHeight){
                 		childContentHeightSum = summaryContentHeight;
                 	}
                 }
             }
 			if(! isItem || ! isFreeNode){
 				if(child.isFirstGroupNode()){
 					groupStartY[level] = data.ly[i] -childCloudHeigth/2 + child.getTopOverlap();
 					groupEndY[level] = data.ly[i] + childHeight+childCloudHeigth/2 - child.getBottomOverlap();
 				}
 				else{
 					groupStartY[level] = Math.min(groupStartY[level],data.ly[i]-childCloudHeigth/2 + child.getTopOverlap());
 					groupEndY[level] = Math.max(data.ly[i] + childHeight+childCloudHeigth/2  - child.getBottomOverlap(), groupEndY[level]);
 				}
 			}
             final int x;
             final int baseX;
             if(level > 0)
                 baseX = summaryBaseX[level - 1];
             else{
                 if(child.isLeft() != (isItem && isFreeNode)){
                     baseX = 0;
                 }
                 else{
                     baseX = contentWidth;
                 }
             }
             if(child.isLeft()){
                 x = baseX - childHGap - child.getContent().getX() - child.getContent().getWidth();
                 summaryBaseX[level] = Math.min(summaryBaseX[level], x + getSpaceAround());
             }
             else{
                 x = baseX + childHGap - child.getContent().getX();
                 summaryBaseX[level] = Math.max(summaryBaseX[level], x + child.getWidth() - getSpaceAround());
             }
             left = Math.min(left, x);
             data.lx[i] = x; 
         }
         
         top += (contentHeight - childContentHeightSum) / 2;
         setData(data, isLeft, left, childContentHeightSum, top);
     }
     
     private void setData(final LayoutData data, boolean isLeft, int left, int childContentHeight, int top) {
         if(!isLeft && data.leftDataSet || isLeft && data.rightDataSet){
             data.left = Math.min(data.left, left);
             data.childContentHeight = Math.max(data.childContentHeight, childContentHeight);
             int deltaTop = top - data.top;
             final boolean changeLeft;
             if(deltaTop < 0){
                 data.top = top;
                 changeLeft = !isLeft;
                 deltaTop = - deltaTop;
             }
             else{
                 changeLeft = isLeft;
             }
             for(int i = 0; i < getChildCount(); i++){
                 NodeView child = (NodeView) getView().getComponent(i);
                 if(child.isLeft() == changeLeft && (data.summary[i] || !data.free[i])){
                     data.ly[i] += deltaTop;
                 }
             }
         }
         else{
             data.left = left;
             data.childContentHeight = childContentHeight;
             data.top = top;
         }
         if(isLeft)
             data.leftDataSet = true;
         else
             data.rightDataSet = true;
     }
     
 
     protected void placeChildren(final LayoutData data) {
         final int contentX = Math.max(getSpaceAround(), -data.left);
         int contentY= getSpaceAround() - Math.min(0, data.top);
         
         if (getView().isContentVisible()) {
             getContent().setVisible(true);
         }
         else {
             getContent().setVisible(false);
         }
         
         int baseY = contentY - getSpaceAround() + data.top;
         int minY = 0;
         for (int i = 0; i < getChildCount(); i++) {
             if(!data.summary[i] && data.free[i]){
             	minY = Math.min(minY, contentY + data.ly[i]);
             }
             else
             	minY = Math.min(minY, baseY + data.ly[i]);
         }
         if(minY < 0){
 			contentY -= minY;
         	baseY -= minY;
         }
         int width =  contentX + contentWidth + getSpaceAround();
         int height = contentY + contentHeight + getSpaceAround();
         getContent().setBounds(contentX, contentY, contentWidth, contentHeight);
         int topOverlap = -minY;
         int heigthWithoutOverlap = height;
         for (int i = 0; i < getChildCount(); i++) {
             NodeView child = (NodeView) getView().getComponent(i);
             final int y;
             if(!data.summary[i] && data.free[i]){
             	y = contentY + data.ly[i];
             }
             else{
             	y = baseY + data.ly[i];
             	if(! data.free[i])
             		heigthWithoutOverlap = Math.max(heigthWithoutOverlap, y + child.getHeight() - child.getBottomOverlap());
             }
 			final int x = contentX + data.lx[i];
 			child.setLocation(x, y);
             width = Math.max(width, child.getX() + child.getWidth());
             height = Math.max(height, y + child.getHeight());
         }
         
         view.setSize(width, height);
         view.setTopOverlap(topOverlap);
         view.setBottomOverlap(height - heigthWithoutOverlap);
     }
 
 	public void layoutNodeMotionListenerView(final NodeMotionListenerView view) {
 		final NodeView movedView = view.getMovedView();
 		final JComponent content = movedView.getContent();
 		location.x = -LISTENER_VIEW_WIDTH;
 		location.y = 0;
 		UITools.convertPointToAncestor(content, location, view.getParent());
 		view.setLocation(location);
 		view.setSize(LISTENER_VIEW_WIDTH, content.getHeight());
 	}
 }
