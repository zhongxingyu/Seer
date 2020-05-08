 import java.util.regex.*;
 import java.lang.Math.*;
 
 class ProfileHMM_Node {
     public String state;	// must match /^[MID]\d+$/, 'begin', 'end', or I0
     public String state_type;	// must be one of M, I, D, begin, end
     public int state_num;	// must be >0, unless state_type=="I"
 
     public double to_m;
     public double to_i;
     public double to_d;		// convention: also "to_end" if last==true
     public boolean last;
 
     public double Eb[];	   // emission probabilities out of this state
     private final int Eb_size=26;
     public  final int n_aas=20;
     private final String aas_re="[ACDEFGHIKLMNPQRSTVWY]"; // [A-Z]-[BJOUXZ];
 
     private String valid_statename="^[MID]\\d+|begin|end$"; 
 
 
     public ProfileHMM_Node(String state, boolean last) 
 	throws ProfileHMM_BadStateException 
     {
 	if (state.matches(valid_statename)) {
 	    this.state=state;
 	    if (state.matches("[MID]\\d+")) {
 		this.state_type=state.substring(0,1);
 		this.state_num=Integer.valueOf(state.substring(1)).intValue();
 	    } else {
 		state_type=state;
 	    }
 
 	    this.Eb=new double[Eb_size];
 	    this.last=last;
 
 	} else {
 	    // throw exception?
 	    throw new ProfileHMM_BadStateException("Bad state name: "+state);
 	} 
 
     }
 
 
     public String toString() {
 	return String.format("%s (%d): %s=%6.4f %s=%6.4f %s=%6.4f", state, state_num,
 			     nextState("M"), to_m, nextState("I"), to_i, nextState("D"), to_d);
     }
     
     public String rawString() {
 	return String.format("%s (%d): to_m=%6.4f to_i=%6.4f to_d=%6.4f last=%b", state, state_num,
 			     to_m, to_i, to_d, last);
     }
 
     // Return a string listing the Eb(j) values (eg "A: 0.232 B: 0.093 ...")
     public String Ebs() {
 	StringBuffer buf=new StringBuffer();
 	for (int i=0; i<Eb_size; i++) {
 	    int aa='A'+i;
 	    char[] aaa=new char[1]; aaa[0]=(char)aa; // String(char) doesn't exist, but String(char[]) does
 	    if (! new String(aaa).matches(aas_re)) continue;
 	    buf.append(String.format("%c: %.3f ", aa, this.Eb[i]));
 	}
 	return new String(buf);
     }
 
 
 
     // Generate the name of the kth state following this state, taking into account
     // the structure of the HMM.  This routine is used as a self-check and in toString.
     public String nextState(String next_type) {
 	if (next_type.equals("end")) return "end";
 
 	if (next_type.equals("I")) {
 	    return String.format("I%d",state_num);
 
 	} else if (this.last) {
 	    if (next_type.equals("I")) return String.format("I%d",state_num);
 	    else return "end";
 
 	}  else {
 	    return String.format("%s%d", next_type, state_num+1);
 	}
 	
     }
 
 
     // Return the transition prob from this state to the given state k.
     // Convention: if k==end, use to_d.
     public double get_tr(String k) throws ProfileHMM_BadStateException {
 	String k_type=k.substring(0,1);
 	if      (k_type.equals("M")) return to_m;
 	else if (k_type.equals("I")) return to_i;
 	else if (k_type.equals("D")) return to_d;
	else if (k_type.equals("end")) return to_d;
 	else throw new ProfileHMM_BadStateException(k);
     }
 
 
     // Training methods:
     // Increment the count to a given state k by amount d (default d=1, as below)
     public void inc_tr(String k, double d) {
 	if      (k.equals("M")) this.to_m+=d;
 	else if (k.equals("I")) this.to_i+=d;
 	else if (k.equals("D")) this.to_d+=d;
 	else if (k.equals("end")) this.to_d+=d;
 	else throw new ProfileHMM_BadStateException(k);
     }
     public void inc_tr(String k) { inc_tr(k,1); }
 
 
 
 
     // Get the transitional (a_jk) pseudocount going to a given state k:
     public double get_tr_pc(String k) {
 	double psc=0;		// pseudo count
 	if (state_type.equals("M"))     { 
 	    if (k.equals("M")) psc=last? 0.00 : 0.90; 	    
 	    if (k.equals("I")) psc=last? 0.90 : 0.05; 
 	    if (k.equals("D")) psc=last? 0.10 : 0.05; 
 	    // to_m=0.90; to_i=0.05; to_d=0.05; 
 	}
 	if (state_type.equals("I"))     { 
 	    if (k.equals("M")) psc=last? 0.00 : 0.05; 	    
 	    if (k.equals("I")) psc=last? 0.90 : 0.90; 
 	    if (k.equals("D")) psc=last? 0.10 : 0.05; 
 	    // to_m=0.05; to_i=0.90; to_d=0.05; 
 	}
 	if (state_type.equals("D"))     { 
 	    if (k.equals("M")) psc=last? 0.00 : 0.05; 	    
 	    if (k.equals("I")) psc=last? 0.10 : 0.05; 
 	    if (k.equals("D")) psc=last? 0.90 : 0.90; 
 	    //to_m=0.05; to_i=0.05; to_d=0.90; 
 	}
 
 	if (state_type.equals("begin")) { 
 	    if (k.equals("M")) psc=0.10;
 	    if (k.equals("I")) psc=0.45;
 	    if (k.equals("D")) psc=0.45;
 	    // to_m=0.10; to_i=0.45; to_d=0.45; 
 	}
 	if (state_type.equals("end"  )) { 
 	    psc=0.0;
 	    // to_m=0.00; to_i=0.00; to_d=0.00; 
 	}
 	return psc;
     }
 
 
     // Normalize the counts to produce probabilities
     // Pseduocounts: sum will include them, but will include all of them for each of to_[mid].
     public void normalize_trs() {
 	double sum=to_m+to_i+to_d+1; // +1 to account for sum of pseudocounts
 	//System.out.println(String.format("normalizing %s: sum=%g", this.rawString(), sum));
 	to_m = (to_m+get_tr_pc("M"))/sum;
 	to_i = (to_i+get_tr_pc("I"))/sum;
 	to_d = (to_d+get_tr_pc("D"))/sum;
 	if (!state.equals("end")) {
 	    Die.assert_true(Math.abs(to_m+to_i+to_d-1.0) < 0.0001,
 			    String.format("%s: probs don't add up to 1 (%g)", rawString(), to_m+to_i+to_d));
 	}
 	// System.out.println(String.format("normalizing %s: sum=%g\n", this.rawString(), sum));
     }
 
     public void inc_em(char aa, double d) { Eb[aa-'A']+=d; }
     public void inc_em(char aa) { inc_em(aa,1); }
     public double get_em(char aa) { return Eb[aa-'A']; }
 
 
     public void normalize_ems(BackgroundProbs bps) {
 	double sum=1;		// +1 to account for sum of pseudocounts
 	for (char aa='A'; aa<='Z'; aa++) { sum+=Eb[aa-'A']; }
 	double check=0;
 	for (char aa='A'; aa<='Z'; aa++) { 
 	    Eb[aa-'A'] = (Eb[aa-'A'] + bps.pr(aa)) / sum;
 	    check+=Eb[aa-'A'];
 	}
 	Die.assert_true(Math.abs(check-1.00)<0.0001, String.format("%s: Em(b)'s don't add up to 1 (%g) (sum=%g)\n%s", 
 						   rawString(), check, sum, Ebs()));
     }
 
 ////////////////////////////////////////////////////////////////////////
 
     public static void main(String[] argv) {
 	ProfileHMM_Node n=new ProfileHMM_Node("M13", false);
 	n.inc_tr("M");
 	n.inc_tr("D");
 	n.inc_tr("M");
 	n.inc_tr("I");
 	n.inc_tr("I");
 	n.inc_tr("D");
 	n.inc_tr("D");
 	n.inc_tr("M");
 	n.inc_tr("M");
 	n.inc_tr("I");
 	n.inc_tr("D");
 	System.out.println(String.format("n is %s",n));
  
 	System.out.println(String.format("next M state: %s", n.nextState("M")));
 	System.out.println(String.format("next I state: %s", n.nextState("I")));
 	System.out.println(String.format("next D state: %s", n.nextState("D")));
    }
     
 }
