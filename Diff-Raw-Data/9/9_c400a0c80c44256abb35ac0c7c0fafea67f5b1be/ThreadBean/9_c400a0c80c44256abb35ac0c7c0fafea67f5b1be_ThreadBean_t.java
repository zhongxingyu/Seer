 package beans;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import models.ReplyModel;
 import models.ThreadModel;
 
 @ManagedBean
 @SessionScoped
 public class ThreadBean {
 	private String title, content;
 	
 	public ThreadModel getThread() throws Throwable {
 		Map<String, String> request = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
 		Integer threadId = Integer.parseInt(request.get("threadId"));
 		
 		if(threadId == null) throw new Exception("Invalid Request");
 		
 		return new ThreadModel(threadId);
 	}
 	
 	public ArrayList<ReplyModel> getReplies() throws Throwable {
 		ThreadModel thread = getThread();
 		
 		ArrayList<ReplyModel> replies = thread.getReplies(0, 20);
 		if(true)  { // Nur auf erster Seite TODO !!
 			// Adding a "fake" reply representing the thread
 			ReplyModel reply = new ReplyModel();
 			reply.setContent(thread.getContent());
 			reply.setCreatedOn(thread.getCreatedOn());
 			reply.setThreadId(thread.getThreadId());
 			reply.setUserId(thread.getUserId());
 			replies.add(0, reply);
 		}
 		return replies;
 	}
 	
	public String save(Integer userId) throws Throwable {
 		ThreadModel thread = new ThreadModel();
 		
		int boardId = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("boardId"));
		
 		thread.setBoardId(boardId);
 		thread.setContent(content);
 		thread.setTitle(title);
 		thread.setUserId(userId);
 		
 		thread.save();
 		
		return String.format("board.xhtml?boardId=%d", boardId);
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 }
