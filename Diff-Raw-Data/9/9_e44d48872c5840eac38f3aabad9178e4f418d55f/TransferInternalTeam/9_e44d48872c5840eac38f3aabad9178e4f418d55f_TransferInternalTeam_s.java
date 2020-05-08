 package svm.logic.implementation.tranferobjects;
 
 import svm.domain.abstraction.modelInterfaces.ITeam;
 import svm.logic.abstraction.transferobjects.ITransferInternalTeam;
 
 /**
  * Projectteam: Team C
  * Date: 05.11.12
  */
 public class TransferInternalTeam extends TransferTeam implements ITransferInternalTeam {
 
     private ITeam teamEntity;
     private String name;
 
     public TransferInternalTeam() {
 
     }
 
     public TransferInternalTeam(ITeam teamEntity) {
         this.teamEntity = teamEntity;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public ITeam getModel() {
         return teamEntity;
     }
 
     @Override
     public void setObject(Object o) {
         this.teamEntity = (ITeam) o;
         this.name = teamEntity.getName();
     }
 
     @Override
     public String toString() {
        return this.teamEntity.getName();
     }
 
 }
