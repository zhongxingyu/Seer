 package com.wtf.client;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.RpcRequestBuilderWN;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 import com.wtf.client.Poll.Answer;
 import com.wtf.client.dto.DiscussionDTO;
 import com.wtf.client.dto.PageDTO;
 import com.wtf.client.dto.PostDTO;
 import com.wtf.client.rpc.WTFService;
 import com.wtf.client.rpc.WTFServiceAsync;
 
 public class DiscussionsManager {
   private static boolean _fetched = false;
   private static boolean _fetching = false;
   private static boolean _icons_visible = false;
   private static Poll _poll = null;
   private static boolean _poll_fetching = false;
   private static HashSet<DiscussionPresenter> _discussions = new HashSet<DiscussionPresenter>();
   private static Integer[] _old_to_new = null;
 
   private static WTFServiceAsync wtfService = GWT.create(WTFService.class);
   private static String pageUrl = Window.Location.getHref();
   static {
     ServiceDefTarget sdt = (ServiceDefTarget) wtfService;
     sdt.setServiceEntryPoint("http://wtf-review.appspot.com/wtf/rpc");
     sdt.setRpcRequestBuilder(new RpcRequestBuilderWN(
         Config.getOptionString("dummy_url", "")));
   }
 
   public static void addDiscussion(DiscussionPresenter d) {
     _discussions.add(d);
   }
 
   //TODO: move to DiscussionPresenter
   public static void addPost(Discussion discussion, PostDTO post,
       final Command callback) {
     StatusBar.setStatus("Adding post...");
     Debug.log("Adding post...");
 
     wtfService.addPost(discussion.getKey(), post, new AsyncCallback<Boolean>() {
       @Override
       public void onFailure(Throwable caught) {
       }
 
       @Override
       public void onSuccess(Boolean result) {
         // after adding do this:
         StatusBar.setStatus("Post added");
         Debug.log("Post added: " + result);;
         callback.execute();
       }
     });
 
   }
 
   //TODO: move to DiscussionPresenter
   public static void createDiscussion(final Discussion discussion,
       final Command callback) {
     StatusBar.setStatus("Creating discussion...");
     Debug.log("Creating discussion...");
     // create LineNumbres object
     LineNumbers line_numbers = DOMMagic.getLineNumbersFromSelection(discussion.getSelection());
 
     // debug
     // line_numbers.debug();
     
     Request r = wtfService.createDiscussion(pageUrl, line_numbers,
         new AsyncCallback<String>() {
       @Override
       public void onFailure(Throwable caught) {
         Debug.log("Creating discussion fail: " + caught.getMessage());
         StatusBar.setStatus("Creating discussion fail: "
             + caught.getMessage());
       }
 
       @Override
       public void onSuccess(String key) {
         // after creating do this:
         discussion.setKey(key);
         StatusBar.setStatus("Discussion created");
         Debug.log("Discussion created");
         
         wtfService.updateContent(pageUrl, DOMMagic.getRowFormat(), new AsyncCallback<Boolean>() {
           @Override
           public void onFailure(Throwable caught) {
             Debug.log("Page content update fail: " + caught.getMessage());
           }
 
           @Override
           public void onSuccess(Boolean result) {
             Debug.log("Page content updated? " + result);
             callback.execute();
           }
         });
       }
     });
   }
 
   //TODO: move to DiscussionPresenter
   public static void fetchDiscussionDetails(final Discussion discussion,
       final Command callback) {
     // do we want to fetch every time discussion is viewed??
     if (discussion.isFetched()) {
       callback.execute();
       return;
     }
 
     if (discussion.isFetching())
       return;
     discussion.setFetching(true);
 
     StatusBar.setStatus("Fetching details...");
     Debug.log("Fetching details...");
 
     // TODO (peper): to z RPC: ankieta z wynikami i tresc dyskusji
     List<Answer> answers = new LinkedList<Answer>();
 
     // simulator
     answers.add(new Answer("OK", "a1", "wtf_poll_green", 23));
     answers.add(new Answer("NIEJASNE", "a2", "wtf_poll_gray", 3));
     answers.add(new Answer("BLAD", "a3", "wtf_poll_red", 56));
     final Poll poll = new Poll(answers);
 
     final List<PostDTO> thread = new LinkedList<PostDTO>();
 
     wtfService.getPosts(discussion.getKey(),
         new AsyncCallback<List<PostDTO>>() {
       @Override
       public void onFailure(Throwable caught) {
         StatusBar.setStatus("Fetching details failed: "
             + caught.getMessage());
       }
 
       @Override
       public void onSuccess(List<PostDTO> posts) {
         thread.addAll(posts);
 
         // after fetching do this:
         discussion.setPoll(poll);
         discussion.setThread(thread);
 
         StatusBar.setStatus("Details fetched");
         discussion.setFetching(false);
         discussion.setFetched(true);
         callback.execute();
       }
     });
   }
 
   public static void fetchDiscussionsList(final Command callback) {
     if (_fetching || _fetched) {
       return;
     }
     _fetching = true;
     StatusBar.setStatus("Fetching content...");
     
     StatusBar.setStatus("Fetching discussions...");
     Debug.log("Fetching discussions...");
 
     wtfService.getPage(pageUrl, new AsyncCallback<PageDTO>() {
       @Override
       public void onFailure(Throwable caught) {
         StatusBar.setStatus("Fetching discussions fail:"
             + caught.getMessage());
       }
 
       @Override
       public void onSuccess(final PageDTO p) {
         if (p == null) {
           _fetching = false;
           _fetched = true;
           callback.execute();
           return;
         }
         
        // String ptmp = p.getContent().replace("<", "&lt;");
         //ptmp = ptmp.replace(">", "&gt;");
        // Debug.log("Fetching content win: '" + ptmp + "'");
         Debug.log("Fetching " + p.getDiscussions().size() + " discussions win");
 
         final Command add_discussions = new Command() {
           @Override
           public void execute() {
             for (DiscussionDTO d : p.getDiscussions()) {
               LineNumbers lines = d.getLines();
               if (_old_to_new != null) {
                 lines = updateLines(lines);
                 if (lines == null)
                   continue;
               }
 
               Selection sel = DOMMagic.getSelectionFromLineNumbers(lines);
               if (sel != null) {
                 Discussion dis = new Discussion(sel, d.getPostsCount());
                 dis.setKey(d.getKey());
                 _discussions.add(new DiscussionPresenter(dis, null));
               }
             }
             updateLineNumbers(new Command() {
               public void execute() {
                 StatusBar.setStatus("DOM changes submited");
               }
             });
             _fetching = false;
             _fetched = true;
             callback.execute();
           }
         };
 
         Command update_and_add = new Command() {
           public void execute() {
             DiffManager._old_string = p.getContent();
             DiffManager.computeDiff(DOMMagic.getRowFormat(),
                 //this will execute if row_formats differ
                 new Command() {
               public void execute() { 
                 _old_to_new = DiffManager.getOldToNew();
                 Debug.log("DIFFER");
                 add_discussions.execute();
               }
             },
             //this will execute if row_formats do not differ
             new Command() {
               public void execute() { 
                 add_discussions.execute();
                 Debug.log("DO NOT DIFFER");
               }
             });
           }
         };
 
         // DOMMagic must be computed before computing diff and applying fetched discussions
         if (DOMMagic.isComputed()) {
           update_and_add.execute();
         } else {
           DOMMagic.requestComputingRowFormat();
           DeferredCommand.addCommand(update_and_add);
         }
       }
     });
   }
 
   public static void updateLineNumbers(Command callback) {
     wtfService.updateContent(pageUrl, DOMMagic.getRowFormat(),
         new AsyncCallback<Boolean>() {
           @Override
           public void onFailure(Throwable caught) {
               Debug.log("Updating page content fail: " + caught.getMessage());
           }
 
           @Override
           public void onSuccess(Boolean result) {
               Debug.log("Updating page content win? " + result);
           }
     });
     
     for (DiscussionPresenter d : _discussions) {
       wtfService.updateLineNumbers(d.getDiscussion().getKey(),
           DOMMagic.getLineNumbersFromSelection(d.getSelection()),
           new AsyncCallback<Boolean>() {
             @Override
             public void onFailure(Throwable caught) {
               Debug.log("Updating lines fail: " + caught.getMessage());
             }
 
             @Override
             public void onSuccess(Boolean result) {
               Debug.log("Updating lines win? " + result);
             }
       });
     }
 
     //after update:
     callback.execute();
   }
 
   public static void fetchPollInfo(Command callback) {
     if (_poll_fetching || _poll != null) {
       callback.execute();
       return;
     }
     _poll_fetching = true;
     StatusBar.setStatus("Fetching poll...");
     Debug.log("Fetching poll...");
 
     // TODO (peper): to z RPC: zbior odpowiedzi (same dane potrzebne do
     // wyswietlenia - bez wynikow)
     List<Answer> answers = new LinkedList<Answer>();
 
     // simulator
     answers.add(new Answer("OK", "a1", "wtf_poll_green"));
     answers.add(new Answer("NIEJASNE", "a2", "wtf_poll_gray"));
     answers.add(new Answer("BLAD", "a3", "wtf_poll_red"));
 
     _poll = new Poll(answers);
     // after creating do this:
     StatusBar.setStatus("Poll fetched");
     _poll_fetching = false;
     callback.execute();
   }
 
   public static Poll getNewPoll() {
     return _poll;
   }
 
   public static void init() {
     Window.addResizeHandler(new ResizeHandler() {
       public void onResize(ResizeEvent event) {
         DiscussionsManager.redrawIcons();
         DiscussionsManager.redrawDiscussions();
       }
     });
   }
 
   public static void redrawDiscussions() {
     for (DiscussionPresenter d : _discussions) {
       d.reposition();
     }
   }
 
   public static void redrawIcons() {
     if (_icons_visible) {
       removeIcons();
       showIcons();
     }
   }
 
   public static void removeIcons() { //TODO: troche za duzo odwolan?
     if (!_fetched)
       return;
     for (DiscussionPresenter d : _discussions) {
       d.removeIcon();
       d.hide();
     }
     _icons_visible = false;
     StatusBar.setDiscussionMode(false);
   }
 
   public static void showIcons() {
     StatusBar.setDiscussionMode(true);
     final Command cmd = new Command() {
       public void execute() {
         if (!StatusBar.isDiscussionMode())
           return;
         for (DiscussionPresenter d : _discussions) {
           d.showIcon();
         }
         _icons_visible = true;
       }
     };
     if (!_fetched) {
       // Fetch Discussions (it triggers computing Row Format)
       fetchDiscussionsList(new Command() {
         public void execute() {
           cmd.execute();
         }
       });
     } else {
       cmd.execute();
     }
   }
 
   public static LineNumbers updateLines(LineNumbers old) {
     if(_old_to_new == null)
       return old; 
     Debug.log("update discussion...");
 
     HashMap<TagLines, TagLines> tmp = new HashMap<TagLines, TagLines>();
     LineNumbers updated = new LineNumbers();
     HashSet<TagLines> elems = old.getElements();
     for(TagLines tag : elems) {
       TagLines ntag = updateTag(tag);
       if(ntag == null)
         continue;
       updated.addElement(ntag);
       tmp.put(tag, ntag);
     }
 
     HashSet<WordsLines> next_level = old.getNextLevelWords();
     for(WordsLines wl : next_level) {
       TagLines ntag = tmp.get(wl.getParentTag());
      if(ntag== null) {
        Debug.log("Error in updateLines: Incorrect next level description");
        return null;
       }
       
       HashSet<Integer> lines = wl.getLines();
       HashSet<Integer> updated_lines = new HashSet<Integer>();
       for(int line : lines) {
         int with_open_tag = wl.getParentTag().getOpenLine() + line + 1;
         //Debug.log("line: " + line + " with_open_tag: " + with_open_tag);
         if(_old_to_new[with_open_tag] != -1) {
           updated_lines.add(_old_to_new[with_open_tag] - ntag.getOpenLine() - 1);     
         } else {
           Debug.log("diff: word is missing");
         }
       }
       
       updated.addNextLevelWords(ntag, updated_lines);
     }
     return updated;
   }
 
   private static TagLines updateTag(TagLines tag) {
     if(_old_to_new[tag.getOpenLine()] == -1 && _old_to_new[tag.getCloseLine()] == -1) {
       //tag is missing
       //TODO: do something useful
       Debug.log("diff: tag is missing");
       return null;
     } else {
       int open = _old_to_new[tag.getOpenLine()];
       int close = _old_to_new[tag.getCloseLine()];
       //Debug.log("open: " + tag.getOpenLine() + " nopen: " + open);
       //Debug.log("close: " + tag.getCloseLine() + " nclose: " + close);
       return new TagLines(open != -1 ? open : tag.getOpenLine(), 
           close != -1 ? close : tag.getCloseLine());
     }
   }
 }
