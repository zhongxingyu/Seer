 package kniemkiewicz.jqblocks.ingame.item.controller;
 
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import kniemkiewicz.jqblocks.ingame.block.SolidBlocks;
 import kniemkiewicz.jqblocks.ingame.block.WallBlockType;
 import kniemkiewicz.jqblocks.ingame.item.DirtBlockItem;
 import kniemkiewicz.jqblocks.ingame.object.MovingPhysicalObject;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.Iterator;
 
 /**
  * User: krzysiek
  * Date: 14.07.12
  */
 @Component
 public class DirtBlockItemController extends AbstractActionItemController<DirtBlockItem> {
   @Autowired
   SolidBlocks blocks;
 
   @Override
   boolean canPerformAction(int x, int y) {
    return blocks.getBlocks().collidesWithNonEmpty(new Rectangle(x, y, 1, 1));
   }
 
   @Override
   Rectangle getAffectedRectangle(int x, int y) {
     return new Rectangle(x, y, Sizes.BLOCK - 1, Sizes.BLOCK - 1);
   }
 
   @Override
   void startAction(DirtBlockItem item) {  }
 
   @Override
   void stopAction(DirtBlockItem item) {  }
 
   @Override
   void updateAction(DirtBlockItem item, int delta) { }
 
   @Override
   boolean isActionCompleted() {
     return true;
   }
 
   @Override
   void onAction() {
     addBlock(affectedRectangle.getX(), affectedRectangle.getY());
   }
 
   private void addBlock(float x, float y) {
     addBlock(Sizes.roundToBlockSizeX(x), Sizes.roundToBlockSizeY(y));
   }
 
   private void addBlock(int x, int y) {
     Rectangle rect = new Rectangle(x, y, Sizes.BLOCK - 1, Sizes.BLOCK - 1);
     if (!blocks.getBlocks().collidesWithNonEmpty(rect)) {
       blocks.add(new Rectangle(x, y, Sizes.BLOCK, Sizes.BLOCK), WallBlockType.DIRT);
     }
   }
 
   @Override
   public Shape getDropObjectShape(DirtBlockItem item, int centerX, int centerY) {
     return null;
   }
 
   @Override
   public MovingPhysicalObject getDropObject(DirtBlockItem item, int centerX, int centerY) {
     return null;
   }
 }
