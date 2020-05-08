 /* Copyright (C) 1999, 2000, 2001, 2002 Hewlett-Packard Company
 
    This file is part of Mauve.
 
    Mauve is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2, or (at your option)
    any later version.
 
    Mauve is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with Mauve; see the file COPYING.  If not, write to
    the Free Software Foundation, 59 Temple Place - Suite 330,
    Boston, MA 02111-1307, USA.
 */
 
 // Tags: JLS1.0
 
 package gnu.testlet.java.lang.Class;
 import gnu.testlet.Testlet;
 import gnu.testlet.TestHarness;
 
 import java.io.*;
 import java.net.*;
 
 public class ClassTest	 implements Cloneable, java.io.Serializable, Testlet
 {
   protected static TestHarness harness;
 	public void test_toString()
 	{
 		if ( !getClass().toString().equals(getClass().isInterface()? 
 											"interface " : "class " + 
 													getClass().getName()))
 		{
 			harness.fail("Error: toString returned wrong string");
 		}
 
 		if ( !(new Object()).getClass().toString().equals("class java.lang.Object"))
 		{
 			harness.fail("Error: toString returned wrong string");
 		}
 	}
 
 	public void test_getName()
 	{
 	   try { 
 		if ( ! (new java.util.Vector()).getClass().getName().equals("java.util.Vector"))
 			harness.fail("Error: getName returned wrong string - 1");
 
 		if ( ! (new Object[3]).getClass().getName().equals("[Ljava.lang.Object;"))
 			harness.fail("Error: getName returned wrong string - 2");
 
 		if ( ! ( new int[6][7][8]).getClass().getName().equals("[[[I"))
 			harness.fail("Error: getName returned wrong string - 3");
 	   }catch ( Error e ){ 
 	       harness.fail("Error: getName failed  - 4");
 	   }
 
 	}
 
 	public void test_isInterface()
 	{
 		if ( (new Object()).getClass().isInterface())
 			harness.fail("Error: isInterface returned wrong result - 1");
 
 		if ( getClass().isInterface())
 			harness.fail("Error: isInterface returned wrong result - 2");
 
 		try {
 		if ( !Class.forName("java.lang.Cloneable").isInterface())
 			harness.fail("Error: isInterface returned wrong result - 3");
 		}
 		catch ( Exception e ){}
 	}
 
 	public void test_getSuperclass()
 	{
 		try {
 			if(  (new Boolean(true)).getClass().getSuperclass() != 
 				  Class.forName("java.lang.Object"))
 				harness.fail("Error: getSuperclass returned wrong values - 1");
 		}catch ( Exception e ){}
 
 		if ( (new Object()).getClass().getSuperclass() != null )
 			harness.fail("Error: getSuperclass returned wrong values - 2");
 
 		try {	
 		   Class clss = Class.forName("[[I");
 		   if ( clss.getSuperclass() != Class.forName(
 				"java.lang.Object"))
 		   harness.fail(" Error : getSuperclass  " +
 		        		" failed - 3 " );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getSuperclass failed - 4");
 		}
 
 		try {	
 		   Class clss = Class.forName("[D");
 		   if ( clss.getSuperclass() != Class.forName(
 				"java.lang.Object"))
 		   harness.fail(" Error : getSuperclass  " +
 		        		" failed - 5 " );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getSuperclass failed - 6");
 		}
 
 		try {	
 		   Class clss = Class.forName("java.lang.Cloneable");
		   if ( clss.getSuperclass() != null))
 		   harness.fail(" Error : getSuperclass  " +
 		        		" failed - 6 " );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getSuperclass failed - 7");
 		}
 
 		try {	
 		   Class clss = Void.TYPE;
		   if ( clss.getSuperclass() != null))
 		   harness.fail(" Error : getSuperclass  " +
 		        		" failed - 8 " );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getSuperclass failed - 9");
 		}
 
 		try {	
 		   Class clss = Double.TYPE;
		   if ( clss.getSuperclass() != null))
 		   harness.fail(" Error : getSuperclass  " +
 		        		" failed - 10 " );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getSuperclass failed - 11");
 		}
 	}
 
 	public void test_getInterfaces()
 	{
 		Class clss[] = getClass().getInterfaces();
 
 		Class clclass = null,clclass1 = null;
 		try {
 			clclass = Class.forName("java.lang.Cloneable");
 			clclass1 = Class.forName("java.io.Serializable");
 		}catch ( Exception e ){}
 
 		if ( clss == null )
 			harness.fail("Error: getInterfaces returned wrong values - 1");
 		else
 		if ( clss.length != 3 )
 			harness.fail("Error: getInterfaces returned wrong values - 2");
 		else
 		  if (!( clss[0] == clclass  && clss[1] == clclass1))
 		      {
 			harness.fail("Error: getInterfaces returned wrong values - 3");
 			for (int i = 0; i < clss.length; i++)
 			  {
 			    harness.debug ("" + clss[i], false);
 			    harness.debug (" ", false);
 			  }
 			harness.debug ("");
 		      }
 		try {	
 		   Class clsss = Class.forName("[[I");
 		   harness.check ( clsss.getInterfaces().length,  2 );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getInterfaces failed - 5");
 		}
 
 		try {	
 		   Class clsss = Class.forName("[D");
 		   harness.check ( clsss.getInterfaces().length, 2 );
 		}
 		catch ( Exception e ){
 		   harness.fail(" Error: getInterfaces failed - 7");
 		}
 	}
 
 	public void test_newInstance()
 	{
 		Class clss = getClass();
 		Object obj;
 
 		try {
 			obj = clss.newInstance();
 			obj = clss.newInstance();
 			obj = clss.newInstance();
 			obj = clss.newInstance();
 		}
 		catch ( Exception e ){
 			harness.fail("Error: newInstance failed ");
 		}
 		catch ( Error e ){
 			harness.fail("Error: newInstance failed " + 
                           " with out of memory error " );
 		}
 	}
 
 
 	public void test_forName()
 	{
 		harness.checkPoint("forName");
 		try {
 			Object obj = Class.forName("java.lang.Object");
 			harness.check ( obj != null );
 		}
 		catch ( Exception e ){
 			harness.check(false);
 		}
 
 		try {
 			Object obj1 = Class.forName("ab.cd.ef");
 			harness.check(false);
 		}
 		catch ( ClassNotFoundException e ){
 			harness.check(true);
 		}
 
 		try {
 			// The docs say that this should fail.
 			Object obj2 = Class.forName("I");
 			harness.check(false);
 		}
 		catch ( ClassNotFoundException e ){
 			harness.check(true);
 		}
 
 		try {
 			Object obj2 = Class.forName ("[int");
 			harness.check(false);
 		}
 		catch (ClassNotFoundException _) {
 			harness.check(true);
 		}
 	}
 
         public void test_getClassloader()
 	{
 	        try {
                     Class obj1 = Class.forName("java.lang.String");
                     ClassLoader ldr = obj1.getClassLoader();
                     if ( ldr != null )
 			harness.fail("Error: test_getClassLoader failed - 1");
                     Class obj2 = Class.forName("gnu.testlet.java.lang.Class.ClassTest");
                     ClassLoader ldr1 = obj2.getClassLoader();
                     if ( ldr1 != null )
 			harness.fail("Error: test_getClassLoader failed - 2");
                 }
                 catch ( Exception e ){
 			harness.fail("Error: test_getClassLoader failed - 2");
 		}	
 	}
 
         public void test_ComponentType()
 	{
 	        try {
                     Class obj1 = Class.forName("java.lang.String");
                     if ( obj1.getComponentType() != null )
 			harness.fail("Error: test_getComponentType failed - 1");
                     Class obj2 = Class.forName("java.lang.Exception");
                     if ( obj2.getComponentType() != null )
 			harness.fail("Error: test_getComponentType failed - 2");
 		    Class arrclass = Class.forName("[I");
                     if ( arrclass.getComponentType() == null )
 			harness.fail("Error: test_getComponentType failed - 3");
 		    arrclass = Class.forName("[[[[I");
                     if ( arrclass.getComponentType() == null )
 			harness.fail("Error: test_getComponentType failed - 4");
 
 		}
                 catch ( Exception e ){
 			harness.fail("Error: test_getComponentType failed - 6");
 		}	
 		
 	}
 
         public void test_isMethods()
         {
 	        try {
                     Class obj1 = Class.forName("java.lang.String");
                     if ( !obj1.isInstance("babu"))
 			harness.fail("Error: test_isMethods failed - 1");
 
                     Class obj2 = Class.forName("java.lang.Integer");
                     if ( !obj2.isInstance(new Integer(10)))
 			harness.fail("Error: test_isMethods failed - 2");
 
                     int arr[]= new int[3];
 		    Class arrclass = Class.forName("[I");
                     if ( !arrclass.isInstance(arr))
 			harness.fail("Error: test_isMethods failed - 3");
 
                     Class cls1 = Class.forName("java.lang.String");
                     Class supercls = Class.forName("java.lang.Object"); 
                     if ( !supercls.isAssignableFrom( cls1 ))
 			harness.fail("Error: test_isMethods failed - 4");
                     if ( cls1.isAssignableFrom( supercls ))
 			harness.fail("Error: test_isMethods failed - 5");
 		    
                     Class cls2 = Class.forName("java.lang.String");
                     if ( !cls2.isAssignableFrom( cls1 ))
 			harness.fail("Error: test_isMethods failed - 6");
 
                     
 		    arrclass = Class.forName("[I");
 		    Class arrclass1 = Class.forName("[[[I");
 		    Class arrclass2 = Class.forName("[[D");
 		    
                     if ( arrclass.isArray() &&    
                          arrclass1.isArray() &&    
                          arrclass2.isArray() )
                    {}
                    else
 			harness.fail("Error : test_isMethods failed - 7" );    
 		}
                 catch ( Exception e ){
 			harness.fail("Error: test_isMethods failed - 6");
 		}	
 	}		
 
         public void test_getResource()
         {
 	  // this test assume the classpath setting include current directory
  	  try {
  	    FileInputStream is = new FileInputStream("ClassTest.class");
  	    URL url = getClass().getResource("ClassTest.class");
  	    if (url == null)
  	      harness.fail("Error : test_getResource Failed - 1");
 
  	    InputStream uis = url.openStream();
  	    byte[] b1 = new byte[100];
  	    byte[] b2 = new byte[100];
  	    int ret = is.read(b1);
  	    if (ret != 100)
  	      harness.fail("Error : test_getResource Failed - 2");
  	    ret = uis.read(b2);
  	    if (ret != 100)
  	      harness.fail("Error : test_getResource Failed - 3");
  	    for (int i=0; i < 100; i++){
  	      if (b1[i] != b2[i]){
  		harness.fail("Error : test_getResource Failed - 4");
  		break;
  	      }
  	    }
 
  	    uis = getClass().getResourceAsStream("ClassTest.class");
  	    if (uis == null)
  	      harness.fail("Error : test_getResource Failed - 5");
  	    ret = uis.read(b2);
  	    if (ret != 100)
  	      harness.fail("Error : test_getResource Failed - 6");
  	    for (int i=0; i < 100; i++){
  	      if (b1[i] != b2[i]){
  		harness.fail("Error : test_getResource Failed - 7");
  		break;
  	      }
  	    }
 
  	  }catch (Throwable e){
  	    harness.fail("Error : test_getResource Failed - 0");
  	  }
 	}
 
 	public void testall()
 	{
 		test_toString();
 		test_getName();
 		test_isInterface();
 		test_getSuperclass();
 		test_getInterfaces();
 		test_newInstance();
 		test_forName();
                 test_ComponentType();
                 test_getClassloader();
                 test_isMethods();
                 // This one doesn't work so well in Mauve.
 		// test_getResource();
 
 	}
 
   public void test (TestHarness the_harness)
   {
     harness = the_harness;
     testall ();
   }
 
 }
