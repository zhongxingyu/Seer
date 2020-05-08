 /**
  * 
  */
 package meta2d.statements;
 
 import java.util.BitSet;
 import java.util.Collection;
 
 import meta2d.MetaController;
 import meta2d.Statement;
 
 
 public class DisconnectForeigners extends Statement {
 
     public DisconnectForeigners(int self) { super(self); }
 
     @Override
     protected boolean evaluateImplementation(MetaController metaController) {
         Collection<Integer> metaNeighbors = metaController.getMetaNeighbors();
         BitSet actuatingConnectors = new BitSet(8);
         for(int c=0; c<8; c++) {
             if(!metaNeighbors.contains(c) && metaController.isConnected(c)) {
                 metaController.symmetricDisconnect(c);
                 actuatingConnectors.set(c);
             }
         }
         // Wait for all connectors to be disconnected
         while(actuatingConnectors.cardinality()>0) {
             for(int c=0;c<8;c++) {
                 if(actuatingConnectors.get(c) && !metaController.isConnected(c)) actuatingConnectors.clear(c);
             }
            metaController.ussrYield();
         }
         return true;
     }
 
     @Override
     public Statement reverseStatement() {
         return new ConnectForeigners(self);
     }
 
 }
