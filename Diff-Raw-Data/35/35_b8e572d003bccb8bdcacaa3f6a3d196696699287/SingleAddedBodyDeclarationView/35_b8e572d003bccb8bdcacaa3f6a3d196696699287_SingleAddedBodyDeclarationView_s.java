 package de.fkoeberle.autocommit.message.java;
 
 import java.io.IOException;
 
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 
 import de.fkoeberle.autocommit.message.InjectedBySession;
 
 public class SingleAddedBodyDeclarationView extends
 		AbstractViewWithCache<BodyDeclaration> {
 
 	@InjectedBySession
 	private SingleChangedTypeView singleChangedTypeView;
 
 
 	@Override
 	protected BodyDeclaration determineCachableValue() throws IOException {
 		TypeDelta typeDelta = singleChangedTypeView.getTypeDelta();
 		if (typeDelta == null) {
 			return null;
 		}
 
		if (typeDelta.isDeclarationListOnlyChange()) {
 			return null;
 		}
 		DeclarationListDelta declarationListDelta = typeDelta
 				.getDeclarationListDelta();
 
 		if (declarationListDelta.getAddedDeclarations().size() != 1) {
 			return null;
 		}
 
 		if (declarationListDelta.getChangedDeclarations().size() != 0) {
 			return null;
 		}
 
 		if (declarationListDelta.getRemovedDeclarations().size() != 0) {
 			return null;
 		}
 
 		return declarationListDelta.getAddedDeclarations().get(0);
 	}
 
 	public BodyDeclaration getAddedDeclaration() throws IOException {
 		return getCachableValue();
 	}
 
 }
