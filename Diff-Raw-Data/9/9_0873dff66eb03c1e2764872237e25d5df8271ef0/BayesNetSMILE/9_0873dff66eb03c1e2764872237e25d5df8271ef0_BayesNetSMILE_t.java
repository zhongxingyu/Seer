 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.smile;
 
 import edu.pitt.isp.sverchkov.arrays.ArrayTools;
 import edu.pitt.isp.sverchkov.bn.BayesNet;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import smile.Network;
 
 /**
  *
  * @author YUS24
  */
 public class BayesNetSMILE implements BayesNet<String,String> {
     
     private final Network net;
     private boolean convertIDs = true;
     
     public BayesNetSMILE( File file ){
         net = new Network();
         try {
             net.readFile( file.getCanonicalPath() );
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
     
     /**
      * @return whether we are currently converting SMILE IDs
      */
     public boolean isConvertIDs(){
         return convertIDs;
     }
     
     /**
      * Sets whether to convert SMILE IDs
      * @param convert true to convert SMILE IDs, false not to.
      */
     public void convertIDs( boolean convert ){
         convertIDs = convert;
     }
 
     @Override
     public int size() {
         return net.getNodeCount();
     }
 
     @Override
     public Collection<String> parents(String node) {
         return Arrays.asList( net.getParentIds( node ) );
     }
 
     @Override
     public Collection<String> values(String node) {
         if( convertIDs ){
             String[] values = net.getOutcomeIds(node);
             Collection<String> result = new ArrayList<>(values.length);
             for( String value : values )
                 result.add( fromSMILEID( value ) );
             return result;
         }
         return Arrays.asList( net.getOutcomeIds(node) );
     }
 
     @Override
     public double probability(Map<String, String> outcomes, Map<String, String> conditions) {
         
         Map<String,String> o = outcomes, c = conditions;
         
         // String conversion check
         if( convertIDs ){
             o = new HashMap<>();
             for( Map.Entry<String,String> entry : outcomes.entrySet() )
                 o.put( entry.getKey(), toSMILEID(entry.getValue()) );
             c = new HashMap<>();
             for( Map.Entry<String,String> entry : conditions.entrySet() )
                 c.put( entry.getKey(), toSMILEID(entry.getValue()) );
         }
         
         // If there is only one outcome this is a simple query
         if( o.size() == 1 ){
             Map.Entry<String,String> entry = o.entrySet().iterator().next();
             return probability( entry.getKey(), entry.getValue(), c );
         }
         
         // Otherwise we need to use the product rule
         Map<String,String> cond = new HashMap<>( c );
         double logResult = 0;
         for( Map.Entry<String,String> entry : o.entrySet() ){
             logResult += Math.log( probability( entry.getKey(), entry.getValue(), cond ) );
             cond.put( entry.getKey(), entry.getValue() );
         }
         
         return Math.exp(logResult);
     }
     
     /**
      * Computes P( node = value | conditions ), assumes that all strings have been converted to their notation in the SMILE net.
      * @param node
      * @param value
      * @param conditions Map representation of variable-value assignments.
      * @return P( node = value | conditions )
      */
     private double probability( String node, String value, Map<String,String> conditions ){
         
         // Check if this is just a CPT lookup
         if( new HashSet( parents( node ) ).equals( conditions.keySet() ) ){
             // Compute position in node definition
             // return
         }
             
         // Otherwise do inference
         net.clearAllEvidence();
         net.clearAllTargets();
         
         for( Map.Entry<String,String> entry : conditions.entrySet() ){
             net.setEvidence( entry.getKey(), entry.getValue() );
         }
         net.setTarget(node, true);
         
         net.updateBeliefs();
         
         return net.getNodeValue(node)[ArrayTools.firstIndexOf(value, net.getOutcomeIds(node))];
     }
 
     @Override
     public Iterator<String> iterator() {
         return Arrays.asList( net.getAllNodeIds() ).iterator();
     }
     
     public List<Float> getNodeCoordinates( String node ){
        Float[] coords = new Float[2];
         Rectangle pos = net.getNodePosition(node);
        coords[0] = new Float(pos.getMinX());
        coords[1] = new Float(pos.getMinY());
        return Arrays.asList(coords);
     }
     
     /**
      * Encode the name into a SMILE-friendly format.
      * @param name
      * @return corresponding SMILE ID
      */
     public static String toSMILEID( String name ){
         StringBuilder result = new StringBuilder("ID_");
         for( char c : name.toCharArray() ){
             result.append( Character.isLetterOrDigit(c)? c : String.format("_%2x", (byte)c) );
         }
         return result.toString();
     }
 
     /**
      * Decode smile-friendly IDs to user-readable names
      * @param id
      * @return corresponding readable name
      */
     public static String fromSMILEID( String id ){
         StringBuilder result = new StringBuilder();
         for( int i = 3; i < id.length(); i++ )
             if( id.charAt(i) == '_' ){
                 result.append( (char) Byte.parseByte(id.substring(i+1, i+3), 16) );
                 i+=2;
             } else result.append(id.charAt(i));
         return result.toString();
     }
 
 }
