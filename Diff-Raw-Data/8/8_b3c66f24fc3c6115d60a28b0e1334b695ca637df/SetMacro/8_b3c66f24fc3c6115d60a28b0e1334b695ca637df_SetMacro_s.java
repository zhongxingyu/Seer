 package org.wings.macro;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mvel.MVEL;
 
 import java.math.BigDecimal;
 
 /**
  * #set directive
  * <p/>
 * The #set directive is used for setting the rhsReference of a lhsReference.
 * A rhsReference can be assigned to either a variable lhsReference or a property lhsReference.
 * For example:
  * #set( $primate = "monkey" )
  * #set( $customer.Behavior = $primate )
  * <p/>
  * ...
  * (For more info see: http://velocity.apache.org/engine/devel/user-guide.html#directives)
  */
 public class SetMacro extends AbstractMacro {
 
     private static final Log LOG = LogFactory.getLog(SetMacro.class);
 
     private String instruction;
 
     private boolean lhsIsPropertyReference = false;
     private boolean lhsIsVariableReference = false;
 
     private boolean rhsIsPropertyReference = false;
     private boolean rhsIsStringLiteral = false;
     private boolean rhsIsVariableReference = false;
     private boolean rhsIsMethodReference = false;
     private boolean rhsIsNumberLiteral = false;
     private boolean rhsIsArrayList = false;
     private boolean rhsIsMap = false;
 
     private String lhsReference;
     private String rhsReference;
 
     public SetMacro(String instruction) {
         this.instruction = instruction;
         String[] tokens = instruction.split("=");
         if (tokens != null && tokens.length == 2) {
             String lhs = tokens[0].trim(); // left hand side
             String rhs = tokens[1].trim(); // right hand side
 
             if (lhs.startsWith("$") && lhs.length() > 1) {
                 if (lhs.contains(".")) {
                     lhsIsPropertyReference = true;
                 } else {
                     lhsIsVariableReference = true;
                 }
                 lhsReference = lhs.substring(1);
             } else {
                LOG.debug("Invalid lhsReference on LHS");
             }
 
             if (rhs.length() > 0) {
                 if (rhs.startsWith("$")) {
                     // todo: handle property lhsReference
                     // todo: handle variable lhsReference
                     // todo: handle method lhsReference
                 } else if (Character.isDigit(rhs.charAt(0))) {
                     // handle number literal
                     rhsIsNumberLiteral = true;
                     rhsReference = rhs;
                 } else if (rhs.charAt(0) == '[') {
                     // todo: handle array list
                 } else if (rhs.charAt(0) == '{') {
                     // todo: handle map
                 } else {
                     // handle string literal
                     rhsIsStringLiteral = true;
                     rhsReference = rhs;
                 }
             } else {
                 LOG.debug("Invalid type on RHS");
             }
         }
     }
 
     public void execute(MacroContext ctx) {
         Object value = resolveRhsReference();
         if (lhsIsVariableReference) {
             // step 1: store value in macro context...
             ctx.put(lhsReference, value);
 
             // step 2: try to write through into component
             try {
                 MVEL.setProperty(ctx.getComponent(), lhsReference, value);
             } catch (Exception e) {
                 // ignore...
             }
 
         } else if (lhsIsPropertyReference) {
             MVEL.setProperty(ctx, lhsReference, value); // XXX: not sure about this...
 
         } else {
             LOG.debug("Invalid set directive: \"" + instruction + "\"");
         }
     }
 
     private Object resolveRhsReference() {
         if (rhsIsStringLiteral) {
             return rhsReference;
         } else if (rhsIsNumberLiteral) {
             return new BigDecimal(rhsReference); // XXX: not sure about this...
         } else {
             LOG.debug("Type not supported yet");
             return null; //
         }
     }
 }
 
