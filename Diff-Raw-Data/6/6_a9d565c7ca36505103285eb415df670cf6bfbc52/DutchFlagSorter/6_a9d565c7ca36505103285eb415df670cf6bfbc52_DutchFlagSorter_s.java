 /**
  * @author Matt Dole
  * @author Tim Youtz
  *
  * Note: Help received from Mira Hall regarding syntax for testing arrays
  */
 
 package edu.grinnell.CSC207.dolematt.hw6;
 
 public class DutchFlagSorter {
     public static void sort(String[] flag) throws Exception {
 	if (flag.length == 0) {
 	    throw new Exception("Sort cannot be applied to an empty array");
 	} // if
 	if (flag.length == 1)
 	    return;
 	int white = 0;
 	int red = 0;
 	int blue = flag.length - 1;
 	String temp1 = "";
 	String temp2 = "";
 	while (flag[blue].compareToIgnoreCase("blue") == 0 && blue > 0) {
 	    blue--;
 	} // while
 	while (white <= blue) {
 	    // check if an element is red
 	    if (flag[white].compareToIgnoreCase("red") == 0) {
 		temp1 = flag[white];
 		temp2 = flag[red];
 		flag[red] = temp1;
 		flag[white] = temp2;
 		red++;
 	    }
 	    // check if an element is blue
 	    else if (flag[white].compareToIgnoreCase("blue") == 0) {
 		temp1 = flag[white];
 		temp2 = flag[blue];
 		flag[blue] = temp1;
 		flag[white] = temp2;
 		blue--;
 	    }
	    // if it's not red or blue, check if it's not white and throw an
 	    // exception if it isn't
 	    else if (flag[white].compareToIgnoreCase("white") != 0) {
 		throw new Exception(
 			"Array contains values other than \"white\", \"red\", and \"blue\"");
 	    }
 	    white++;
 	}// while
     }// sort
}// DutchFlagSorter
