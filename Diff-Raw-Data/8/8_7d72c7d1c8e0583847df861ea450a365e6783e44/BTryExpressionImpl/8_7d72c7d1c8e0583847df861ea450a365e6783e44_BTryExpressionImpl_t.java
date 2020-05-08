 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 import java.util.Collection;
 
 import org.eclipse.b3.backend.core.B3EngineException;
 import org.eclipse.b3.backend.evaluator.BackendHelper;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BCatch;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BTryExpression;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BTry Expression</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BTryExpressionImpl#getTryExpr <em>Try Expr</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BTryExpressionImpl#getCatchBlocks <em>Catch Blocks</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BTryExpressionImpl#getFinallyExpr <em>Finally Expr</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class BTryExpressionImpl extends BExpressionImpl implements BTryExpression {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The cached value of the '{@link #getTryExpr() <em>Try Expr</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTryExpr()
 	 * @generated
 	 * @ordered
 	 */
 	protected BExpression tryExpr;
 
 	/**
 	 * The cached value of the '{@link #getCatchBlocks() <em>Catch Blocks</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCatchBlocks()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<BCatch> catchBlocks;
 
 	/**
 	 * The cached value of the '{@link #getFinallyExpr() <em>Finally Expr</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFinallyExpr()
 	 * @generated
 	 * @ordered
 	 */
 	protected BExpression finallyExpr;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BTryExpressionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BTRY_EXPRESSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExpression getTryExpr() {
 		return tryExpr;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetTryExpr(BExpression newTryExpr, NotificationChain msgs) {
 		BExpression oldTryExpr = tryExpr;
 		tryExpr = newTryExpr;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BTRY_EXPRESSION__TRY_EXPR, oldTryExpr, newTryExpr);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTryExpr(BExpression newTryExpr) {
 		if (newTryExpr != tryExpr) {
 			NotificationChain msgs = null;
 			if (tryExpr != null)
 				msgs = ((InternalEObject)tryExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BTRY_EXPRESSION__TRY_EXPR, null, msgs);
 			if (newTryExpr != null)
 				msgs = ((InternalEObject)newTryExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BTRY_EXPRESSION__TRY_EXPR, null, msgs);
 			msgs = basicSetTryExpr(newTryExpr, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BTRY_EXPRESSION__TRY_EXPR, newTryExpr, newTryExpr));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<BCatch> getCatchBlocks() {
 		if (catchBlocks == null) {
 			catchBlocks = new EObjectContainmentEList<BCatch>(BCatch.class, this, B3backendPackage.BTRY_EXPRESSION__CATCH_BLOCKS);
 		}
 		return catchBlocks;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExpression getFinallyExpr() {
 		return finallyExpr;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFinallyExpr(BExpression newFinallyExpr, NotificationChain msgs) {
 		BExpression oldFinallyExpr = finallyExpr;
 		finallyExpr = newFinallyExpr;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR, oldFinallyExpr, newFinallyExpr);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFinallyExpr(BExpression newFinallyExpr) {
 		if (newFinallyExpr != finallyExpr) {
 			NotificationChain msgs = null;
 			if (finallyExpr != null)
 				msgs = ((InternalEObject)finallyExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR, null, msgs);
 			if (newFinallyExpr != null)
 				msgs = ((InternalEObject)newFinallyExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR, null, msgs);
 			msgs = basicSetFinallyExpr(newFinallyExpr, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR, newFinallyExpr, newFinallyExpr));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BTRY_EXPRESSION__TRY_EXPR:
 				return basicSetTryExpr(null, msgs);
 			case B3backendPackage.BTRY_EXPRESSION__CATCH_BLOCKS:
 				return ((InternalEList<?>)getCatchBlocks()).basicRemove(otherEnd, msgs);
 			case B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR:
 				return basicSetFinallyExpr(null, msgs);
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
 			case B3backendPackage.BTRY_EXPRESSION__TRY_EXPR:
 				return getTryExpr();
 			case B3backendPackage.BTRY_EXPRESSION__CATCH_BLOCKS:
 				return getCatchBlocks();
 			case B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR:
 				return getFinallyExpr();
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
 			case B3backendPackage.BTRY_EXPRESSION__TRY_EXPR:
 				setTryExpr((BExpression)newValue);
 				return;
 			case B3backendPackage.BTRY_EXPRESSION__CATCH_BLOCKS:
 				getCatchBlocks().clear();
 				getCatchBlocks().addAll((Collection<? extends BCatch>)newValue);
 				return;
 			case B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR:
 				setFinallyExpr((BExpression)newValue);
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
 			case B3backendPackage.BTRY_EXPRESSION__TRY_EXPR:
 				setTryExpr((BExpression)null);
 				return;
 			case B3backendPackage.BTRY_EXPRESSION__CATCH_BLOCKS:
 				getCatchBlocks().clear();
 				return;
 			case B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR:
 				setFinallyExpr((BExpression)null);
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
 			case B3backendPackage.BTRY_EXPRESSION__TRY_EXPR:
 				return tryExpr != null;
 			case B3backendPackage.BTRY_EXPRESSION__CATCH_BLOCKS:
 				return catchBlocks != null && !catchBlocks.isEmpty();
 			case B3backendPackage.BTRY_EXPRESSION__FINALLY_EXPR:
 				return finallyExpr != null;
 		}
 		return super.eIsSet(featureID);
 	}
 	@Override
 	public Object evaluate(BExecutionContext ctx) throws Throwable {
 		try {
 			return tryExpr.evaluate(ctx);
 		} catch(Throwable t) {
 			// select catch block, or if exception uncaught, re-throw
 			for(BCatch catchBlock : catchBlocks) {
				if((TypeUtils.getRaw((catchBlock.getExceptionType())).isAssignableFrom(t.getClass()))) {
 					BExecutionContext inner = ctx.createInnerContext();
 					try {
 						inner.defineValue(catchBlock.getVarname(), t, catchBlock.getExceptionType());
 					} catch (B3EngineException e) {
 						throw BackendHelper.createException(this, e, 
 								"Could not create closure to evaluate catch.");
 					}
 					return catchBlock.evaluate(inner);
 				}
 			}
 			// no match
 			throw t;
 		} finally {
			if(finallyExpr != null)
				finallyExpr.evaluate(ctx);
 		}
 	}
 	/** 
 	 * Returns the common super type of the try and all catch expressions.
 	 */
 	@Override
 	public Type getDeclaredType(BExecutionContext ctx) throws Throwable {
 		Type[] types = new Type[catchBlocks.size() + 1];
 		types[0] = tryExpr.getDeclaredType(ctx);
 		int counter = 1;
 		for(BCatch catchBlock : catchBlocks) {
 			BExecutionContext inner = ctx.createInnerContext();
 			inner.defineValue(catchBlock.getVarname(), catchBlock.getExceptionType(), null);
 			types[counter++] = catchBlock.getDeclaredType(inner);
 		}
 		return TypeUtils.getCommonSuperType(types);
 	}
 } //BTryExpressionImpl
