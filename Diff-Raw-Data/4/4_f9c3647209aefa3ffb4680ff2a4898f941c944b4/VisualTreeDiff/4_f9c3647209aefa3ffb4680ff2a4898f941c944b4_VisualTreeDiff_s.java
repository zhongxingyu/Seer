 package ml;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 
 class PimmxlPrinter {
 	public static Set<UnorderedPair<ANode, ANode>> auxSet;
 
 	static void printIndent( int indent, PrintStream s) {
     	for( int i = 0; i < indent; i++ ) {
     		
     		s.print( "  " );
     	}
     
     }
     static void printCladeTags( int indent, boolean open, PrintStream s ) {
     	printIndent(indent, s);
     	if( open ) {
     		s.println( "<clade>" );
     	} else {
     		s.println( "</clade>");
     	}
     }
 
     static void printColor( int indent, int[]c, PrintStream s ) {
     	printIndent(indent, s);
     	s.println( "<color>");
     	printIndent(indent+1, s);
     	s.printf( "<red>%d</red>\n", c[0]);
     	printIndent(indent+1, s);
     	s.printf( "<green>%d</green>\n", c[1]);
     	printIndent(indent+1, s);
     	s.printf( "<blue>%d</blue>\n", c[2]);
     	
     	printIndent(indent, s);
     	s.println( "</color>");
     	
     	
     }
     
     static void printClade( int indent, LN n, PrintStream s ) {
     	printCladeTags(indent, true, s);
     	
     	indent++;
     	
     	int[] color = {255, 255, 255};
     	if( !PimmxlPrinter.auxSet.contains(new UnorderedPair<ANode, ANode>(n.back.data, n.data))) {
     		color[1] = 0;
     		color[2] = 0;
     		
     	}
     	
     	if( n.data.isTip ) {
     		printIndent(indent, s);
         	s.printf( "<name>%s</name>\n", n.data.getTipName());	
         	printIndent(indent, s);
         	s.printf( "<branch_length>%f</branch_length>\n", n.backLen );
         	printColor( indent, color, s );
         			
     	} else {
     		assert( n.next.back != null && n.next.next.back != null );
     		printIndent(indent, s);
         	s.printf( "<branch_length>%f</branch_length>\n", n.backLen );
         	printColor( indent, color, s );
         	
     		printClade( indent + 1, n.next.back, s);
     		printClade( indent + 1, n.next.next.back, s);
     		
     		
     	}
     	
     	printCladeTags(indent-1, false, s);
     }
     
     static void printPhyloxml( LN node, PrintStream s ) {
     	if( node.data.isTip ) {
     		if( node.back != null ) {
     			node = node.back;
     		} else if( node.next.back != null ) {
     			node = node.next.back;
     		} else if( node.next.next.back != null ) {
     			node = node.next.next.back;
     		} else {
     			throw new RuntimeException( "can not print single unlinked node");
     		}
     		
     		if( node.data.isTip ) {
     			throw new RuntimeException( "could not find non-tip node for writing the three (this is a braindead limitation of this tree printer!)");
     		}
     	}
     	
     	s.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
     	s.println( "<phyloxml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd\" xmlns=\"http://www.phyloxml.org\">" );
     	s.println( "<phylogeny rooted=\"false\">");
     	
     	int indent = 1;
     	
     	printCladeTags(indent, true, s);
     	
     	printClade( indent + 1, node.back, s );
     	printClade( indent + 1, node.next.back, s );
     	printClade( indent + 1, node.next.next.back, s );
     	
     	printCladeTags(indent, false, s);
     	s.println("</phylogeny>\n</phyloxml>");
     }	
 
 }
 
 public class VisualTreeDiff {
 	public static void main(String[] args) {
 		File t1_name = new File( args[0] );
 		File t2_name = new File( args[1] );
 		
 		LN t1 = TreeParser.parse(t1_name);
 		LN t2 = TreeParser.parse(t2_name);
 		
 		final String[] t1_tips;
 		LN[] t1_list = LN.getAsList(t1);
 		LN[] t2_list = LN.getAsList(t2);
 		
 		
 		{
 			
 			
 			Set<String> t1_tipset = LN.getTipSet(t1_list);
 			Set<String> t2_tipset = LN.getTipSet(t2_list);
 			
 			
 			final boolean c12 = t1_tipset.containsAll(t2_tipset);
 			final boolean c21 = t2_tipset.containsAll(t1_tipset);
 			
 			System.err.printf( "equal: %s\n", (c12 && c21) ? "true" : "false" );
 			
 			
 			t1_tips = t1_tipset.toArray(new String[t1_tipset.size()]);
 			Arrays.sort(t1_tips);
 		}
 
 		LN[][] t1_br = LN.getAllBranchList3(t1);
 		LN[][] t2_br = LN.getAllBranchList3(t2);
 		
 		System.err.printf( "br: %d %d\n", t1_br.length, t2_br.length );
 		
 		assert( t1_br.length == t2_br.length );
 		
 		BitSet[] t1_splits = new BitSet[t1_br.length];
 		BitSet[] t2_splits = new BitSet[t2_br.length];
 		
 		for( int i = 0; i < t1_br.length; i++ ) {
 			t1_splits[i] = splitToBitset( LN.getSmallerSplitSet(t1_br[i]), t1_tips);
 			t2_splits[i] = splitToBitset( LN.getSmallerSplitSet(t2_br[i]), t1_tips);
 		}
 
 		Set<UnorderedPair<ANode, ANode>> branchFound = new HashSet<UnorderedPair<ANode, ANode>>(); 
 		
 		int nFound = 0;
 		for( int i = 0; i < t1_splits.length; i++ ) {
 			boolean found = false;
 			
 			for( int j = 0; j < t2_splits.length; j++ ) {
 				if( t1_splits[i].equals(t2_splits[j]) ) {
 					nFound++;
 					found = true;
 					break;
 				
 				}
 			}
 			if( found ) {
 				branchFound.add( new UnorderedPair<ANode, ANode>(t1_br[i][0].data, t1_br[i][1].data));
 			}
 		}
 			
 		System.err.printf( "nFound: %d\n", nFound );
 		
 		
 		PimmxlPrinter.auxSet = branchFound;
 		PimmxlPrinter.printPhyloxml(t1, System.out);
 		
 		
 	}
 
 	private static BitSet splitToBitset(String[] splitSet,
 			String[] refOrder ) {
 		Arrays.sort( splitSet );
 		
 		BitSet bs = new BitSet(refOrder.length);
 		
 		for( int i = 0, j = 0; i < refOrder.length && j < splitSet.length; ++i ) {
 			if( refOrder[i].equals(splitSet[j] )) {
 				bs.set(i);
 				j++;
 			}
 		}
 		
 		return bs;
 	}
  }
