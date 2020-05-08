 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: ProductRelease.java,v 1.23 2004-02-10 16:31:24 shahid.shah Exp $
  */
 
 package com.netspective.sparx;
 
 import com.netspective.commons.Product;
 
 public class ProductRelease implements Product
 {
     public static final Product PRODUCT_RELEASE = new ProductRelease();
 
     public static final String PRODUCT_NAME = "Netspective Sparx";
     public static final String PRODUCT_ID = "netspective-sparx";
 
     public static final int PRODUCT_RELEASE_NUMBER = 7;
     public static final int PRODUCT_VERSION_MAJOR = 0;
    public static final int PRODUCT_VERSION_MINOR = 18;
 
     public ProductRelease()
     {
     }
 
     public String getProductId()
     {
         return PRODUCT_ID;
     }
 
     public String getProductName()
     {
         return PRODUCT_NAME;
     }
 
     public final int getReleaseNumber()
     {
         return PRODUCT_RELEASE_NUMBER;
     }
 
     public final int getVersionMajor()
     {
         return PRODUCT_VERSION_MAJOR;
     }
 
     public final int getVersionMinor()
     {
         return PRODUCT_VERSION_MINOR;
     }
 
     public final int getBuildNumber()
     {
         return BuildLog.BUILD_NUMBER;
     }
 
     public final String getBuildFilePrefix(boolean includeBuildNumber)
     {
         String filePrefix = PRODUCT_ID + "-" + PRODUCT_RELEASE_NUMBER + "." + PRODUCT_VERSION_MAJOR + "." + PRODUCT_VERSION_MINOR;
         if(includeBuildNumber)
             filePrefix = filePrefix + "_" + BuildLog.BUILD_NUMBER;
         return filePrefix;
     }
 
     public final String getVersion()
     {
         return PRODUCT_RELEASE_NUMBER + "." + PRODUCT_VERSION_MAJOR + "." + PRODUCT_VERSION_MINOR;
     }
 
     public final String getVersionAndBuild()
     {
         return "Version " + getVersion() + " Build " + BuildLog.BUILD_NUMBER;
     }
 
     public final String getProductBuild()
     {
         return PRODUCT_NAME + " Version " + getVersion() + " Build " + BuildLog.BUILD_NUMBER;
     }
 
     public final String getVersionAndBuildShort()
     {
         return "v" + getVersion() + " b" + BuildLog.BUILD_NUMBER;
     }
 }
