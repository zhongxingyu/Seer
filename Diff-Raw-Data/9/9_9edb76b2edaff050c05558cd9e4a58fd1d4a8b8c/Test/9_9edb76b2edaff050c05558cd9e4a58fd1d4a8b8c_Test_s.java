 
 public class Test {
 
 	public static void main(String[] args) {
 		//TestRunner.main();
 		
 		String[] map = new String[]{
 			"-o-o",
 			"    ",
 			" o-o",
 			"   |",
 			"-o o",
 			"   |"
 		};
 		int[][] treasure = new int[][] {
 			new int[]{50, 5},
 			new int[]{ 0, 0},
 			new int[]{ 0,10}
 		};
 		
 		Labyrinth lab = new Labyrinth(map, treasure);
 		for (int i = 0; i < 10; i++) { 
			lab.addPlayer(new TreasureHunter(lab.getCell(0,0), 10, 100));
 		}
 		lab.start();
 		lab.waitForEnd();
 		
 		for (Player player : lab.getPlayers()) {
 			if (player instanceof TreasureHunter) {
 				TreasureHunter hunter = (TreasureHunter) player;
 				System.out.println(hunter.getGold());
 			}
 		}
 	}
 
 }
