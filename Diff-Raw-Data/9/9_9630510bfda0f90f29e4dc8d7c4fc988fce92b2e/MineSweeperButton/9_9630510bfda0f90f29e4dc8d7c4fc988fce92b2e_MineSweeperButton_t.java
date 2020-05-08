 package com.NinjaMines;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.PorterDuff;
 import android.widget.Button;
 
 public class MineSweeperButton extends Button {
     private boolean isMine;
     private boolean toggled;
     private boolean flagged;
 
     public MineSweeperButton(Context context) {
         super(context);
         isMine = false;
         toggled = false;
         flagged =  false;
     }
 
     public boolean isMined() {
         return isMine;
     }
 
     public boolean isToggled() {
         return toggled;
     }
 
     public void toggle() {
         if (!toggled) {
             this.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
             toggled = true;
         }
     }
 
     public void mine() {
         isMine = true;
     }
 
     public void clearMine() {
         isMine = false;
         this.setText("");
 
     }
 
     public void resetStatus() {
         isMine = false;
         toggled = false;
         flagged=false;
         this.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
         this.setText("");
     }
 
     public void flagMine() {
        if(!toggled){
            flagged =!flagged;
            if (flagged) this.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            else if(!flagged) this.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        }

     }
     public boolean isFlagged(){
         return flagged;
     }
 
 
 }
