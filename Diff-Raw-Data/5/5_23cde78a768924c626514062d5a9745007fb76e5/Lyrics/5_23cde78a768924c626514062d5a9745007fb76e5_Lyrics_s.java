 /* CPAC I, Evan Korth
  * Assignment #0 (from Recitation, jared.wyatt@nyu.edu)
  * 12 Sep 12
  * Victor (Ben) Turner, vt520@nyu.edu, N15271750
 
  * Pretty-print out lyrics to a song.  printSongLyrics() takes lyric
  * constant string array as a parameter.
 */
 
 public class Lyrics {
 
 	// Add new lyrics into an array like this:
	public static final String[] TWISTANDSHOUT = new String[] {
 		"The Beatles - \"Twist and Shout\"",
 		"",
 		"Well, shake it up, baby, now",
 		"(Shake it up, baby)",
 		"Twist and shout",
 		"(Twist and shout)",
 		"",
 		"C'mon, c'mon, c'mon, c'mon, baby, now",
 		"(Come on baby)",
 		"Come on and work it on out",
 		"(Work it on out)",
 		"",
 		"Well, work it on out, honey",
 		"(Work it on out)",
 		"You know you look so good",
 		"(Look so good)",
 		"",
 		"etc."
 	};
 
 	// Prints out the passed-in lyrics.
 	public static void printSongLyrics(String[] lyrics) {
 		for (int i=0; i<lyrics.length; i++) {
 			System.out.println(lyrics[i]);
 		}
 	}
 
 	public static void main(String args[]) {
		printSongLyrics(TWISTANDSHOUT);
 	}
 
 }
