 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.util.List;
 
 /**
  * <p>Indicates that a loop is detected in dependencies between the {@link Module modules}
  * involved. In other words, there is a module in the given modules that depends upon
  * itself. Circular dependencies do not allow a {@link DependencyResolver} to determine
  * an order in which modules could be processes so that each module is processed
  * after all its dependee modules are processed.</p>
 *
 * @author @author D&#378;mitry La&#365;&#269;uk
  * 
  * @see DependencyResolver
  */
 public class CyclicDependenciesDetectedException extends Exception
 {
     private final List<Module> loop;
     
     /**
      * <p>Creates an instance of {@code CyclicDependenciesDetectedException} and
      * initialises it with the given module list that form a dependency loop. The modules
      * passed are expected to be in the order they are placed in this loop.
      * The first and the last elements of this list are not expected to be the same
      * module. For instance, if the modules {@code A}, {@code B}, {@code C} are in
      * a dependency loop {@code A->B->C->} then the list {@code [A, B, C]} should
      * be passed.</p>
      * 
      * <p>The exception message is composed basing on the given modules.</p>
      * 
      * <p>Ownership over the given list is taken by the instance created. It should
      * not be modified after the exception is created.</p>
      * 
      * @param loop the list of modules that form the dependency loop. It must not be
      *      {@code null}.
      * 
      * @throws NullPointerException if <em>loop</em> is {@code null}.
      */
     public CyclicDependenciesDetectedException(final List<Module> loop)
     {
         super(errorMessage(loop));
         this.loop = loop;
     }
     
     /**
      * <p>Returns the list of the modules that form the dependency loop this exception
      * is associated with. The list returned is mutable. It is never {@code null}.</p>
      * 
      * @return the list of the modules that form the dependency loop.
      */
     public List<Module> getLoop()
     {
         return loop;
     }
     
     private static String errorMessage(final List<Module> loop)
     {
         final StringBuilder buf = new StringBuilder("Cyclic dependencies detected: [");
         if (!loop.isEmpty()) {
             buf.append("->");
             for (final Module module : loop) {
                 buf.append(module.getPath()).append("->");
             }
         }
         buf.append("].");
         return buf.toString();
     }
 }
