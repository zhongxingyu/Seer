 package com.williamsinteractive.casino.wager.core;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.util.concurrent.SettableFuture;
 import com.google.common.util.concurrent.Uninterruptibles;
 import com.williamsinteractive.casino.wager.model.ExchangeRate;
 import com.williamsinteractive.casino.wager.model.Game;
 import com.williamsinteractive.casino.wager.model.Id;
 import com.williamsinteractive.casino.wager.model.Wager;
 import com.williamsinteractive.casino.wager.model.WagerRound;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Timer;
 import com.yammer.metrics.core.TimerContext;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.voltdb.VoltTable;
 import org.voltdb.client.Client;
 import org.voltdb.client.ClientResponse;
 import org.voltdb.client.ProcedureCallback;
 
 import javax.inject.Inject;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import static com.google.common.base.Preconditions.checkState;
 import static java.util.concurrent.TimeUnit.MICROSECONDS;
 
 /**
  * TODO: document!
  *
  * @author Petter Måhlén
  */
 public class VoltWagerRoundStateStore implements WagerRoundStateStore {
     private static final Logger LOGGER = LoggerFactory.getLogger(VoltWagerRoundStateStore.class);
     private static final Timer RECORD_WAGER_TIMER = Metrics.newTimer(VoltWagerRoundStateStore.class, "recordWager");
     private static final Timer CONFIRM_WAGER_TIMER = Metrics.newTimer(VoltWagerRoundStateStore.class, "confirmWager");
     private static final Timer RECORD_OUTCOME_TIMER = Metrics.newTimer(VoltWagerRoundStateStore.class, "recordOutcome");
     private static final Timer CONFIRM_OUTCOME_TIMER = Metrics.newTimer(VoltWagerRoundStateStore.class, "confirmOutcome");
 
     private final Client client;
 
     @Inject
     public VoltWagerRoundStateStore(Client client) {
         this.client = client;
     }
 
     @Override
     public void recordWager(Id<WagerRound> wagerRoundId,
                             Id<Wager> wagerId,
                             long wagerAmount,
                             Id<Game> gameId,
                             Id<ExchangeRate> exchangeRateId) {
         TimerContext context = RECORD_WAGER_TIMER.time();
 
         try {
             LOGGER.debug("Recording: {} {} {} {} {}", wagerRoundId, wagerId, wagerAmount, gameId, exchangeRateId);
             final SettableFuture<Boolean> resultFuture = callVolt("RecordWager", wagerRoundId.id(), wagerId.id(), wagerAmount, gameId.id(), exchangeRateId.id());
 
             verifySuccess(resultFuture);
         }
         finally {
             context.stop();
         }
     }
 
     @Override
     public void confirmWager(Id<WagerRound> wagerRoundId, Id<Wager> wagerId) {
         TimerContext context = CONFIRM_WAGER_TIMER.time();
 
         try {
             LOGGER.debug("Confirming: {} {} {} {} {}", wagerRoundId, wagerId);
             final SettableFuture<Boolean> resultFuture = callVolt("ConfirmWager", wagerRoundId.id(), wagerId.id(), 0, 0, 0);
 
             verifySuccess(resultFuture);
         }
         finally {
             context.stop();
         }
     }
 
     @Override
     public void recordOutcome(Id<WagerRound> wagerRoundId, long winAmount) {
         TimerContext context = RECORD_OUTCOME_TIMER.time();
 
         try {
             LOGGER.debug("Recording outcome: {} {} {} {} {}", wagerRoundId, winAmount);
             final SettableFuture<Boolean> resultFuture = callVolt("RecordOutcome", wagerRoundId.id(), winAmount);
 
             verifySuccess(resultFuture);
         }
         finally {
             context.stop();
         }
     }
 
     @Override
     public CompletedWagerRound confirmOutcome(Id<WagerRound> wagerRoundId, long winAmount) {
         TimerContext context = CONFIRM_OUTCOME_TIMER.time();
 
         try {
             LOGGER.debug("Confirming outcome: {} {}", wagerRoundId, winAmount);
 
             SettableFuture<CompletedWagerRound> responseFuture = SettableFuture.create();
 
             client.callProcedure(new ConfirmOutcomeCallback(responseFuture), "ConfirmOutcome", wagerRoundId.id(), winAmount);
 
             return Uninterruptibles.getUninterruptibly(responseFuture);
         }
         catch (IOException | ExecutionException e) {
             throw new RuntimeException(e);
         }
         finally {
             context.stop();
         }
    }
 
     @Override
     public void recordArchival(Id<WagerRound> wagerRoundId) {
         throw new UnsupportedOperationException();
     }
 
     private SettableFuture<Boolean> callVolt(String procedureName, Object... args) {
         final SettableFuture<Boolean> resultFuture = SettableFuture.create();
 
         try {
             client.callProcedure(new WageRoundTransitionCallback(resultFuture), procedureName, args);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
         return resultFuture;
     }
 
     private void verifySuccess(Future<Boolean> resultFuture) {
         try {
             // ignoring the result - errors will be communicated via ExecutionExceptions
             Uninterruptibles.getUninterruptibly(resultFuture);
         }
         catch (ExecutionException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     private static class WageRoundTransitionCallback implements ProcedureCallback {
         private final SettableFuture<Boolean> resultFuture;
 
         public WageRoundTransitionCallback(SettableFuture<Boolean> resultFuture) {
             this.resultFuture = resultFuture;
         }
 
         @Override
         public void clientCallback(ClientResponse clientResponse) throws Exception {
             if (isError(clientResponse)) {
                resultFuture.setException(new RuntimeException("Procedure call failed: " + clientResponse.getStatusString()));
             } else if (isFailure(clientResponse)) {
                resultFuture.setException(new RuntimeException("Validation failure: " + clientResponse.getAppStatusString()));
             } else {
                 resultFuture.set(true);
             }
         }
     }
 
     private static class ConfirmOutcomeCallback implements ProcedureCallback {
         private final SettableFuture<CompletedWagerRound> responseFuture;
 
         public ConfirmOutcomeCallback(SettableFuture<CompletedWagerRound> responseFuture) {
             this.responseFuture = responseFuture;
         }
 
         @Override
         public void clientCallback(ClientResponse clientResponse) throws Exception {
             if (isError(clientResponse)) {
                 throw new Exception("Call failed: " + clientResponse.getStatusString());
             } else if (isFailure(clientResponse)) {
                 throw new RuntimeException("Validation failure: " + clientResponse.getAppStatusString());
             }
 
             CompletedWagerRound result = extractWagerRoundData(clientResponse.getResults());
 
             responseFuture.set(result);
         }
 
         private CompletedWagerRound extractWagerRoundData(VoltTable[] results) {
             List<CompletedWager> completedWagers = extractWagers(results[0]);
 
             VoltTable wagerRound = results[1];
 
             wagerRound.advanceRow();
 
             Id<WagerRound> wagerRoundId = Id.of(wagerRound.getLong("wager_round_id"));
             Id<Game> gameId = Id.of(wagerRound.getLong("game_id"));
             Id<ExchangeRate> exchangeRateId = Id.of(wagerRound.getLong("exchange_rate_id"));
             long winAmount = wagerRound.getLong("outcome_amount");
 
             return new CompletedWagerRound(wagerRoundId, completedWagers, gameId, exchangeRateId, winAmount);
         }
 
         private List<CompletedWager> extractWagers(VoltTable result) {
             ImmutableList.Builder<CompletedWager> builder = ImmutableList.builder();
 
             // we should always have valid transitions, ordered properly - the RecordOutcome stored procedure
             // will fail otherwise. So a little bit lenient on the error checking here.
             while (result.advanceRow()) {
                 // first row: get base data and wager request timestamp
                 Id<Wager> wagerId = Id.of(result.getLong("wager_id"));
                 long wagerAmount = result.getLong("amount");
 
                 checkState(result.getLong("state") == 1, "expected a row in REQUEST_MONEY state, got %d", result.getLong("state"));
 
                 DateTime requestDate = new DateTime(MICROSECONDS.toMillis(result.getTimestampAsLong("created")));
 
                 // second row: get wager confirmation timestamp
                 result.advanceRow();
 
                 checkState(result.getLong("wager_id") == wagerId.id(), "expected a row for wager id %s, got wager id %d", wagerId, result.getLong("wager_id"));
                 checkState(result.getLong("state") == 2, "expected a row in GOT_MONEY state, got %d", result.getLong("state"));
 
                 DateTime confirmDate = new DateTime(MICROSECONDS.toMillis(result.getTimestampAsLong("created")));
 
                 builder.add(new CompletedWager(wagerId, wagerAmount, requestDate, confirmDate));
             }
 
             return builder.build();
         }
     }
 
     private static boolean isError(ClientResponse clientResponse) {
         return clientResponse.getStatus() != ClientResponse.SUCCESS;
     }
 
     private static boolean isFailure(ClientResponse clientResponse) {
         return clientResponse.getAppStatus() != (byte) 1; // TODO: should get that constant from somewhere..
     }
 }
