 /*
  * Copyright (C) 2005 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util;
 
 import java.lang.reflect.Array;
 
 /**
  * Collection of methods making implementation of hashcode method easier.
  * 
  * Example how hashCode can be implemented using this method:
  * 
  * public int hashCode()
  * {
  *    int iResult = HashCodeUtils.SEED;
  *    iResult = HashCodeUtils.hash(iResult, primitiveValue);
  *    iResult = HashCodeUtils.hash(iResult, object);
  *    iResult = HashCodeUtils.hash(iResult, array);
  *    return iResult;
  * }
  * 
  * This code was inspired by class published at
  * http://www.javapractices.com/Topic28.cjp
  * 
  * @author bastafidli
  */
 public final class HashCodeUtils extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * An initial value to decreases collisions of computed values.
     */
    public static final int SEED = 15;
 
    /**
     * Constant to add to seed or previous term.
     */
    public static final int ODD_PRIME_NUMBER = 37;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /** 
     * Private constructor since this class cannot be instantiated
     */
    private HashCodeUtils(
    )
    {
       // Do nothing
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Hash for booleans.
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param bValue - value contributing to hashcode
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int     iSeed, 
       boolean bValue
    )
    {
       return firstTerm(iSeed) + (bValue ? 1 : 0);
    }
 
    /**
     * Hash for chars.
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param cValue - value contributing to hashcode
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int  iSeed, 
       char cValue
    )
    {
       return firstTerm(iSeed) + (int)cValue;
    }
 
    /**
     * Hash for ints. Byte and short are handled by this method, through implicit 
     * conversion.
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param iValue - value contributing to hashcode
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int iSeed, 
       int iValue
    )
    {
       return firstTerm(iSeed) + iValue;
    }
 
    /**
     * Hash for longs.
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param lValue - value contributing to hashcode
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int  iSeed, 
       long lValue
    )
    {
       return firstTerm(iSeed) + (int)(lValue ^ (lValue >>> 32));
    }
 
    /**
     * Hash for floats.
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param fValue - value contributing to hashcode
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int   iSeed, 
       float fValue
    )
    {
       return hash(iSeed, Float.floatToIntBits(fValue));
    }
 
    /**
     * Hash for doubles.
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param dValue - value contributing to hashcode
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int    iSeed, 
       double dValue
    )
    {
       return hash(iSeed, Double.doubleToLongBits(dValue));
    }
 
    /**
     * Hash for objects .
     * 
     * @param iSeed - previous hashcode contributor or seed value
     * @param oValue - value contributing to hashcode. It is a possibly null 
     *                 object field, and possibly an array. If it is an array, 
     *                 then each element may be a primitive or a possibly null 
     *                 object.
     * @return int - computed hashcode contributor
     */
    public static int hash(
       int  iSeed, 
       Object oValue
    )
    {
       int result = iSeed;
       if (oValue == null)
       {
          result = hash(result, 0);
       }
       else if (!isArray(oValue))
       {
          result = hash(result, oValue.hashCode());
       }
       else
       {
          int length = Array.getLength(oValue);
          for (int idx = 0; idx < length; ++idx)
          {
             Object item = Array.get(oValue, idx);
             // recursive call!
             result = hash(result, item);
          }
       }
       return result;
    }
    
    // Helper methods ///////////////////////////////////////////////////////////
 
    /**
     * Compute first term for hashcode
     * 
     * @param iSeed - value to use to compute first term
     * @return int - hashcode contributor
     */
   protected static int firstTerm(
       int iSeed
    )
    {
       return ODD_PRIME_NUMBER * iSeed;
    }
 
    /**
     * Test if object is an array
     * 
     * @param oObject - object to test
     * @return boolean
     */
   protected static boolean isArray(
       Object oObject
    )
    {
       return oObject.getClass().isArray();
    }
 }
