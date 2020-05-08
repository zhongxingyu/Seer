 /**
  */
 package com.github.kanafghan.welipse.webdsl.impl;
 
 import java.util.ArrayList;
 
 import com.github.kanafghan.welipse.webdsl.Page;
 import com.github.kanafghan.welipse.webdsl.WebDSLPackage;
 import com.github.kanafghan.welipse.webdsl.WebUtilExp;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Web Util Exp</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * </p>
  *
  * @generated
  */
 public class WebUtilExpImpl extends PropertyOperationImpl implements WebUtilExp {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected WebUtilExpImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return WebDSLPackage.Literals.WEB_UTIL_EXP;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public EClassifier type() {
 		// We don't have a type for this expression
 		return null;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public void initialize(Page page) {
 		final String OPERATION = "getAll";
 		java.util.List<String> availableOperations = new ArrayList<String>();
 		
 		boolean isValidOperation = false;
 		Resource resource = page.eResource();
 		if (resource != null) {
 			ResourceSet rSet = resource.getResourceSet();
 			for (Resource r : rSet.getResources()) {
 				if (!r.getURI().equals(resource.getURI())) {
 					if (r.isLoaded()) {
 						TreeIterator<EObject> iterator = r.getAllContents();
 						while (iterator.hasNext()) {
 							EObject obj = iterator.next();
 							if (obj instanceof EClassifier) {
 								EClassifier cls = (EClassifier) obj;
 								String utilOperation = OPERATION.concat(cls.getName());
 								availableOperations.add(utilOperation);
 								if (utilOperation.equals(getIdentifier())) {
 									isValidOperation = true;
 									break;
 								}
 							}
 						}
 					}
 				}
 				if (isValidOperation) {
 					break;
 				}
 			}
 		}	
 		if (!isValidOperation) {
 			throw new Error("The operation '"+ getIdentifier() +"()' is not among the available utility operations: '"
 					+ availableOperations.toString() +"'.");
 		}
 	}
 
 } //WebUtilExpImpl
