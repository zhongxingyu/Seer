 /*
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.walkaround.wave.server.googleimport;
 
 import com.google.common.collect.ImmutableList;
 import com.google.gxp.base.GxpContext;
 import com.google.gxp.html.HtmlClosure;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.walkaround.proto.ImportTaskPayload;
 import com.google.walkaround.proto.ImportWaveTask;
 import com.google.walkaround.proto.gson.ImportTaskPayloadGsonImpl;
 import com.google.walkaround.proto.gson.ImportWaveTaskGsonImpl;
 import com.google.walkaround.util.server.HtmlEscaper;
 import com.google.walkaround.util.server.RetryHelper;
 import com.google.walkaround.util.server.RetryHelper.PermanentFailure;
 import com.google.walkaround.util.server.RetryHelper.RetryableFailure;
 import com.google.walkaround.util.server.appengine.CheckedDatastore;
 import com.google.walkaround.util.server.appengine.CheckedDatastore.CheckedTransaction;
 import com.google.walkaround.util.server.auth.InvalidSecurityTokenException;
 import com.google.walkaround.util.server.servlet.BadRequestException;
 import com.google.walkaround.util.shared.Assert;
 import com.google.walkaround.wave.server.auth.NeedNewOAuthTokenException;
 import com.google.walkaround.wave.server.auth.UserContext;
 import com.google.walkaround.wave.server.auth.StableUserId;
 import com.google.walkaround.wave.server.auth.XsrfHelper;
 import com.google.walkaround.wave.server.auth.XsrfHelper.XsrfTokenExpiredException;
 import com.google.walkaround.wave.server.gxp.ImportOverviewFragment;
 import com.google.walkaround.wave.server.gxp.ImportWaveDisplayRecord;
 import com.google.walkaround.wave.server.gxp.SourceInstance;
 import com.google.walkaround.wave.server.servlet.PageSkinWriter;
 import com.google.walkaround.wave.server.util.AbstractHandler;
 
 import org.joda.time.Instant;
 import org.joda.time.LocalDate;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.util.Pair;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Interactive entry point for import features.
  *
  * @author ohler@google.com (Christian Ohler)
  */
 // For now, this shows a lot of detail, more than typical users would want.
 // Once it works well, we should simplify it.
 public class ImportOverviewHandler extends AbstractHandler {
 
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(ImportOverviewHandler.class.getName());
 
   private static final String XSRF_ACTION = "importaction";
 
   @Inject ParticipantId participantId;
   @Inject StableUserId userId;
   @Inject XsrfHelper xsrfHelper;
   @Inject SourceInstance.Factory sourceInstanceFactory;
   @Inject TaskDispatcher taskDispatcher;
   // Providers because the values are only needed in some branches of the code.
   @Inject Provider<CheckedDatastore> datastore;
   @Inject Provider<PerUserTable> perUserTable;
   @Inject Provider<FindRemoteWavesProcessor> findProcessor;
   @Inject PageSkinWriter pageSkinWriter;
   @Inject UserContext userContext;
 
   private List<String> getTasksInProgress(CheckedTransaction tx)
       throws RetryableFailure, PermanentFailure {
     ImmutableList.Builder<String> out = ImmutableList.builder();
     List<ImportTask> tasks = perUserTable.get().getAllTasks(tx, userId);
     for (ImportTask task : tasks) {
       out.add(taskDispatcher.describeTask(task));
     }
     return out.build();
   }
 
   private List<ImportWaveDisplayRecord> getWaves(CheckedTransaction tx)
       throws RetryableFailure, PermanentFailure {
     ImmutableList.Builder<ImportWaveDisplayRecord> out = ImmutableList.builder();
     List<RemoteWave> waves = perUserTable.get().getAllWaves(tx, userId);
     for (RemoteWave wave : waves) {
       WaveId waveId = WaveId.deserialise(wave.getDigest().getWaveId());
       out.add(
           new ImportWaveDisplayRecord(
               wave.getSourceInstance(),
               waveId,
               wave.getSourceInstance().getWaveLink(waveId),
               // Let's assume that participant 0 is the creator even if that's not always true.
               // Participant lists can be empty.
               wave.getDigest().getParticipantSize() == 0
                   ? "<unknown>"
                   : wave.getDigest().getParticipant(0),
               wave.getDigest().getTitle(),
               "" + new LocalDate(new Instant(wave.getDigest().getLastModifiedMillis())),
               // TODO(ohler): implement this.
               "?",
               // The robot API only allows access to waves with ids that start with "w".
               waveId.getId().startsWith("w+"),
               // TODO(ohler): implement this.
               null));
     }
     return out.build();
   }
 
   private String getInstanceSelectionHtml() {
     StringBuilder out = new StringBuilder();
     boolean first = true;
     for (SourceInstance instance : sourceInstanceFactory.getInstances()) {
       String instanceId = instance.serialize();
       Assert.check(instanceId.matches("[a-zA-Z_.]+"),
           "Bad characters in instance id: %s", instance);
       out.append("<input type='radio'" + (first ? " checked='checked'" : "")
           + " name='instance' value='" + instanceId + "'>"
           + HtmlEscaper.HTML_ESCAPER.escape(instance.getLongName())
           + "</input><br/>");
       first = false;
     }
     return out.toString();
   }
 
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (!userContext.hasOAuthCredentials()) {
      throw new NeedNewOAuthTokenException("No OAuth credentials: " + userContext);
    }
     Pair<List<String>, List<ImportWaveDisplayRecord>> pair;
     try {
       pair = new RetryHelper().run(
           new RetryHelper.Body<Pair<List<String>, List<ImportWaveDisplayRecord>>>() {
             @Override public Pair<List<String>, List<ImportWaveDisplayRecord>> run()
                 throws RetryableFailure, PermanentFailure {
               CheckedTransaction tx = datastore.get().beginTransaction();
               try {
                 return Pair.of(getTasksInProgress(tx), getWaves(tx));
               } finally {
                 tx.rollback();
               }
             }
           });
     } catch (PermanentFailure e) {
       throw new IOException("PermanentFailure retrieving import records", e);
     }
     List<String> tasksInProgress = pair.getFirst();
     List<ImportWaveDisplayRecord> waveDisplayRecords = pair.getSecond();
     final String instanceSelectionHtml = getInstanceSelectionHtml();
     resp.setContentType("text/html");
     resp.setCharacterEncoding("UTF-8");
     pageSkinWriter.write("Walkaround Import", participantId.getAddress(),
         ImportOverviewFragment.getGxpClosure(
             participantId.getAddress(),
             xsrfHelper.createToken(XSRF_ACTION),
             new HtmlClosure() {
               @Override public void write(Appendable out, GxpContext context) throws IOException {
                 out.append(instanceSelectionHtml);
               }
             },
             tasksInProgress,
             waveDisplayRecords));
   }
 
   @Override
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     if (!userContext.hasOAuthCredentials()) {
      throw new NeedNewOAuthTokenException("POST with no OAuth credentials: " + userContext);
     }
     try {
       xsrfHelper.verify(XSRF_ACTION, requireParameter(req, "token"));
     } catch (XsrfTokenExpiredException e) {
       throw new BadRequestException(e);
     } catch (InvalidSecurityTokenException e) {
       throw new BadRequestException(e);
     }
     String action = requireParameter(req, "action");
     if ("findwaves".equals(action)) {
       SourceInstance instance =
           sourceInstanceFactory.parseUnchecked(requireParameter(req, "instance"));
       try {
         // Rather than enqueuing just one interval 2008-01-01 to 2013-01-01, we
         // split that interval into random parts.  See the note on randomization
         // in FindRemoteWavesProcessor.
         log.info("Enqueueing find waves tasks");
         findProcessor.get().enqueueRandomTasksForInterval(instance,
             DaysSinceEpoch.fromYMD(2008, 1, 1),
             DaysSinceEpoch.fromYMD(2013, 1, 1));
       } catch (PermanentFailure e) {
         throw new RuntimeException("PermanentFailure writing initial tasks", e);
       }
     } else if ("importwave".equals(action)) {
       SourceInstance instance =
           sourceInstanceFactory.parseUnchecked(requireParameter(req, "instance"));
       WaveId waveId = WaveId.deserialise(requireParameter(req, "waveid"));
       ImportWaveTask task = new ImportWaveTaskGsonImpl();
       task.setInstance(instance.serialize());
       task.setWaveId(waveId.serialise());
       final ImportTaskPayload payload = new ImportTaskPayloadGsonImpl();
       payload.setImportWaveTask(task);
       log.info("Enqueueing task import task for " + waveId);
       try {
         new RetryHelper().run(new RetryHelper.VoidBody() {
           @Override public void run() throws RetryableFailure, PermanentFailure {
             CheckedTransaction tx = datastore.get().beginTransaction();
             try {
               perUserTable.get().addTask(tx, userId, payload);
               tx.commit();
             } finally {
               tx.close();
             }
           }
         });
       } catch (PermanentFailure e) {
         throw new IOException("Failed to enqueue import task", e);
       }
     } else if ("canceltasks".equals(action)) {
       log.info("Cancelling all tasks for " + userId);
       try {
         new RetryHelper().run(new RetryHelper.VoidBody() {
           @Override public void run() throws RetryableFailure, PermanentFailure {
             CheckedTransaction tx = datastore.get().beginTransaction();
             try {
               if (perUserTable.get().deleteAllTasks(tx, userId)) {
                 tx.commit();
               }
             } finally {
               tx.close();
             }
           }
         });
       } catch (PermanentFailure e) {
         throw new IOException("Failed to delete tasks", e);
       }
     } else if ("forgetwaves".equals(action)) {
       log.info("Forgetting all waves for " + userId);
       try {
         new RetryHelper().run(new RetryHelper.VoidBody() {
           @Override public void run() throws RetryableFailure, PermanentFailure {
             CheckedTransaction tx = datastore.get().beginTransaction();
             try {
               if (perUserTable.get().deleteAllWaves(tx, userId)) {
                 tx.commit();
               }
             } finally {
               tx.close();
             }
           }
         });
       } catch (PermanentFailure e) {
         throw new IOException("Failed to delete tasks", e);
       }
     } else {
       throw new BadRequestException("Unknown action: " + action);
     }
     // TODO(ohler): Send 303, not 302.  See
     // http://en.wikipedia.org/wiki/Post/Redirect/Get .
     resp.sendRedirect(req.getServletPath());
   }
 
 }
