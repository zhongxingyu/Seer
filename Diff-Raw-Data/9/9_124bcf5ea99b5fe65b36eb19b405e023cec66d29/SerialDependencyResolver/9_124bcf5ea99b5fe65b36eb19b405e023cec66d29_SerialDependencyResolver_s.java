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
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 
 public class SerialDependencyResolver implements DependencyResolver
 {
     private ArrayList<Module> moduleOrder;
     private Module moduleAcquired;
     private int pos;
     
     public void init(final Collection<Module> rootModules) throws CyclicDependenciesDetectedException
     {
         if (rootModules == null) {
             throw new NullPointerException("rootModules");
         }
         for (final Module module : rootModules) {
             if (module == null) {
                 throw new NullPointerException("rootModules contains null element.");
             }
         }
         moduleOrder = orderModules(rootModules);
         pos = 0;
         moduleAcquired = null;
     }
     
     // returns a module that does not have dependencies
     public Module getFreeModule()
     {
         ensureInitialised();
         if (moduleAcquired != null) {
             throw new IllegalStateException("#getFreeModule() is called when there is a module being processed.");
         }
         if (pos == moduleOrder.size()) {
             return null;
         }
         return moduleAcquired = moduleOrder.get(pos);
     }
     
     public void moduleProcessed(final Module module)
     {
         ensureInitialised();
         if (module == null) {
             throw new NullPointerException("module");
         }
         if (moduleAcquired == null) {
             throw new IllegalStateException("No module is being processed.");
         }
         if (moduleAcquired != module) {
             throw new IllegalArgumentException(MessageFormat.format(
                     "The module ''{0}'' is not being processed.", module.getPath()));
         }
         ++pos;
         moduleAcquired = null;
     }
     
     private void ensureInitialised()
     {
         if (moduleOrder == null) {
             throw new IllegalStateException("Resolver is not initialised.");
         }
     }
     
     // Returns modules in the order so that each module's dependee modules are located before this module.
     private static ArrayList<Module> orderModules(final Collection<Module> rootModules)
             throws CyclicDependenciesDetectedException
     {
         final Context ctx = new Context();
         for (final Module module : rootModules) {
             addModuleDeep(module, ctx);
         }
         return ctx.moduleOrder;
     }
     
     // Data that is used by addModuleDeep. These objects are the same at each step of the recusion.
     private static class Context
     {
         public final IdentityHashMap<Module, ?> registry = new IdentityHashMap<Module, Object>();
         public final LinkedHashSet<Module> path = new LinkedHashSet<Module>();
         public final ArrayList<Module> moduleOrder = new ArrayList<Module>();
     }
     
     /* TODO think if path could be used as an array-based stack and the status of the modules
        is set as registry module. However, the current implementation shows itself to be slightly faster.*/
     private static void addModuleDeep(final Module module, final Context ctx)
             throws CyclicDependenciesDetectedException
     {
         if (ctx.registry.containsKey(module)) {
             return; // the module is already processed
         }
         
         if (ctx.path.add(module)) {
             // the dependee modules are added before this module
             for (int i = 0, n = module.dependencies.size(); i < n; ++i) {
                 final Module dep = module.dependencies.get(i);
                 addModuleDeep(dep, ctx);
             }
             ctx.path.remove(module);
             ctx.registry.put(module, null);
             ctx.moduleOrder.add(module);
             return;
         }
         
         /* A loop is detected. It does not necessarily end with the starting node,
          * some leading path elements could be truncated.
          * 
          * it.remove() has non-optional performance: just skipping to the module's
         * position and then copy the remanings modules to a list works faster.
          * However, this implementation is simpler and for an error case
          * the minor difference in performance does not matter.
          */
         final Iterator<Module> it = ctx.path.iterator();
         while (it.next() != module) {
             // skipping all leading modules that are outside the loop
             it.remove();
         }
         throw new CyclicDependenciesDetectedException(new ArrayList<Module>(ctx.path));
     }
 }
