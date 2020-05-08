 package org.openetcs.es3f;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecp.core.ECPProject;
 import org.eclipse.emf.edit.command.ChangeCommand;
 import org.openetcs.es3f.generated.Dictionary;
 import org.openetcs.es3f.generated.Message;
 import org.openetcs.es3f.generated.MsgVariable;
 import org.openetcs.es3f.generated.Paragraph;
 import org.openetcs.es3f.generated.Specification;
 import org.openetcs.es3f.generated.TypeSpec;
 import org.openetcs.es3f.generated.Visitor;
 import org.openetcs.es3f.generated.acceptor;
 import org.openetcs.es3f.generated.meaning;
 import org.openetcs.es3f.importer.Importer;
 import org.openetcs.es3f.utils.ManualTranslation;
 import org.openetcs.model.ertmsformalspecs.BaseLine;
 import org.openetcs.model.ertmsformalspecs.FunctionalBlocks;
 import org.openetcs.model.ertmsformalspecs.ModelFactory;
 import org.openetcs.model.ertmsformalspecs.ModelPackage;
 import org.openetcs.model.ertmsformalspecs.requirements.FunctionalBlock;
 import org.openetcs.model.ertmsformalspecs.requirements.RequirementsFactory;
 
 import com.raincode.xmlbooster.xmlb.xmlBException;
 import com.raincode.xmlbooster.xmlb.xmlBFileContext;
 
 
 /**
  * This is the Util class for importing a es3f file containing a model into an {@link ECPProject}.
  * 
  * @author Eugen Neufeld
  *
  */
 public class ImportUtil {
 
 	private static class ImportContext 
 		extends Visitor
 	{
 		public HashSet<String> Baselines = new HashSet<String>();
 		public HashSet<String> FunctionalBlocks = new HashSet<String>();
 		
 		@Override
 		public void visit(Specification obj, boolean visitSubNodes) {
 			if ( obj.getVersion() != null )
 			{
 				Baselines.add(obj.getVersion());
 			}
 			super.visit(obj, visitSubNodes);
 		}
 		
 		@Override
 		public void visit(Paragraph obj, boolean visitSubNodes) {
 			if ( obj.getVersion() != null )
 			{
 				Baselines.add(obj.getVersion());
 			}
 			if ( obj.getBl() != null )
 			{
 				Baselines.add(obj.getBl());
 			}
 			
 			if ( obj.getFunctionalBlockName()!=null )
 			{
 				FunctionalBlocks.add(obj.getFunctionalBlockName());
 			}
 
 			super.visit(obj, visitSubNodes);
 		}
 		
 		@Override
 		public void visit(Message obj, boolean visitSubNodes) {
 			if ( obj.getBl() != null )
 			{
 				Baselines.add(obj.getBl());
 			}
 
 			super.visit(obj, visitSubNodes);
 		}
 		
 		@Override
 		public void visit(MsgVariable obj, boolean visitSubNodes) {
 			if ( obj.getBl() != null )
 			{
 				Baselines.add(obj.getBl());
 			}
 
 			super.visit(obj, visitSubNodes);
 		}
 	
 		@Override
 		public void visit(TypeSpec obj, boolean visitSubNodes) {
 			if ( obj.getBl() != null )
 			{
 				Baselines.add(obj.getBl());
 			}
 
 			super.visit(obj, visitSubNodes);
 		}
 		
 		@Override
 		public void visit(meaning obj, boolean visitSubNodes) {
 			if ( obj.getBl() != null )
 			{
 				Baselines.add(obj.getBl());
 			}
 
 			super.visit(obj, visitSubNodes);
 		}
 	}
 	
 	public static void importModel(File fileToImport, final ECPProject importProject, IProgressMonitor monitor)
 	{
 		monitor.beginTask("Importing project...", 100);
 		xmlBFileContext ctxt = new xmlBFileContext();
 		monitor.subTask("Reading file...");
 		if ( ctxt.readFile(fileToImport.getAbsolutePath()) )
 		{
 			try 
 			{
 				final org.openetcs.es3f.generated.Dictionary dictionary = acceptor.acceptDictionary(ctxt);
 			
 				if ( dictionary != null )
 				{
 					monitor.subTask("Filling context...");
 					ensureProjectContextIsCreated(importProject);
 					fillContext(importProject, dictionary);
 					monitor.worked(10);
 					
 
 					ManualTranslation translation = new ManualTranslation();
 					monitor.subTask("Importing dictionary...");
 					final org.openetcs.model.ertmsformalspecs.Dictionary importedDictionary = Importer.importDictionary(importProject, translation, dictionary);
 					monitor.worked(50);
 					monitor.subTask("Resolving cross references...");
 					translation.crossReference(importProject);
 					monitor.worked(20);
 					monitor.subTask("Adding element to project...");
 					importProject.getEditingDomain().getCommandStack().execute(new ChangeCommand(importedDictionary) {
 						@Override
 						protected void doExecute() {
							importProject.getElements().add(importedDictionary);
 						}
 					});
 					monitor.worked(20);
 					monitor.done();
 				}
 				else 
 				{
 					throw new RuntimeException("Cannot read file contents");
 				}
 			} 
 			catch (xmlBException e)
 			{
 				throw new RuntimeException("Cannot read file contents", e);				
 			}
 		}
 		else 
 		{
 			throw new RuntimeException("Cannot open file " + fileToImport.getAbsolutePath());
 		}
 	}
 
 	private static org.openetcs.model.ertmsformalspecs.Baselines findBaselines(final ECPProject importProject)
 	{
 		org.openetcs.model.ertmsformalspecs.Baselines retVal = null;
 		
		List<Object> elements=importProject.getElements();
 		for(Object object:elements)
 		{
 			EObject eObject=(EObject)object;
 			if (ModelPackage.eINSTANCE.getBaselines().equals(eObject.eClass()))
 			{
 				retVal = (org.openetcs.model.ertmsformalspecs.Baselines) eObject;
 				break;
 			}
 		}
 		
 		return retVal;
 	}
 	
 	private static org.openetcs.model.ertmsformalspecs.FunctionalBlocks findFunctionalBlocks(final ECPProject importProject) 
 	{
 		org.openetcs.model.ertmsformalspecs.FunctionalBlocks retVal = null;
 		
		List<Object> elements=importProject.getElements();
 		for(Object object:elements)
 		{
 			EObject eObject=(EObject)object;
 			if (ModelPackage.eINSTANCE.getFunctionalBlocks().equals(eObject.eClass()))
 			{
 				retVal = (org.openetcs.model.ertmsformalspecs.FunctionalBlocks) eObject;
 				break;
 			}
 		}
 		
 		return retVal;
 	}
 	
 	private static void ensureProjectContextIsCreated(final ECPProject importProject) 
 	{
 		if ( findBaselines(importProject) == null )
 		{
 			final org.openetcs.model.ertmsformalspecs.Baselines newBaselines = ModelFactory.eINSTANCE.createBaselines();
 			importProject.getEditingDomain().getCommandStack().execute(new ChangeCommand(newBaselines) {
 				@Override
 				protected void doExecute() {
					importProject.getElements().add(newBaselines);
 				}
 			});
 		}
 		
 		if ( findFunctionalBlocks(importProject) == null )
 		{
 			final org.openetcs.model.ertmsformalspecs.FunctionalBlocks newFunctionalBlocks = ModelFactory.eINSTANCE.createFunctionalBlocks();
 			importProject.getEditingDomain().getCommandStack().execute(new ChangeCommand(newFunctionalBlocks) {
 				@Override
 				protected void doExecute() {
					importProject.getElements().add(newFunctionalBlocks);
 				}
 			});
 		}
 	}
 
 	private static void fillContext(
 			final ECPProject importProject,
 			final org.openetcs.es3f.generated.Dictionary dictionary) 
 	{
 		final ImportContext context = new ImportContext();
 		context.visit(dictionary,true);
 		
 		final org.openetcs.model.ertmsformalspecs.Baselines baselines = findBaselines(importProject);
 		for ( String baselineName : context.Baselines )
 		{
 			boolean found = false;
 			
 			for ( BaseLine baseline : baselines.getAvailableBaselines() )
 			{
 				if ( baseline.getName().equals(baselineName))
 				{
 					found = true;
 					break;
 				}
 			}
 			
 			if ( !found )
 			{
 				final org.openetcs.model.ertmsformalspecs.BaseLine newBaseLine = ModelFactory.eINSTANCE.createBaseLine();
 				newBaseLine.setName(baselineName);
 				
 				importProject.getEditingDomain().getCommandStack().execute(new ChangeCommand(newBaseLine) {
 					@Override
 					protected void doExecute() {
 						baselines.getAvailableBaselines().add(newBaseLine);
 					}
 				});
 				
 			}
 		}
 
 		final org.openetcs.model.ertmsformalspecs.FunctionalBlocks functionalBlocks = findFunctionalBlocks(importProject);
 		for ( String functionalBlockName : context.FunctionalBlocks )
 		{
 			boolean found = false;
 			
 			for ( FunctionalBlock functionalBlock : functionalBlocks.getAvailableFunctionalBlocks() )
 			{
 				if ( functionalBlock.getName().equals(functionalBlockName))
 				{
 					found = true;
 					break;
 				}
 			}
 			
 			if ( !found )
 			{
 				final FunctionalBlock newFunctionalBlock = RequirementsFactory.eINSTANCE.createFunctionalBlock();
 				newFunctionalBlock.setName(functionalBlockName);
 				
 				importProject.getEditingDomain().getCommandStack().execute(new ChangeCommand(newFunctionalBlock) {
 					@Override
 					protected void doExecute() {
 						functionalBlocks.getAvailableFunctionalBlocks().add(newFunctionalBlock);
 					}
 				});
 			}
 		}
 
 	}
 }
