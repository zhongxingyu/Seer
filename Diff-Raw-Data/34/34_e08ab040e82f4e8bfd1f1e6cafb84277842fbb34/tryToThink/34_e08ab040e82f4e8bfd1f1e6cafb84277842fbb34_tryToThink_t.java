 import java.util.Scanner;
 import java.io.*;
 
 public class tryToThink{
 
 	int lines;
 	String[] line;
 
 	public tryToThink(String[] input) {
 		line = input;
 		lines = line.length;
 	}
 
 	public String calc() {
 		String str = "";
 		int tmp;
 		String tmpStr;
		int count = 1;
		int second = 0;
 		for (int i = 0; i < lines; i++) {
 
 			if (line[i].indexOf("throw") >= 0) {
 				tmp = line[i].indexOf("(");
 				tmpStr = line[i].substring(tmp + 1, line[i].indexOf(")",tmp+1));
 				tmpStr = tmpStr.trim();
 //System.out.println(tmpStr);
 				i+=1;
 				while (i<lines) {
 					if (line[i].indexOf("catch") >= 0 && line[i].indexOf(tmpStr) >=0 && line[i].indexOf(tmpStr) < line[i].indexOf(",") ) {
						//System.out.println("Count " + count + " tmpStr " + line[i].indexOf(tmpStr) + " ;;; ',' " + line[i].indexOf(","));
						tmp = line[i].indexOf("\"");
						return line[i].substring(tmp + 1, line[i].indexOf("\"",tmp+1));
					}
					else if (line[i].indexOf("try") >= 0) { 
						//System.out.println("-----1");
						for (int j=i; count > 0 || second > 0; j++) {
							//System.out.println("-----");
							count = 0;
							if (line[j].indexOf("try") >= 0)
								second++;
							else if (line[j].indexOf("catch") >= 0)
								second--;
							i=j;
 						}
						count = 1;
					}
					i+=1;
 				}
 			}
 		}
 
 		return "Unhandled Exception";
 	}
 
 	public static void main(String[] args) {
 		Scanner in = new Scanner(System.in);
 		int lines = Integer.parseInt(in.nextLine());
 
 		String[] line = new String[lines];
 		for (int i = 0; i < lines; i++) {
 			line[i] = in.nextLine();
 			//System.out.println(line[i]);
 		}
 
 
 		//String str = "           catch     ( BE,   \"BE in lin    e 3        \"    )";
 
 		//System.out.println(str.replaceAll("\\s+",""));
 		tryToThink test = new tryToThink(line);
 
 		System.out.println(test.calc());
 
 		return;
 	}
 }
