 package com.rhoward;
 
 import static com.rhoward.Block.BlockType;
 
 public class BlockDomino extends Domino{
 
    private final int NUM_BLOCKS = 4;
    private Block[] blocks = new Block[NUM_BLOCKS];
    BlockType type;

    private final float BLOCK_WIDTH = Block.getBLOCK_WIDTH();
    private final float BLOCK_HEIGHT = Block.getBLOCK_HEIGHT();

    private float x, y;
    private float velocityX, velocityY;

     public BlockDomino(BlockType type, float x, float y) {
         super(type, 4, x, y);
     }
 
     @Override
     protected void calculateBlockPositions() {
         // top-left block
         blocks[0].setX(this.x);
         blocks[0].setY(this.y);
 
         // top-right block
         blocks[1].setX(this.x + BLOCK_WIDTH);
         blocks[1].setY(this.y);
 
         // bottom-right block
         blocks[2].setX(this.x + BLOCK_WIDTH);
         blocks[2].setY(this.y + BLOCK_HEIGHT);
 
         // bottom-left block
         blocks[3].setX(this.x);
         blocks[3].setY(this.y + BLOCK_HEIGHT);
     }
 
     public float getHeight() {
         return BLOCK_HEIGHT * 2;
     }
 
     public float getWidth() {
         return BLOCK_WIDTH * 2;
     }
 }
