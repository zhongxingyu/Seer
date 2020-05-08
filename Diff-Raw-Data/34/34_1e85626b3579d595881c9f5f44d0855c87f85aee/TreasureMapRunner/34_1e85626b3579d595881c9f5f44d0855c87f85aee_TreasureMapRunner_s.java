 package conrad.treasureMap;
 
 import java.util.Scanner;
 
 public class TreasureMapRunner {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		//		Grid g = new Grid(4, 5);
		//		g.addTreasureChest(new TreasureChest("1"), 0, 0);
		//		g.addTreasureChest(new TreasureChest("2"), 1, 2);
		//		TreasureChestManager v = new TreasureChestManager();
		//		v.displayTreasureChestNearby(g);
 		instructions();
 		runTreasurMapApp();		
 
 	}
 
 	private static void instructions(){
 		System.out.println("*************************************************************************************");
 		System.out.println();
 		System.out.println("                     TREASURE MAP INSTRUCTIONS        ");
 		System.out.println();
 		System.out.println("1.Create a grid by defining the number of colums and rows");
 		System.out.println("2.Add treasure chests to your grid");
 		System.out.println("3.Display how many treasure chests are nearby");
 		System.out.println("1.TYPE EXIT TO TERMINATE");
 		System.out.println();
 		System.out.println("*************************************************************************************");
 
 	}
 
 	private static void runTreasurMapApp(){
 
 		Scanner scanner = new Scanner(System.in);
 		Grid grid = createGrid(scanner);
 		addTreasureChestToGrid(scanner,grid);
 	}
 
 
 	private static Grid createGrid(Scanner scanner){
 		Grid grid = null;
 		System.out.println("Enter a name for your Grid :");
 		String name =getName(scanner);
 		while(name==null||name.equals("")){
 			System.out.println("please enter a name for your grid :");
 			name =getName(scanner);
 		}
 
 		System.out.println("how many rows would you like your grid to have: ");
 		int numRows =getRows(scanner);
 		while(numRows<0){
 			System.out.println("Please enter a number and ensure its greater than 0 :");
 			numRows=getRows(scanner);
 		}
 
 		System.out.println("how many columns would you like :");
 		int numCols =getColumns(scanner);
 		while(numCols<0){
 			System.out.println("Please enter a number and ensure its greater than 0 :");
 			numCols=getColumns(scanner);
 		}
 		// display grid
 		grid = new Grid(numRows, numCols);
 		grid.setName(name);
 		grid.displayGrid();
 
 		return grid;
 
 	}
 
 	private static void addTreasureChestToGrid(Scanner scanner,Grid grid) {
 		System.out.println("would you like to add a treasure chest to your grid. Enter Y or N :");
 		String addTreasure = addTreasure(scanner);
 		while(!addTreasure.equalsIgnoreCase("y")&&!addTreasure.equalsIgnoreCase("n")){
 			System.out.println("enter Y or N :");
 			addTreasure = addTreasure(scanner);
 		}
 		if(addTreasure.trim().toLowerCase().equalsIgnoreCase("y")){
 			addTreasureChest(scanner,grid);
 		}else{
 			TreasureChestCounter counter = new TreasureChestManager();
 			System.out.println("**********Treasure maps nearby****************" );
 			counter.displayTreasureChestNearby(grid);
 
 		}
 
 
 	}
 
 	private static int getRows(Scanner scanner){
 
 		String rows = scanner.nextLine();
 		int numRows =TreasureMapUtils.convertToInt(rows, -1);
 		return numRows;
 	}
 
 	private static String getName(Scanner scanner){
 		return scanner.nextLine();
 
 	}
 
 	private static int getColumns(Scanner scanner){
 		String cols = scanner.nextLine();
 		int numCols =TreasureMapUtils.convertToInt(cols, -1);
 		return numCols;
 	}
 
 	private static void addTreasureChest(Scanner scanner,Grid grid){
 		System.out.println("please provide a name for your treasure chest :");
 		String chest = scanner.nextLine();
 		while(chest==null||chest.equals("")){
 			System.out.println("please provide a name for your treasure chest :");
 			chest=scanner.nextLine();
 		}
 		System.out.println("please provide a position for your treasure chest ");
 		System.out.println("please enter a row number row numbers start from 0 and should be less than "+grid.getRows()+" : ");
 		int row = getRows(scanner);
 		while(row==-1||row>grid.getRows()){
 			System.out.println("please enter a valid row number.Row numbers start from 0 and is less than "+grid.getRows()+" :");
 			row =getRows(scanner);
 		}
 		int col = getColumns(scanner);
 		System.out.println("please enter the column you would like to position your treasure chest columns starts 1 and should be less than "+grid.getColumns()+" : ");
 		while(col==-1||col>grid.getColumns()){
 			System.out.println("please enter a valid column number. Column numbers start from 1 and is less than "+grid.getColumns());
 			col=getColumns(scanner);
 		}
 		grid.addTreasureChest(new TreasureChest(chest), row, col);
 		grid.displayGrid();
 		System.out.println("would you like to add another treasure chest to the grid ? Y or N");
 		String yn=addTreasure(scanner);
 		while(!yn.trim().equalsIgnoreCase("Y")&&!yn.trim().equalsIgnoreCase("N")){
 			System.out.println("enter Y or N :");
 			yn = scanner.nextLine();
 		}
 		if(yn.trim().equalsIgnoreCase("Y")){
 			addTreasureChest(scanner,grid);
 		}else{
 			TreasureChestCounter counter = new TreasureChestManager();
 			System.out.println("**********Treasure maps nearby****************" );
 			counter.displayTreasureChestNearby(grid);
			
 			System.out.println("thank you for using the trasure map finder");
 
 		}
 
 	}
 
 	private static String addTreasure(Scanner scanner){
 		return scanner.nextLine();
 
 	}
	
	
 }
