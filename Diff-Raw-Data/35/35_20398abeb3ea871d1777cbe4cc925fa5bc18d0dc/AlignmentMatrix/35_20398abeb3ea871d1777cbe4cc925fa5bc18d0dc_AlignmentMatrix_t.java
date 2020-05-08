 import java.io.PrintStream;
 public class AlignmentMatrix
 {
 	private String top;
 	private String left;
 	private AlignmentElement[][] a;
	private AlignmentElement[][] n;
	private AlignmentElement[][] w;
 	private AlignmentScoringSystem scoringSystem;
 	public AlignmentMatrix(AlignmentScoringSystem scoringSystem_, String top_, String left_)
 	{
 		scoringSystem = scoringSystem_;
 		top = top_;
 		left = left_;
		a = new AlignmentElement[left.length() + 1][];
		for (int i = 0; i <= left.length(); i++)
 		{
			a[i] = new AlignmentElement[top.length() + 1];
		}
		n = new AlignmentElement[left.length() + 1][];
		for (int i = 0; i <= left.length(); i++)
		{
			n[i] = new AlignmentElement[top.length() + 1];
		}
		w = new AlignmentElement[left.length() + 1][];
		for (int i = 0; i <= left.length(); i++)
		{
			w[i] = new AlignmentElement[top.length() + 1];
 		}
 		fillTable();
 	}
 	private void fillTable()
 	{
 		a[0][0] = new AlignmentElement(0, null);
		a[1][0] = new AlignmentElement(scoringSystem.gapStart + scoringSystem.gapContinue, PointerDirection.UP);
 		for (int i = 2; i <= top.length(); i++)
 		{
			a[i][0] = new AlignmentElement(a[i - 1][0].score + scoringSystem.gapContinue, PointerDirection.UP);
 		}
		a[0][1] = new AlignmentElement(scoringSystem.gapStart + scoringSystem.gapContinue, PointerDirection.LEFT);
 		for (int j = 2; j <= left.length(); j++)
 		{
			a[0][j] = n[0][j] = w[0][j]= new AlignmentElement(a[0][j - 1].score + scoringSystem.gapContinue, PointerDirection.LEFT);
 		}
 		for (int i = 0; i < top.length(); i++)
 		{
 			for (int j = 0; j < left.length(); j++)
 			{
 				PointerDirection dir = null;
 				int score;
 				int characterScore = (top.charAt(i) == left.charAt(j)) ? scoringSystem.match : scoringSystem.mismatch;
				// North
				//int northGapStartScore = a[]
 			}
 		}
 	}
 	public void output(PrintStream out)
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("\\begin{tabular}{|c|c");
 		for (int i = 0; i < top.length(); i++)
 		{
 			sb.append("c");
 		}
 		sb.append("|}");
 		out.println(sb.toString());
 		out.println("\\hline");
 	}
 	private class AlignmentElement
 	{
 		public final int score;
 		public final PointerDirection direction;
 		public AlignmentElement(int score_, PointerDirection direction_)
 		{
 			score = score_;
 			direction = direction_;
 		}
 	}
 }
