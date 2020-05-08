 package jp.sf.fess.solr.plugin.update;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.solr.core.SolrConfig;
 import org.apache.solr.core.SolrCore;
 import org.apache.solr.update.AddUpdateCommand;
 import org.apache.solr.update.CommitUpdateCommand;
 import org.apache.solr.update.DeleteUpdateCommand;
 import org.apache.solr.update.DirectUpdateHandler2;
 import org.apache.solr.update.MergeIndexesCommand;
 import org.apache.solr.update.RollbackUpdateCommand;
 import org.apache.solr.update.SplitIndexCommand;
 import org.apache.solr.update.UpdateHandler;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class FessUpdateHandler extends DirectUpdateHandler2 {
 
     protected UpdateHandlerFilter[] filters;
 
     public FessUpdateHandler(final SolrCore core) {
         super(core);
         loadFilters();
     }
 
     public FessUpdateHandler(final SolrCore core,
             final UpdateHandler updateHandler) {
         super(core, updateHandler);
         loadFilters();
     }
 
     public SolrCore getSolrCore() {
         return core;
     }
 
     protected void loadFilters() {
         final SolrConfig solrConfig = core.getSolrConfig();
         final NodeList filterNodeList = solrConfig.getNodeList(
                 "updateHandler/filters", false);
 
         if (filterNodeList == null) {
             log.info("No UpdateHandlerFilter.");
             filters = new UpdateHandlerFilter[0];
         } else {
             final List<UpdateHandlerFilter> filterList = new ArrayList<UpdateHandlerFilter>();
             for (int i = 0; i < filterNodeList.getLength(); i++) {
                 final Node filterItem = filterNodeList.item(i);
                 final String nodeName = filterItem.getNodeName();
                 if ("filter".equals(nodeName)) {
                     final Node classItem = filterItem.getAttributes()
                             .getNamedItem("class");
                     final String className = classItem.getNodeValue();
                     try {
                         @SuppressWarnings("unchecked")
                         final Class<UpdateHandlerFilter> clazz = (Class<UpdateHandlerFilter>) Class
                                 .forName(className);
                         final UpdateHandlerFilter filter = clazz.newInstance();
                         filter.setFessUpdateHandler(this);
                         log.info("Load " + className);
                         filterList.add(filter);
                     } catch (final Exception e) {
                         throw new IllegalStateException("Could not load "
                                 + className, e);
                     }
                 }
 
             }
             filters = filterList.toArray(new UpdateHandlerFilter[filterList
                     .size()]);
         }
     }
 
     @Override
     public int addDoc(final AddUpdateCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         return chain.addDoc(cmd);
     }
 
     protected int doAddDoc(final AddUpdateCommand cmd) throws IOException {
         return super.addDoc(cmd);
     }
 
     @Override
     public void delete(final DeleteUpdateCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         chain.delete(cmd);
     }
 
     protected void doDelete(final DeleteUpdateCommand cmd) throws IOException {
         super.delete(cmd);
     }
 
     @Override
     public void deleteByQuery(final DeleteUpdateCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         chain.deleteByQuery(cmd);
     }
 
     protected void doDeleteByQuery(final DeleteUpdateCommand cmd)
             throws IOException {
         super.deleteByQuery(cmd);
     }
 
     @Override
     public int mergeIndexes(final MergeIndexesCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         return chain.mergeIndexes(cmd);
     }
 
     protected int doMergeIndexes(final MergeIndexesCommand cmd)
             throws IOException {
         return super.mergeIndexes(cmd);
     }
 
     @Override
     public void prepareCommit(final CommitUpdateCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         chain.prepareCommit(cmd);
     }
 
     protected void doPrepareCommit(final CommitUpdateCommand cmd)
             throws IOException {
         super.prepareCommit(cmd);
     }
 
     @Override
     public void commit(final CommitUpdateCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         chain.commit(cmd);
     }
 
     protected void doCommit(final CommitUpdateCommand cmd) throws IOException {
         super.commit(cmd);
     }
 
     @Override
     public void rollback(final RollbackUpdateCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         chain.rollback(cmd);
     }
 
     protected void doRollback(final RollbackUpdateCommand cmd)
             throws IOException {
         super.rollback(cmd);
     }
 
     @Override
     public void split(final SplitIndexCommand cmd) throws IOException {
         final UpdateHandlerFilterChain chain = new UpdateHandlerFilterChain(
                 this, filters);
         chain.split(cmd);
     }
 
     protected void doSplit(final SplitIndexCommand cmd) throws IOException {
         super.split(cmd);
     }
 }
