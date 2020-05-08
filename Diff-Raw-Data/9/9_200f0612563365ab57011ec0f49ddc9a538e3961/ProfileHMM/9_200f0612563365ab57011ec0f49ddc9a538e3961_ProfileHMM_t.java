 import java.util.*;
 import java.io.*;
 import java.lang.Math.*;
 
 class ProfileHMM {
     public HashMap<String,ProfileHMM_Node> graph; // Graph structure
     public int length;				  // equal to the number of Match states; aka alignment.n_match_cols
     public Alignment alignment;
     public BackgroundProbs bps;
 
     private static boolean debugging=false;
 
     // length is the length of strings this PHMM can profile; 
     public ProfileHMM() {
 	this.graph=new HashMap<String,ProfileHMM_Node>();
     }
 
     public void initialize(Alignment a, BackgroundProbs bps) {
 	this.graph.clear();	
 
 	this.new_node("begin", false);
 	this.new_node("end", false);
 	this.new_node("I0", false);
 	for (int i=1; i<=a.n_match_cols; i++) {
 	    boolean last=i==a.n_match_cols;
 	    this.new_node("M"+String.valueOf(i), last);
 	    this.new_node("I"+String.valueOf(i), last);
 	    this.new_node("D"+String.valueOf(i), last);
 	}
 	this.length=a.n_match_cols;
 	this.bps=bps;
     }
 
     public void new_node(String state, boolean last) {
 	ProfileHMM_Node node=new ProfileHMM_Node(state, last);
 	this.graph.put(state, node);
     }
 	
     public ProfileHMM_Node get_node(String state) throws ProfileHMM_BadStateException {
 	ProfileHMM_Node n=(ProfileHMM_Node)this.graph.get(state);
 	if (n==null) throw new ProfileHMM_BadStateException(state);
 	return n;
     }
 
     // return the transition probability from state j to state k: fixme: implement
     public double tr_pr(String j, String k) {
 	ProfileHMM_Node n=get_node(j);
 	
 	return 0;
     }
 
     // return the emission probability of char b from state j: fixme: implement
     public double em_pr(String j, char b) {
 	return 0;
     }
 
     public void train(Alignment align, BackgroundProbs bps) {
 	this.initialize(align, bps);
 	System.out.println("training on alignment...");
 	for (int r=0; r<align.height(); r++) {
 	    ProfileHMM_Node cs=get_node("begin"); // cs for "current state"
 	    char[] path=align.row(r);
 	    for (int c=0; c<path.length; c++) {
 		char aa=path[c];
 		
 		String ns="";
 		if (aa != '-') {
 		    ns=align.is_match_col(c)? "M":"I";
 		} else {	// aa=='-'
 		    if (align.is_match_col(c)) ns="D"; 
 		    else continue; // ignore '-' chars in non-match cols
 		}
 	    
 		// increment Ajk's and Eb(j)'s
 		cs.inc_tr(ns);
 		if (aa != '-') cs.inc_em(aa);
 
 		//System.out.println(String.format("a[%2d][%2d]=%c (%s)   %s ->%s", 
 		//r, c, aa, (align.is_match_col(c)? '*':' '), cs, cs.nextState(ns)));
 		cs=get_node(cs.nextState(ns));
 	    }
 	    cs.inc_tr("end");
 
 	    //System.out.println(String.format("last state: %s",  cs));
 	    //System.out.println(String.format("end state: %s\n",  get_node("end")));
 	}
 
 	// Normalize values (sort for purposes of debugging):
 	SortedSet<String> sortedKeys=new TreeSet<String>(graph.keySet());
 	Iterator it=sortedKeys.iterator();
 	System.out.println("normalizing...");
 	while (it.hasNext()) {
 	    ProfileHMM_Node s=get_node((String)it.next());
 	    s.normalize_trs();
 	    s.normalize_ems(bps);
 	}
     }
 
     double viterbi(String path) {
 	//System.out.println(String.format("\nAligning %s (%d)", path, path.length()));
 	// Allocate arrays.  
 	// Major index: We need one more element in each because we're 1-based
 	// Minor index: Again, 1-based.
 	// System.out.println(String.format("allocating %d x %d", this.length+1, path.length()+1));
 	double[][] Vm=new double[this.length+1][path.length()+1];
 	double[][] Vi=new double[this.length+1][path.length()+1];
 	double[][] Vd=new double[this.length+1][path.length()+1];
 	double[] Vend=new double[path.length()+2];
 	double[] Vbegin=new double[path.length()+1];;
 
 	// Establish basis:
 	for (int j=0; j<this.length; j++) {
 	    Vm[j][0]=Double.NEGATIVE_INFINITY;
 	    Vi[j][0]=Double.NEGATIVE_INFINITY;
 	    Vd[j][0]=Double.NEGATIVE_INFINITY;
 
 	    Vm[j][1]=Double.NEGATIVE_INFINITY;
 	    Vi[j][1]=Double.NEGATIVE_INFINITY;
 	    Vd[j][1]=Double.NEGATIVE_INFINITY;
 	}
 
 	// For states that don't exist, but have a place in the arrays due to 1-based indexing, set their values to NaN:
 	for (int i=0; i<path.length()+1; i++) {
 	    Vm[0][i]=Double.NaN;
 	    Vd[0][i]=Double.NaN;
 	}
 
 	// begin and end states:
 	for (int i=0; i<=path.length(); i++) {
 	    Vbegin[i]=Double.NEGATIVE_INFINITY;	// Vbegin gets set to 0, below
 	    Vend[i]=Double.NEGATIVE_INFINITY; // these will get overwritten, later
 	}
 	Vbegin[0]=0;
 	// System.out.println(String.format("%s:\n%s\n", "Vbegin", d1s(Vbegin, "Vbegin")));
 
 	
 	// Special recurrences that depend on start state: M1, I0, D1
 	// Also, I1, even though it doesn't depend on begin, so that the loop below can start at 2
 	// fixme: double-check this shit
 	for (int i=1; i<=path.length(); i++) { 
 	    char Xi=path.charAt(i-1); // strings are still 0-based
 	    Vm[1][i]=log2( get_node("M1").get_em(Xi) / bps.pr(Xi)) + 
 		max2( Vbegin[0] + log2(get_node("begin").get_tr("M1")),
 		      Vi[0][i] +  log2(get_node("I0").get_tr("M1"))); 
 	    
 	    Vi[0][i]=log2(get_node("I0").get_em(Xi) / bps.pr(Xi)) + 
 		max2(Vbegin[i-1] + log2(get_node("begin").get_tr("I")),
 		     Vi[0][i-1]  + log2(get_node("I0").get_tr("I")));
 
 	    Vi[1][i]=log2(get_node("I1").get_em(Xi) / bps.pr(Xi)) + 
 		max3(Vm[1][i-1] + log2(get_node("M1").get_tr("I")),
 		     Vi[1][i-1] + log2(get_node("I0").get_tr("I")),
 		     Vd[1][i-1] + log2(get_node("D1").get_tr("I")));
 
 	    Vd[1][i]=max2(Vbegin[0] + get_node("begin").get_tr("D"),
 			  Vi[0][i] + get_node("I0").get_tr("D"));
 	}
 
 	/* 
 	   System.out.println(d1s(Vm[1], "Vm[1]"));
 	   System.out.println(d1s(Vi[0], "Vi[0]"));
 	   System.out.println(d1s(Vi[1], "Vi[1]"));
 	   System.out.println(d1s(Vd[1], "Vd[1]"));
 	*/
 
        	// Basic recurrence:
 	for (int i=1; i<=path.length(); i++) { // fixme: check indexing for i, given 1-based; 
 	    char Xi=path.charAt(i-1); // strings are still 0-based
 	    double qXi=bps.pr(Xi);
 	    //System.out.println(String.format("\n%c: qXi=%g", Xi, qXi));
 
 	    for (int j=2; j<=this.length; j++) {
 		double eMj=get_node("M"+String.valueOf(j)).get_em(Xi);
 		Vm[j][i]=log2(eMj/qXi) + 
 		    max3(Vm[j-1][i-1]+log2(get_node("M"+String.valueOf(j-1)).get_tr("M")),
 			 Vi[j-1][i-1]+log2(get_node("I"+String.valueOf(j-1)).get_tr("M")),
 			 Vd[j-1][i-1]+log2(get_node("D"+String.valueOf(j-1)).get_tr("M")));
 
 		//System.out.println(String.format("eM[%d](%c)=%g\tVm[%d][%d]=%g", j, Xi, eMj, j, i, Vm[j][i]));
 
 		double eIj=get_node("I"+String.valueOf(j)).get_em(Xi);
 		Vi[j][i]=log2(eIj/qXi) + 
 		    max3(Vm[j][i-1]+log2(get_node("M"+String.valueOf(j-1)).get_tr("I")),
 			 Vi[j][i-1]+log2(get_node("I"+String.valueOf(j-1)).get_tr("I")),
 			 Vd[j][i-1]+log2(get_node("D"+String.valueOf(j-1)).get_tr("I")));
 
 		//System.out.println(String.format("eI[%d](%c)=%g\tVi[%d][%d]=%g", j, Xi, eIj, j, i, Vi[j][i]));
 
 		Vd[j][i]=
 		    max3(Vm[j-1][i]+log2(get_node("M"+String.valueOf(j-1)).get_tr("D")),
 			 Vi[j-1][i]+log2(get_node("I"+String.valueOf(j-1)).get_tr("D")),
 			 Vd[j-1][i]+log2(get_node("D"+String.valueOf(j-1)).get_tr("D")));
 
 		//System.out.println(String.format("Vd[%d][%d]=%g", j, i, Vd[j][i]));
 
 	    }
 	}
 
 	if (1==0) {
 	   System.out.println(String.format("%s:\n%s\n", "Vm",   d2s(Vm, "Vm")));
 	   System.out.println(String.format("%s:\n%s\n", "Vi",   d2s(Vi, "Vi")));
 	   System.out.println(String.format("%s:\n%s\n", "Vd",   d2s(Vd, "Vd")));
 	}
 
 	// Final recurrence: Vend[j]
 	// I don't think it makes sense to build Vend for all of 0<=i<=L+1, only L+1.  But do it anyway...
 	Vend[0]=Double.NaN;
 	int L=this.length;
 	for (int i=1; i<=path.length()+1; i++) {
 	    Vend[i]=max3(Vm[L][i-1] + log2(get_node("M"+String.valueOf(L)).get_tr("end")),
 			 Vi[L][i-1] + log2(get_node("I"+String.valueOf(L)).get_tr("end")),
 			 Vd[L][i-1] + log2(get_node("D"+String.valueOf(L)).get_tr("end")));
 	}
 	if (debugging) System.out.println(String.format("%s:\n%s\n", "Vend", d1s(Vend, "Vend")));
 	
 	return Vend[path.length()+1];	// fixme; check index
     }
     
 
     private static double log2bE=Math.log(2.0);
     public static double log2(double d) { return Math.log(d)/log2bE; }
     public static double max2(double d1, double d2) { return (d1>d2? d1:d2); }
     public static double max3(double d1, double d2, double d3) { return d1>d2? (d1>d3? d1:d3) : (d2>d3? d2:d3); }
 
     public static String d1s(double[] r, String header) {
 	StringBuffer buf=new StringBuffer();
 	if (header!=null) { buf.append(String.format("%s: ", header)); }
 	for (int j=0; j<r.length; j++) {
 	    buf.append(String.format("%7.3f ", r[j]));
 	}
 	return new String(buf);
     }
     public static String d1s(double[] r) { return d1s(r,null); }
 
     public static String d2s(double[][] a, String header) {
 	StringBuffer buf=new StringBuffer();
 	if (header!=null) {
 	    buf.append(header);
 	    buf.append("\n");
 	}
 	for (int i=0; i<a.length; i++) {
 	    buf.append(d1s(a[i], header+String.format("[%d]", i)));
 	    buf.append("\n");
 	}
 	return new String(buf);
     }
     public static String d2s(double[][] a) { return d2s(a, null); }
 	
 
 ////////////////////////////////////////////////////////////////////////
 
     public ArrayList<ViterbiResult> align_genome(String prot_file) {
 	System.out.println(String.format("aligning proteins in %s", prot_file));
 	ProtStream ps=new ProtStream(prot_file);
 	ArrayList<ViterbiResult> results=new ArrayList<ViterbiResult>();
 	String prot;
 	while ((prot=ps.next())!=null) {
 	    double v=viterbi(prot);
 	    results.add(new ViterbiResult(ps.last_prot_name, prot, v));
	    System.out.println(String.format("%s: %g", ps.last_prot_name, v));
 	}
 	return results;
     }
 
     public ArrayList<ViterbiResult> align_alignment(Alignment a) {
 	ArrayList<ViterbiResult> results=new ArrayList<ViterbiResult>();
 	for (int i=0; i<a.n_rows; i++) {
 	    String prot=a.rowAsString(i).replaceAll("[-]", "");
 	    double v=viterbi(prot);
 	    results.add(new ViterbiResult(a.names[i], prot, v));
 	    System.out.println(String.format("%s: %g", a.names[i], v));
 	}
 	return results;
     }
 
     public static BackgroundProbs get_bgprobs(String bps_file) {
 	BackgroundProbs bps=null;
 	try {
 	    bps=BackgroundProbs.readBps(bps_file);
 	} catch (IOException ioe) {
 	    new Die(ioe);
 	} catch (ClassNotFoundException e) {
 	    new Die(e);
 	}
 	return bps;
     }
 
 ////////////////////////////////////////////////////////////////////////
 
     public static void main(String[] argv) {
 	String align_file="hw2-muscle17.txt";
 	String prot_file="NC_011660.faa";
 	boolean align_from_alignment=false;
 	try {
 	    prot_file=argv[0];
 	    align_file=argv[1];
 	    align_from_alignment=argv.length >= 3;
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    // pass
 	}
 
 	Alignment a=new Alignment(align_file,8).read();
 	System.out.println(String.format("using alignment %s from %s", a, align_file));
 
 	String bps_file=prot_file.replaceAll(".faa", ".bps.ser");
 	BackgroundProbs bps=get_bgprobs(bps_file);
 	System.out.println("Using background probs in "+bps_file);
 	ProfileHMM hmm=new ProfileHMM();
 	hmm.train(a, bps);
 
 	ArrayList<ViterbiResult> results;
 	if (align_from_alignment) {
 	    results=hmm.align_alignment(a);
 	} else {
 	    results=hmm.align_genome(prot_file);
 	}
 	
 	System.out.println("Sorted results:");
 	ViterbiResult[] sorted=results.toArray(new ViterbiResult[results.size()]);
 	Arrays.sort(sorted);
 	for (int i=0; i<sorted.length; i++) {
 	    System.out.println(String.format("%s %s", sorted[i].header(), (debugging? sorted[i].seq : "")));
 	}
     }
 }
