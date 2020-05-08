 /**
  * 
  */
 package edu.jhu.hlt.rebar.file;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.protobuf.Descriptors.FieldDescriptor;
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.google.protobuf.Message;
 
 import edu.jhu.hlt.concrete.Concrete;
 import edu.jhu.hlt.concrete.Concrete.Communication;
 import edu.jhu.hlt.concrete.ConcreteException;
 import edu.jhu.hlt.concrete.io.ProtocolBufferReader;
 import edu.jhu.hlt.concrete.util.IdUtil;
 import edu.jhu.hlt.concrete.util.ProtoFactory;
 import edu.jhu.hlt.rebar.Corpus;
 import edu.jhu.hlt.rebar.IndexedCommunication;
 import edu.jhu.hlt.rebar.ProtoIndex;
 import edu.jhu.hlt.rebar.ProtoIndex.ModificationTarget;
 import edu.jhu.hlt.rebar.RebarException;
 import edu.jhu.hlt.rebar.Stage;
 
 /**
  * @author max
  * 
  */
 public class FileBackedCorpus implements Corpus {
 
   private static final Logger logger = LoggerFactory.getLogger(FileBackedCorpus.class);
 
   private final Path pathOnDisk;
   private final Path stagesPath;
   private final Path commsPath;
 
   private final Connection conn;
   private final Set<String> commIdSet;
 
   public FileBackedCorpus(Path pathOnDisk, Set<String> commIdSet, Connection conn) throws RebarException {
     this.pathOnDisk = pathOnDisk;
     this.stagesPath = this.pathOnDisk.resolve("stages");
     this.commsPath = this.pathOnDisk.resolve("communications");
     this.conn = conn;
     this.commIdSet = commIdSet;
   }
 
   public FileBackedCorpus(Path pathOnDisk, Connection conn) throws RebarException {
     this.pathOnDisk = pathOnDisk;
     this.stagesPath = this.pathOnDisk.resolve("stages");
     this.commsPath = this.pathOnDisk.resolve("communications");
     this.conn = conn;
     this.commIdSet = this.queryCommIdSet();
   }
 
   public Path getPath() {
     return this.pathOnDisk;
   }
 
   public Set<String> getCommIdSet() {
     return Collections.unmodifiableSet(this.commIdSet);
   }
 
   @Override
   public String getName() {
     return this.pathOnDisk.getFileName().toString();
   }
 
   @Override
   public void close() throws RebarException {
     try {
       this.conn.close();
     } catch (SQLException e) {
       throw new RebarException(e);
     }
   }
 
   @Override
   public Stage makeStage(String stageName, String stageVersion, Set<Stage> dependencies, String description, boolean deleteIfExists) throws RebarException {
     try {
       if (deleteIfExists)
         if (this.hasStage(stageName, stageVersion))
           this.deleteStage(this.getStage(stageName, stageVersion));
 
       // if we don't want to delete and the stage exists, throw an
       // exception.
       if (this.hasStage(stageName, stageVersion) && !deleteIfExists)
         throw new RebarException("Stage: " + stageName + " already exists!");
 
       PreparedStatement ps = this.conn.prepareStatement("INSERT INTO stages VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
       ps.setString(1, null);
       ps.setString(2, stageName);
       ps.setString(3, stageVersion);
       ps.setString(4, description);
       ps.execute();
       ResultSet rs = ps.getGeneratedKeys();
       int id;
       if (rs.next())
         id = rs.getInt(1);
       else
         throw new SQLException("Creating stage failed; didn't get an id back after insert.");
       if (dependencies.size() > 0) {
         PreparedStatement depStmt = this.conn.prepareStatement("INSERT INTO dependencies VALUES (?, ?)");
         for (Stage dep : dependencies) {
           depStmt.setInt(1, id);
           depStmt.setInt(2, dep.getStageId());
           depStmt.addBatch();
         }
 
         depStmt.executeBatch();
       }
       return new FileStage(stageName, stageVersion, id, this.pathOnDisk, dependencies, description, false);
     } catch (SQLException e) {
       throw new RebarException(e);
     }
   }
 
   @Override
   public void markStagePublic(Stage stage) throws RebarException {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public Stage getStage(String stageName, String stageVersion) throws RebarException {
     return this.queryStage(stageName, stageVersion);
   }
 
   @Override
   public Stage getStage(int stageId) throws RebarException {
     return this.queryStage(stageId);
   }
 
   @Override
   public Stage getStage(String stageString) throws RebarException {
     String[] splits = stageString.split(" ");
     return this.queryStage(splits[0], splits[1]);
   }
 
   @Override
   public SortedSet<Stage> getStages() throws RebarException {
     return this.getStagesInCorpus();
   }
 
   @Override
   public boolean hasStage(String stageName, String stageVersion) throws RebarException {
     return this.queryStageExists(stageName, stageVersion);
   }
 
   @Override
   public void deleteStage(Stage stage) throws RebarException {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public Collection<String> readComIdSet(File filename) throws RebarException {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public void registerComIdSet(String name, Collection<String> idSet) throws RebarException {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public Collection<String> lookupComIdSet(String name) throws RebarException {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public Collection<String> getComIdSetNames() throws RebarException {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public Initializer makeInitializer() throws RebarException {
     // TODO
     return null;
     // return new FileCorpusInitializer(this.pathOnDisk.resolve("raw"),
     // this.getConnection());
   }
 
   public class FileCorpusReader implements Reader {
 
     private final Path commPath;
     private final Set<Stage> stagesToLoad;
     private final Connection conn;
     private final NavigableMap<Integer, Stage> stagesToRead;
 
     public FileCorpusReader() {
       this(new TreeSet<Stage>());
     }
 
     public FileCorpusReader(Set<Stage> stagesToLoad) {
       this.commPath = FileBackedCorpus.this.commsPath;
       this.stagesToLoad = stagesToLoad;
       this.conn = FileBackedCorpus.this.conn;
       this.stagesToRead = new TreeMap<>();
       stagesToRead.put(0, null); // root cell.
       for (Stage stage : this.stagesToLoad)
         addStageDependencies(stage);
     }
 
     /**
      * Recursively add dependencies.
      * 
      * @param stage
      * @param ids
      */
     private void addStageDependencies(Stage stage) {
       if (!this.stagesToRead.containsKey(stage.getStageId())) {
         this.stagesToRead.put(stage.getStageId(), stage);
         for (Stage dep : stage.getDependencies())
           addStageDependencies(dep);
       }
     }
 
     @Override
     public IndexedCommunication loadCommunication(String comid) throws RebarException {
       Path pathToComm = this.commPath.resolve(comid + ".pb");
       if (!Files.exists(pathToComm))
         throw new RebarException("Comm: " + comid + " doesn't exist.");
 
       try {
         File commFile = pathToComm.toFile();
        ProtocolBufferReader<Communication> pbr = new ProtocolBufferReader<Communication>(new FileInputStream(commFile), Communication.class);
        Communication comm = pbr.next();
         pbr.close();
 
         Communication commToUse = this.assembleComm(comm);
         IndexedCommunication ic = new IndexedCommunication(commToUse, new ProtoIndex(commToUse), null);
         return ic;
       } catch (ConcreteException | IOException e) {
         throw new RebarException(e);
       }
     }
 
     /**
      * Returns null if nothing changed, or the new value if we merged something in.
      */
     private Message mergeStages(Message msg, Map<ProtoIndex.ModificationTarget, List<StageOutput>> stageValues) throws RebarException {
       Message.Builder builder = null;
 
       // Get the modification target for this message.
       final ProtoIndex.ModificationTarget target;
       Concrete.UUID uuid = IdUtil.getUUIDOrNull(msg); // uuid may be null.
       if (uuid != null)
         target = new ProtoIndex.ModificationTarget(uuid);
       else
         target = null;
       // System.err.println("Merging, target="+target);
       // Get any values we should merge into this target, and merge them.
       List<StageOutput> valuesToMerge = stageValues.remove(target);
       if (valuesToMerge != null) {
         try {
           // Merge in the new values.
           for (StageOutput stageOutput : valuesToMerge) {
             if (builder == null)
               builder = msg.toBuilder();
 
             builder.mergeFrom(stageOutput.mod);
 
           }
         } catch (InvalidProtocolBufferException e) {
           throw new RebarException(e);
         }
         // If we're done with everything, then return immediately.
         if (stageValues.isEmpty())
           return builder.build();
         // If the stage we just merged in is depended on by other
         // stages, then we need to rebuild the message before we
         // traverse it... Currently we don't keep that info, so
         // for now, just always do it.
         if (true) {
           msg = builder.build();
           builder = null;
         }
       }
 
       // Recurse to subfields
       for (Map.Entry<FieldDescriptor, Object> field : msg.getAllFields().entrySet()) {
         FieldDescriptor fd = field.getKey();
         if (fd.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
           if (fd.isRepeated()) {
             @SuppressWarnings("unchecked")
             List<Message> children = (List<Message>) field.getValue();
             for (int i = 0; i < children.size(); i++) {
               Message child = children.get(i);
               Message mergedChild = mergeStages(child, stageValues);
               if (mergedChild != null) {
                 if (builder == null)
                   builder = msg.toBuilder();
                 builder.setRepeatedField(fd, i, mergedChild);
                 if (stageValues.isEmpty())
                   return builder.build();
               }
             }
           } else {
             Message child = (Message) field.getValue();
 
             Message mergedChild = mergeStages(child, stageValues);
             if (mergedChild != null) {
               if (builder == null)
                 builder = msg.toBuilder();
               builder.setField(fd, mergedChild);
               if (stageValues.isEmpty())
                 return builder.build();
             }
           }
         }
       }
 
       if (builder != null)
         return builder.build();
       else if (valuesToMerge != null)
         return msg;
       else
         return null;
     }
 
     private List<StageOutput> queryStages(String commId) throws RebarException {
       try {
         List<StageOutput> soList = new ArrayList<>();
         String sql = "SELECT stage_id, target, mods FROM stage_mods WHERE " + "comm_id = ?";
         PreparedStatement ps = this.conn.prepareStatement(sql);
         ps.setString(1, commId);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
           int stageId = rs.getInt(1);
           ModificationTarget mt = new ModificationTarget(rs.getBytes(2));
           byte[] value = rs.getBytes(3);
 
           StageOutput so = new StageOutput(stageId, mt, value);
           soList.add(so);
         }
         ps.close();
 
         return soList;
       } catch (SQLException e) {
         throw new RebarException(e);
       }
     }
 
     private Communication assembleComm(Communication original) throws RebarException {
       try {
         String commId = original.getGuid().getCommunicationId();
         List<StageOutput> soList = this.queryStages(commId);
         Communication.Builder mergedComm = original.toBuilder();
         for (StageOutput so : soList)
           mergedComm = mergedComm.mergeFrom(so.mod);
 
         Communication commToUse = mergedComm.build();
         return commToUse;
       } catch (InvalidProtocolBufferException e) {
         throw new RebarException(e);
       }
     }
 
     @Override
     public Iterator<IndexedCommunication> loadCommunications(Collection<String> subset) throws RebarException {
       if (!commIdSet.containsAll(subset))
         throw new RebarException("One or more subset comms " + " were not in this corpus' communication ID set.");
       try {
         List<IndexedCommunication> commSet = new ArrayList<>();
 
         DirectoryStream<Path> ds = Files.newDirectoryStream(this.commPath);
         Iterator<Path> pathIter = ds.iterator();
         while (pathIter.hasNext()) {
           Path nextPath = pathIter.next();
           String fileName = nextPath.getFileName().toString();
           // remove ".pb" from filename
           String commId = fileName.substring(0, fileName.length() - 3);
           if (subset.contains(commId)) {
             Communication c = ProtoFactory.readCommunicationFromPath(nextPath);
             c = this.assembleComm(c);
             IndexedCommunication ic = new IndexedCommunication(c, new ProtoIndex(c), null);
             commSet.add(ic);
           }
         }
 
         ds.close();
         return commSet.iterator();
       } catch (IOException | ConcreteException e) {
         throw new RebarException(e);
       }
     }
 
     @Override
     public Iterator<IndexedCommunication> loadCommunications() throws RebarException {
       return loadCommunications(commIdSet);
     }
 
     @Override
     public void close() throws RebarException {
       // nothing to do here.
     }
 
     @Override
     public Collection<Stage> getInputStages() throws RebarException {
       return this.stagesToLoad;
     }
 
     @Override
     public Corpus getCorpus() {
       return FileBackedCorpus.this;
     }
 
   }
 
   @Override
   public Reader makeReader(Collection<Stage> stages) throws RebarException {
     Set<Stage> stageSet = new TreeSet<>();
     stageSet.addAll(stages);
     return new FileCorpusReader(stageSet);
   }
 
   @Override
   public Reader makeReader(Stage[] stages) throws RebarException {
     Set<Stage> stageSet = new TreeSet<>();
     List<Stage> stageList = Arrays.asList(stages);
     stageSet.addAll(stageList);
     return new FileCorpusReader(stageSet);
   }
 
   @Override
   public Reader makeReader(Stage stage) throws RebarException {
     if (!this.hasStage(stage.getStageName(), stage.getStageVersion()))
       throw new RebarException("Stage: " + stage.getStageName() + " doesn't exist.");
     Set<Stage> stageSet = new TreeSet<>();
     stageSet.add(stage);
     return new FileCorpusReader(stageSet);
   }
 
   @Override
   public Reader makeReader() throws RebarException {
     return new FileCorpusReader();
   }
 
   @Override
   public Reader makeReader(Collection<Stage> stages, boolean loadStageOwnership) throws RebarException {
     return this.makeReader(stages);
   }
 
   @Override
   public Reader makeReader(Stage[] stages, boolean loadStageOwnership) throws RebarException {
     return this.makeReader(stages);
   }
 
   @Override
   public Reader makeReader(Stage stage, boolean loadStageOwnership) throws RebarException {
     return this.makeReader(stage);
   }
 
   @Override
   public Reader makeReader(boolean loadStageOwnership) throws RebarException {
     return this.makeReader();
   }
 
   @Override
   public long getNumCommunications() throws RebarException {
     return this.commIdSet.size();
   }
 
   public class FileCorpusWriter implements Writer {
 
     private Stage stage;
     private String tableName;
     private Connection conn;
 
     // private Kryo kryo;
 
     public FileCorpusWriter(Stage stage) throws RebarException {
       try {
         this.stage = stage;
         this.conn = FileBackedCorpus.this.conn;
         this.tableName = stage.getStageName() + "_" + stage.getStageVersion();
       } finally {
 
       }
 
     }
 
     @Override
     public void saveCommunication(IndexedCommunication comm) throws RebarException {
       try {
         String sql = "INSERT INTO '" + "stage_mods" + "' VALUES(?, ?, ?, ?)";
         PreparedStatement ps = this.conn.prepareStatement(sql);
         ps.setInt(1, this.stage.getStageId());
         ps.setString(2, comm.getCommunicationId());
 
         ProtoIndex pi = comm.getIndex();
         Map<ModificationTarget, byte[]> delta = pi.getUnsavedModifications();
         for (Entry<ModificationTarget, byte[]> entry : delta.entrySet()) {
           ModificationTarget mt = entry.getKey();
           byte[] mods = entry.getValue();
 
           ps.setBytes(3, mt.toBytes());
           ps.setBytes(4, mods);
 
           ps.addBatch();
         }
 
         ps.executeBatch();
         ps.close();
         pi.clearUnsavedModifications();
       } catch (SQLException e) {
         throw new RebarException(e);
       }
     }
 
     @Override
     public void flush() throws RebarException {
 
     }
 
     @Override
     public void close() throws RebarException {
       try {
         this.conn = null;
       } finally {
 
       }
     }
 
     @Override
     public Stage getOutputStage() {
       return this.stage;
     }
   }
 
   @Override
   public Writer makeWriter(Stage stage) throws RebarException {
     return new FileCorpusWriter(stage);
   }
 
   @Override
   public DiffWriter makeDiffWriter(Stage stage) throws RebarException {
     // TODO Auto-generated method stub
     return null;
   }
 
   private int queryStageId(String stageName, String stageVersion) throws RebarException {
     PreparedStatement ps = null;
     try {
       ps = this.conn.prepareStatement("SELECT id FROM stages WHERE name = ? AND version = ?");
       ps.setString(1, stageName);
       ps.setString(2, stageVersion);
       ResultSet rs = ps.executeQuery();
       if (rs.next())
         return rs.getInt(1);
       else
         throw new SQLException("Didn't get an id for stage: " + stageName + " and version: " + stageVersion);
 
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private boolean queryStageExists(String stageName, String stageVersion) throws RebarException {
     PreparedStatement ps = null;
     try {
       ps = this.conn.prepareStatement("SELECT id FROM stages WHERE name = ? AND version = ?");
       ps.setString(1, stageName);
       ps.setString(2, stageVersion);
       ResultSet rs = ps.executeQuery();
       return rs.next();
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private FileStage queryStage(int stageId) throws RebarException {
     PreparedStatement ps = null;
     try {
       Set<Stage> dependencies = this.queryStageDependencies(stageId);
       ps = this.conn.prepareStatement("SELECT name, version, description FROM stages WHERE id = ?");
       ps.setInt(1, stageId);
       ResultSet rs = ps.executeQuery();
       if (rs.next()) {
         String name = rs.getString(1);
         String vers = rs.getString(2);
         String desc = rs.getString(3);
         return new FileStage(name, vers, stageId, this.pathOnDisk, dependencies, desc, true);
       } else
         throw new SQLException("Didn't find id: " + stageId);
 
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private FileStage queryStage(String stageName, String stageVersion) throws RebarException {
     PreparedStatement ps = null;
     try {
       int stageId = this.queryStageId(stageName, stageVersion);
       Set<Stage> dependencies = this.queryStageDependencies(stageId);
       ps = this.conn.prepareStatement("SELECT description FROM stages WHERE id = ?");
       ps.setInt(1, stageId);
       ResultSet rs = ps.executeQuery();
       if (rs.next()) {
         String desc = rs.getString(1);
         return new FileStage(stageName, stageVersion, stageId, this.pathOnDisk, dependencies, desc, true);
       } else
         throw new SQLException("Didn't get an id for stage: " + stageName + " and version: " + stageVersion);
 
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private List<Integer> queryDependencyIds(int stageId) throws RebarException {
     List<Integer> intList = new ArrayList<>();
     PreparedStatement ps = null;
     try {
       ps = this.conn.prepareStatement("SELECT dependency_id FROM dependencies WHERE stage_id = ?");
       ps.setInt(1, stageId);
       ResultSet rs = ps.executeQuery();
       while (rs.next())
         intList.add(rs.getInt(1));
 
       rs.close();
       return intList;
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private SortedSet<Stage> getStagesInCorpus() throws RebarException {
     try {
       SortedSet<Stage> corpusStages = new TreeSet<>();
       PreparedStatement ps = this.conn.prepareStatement("SELECT id, name, version, description FROM stages");
       ResultSet rs = ps.executeQuery();
       while (rs.next()) {
         int stageId = rs.getInt("id");
         String stageName = rs.getString("name");
         String stageVersion = rs.getString("version");
         String stageDesc = rs.getString("description");
         corpusStages.add(new FileStage(stageName, stageVersion, stageId, this.pathOnDisk, this.queryStageDependencies(stageId), stageDesc, true));
       }
 
       return corpusStages;
     } catch (SQLException e) {
       throw new RebarException(e);
     }
   }
 
   private class StringThreeTuple {
     public final String name;
     public final String version;
     public final String desc;
 
     private StringThreeTuple(String name, String version, String desc) {
       this.name = name;
       this.version = version;
       this.desc = desc;
     }
   }
 
   private StringThreeTuple queryStageInfo(int stageId) throws RebarException {
     String sql = "SELECT name, version, description FROM stages " + "WHERE id = ?";
     PreparedStatement ps = null;
     try {
       ps = this.conn.prepareStatement(sql);
       ps.setInt(1, stageId);
       ResultSet rs = ps.executeQuery();
       while (rs.next()) {
         String n = rs.getString(1);
         String v = rs.getString(2);
         String d = rs.getString(3);
 
         return new StringThreeTuple(n, v, d);
       }
     } catch (SQLException e) {
       throw new RebarException(e);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.trace(e.getMessage(), e);
         }
     }
 
     // if we get here, we didn't find a stage with that ID.
     // throw an exception.
     throw new RebarException("Couldn't find stage with id: " + stageId + " in stages table.");
   }
 
   private Set<Stage> queryStageDependencies(int stageId) throws RebarException {
     Set<Stage> stages = new TreeSet<>();
     PreparedStatement ps = null;
     try {
       ps = this.conn.prepareStatement("SELECT dependency_id " + "FROM dependencies WHERE stage_id = ?");
       ps.setInt(1, stageId);
       ResultSet rs = ps.executeQuery();
       boolean hasMoreDeps = rs.next();
 
       // if no deps, exit recursion.
       if (!hasMoreDeps) {
         return new TreeSet<Stage>();
       } else {
         while (hasMoreDeps) {
           int depId = rs.getInt(1);
           // recurse to find dependencies until we've satisfied them.
           Set<Stage> deps = this.queryStageDependencies(depId);
           StringThreeTuple stt = this.queryStageInfo(depId);
           stages.add(new FileStage(stt.name, stt.version, stageId, this.pathOnDisk, deps, stt.desc, true));
 
           hasMoreDeps = rs.next();
         }
 
         return stages;
       }
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private Set<String> queryCommunicationIds() throws RebarException {
     Set<String> stages = new TreeSet<>();
     PreparedStatement ps = null;
     try {
       ps = this.conn.prepareStatement("SELECT guid " + "FROM comm_ids");
       ResultSet rs = ps.executeQuery();
       while (rs.next()) {
         stages.add(rs.getString(1));
       }
 
       return stages;
     } catch (SQLException ioe) {
       throw new RebarException(ioe);
     } finally {
       if (ps != null)
         try {
           ps.close();
         } catch (SQLException e) {
           logger.debug("Failed to close statement.", e);
         }
     }
   }
 
   private Set<String> queryCommIdSet() throws RebarException {
     try {
       Set<String> commIdSet = new TreeSet<>();
       PreparedStatement ps = this.conn.prepareStatement("SELECT guid FROM comm_ids");
       ResultSet rs = ps.executeQuery();
       while (rs.next()) {
         commIdSet.add(rs.getString(1));
       }
 
       ps.close();
       return commIdSet;
     } catch (SQLException e) {
       throw new RebarException(e);
     }
   }
 }
