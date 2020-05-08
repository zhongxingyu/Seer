 package com.polopoly.ps.test;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import com.polopoly.cm.ContentId;
 import com.polopoly.model.ModelTransparent;
 import com.polopoly.siteengine.model.TopModel;
 import com.polopoly.siteengine.scope.TopModelImpl;
 
 public class RenderOperationStorage {
 
     public static Collection<RenderOperation> getOperations() {
         return operations;
     }
 
     public static void storeOperations() {
         storeOperations = true;
         operations.clear();
     }
 
     private static boolean storeOperations;
 
     public static class RenderOperation {
         private ContentId policy;
        private TopModel model;
         private TestableController controller;
 
         public RenderOperation(ContentId policy, TestableController controller, TopModel topModel) {
             this.policy = policy;
            this.model = new TopModelImpl((ModelTransparent) ((TopModelImpl) model).getModel().clone());
             this.controller = controller;
         }
 
         public ContentId getPolicy() {
             return this.policy;
         }
 
         public TopModel getModel() {
            return this.model;
         }
 
         public TestableController getController() {
             return this.controller;
         }
     }
 
     private static Collection<RenderOperation> operations = new ArrayList<RenderOperation>();
 
 	public static void store(
 			com.polopoly.ps.test.RenderOperationStorage.RenderOperation renderOperation) {
 		if (storeOperations) {
 			operations.add(renderOperation);
 		}
 	}
     
 }
