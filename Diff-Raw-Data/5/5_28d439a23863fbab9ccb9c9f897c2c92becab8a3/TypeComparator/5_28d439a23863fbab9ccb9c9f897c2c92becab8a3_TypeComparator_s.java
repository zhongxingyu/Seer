 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.common.internal.types;
 
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jdt.core.Signature;
 
 /**
  * Static utility class used to compare two CompositeTypes for compatability
  * 
  * @author cbateman
  *
  */
 public final class TypeComparator
 {
     /**
      * @param firstType
      * @param secondType
      * @return true if firstType is assignable to secondType or vice-versa,
      * depending on their assignment and runtime types
      */
     public static Diagnostic calculateTypeCompatibility(final CompositeType firstType,
                                                         final CompositeType secondType)
     {
         // first, box all primitives
         final CompositeType boxedFirstType = 
             TypeTransformer.transformBoxPrimitives(firstType);
         final CompositeType boxedSecondType =
             TypeTransformer.transformBoxPrimitives(secondType);
         
         final String[] mustBeSatisfied = boxedFirstType.getSignatures();
         final String[] testSignatures = boxedSecondType.getSignatures();
         // TODO: need better user messages here
        Diagnostic result = new BasicDiagnostic(Diagnostic.ERROR, "", 0,  //$NON-NLS-1$
                 Messages.getString("TypeComparator.Expression.Doesnt.Match.Expected.Types"), null); //$NON-NLS-1$
         // now loop through each type in the first type and see
         // if there is a type satisfying it in the second
         MAIN_LOOP:
         for  (int i = 0; i < mustBeSatisfied.length; i++)
         {
             final String curSatisfyType = mustBeSatisfied[i];
 
             CHECK_CANDIDATES:
             for (int j = 0; j < testSignatures.length; j++)
             {
                 final String testType = testSignatures[j];
                 
                 // simplest success is an exact match
                 if (curSatisfyType.equals(testType))
                 {
                     // check assignability mask
                     // returns Diagnostic.OK if okay
                     result = checkAssignability(firstType, secondType);
                     break MAIN_LOOP;
                 }
                 
                 // or if both are methods, check to see if the
                 // method signatures match
                 if (TypeUtil.isMethodSignature(curSatisfyType))
                 {
                     // if the satisfy type is a method,
                     // the test  type is not, then don't go any
                     // further, since we know the won't match
                     if (!TypeUtil.isMethodSignature(testType))
                     {
                         continue CHECK_CANDIDATES;
                     }
 
                     final Diagnostic test =
                         methodSignaturesMatch(curSatisfyType, testType);
                     
                     if (test.getSeverity() ==  Diagnostic.OK)
                     {
                         result = Diagnostic.OK_INSTANCE;
                         // found a match so break
                         break MAIN_LOOP;
                     }
                 }
                 
                 // or, can we coerce testType to curSatisfyType
                 if (canCoerce(testType, curSatisfyType, firstType.isLHS()))
                 {
                     result = checkAssignability(firstType, secondType);
                     break MAIN_LOOP;
                 }
             }
         }
 
         return result;
     }
     
     private static boolean canCoerce(String testType, String checkType,
                                                 boolean checkTypeIsWritable)
     {
         boolean canCoerce = canCoerce(testType, checkType);
         
         // if the check type is writable, we need to be sure that the
         // coercion can work in both directions
         if (canCoerce && checkTypeIsWritable)
         {
             // reverse roles: can checkType assign back to test type?
             canCoerce &= canCoerce(checkType, testType);
         }
         
         return canCoerce;
     }
     
     private static boolean canCoerce(String testType, String checkType)
     {
         // can always to coerce to string
         if (TypeCoercer.typeIsString(checkType))
         {
             // if check type expects writability, need to ensure that 
             // coercability is reversible
             return true;
         }
         else if (TypeCoercer.typeIsNumeric(checkType))
         {
             return canCoerceNumeric(testType);
         }
         else if (TypeCoercer.typeIsBoolean(checkType))
         {
             return TypeCoercer.canCoerceToBoolean(testType);
         }
         
         // otherwise, no type coercion available
         return false;
     }
     
     private static boolean canCoerceNumeric(String testType)
     {
         try
         {
             TypeCoercer.coerceToNumber(testType);
             // TODO: there is a case when coerceToNumber returns
             // null meaning "not sure", that we may want to handle
             // differently, with a warning
             return true;
         }
         catch (TypeCoercionException tce)
         {
             // outright failure -- can't coerce
             return false;
         }
     }
     
     private static Diagnostic methodSignaturesMatch(String firstMethodSig, String secondMethodSig)
     {
         // TODO: need to account for primitive type coercions
         final String[]  firstMethodParams = 
             Signature.getParameterTypes(firstMethodSig);
         final String[]  secondMethodParams =
             Signature.getParameterTypes(secondMethodSig);
         
         // fail fast if param count doesn't match
         if (firstMethodParams.length != secondMethodParams.length)
         {
             return new BasicDiagnostic(Diagnostic.ERROR, "", 0,  //$NON-NLS-1$
                     Messages.getString("TypeComparator.Parameter.count.mismatch"), null); //$NON-NLS-1$
         }
         
         // now check each parameter
         for (int i = 0; i < firstMethodParams.length; i++)
         {
             // need to box primitives before comparing
             final String firstMethodParam = 
                 TypeTransformer.transformBoxPrimitives(firstMethodParams[i]);
             final String secondMethodParam = 
                 TypeTransformer.transformBoxPrimitives(secondMethodParams[i]);
             
             if (!firstMethodParam.equals(secondMethodParam))
             {
                 return new BasicDiagnostic(Diagnostic.ERROR, "", 0, //$NON-NLS-1$
                         Messages.getString("TypeComparator.Type.mismatch.on.parameter")+i, null); //$NON-NLS-1$
             }
         }
         
         // if we get to here then we need only check the return type
         final String firstReturn =
             TypeTransformer.transformBoxPrimitives(Signature.getReturnType(firstMethodSig));
         final String secondReturn =
             TypeTransformer.transformBoxPrimitives(Signature.getReturnType(secondMethodSig));
         
         if (!firstReturn.equals(secondReturn))
         {
             return new BasicDiagnostic(Diagnostic.ERROR, "", 0, //$NON-NLS-1$
                     Messages.getString("TypeComparator.Return.Types.Dont.Match"), null); //$NON-NLS-1$
         }
         
         // if we get to here, then everything checks out
         return Diagnostic.OK_INSTANCE;
     }
     
     /**
      * Precond: both firstType and secondType must represent value bindings.
      * 
      * @param firstType
      * @param secondType
      * @return a diagnostic validating that the two composite have compatible
      * assignability
      */
     private static Diagnostic checkAssignability(CompositeType firstType, CompositeType secondType)
     {
         if (firstType.isRHS() && !secondType.isRHS())
         {
            return new BasicDiagnostic(Diagnostic.ERROR, "", 0,  //$NON-NLS-1$
                             Messages.getString("TypeComparator.Expression.Not.Gettable"), null); //$NON-NLS-1$
         }
         
         if (firstType.isLHS() && !secondType.isLHS())
         {
             return new BasicDiagnostic(Diagnostic.WARNING, "", 0, //$NON-NLS-1$
                             Messages.getString("TypeComparator.Expression.Expected.Settable"), null); //$NON-NLS-1$
         }
         
         return Diagnostic.OK_INSTANCE;
     }
     
     private TypeComparator()
     {
         // static utility class; not instantiable
     }
 }
