 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 public class Reader {
 	private ArrayList<String[]> store = new ArrayList<String[]>();
 
 	public Reader(String fileName) throws IOException {
 		BufferedReader CSVFile = new BufferedReader(new FileReader(fileName));
 		String dataRow = CSVFile.readLine();
 		while (dataRow != null){
 			String[] dataArray = dataRow.split(",");
 			store.add(dataArray);
 		    dataRow = CSVFile.readLine();
 	    }
 		CSVFile.close();
 	}
 	
 	public String[] getRow(int row) {
 		return store.get(row);
 	}
 	
 	public String getCell(int row, int col) {
 		return store.get(row)[col];
 	}
 	
 	public void printRow(int row) {
 		String[] dataArray = getRow(row);
 		for (String item:dataArray) {
 			System.out.print(item + "\t");
 		}
 	}
	
	private boolean detectInt(String cell) {
		return cell.matches("-?\\d+(\\.\\d+)?");
	}
 
 }
