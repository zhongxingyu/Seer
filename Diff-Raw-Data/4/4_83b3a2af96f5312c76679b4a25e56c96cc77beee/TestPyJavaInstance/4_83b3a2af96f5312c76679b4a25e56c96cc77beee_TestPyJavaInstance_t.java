 // Copyright (C) 2002 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.process;
 
import org.python.core.Py;
 import org.python.core.PyJavaInstance;
 import org.python.core.PyObject;
 
 
 /**
  *
  * @author Philip Aston
  * @version $Revision$
  */ 
 class TestPyJavaInstance extends PyJavaInstance
 {
     private final TestData m_testData;
     private final PyObject m_pyTest;
     
     public TestPyJavaInstance(TestData testData, Object target)
     {
         super(target);
 
 	m_testData = testData;
 	m_pyTest = new PyJavaInstance(testData.getTest());
     }
 
     private final PyObject dispatch(TestData.Invokeable invokeable)
     {
 	try {
 	    return (PyObject)m_testData.dispatch(invokeable);
 	}
 	catch (Exception e) {
	    throw Py.JavaError(e);
 	}
     }
 
     protected PyObject ifindlocal(String name) {
         if (name == "__test__") { // Valid because name is interned.
 	    return m_pyTest;
 	}
 
 	return super.ifindlocal(name);
     }
 
     public PyObject invoke(final String name)
     {
 	return dispatch(
 	    new TestData.Invokeable() {
 		public Object call() {
 		    return TestPyJavaInstance.super.invoke(name);
 		}});
     }
 
     public PyObject invoke(final String name, final PyObject arg1) 
     {
 	return dispatch(
 	    new TestData.Invokeable() {
 		public Object call() {
 		    return TestPyJavaInstance.super.invoke(name, arg1);
 		}});
     }
 
     public PyObject invoke(final String name, final PyObject arg1,
 			   final PyObject arg2) 
     {
 	return dispatch(
 	    new TestData.Invokeable() {
 		public Object call() {
 		    return TestPyJavaInstance.super.invoke(name, arg1, arg2);
 		}});
     }
 
     public PyObject invoke(final String name, final PyObject[] args) 
     {
 	return dispatch(
 	    new TestData.Invokeable() {
 		public Object call() {
 		    return TestPyJavaInstance.super.invoke(name, args);
 		}});
     }
 
     public PyObject invoke(final String name, final PyObject[] args,
 			   final String[] keywords) 
     {
 	return dispatch(
 	    new TestData.Invokeable() {
 		public Object call() {
 		    return TestPyJavaInstance.super.invoke(name, args,
 							   keywords);
 		}});
     }
 }
 
