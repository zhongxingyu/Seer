 package gov.nih.nci.iso21090.hibernate.node;
 
 import java.util.Comparator;
 
 public class NodeNameComparator implements Comparator<Node> {
 
 	public int compare(Node o1, Node o2) {
 
		int number1 = Integer.valueOf(o1.getName().substring(5));
		int number2 = Integer.valueOf(o2.getName().substring(5));
		if (number1 > number2)
			return 1;
		else if (number1 < number2)
			return -1;
		else
			return 0;
 	}
 
 }
