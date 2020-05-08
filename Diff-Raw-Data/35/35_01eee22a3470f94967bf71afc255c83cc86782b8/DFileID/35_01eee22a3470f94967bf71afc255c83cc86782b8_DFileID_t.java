 package common;
 
 /* typedef DFileID to int */
 public class DFileID
 {
 	private int _dFID;
 
 
 	public DFileID(int dFID) {
 		_dFID = dFID;
 	}
 
     public int block()
     {
         if (_dFID == 0) {
             // First block may contain disk metadata
             return 0;
         }
        else if (_dFID <= Constants.NUM_OF_INODES) {
             // Multiple inodes share a block starting at block 1
            return (_dFID - 1) * Constants.INODE_SIZE / Constants.BLOCK_SIZE + 1;
         }
         else {
             // Regular file blocks take up full block
             return 1 + // First block is metadata
                 (Constants.NUM_OF_INODES * Constants.INODE_SIZE /
                  Constants.BLOCK_SIZE) +  // Number of inode blocks
                (_dFID - Constants.NUM_OF_INODES - 1);
         }
     }
 
     public int offset()
     {
         if (_dFID == 0) {
             // First block may contain disk metadata
             return 0;
         }
        else if (_dFID <= Constants.NUM_OF_INODES) {
             // Multiple inodes share a block starting at block 1
            return (_dFID - 1) * Constants.INODE_SIZE % Constants.BLOCK_SIZE;
         }
         else {
             // Regular file blocks take up full block
             return 0;
         }
     }
    public int id(){
    	return _dFID;
    }
 }
