 import java.util.Scanner;
 
 /**
 * @FIXME This code doesn't produce the correct output
  * Problem C. Perfect Harmony
  * http://code.google.com/codejam/contest/dashboard?c=1128486#s=p2
  * 
  *        Problem
  * 
  *        Jeff is a part of the great Atlantean orchestra. Each player of the
  *        orchestra has already decided what sound will he play (for the sake of
  *        simplicity we assume each player plays only one sound). We say two
  *        sounds are in harmony if the frequency of any one of them divides the
  *        frequency of the other (that's a pretty restrictive idea of harmony,
  *        but the Atlanteans are known to be very conservative in music). Jeff
  *        knows that the notes played by other players are not necessarily in
  *        harmony with each other. He wants his own note to improve the
  *        symphony, so he wants to choose his note so that it is in harmony with
  *        the notes all the other players play.
  * 
  *        Now, this sounds simple (as all the frequencies are positive integers,
  *        it would be enough for Jeff to play the note with frequency 1, or,
  *        from the other side, the Least Common Multiple of all the other
  *        notes), but unfortunately Jeff's instrument has only a limited range
  *        of notes available. Help Jeff find out if playing a note harmonious
  *        with all others is possible.
  * 
  *        Input
  * 
  *        The first line of the input gives the number of test cases, T. T test
  *        cases follow. Each test case is described by two lines. The first
  *        contains three numbers: N, L and H, denoting the number of other
  *        players, the lowest and the highest note Jeff's instrument can play.
  *        The second line contains N integers denoting the frequencies of notes
  *        played by the other players.
  * 
  *        Output
  * 
  *        For each test case, output one line containing "Case #x: y", where x
  *        is the case number (starting from 1) and y is either the string "NO"
  *        (if Jeff cannot play an appropriate note), or a possible frequency. If
  *        there are multiple frequencies Jeff could play, output the lowest one.
  * 
  *        Limits
  * 
  *        1 ≤ T ≤ 40.
  * 
  *        Small dataset
  * 
  *        1 ≤ N ≤ 100. 
  *        1 ≤ L ≤ H ≤ 10000. 
  *        All the frequencies are no larger than 10000.
  * 
  *        Large dataset
  * 
  *        1 ≤ N ≤ 10^4. 
  *        1 ≤ L ≤ H ≤ 10^16 
  *        All the frequencies are no larger than 10^16
  * 
  *        Sample
  * 
  *        Input 
  *        2 
  *        3 2 100
  *        3 5 7
  *        4 8 16
  *        1 20 5 2
  * 
  *        Output 
  *        Case #1: NO 
  *        Case #2: 10
  * 
  * @author Felipe Ribeiro <felipernb@gmail.com>
  */
 public class PerfectHarmony {
 	static int[] freqs;
 	
 	public static void main(String args[]) {
 		Scanner in = new Scanner(System.in);
 		int t = in.nextInt();
 		for (int i = 1; i <= t; i++) {
 			int n = in.nextInt();
 			int l = in.nextInt();
 			int h = in.nextInt();
 			freqs = new int[n];
 			for (int j = 0; j < n; j++) {
 				freqs[j] = in.nextInt();
 			}
 			System.out.print("Case #"+i+": ");
 			try {
 				System.out.println(findFrequency(l, h));
 			} catch (Exception e) {
 				System.out.println("NO");
 			}
 		}
 	}
 	private static int findFrequency(int l, int h) throws Exception {
 		boolean isInHarmony = false;
 		
		for(int i = l; i < h; i++) {
 			for(int f: freqs) {
 				isInHarmony = f % i == 0 || i % f == 0;
 				if(!isInHarmony) break;
 			}
 			if (isInHarmony) return i;
 		}
 		throw new Exception("Impossible");
 	}
 }
