 package com.rhoward;
 
 import static com.rhoward.Block.BlockType;
 
 public abstract class Domino {
 
     protected Block[] blocks;
     protected BlockType type;
     protected int numBlocks;
     protected float x, y;
    protected float velocityX, velocityY;
 
    protected final float BLOCK_WIDTH = Block.getBLOCK_WIDTH();
    protected final float BLOCK_HEIGHT = Block.getBLOCK_HEIGHT();
 
     public Domino(BlockType type, int numBlocks, float x, float y) {
         this.type = type;
         this.numBlocks = numBlocks;
         this.x = x;
         this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
 
         blocks = new Block[this.numBlocks];
 
         for (int i = 0; i < this.numBlocks; i++)
             this.blocks[i] = new Block(this.type, 0, 0);
 
         calculateBlockPositions();
     }
 
     public void update(int delta) {
        this.x += ((float) delta / 100) * this.velocityX;
        this.y += ((float) delta / 100) * this.velocityY;

        calculateBlockPositions();
     }
 
     public void draw() {
         for (int i = 0; i < numBlocks; i++) {
             blocks[i].draw();
         }
     }
 
     protected abstract void calculateBlockPositions();
 
     public void modVelocityX(float velocityX) {
         this.velocityX += velocityX;
     }
 
     public void modVelocityY(float velocityY) {
         this.velocityY += velocityY;
     }
 }
