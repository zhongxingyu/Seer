 package com.rhoward;
 
 public class LDomino extends Domino {
 
     public LDomino(Block.BlockType type, int x, int y) {
         super(type, x, y);
     }
 
     @Override
     public Domino rotate() {
         LDomino newState = new LDomino(this.type, this.x, this.y);
         newState.rotateState = this.rotateState;
         newState.rotateState = newState.nextRotation();
 
         return newState;
     }
 
     @Override
     public Domino translate(int x, int y) {
         LDomino translated = new LDomino(this.type, this.x + x, this.y + y);
         translated.rotateState = this.rotateState;
         return translated;
     }
 
     @Override
     public String getGrid() {
         String grid;
 
         switch (rotateState) {
             case NORMAL:
                 grid = "10,10,11";
                 break;
            case RIGHT:
                grid = "111,100";
                break;
             case DOWN:
                 grid = "11,01,01";
                 break;
             case LEFT:
                 grid = "001,111";
                 break;
             default:
                 grid = "";
                 break;
         }
 
         return grid;
     }
 
     @Override
     public int getHeight() {
         if ((this.rotateState == RotateState.NORMAL) || (this.rotateState == RotateState.DOWN))
             return 3;
         else
             return 2;
     }
 
     @Override
     public int getWidth() {
         if ((this.rotateState == RotateState.NORMAL) || (this.rotateState == RotateState.DOWN))
             return 2;
         else
             return 3;
     }
 }
