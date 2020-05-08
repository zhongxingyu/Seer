 /*
  * Sweeper - Duplicate file cleaner
  * Copyright (C) 2012 Bogdan Ciprian Pistol
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gg.pistol.sweeper.core;
 
 import gg.pistol.lumberjack.JackLogger;
 import gg.pistol.lumberjack.JackLoggerFactory;
 import gg.pistol.sweeper.core.Target.Type;
 import gg.pistol.sweeper.core.resource.Resource;
 import gg.pistol.sweeper.core.resource.ResourceDirectory;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.annotation.Nullable;
 
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 /**
  * Analyzes a set of targets to find duplicates.
  *
  * <p>The {@link #analyze} and {@link #delete} methods are not thread safe and must be called from the same thread or
  * using synchronization techniques. The {@link #abortAnalysis} and {@link #abortDeletion} methods are thread safe
  * and can be called from any thread.
  *
  * @author Bogdan Pistol
  */
 // package private
 class Analyzer {
 
     private final JackLogger log;
 
     private final HashFunction hashFunction;
 
     private boolean analyzing;
     private boolean deleting;
 
     private final AtomicBoolean abortAnalysis;
     private final AtomicBoolean abortDeletion;
 
     @Nullable private SweeperCountImpl count;
     @Nullable private TargetImpl rootTarget;
 
 
     Analyzer() throws SweeperException {
         try {
             hashFunction = new HashFunction();
         } catch (NoSuchAlgorithmException e) {
             throw new SweeperException(e);
         }
         abortAnalysis = new AtomicBoolean();
         abortDeletion = new AtomicBoolean();
         log = JackLoggerFactory.getLogger(LoggerFactory.getLogger(Analyzer.class));
     }
 
     /**
      * Compute the analysis.
      *
      * @return the set of all {@link DuplicateGroup}s sorted decreasingly by size.
      */
     NavigableSet<DuplicateGroup> analyze(Collection<? extends Resource> targetResources, SweeperOperationListener listener)
             throws SweeperAbortException {
         Preconditions.checkNotNull(targetResources);
         Preconditions.checkNotNull(listener);
         Preconditions.checkArgument(!targetResources.isEmpty());
 
         log.trace("Computing the analysis for the resources {}.", targetResources);
         analyzing = true;
         deleting = false;
         abortAnalysis.set(false);
         OperationTrackingListener trackingListener = new OperationTrackingListener(listener);
 
         // The number of total targets (including the ROOT target) calculated at the beginning (before sizing the targets)
         // by traverseResources().
         MutableInteger totalTargets = new MutableInteger(0);
 
         rootTarget = traverseResources(targetResources, totalTargets, trackingListener);
         Collection<TargetImpl> sized = computeSize(rootTarget, totalTargets.intValue(), trackingListener);
         Multimap<Long, TargetImpl> sizeDups = filterDuplicateSize(sized, trackingListener);
 
         computeHash(sizeDups.values(), trackingListener);
         Multimap<String, TargetImpl> hashDups = filterDuplicateHash(sizeDups.values(), trackingListener);
 
         count = computeCount(rootTarget, hashDups, trackingListener);
         NavigableSet<DuplicateGroup> duplicates = createDuplicateGroups(hashDups, trackingListener);
         analyzing = false;
         return duplicates;
     }
 
     /**
      * Traverse the resources and expand them.
      *
      * @return a root target that wraps the {@code targetResources}</code>
      */
     private TargetImpl traverseResources(Collection<? extends Resource> targetResources, MutableInteger totalTargets,
                                          OperationTrackingListener listener) throws SweeperAbortException {
         log.trace("Traversing the resources.");
         listener.updateOperation(SweeperOperation.RESOURCE_TRAVERSING);
         TargetImpl root = new TargetImpl(new LinkedHashSet<Resource>(targetResources));
         totalTargets.setValue(1);
 
         int expandedTargets = expand(root.getChildren(), listener);
         totalTargets.add(expandedTargets);
 
         listener.operationCompleted();
         return root;
     }
 
     /**
      * Expand recursively.
      *
      * <p>This method has side effects: if there is an expanded target equal to any of the {@code rootChildren} then that root
      * child will be removed from the {@code rootChildren} collection. This is done to prevent a target from having
      * multiple parents.
      *
      * <p>Example of multiple parent situation: supposing that {@link #analyze} is called with the resource arguments
      * "res1" and "res2", it could be the case that res2 is a descendant of res1:
      *
      * <pre><code>
      * root---res1---dir---res2
      *     \
      *      --res2
      * </code></pre>
      *
      * In this case res2 has two parents: root and dir, to prevent this from happening the "root---res2" child is
      * removed.
      *
      * @return the number of traversed children targets
      */
     private int expand(Collection<TargetImpl> rootChildren, OperationTrackingListener listener) throws SweeperAbortException {
         Set<TargetImpl> rootChildrenSet = new HashSet<TargetImpl>(rootChildren);
         Deque<TargetImpl> stack = new LinkedList<TargetImpl>();
         stack.addAll(rootChildren);
         int targetCount = 0;
 
         while (!stack.isEmpty()) {
             TargetImpl target = stack.pop();
             target.expand(listener);
             targetCount++;
 
             for (TargetImpl t : target.getChildren()) {
                 if (t.getType() != Type.FILE) {
                     stack.push(t);
                 }
 
                 // resolve the multiple parent situations
                 if (rootChildrenSet.contains(t)) {
                     rootChildrenSet.remove(t);
                     rootChildren.remove(t);
                 }
             }
             checkAbortFlag();
         }
         return targetCount;
     }
 
     private void checkAbortFlag() throws SweeperAbortException {
         if (analyzing && abortAnalysis.get()) {
             log.info("Aborting the analysis.");
             throw new SweeperAbortException();
         }
         if (deleting && abortDeletion.get()) {
             log.info("Aborting the deletion.");
             throw new SweeperAbortException();
         }
     }
 
     /**
      * Compute the size recursively with progress indication (the maximum progress is specified by
      * the {@code totalTargets} parameter).
      *
      * @return the collection of all the targets with computed size traversed from the {@code root}
      */
     private Collection<TargetImpl> computeSize(TargetImpl root, int totalTargets,
                                                final OperationTrackingListener listener) throws SweeperAbortException {
         log.trace("Computing the size for {} that has <{}> total sub-targets.", root, totalTargets);
         final Collection<TargetImpl> ret = new ArrayList<TargetImpl>();
         listener.updateOperation(SweeperOperation.SIZE_COMPUTATION);
         listener.setOperationMaxProgress(totalTargets);
 
         traverseBottomUp(Collections.singleton(root), new TargetVisitorMethod() {
             public void visit(TargetImpl target, int targetIndex) {
                 target.computeSize(listener);
                 if (target.isSized()) {
                     ret.add(target);
                 }
                 listener.incrementOperationProgress(targetIndex);
             }
         });
 
         listener.operationCompleted();
         return ret;
     }
 
     /*
      * Bottom-up traversal of the tree of targets.
      *
      * For example the following tree has the bottom-up traversal: A, B, C, D, E, F, root.
      *
      *          --E
      *         /
      * root---F---D
      *     \
      *      --C---B
      *         \
      *          --A
      */
     private void traverseBottomUp(Collection<TargetImpl> roots, TargetVisitorMethod visitor) throws SweeperAbortException {
         Deque<TargetImpl> stack = new LinkedList<TargetImpl>(); // DFS style stack
         Set<TargetImpl> childrenPushed = new HashSet<TargetImpl>(); // targets with the children pushed on the stack
         stack.addAll(roots);
         int targetIndex = 1; // counter for the n-th visited target
 
         while (!stack.isEmpty()) {
             TargetImpl target = stack.peek();
 
             if (!childrenPushed.contains(target)) {
                 childrenPushed.add(target);
                 for (TargetImpl child : target.getChildren()) {
                     stack.push(child);
                 }
             } else {
                 visitor.visit(target, targetIndex);
                 targetIndex++;
                 stack.pop();
             }
             checkAbortFlag();
         }
     }
 
     /**
      * Select all the targets for which there is at least another target with the same size.
      *
      * <p>The {@link Target.Type#ROOT} target is excluded.
      *
      * @return a multimap with sizes as keys and the targets with that same size as values for the key
      */
     private Multimap<Long, TargetImpl> filterDuplicateSize(Collection<TargetImpl> list,
                                                            OperationTrackingListener listener) throws SweeperAbortException {
         log.trace("Deduplicating the size.");
         listener.updateOperation(SweeperOperation.SIZE_DEDUPLICATION);
 
         Multimap<Long, TargetImpl> sizeDups = filterDuplicates(list, new Function<TargetImpl, Long>() {
             @Nullable
             public Long apply(TargetImpl input) {
                 // all the null return values will be ignored
                 return input.getType() != Target.Type.ROOT ? input.getSize() : null;
             }
         });
 
         listener.operationCompleted();
         return sizeDups;
     }
 
     /**
      * Select all the duplicates from the targets based on a criteria function.
      * If the function returns the same value for two input targets then those targets are considered duplicates (in
      * the context of the criteria function).
      *
      * @return a multimap with function values as keys and the targets that are considered duplicates as values for
      *         the key
      */
     private <T> Multimap<T, TargetImpl> filterDuplicates(Collection<TargetImpl> targets,
                                                          Function<TargetImpl, T> indexFunction) throws SweeperAbortException {
 
         // Dumping all the targets into the multimap (Multimaps.index() doesn't work because it does not support
         // skipping null function values and also because of checking the abort flag).
         Multimap<T, TargetImpl> map = ArrayListMultimap.create();
         for (TargetImpl target : targets) {
             T key = indexFunction.apply(target);
             if (key != null) { // ignore null values
                 map.put(key, target);
             }
             checkAbortFlag();
         }
 
         // Filtering the targets (Multimaps.filterKeys() and/or Multimaps.filterValues() don't work because of checking
         // the abort flag).
         Multimap<T, TargetImpl> ret = ArrayListMultimap.create();
         for (T key : map.keySet()) {
             checkAbortFlag();
             Collection<TargetImpl> collection = map.get(key);
 
             // Ignore all the targets that are not duplicates.
             if (collection.size() == 1) {
                 continue;
             }
 
             // Ignore all the targets that are a single child of a directory. In this case the directory will represent
             // the child's content.
             Collection<TargetImpl> values = new ArrayList<TargetImpl>();
             for (TargetImpl target : collection) {
                 if (target.getParent() == null || target.getParent().getChildren().size() > 1) {
                     values.add(target);
                 }
             }
 
             if (values.size() > 1) {
                 ret.putAll(key, values);
             }
         }
         return ret;
     }
 
     /**
      * Compute the hash recursively for the specified targets.
      */
     private void computeHash(Collection<TargetImpl> targets, final OperationTrackingListener listener)
             throws SweeperAbortException {
        log.trace("Computing the hash for targets {}.", targets);
         listener.updateOperation(SweeperOperation.HASH_COMPUTATION);
 
         // Filter the targets that are not the children of other targets. All the children targets will have the hash
         // computed recursively from the parent target.
         targets = filterUpperTargets(targets);
 
         // Compute the total size of the targets to hash for progress tracking purposes.
         long totalHashSize = 0;
         for (TargetImpl target : targets) {
             totalHashSize += target.getSize();
             checkAbortFlag();
         }
         listener.setOperationMaxProgress(totalHashSize);
 
         traverseBottomUp(targets, getHashVisitorMethod(listener));
         listener.operationCompleted();
     }
 
     /**
      * Filter the targets that are not the children of other targets.
      *
      * @return the collection of filtered targets
      */
     private Collection<TargetImpl> filterUpperTargets(Collection<TargetImpl> targets) throws SweeperAbortException {
         Set<TargetImpl> set = new HashSet<TargetImpl>();
         Collection<TargetImpl> ret = new ArrayList<TargetImpl>();
 
         for (TargetImpl target : targets) {
             set.add(target);
             checkAbortFlag();
         }
 
         for (TargetImpl target : targets) {
             TargetImpl parent = target;
 
             boolean isUpper = true;
             while ((parent = parent.getParent()) != null) {
                 if (set.contains(parent)) {
                     isUpper = false;
                     break;
                 }
             }
             if (isUpper) {
                 ret.add(target);
             }
 
             checkAbortFlag();
         }
 
         return ret;
     }
 
     private TargetVisitorMethod getHashVisitorMethod(final OperationTrackingListener listener) {
         return new TargetVisitorMethod() {
             long currentSize = 0;
 
             public void visit(TargetImpl target, int targetIndex) throws SweeperAbortException {
                 target.computeHash(hashFunction, listener, abortAnalysis);
 
                 // Keep track of file sizes only as directories only re-hash the hash of their children which should be
                 // fast compared to reading I/O operations and hashing of potentially very large files.
                 if (target.getType() == Type.FILE) {
                     currentSize += target.getSize();
                     listener.incrementOperationProgress(currentSize);
                 }
             }
         };
     }
 
     /**
      * Select duplicate targets (having the same hash).
      *
      * @return a multimap with hashes as keys and duplicate targets as values for the key
      */
     private Multimap<String, TargetImpl> filterDuplicateHash(Collection<TargetImpl> targets,
                                                              OperationTrackingListener listener) throws SweeperAbortException {
        log.trace("Deduplicating the hash for targets {}.", targets);
         listener.updateOperation(SweeperOperation.HASH_DEDUPLICATION);
 
         Multimap<String, TargetImpl> hashDups = filterDuplicates(targets,
                 new Function<TargetImpl, String>() {
                     @Nullable
                     public String apply(TargetImpl input) {
                         // all the null return values will be ignored
                         return input.isHashed() ? input.getHash() : null;
                     }
                 });
 
         listener.operationCompleted();
         return hashDups;
     }
 
     private SweeperCountImpl computeCount(TargetImpl root, Multimap<String, TargetImpl> hashDups,
                                           OperationTrackingListener listener) throws SweeperAbortException {
         log.trace("Counting the root {} and the hash duplicates {}.", root, hashDups);
         listener.updateOperation(SweeperOperation.COUNTING);
 
         int totalTargets = root.getTotalTargets();
         int totalTargetFiles = root.getTotalTargetFiles();
         long totalSize = root.getSize();
 
         int duplicateTargets = 0;
         int duplicateTargetFiles = 0;
         long duplicateSize = 0;
 
         // Filter the upper targets in order to have correct aggregate counting of duplicates. The hashDups can contain
         // targets that are children of other targets.
         Collection<TargetImpl> hashDupUpperTargets = filterUpperTargets(hashDups.values());
 
         // Group the duplicate targets by hash.
         Multimap<String, TargetImpl> dups = filterDuplicateHash(hashDupUpperTargets, OperationTrackingListener.NOOP_LISTENER);
 
         for (String key : dups.keySet()) {
             Iterator<TargetImpl> iterator = dups.get(key).iterator();
 
             // Jump over the first value from a duplicate group because deleting all the others will make this one
             // a non-duplicate.
             iterator.next();
 
             while (iterator.hasNext()) {
                 TargetImpl target = iterator.next();
                 duplicateTargets += target.getTotalTargets();
                 duplicateTargetFiles += target.getTotalTargetFiles();
                 duplicateSize += target.getSize();
 
                 checkAbortFlag();
             }
         }
 
         SweeperCountImpl count = new SweeperCountImpl(totalTargets, totalTargetFiles, totalSize, duplicateTargets,
                 duplicateTargetFiles, duplicateSize);
 
         listener.operationCompleted();
         return count;
     }
 
     private NavigableSet<DuplicateGroup> createDuplicateGroups(Multimap<String, TargetImpl> hashDups,
                                                                OperationTrackingListener listener) {
         log.trace("Duplicate grouping.");
         listener.updateOperation(SweeperOperation.DUPLICATE_GROUPING);
 
         NavigableSet<DuplicateGroup> ret = new TreeSet<DuplicateGroup>();
         for (String key : hashDups.keySet()) {
             Collection<TargetImpl> values = hashDups.get(key);
             DuplicateGroup dup = new DuplicateGroup(values);
             ret.add(dup);
         }
 
         listener.operationCompleted();
         return ret;
     }
 
     void delete(Collection<? extends Target> targets, SweeperOperationListener listener) throws SweeperAbortException {
         Preconditions.checkNotNull(targets);
         Preconditions.checkNotNull(listener);
         Preconditions.checkArgument(!targets.isEmpty());
 
         log.trace("Deleting targets.");
         analyzing = false;
         deleting = true;
         abortDeletion.set(false);
         OperationTrackingListener trackingListener = new OperationTrackingListener(listener);
         trackingListener.updateOperation(SweeperOperation.RESOURCE_DELETION);
 
         // Remove the possible multiple instances of the same target and get the children of any ROOT target (deleting
         // a ROOT target means to delete its children).
         Set<TargetImpl> targetSet = new LinkedHashSet<TargetImpl>();
         for (Target target : targets) {
             if (target.getType() == Type.ROOT) {
                 targetSet.addAll(((TargetImpl) target).getChildren());
             } else {
                 targetSet.add((TargetImpl) target);
             }
             checkAbortFlag();
         }
 
         // Use only the upper targets (deleting an upper target will also delete all of its descendants).
         Collection<TargetImpl> upperTargets = filterUpperTargets(targetSet);
 
         int totalProgress = 0; // total individual targets to delete
         for (TargetImpl target : upperTargets) {
             // In case of recursive deletion then a more granular progress tracking is possible, otherwise if
             // a directory (with all of its contents) can be deleted in one step then the progress will be more coarse.
             if (target.getType() == Type.DIRECTORY && ((ResourceDirectory) target.getResource()).deleteOnlyEmpty()) {
                 totalProgress += target.getTotalTargets();
             } else {
                 totalProgress++;
             }
             checkAbortFlag();
         }
         trackingListener.setOperationMaxProgress(totalProgress);
         MutableInteger progress = new MutableInteger(0);
 
         // The visitor pattern is used for recursive deletion (bottom-up).
         TargetVisitorMethod deleteMethod = getDeleteVisitorMethod(progress, trackingListener);
 
         for (TargetImpl target : upperTargets) {
             if (target.getType() == Type.DIRECTORY && ((ResourceDirectory) target.getResource()).deleteOnlyEmpty()) {
                 traverseBottomUp(Collections.singleton(target), deleteMethod);
             } else {
                 // Deletion of a file or a directory that can be deleted in one single step.
                 target.delete(trackingListener);
                 progress.increment();
                 trackingListener.incrementOperationProgress(progress.intValue());
             }
         }
 
         trackingListener.operationCompleted();
         deleting = false;
     }
 
     private TargetVisitorMethod getDeleteVisitorMethod(final MutableInteger progress, final OperationTrackingListener listener) {
         return new TargetVisitorMethod() {
             public void visit(TargetImpl target, int targetIndex) {
                 target.delete(listener);
                 progress.increment();
                 listener.incrementOperationProgress(progress.intValue());
             }
         };
     }
 
     /**
      * Abort the analyze operation.
      *
      * <p>This method is thread safe.
      */
     void abortAnalysis() {
         log.trace("Turning on the analysis abort flag.");
         abortAnalysis.set(true);
     }
 
     /**
      * Abort the delete operation.
      *
      * <p>This method is thread safe.
      */
     void abortDeletion() {
         log.trace("Turning on the deletion abort flag.");
         abortDeletion.set(true);
     }
 
     @Nullable
     SweeperCountImpl getCount() {
         return count;
     }
 
     @Nullable
     TargetImpl getRootTarget() {
         return rootTarget;
     }
 
     /**
      * Visitor pattern interface for hierarchies of targets.
      */
     private static interface TargetVisitorMethod {
         void visit(TargetImpl target, int targetIndex) throws SweeperAbortException;
     }
 
 }
