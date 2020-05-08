 package project;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 	    try
 	    {
 	    	System.out.println("Balance");
 	        String[][] balanceOfPaymentsRawData = CSVFileReader.read("Files/Project/DataSets/Balance_of_payments_annual.csv", false);
 	        System.out.println("UE");
     		String[][] unemploymentRawData = CSVFileReader.read("Files/Project/DataSets/Unemployment_rate_annual.csv", false);
     		System.out.println("GDP");
     		String[][] GDPRawData = CSVFileReader.read("Files/Project/DataSets/Euro_per_inhabitant.csv", false);
     		System.out.println("Pop");
     		String[][] populationRawData = CSVFileReader.read("Files/Project/DataSets/Population_Annual.csv", false);
     		
     		String[][] balanceOfPaymentsFlagged = getEntriesWithFlags(balanceOfPaymentsRawData, 7);
     		String[][] unemploymentFlagged = getEntriesWithFlags(unemploymentRawData, 6);
     		String[][] GDPFlagged = getEntriesWithFlags(GDPRawData, 5);
     		String[][] populationFlagged = getEntriesWithFlags(populationRawData, 5);
 
     		CsvWriter.writeDataToFile(balanceOfPaymentsFlagged, "Files/Project/DataSets/Flagged/balance_flagged.csv");
     		CsvWriter.writeDataToFile(unemploymentFlagged, "Files/Project/DataSets/Flagged/unemployment_flagged.csv");
     		CsvWriter.writeDataToFile(GDPFlagged, "Files/Project/DataSets/Flagged/GDP_flagged.csv");
     		CsvWriter.writeDataToFile(populationFlagged, "Files/Project/DataSets/Flagged/population_flagged.csv");
     		
     		int[] matchColumns = {0, 1};
     		int[] balanceOfPaymentsColumns = {6};
     		int[] unemplomentColumns = {5};
     		int[] GDPColumns = {4};
     		int[] populationColumns = {4};
     		
     		String[][] data = DataSetHelpers.combineDataSets(unemploymentRawData, unemplomentColumns, matchColumns, balanceOfPaymentsRawData, balanceOfPaymentsColumns, matchColumns);
     		int[] columnsToKeep1 = {2, 3};
     		data = DataSetHelpers.combineDataSets(data, columnsToKeep1, matchColumns, GDPRawData, GDPColumns, matchColumns);
     		int[] columnsToKeep2 = {2, 3, 4};
     		data = DataSetHelpers.combineDataSets(data, columnsToKeep2, matchColumns, populationRawData, populationColumns, matchColumns); 
 			
     		data = removeSpaces(data);
     		data = replace(data, "\":\"", "\"?\"");
 
     		// Normalizes data
     		/*DataSetHelpers.NormalizeDataset(data, 1, 0, 2, 0, 10);
     		DataSetHelpers.NormalizeDataset(data, 1, 0, 3, 0, 10);
     		DataSetHelpers.NormalizeDataset(data, 1, 0, 4, 0, 10);
     		DataSetHelpers.NormalizeDataset(data, 1, 0, 5, 0, 10);
 
         	for (String[] d : data)
         	{
         		System.out.println(Arrays.toString(d));
         	}*/
     		
     		WekaWriter.writeDataToFile(data, "datafile");
 	    }
 		catch (IOException e)
 		{
 		    System.out.println(e.getMessage());
 		}
 	}
 	
 	private static String[][] removeSpaces(String[][] data)
 	{
 		for(int i = 0; i < data.length; i++)
 		{
 			for(int j = 0; j < data[i].length; j++)
 			{
 				data[i][j] = data[i][j].replaceAll(" ", "");
 			}
 		}
 		
 		return data;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param data
 	 * @param stringToReplace
 	 * @param stringToInsert The string to insert
 	 * @return The updated data set
 	 */
 	private static String[][] replace(String[][] data, String stringToReplace, String stringToInsert)
 	{
 		for(int i = 0; i < data.length; i++)
 		{
 			for(int j = 0; j < data[i].length; j++)
 			{
 				if(data[i][j].equals(stringToReplace))
 				{
 					data[i][j] = stringToInsert;
 				}
 			}
 		}
 		
 		return data;
 	}
 	
 	private static String[][] getEntriesWithFlags(String[][] data, int flagColumn)
 	{
 		String[][] temp = new String[data.length][3];
 		for(int i = 0; i < data.length; i++)
 		{
 			temp[i][0] = data[i][0];
 			temp[i][1] = data[i][1];
 			temp[i][2] = data[i][flagColumn];
 		}
 		
 		List<Integer> flagList = new ArrayList<Integer>();
 		for(int i = 0; i < temp.length; i++)
 		{
 			if(!temp[i][2].equals("\"\""))
 			{
 				System.out.println(temp[i][2]);
 				flagList.add(i);
 			}
 			
 		}
 		
 		int flSize = flagList.size();
 		
 		String[][] rarray = new String[flSize][3]; 
 		
 		for(int i = 0; i < flSize; i++)
 		{
 			rarray[i][0] = temp[flagList.get(i)][0];
 			rarray[i][1] = temp[flagList.get(i)][1];
 			rarray[i][2] = temp[flagList.get(i)][2];
 		}
 		
 		return rarray;
 	}
 }
