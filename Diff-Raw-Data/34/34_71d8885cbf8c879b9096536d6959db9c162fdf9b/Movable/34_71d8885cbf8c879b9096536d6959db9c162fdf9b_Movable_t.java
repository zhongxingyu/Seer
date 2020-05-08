 
 public interface Movable {
  /**
   * A function that determines if a piece can move to a certain location.
   * For every side, we flip the board around so that increasing rank is 
   * considered "forward".
   * 
   * @param rank
   * @param file
   * @return
   */
  public boolean canMoveTo(int rank, int file);
 }
