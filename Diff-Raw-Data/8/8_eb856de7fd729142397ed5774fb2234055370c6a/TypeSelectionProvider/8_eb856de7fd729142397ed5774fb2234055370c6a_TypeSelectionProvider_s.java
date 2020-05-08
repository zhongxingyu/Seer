 package org.fest.eclipse.assertions.generator.internal.commands;
 
 import java.util.Collection;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.expressions.IEvaluationContext;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import org.fest.eclipse.assertions.generator.internal.log.Logger;
 
 /**
  * Understands how to extract a {@link IType} from an {@link ExecutionEvent} corresponding to a user selecting a class to generate assertion for.
  */
 public class TypeSelectionProvider {
 
   private final Logger logger;
 
   public TypeSelectionProvider(Logger logger) {
     this.logger = logger;
   }
 
   public IType getSelectedType(ExecutionEvent event) {
     Object selectedElement = getUniqueSelectedElement(event);
     if (selectedElement instanceof IAdaptable) {
       IType type = toType((IAdaptable) selectedElement);
       if (type != null) {
         logger.debug("Found type " + type.getFullyQualifiedName() + " in selection");
         return type;
       }
     }
 
     IType type = toType(getActiveEditorInput(event));
     if (type != null) {
       logger.debug("Found type " + type.getFullyQualifiedName() + " in editor");
       return type;
     }
 
     return null;
   }
 
   private Object getUniqueSelectedElement(ExecutionEvent event) {
     if (!(event.getApplicationContext() instanceof IEvaluationContext)) {
       return null;
     }
 
     IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
 
     Collection<?> selectedElements = (Collection<?>) context.getDefaultVariable();
     if (selectedElements == null || selectedElements.size() != 1) {
       return null;
     }
 
     return selectedElements.iterator().next();
   }
 
   private IType toType(IAdaptable adaptable) {
     if (adaptable == null) {
       return null;
     }
 
     IType type = (IType) adaptable.getAdapter(IType.class);
     if (type != null) {
       return type;
     }
 
     ICompilationUnit cu = (ICompilationUnit) adaptable.getAdapter(ICompilationUnit.class);
     if (cu != null) {
       return cu.findPrimaryType();
     }
 
     IFile file = (IFile) adaptable.getAdapter(IFile.class);
     if (file != null) {
       cu = JavaCore.createCompilationUnitFrom(file);
       return cu.findPrimaryType();
     }
 
     return null;
   }
 
   private static IEditorInput getActiveEditorInput(ExecutionEvent event) {
    Object o = HandlerUtil.getVariable(event, ISources.ACTIVE_EDITOR_INPUT_NAME);
    if (o instanceof IEditorInput) {
      return (IEditorInput) o;
     }
     return null;
   }
 }
