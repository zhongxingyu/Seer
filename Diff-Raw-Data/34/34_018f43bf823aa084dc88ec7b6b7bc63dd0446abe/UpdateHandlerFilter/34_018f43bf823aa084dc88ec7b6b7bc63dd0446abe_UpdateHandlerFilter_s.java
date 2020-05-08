 package jp.sf.fess.solr.plugin.update;
 
 import java.io.IOException;
 
 import org.apache.solr.update.AddUpdateCommand;
 import org.apache.solr.update.CommitUpdateCommand;
 import org.apache.solr.update.DeleteUpdateCommand;
 import org.apache.solr.update.MergeIndexesCommand;
 import org.apache.solr.update.RollbackUpdateCommand;
 import org.apache.solr.update.SplitIndexCommand;
 
 public abstract class UpdateHandlerFilter {
 
     protected FessUpdateHandler updateHandler;
 
     public void setFessUpdateHandler(final FessUpdateHandler updateHandler) {
         this.updateHandler = updateHandler;
     }
 
     public int addDoc(final AddUpdateCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         return chain.addDoc(cmd);
     }
 
     public void delete(final DeleteUpdateCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         chain.delete(cmd);
     }
 
     public void deleteByQuery(final DeleteUpdateCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         chain.deleteByQuery(cmd);
     }
 
     public int mergeIndexes(final MergeIndexesCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         return chain.mergeIndexes(cmd);
     }
 
     public void prepareCommit(final CommitUpdateCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         chain.prepareCommit(cmd);
     }
 
     public void commit(final CommitUpdateCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         chain.commit(cmd);
     }
 
     public void rollback(final RollbackUpdateCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         chain.rollback(cmd);
     }
 
     public void split(final SplitIndexCommand cmd,
             final UpdateHandlerFilterChain chain) throws IOException {
         chain.split(cmd);
     }
 
 }
