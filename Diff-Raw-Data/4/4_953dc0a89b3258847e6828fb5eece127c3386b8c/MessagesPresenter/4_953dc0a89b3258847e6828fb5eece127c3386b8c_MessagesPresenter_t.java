 package org.iplantc.de.client.sysmsgs.presenter;
 
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.sysmsgs.cache.SystemMessageCache;
 import org.iplantc.de.client.sysmsgs.events.MessagesUpdatedEvent;
 import org.iplantc.de.client.sysmsgs.model.Message;
 import org.iplantc.de.client.sysmsgs.view.MessagesView;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.loader.ListLoadResult;
 
 /**
  * The system messages presenter.
  */
 public final class MessagesPresenter implements MessagesView.Presenter<Message> {
 
     interface MessageProperties extends MessagesView.MessageProperties<Message> {
     }
 
     private static final MessageProperties MSG_PROPS = GWT.create(MessageProperties.class);
     private static final MessagesView.Factory<Message> VIEW_FACTORY = GWT.create(MessagesView.Factory.class);
     
     private final MessagesView<Message> view = VIEW_FACTORY.make(this, MSG_PROPS, new ActivationTimeRenderer());
 
     private HandlerRegistration updateHandlerReg = null;
 	
     /**
      * @see MessageView.Presenter<T>#handleDismissMessage(T)
      */
     @Override
     public void handleDismissMessage(final Message message) {
         if (SystemMessageCache.instance().hasMessage(message)) {
             view.verifyMessageDismissal(new Command() {
                 @Override
                 public void execute() {
                     dismissMessage(message);
                 }
             });
         }
     }
 
     /**
      * @see MessageView.Presenter<T>#handleSelectMessage(T)
      */
     @Override
     public void handleSelectMessage(final Message message) {
         if (SystemMessageCache.instance().hasMessage(message)) {
             view.getSelectionModel().select(false, message);
             showBodyOf(message);
             showExpiryOf(message);
             markSeen(message);
         }
     }
 
     /**
      * Starts the presenter and attaches the view to the provided container. This also starts the
      * message caching.
      * 
      * @param container The container that will hold the view.
      */
 	public void go(final AcceptsOneWidget container) {
         if (container == null) {
             stop();
         } else {
             SystemMessageCache.instance().startSyncing();
             updateStoreAsync();
             if (updateHandlerReg == null) {
                 updateHandlerReg = EventBus.getInstance().addHandler(MessagesUpdatedEvent.TYPE, new MessagesUpdatedEvent.Handler() {
                     @Override
                     public void onUpdate(final MessagesUpdatedEvent event) {
                         updateStoreAsync();
                     }
                 });
             }
             container.setWidget(view);
             view.showLoading();
         }
 	}
 	
     /**
      * This should be called when the container holding the view has been closed. It stops the
      * message caching.
      */
     public void stop() {
         if (updateHandlerReg != null) {
             updateHandlerReg.removeHandler();
         }
         SystemMessageCache.instance().stopSyncing();
     }
 
     private void dismissMessage(final Message message) {
         // TODO externalize message
         view.mask("dismissing message");
         SystemMessageCache.instance().dismissMessage(message, new Callback<Void, Throwable>() {
             @Override
             public void onFailure(final Throwable reason) {
                 // FIXME handle failure
                 Window.alert(reason.getMessage());
                 view.unmask();
             }
             @Override
             public void onSuccess(Void unused) {
                 removeMessage(message);
                 view.unmask();
             }
         });
     }
 
     private void markSeen(final Message message) {
         SystemMessageCache.instance().markSeen(message, new Callback<Void, Throwable>() {
             @Override
             public void onFailure(final Throwable reason) {
                 // TODO Figure out how to handle this
                 Window.alert(reason.getMessage());
             }
             @Override
             public void onSuccess(Void unused) {
                 view.getMessageStore().update(message);
             }
         });
     }
 
     private void showBodyOf(final Message message) {
         final SafeHtmlBuilder bodyBuilder = new SafeHtmlBuilder();
         bodyBuilder.appendHtmlConstant(message.getBody());
         view.setMessageBody(bodyBuilder.toSafeHtml());
     }
 
     private void showExpiryOf(final Message message) {
         final DateTimeFormat expiryFmt = DateTimeFormat.getFormat("dd MMMM yyyy");
         final String expiryStr = expiryFmt.format(message.getDeactivationTime());
         view.setExpiryMessage(I18N.DISPLAY.expirationMessage(expiryStr));
     }
 
 	private void updateStoreAsync() {
 		SystemMessageCache.instance().load(null, 
 				new Callback<ListLoadResult<Message>, Throwable>() {
 					@Override
 					public void onFailure(final Throwable reason) {
 						// TODO implement
 						Window.alert("Failed to retrieve messages");
 					}
 					@Override
 					public void onSuccess(final ListLoadResult<Message> result) {
 						updateStore(result.getData());
 					}});
 	}
 	
 	private void updateStore(final List<Message> updatedMessages) {
         final Message curSelect = view.getSelectionModel().getSelectedItem();
         final ListStore<Message> store = view.getMessageStore();
         store.replaceAll(updatedMessages);
         if (curSelect != null && store.findModel(curSelect) == null) {
             store.add(curSelect);
 		}
         if (store.size() > 0) {
 			if (curSelect != null) {
                 showMessageSelected(store.indexOf(curSelect));
 			} else {
 				showMessageSelected(0);
 			}
 		} else {
             view.showNoMessages();
 		}
 	}
 
 	private void removeMessage(final Message message) {
        final int idx = view.getMessageStore().indexOf(message);
         view.getMessageStore().remove(message);
         if (view.getMessageStore().size() <= 0) {
             view.showNoMessages();
        } else {
            showMessageSelected(view.getMessageStore().size() <= idx ? idx - 1 : idx);
 		}
 	}
 	
 	private void showMessageSelected(final int index) {
         view.showMessages();
         view.getSelectionModel().select(index, false);
 	}
 
 }
