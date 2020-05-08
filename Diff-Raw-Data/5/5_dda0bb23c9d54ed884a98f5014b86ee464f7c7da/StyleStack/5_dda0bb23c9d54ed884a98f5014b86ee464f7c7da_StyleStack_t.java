 /*
  * Created on Dec 24, 2004
  */
 package org.spacebar.escape.common;
 
 /**
  * @author adam
  */
 public class StyleStack {
     public static final int COLOR_WHITE = 0;
 
     public static final int COLOR_BLUE = 1;
 
     public static final int COLOR_RED = 2;
 
     public static final int COLOR_YELLOW = 3;
 
     public static final int COLOR_GRAY = 4;
 
     public static final int COLOR_GREEN = 5;
 
     public static final int ALPHA_100 = 0;
 
     public static final int ALPHA_50 = 1;
 
     public static final int ALPHA_25 = 2;
 
     protected int alpha;
 
     protected int color;
 
     private FontAttribute top;
 
     public void push(char c) {
         FontAttribute next;
         if (c >= '#' && c <= '\'') {
             next = new FontAttribute(FontAttribute.TYPE_ALPHA, alpha, top);
             alpha = c - '#';
         } else {
             next = new FontAttribute(FontAttribute.TYPE_COLOR, color, top);
             color = c - '0';
         }
         top = next;
     }
 
     public void pop() {
         if (top == null) {
 //            System.out.println("Popping empty StyleStack!");
             return;
         }
 
         int what = top.getWhat();
         switch (what) {
         case FontAttribute.TYPE_ALPHA:
             alpha = top.getValue();
             break;
         case FontAttribute.TYPE_COLOR:
             color = top.getValue();
             break;
         }
         top = top.getNext();
     }
 
     public int getAlpha() {
         return alpha;
     }
 
     public int getAlphaValue() {
         int result;
         switch (alpha) {
         case ALPHA_100:
             result = 255;
            break;
         case ALPHA_50:
             result = (int) (0.5 * 255);
            break;
         case ALPHA_25:
             result = (int) (0.25 * 255);
            break;
         default:
             result = 255;
         }
         return result;
     }
 
     public int getColor() {
         return color;
     }
 
     public static String removeStyle(String text) {
         StyleStack s = new StyleStack();
         StringBuffer str = new StringBuffer();
         for (int i = 0; i < text.length(); i++) {
             char ch = text.charAt(i);
             if (ch == '^') {
                 i++;
                 ch = text.charAt(i);
                 switch (ch) {
                 case '^':
                     str.append(ch);
                     break;
                 case '<':
                     s.pop();
                     break;
                 default:
                     s.push(ch);
                     break;
                 }
             } else {
                 str.append(ch);
             }
         }
         return str.toString();
     }
     
     private static class FontAttribute {
         public static final int TYPE_COLOR = 0;
 
         public static final int TYPE_ALPHA = 1;
 
         private int what;
 
         private int value;
 
         FontAttribute next;
 
         public FontAttribute(int what, int value, FontAttribute next) {
             if (what != TYPE_ALPHA && what != TYPE_COLOR) {
                 throw new IllegalArgumentException(
                         "what must be TYPE_ALPHA or TYPE_COLOR");
             }
 
             this.what = what;
             this.value = value;
             this.next = next;
         }
 
         public int getWhat() {
             return what;
         }
 
         public int getValue() {
             return value;
         }
 
         public FontAttribute getNext() {
             return next;
         }
     }
 }
