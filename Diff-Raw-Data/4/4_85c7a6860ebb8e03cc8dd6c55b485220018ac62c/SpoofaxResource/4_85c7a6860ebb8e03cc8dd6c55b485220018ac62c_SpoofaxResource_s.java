 package org.spoofax.modelware.emf.resource;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.CommonPlugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.modelware.emf.Model2Term;
 import org.spoofax.modelware.emf.Term2Model;
 import org.spoofax.terms.TermFactory;
 import org.strategoxt.imp.generator.construct_textual_change_4_0;
 import org.strategoxt.imp.runtime.Environment;
 import org.strategoxt.imp.runtime.FileState;
 import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
 import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
 import org.strategoxt.imp.runtime.dynamicloading.RefactoringFactory;
 import org.strategoxt.imp.runtime.services.StrategoObserver;
 import org.strategoxt.lang.Context;
 import org.strategoxt.lang.Strategy;
 
 public class SpoofaxResource extends ResourceImpl {
 
 	protected IPath filePath;
 	protected ITermFactory termFactory;
 	protected FileState fileState;
 
 	public SpoofaxResource(URI uri) {
 		this.uri = uri;
 
 		URI resolvedFile = CommonPlugin.resolve(uri);
 		this.filePath = new Path(resolvedFile.toFileString());
 
 		this.termFactory = new TermFactory();
 	}
 
 	/**
 	 * @override
 	 */
 	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
 		IStrategoTerm analysedAST = null;
 		try {
 			fileState = FileState.getFile(filePath, null);
 			analysedAST = fileState.getAnalyzedAst();
 		} catch (BadDescriptorException | FileNotFoundException | ModelException e) {
 			e.printStackTrace();
 		}
 
 		if (analysedAST == null)
 			;
 
 		String languageName = null;
 		try {
 			languageName = fileState.getDescriptor().getLanguage().getName();
 		} catch (BadDescriptorException e) {
 			e.printStackTrace();
 		}
 		EPackage ePackage = EPackageRegistryImpl.INSTANCE.getEPackage(languageName);
 
 		EObject eObject = null;
 
 		if (analysedAST == null) {
 			EAnnotation rootElementAnnotation = ePackage.getEAnnotation("Spoofax");
 			if (rootElementAnnotation == null || rootElementAnnotation.getDetails().get("RootElement") == null) {
 				Environment.logException("Root class unspecified");
 			} else {
 				EClass rootClassifier = (EClass) ePackage.getEClassifier(rootElementAnnotation.getDetails().get("RootElement"));
 				if (rootClassifier != null) {
 					eObject = ePackage.getEFactoryInstance().create(rootClassifier);
 				}
 			}
 
 		} else {
 			Term2Model term2Model = new Term2Model(ePackage);
 			eObject = term2Model.convert(analysedAST);
 		}
 
 		getContents().add(0, eObject);
 	}
 
 	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
 		EObject object = getContents().get(0);
 		Model2Term model2term = new Model2Term(new TermFactory());
 
 		IStrategoTerm newAST = model2term.convert(object);
 
 		if (fileState == null) {
 			try {
 				outputStream.write("".getBytes());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return;
 		}
 
 		IStrategoTerm oldAST = fileState.getCurrentAst();
 		IStrategoTerm resultTuple = termFactory.makeList(termFactory.makeTuple(oldAST, newAST));
 
 		File file = filePath.toFile();
 		IStrategoTerm textreplace = null;
 		String result = null;
 
 		// TODO call TextReplacer instead
 		try {
 			Descriptor descriptor = fileState.getDescriptor();
 			StrategoObserver observer = descriptor.createService(StrategoObserver.class, fileState.getParseController());
 			textreplace = construct_textual_change_4_0.instance.invoke(observer.getRuntime().getCompiledContext(), resultTuple, createStrategy(RefactoringFactory.getPPStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getParenthesizeStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getOverrideReconstructionStrategy(descriptor), file, observer), createStrategy(RefactoringFactory.getResugarStrategy(descriptor), file, observer));
 			result = ((IStrategoString) textreplace.getSubterm(0).getSubterm(2)).stringValue();
 		} catch (BadDescriptorException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			outputStream.write(result.getBytes());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// TODO: remove from here
 	private Strategy createStrategy(final String sname, final File file, final StrategoObserver observer) {
 		return new Strategy() {
 			@Override
 			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
 				if (sname != null)
 					return observer.invokeSilent(sname, current, file);
 				return null;
 			}
 		};
 	}
 }
