 package edu.uw.zookeeper.orchestra.control;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import javax.annotation.Nullable;
 
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.KeeperException;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.util.concurrent.AsyncFunction;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 
 import edu.uw.zookeeper.EnsembleView;
 import edu.uw.zookeeper.ServerInetAddressView;
 import edu.uw.zookeeper.client.ClientExecutor;
 import edu.uw.zookeeper.client.Materializer;
 import edu.uw.zookeeper.data.Label;
 import edu.uw.zookeeper.data.Operations;
 import edu.uw.zookeeper.data.ZNode;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.data.Schema.LabelType;
 import edu.uw.zookeeper.orchestra.CachedFunction;
 import edu.uw.zookeeper.orchestra.Identifier;
 import edu.uw.zookeeper.orchestra.VolumeDescriptor;
 import edu.uw.zookeeper.orchestra.backend.BackendView;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.proto.Records;
 import edu.uw.zookeeper.util.Pair;
 import edu.uw.zookeeper.util.Promise;
 import edu.uw.zookeeper.util.PromiseTask;
 import edu.uw.zookeeper.util.SettableFuturePromise;
 
 public abstract class Orchestra extends Control.ControlZNode {
     
     @Label
     public static final ZNodeLabel.Path ROOT = ZNodeLabel.Path.of("/orchestra");
     
     @ZNode(label="peers")
     public static abstract class Peers extends Control.ControlZNode {
 
         @ZNode
         public static class Entity extends Control.TypedLabelZNode<Identifier> {
 
             @Label(type=LabelType.PATTERN)
             public static final String LABEL_PATTERN = Identifier.PATTERN;
 
             public static <I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>> ListenableFuture<Peers.Entity> create(
                     final ServerInetAddressView value, 
                     final Materializer<I,O> materializer,
                     final Executor executor) {
                 Control.RegisterHashedTask<I, O, ServerInetAddressView, Orchestra.Peers.Entity> task = 
                         Control.RegisterHashedTask.of(
                                 value,
                                 Orchestra.Peers.Entity.hashOf(value),
                                 new Function<Identifier, Orchestra.Peers.Entity>() {
                                     @Override
                                     @Nullable
                                     public
                                     Peers.Entity apply(@Nullable Identifier input) {
                                         return Orchestra.Peers.Entity.of(input);
                                     }
                                 },
                                 new Function<Orchestra.Peers.Entity, ZNodeLabel.Path>() {
                                     @Override
                                     @Nullable
                                     public
                                     ZNodeLabel.Path apply(@Nullable Peers.Entity input) {
                                         return Orchestra.Peers.Entity.PeerAddress.pathOf(input);
                                     }
                                 },
                                 new AsyncFunction<Orchestra.Peers.Entity, Orchestra.Peers.Entity.PeerAddress>() {
                                     @Override
                                     public ListenableFuture<Orchestra.Peers.Entity.PeerAddress> apply(
                                             Peers.Entity input) {
                                         return Orchestra.Peers.Entity.PeerAddress.get(input, materializer);
                                     }
                                 },
                                 materializer, 
                                 executor);
                 executor.execute(task);
                 return task;
             }
             
             public static CachedFunction<Peers.Entity, Boolean> isPresent(
                     final Materializer<?,?> materializer) {
                 Function<Peers.Entity, Boolean> cached = new Function<Peers.Entity, Boolean>() {
                     @Override
                     public Boolean apply(Peers.Entity input) {
                         return materializer.contains(input.presence().path());
                     }
                 };
                 AsyncFunction<Peers.Entity, Boolean> lookup = new AsyncFunction<Peers.Entity, Boolean>() {
                     @Override
                     public ListenableFuture<Boolean> apply(Peers.Entity input) {
                         return input.presence().exists(materializer);
                     }
                 };
                 return CachedFunction.create(cached, lookup);
             }
             
             public static Peers.Entity valueOf(String label) {
                 return of(Identifier.valueOf(label));
             }
             
             public static Hash.Hashed hashOf(ServerInetAddressView value) {
                 return Hash.default32().apply(ServerInetAddressView.toString(value));
             }
             
             public static Peers.Entity of(Identifier identifier) {
                 return new Entity(identifier);
             }
             
             public Entity(Identifier identifier) {
                 super(identifier);
             }
             
             @Override
             public String toString() {
                 return get().toString();
             }
             
             public Entity.Presence presence() {
                 return Entity.Presence.of(this);
             }
 
             @ZNode(createMode=CreateMode.EPHEMERAL)
             public static class Presence extends Control.ControlZNode {
 
                 protected static enum Exists implements Function<Pair<? extends Operation.ProtocolRequest<?>, ? extends Operation.ProtocolResponse<?>>, Boolean> {
                     EXISTS;
 
                     @Override
                     public Boolean apply(
                             Pair<? extends Operation.ProtocolRequest<?>, ? extends Operation.ProtocolResponse<?>> input) {
                         return ! (input.second().getRecord() instanceof Operation.Error);
                     }
                 }
                 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("presence");
 
                 public static Presence of(Peers.Entity parent) {
                     return new Presence(parent);
                 }
                 
                 public Presence(Peers.Entity parent) {
                     super(parent);
                 }
 
                 public <T extends Operation.ProtocolRequest<Records.Request>, V extends Operation.ProtocolResponse<Records.Response>> 
                ListenableFuture<Pair<T,V>> create(ClientExecutor<? super Records.Request,T,V> client) {
                    return client.submit(Operations.Requests.create().setPath(path()).build());
                 }
                 
                 public ListenableFuture<Boolean> exists(ClientExecutor<? super Records.Request, ?, ?> client) {
                     return Futures.transform(
                             client.submit(Operations.Requests.exists().setPath(path()).build()), 
                             Exists.EXISTS);
                 }
             }
             
             @ZNode(type=ServerInetAddressView.class)
             public static class ClientAddress extends Control.TypedValueZNode<ServerInetAddressView> {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("clientAddress");
                 
                 public static ListenableFuture<ClientAddress> get(Peers.Entity entity, Materializer<?,?> materializer) {
                     return get(ClientAddress.class, entity, materializer);
                 }
                 
                 public static ListenableFuture<ClientAddress> create(ServerInetAddressView value, Peers.Entity entity, Materializer<?,?> materializer) {
                     return create(ClientAddress.class, value, entity, materializer);
                 }
                 
                 public static ClientAddress valueOf(String label, Peers.Entity parent) {
                     return of(ServerInetAddressView.fromString(label), parent);
                 }
                 
                 public static ClientAddress of(ServerInetAddressView address, Peers.Entity parent) {
                     return new ClientAddress(address, parent);
                 }
                 
                 public ClientAddress(ServerInetAddressView address, Peers.Entity parent) {
                     super(address, parent);
                 }
             }
             
             @ZNode(type=ServerInetAddressView.class)
             public static class PeerAddress extends Control.TypedValueZNode<ServerInetAddressView> {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("peerAddress");
                 
                 public static ZNodeLabel.Path pathOf(Peers.Entity entity) {
                     return ZNodeLabel.Path.of(entity.path(), LABEL);
                 }
                 
                 public static CachedFunction<Identifier, PeerAddress> lookup(
                         final Materializer<?,?> materializer) {
                     Function<Identifier, PeerAddress> cached = new Function<Identifier, PeerAddress>() {
                         @Override
                         public @Nullable PeerAddress apply(Identifier peer) {
                             Entity entity = Entity.of(peer);
                             ServerInetAddressView address = null;
                             ZNodeLabel.Path path = pathOf(entity);
                             Materializer.MaterializedNode node = materializer.get(path);
                             if (node != null) {
                                 address = (ServerInetAddressView) node.get().get();
                             }                    
                             if (address == null) {
                                 return null;
                             } else {
                                 return of(address, entity);
                             }
                         }
                     };
                     AsyncFunction<Identifier, PeerAddress> lookup = new AsyncFunction<Identifier, PeerAddress>() {
                         @Override
                         public ListenableFuture<PeerAddress> apply(final Identifier peer) {
                             final Entity entity = Entity.of(peer);
                             final ZNodeLabel.Path path = pathOf(entity);
                             Function<Pair<? extends Operation.ProtocolRequest<?>, ? extends Operation.ProtocolResponse<?>>, PeerAddress> transformer = new Function<Pair<? extends Operation.ProtocolRequest<?>, ? extends Operation.ProtocolResponse<?>>, PeerAddress>() {
                                 @Override
                                 public @Nullable PeerAddress apply(
                                         Pair<? extends Operation.ProtocolRequest<?>, ? extends Operation.ProtocolResponse<?>> input) {
                                     try {
                                         Operations.maybeError(input.second().getRecord(), KeeperException.Code.NONODE);
                                     } catch (KeeperException e) {
                                         return null;
                                     }
                                     ServerInetAddressView address = null;
                                     Materializer.MaterializedNode node = materializer.get(path);
                                     if (node != null) {
                                         address = (ServerInetAddressView) node.get().get();
                                     }
                                     if (address == null) {
                                         return null;
                                     } else {
                                         return of(address, entity);
                                     }
                                 }
                             };
                             return Futures.transform(materializer.operator().getData(path).submit(), transformer);
                         }
                     };
                     return CachedFunction.create(cached, lookup);
                 }
                 
                 public static ListenableFuture<PeerAddress> get(Peers.Entity entity, Materializer<?,?> materializer) {
                     return get(PeerAddress.class, entity, materializer);
                 }
                 
                 public static ListenableFuture<PeerAddress> create(ServerInetAddressView value, Peers.Entity entity, Materializer<?,?> materializer) {
                     return create(PeerAddress.class, value, entity, materializer);
                 }
                 
                 public static PeerAddress valueOf(String label, Peers.Entity parent) {
                     return of(ServerInetAddressView.fromString(label), parent);
                 }
                 
                 public static PeerAddress of(ServerInetAddressView address, Peers.Entity parent) {
                     return new PeerAddress(address, parent);
                 }
 
                 public PeerAddress(ServerInetAddressView address, Peers.Entity parent) {
                     super(checkNotNull(address), parent);
                 }
             }
             
             @ZNode(label="backend", type=BackendView.class)
             public static class Backend extends Control.TypedValueZNode<BackendView> {
                 
                 public static ListenableFuture<Entity.Backend> get(Peers.Entity entity, Materializer<?,?> materializer) {
                     return get(Entity.Backend.class, entity, materializer);
                 }
                 
                 public static ListenableFuture<Entity.Backend> create(BackendView value, Peers.Entity entity, Materializer<?,?> materializer) {
                     return create(Entity.Backend.class, value, entity, materializer);
                 }
                 
                 public static Entity.Backend of(BackendView value, Peers.Entity parent) {
                     return new Backend(value, parent);
                 }
 
                 public Backend(BackendView value, Peers.Entity parent) {
                     super(value, parent);
                 }
             }
         }
     }
 
     @ZNode(label="ensembles")
     public static abstract class Ensembles extends Control.ControlZNode {
         
         public static ListenableFuture<List<Ensembles.Entity>> getEnsembles(ClientExecutor<? super Records.Request, ?, ?> client) {
             return Futures.transform(
                     client.submit(Operations.Requests.getChildren().setPath(path(Ensembles.class)).build()), 
                     new AsyncFunction<Pair<? extends Operation.ProtocolRequest<Records.Request>, ? extends Operation.ProtocolResponse<Records.Response>>, List<Ensembles.Entity>>() {
                         @Override
                         public ListenableFuture<List<Ensembles.Entity>> apply(Pair<? extends Operation.ProtocolRequest<Records.Request>, ? extends Operation.ProtocolResponse<Records.Response>> input) throws KeeperException {
                             Records.ChildrenGetter response = (Records.ChildrenGetter) Operations.unlessError(input.second().getRecord());
                             List<Ensembles.Entity> result = Lists.newArrayListWithCapacity(response.getChildren().size());
                             for (String child: response.getChildren()) {
                                 result.add(Ensembles.Entity.of(Identifier.valueOf(child)));
                             }
                             return Futures.immediateFuture(result);
                         }
                     });
         }
         
         @ZNode
         public static class Entity extends Control.TypedLabelZNode<Identifier> {
 
             @Label(type=LabelType.PATTERN)
             public static final String LABEL_PATTERN = Identifier.PATTERN;
 
             public static <I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>> ListenableFuture<Ensembles.Entity> create(
                     final EnsembleView<ServerInetAddressView> value, 
                     final Materializer<I,O> materializer,
                     final Executor executor) {
                 Control.RegisterHashedTask<I, O, EnsembleView<ServerInetAddressView>, Orchestra.Ensembles.Entity> task = 
                         Control.RegisterHashedTask.of(
                                 value,
                                 Orchestra.Ensembles.Entity.hashOf(value),
                                 new Function<Identifier, Orchestra.Ensembles.Entity>() {
                                     @Override
                                     @Nullable
                                     public
                                     Ensembles.Entity apply(@Nullable Identifier input) {
                                         return Orchestra.Ensembles.Entity.of(input);
                                     }
                                 },
                                 new Function<Orchestra.Ensembles.Entity, ZNodeLabel.Path>() {
                                     @Override
                                     @Nullable
                                     public
                                     ZNodeLabel.Path apply(@Nullable Ensembles.Entity input) {
                                         return Orchestra.Ensembles.Entity.Backend.pathOf(input);
                                     }
                                 },
                                 new AsyncFunction<Orchestra.Ensembles.Entity, Orchestra.Ensembles.Entity.Backend>() {
                                     @Override
                                     public ListenableFuture<Orchestra.Ensembles.Entity.Backend> apply(
                                             Ensembles.Entity input) {
                                         return Orchestra.Ensembles.Entity.Backend.get(input, materializer);
                                     }
                                 },
                                 materializer, 
                                 executor);
                 executor.execute(task);
                 return task;
             }
 
             public static Hash.Hashed hashOf(EnsembleView<ServerInetAddressView> value) {
                 return Hash.default32().apply(EnsembleView.toString(value));
             }
             
             public static Ensembles.Entity valueOf(String label) {
                 return of(Identifier.valueOf(label));
             }
             
             public static Ensembles.Entity of(Identifier identifier) {
                 return new Entity(identifier);
             }
             
             public Entity(Identifier identifier) {
                 super(identifier);
             }
             
             @Override
             public String toString() {
                 return get().toString();
             }
 
             @ZNode(type=EnsembleView.class)
             public static class Backend extends Control.TypedValueZNode<EnsembleView<ServerInetAddressView>> {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("backend");
 
                 public static ZNodeLabel.Path pathOf(Ensembles.Entity entity) {
                     return ZNodeLabel.Path.of(entity.path(), LABEL);
                 }
                 
                 public static ListenableFuture<Entity.Backend> get(Ensembles.Entity entity, Materializer<?,?> materializer) {
                     return get(Entity.Backend.class, entity, materializer);
                 }
                 
                 public static ListenableFuture<Entity.Backend> create(EnsembleView<ServerInetAddressView> value, Ensembles.Entity entity, Materializer<?,?> materializer) {
                     return create(Ensembles.Entity.Backend.class, value, entity, materializer);
                 }
                 
                 public static Entity.Backend of(EnsembleView<ServerInetAddressView> value, Ensembles.Entity parent) {
                     return new Backend(value, parent);
                 }
 
                 public Backend(EnsembleView<ServerInetAddressView> value, Ensembles.Entity parent) {
                     super(value, parent);
                 }
             }
             
             @ZNode
             public static class Peers extends Control.ControlZNode {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("peers");
                 
                 public static Entity.Peers of(Ensembles.Entity parent) {
                     return new Peers(parent);
                 }
 
                 public static CachedFunction<Identifier, List<Member>> getMembers(
                         final Materializer<?,?> materializer) {
                     Function<Identifier, List<Member>> cached = new Function<Identifier, List<Peers.Member>>() {
                         @Override
                         @Nullable
                         public
                         List<Member> apply(Identifier ensemble) {
                             Peers peers = Peers.of(Entity.of(ensemble));
                             return peers.get(materializer);
                         }
                     };
                     AsyncFunction<Identifier, List<Member>> lookup = new AsyncFunction<Identifier, List<Peers.Member>>() {
                         @Override
                         public ListenableFuture<List<Member>> apply(Identifier ensemble) {
                             final Peers peers = Peers.of(Entity.of(ensemble));
                             return Futures.transform(
                                     materializer.operator().getChildren(peers.path()).submit(),
                                     new AsyncFunction<Pair<? extends Operation.ProtocolRequest<Records.Request>, ? extends Operation.ProtocolResponse<Records.Response>>, List<Member>>() {
                                         @Override
                                         @Nullable
                                         public ListenableFuture<List<Member>> apply(Pair<? extends Operation.ProtocolRequest<Records.Request>, ? extends Operation.ProtocolResponse<Records.Response>> input) throws KeeperException {
                                             Operations.unlessError(input.second().getRecord());
                                             return Futures.immediateFuture(peers.get(materializer));
                                         }
                                     });
                         }
                     };
                     return CachedFunction.create(cached, lookup);
                 }
                 
                 public Peers(Ensembles.Entity parent) {
                     super(parent);
                 }
 
                 public List<Member> get(Materializer<?,?> materializer) {
                     ImmutableList.Builder<Member> members = ImmutableList.builder();
                     Materializer.MaterializedNode parent = materializer.get(path());
                     if (parent != null) {
                         for (ZNodeLabel.Component e: parent.keySet()) {
                             members.add(Member.valueOf(e.toString(), this));
                         }
                     }
                     return members.build();
                 }
                 
                 @ZNode
                 public static class Member extends Control.TypedLabelZNode<Identifier> {
 
                     @Label(type=LabelType.PATTERN)
                     public static final String LABEL_PATTERN = Identifier.PATTERN;
 
                     public static Peers.Member valueOf(String label, Entity.Peers parent) {
                         return of(Identifier.valueOf(label), parent);
                     }
                     
                     public static Peers.Member of(Identifier identifier, Entity.Peers parent) {
                         return new Member(identifier, parent);
                     }
                     
                     public Member(Identifier identifier, Entity.Peers parent) {
                         super(identifier, parent);
                     }
                     
                     @Override
                     public String toString() {
                         return get().toString();
                     }
                 }
             }
             
             @ZNode(type=Identifier.class, createMode=CreateMode.EPHEMERAL)
             public static class Leader extends Control.TypedValueZNode<Identifier> {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("leader");
                 
                 public static ListenableFuture<Entity.Leader> get(Ensembles.Entity parent, Materializer<?,?> materializer) {
                     return get(Entity.Leader.class, parent, materializer);
                 }
                 
                 public static ListenableFuture<Entity.Leader> create(Identifier value, Ensembles.Entity parent, Materializer<?,?> materializer) {
                     return create(Entity.Leader.class, value, parent, materializer);
                 }
                 
                 public static class Proposal<I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>> extends PromiseTask<Entity.Leader, Entity.Leader> implements Runnable {
 
                     public static <I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>>
                     Proposal<I,O> of(
                             Entity.Leader task, 
                             Materializer<I,O> materializer,
                             Executor executor) {
                         Promise<Entity.Leader> promise = SettableFuturePromise.create();
                         Proposal<I,O> proposal = new Proposal<I,O>(task, materializer, executor, promise);
                         executor.execute(proposal);
                         return proposal;
                     }
                     
                     protected final Materializer<I,O> materializer;
                     protected final Executor executor;
                     protected volatile ListenableFuture<Pair<I,O>> future;
                     
                     public Proposal(
                             Entity.Leader task, 
                             Materializer<I,O> materializer,
                             Executor executor,
                             Promise<Entity.Leader> delegate) {
                         super(task, delegate);
                         this.materializer = materializer;
                         this.executor = executor;
                         this.future = null;
                     }
                     
                     @Override
                     public synchronized void run() {
                         if (isDone()) {
                             return;
                         }
                         
                         if (future == null) {
                             Materializer<I,O>.Operator operator = materializer.operator();
                             operator.create(task.path(), task.get()).submit();
                             operator.sync(task.path()).submit();
                             future = operator.getData(task().path(), true).submit();
                             future.addListener(this, executor);
                         } else if (future.isDone()) {
                             try {
                                 Pair<I,O> result = future.get();
                                 Optional<Operation.Error> error = Operations.maybeError(result.second().getRecord(), KeeperException.Code.NONODE, result.toString());
                                 if (! error.isPresent()) {
                                     Materializer.MaterializedNode node = materializer.get(task.path());
                                     set(Entity.Leader.of((Identifier) node.get().get(), task().parent()));
                                 }
                             } catch (Throwable t) {
                                 setException(t);
                             }
                         }
                     }
                 }
                 
                 public static class Proposer<I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>> implements AsyncFunction<Entity.Leader, Entity.Leader> {
 
                     public static <I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>> Proposer<I,O> of(
                             Materializer<I,O> materializer,
                             Executor executor) {
                         return new Proposer<I,O>(materializer, executor);
                     }
                     
                     protected final Materializer<I,O> materializer;
                     protected final Executor executor;
                     
                     public Proposer(
                             Materializer<I,O> materializer,
                             Executor executor) {
                         this.materializer = materializer;
                         this.executor = executor;
                     }
                     
                     @Override
                     public ListenableFuture<Entity.Leader> apply(Entity.Leader input) {
                         return Proposal.of(input, materializer, executor);
                     }
                 }
                 
                 public static Entity.Leader of(Identifier value, Ensembles.Entity parent) {
                     return new Leader(value, parent);
                 }
 
                 public Leader(Identifier value, Ensembles.Entity parent) {
                     super(value, parent);
                 }
                 
                 public Ensembles.Entity parent() {
                     return (Ensembles.Entity) parent;
                 }
             }
         }
     }
 
     @ZNode(label="volumes")
     public static abstract class Volumes extends Control.ControlZNode {
         
         @ZNode
         public static class Entity extends Control.TypedLabelZNode<Identifier> {
 
             @Label(type=LabelType.PATTERN)
             public static final String LABEL_PATTERN = Identifier.PATTERN;
 
             public static <I extends Operation.ProtocolRequest<Records.Request>, O extends Operation.ProtocolResponse<Records.Response>> ListenableFuture<Volumes.Entity> create(
                     final VolumeDescriptor value, 
                     final Materializer<I,O> materializer,
                     final Executor executor) {
                 Control.RegisterHashedTask<I, O, VolumeDescriptor, Orchestra.Volumes.Entity> task = 
                         Control.RegisterHashedTask.of(
                                 value,
                                 Orchestra.Volumes.Entity.hashOf(value),
                                 new Function<Identifier, Orchestra.Volumes.Entity>() {
                                     @Override
                                     @Nullable
                                     public
                                     Volumes.Entity apply(@Nullable Identifier input) {
                                         return Orchestra.Volumes.Entity.of(input);
                                     }
                                 },
                                 new Function<Orchestra.Volumes.Entity, ZNodeLabel.Path>() {
                                     @Override
                                     @Nullable
                                     public
                                     ZNodeLabel.Path apply(@Nullable Volumes.Entity input) {
                                         return Orchestra.Volumes.Entity.Volume.pathOf(input);
                                     }
                                 },
                                 new AsyncFunction<Orchestra.Volumes.Entity, Orchestra.Volumes.Entity.Volume>() {
                                     @Override
                                     public ListenableFuture<Orchestra.Volumes.Entity.Volume> apply(
                                             Volumes.Entity input) {
                                         return Orchestra.Volumes.Entity.Volume.get(input, materializer);
                                     }
                                 },
                                 materializer, 
                                 executor);
                 executor.execute(task);
                 return task;
             }
             
             public static Hash.Hashed hashOf(VolumeDescriptor value) {
                 return Hash.default32().apply(value.getRoot().toString());
             }
             
             public static Volumes.Entity valueOf(String label) {
                 return of(Identifier.valueOf(label));
             }
             
             public static Volumes.Entity of(Identifier identifier) {
                 return new Entity(identifier);
             }
             
             public Entity(Identifier identifier) {
                 super(identifier);
             }
             
             @Override
             public String toString() {
                 return get().toString();
             }
             
             @ZNode(type=VolumeDescriptor.class)
             public static class Volume extends Control.TypedValueZNode<VolumeDescriptor> {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("volume");
 
                 public static ZNodeLabel.Path pathOf(Volumes.Entity entity) {
                     return ZNodeLabel.Path.of(entity.path(), LABEL);
                 }
                 
                 public static ListenableFuture<Entity.Volume> get(Volumes.Entity entity, Materializer<?,?> materializer) {
                     return get(Entity.Volume.class, entity, materializer);
                 }
                 
                 public static ListenableFuture<Entity.Volume> create(VolumeDescriptor value, Volumes.Entity entity, Materializer<?,?> materializer) {
                     return create(Entity.Volume.class, value, entity, materializer);
                 }
                 
                 public static Entity.Volume of(VolumeDescriptor value, Volumes.Entity parent) {
                     return new Volume(value, parent);
                 }
 
                 public Volume(VolumeDescriptor value, Volumes.Entity parent) {
                     super(value, parent);
                 }
             }
 
             @ZNode(type=Identifier.class)
             public static class Ensemble extends Control.TypedValueZNode<Identifier> {
 
                 @Label
                 public static ZNodeLabel.Component LABEL = ZNodeLabel.Component.of("ensemble");
                 
                 public static ListenableFuture<Entity.Ensemble> get(Volumes.Entity entity, Materializer<?,?> materializer) {
                     return get(Entity.Ensemble.class, entity, materializer);
                 }
                 
                 public static ListenableFuture<Entity.Ensemble> create(Identifier value, Volumes.Entity entity, Materializer<?,?> materializer) {
                     return create(Entity.Ensemble.class, value, entity, materializer);
                 }
                 
                 public static Entity.Ensemble of(Identifier value, Volumes.Entity parent) {
                     return new Ensemble(value, parent);
                 }
 
                 public Ensemble(Identifier value, Volumes.Entity parent) {
                     super(value, parent);
                 }
             }
         }            
     }
 }
