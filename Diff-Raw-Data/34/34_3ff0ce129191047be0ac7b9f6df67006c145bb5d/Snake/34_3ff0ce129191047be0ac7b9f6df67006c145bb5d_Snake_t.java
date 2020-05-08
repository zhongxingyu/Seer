 package com.androidgames.mrmunch;
 
 import java.util.ArrayList;
 import java.util.List;
import android.util.Log;
 
 public class Snake {
     public static final int UP = 0;
     public static final int LEFT = 1;
     public static final int DOWN = 2;
     public static final int RIGHT = 3;
     
     public List<SnakePart> parts = new ArrayList<SnakePart>();
     public int direction;
     public boolean already_turned;
     
     public Snake() {        
         direction = UP;
         parts.add(new SnakePart(5, 6));
         parts.add(new SnakePart(5, 7));
         parts.add(new SnakePart(5, 8));
         already_turned= false;
         
     }
     
     public void turnLeft() {
         direction += 1;
         if(direction > RIGHT)
             direction = UP;
         already_turned = true;
     }
     
     public void turnRight() {
         direction -= 1;
         if(direction < UP)
             direction = RIGHT;
         already_turned = true;
     }
     
     public void eat() {
    	Log.e("DEBUG","World tick = " + World.tick);
         SnakePart end = parts.get(parts.size()-1); 
         parts.add(new SnakePart(end.x, end.y));
     }
     
     public void advance() {
         SnakePart head = parts.get(0);               
         
         int len = parts.size() - 1;
         for(int i = len; i > 0; i--) {
             SnakePart before = parts.get(i-1);
             SnakePart part = parts.get(i);
             part.x = before.x;
             part.y = before.y;
         }
         
         if(direction == UP)
             head.y -= 1;
         if(direction == LEFT)
             head.x -= 1;
         if(direction == DOWN)
             head.y += 1;
         if(direction == RIGHT)
             head.x += 1;
         
         if(head.x < 0)
             head.x = 9;
         if(head.x > 9)
             head.x = 0;
         if(head.y < 0)
             head.y = 12;
         if(head.y > 12)
             head.y = 0;
         
         already_turned = false;
     }
     
     public boolean checkBitten() {
         int len = parts.size();
         SnakePart head = parts.get(0);
         for(int i = 1; i < len; i++) {
             SnakePart part = parts.get(i);
             if(part.x == head.x && part.y == head.y)
                 return true;
         }        
         return false;
     }   
     
     public void shrink() {
     	for (int i = 0; i < 5; i++) {
     		SnakePart end = parts.get(parts.size()-1);
     		parts.remove(end);
     	}
     }
 }
