 package de.blocks;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 
 public class GameField extends Object3D {
     private final ArrayList<Block>       blocks;
     private final Color[]                colors;
     private final float                  width;
     private final float                  height;
     private int                          currentColorIndex;
     private final ArrayList<Block>       grayList;
     private final ArrayList<RemovalZone> removalZones;
     private int                          removedBlocks;
     private int[]						 removalZonesCountByColor;
     private ArrayList<Block>[]			 blocksInRemovalZones;
     
     
     @SuppressWarnings("unchecked")
 	public GameField(final StillModel model, final float width, final float height) {
         super(model);
         blocks = new ArrayList<Block>();
         grayList = new ArrayList<Block>();
         this.width = width;
         this.height = height;
 
         currentColorIndex = 0;
         colors = new Color[4];
         colors[0] = Color.YELLOW;
         colors[1] = Color.BLUE;
         colors[2] = Color.GREEN;
         colors[3] = Color.RED;
 
         modelMatrix.scale(width, height, 1.0f);
 
         removalZones = new ArrayList<RemovalZone>();
         removalZonesCountByColor = new int[4];
         blocksInRemovalZones = (ArrayList<Block>[]) new ArrayList[4];
         for(int i = 0; i <4; i++){
         	blocksInRemovalZones[i] = new ArrayList<Block>();
         }
     }
 
     public void addBlock(final Block block) {
         blocks.add(block);
     }
 
     public void addRemovalZone(final float x, final float y, final Color color) {
         removalZones.add(new RemovalZone(Blocks3DGraphics.PlaneModel, x, y, color));
         for(int i = 0; i < 4; i++){
         	if(colors[i] == color)
         		removalZonesCountByColor[i]++;
         }
     }
     
     public void update(){
     	if(blocksInRemovalZones[currentColorIndex].size() == removalZonesCountByColor[currentColorIndex]){
     		blocks.removeAll(blocksInRemovalZones[currentColorIndex]);
     		removedBlocks += blocksInRemovalZones[currentColorIndex].size();
     		blocksInRemovalZones[currentColorIndex].clear();
     	}
     }
 
     private static final float FAKE_GRID_SIZE = 0.1f;
     
     public void createBlock() {
     	boolean blockFound = false;
     	Random rand = new Random(System.currentTimeMillis());
     	
     	while(!blockFound) {
             final float x = (int) (rand.nextDouble() * (width-6) - (width-6) / 2);
             final float y = (int) (rand.nextDouble() * (height-6) - (height-6) / 2);
             if (hasFreeSpace(x, y)) {
                 final Block block = new Block(Blocks3DGraphics.BlockModel, x, y, Color.GRAY);
                 grayList.add(block);
                 addBlock(block);
                 blockFound = true;
             }    		
     	}
         
     }
 
     public Color getCurrentColor() {
         return colors[currentColorIndex];
     }
 
     public float getHeight() {
         return height;
     }
 
     public float getWidth() {
         return width;
     }
 
     public boolean hasFreeSpace(final float x, final float y) {
         for (final Block block : blocks) {
             if (block.getBoundingRectangle().collides(new Square(x, y, 0.5f))) {
                 return false;
             }
         }
 
         return true;
     }
 
     public void moveBlocks(final float x, final float y) {
         final LinkedList<Block> movables = new LinkedList<Block>();
         final LinkedList<Block> nonMovables = new LinkedList<Block>();
         
         blocksInRemovalZones[currentColorIndex].clear();
 
         for (final Block block : blocks) {
             if (block.getColor() == colors[currentColorIndex]) {
                 block.getTestBoundingRectangle().set(block.getBoundingRectangle());
                 block.getTestBoundingRectangle().centerX += x;
                 block.getTestBoundingRectangle().centerY += y;
                 movables.add(block);
             } else {
                 nonMovables.add(block);
             }
         }
 
         final LinkedList<Block> collided = new LinkedList<Block>();
         
         
        boolean noCollision = true;
         while (true) {
             collided.clear();

             for (final Block movable : movables) {
 
                 for (final Block nonMovable : nonMovables) {
                     if (movable.getTestBoundingRectangle().collides(nonMovable.getBoundingRectangle())) {
                         resolveCollision(movable, nonMovable);
                         collided.add(movable);
                         noCollision = false;
                     }
                 }
             }
 
             for (final Block block : collided) {
                 block.setX(block.getTestBoundingRectangle().centerX);
                 block.setY(block.getTestBoundingRectangle().centerY);
                 block.getBoundingRectangle().set(block.getTestBoundingRectangle());
                 
                 for(final RemovalZone removalZone : removalZones){
                 	if(removalZone.getColor() != colors[currentColorIndex])
                 		continue;
                 	if(block.getBoundingRectangle().covers(removalZone.getZone())){
                 		blocksInRemovalZones[currentColorIndex].add(block);
                 	}
                 }
             }
 
             nonMovables.addAll(collided);
             movables.removeAll(collided);
 
             if (noCollision) {
                 break;
             }
         }
 
         for (final Block block : movables) {
             block.setX(block.getTestBoundingRectangle().centerX);
             block.setY(block.getTestBoundingRectangle().centerY);
             block.getBoundingRectangle().set(block.getTestBoundingRectangle());
             
             for(final RemovalZone removalZone : removalZones){
             	if(removalZone.getColor() != colors[currentColorIndex])
             		continue;
             	if(block.getBoundingRectangle().covers(removalZone.getZone())){
             		blocksInRemovalZones[currentColorIndex].add(block);
             	}
             }
         }
     }
 
     public void nextColor() {
         for (final Block block : grayList) {
             block.setColor(getCurrentColor());
         }
 
         grayList.clear();
         currentColorIndex = (currentColorIndex + 1) % 4;
     }
 
     public void removeBlock(final Block block) {
         blocks.remove(block);
     }
 
     @Override
     public void render(final ShaderProgram shader) {
         shader.setUniformMatrix("u_modelMatrix", modelMatrix);
         shader.setUniformf("u_color", Color.WHITE);
         model.render(shader);
 
         for (final RemovalZone removalZone : removalZones) {
             removalZone.render(shader);
         }
 
         for (final Block block : blocks) {
             block.render(shader);
         }
     }
 
     private void resolveCollision(final Block blockToResolve, final Block other) {
         final float distanceX = other.getBoundingRectangle().centerX - blockToResolve.getBoundingRectangle().centerX;
         final float distanceY = other.getBoundingRectangle().centerY - blockToResolve.getBoundingRectangle().centerY;
 
         if (Math.abs(distanceX) < Math.abs(distanceY)) {
             // Hit left or right side
             blockToResolve.getTestBoundingRectangle().setCenterY(other.getY() - Math.signum(distanceY));
         } else {
             // Hit bottom or top side
             blockToResolve.getTestBoundingRectangle().setCenterX(other.getX() - Math.signum(distanceX));
         }
     }
     
     public int getRemovedBlocks() {
         return removedBlocks;
     }
 }
