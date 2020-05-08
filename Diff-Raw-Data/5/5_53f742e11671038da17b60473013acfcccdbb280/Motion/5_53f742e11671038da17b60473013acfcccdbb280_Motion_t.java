 package j3chess;
 
 /**
  * a generalized abstraction of a motion.
  */
 public class Motion {
 
     /** @brief the general direction of the motion */
     private DirectionGroup mDirection;
     /**
      * @brief the enforced number of steps of the motion
      *        [0  ] = infinity
      *        [1..] = enforced finite number of steps
      */
     private int mSteps;
     /**
      * @brief whether the motion is blockable or not (e.g. jumping is
      *        unblockable) (if true = unblockable by pieces)
      */
     private boolean mUnblockable;
 
     /**
      * @brief default empty constructor for a motion
      */
     public Motion() {
         this(DirectionGroup.Vertical, 1, false);
     }
 
     /**
      * @brief complete constructor for a motion
      * @param direction
      *            the general direction of the motion
      * @param steps
      *            the enforced number of steps of the motion
      * @param unblockable
      *            whether the motion is unblockable or not
      */
     public Motion(
             final DirectionGroup direction,
             final int steps,
             final boolean unblockable) {
         this.mDirection = direction;
         this.mSteps = steps;
         this.mUnblockable = unblockable;
     }
 
     /**
      * @brief getter for the mDirection member
      * @return the general direction of the motion
      */
     public final DirectionGroup getDirection() {
         return mDirection;
     }
 
     /**
      * @brief setter for the mDirection member
      * @param direction
      *            the general direction of the motion
      */
     public final void setDirection(final DirectionGroup direction) {
         this.mDirection = direction;
     }
 
     /**
      * @brief getter for the mSteps member
      * @return the enforced number of steps of the motion
      */
     public final int getSteps() {
         return mSteps;
     }
 
     /**
      * @brief setter for the mSteps member
      * @param steps
      *            the enforced number of steps of the motion
      */
     public final void setSteps(final int steps) {
         this.mSteps = steps;
     }
 
     /**
     * @brief getter for the mUnblockable member
      * @return whether the motion is unblockable or not
      */
     public final boolean getUnblockable() {
         return mUnblockable;
     }
 
     /**
     * @brief setter for the mUnblockable member
      * @param unblockable
      *            whether the motion is a jump or not
      */
     public final void setUnblockable(final boolean unblockable) {
         this.mUnblockable = unblockable;
     }
 
 }
