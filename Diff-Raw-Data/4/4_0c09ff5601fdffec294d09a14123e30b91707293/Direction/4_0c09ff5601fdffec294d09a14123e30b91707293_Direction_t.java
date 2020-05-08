 package ru.spbau.bioinf.mgra;
 
 
 public enum Direction {
     MINUS("minus") {
         @Override
         int getSide(EndType endType) {
             return endType == EndType.HEAD ? - 1: 1;
         }
 
         @Override
         Direction reverse() {
             return Direction.PLUS;
         }
     }, PLUS("plus") {
         @Override
         int getSide(EndType endType) {
             return endType == EndType.TAIL ? - 1: 1;
         }
 
         @Override
         Direction reverse() {
             return Direction.MINUS;
         }
     };
 
     private String text;
 
     Direction(String text) {
         this.text = text;
     }
 
     public static Direction getDirection(char ch) {
         return ch == '-' ? MINUS : PLUS;
     }
 
     abstract int getSide(EndType endType);

    public int getSide(End end) {
        return getSide(end.getType());
    }
     abstract Direction reverse();
 
     @Override
     public String toString() {
         return text;
     }
 }
