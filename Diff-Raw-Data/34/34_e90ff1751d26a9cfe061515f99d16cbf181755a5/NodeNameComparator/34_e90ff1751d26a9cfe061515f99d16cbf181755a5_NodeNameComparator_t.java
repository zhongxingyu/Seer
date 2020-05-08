 package gov.nih.nci.iso21090.hibernate.node;
 
 import java.util.Comparator;
 
 public class NodeNameComparator implements Comparator<Node> {
 
 	public int compare(Node o1, Node o2) {
 
		if( (o1 !=null && o1.getName()!=null  ) && (o2 !=null && o2.getName()!=null ) ){
			String o1Name = o1.getName();
			String o2Name = o2.getName();
			
			o1Name = o1Name.replace('[','_');
			o2Name = o2Name.replace('[','_');
			
			if(o1Name.endsWith("]")) o1Name = o1Name.substring(0,o1Name.length()-1);
			if(o2Name.endsWith("]")) o2Name = o2Name.substring(0,o2Name.length()-1);
			
			if(o1Name.startsWith("part_") && o2Name.startsWith("part_") ){
				int number1 = Integer.valueOf(o1Name.substring(5));
				int number2 = Integer.valueOf(o2Name.substring(5));
				if (number1 > number2)
					return 1;
				else if (number1 < number2)
					return -1;
				else
					return 0;	
			}else
				return 0;
			
		}else{
			return o1.compareTo(o2);
			
		}
 	}
 
 }
