 package fearlesscode.util;
 
 import java.io.*;
 import java.util.*;
 
 public class Diff {
 	static void diff(BufferedReader fix, BufferedReader input, FileWriter output){
 		String gap = "  ";
 		String plus = "++";
 		String minus = "--";
 
 		ArrayList<String> fixArray = new ArrayList<String>();
 		ArrayList<String> inputArray = new ArrayList<String>();
 
         try{
             for(String i = fix.readLine(); i != null; i = fix.readLine()){
                 fixArray.add(i);
             }
         } catch (Exception e){
             System.out.println("Error during opening the fix input file.");
         }
 
         try{
             for(String i = input.readLine(); i != null; i = input.readLine()){
                 inputArray.add(i);
             }
         } catch (Exception e){
             System.out.println("Error during opening the program's input file.");
         }
 
         ArrayList<String> pairs = Diff.findPairs(fixArray, inputArray);
         ArrayList<String> minusArray = new ArrayList<String>();
         ArrayList<String> plusArray = new ArrayList<String>();
 
         int x = 0;
         int y = 0;
         for(int j = 0; j < pairs.size(); j++){
             for(; x < fixArray.size(); x++){
                 if(pairs.get(j).equalsIgnoreCase(fixArray.get(x))){
                     Diff.writeToFileWriter(gap + pairs.get(j), output);
                     x++;
                     break;
                 } else {
                     Diff.writeToFileWriter(minus + fixArray.get(x), output);
                 }
             }
 
             for(; y < inputArray.size(); x++){
                 if(pairs.get(j).equalsIgnoreCase(inputArray.get(y))){
                     y++;
                     break;
                 } else {
                     Diff.writeToFileWriter(plus + inputArray.get(y), output);
                 }
             }
         }
     }
 
 	private static ArrayList<String> findPairs(ArrayList<String> fix, ArrayList<String> input){
 		ArrayList<String> pairs = new ArrayList<String>();
 
 		for(int i = 0; i < fix.size(); i++){
 			for(int j = 0; j < input.size(); j++){
 				if(fix.get(i).equalsIgnoreCase(input.get(j))){
 					pairs.add(fix.get(i));
 					j = input.size();
 				}
 			}
 		}
 		
 		return pairs;
 	}
 
 	private static void writeToFileWriter(String string, FileWriter output){
        PrintWriter out = new PrintWriter(output);

         try{
 			out.println(string);
 		} catch (Exception e){
			System.err.println("Error during printing to output.");
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		File result=new File("./result.txt");
 		result.delete();
 		try
 		{
 			FileWriter o=new FileWriter("./result.txt");
 			File out=new File("./out/");
 			File[] list=out.listFiles();
 			for(File file:list)
 			{
 				File fix=new File("./maps/"+file.getName());
 				System.out.println(file.getName());
 				try
 				{
                     BufferedReader fixBr = new BufferedReader(new FileReader(fix));
 
                     BufferedReader fileBr = new BufferedReader(new FileReader(file));
 
 					diff(fixBr, fileBr, new FileWriter("./result.txt", true));
 					o.write(file.getName());
 					o.write(fix.length()+"-"+file.length()+":"+((fix.length() == file.length())?"OK":"FAIL"));
 					System.out.print(file.getName()+": ");
 					System.out.println(fix.length()+"-"+file.length()+":"+((fix.length() == file.length())?"OK":"FAIL"));
 				}
 				catch(Exception e)
 				{
 					System.out.println("Hiba...");
 					e.printStackTrace();
 				}
 			}
 		}
 		catch(Exception e)
 		{
 
 		}
 	}
 }
