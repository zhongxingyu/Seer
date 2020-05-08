 package interpreter;
 import tokenizer.NFA;
 import tokenizer.Token;
 import tokenizer.Tokenizer;
 
 import java.io.*;
 import java.nio.channels.*;
 import java.nio.charset.Charset;
 import java.nio.*;
 import java.util.*;
 
 /**
  * 
  * Currently performs find and replace operations on text files. Needs to be
  * expanded to walk the AST tree
  *
  */
 public class Evaluator {
 
     HashMap<String, Object> symbolTable;
 
     public Evaluator() {
         symbolTable = new HashMap<String, Object>();
     }
 
     /**
      * Evaluates the AST.
      * @param head the head
      * @return the result of the evaluation
      */
     public Object eval(SyntaxTreeNode head) {
 
         String nodeType = head.nodeType;
         String nodeId = head.id;
         ArrayList<SyntaxTreeNode> children = head.children;
         int count = 0;
 
         if (nodeType == null) {
             return head.value;
         }
         if (nodeType.equals("MiniRE-program")) {
             eval(children.get(1));
         } else if (nodeType.equals("STATEMENT-LIST")) {
             eval(children.get(0));
             eval(children.get(1));
         } else if (nodeType.equals("STATEMENT-LIST-TAIL")) {
             for (SyntaxTreeNode child : children) {
                 eval(child);
             }
         } else if (nodeType.equals("STATEMENT")) {
             if (children.get(0).id.equals("$REPLACE")) {
                 String regex = children.get(1).value;
                 String asciiStr = children.get(3).value;
                 ArrayList<String> fileNames = (ArrayList<String>) eval(children.get(5));
                 return replace(regex, asciiStr, fileNames.get(0), fileNames.get(1));
             } else if (children.get(0).id.equals("$RECREP")) {
                 String regex = children.get(1).value;
                 String asciiStr = children.get(3).value;
                 ArrayList<String> fileNames = (ArrayList<String>) eval(children.get(5));
                 recursivereplace(regex, asciiStr, fileNames.get(0), fileNames.get(1));
             } else if (children.get(0).id.equals("$ID")) {
                 String idName = children.get(0).value;
                 Object idValue = eval(children.get(2));
                 symbolTable.put(idName, idValue);
             } else if (children.get(0).id.equals("$PRINT")) {
                 SyntaxTreeNode exp_list = children.get(2);
                 try {
                     StringList exp = (StringList) eval(exp_list.children.get(0));
                     if (exp.length() > 0)
                         System.out.println(exp.toString());
                     else
                         System.out.println("[]");
                 } catch (ClassCastException cce) {
                     Integer exp  = (Integer) eval(exp_list.children.get(0));
                     System.out.println(exp);
                 }
                 SyntaxTreeNode exp_list_tail = exp_list.children.get(1);
                 while (exp_list_tail.children.get(0).nodeType == null) {
                     try {
                         StringList exp = (StringList) eval(exp_list_tail.children.get(1));
                         if (exp.length() > 0)
                             System.out.println(exp.toString());
                         else
                             System.out.println("[]");
                     } catch (ClassCastException cce) {
                         Integer exp  = (Integer) eval(exp_list_tail.children.get(1));
                         System.out.println(exp);
                     }
                     exp_list_tail = exp_list_tail.children.get(2);
                 }
             }
         } else if (nodeType.equals("STATEMENT-RIGHTHAND")) {
             if (children.get(0).nodeType != null && children.get(0).nodeType.equals("EXP")) {
                 return eval(children.get(0));
             } else if (children.get(0).id.equals("$HASH")) {
                 return ((StringList) eval(children.get(1))).length();
             } else if (children.get(0).id.equals("$MAXFREQSTRING")) {
                 return ((StringList) symbolTable.get(children.get(2).value)).maxfreqstring();
             }
         } else if (nodeType.equals("FILE-NAMES")) {
             ArrayList<String> fileNames = new ArrayList<String>();
             fileNames.add((String) eval(children.get(0)));
             fileNames.add((String) eval(children.get(2)));
             return fileNames;
         } else if (nodeType.equals("SOURCE-FILE")) {
             return children.get(0).value;
         } else if (nodeType.equals("DESTINATION-FILE")) {
             return children.get(0).value;
         } else if (nodeType.equals("EXP-LIST-TAIL")) {
             if (children.get(0).nodeType.equals("EPSILON")) {
                 return null;
             } else {
                 return StringList.union((StringList) eval(children.get(1)), (StringList) eval(children.get(2)));
             }
         } else if (nodeType.equals("EXP")) {
             if (children.get(0).id != null && children.get(0).id.equals("$ID")) {
                 if (symbolTable.containsKey(children.get(0).value)) {
                     return symbolTable.get(children.get(0).value);
                 } else {
                     return children.get(0).value;
                 }
             } else if (children.get(0).id != null && children.get(0).id.equals("$LPAREN")) {
                 return eval(children.get(2));
             } else if (children.get(0).nodeType != null && children.get(0).nodeType.equals("TERM")) {
                 if (children.get(1).children.get(0).nodeType.equals("EPSILON")) {
                     return eval(children.get(0));
                 } else {
                     StringList result = (StringList) eval(children.get(0));
                     SyntaxTreeNode exp_list = children.get(1);
                     while (!exp_list.children.get(0).nodeType.equals("EPSILON")) {
                         if (exp_list.children.get(0).children.get(0).id.equals("$DIFF")) {
                             result = StringList.diff(result, (StringList) eval(exp_list.children.get(1)));
                         } else if (exp_list.children.get(0).children.get(0).id.equals("$UNION")) {
                             result = StringList.union(result, (StringList) eval(exp_list.children.get(1)));
                         } else if (exp_list.children.get(0).children.get(0).id.equals("$INTERS")) {
                             result = StringList.inters(result, (StringList) eval(exp_list.children.get(1)));
                         }
                         exp_list = exp_list.children.get(2);
                     }
                     return result;
                 }
             }
         } else if (nodeType.equals("TERM")) {
             String fileName = (String) eval(children.get(3));
             String regex = (String) eval(children.get(1));
             return find(regex, fileName);
         } else if (nodeType.equals("FILE-NAME")) {
             return (String) children.get(0).value;
         } else {
             System.out.println("Finished evaluation.");
         }
         return null;
     }
 
     // TODO
     public static String maxFreqString(StringList list) {
         //String mostFrequentString = "";
         return list.maxfreqstring();
     }
 
 	/**
      * Finds all strings in the given file that match the given regex, and
      * returns them in a StringList
      * 
      * ASSUMES REGEX HAS BEEN STRIPPED OF SURROUNDING QUOTES
      *
      * @param regex a Regular expression, as a string
      * @param filename a file to scan for strings matching regex
      * @param return a StringList containing all matching strings and their
      *          metadata
      */
     public static StringList find(String regex, String filename){
        filename = filename.replaceAll("\"", "");
        regex = regex.replaceAll("'", "");
        System.out.println(filename);
 		Tokenizer d = new Tokenizer("id", regex, filename);
 		d.generateTokens();
 
 		return StringList.toStringList(d.getTokens());
 	}
 
 	/**
      * Replaces all strings in the given file1 that match the given regex with
      * the replacement string and prints the final output to file2. Returns true
      * if matches were found.
      * 
      * ASSUMES REGEX AND REPLACEMENT STRING HAVE BEEN STRIPPED OF SURROUNDING
      * QUOTES
      *
      * @param regex a Regular expression, as a string
      * @param replacement a string to replace everything that matches regex with
      * @param file1 a file to scan for strings matching regex
      * @param file2 a file identical to file1, but with the matches replaced
      * @return true if and only if strings matching regex were found in file1
      */
     public static boolean replace(String regex, String replacement,
         String file1, String file2) {
        regex = regex.replaceAll("'", "");
        file1 = file1.replace("\"", "");
        file2 = file2.replace("\"", "");
 
 		String fileText = "";
 
         if (file1.equals(file2)) {
             System.out.println("Error: input and output file are the same in:\n"
                 + "replace \'" + regex + "\' with \"" + replacement + "\" in "
                 + file1 + " >! " + file2);
             return false;
         }
 
 		String[] matches = find(regex, file1).strings();
 
 		if (matches.length < 1) {
 			return false;
 		}
 
         fileText = getFileText(file1);
 		  
 		for (int i=0; i < matches.length; i++){
 			fileText = fileText.replace(matches[i], replacement);
 		}
         return writeFile(fileText, file2);
 	}
 	
 	/**
      * Will recursively replace all strings in the given file1 that match the
      * given regex with the replacement string and print the final output to
      * file2.
      * 
      * ASSUMES REGEX AND REPLACEMENT STRING HAVE BEEN STRIPPED OF SURROUNDING
      * QUOTES
      *
      * @param regex a Regular expression, as a string
      * @param replacement a string to recursively replace everything that
      *          matches regex with
      * @param file1 a file to scan for strings matching regex
      * @param file2 a file identical to file1, but with all matches replaced
      */
     public static void recursivereplace(String regex, String replacement,
         String filename1, String filename2) {
 
         // Check that filename 1 and filename2 are distinct
 
         if (filename1.equals(filename2)) {
             System.out.println("Error: input and output file are the same in:\n"
                 + "recursivereplace \'" + regex + "\' with \"" + replacement +
                 "\" in " + filename1 + " >! " + filename2);
             return;
         }
         
         // Prevent infinite recursion
 
         NFA nfa = new NFA(regex);
         String sub = replacement;
         
         while (sub.length() > 0) {
             if (nfa.testString(nfa.automata(), sub)) {
                 // throw some kind of error - the recursion is infinite
                 System.out.println("Error: infinite loop inevitable at:\n" +
                     "recursivereplace '" + regex + "' with \"" + replacement +
                     "\" in " + filename1 + " >! " + filename2);
                 return;
             }
             sub = sub.substring(0, sub.length()-1);
         }
         
 		// Recursively replace until no more changes can be made
 
         String tempFilename1;
         
         try {
             File file = File.createTempFile("MRE", ".txt", new File(System.getProperty("user.dir")));
             file.deleteOnExit();
             tempFilename1 = file.getName();
         } catch (IOException e) {
             System.out.println("Unexpected IOException");
             e.printStackTrace();
             return;
         }
         Evaluator.replace(regex, replacement, filename1, filename2);
 
         // I put this check below replace to ensure the output file is created       
         if (regex.equals("") && replacement.equals("")) {
             // throw some kind of error - the recursion is infinite
             System.out.println("Error: infinite loop inevitable at:\n" +
                 "recursivereplace '" + regex + "' with \"" + replacement +
                 "\" in " + filename1 + " >! " + filename2);
             return;
         }
 
         while (Evaluator.replace(regex, replacement, filename2, tempFilename1)) {
             if (!Evaluator.replace(regex, replacement, tempFilename1, filename2)) {
                 // Copy 1 to 2 and be done
                 copy(tempFilename1, filename2);
                 break;
             }
         }
 	}
 
 	/**
      * Copies the contents of srcFile to a file named dstFile
      * Creates dstFile if it doesn't exist; overwrites it if it does
      * Returns true if and only if the copy was successful
      *
      * @param srcFile the file whose contents will be copied
      * @param dstFile the file to copy to
      * @return true if and only if the copy was successful
      */
     private static boolean copy(String srcFile, String dstFile) {
         return writeFile(getFileText(srcFile), dstFile);
     }
 
     /**
      * Returns the contents of src
      *
      * @param src the file whose contents will be returned
      * @return the contents of src
      */
     private static String getFileText(String src) {
         String fileText = "";
 
         try {
             FileInputStream stream = new FileInputStream(new File(src));
             FileChannel fc = stream.getChannel();
             MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             fileText = Charset.defaultCharset().decode(mbb).toString();
         } catch (FileNotFoundException e) {
             System.out.println("File " + src + " not found");
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Unexpected IOException");
             e.printStackTrace();
         }
         return fileText;
     }
 
     /**
      * Copies the fileText to a file named dstFile
      * Creates dstFile if it doesn't exist; overwrites it if it does
      * Returns true if and only if the copy was successful
      *
      * @param filetext the text to copy into dstFile
      * @param dstFile the file to copy to
      * @return true if and only if the copy was successful
      */
     private static boolean writeFile(String fileText, String dstFile) {
         boolean written = true;
         try {
             FileWriter fstream = new FileWriter(dstFile);
     		BufferedWriter out = new BufferedWriter(fstream);
         	out.write(fileText);
         	out.close();
         } catch (IOException e) {
             written = false;
             System.out.println("Unexpected IOException");
             e.printStackTrace();
         }
         return written;
     }
 }
