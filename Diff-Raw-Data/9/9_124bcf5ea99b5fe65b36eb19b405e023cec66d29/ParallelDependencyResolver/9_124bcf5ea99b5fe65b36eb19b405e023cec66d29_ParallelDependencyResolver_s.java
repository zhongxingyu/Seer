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
 
 public class ParallelDependencyResolver implements DependencyResolver
 {
     private ArrayList<Node> shortlist;
     private IdentityHashMap<Module, Node> modulesAcquired;
     private int remainingModuleCount;
     
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
         synchronized (this) {
             final ArrayList<Node> newShortlist = new ArrayList<Node>();
             /* If buildNodeGraph() throws an exception then the state is not changed
                so that this ParallelDependencyResolver instance could be used as if
                this init() were not invoked. */
             remainingModuleCount = buildNodeGraph(rootModules, newShortlist);
             shortlist = newShortlist;
             modulesAcquired = new IdentityHashMap<Module, Node>();
         }
     }
     
     // returns a module that does not have dependencies
     public synchronized Module getFreeModule()
     {
         ensureInitialised();
         
         try {
             while (shortlist.isEmpty()) {
                 if (remainingModuleCount <= 0) {
                     // Either all modules are processed or #abort() has been called.
                     return null;
                 }
                 wait();
             }
             final Node node = shortlist.remove(shortlist.size()-1);
             final Module module = node.module;
             modulesAcquired.put(module, node);
             --remainingModuleCount;
             
             return module;
         }
         catch (InterruptedException ex) {
             Thread.currentThread().interrupt();
             throw new IllegalStateException();
         }
     }
     
     public synchronized void moduleProcessed(final Module module)
     {
         ensureInitialised();
         if (module == null) {
             throw new NullPointerException("module");
         }
         if (remainingModuleCount < 0) {
             // #abort() has been called.
             return;
         }
         final Node node = modulesAcquired.remove(module);
         if (node == null) {
             throw new IllegalArgumentException(MessageFormat.format(
                     "The module ''{0}'' is not being processed.", module.getPath()));
         }
         for (int i = 0, n = node.dependencyOf.size(); i < n; ++i) {
             final Node depOf = node.dependencyOf.get(i);
             if (--depOf.dependencyCount == 0) {
                 // all modules with no dependencies go to the shortlist
                 shortlist.add(depOf);
             }
         }
         /* Notifying all threads after the module is removed and its dependencies are processed
            so that each thread waiting can either get a free module or finish execution. */
         notifyAll();
     }
     
     public synchronized void abort()
     {
         ensureInitialised();
         shortlist.clear();
         modulesAcquired.clear(); // Tracking the acquired modules does not make sense anymore.
         remainingModuleCount = -1; // Indicates that #abort() has been called.
         notifyAll();
     }
     
     private void ensureInitialised()
     {
         if (shortlist == null) {
             throw new IllegalStateException("Resolver is not initialised.");
         }
     }
     
     private static class Node
     {
         private Node(final Module module)
         {
             this.module = module;
             dependencyCount = module.getDependencies().size();
             dependencyOf = new ArrayList<Node>();
         }
         
         private final Module module;
         /* Knowing just dependency count is enough to detect the moment
            when this node has no dependencies remaining. */
         private int dependencyCount;
         private final ArrayList<Node> dependencyOf;
     }
     
     /*
      * Builds a DAG which nodes hold modules and arcs that represent inverted module dependencies.
      * The list of nodes returned via shortlist contains the starting vertices of the graph.
      * The modules that are bound to these vertices do not have dependencies on other modules
      * and are used as modules to start unwinding dependencies from.
      * 
      * @returns the total number of modules.
      */
     private static int buildNodeGraph(final Collection<Module> rootModules, ArrayList<Node> shortlist)
             throws CyclicDependenciesDetectedException
     {
         final IdentityHashMap<Module, Node> registry = new IdentityHashMap<Module, Node>();
         final LinkedHashSet<Module> path = new LinkedHashSet<Module>();
         for (final Module module : rootModules) {
             addNodeDeep(module, shortlist, registry, path);
         }
         // the number of nodes in the graph
         return registry.size();
     }
     
     // TODO reduce the number of parameters in this function (see SerialDependencyResolver#addModuleDeep).
     private static Node addNodeDeep(final Module module, final ArrayList<Node> shortlist,
             final IdentityHashMap<Module, Node> registry, final LinkedHashSet<Module> path)
             throws CyclicDependenciesDetectedException
     {
         Node node = registry.get(module);
         if (node != null) {
             return node; // the module is already processed
         }
         
         if (path.add(module)) {
             node = new Node(module);
             
             final ArrayList<Module> deps = module.dependencies;
             if (deps.isEmpty()) {
                 shortlist.add(node);
             } else {
                 // inverted dependencies are assigned
                 for (int i = 0, n = deps.size(); i < n; ++i) {
                     final Module dep = deps.get(i);
                     final Node depNode = addNodeDeep(dep, shortlist, registry, path);
                     depNode.dependencyOf.add(node);
                 }
             }
             
             registry.put(module, node);
             path.remove(module);
             return node;
         }
         
         /* A loop is detected. It does not necessarily end with the starting node,
          * some leading path elements could be truncated.
          * 
          * it.remove() has non-optional performance: just skipping to the module's
         * position and then copy the remanings modules to a list works faster.
          * However, this implementation is simpler and for an error case
          * the minor difference in performance does not matter.
          */
         final Iterator<Module> it = path.iterator();
         while (it.next() != module) {
             // skipping all leading modules that are outside the loop
             it.remove();
         }
         throw new CyclicDependenciesDetectedException(new ArrayList<Module>(path));
     }
 }
