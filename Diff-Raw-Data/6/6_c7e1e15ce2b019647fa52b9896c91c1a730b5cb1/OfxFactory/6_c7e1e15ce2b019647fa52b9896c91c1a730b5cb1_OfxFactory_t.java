 package com.pawelniewiadomski.budget.utils;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 import com.pawelniewiadomski.budget.banks.TransactionTypeMapper;
 import net.sf.ofx4j.domain.data.ResponseEnvelope;
 import net.sf.ofx4j.domain.data.ResponseMessageSet;
 import net.sf.ofx4j.domain.data.banking.*;
 import net.sf.ofx4j.domain.data.common.*;
 import net.sf.ofx4j.domain.data.signon.SignonResponse;
 import net.sf.ofx4j.domain.data.signon.SignonResponseMessageSet;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.text.Normalizer;
 import java.util.*;
 
 /**
  * TODO: Document this class / interface here
  *
  * @since v5.0.1
  */
 public class OfxFactory {
 
     public static final Duration HALF_DAY = Duration.standardHours(12);
 
     public static Transaction createTransaction(DateTime opDate, DateTime acDate, String opDesc, double v, TransactionTypeMapper mapper) {
         // YNAB can't handle time zone properly so add enough hours that the imported date will still be on the transaction date
         Transaction t = new Transaction();
         t.setAmount(v);
        t.setDateInitiated(opDate.toDateTime().plus(HALF_DAY).toDate());
        t.setDatePosted(opDate.toDateTime().plus(HALF_DAY).toDate());
        t.setDateAvailable(acDate.toDateTime().plus(HALF_DAY).toDate());
         t.setTransactionType(TransactionType.DEBIT);
         t.setName(StringUtils.replace(opDesc, "\n", " "));
         t.setId(createId(t));
         return t;
     }
 
     @Nonnull
     public static String toAscii(@Nullable String input) {
         return Normalizer
                 .normalize(StringUtils.defaultString(input, ""), Normalizer.Form.NFD)
                 .replaceAll("[^\\p{ASCII}]", "");
     }
 
     private static String createId(Transaction t) {
         String toSign = "" + t.getAmount() + t.getDateInitiated() + t.getDatePosted() + t.getDateAvailable() + t.getTransactionType() + t.getName();
         return DigestUtils.sha256Hex(toSign);
     }
 
     public static BankAccountDetails createBankAccountDetails(@Nonnull String accountNumber, @Nonnull String bankId) {
         BankAccountDetails account = new BankAccountDetails();
         account.setAccountNumber(accountNumber);
         account.setAccountType(AccountType.CHECKING);
         account.setBankId(bankId);
         return account;
     }
 
     public static TransactionList createTransactionList(List<Transaction> transactions) {
         Date startDate = Ordering.natural().min(Iterables.transform(transactions, new Function<Transaction, Date>() {
             @Override
             public Date apply(@Nullable Transaction from) {
                 return from.getDateInitiated();
             }
         }));
         Date endDate = Ordering.natural().max(Iterables.transform(transactions, new Function<Transaction, Date>() {
             @Override
             public Date apply(@Nullable Transaction from) {
                 return from.getDateAvailable();
             }
         }));
 
         TransactionList tl = new TransactionList();
         tl.setEnd(endDate);
         tl.setStart(startDate);
         tl.setTransactions(transactions);
         return tl;
     }
 
     public static Status okStatus() {
         Status s = new Status();
         s.setCode(Status.KnownCode.SUCCESS);
         return s;
     }
 
     @Nonnull
     public static SignonResponseMessageSet createSignonResponse() {
         final SignonResponseMessageSet srms = new SignonResponseMessageSet();
         final SignonResponse signonResponse = new SignonResponse();
         signonResponse.setLanguage("Polish");
         final Status statusOk = new Status();
         statusOk.setCode(Status.KnownCode.SUCCESS);
         signonResponse.setStatus(okStatus());
         signonResponse.setTimestamp(new Date());
         srms.setSignonResponse(signonResponse);
         return srms;
     }
 
     @Nonnull
     public static BankStatementResponseTransaction createBankStatementResponseTransaction(@Nonnull BankStatementResponse response) {
         BankStatementResponseTransaction responseTransaction = new BankStatementResponseTransaction();
         responseTransaction.setMessage(response);
         responseTransaction.setStatus(OfxFactory.okStatus());
         responseTransaction.setUID(UUID.randomUUID().toString());
         return responseTransaction;
     }
 
     public static ResponseEnvelope createResponseEnvelope(List<BankStatementResponseTransaction> responses) {
         BankingResponseMessageSet messageSet = new BankingResponseMessageSet();
         messageSet.setStatementResponses(responses);
 
         ResponseEnvelope envelope = new ResponseEnvelope();
         envelope.setMessageSets(ImmutableSortedSet.<ResponseMessageSet>of(createSignonResponse(), messageSet));
         return envelope;
     }
 }
