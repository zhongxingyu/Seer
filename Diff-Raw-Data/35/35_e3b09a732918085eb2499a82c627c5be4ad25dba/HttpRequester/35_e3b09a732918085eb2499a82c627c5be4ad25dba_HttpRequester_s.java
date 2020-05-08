 package httprequester;
 
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  *
  * @author Benjamin
  */
 public class HttpRequester implements HttpRequesterObservable
 {
    public static final String VERSION = "1.3";
     
     protected int requests, sleep, threads;
     protected String url;
     private boolean urlCheck = false;
     private boolean getData = false;
     private boolean getHeaders = false;
     private List<HttpRequesterObserver> observers;
     private int finishedThreads = 0, successfulRequests = 0;
     private NotifyObject notifyObject;
     
     public HttpRequester()
     {
         flush();
         observers = new LinkedList<>();
         notifyObject = new NotifyObject();
     }
 
     public final void flush()
     {
         this.requests = 10;
         this.sleep = 500;
         this.threads = 1;
         this.url = null;
         this.finishedThreads = 0;
         this.successfulRequests = 0;
     }
     
     public void start()
     {
         notifyObject.setMessage("Sending " + requests + " requests to " + url);
         notifyObject.setStatus(NotifyObject.STARTING);
         notifyObservers(notifyObject);
             
         if(!urlCheck || checkUrl())
         {
             int number = requests / threads;
             
             for(int i = 0; i < threads; i++)
             {
                 if(i == threads - 1) number += requests - (number * threads);
                 HttpRequesterThread hrt = new HttpRequesterThread(number);
                 hrt.setName("HttpRequesterThread " + i + " (" + number + ")");
                 hrt.start();
             }
         }
         else
         {
             notifyObject.setMessage("URL Check: your URL seems to be invalid.");
             notifyObject.setStatus(NotifyObject.ERROR);
             notifyObservers(notifyObject);
         }
     }
 
     public void setRequests(int requests)
     {
         if(requests < 0) sendMessage("Negative number of requests found, using absolute value.");
         this.requests = Math.abs(requests);
     }
 
     public void setSleep(int sleep)
     {
         if(sleep < 0) sendMessage("Negative sleep time found, using absolute value.");
         this.sleep = Math.abs(sleep);
     }
 
     public void setThreads(int threads)
     {
         if(threads < 0) sendMessage("Negative number of threads found, using absolute value.");
         this.threads = Math.abs(threads);
     }
 
     public void setUrl(String url)
     {
         this.url = cleanUrl(url);
     }
     
     public void setUrlCheck(boolean check)
     {
         this.urlCheck = check;
     }
     
     public void setGetHeaders(boolean headers)
     {
         this.getHeaders = headers;
     }
     
     public void setGetData(boolean data)
     {
         this.getData = data;
     }
     
     private String cleanUrl(String link)
     {
         if(link.length() <= 7 || (!link.substring(0, 7).equals("http://") && !link.substring(0, 8).equals("https://")))
         {
             StringBuilder sb = new StringBuilder();
             sb.append("http://");
             sb.append(link);
             link = sb.toString();
         }
         
         return link;
     }
     
     private boolean checkUrl()
     {
         try
         {  
             RequestSender rs = new RequestSender(url, false);
             rs.sendRequest();
             return true;
         }
         catch(MyException ex)
         {
             return false;
         }
     }
     
     private synchronized void augmentSuccessfulRequests()
     {
         successfulRequests++;
     }
     
     private synchronized void threadFinishedHandler()
     {
         finishedThreads++;
         if(finishedThreads == threads)
         {
             notifyObject.setMessage(requests + " requests sent, " + successfulRequests + " OK.");
             notifyObject.setStatus(NotifyObject.FINISHED);
             notifyObservers(notifyObject);
         }
     }
 
     private void sendMessage(String message)
     {
         notifyObject.setStatus(NotifyObject.MESSAGE);
         notifyObject.setMessage(message);
         notifyObservers(notifyObject);
     }
 
     @Override
     public synchronized void notifyObservers(NotifyObject arg)
     {
         for(HttpRequesterObserver o : observers)
         {
             o.update(arg);
         }
     }
 
     @Override
     public void addObserver(HttpRequesterObserver o)
     {
         observers.add(o);
     }
 
     @Override
     public void removeObserver(HttpRequesterObserver o)
     {
         observers.remove(o);
     }
     
     private class HttpRequesterThread extends Thread
     {
         private int number;
         private NotifyObject threadNotifyObject;
         
         public HttpRequesterThread(int number)
         {
             this.number = number;
             threadNotifyObject = new NotifyObject();
         }
         
         @Override
         public void run()
         {
             for(int i = 0; i < number; i++)
             {
                 try
                 {
                     RequestSender rs = new RequestSender(url, getData);
                     rs.sendRequest();
                     if(rs.isSuccess()) augmentSuccessfulRequests();
                     
                     threadNotifyObject.flush();
                     threadNotifyObject.setMessage(rs.getResponse());
                     threadNotifyObject.setStatus(NotifyObject.RESPONSE);
                     
                     if(getHeaders) threadNotifyObject.setHeaders(rs.getHeaders());
                     if(getData) threadNotifyObject.setData(rs.getData());
                     
                     notifyObservers(threadNotifyObject);
                     
                     if(i < number - 1) Thread.sleep(sleep);
                 }
                 catch (MyException | InterruptedException ex)
                 {
                     threadNotifyObject.setMessage(ex.getMessage());
                     threadNotifyObject.setStatus(NotifyObject.ERROR);
                     notifyObservers(threadNotifyObject);
                 }
             }
             
             threadFinishedHandler();
         }
     }
 }
