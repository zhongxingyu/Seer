 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 import java.util.Collection;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BChainedExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BChained Expression</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BChainedExpressionImpl#getExpressions <em>Expressions</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class BChainedExpressionImpl extends BExpressionImpl implements BChainedExpression {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 	/**
 	 * The cached value of the '{@link #getExpressions() <em>Expressions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExpressions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<BExpression> expressions;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BChainedExpressionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BCHAINED_EXPRESSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<BExpression> getExpressions() {
 		if (expressions == null) {
 			expressions = new EObjectContainmentEList<BExpression>(BExpression.class, this, B3backendPackage.BCHAINED_EXPRESSION__EXPRESSIONS);
 		}
 		return expressions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BCHAINED_EXPRESSION__EXPRESSIONS:
 				return ((InternalEList<?>)getExpressions()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case B3backendPackage.BCHAINED_EXPRESSION__EXPRESSIONS:
 				return getExpressions();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case B3backendPackage.BCHAINED_EXPRESSION__EXPRESSIONS:
 				getExpressions().clear();
 				getExpressions().addAll((Collection<? extends BExpression>)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case B3backendPackage.BCHAINED_EXPRESSION__EXPRESSIONS:
 				getExpressions().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case B3backendPackage.BCHAINED_EXPRESSION__EXPRESSIONS:
 				return expressions != null && !expressions.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 	/**
 	 * Returns the result of the last expression in the expression block, or if the expression
 	 * is a return expression, this value is returned without evaluating the rest of the expressions
 	 * in the list.
 	 */
 	@Override
 	public Object evaluate(BExecutionContext ctx) throws Throwable {
 		Object result = null;
		for(BExpression expr : expressions)
 			result = expr.evaluate(ctx);
 		return result;
 	}
 	/**
 	 * Returns the type of the last expression in the chain.
 	 */
 	@Override
 	public Type getDeclaredType(BExecutionContext ctx) throws Throwable {
		if(expressions.size() == 0)
 			return Object.class; // TODO: This may be too relaxed  - should perhaps be <?>
 		return expressions.get(expressions.size()-1).getDeclaredType(ctx);
 	}
 } //BChainedExpressionImpl
