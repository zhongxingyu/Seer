 package org.eclipse.ecf.services.quotes;
 
 //import org.eclipse.ecf.remoteservice.IAsyncCallback;
 //import org.eclipse.ecf.remoteservice.IAsyncRemoteServiceProxy;
//import org.eclipse.equinox.concurrent.future.IFuture;
 
 @SuppressWarnings("restriction")
 public interface QuoteServiceAsync /* extends IAsyncRemoteServiceProxy*/ {
 	
 	/**
 	 * IFuture invocation
 	 * 
 	 * @see org.eclipse.ecf.services.quotes.QuoteService#getAllQuotes()
 	 */
//	public IFuture getAllQuotesAsync();
 	
 	/**
 	 * Callback invocation
 	 * 
 	 * @see org.eclipse.ecf.services.quotes.QuoteService#getAllQuotes()
 	 */
 //	public void getAllQuotesAsync(IAsyncCallback<String[]> callback);
 }
