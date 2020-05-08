 public class updateTable {
 	public  int[][] NorthTable;
 	public  int[][] EastTable;
 	public  int[][] SouthTable;
 	public  int[][] WestTable;
 
 	/**
 	 * 
 	 * @return NorthTable contains locations of all the tiles that need to be
 	 *         updated when ants moves to the North
 	 */
 	public int[][] getNorthTable() {
 		return NorthTable;
 	}
 	/**
 	 * 
 	 * @return SouthTable contains locations of all the tiles that need to be
 	 *         updated when ants moves to the South
 	 */
 	public int[][] getSouthTable() {
 		return SouthTable;
 	}
 	/**
 	 * 
 	 * @return EastTable contains locations of all the tiles that need to be
 	 *         updated when ants moves to the East
 	 */
 	public int[][] getEastTable() {
 		return EastTable;
 	}
 	/**
 	 * 
 	 * @return WestTable contains locations of all the tiles that need to be
 	 *         updated when ants moves to the West
 	 */
 	public int[][] getWestTable() {
 		return WestTable;
 	}
 
 	/**
 	 * checks ants movement direction and returns the table containing the
 	 * locations of tiles that need to be updated.
 	 * 
 	 * @param direction
 	 *            where ant is moving
 	 * @return table that matches the movement direction
 	 */
 	public int[][] setUpdateTable(Aim direction) {
 		if (direction == Aim.NORTH) {
 			return getNorthTable();
 		} else if (direction == Aim.EAST) {
 			return getEastTable();
 		} else if (direction == Aim.SOUTH) {
 			return getSouthTable();
 		} else {
 			return getWestTable();
 		}
 	}
 	/**
 	 * initializes a NorthTable
 	 * 
 	 * @param vision
 	 *            value of ants range of vision in a square format.
 	 */
 	public void setNorthTable(int vision) {
 		int k = -1;
 		int i = -1;
 		int value = 0;
 		int range = (int) Math.sqrt(vision);
 		NorthTable = new int[2 * range + 1][2];
 		for (int j = range; j > 0; j--) {
 			while (value < vision) {
 				i++;
 				value = (int) Math.pow(j, 2) + (int) Math.pow(i, 2);
 				}
 			k++;
 			NorthTable[k][0] = j;
 			NorthTable[k][1] = i;
 			i = 0;
 			value = 0;
 		}
 		i = range;
 		NorthTable[k+1][0] = 0;
 		NorthTable[k+1][1] = range + 1;
 		for (;k >= 0; k--) {
 			i++;
 			NorthTable[i][0] = -1 * NorthTable[k][0];
 			NorthTable[i][1] = NorthTable[k][1];
 		}
		setEastTable();
		setSouthTable();
		setWestTable();
 	}
 
 	/**
 	 * initializes the EastTable by using the NorthTable and simply switches the
 	 * values to changes it to EastTable.
 	 */
 	public void setEastTable() {
 		EastTable = new int[NorthTable.length][NorthTable[0].length];
 		for (int i = 0; i < NorthTable.length; i++) {
 			EastTable[i][0] = NorthTable[i][1];
 			EastTable[i][1] = NorthTable[i][0];
 		}
 	}
 	/**
 	 * initializes the SouthTable by using the NorthTable and multiplies the y
 	 * column with -1 to get SouthTable.
 	 */
 	public void setSouthTable() {
 		SouthTable = new int[NorthTable.length][NorthTable[0].length];
 		for (int i = 0; i < NorthTable.length; i++) {
 			SouthTable[i][0] = NorthTable[i][0];
 			SouthTable[i][1] = -1 * NorthTable[i][1];
 		}
 	}
 	/**
 	 * initializes the WestTable by using the NorthTable and first switches y
 	 * and x and then multiplies the x with -1 column to get WestTable.
 	 */
 	public void setWestTable() {
 		WestTable = new int[NorthTable.length][NorthTable[0].length];
 		for (int i = 0; i < NorthTable.length; i++) {
 			WestTable[i][0] = -1 * NorthTable[i][1];
 			WestTable[i][1] = NorthTable[i][0];
 		}
 	}
 }
