 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package projectproof;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Represents a <i>proof table</i>, which is a list of {@code ProofTableState}
  * generated from a {@link Procedure}.
  */
 public class ProofTable {
     private ArrayList<ProofTableState> myStates;
     private Procedure myProcedure;
 
     /**
      * <p>Creates a new {@code ProofTable} for the {@link Procedure} passed in
 	 * with respect to the {@link Definition}s and {@link Operation}s described
 	 * by the current {@link Environment}.</p>
      * @param inProcedure the {@code Procedure} that this {@code ProofTable}
 	 * is to attempt to prove.
      */
     public ProofTable(Procedure inProcedure) throws Exception
     {
         myProcedure = inProcedure;
         myStates = new ArrayList<ProofTableState>();
         HashMap<String, String> subMap = new HashMap<String, String>();
         ArrayList<Expression> instructions = inProcedure.getInstructions();
         Environment e = Environment.getInstance();
         for(int sC = 0; sC<=instructions.size(); sC++)
         {
             subMap.clear();
             String header = "State " + sC + "\n========\n";
             ProofTableState currState;
             Expression assumes, confirms;
             if(sC==0)
             {
                 Expression in = instructions.get(sC);
                if(e.searchEnvironmentDefs(in)==null)
                {
                    throw new Exception("Implicit declaration" +
                            " of definition:\n"+in.getName());
                }
                 Operation op = e.searchEnvironmentOps(in);
                if(op==null)
                {
                    throw new Exception("Implicit declaration" +
                            " of operation:\n"+in.getName());
                }
                 ArrayList<String> values = new ArrayList<String>();
                 ArrayList<String> keys = new ArrayList<String>(op.getArgs());
                 keys.addAll(inProcedure.getArgs());
                 for(int mapNdx = 0; mapNdx<keys.size(); mapNdx++)
                 {
                     values.add(in.getArgs()
                             .get(mapNdx%in.getArgs().size())
                             .toString()+sC
                             );
                     subMap.put(keys.get(mapNdx),
                             in.getArgs()
                             .get(mapNdx%in.getArgs().size())
                             .toString()+sC
                             );
                     subMap.put("#"+keys.get(mapNdx),
                             in.getArgs()
                             .get(mapNdx%in.getArgs().size())
                             .toString()+(sC-1)
                             );
                 }
                 assumes = inProcedure.getRequires().substitute(subMap);
                 confirms = op.getRequires().substitute(subMap);
             }
             else if(sC==instructions.size())
             {
                 Expression in = instructions.get(sC-1);
                if(e.searchEnvironmentDefs(in)==null)
                {
                    throw new Exception("Implicit declaration" +
                            " of definition:\n"+in.getName());
                }
                 Operation op = e.searchEnvironmentOps(in);
                if(op==null)
                {
                    throw new Exception("Implicit declaration" +
                            " of operation:\n"+in.getName());
                }
                 ArrayList<String> values = new ArrayList<String>();
                 ArrayList<String> keys = new ArrayList<String>(op.getArgs());
                 keys.addAll(inProcedure.getArgs());
                 for(int mapNdx = 0; mapNdx<keys.size(); mapNdx++)
                 {
                     values.add(in.getArgs()
                             .get(mapNdx%in.getArgs().size())
                             .toString()+(sC));
                     subMap.put(keys.get(mapNdx),
                             in.getArgs()
                             .get(mapNdx%in.getArgs().size())
                             .toString()+(sC));
                     subMap.put("#"+keys.get(mapNdx),
                             in.getArgs()
                             .get(mapNdx%in.getArgs().size())
                             .toString()+(sC-1));
                 }
                 assumes = e.searchEnvironmentOps(instructions.get(sC-1))
                         .getEnsures().substitute(subMap);
                 confirms = inProcedure.getEnsures().substitute(subMap);
             }
             else
             {
                 Expression in = instructions.get(sC-1);
                if(e.searchEnvironmentDefs(in)==null)
                {
                    throw new Exception("Implicit declaration" +
                            " of definition:\n"+in.getName());
                }
                 Operation op = e.searchEnvironmentOps(in);
                if(op==null)
                {
                    throw new Exception("Implicit declaration" +
                            " of operation:\n"+in.getName());
                }
                 ArrayList<String> values = new ArrayList<String>();
                 ArrayList<String> keys = new ArrayList<String>(op.getArgs());
                 for(int mapNdx = 0; mapNdx<keys.size(); mapNdx++)
                 {
                     values.add(in.getArgs().get(mapNdx).toString()+sC);
                     subMap.put(keys.get(mapNdx),
                             in.getArgs().get(mapNdx).toString()+sC);
                     subMap.put("#"+keys.get(mapNdx),
                             in.getArgs().get(mapNdx).toString()+(sC-1));
                 }
                 assumes = e.searchEnvironmentOps(instructions.get(sC-1))
                         .getEnsures().substitute(subMap);
                 confirms = e.searchEnvironmentOps(instructions.get(sC))
                         .getRequires().substitute(subMap);
             }
             myStates.add(new ProofTableState(header, assumes, confirms));
         }
     }
 
     /**
      * <p>Returns a {@code String} representation of this ProofTable and all of
      * the {@code ProofTableStates} within it.</p>
      * @return a {@code String} representing this {@code ProofTable} in full.
      */
     @Override
     public String toString()
     {
         String retVal = "Proof Table for ["+myProcedure.getName()+"]\n\n";
         for(ProofTableState a : myStates)
         {
             retVal += a;
         }
         return retVal.trim();
     }
 
 }
