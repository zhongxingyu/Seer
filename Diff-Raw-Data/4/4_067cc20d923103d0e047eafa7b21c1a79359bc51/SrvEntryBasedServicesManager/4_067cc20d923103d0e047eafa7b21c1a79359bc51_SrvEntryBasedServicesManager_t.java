 /*****************************************************************************************
  * *** BEGIN LICENSE BLOCK *****
  *
  * Version: MPL 2.0
  *
  * echocat Jomon, Copyright (c) 2012-2013 echocat
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  *
  * *** END LICENSE BLOCK *****
  ****************************************************************************************/
 
 package org.echocat.jomon.net.service;
 
 import org.echocat.jomon.net.HostService;
 import org.echocat.jomon.net.Protocol;
 import org.echocat.jomon.net.dns.SrvDnsEntryEvaluator;
 import org.echocat.jomon.net.dns.SrvDnsEntryEvaluator.NoSuchSrvRecordException;
 import org.echocat.jomon.runtime.CollectionUtils;
 import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xbill.DNS.Resolver;
 
 import javax.annotation.Nonnegative;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.concurrent.NotThreadSafe;
 import javax.annotation.concurrent.ThreadSafe;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
import java.net.UnknownHostException;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import static java.util.Collections.*;
 import static org.echocat.jomon.net.service.SrvEntryBasedServicesManager.State.*;
 import static org.echocat.jomon.runtime.util.ResourceUtils.closeQuietly;
 
 @ThreadSafe
 public abstract class SrvEntryBasedServicesManager<I, O> extends ServicesManager<I, O> {
 
     private static final Logger LOG = LoggerFactory.getLogger(SrvEntryBasedServicesManager.class);
     private static final Object[] EMPTY = new Object[0];
 
     public enum State {
         unknown,
         available,
         unavailable
     }
 
     private final Random _random = new Random();
     private final Lock _lock = new ReentrantLock();
 
     private final Protocol _protocol;
     private final String _service;
 
     private Resolver _resolver;
 
     private Containers<O> _containers;
     private Object[] _outputs;
 
     protected SrvEntryBasedServicesManager(@Nonnull Protocol protocol, @Nonnull String service) {
         _protocol = protocol;
         _service = service;
     }
 
     public Resolver getResolver() {
         return _resolver;
     }
 
     public void setResolver(Resolver resolver) {
         _resolver = resolver;
     }
 
     @Nonnull
     public String getService() {
         return _service;
     }
 
     @Nonnull
     public Protocol getProtocol() {
         return _protocol;
     }
 
     @SuppressWarnings("DuplicateThrows")
     @Override
     protected void check(@Nonnull Collection<I> inputs) throws Exception, InterruptedException {
         final Containers<O> newContainers = new Containers<>();
         final Collection<HostService> oldHostServices;
         final Object[] oldOutputs;
         _lock.lockInterruptibly();
         try {
             oldHostServices = _containers != null ? _containers.getCopyOfAllServices() : null;
             oldOutputs = _outputs;
         } finally {
             _lock.unlock();
         }
         final SrvDnsEntryEvaluator evaluator = new SrvDnsEntryEvaluator(_resolver);
         for (I input : inputs) {
             final List<Container<O>> containers = getContainersFor(input, evaluator, oldHostServices);
             for (Container<O> container : containers) {
                 newContainers.add(container);
             }
         }
         final Object[] newOutputs = rebuildOutputs(newContainers);
         _lock.lockInterruptibly();
         try {
             onContainersSwitch(_containers, newContainers);
             _containers = newContainers;
             _outputs = newOutputs;
         } finally {
             _lock.unlock();
         }
         if ((oldOutputs == null || oldOutputs.length > 0) && newOutputs.length == 0) {
             reportNoServicesAvailable();
         } else if (oldOutputs == null && newOutputs.length > 0) {
             LOG.info("The service " + _service + " is now available and will be served by " + absoluteNumberOf(newOutputs) + " host(s).");
         } else if (oldOutputs != null && oldOutputs.length == 0 && newOutputs.length > 0) {
             LOG.info("The service " + _service + " is now available again and will be served by " + absoluteNumberOf(newOutputs) + " host(s).");
         }
     }
 
     protected void onContainersSwitch(@Nullable Containers<O> oldContainers, @Nullable Containers<O> newContainers) {
         if (oldContainers != null) {
             for (Container<O> oldContainer : oldContainers) {
                 if (newContainers == null || !newContainers.containsOutput(oldContainer.getOutput())) {
                     onContainerGone(oldContainer);
                 }
             }
         }
         if (newContainers != null) {
             for (Container<O> newContainer : newContainers) {
                 if (oldContainers == null || !oldContainers.containsOutput(newContainer.getOutput())) {
                     onContainerEnter(newContainer);
                 }
             }
         }
     }
 
     protected void onContainerEnter(@Nonnull Container<O> container) {}
 
     protected void onContainerGone(@Nonnull Container<O> container) {
         closeQuietly(container);
     }
 
     protected void reportNoServicesAvailable() {
         LOG.warn("There are currently no remote hosts available for service " + _service + ". If a client tries to use this service it will fail with a " + ServiceTemporaryUnavailableException.class.getSimpleName() + ".");
     }
 
     @Nonnegative
     protected int absoluteNumberOf(@Nonnull Object[] newOutputs) {
         final Set<Object> absoluteObjects = new HashSet<>();
         addAll(absoluteObjects, newOutputs);
         return absoluteObjects.size();
     }
 
     @Nullable
     protected List<Container<O>> getContainersFor(@Nonnull I input, @Nonnull SrvDnsEntryEvaluator evaluator, @Nullable Collection<HostService> oldHostServices) throws Exception {
         final List<Container<O>> containers = new ArrayList<>();
         final InetSocketAddress inetSocketAddress = toInetSocketAddress(input);
         if (inetSocketAddress != null) {
             boolean success = false;
             try {
                 for (HostService service : getServicesFor(evaluator, inetSocketAddress)) {
                     final State oldState = getOldSateFor(service, oldHostServices);
                     final Container<O> container = getContainerFor(input, oldState, service);
                     if (container != null) {
                         containers.add(container);
                     }
                 }
                 success = true;
             } finally {
                 if (!success) {
                     closeQuietly(containers);
                 }
             }
         }
         return containers;
     }
 
     @Nonnull
     protected List<HostService> getServicesFor(@Nonnull SrvDnsEntryEvaluator evaluator, @Nonnull InetSocketAddress inetSocketAddress) throws SocketException {
         List<HostService> services;
         try {
             services = evaluator.lookup(_service, _protocol, inetSocketAddress.getHostName());
         } catch (NoSuchSrvRecordException ignored) {
             if (inetSocketAddress.getAddress() != null) {
                 services = singletonList(new HostService(inetSocketAddress, 0, 100, 1));
             } else {
                 services = emptyList();
             }
        } catch (UnknownHostException ignored) {
            services = emptyList();
         }
         return services;
     }
 
     @Nonnull
     protected State getOldSateFor(@Nonnull HostService service, @Nullable Collection<HostService> baseOnOldHostServices) {
         final State oldState;
         if (baseOnOldHostServices == null) {
             oldState = unknown;
         } else {
             oldState = baseOnOldHostServices.contains(service) ? available : unavailable;
         }
         return oldState;
     }
 
     @Nullable
     protected Container<O> getContainerFor(@Nonnull I input, @Nonnull State oldState, @Nonnull HostService service) throws Exception {
         Container<O> result = null;
         try {
             final O output = tryGetOutputFor(input, service.getAddress(), oldState);
             if (output != null) {
                 boolean success = false;
                 try {
                     reportThere(service, oldState);
                     result = new Container<>(service, output);
                     success = true;
                 } finally {
                     if (!success) {
                         closeQuietly(output);
                     }
                 }
             } else {
                 reportGone(service, null, oldState);
             }
         } catch (ServiceTemporaryUnavailableException e) {
             reportGone(service, e.getMessage(), oldState);
         }
         return result;
     }
 
     @Nullable
     protected abstract InetSocketAddress toInetSocketAddress(@Nonnull I input) throws Exception;
 
     @Nullable
     protected abstract O tryGetOutputFor(@Nonnull I input, @Nonnull InetSocketAddress address, @Nonnull State oldState) throws Exception;
 
     public boolean isAvailable() {
         _lock.lock();
         try {
             return _outputs != null && _outputs.length > 0;
         } finally {
             _lock.unlock();
         }
     }
 
     @Nonnull
     protected Object[] getOutputs() {
         final Object[] outputs = _outputs;
         return outputs != null ? outputs : EMPTY;
     }
 
     @Override
     public void markAsGone(@Nonnull O service) throws InterruptedException {
         markAsGone(service, null);
     }
 
     public void markAsGone(@Nonnull O service, @Nullable String cause) throws InterruptedException {
         final Container<O> container;
         _lock.lockInterruptibly();
         try {
             final Containers<O> containers = _containers;
             if (containers == null) {
                 throw new IllegalStateException();
             }
             container = containers.findByOutput(service);
             if (container != null) {
                 containers.remove(container);
             }
             _outputs = rebuildOutputs(containers);
         } finally {
             _lock.unlock();
         }
         if (container != null) {
             reportGone(container.getService(), cause, available);
         }
     }
 
     protected void reportGone(@Nonnull HostService hostService, @Nullable String message, @Nonnull State oldState) {
         if (oldState == unknown || oldState == available) {
             final StringBuilder sb = new StringBuilder();
             sb.append("The service ").append(formatForLogging(hostService)).append(" of ").append(getService());
             if (oldState == unknown) {
                 sb.append(" seems to be not there and will be not available.");
             } else {
                 sb.append(" seems to be gone and will be removed from the cluster.");
             }
             if (message != null) {
                 sb.append(" Got: ");
                 if (message.length() > 255) {
                     sb.append(message.substring(0, 255)).append("...");
                 } else {
                     sb.append(message);
                 }
             }
             LOG.info(sb.toString());
         }
     }
 
     protected void reportThere(@Nonnull HostService hostService, @Nonnull State oldState) {
         if (oldState == unknown || oldState == unavailable) {
             final StringBuilder sb = new StringBuilder();
             sb.append("The service ").append(formatForLogging(hostService)).append(" of ").append(getService());
             if (oldState == unknown) {
                 sb.append(" is now available.");
             } else {
                 sb.append(" is back in the cluster.");
             }
             LOG.info(sb.toString());
         }
     }
 
     @Nonnull
     protected String formatForLogging(@Nonnull HostService hostService) {
         final InetSocketAddress address = hostService.getAddress();
         String output;
         try {
             output = address.getAddress().getCanonicalHostName() + ":" + address.getPort();
         } catch (Exception ignored) {
             output = address.toString();
         }
         return output;
     }
 
     @Nonnull
     protected Object[] rebuildOutputs(@Nonnull Containers<O> containers) {
         final List<Container<O>> newContainers = containers.getContainersByLowersPriority();
         int newArraySize = 0;
         for (Container<O> container : newContainers) {
             newArraySize += container.getService().getWeight();
         }
         final Object[] outputs = new Object[newArraySize];
         int c = 0;
         for (Container<O> container : newContainers) {
             final int weight = container.getService().getWeight();
             for (int i = 0; i < weight; i++) {
                 outputs[c++] = container.getOutput();
             }
         }
         return outputs;
     }
 
     @Override
     @Nullable
     protected O tryTake() {
         synchronized (this) {
             final int numberOfCurrentOutputs = _outputs != null ? _outputs.length : 0;
             // noinspection unchecked
             return numberOfCurrentOutputs > 0 ? (O) _outputs[_random.nextInt(numberOfCurrentOutputs)] : null;
         }
     }
 
     @Override
     public void close() {
         try {
             super.close();
         } finally {
             synchronized (this) {
                 try {
                     onContainersSwitch(_containers, null);
                 } finally {
                     _containers = null;
                     _outputs = null;
                 }
             }
         }
     }
 
     @Override
     public String toString() {
         final String service = _service;
         return service != null ? service : "<unknown>";
     }
 
     @NotThreadSafe
     protected static class Containers<O> implements Iterable<Container<O>> {
 
         private final Map<Integer, List<Container<O>>> _priorityToContainers = new TreeMap<>();
         private final Map<O, Container<O>> _outputToContainer = new HashMap<>();
 
         public void add(@Nonnull Container<O> container) {
             final int priority = container.getService().getPriority();
             List<Container<O>> containers = _priorityToContainers.get(priority);
             if (containers == null) {
                 containers = new ArrayList<>();
                 _priorityToContainers.put(priority, containers);
             }
             containers.add(container);
             _outputToContainer.put(container.getOutput(), container);
         }
 
         public void remove(@Nonnull Container<O> container) {
             final int priority = container.getService().getPriority();
             final List<Container<O>> containers = _priorityToContainers.get(priority);
             if (containers != null) {
                 containers.remove(container);
             }
             if (CollectionUtils.isEmpty(containers)) {
                 _priorityToContainers.remove(priority);
             }
             _outputToContainer.remove(container.getOutput());
         }
 
         @Nullable
         public Container<O> findByOutput(@Nonnull O output) {
             final Container<O> container = _outputToContainer.get(output);
             return container;
         }
 
         public boolean containsOutput(@Nonnull O output) {
             return _outputToContainer.containsKey(output);
         }
 
         @Nonnull
         public List<Container<O>> getContainersByLowersPriority() {
             final List<Container<O>> result;
             if (!_priorityToContainers.isEmpty()) {
                 result = _priorityToContainers.values().iterator().next();
             } else {
                 result = emptyList();
             }
             return result;
         }
 
         @Nonnull
         public Collection<HostService> getCopyOfAllServices() {
             final Collection<HostService> all = new HashSet<>();
             for (List<Container<O>> containers : _priorityToContainers.values()) {
                 for (Container<O> container : containers) {
                     all.add(container.getService());
                 }
             }
             return unmodifiableCollection(all);
         }
 
         @Override
         public Iterator<Container<O>> iterator() {
             return _outputToContainer.values().iterator();
         }
 
         public boolean isEmpty() {
             return _outputToContainer.isEmpty();
         }
 
         @Nonnegative
         public int size() {
             return _outputToContainer.size();
         }
     }
 
     @ThreadSafe
     protected static class Container<O> implements AutoCloseable {
 
         private final O _output;
         private final HostService _service;
 
         public Container(@Nonnull HostService service, @Nonnull O output) {
             _service = service;
             _output = output;
         }
 
         @Nonnull
         public O getOutput() {
             return _output;
         }
 
         @Nonnull
         public HostService getService() {
             return _service;
         }
 
         @Override
         public boolean equals(Object o) {
             final boolean result;
             if (this == o) {
                 result = true;
             } else if (!(o instanceof Container)) {
                 result = false;
             } else {
                 final Container<?> that = (Container) o;
                 result = _service.equals(that._service);
             }
             return result;
         }
 
         @Override
         public void close() throws Exception {
             if (_output instanceof AutoCloseable) {
                 ((AutoCloseable) _output).close();
             }
         }
 
         @Override
         public int hashCode() {
             return _service.hashCode();
         }
 
         @Override
         public String toString() {
             return _service.toString() + ":" + _output.toString();
         }
     }
 }
