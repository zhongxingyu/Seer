 package terminator.model;
 
 import java.util.*;
 import e.util.*;
 
 public class TextLines {
         private LinkedList<TextLine> textLines = new LinkedList<TextLine>();
 
         public TextLines(int width, int height) {
                 setSize(width, height);
         }
 
         public void setSize(int width, int height) {
                 while (textLines.size() > height)
                         textLines.removeFirst();
                while (textLines.size() < height)
                         textLines.addLast(new TextLine());
         }
 
         public int insertLines(int beginningAt, int count, int top, int bottom) {
                 int above = Math.min(count, bottom - beginningAt + 1);
                 replace(beginningAt, bottom + 1, above);
                 replace(bottom + 1, top, count - above);
                 return above;
         }
 
         private void replace(int addAt, int removeAt, int count) {
                 add(addAt, count);
                 remove(removeAt, count);
         }
 
         private void add(int at, int count) {
                 ListIterator<TextLine> it = textLines.listIterator(at);
                 for (int i = 0; i < count; i++)
                         it.add(new TextLine());
         }
 
         private void remove(int at, int count) {
                 ListIterator<TextLine> it = textLines.listIterator(at);
                 for (int i = 0; i < count; i++) {
                         it.next();
                         it.remove();
                 }
         }
 
         public TextLine get(int index) {
                 return textLines.get(index);
         }
 }
