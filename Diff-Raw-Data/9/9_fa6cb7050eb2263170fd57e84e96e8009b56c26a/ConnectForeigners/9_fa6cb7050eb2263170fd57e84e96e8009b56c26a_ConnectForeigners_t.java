 /**
  * 
  */
 package meta2d.statements;
 
 import java.util.Collection;
 import meta2d.MetaController;
 import meta2d.Statement;
 import java.util.BitSet;
 
 public class ConnectForeigners extends Statement {
 
     public ConnectForeigners(int self) { super(self); }
 
     @Override
     protected boolean evaluateImplementation(MetaController metaController) {
         Collection<Integer> metaNeighbors = metaController.getMetaNeighbors();
         BitSet actuatingConnectors = new BitSet(8);
         for(int c=0; c<8; c++)
             if(!metaNeighbors.contains(c) && !metaController.isConnected(c) && metaController.isOtherConnectorNearby(c)) { 
                 metaController.symmetricConnect(c);
                 actuatingConnectors.set(c);
             }
         // Wait for all connectors to be connected
         while(actuatingConnectors.cardinality()>0) {
             for(int c=0;c<8;c++) {
                 if(actuatingConnectors.get(c) && metaController.isConnected(c)) actuatingConnectors.clear(c);
             }
            metaController.controlYield();
         }
         return true;
     }
 
     @Override
     public Statement reverseStatement() {
         return new DisconnectForeigners(self);
     }
 
 }
