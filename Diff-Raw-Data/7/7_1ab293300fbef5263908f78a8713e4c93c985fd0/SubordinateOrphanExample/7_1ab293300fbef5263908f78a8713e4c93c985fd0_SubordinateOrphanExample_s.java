 package io.narayana.txmsc;
 
 import com.arjuna.ats.arjuna.common.Uid;
 import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
 import com.arjuna.ats.arjuna.recovery.RecoveryManager;
 import io.narayana.txmsc.child.SubordinateTransaction;
 import io.narayana.txmsc.child.SubordinateTransactionImporter;
 import io.narayana.txmsc.parent.NodeConfig;
 import io.narayana.txmsc.parent.RootTransaction;
 import io.narayana.txmsc.parent.SubordinateParticipantStub;
 import io.narayana.txmsc.parent.SubordinateParticipantStubRecordTypeMap;
 
 /**
  * This example shows how an orphaned Subordinate Transaction can appear and how orphan detection can be used to correctly
  * resolve it.
  *
  * @author paul.robinson@redhat.com 08/08/2013
  */
 public class SubordinateOrphanExample {
 
     public static void main(String[] args) throws Exception {
 
         if (args.length == 0) {
             runTransaction();
         } else if (args[0].equals("--recover")) {
             recoverTransaction();
         } else {
             System.err.println("Unexpected arg: " + args[0]);
         }
     }
 
     /**
      * Run a transaction, simulating a crash after the subordinate transaction prepares, but before the root transaction
      * completes it's prepare. This results in a Subordinate Transaction being logged, without a corresponding Root Transaction log.
      *
      * @throws Exception
      */
     private static void runTransaction() throws Exception {
 
         /*
             PARENT SIDE
          */
         RootTransaction rootTransaction = new RootTransaction();
         rootTransaction.begin();
 
         ConfigService parentConfigService = new ConfigService("ParentConfigService");
         rootTransaction.add(parentConfigService.getParticipant());
 
         //Make the config change
         parentConfigService.setNewValue("parent-config", "newParentConfigValue");
 
 
         /*
             CHILD SIDE (Propagated transaction context)
          */
         SubordinateTransaction subordinateTransaction = SubordinateTransactionImporter.createSubordinateTransaction(NodeConfig.SERVER_ID);
         subordinateTransaction.begin();
 
         ConfigService childsConfigService = new ConfigService("childConfigService", true);
         subordinateTransaction.add(childsConfigService.getParticipant());
 
         //Make the config change
         childsConfigService.setNewValue("child-config", "newChildConfigValue");
 
         //Get subordinate Uid and pass to parent.
         Uid subordinateUid = subordinateTransaction.get_uid();
 
 
         /*
             PARENT SIDE
          */
         SubordinateParticipantStub subordinateParticipantStub = new SubordinateParticipantStub(subordinateUid, true);
         rootTransaction.add(subordinateParticipantStub);
 
        try {
            rootTransaction.commit();
        } catch (Error e) {
            System.out.println("Server simulated a crash, as expected");
        }
     }
 
 
     /**
      * Setup then run recovery. The Root Transaction does not need recovering as it never prepared. The Subordinate Transaction
      * is rolled back as part of orphan detection.
      *
      * @throws Exception
      */
     private static void recoverTransaction() throws Exception {
 
         /*
             PARENT SIDE
          */
         ConfigParticipantRecordTypeMap map = new ConfigParticipantRecordTypeMap();
         RecordTypeManager.manager().add(map);
 
         SubordinateParticipantStubRecordTypeMap subordinateParticipantStubRecordTypeMap = new SubordinateParticipantStubRecordTypeMap();
         RecordTypeManager.manager().add(subordinateParticipantStubRecordTypeMap);
 
         RecoveryManager recoveryManager = RecoverySetup.getAndConfigureRecoveryManager();
         recoveryManager.scan();
 
         //Print out the state after the transaction is recovered. As it should be rolled back, these values should be null.
         System.out.println(ConfigService.getCommittedValue("child-config"));
         System.out.println(ConfigService.getCommittedValue("parent-config"));
 
         recoveryManager.terminate();
     }
 
 }
