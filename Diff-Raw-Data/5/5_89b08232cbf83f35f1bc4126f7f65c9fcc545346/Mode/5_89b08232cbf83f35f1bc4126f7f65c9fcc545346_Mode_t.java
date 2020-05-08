 package ch.fhnw.cbip.compiler.scanner.token;
 
 import ch.fhnw.cbip.compiler.error.LexicalError;
 import ch.fhnw.cbip.compiler.scanner.AbstractToken;
 import ch.fhnw.cbip.compiler.scanner.enums.ModeAttribute;
 import ch.fhnw.cbip.compiler.scanner.enums.Terminal;
 
 public abstract class Mode extends AbstractToken {
   private final Terminal terminal;
   private final ModeAttribute attribute;
 
   public Mode(int line, Terminal terminal, ModeAttribute attribute) {
     super(terminal, line);
     this.terminal = terminal;
     this.attribute = attribute;
   }
 
   public ModeAttribute getAttribute() {
     return attribute;
   }
 
   @Override
   public String toString() {
     return "(" + terminal.toString() + ", " + attribute.toString() + ")";
   }
 
   public static class ChangeMode extends Mode {
     public ChangeMode(ModeAttribute attribute, int line) throws LexicalError {
       super(line, Terminal.CHANGEMODE, attribute);
       if ((attribute != ModeAttribute.CONST) && (attribute != ModeAttribute.VAR))
        throw new LexicalError("Invalid ChangeMode attribute", line);
     }
 
   }
 
   public static class FlowMode extends Mode {
     public FlowMode(ModeAttribute attribute, int line) throws LexicalError {
       super(line, Terminal.FLOWMODE, attribute);
       if ((attribute != ModeAttribute.IN) && (attribute != ModeAttribute.OUT)
           && (attribute != ModeAttribute.INOUT))
        throw new LexicalError("Invalid FlowMode attribute", line);
     }
   }
 
   public static class MechMode extends Mode {
     public MechMode(ModeAttribute attribute, int line) throws LexicalError {
       super(line, Terminal.MECHMODE, attribute);
       if ((attribute != ModeAttribute.COPY) && (attribute != ModeAttribute.REF))
         throw new LexicalError("Invalid MechMode attribute", line);
     }
   }
 
 }
