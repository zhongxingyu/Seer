 // Copyright (C) 2004 Philip Aston
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
 
 package net.grinder.testutility;
 
 import java.lang.reflect.Method;
 
 /**
  *  {@link AssertingInvocationHandler} that takes a <code>Class</code>
  *  and generates stubs that support all the interface of the given
  *  class and have "null" implementations.
  *
  * @author    Philip Aston
  */
 public class StubInvocationHandler extends AssertingInvocationHandler {
 
   private final RandomObjectFactory m_randomObjectFactory =
     new RandomObjectFactory();
 
   public StubInvocationHandler(Class delegateClass) {
     super(delegateClass);
   }
 
  public final Object invokeInternal(
    Object proxy, Method method, Object[] parameters) throws Throwable {

    if ("equals".equals(method.getName()) &&
        parameters.length == 1) {
      return new Boolean(proxy == parameters[0]);
    }
 
     return m_randomObjectFactory.generateParameter(method.getReturnType());
   }
 
   public String stub_toString() {
     return toString();
   }
 }
