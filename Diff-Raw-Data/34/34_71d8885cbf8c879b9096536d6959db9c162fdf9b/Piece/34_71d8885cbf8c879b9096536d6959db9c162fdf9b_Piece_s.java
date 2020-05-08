 
 public class Piece implements Movable {
   protected int file, rank;
   protected String color;
   protected String type;
   
   public String getColor() {
     return color.equals("white") ? "w" : "b";
   }
   
   public String getType() {
     return this.type;
   }
   
   public String getFile() {
     // lol tricks
     return (String) (new Object[] {'a', 'b','c','d','e','f','g','h'})[this.file];
   }
   
   public String getRank() {
     return String.valueOf(this.rank);
   }
   
   public String toString() {
     return getColor() + getType();
   }
 
   @Override
  public boolean canMoveTo(int file, int rank) {
     return false;
   }
 }
