 /*
  * Created on Nov 12, 2005
  */
 package uk.org.ponder.rsf.state;
 
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.BeanPredicateModel;
 import uk.org.ponder.beanutil.ELReference;
 import uk.org.ponder.mapping.BeanInvalidationBracketer;
 import uk.org.ponder.mapping.BeanInvalidationModel;
 import uk.org.ponder.mapping.ConverterConverter;
 import uk.org.ponder.mapping.DAREnvironment;
 import uk.org.ponder.mapping.DARList;
 import uk.org.ponder.mapping.DARReshaper;
 import uk.org.ponder.mapping.DataAlterationRequest;
 import uk.org.ponder.mapping.DataConverterRegistry;
 import uk.org.ponder.mapping.ListBeanInvalidationModel;
 import uk.org.ponder.mapping.ShellInfo;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.rsf.request.ActionTarget;
 import uk.org.ponder.rsf.request.RequestSubmittedValueCache;
 import uk.org.ponder.rsf.request.SubmittedValueEntry;
 import uk.org.ponder.rsf.state.guards.BeanGuardProcessor;
 import uk.org.ponder.rsf.uitype.UIType;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.RunnableInvoker;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 public class RSVCApplier {
   private VersionCheckPolicy versioncheckpolicy;
   private BeanModelAlterer darapplier;
   private BeanInvalidationModel bim;
   private BeanGuardProcessor beanGuardProcessor;
   private boolean ignoreFossilizedValues = true;
   private TargettedMessageList targettedMessageList;
   private BeanLocator rbl;
   private DataConverterRegistry dataConverterRegistry;
   private BeanPredicateModel addressibleBeanModel;
 
   public void setAddressibleBeanModel(BeanPredicateModel addressibleBeanModel) {
     this.addressibleBeanModel = addressibleBeanModel;
   }
 
   public void setIgnoreFossilizedValues(boolean ignoreFossilizedValues) {
     this.ignoreFossilizedValues = ignoreFossilizedValues;
   }
 
   public void setBeanModelAlterer(BeanModelAlterer darapplier) {
     this.darapplier = darapplier;
   }
 
   public void setVersionCheckPolicy(VersionCheckPolicy versioncheckpolicy) {
     this.versioncheckpolicy = versioncheckpolicy;
   }
 
   public void setBeanInvalidationModel(BeanInvalidationModel bim) {
     this.bim = bim;
   }
 
   public void setBeanGuardProcessor(BeanGuardProcessor beanGuardProcessor) {
     this.beanGuardProcessor = beanGuardProcessor;
   }
 
   public void setTargettedMessageList(TargettedMessageList targettedMessageList) {
     this.targettedMessageList = targettedMessageList;
   }
 
   public void setRootBeanLocator(BeanLocator rbl) {
     this.rbl = rbl;
   }
 
   public void setDataConverterRegistry(DataConverterRegistry dataConverterRegistry) {
     this.dataConverterRegistry = dataConverterRegistry;
   }
   
   public void applyAlterations(DARList toapply) {
     try {
       BeanInvalidationBracketer bib = getBracketer();
       darapplier.applyAlterations(rbl, toapply, 
           new DAREnvironment(targettedMessageList, bib, addressibleBeanModel, 
               dataConverterRegistry));
     }
     finally {
       beanGuardProcessor.processPostGuards(bim, targettedMessageList, rbl);
     }
   }
 
   /**
    * Apply values from this RSVC to the model, and in addition process any
    * validations specified by BeanGuards.
    */
   public void applyValues(RequestSubmittedValueCache rsvc) {
     // TODO: There is scope for a lot of policy here - mainly version checking.
     // Define a VersionCheckPolicy that will compare oldvalue to the model
     // value.
     DARList toapply = new DARList();
 
     for (int i = 0; i < rsvc.getEntries(); ++i) {
       SubmittedValueEntry sve = rsvc.entryAt(i);
       boolean unchangedValue = false;
       // check against "old" values
       if (sve.componentid != null && !ignoreFossilizedValues && !sve.mustapply) {
         if (sve.oldvalue != null && sve.valuebinding != null) {
           versioncheckpolicy.checkOldVersion(sve); // will blow on error
 
           UIType type = UITypes.forObject(sve.oldvalue);
           try {
             // TODO: why did we need to hack the value flat like this - should
             // have been taken care of by FixupNewValue in PostDecoder
             Object flattened = darapplier.getFlattenedValue("", sve.newvalue,
                 sve.oldvalue.getClass(), null);
             // cull the change from touching the model.
             if (type.valueUnchanged(sve.oldvalue, flattened))
               unchangedValue = true;
           }
           catch (Exception e) {
             Logger.log.warn("Error flattening value" + sve.newvalue + " into "
                 + sve.oldvalue.getClass(), e);
           }
         }
       }
       DataAlterationRequest dar = null;
       // NB unchanged CANNOT be EL or deletion SVE.
       Object newvalue = unchangedValue ? DataAlterationRequest.INAPPLICABLE_VALUE
           : sve.newvalue;
       if (sve.isEL) {
         newvalue = new ELReference((String) sve.newvalue);
       }
       if (sve.isdeletion) {
         dar = new DataAlterationRequest(sve.valuebinding, newvalue,
             DataAlterationRequest.DELETE);
       }
       else {
         dar = new DataAlterationRequest(sve.valuebinding, newvalue);
       }
       Object reshapero = null;
       if (sve.reshaperbinding != null) {
         reshapero = darapplier.getBeanValue(sve.reshaperbinding, rbl, addressibleBeanModel);
       }
       else {
         try {
           ShellInfo shellinfo = darapplier.fetchShells(sve.valuebinding, rbl, false);
           reshapero = dataConverterRegistry.fetchConverter(shellinfo);
         }
         catch (Exception e) {
           throw UniversalRuntimeException.accumulate(e, "Error traversing binding path " + sve.valuebinding);
         }
       }
       if (reshapero != null) {
         DARReshaper reshaper = ConverterConverter.toReshaper(reshapero);
         try {
           dar = reshaper.reshapeDAR(dar);
         }
         catch (Exception e) {
           Logger.log.info("Error reshaping value", e);
           // errors initially accumulated referring to paths
           targettedMessageList.addMessage(new TargettedMessage(e.getMessage(),
               e, dar.path));
         }
       }
       toapply.add(dar);
 
     }
     applyAlterations(toapply);
   }
 
   public BeanInvalidationBracketer getBracketer() {
     return new BeanInvalidationBracketer() {
       public void invalidate(String path, Runnable toinvoke) {
         bim.invalidate(path);
         ListBeanInvalidationModel singlebim = new ListBeanInvalidationModel();
         singlebim.invalidate(path);
         RunnableInvoker invoker = beanGuardProcessor.getGuardProcessor(
             singlebim, targettedMessageList, rbl);
         invoker.invokeRunnable(toinvoke);
       }
     };
   }
 
   public Object invokeAction(String actionbinding, String knownvalue) {
     if (!addressibleBeanModel.isMatch(actionbinding)) {
       throw UniversalRuntimeException.accumulate(new SecurityException(),
           "Action binding " + actionbinding 
           + " is not permissible - make sure to mark this path as request addressible - http://www2.caret.cam.ac.uk/rsfwiki/Wiki.jsp?page=RequestWriteableBean");
     }
     ShellInfo shells = darapplier.fetchShells(actionbinding, rbl, true);
     int lastshell = shells.shells.length;
    for (int i = 0; i < lastshell; ++ i) {
      if (shells.shells[i] instanceof ActionTarget) {
        lastshell = i + 1;
      }
    }
     
     Object penultimatebean = shells.shells[lastshell - 1];
     String actionname = shells.segments[lastshell - 1];
     // The only ActionTarget in the world is FlowActionProxyBean, we are not
     // planning to keep it up
     if (penultimatebean instanceof ActionTarget) {
       Object returnvalue = ((ActionTarget) penultimatebean).invokeAction(
           actionname, knownvalue);
       return returnvalue;
     }
     else {
       return darapplier.invokeBeanMethod(shells, addressibleBeanModel);
     }
   }
   
 }
