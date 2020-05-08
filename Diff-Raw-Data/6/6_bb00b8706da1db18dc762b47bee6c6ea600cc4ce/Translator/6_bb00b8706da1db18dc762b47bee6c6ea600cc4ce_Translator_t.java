 import java.util.Hashtable;
 import java.util.regex.*;
 
 public class Translator {
 
     static boolean debug = false;   
 	protected Hashtable<String, String> trans = new Hashtable<String, String>();		
 
 	public Translator() {
 
 	    trans.put("0",  "NAR,1,=Not a Resolver.,0,0");
         trans.put("1",  "R=([a-zA-Z]+).*,1a,=*.,0,0");
 	    trans.put("1a", "[FT]............,2,=Not a Resolver.,0,0");
 	    trans.put("2",  ".[PFX][PFX][PFXT][PFX][PF][APFXT][APFXT][APFXT][APFXT][APFXT][APFTXT].,=Unexpected Result.,3,0,0");
 	    trans.put("3",  ".[PF][PF][PF][PF]........,=OLD/Bad.,4,0,0");
 	    trans.put("4",  "PP...P[AP][AP].[AP]...,=Not DNSSEC.,5,0,0");
 	    trans.put("5",  "......PP.P...,7,8,0,0");
 	    trans.put("7",  ".............,7a,=Validator,0,0");
 	    trans.put("7a", "....P...A....,DNAME,7b,1,0");
 	    trans.put("7b", "...F.......A.,7c,TCP,0,0");
 	    trans.put("7c", "............F,7d,Permissive,0,0");
 	    trans.put("7d", "......[AFTX][AFTX][AFTX][AFTX][AFTX][AFTX].,Mixed,6a,0,1");
 	    trans.put("8",  ".............,8a,=DNSSEC Aware,0,0");
 	    trans.put("8a", "....P...[AP]....,DNAME,8b,0,0");
 	    trans.put("8b", "...F.......[AP].,6a,TCP,0,0");
 	    trans.put("6a", "..F..........,6b,Unknown,0,0");
 	    trans.put("6b", "..........F..,6c,NSEC3,0,0");
 	    trans.put("6c", "...P.......[XTF].,6d,SlowBig,0,0");
 	    trans.put("6d", "...F.......[XTF].,.,NoBig,0,0");
 	    trans.put(".",  ".............,.,.,0,0");
 	}
 
 	public static void set_debug(boolean val) {
         debug = val;
     }
     
 	public String translate(String pfa) {
 		
 	    String g = "";
 	    String gmod = "";
 	    int inhibits = 0;
 	    
 	    String next_state = "0";
 	    while (!next_state.equals("."))
 	    {
 	        String p = trans.get(next_state);
 	        String[] pieces = p.split(",");
 	        if (pieces.length==5)
 	        {
 	            // pattern, fail, succeed
 	            // fail/succeed: transition | [=]output[.]
 	            // = means main ("g"), no = means within ()'s ("gmod")
 	            // . is terminal
 	        	
 	        	Pattern pattern = Pattern.compile(pieces[0]);
 	            int set_inhibits = new Integer(pieces[3]);
 	            int mbz_inhibits = new Integer(pieces[4]);
 
 	            String state = next_state;
 	            next_state = "NOT_A_STATE";
 	            
 	            int m = 0;
 	            Matcher matcher = pattern.matcher(pfa);
 	            String match_action, alt_action;
 
 	            if ((inhibits & mbz_inhibits) != 0) {
 	            	// this is useful only to inhibit "Mixed" when "DNAME" 
 	            	// and unfortunately "Mixed" uses negative logic
 	            	// so the match is backwards, ...
 	            	// not very general purpose, but the best I could come up with.
 	            	match_action = pieces[2];
 	            	alt_action = pieces[1];
 	            }
 	            else if (matcher.matches())            
 	            { 
 	                m = 1;
 	                match_action = pieces[2];
 	                alt_action = pieces[1];
 	            }
 	            else
 	            {
	            	// oddly, set the really special purpose inhibits on the negative match
		            inhibits |= set_inhibits;
 	                match_action = pieces[1];
 	                alt_action = pieces[2];
 	            }
 	            
                 if (debug) {
                     System.out.printf(
                         "state=%-2s, inhibits=%x p=%s, pfa=%s, match=%s, match_action=%s, alt_action=%s, g=%s, gmod=%s\n", 
                         state, inhibits, p, pfa, m, match_action, alt_action, g, gmod);
 	            }
                 
 	            if (trans.containsKey(match_action))
 	            {
 	                next_state = match_action;
 	                continue;
 	            }
 	            else 
 	            {
 	                int have_dot = match_action.indexOf(".");
 	                if (have_dot >= 0)
 	                {
 	                    match_action = match_action.substring(0,have_dot);
 	                }
 
 	                if (match_action.indexOf("=")==0)
 	                {
 	                    g = match_action.substring(1);
 	                    if (g.equals("*"))
 	                    {
 	                    	g = matcher.group(1);
 	                    }
 	                }
 	                else
 	                {
 	                    gmod = gmod + match_action + ",";
 	                }
 	                
 	                if (have_dot > 0)
 	                    next_state = ".";
 	                else
 	                    next_state = alt_action;
 	            }
 	        }
 	        else
 	        {
 	            System.out.printf("ERROR-BadPattern: p=%s, next_state=%s\n", p, next_state);
 	        }
 	    }
 	    if (g.length() == 0)
 	    {
 	        g = "NAR";
 	        System.out.printf("ERROR: pfa=%s, g=%s\n", pfa, g);
 	    }
 	    else
 	    {
 	        int l = gmod.length();
 	        if (l > 0)
 	        {
 	            g = "Partial " + g + "(" + gmod.substring(0, l-1) + ")";
 	        }
 	    }
 	    return g;	
 	}
 }
