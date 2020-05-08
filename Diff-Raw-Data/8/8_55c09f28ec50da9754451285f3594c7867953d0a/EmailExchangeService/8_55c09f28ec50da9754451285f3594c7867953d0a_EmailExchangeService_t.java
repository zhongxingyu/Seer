 package fr.keemto.provider.exchange.importer;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import fr.keemto.core.fetching.FetchingException;
 import microsoft.exchange.webservices.data.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 
 public class EmailExchangeService implements Enumeration<List<EmailMessage>> {
 
     private static final Logger log = LoggerFactory.getLogger(EmailExchangeService.class);
 
     private static final int PAGE_OFFSET = 100;

    private static final long MINIMAL_MILLIS_BETWEEN_TO_MAIL = 1000L;
 
     private final ExchangeService exchangeService;
     private final ItemView offsetView;
     private final SearchFilter.IsGreaterThan filter;
     private PartialEmailList currentPartialEmailList;
 
     public EmailExchangeService(long emailNewerThan, ExchangeService exchangeService) {
         Date greaterThan = new Date(emailNewerThan + MINIMAL_MILLIS_BETWEEN_TO_MAIL);
         filter = new SearchFilter.IsGreaterThan(EmailMessageSchema.DateTimeCreated, greaterThan);
         offsetView = new ItemView(PAGE_OFFSET);
         this.exchangeService = exchangeService;
     }
 
     @Override
     public boolean hasMoreElements() {
         if (currentPartialEmailList == null) return true;
         return currentPartialEmailList.hasMoreItems;
     }
 
     @Override
     public List<EmailMessage> nextElement() {
 
         log.debug("Retrieving email ids with view {}", offsetView);
         try {
             FindItemsResults<Item> itemsResults = exchangeService.findItems(WellKnownFolderName.Inbox, filter, offsetView);
             int leftItems = itemsResults.getTotalCount() - offsetView.getOffset();
             log.debug("{} items ids has been retrieved, {} items left on remote server.", itemsResults.getItems().size(), leftItems);
             List<ItemId> ids = extractItemIdsFrom(itemsResults);
             currentPartialEmailList = new PartialEmailList(ids, itemsResults.isMoreAvailable(), exchangeService);
             incrementOffset();
             return currentPartialEmailList.getFullBindedMessages();
 
         } catch (Exception e) {
             throw new ExchangeServiceException("An error has occurred when trying to retrieve messages from exchange WS service " + exchangeService.getUrl(), e);
         }
     }
 
     private void incrementOffset() {
         offsetView.setOffset(offsetView.getOffset() + PAGE_OFFSET);
     }
 
     private List<ItemId> extractItemIdsFrom(FindItemsResults<Item> itemsResults) {
         return Lists.transform(itemsResults.getItems(), new Function<Item, ItemId>() {
             @Override
             public ItemId apply(Item item) {
                 try {
                     return item.getId();
                 } catch (ServiceLocalException e) {
                     throw new FetchingException(e);
                 }
             }
         });
     }
 
     private static class PartialEmailList {
         private final boolean hasMoreItems;
         private final List<ItemId> ids;
         private final ExchangeService exchangeService;
 
 
         private PartialEmailList(List<ItemId> ids, boolean hasMoreItems, ExchangeService exchangeService) {
             this.ids = ids;
             this.hasMoreItems = hasMoreItems;
             this.exchangeService = exchangeService;
         }
 
         public List<EmailMessage> getFullBindedMessages() throws Exception {

            if (ids.isEmpty()) return new ArrayList<EmailMessage>();//bindToItems does not handle empty ids list

             ServiceResponseCollection<GetItemResponse> response = exchangeService.bindToItems(ids, getPropertiesToLoadForEmailMessage());
             log.debug("email message data has been bounded remotely for {} items", ids.size());
             return convertResponseToEmailList(response);
         }
 
         private PropertySet getPropertiesToLoadForEmailMessage() throws Exception {
             PropertySet ps = new PropertySet();
             ps.add(EmailMessageSchema.Id);
             ps.add(EmailMessageSchema.DateTimeCreated);
             ps.add(EmailMessageSchema.Sender);
             ps.add(EmailMessageSchema.ToRecipients);
             ps.add(EmailMessageSchema.Subject);
             ps.add(EmailMessageSchema.Body);
             return ps;
         }
 
         private List<EmailMessage> convertResponseToEmailList(ServiceResponseCollection<GetItemResponse> bindedItems) {
             List<EmailMessage> messages = new ArrayList<EmailMessage>();
             for (GetItemResponse bindedItem : bindedItems) {
                 EmailMessage message = (EmailMessage) bindedItem.getItem();
                 messages.add(message);
             }
             return messages;
         }
 
     }
 }
