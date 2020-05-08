 package org.smartsnip.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.smartsnip.core.Category;
 import org.smartsnip.core.Code;
 import org.smartsnip.core.Comment;
 import org.smartsnip.core.Snippet;
 import org.smartsnip.core.Tag;
 import org.smartsnip.core.User;
 import org.smartsnip.shared.ISnippet;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XComment;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.rpc.client.impl.RemoteException;
 
 public class ISnippetImpl extends GWTSessionServlet implements ISnippet {
 
 	/** Serialisation ID */
 	private static final long serialVersionUID = -1493947420774219096L;
 
 	@Override
 	public List<XComment> getComments(long snippet, int start, int count) throws NotFoundException {
 
 		Snippet snip = Snippet.getSnippet(snippet);
 		if (snip == null)
 			throw new NotFoundException("Snippet with id " + snippet + " not found");
 
 		List<Comment> comments = snip.getComments();
 		List<XComment> result = new ArrayList<XComment>(comments.size());
 
 		Session session = getSession();
 		for (Comment obj : comments)
 			result.add(ICommentImpl.toXComment(obj, session));
 
 		return result;
 	}
 
 	@Override
 	public int getCommentCount(long hash) {
 		Snippet snippet = Snippet.getSnippet(hash);
 		if (snippet == null)
 			return 0;
 
 		return snippet.getCommentCount();
 	}
 
 	@Override
 	public XSnippet getSnippet(long hash) {
 		Snippet snippet = Snippet.getSnippet(hash);
 		if (snippet == null)
 			return null;
 
 		return toXSnippet(snippet);
 	}
 
 	@Override
 	public void delete(long hash) throws NoAccessException {
 		Snippet snippet = Snippet.getSnippet(hash);
 		if (snippet == null)
 			return;
 
 		Session session = getSession();
 		if (!session.getPolicy().canDeleteSnippet(session, snippet))
 			throw new NoAccessException();
 
 		snippet.delete();
 	}
 
 	@Override
 	public void rateSnippet(long id, int rate) throws NoAccessException, NotFoundException {
 		Snippet snippet = findSnippetThrowsException(id);
 		Session session = getSession();
 		if (!session.getPolicy().canRateSnippet(session, snippet))
 			throw new NoAccessException();
 		User user = session.getUser();
 		if (user == null)
 			throw new NoAccessException();
 
 		if (rate < 0 || rate > 5)
 			return;
 		if (rate == 0) {
 			snippet.unrate(user);
 		} else {
 			snippet.setRating(user, rate);
 		}
 
 	}
 
 	@Override
 	public void setDescription(long id, String desc) throws NoAccessException, NotFoundException {
 		if (desc == null || desc.isEmpty())
 			return;
 
 		Snippet snippet = findSnippetThrowsException(id);
 		Session session = getSession();
 		if (!session.getPolicy().canEditSnippet(session, snippet))
 			throw new NoAccessException();
 		snippet.setDescription(desc);
 	}
 
 	@Override
 	public void setCode(long id, String code) throws NoAccessException, NotFoundException {
 		editCode(id, code);
 	}
 
 	@Override
 	public void addTag(long id, String tag) throws NoAccessException, NotFoundException {
 		if (tag == null || tag.isEmpty())
 			return;
 		Snippet snippet = findSnippetThrowsException(id);
 		Session session = getSession();
 		if (!session.getPolicy().canEditSnippet(session, snippet))
 			throw new NoAccessException();
 
 		snippet.addTag(Tag.createTag(tag));
 	}
 
 	@Override
 	public void removeTag(long id, String tag) throws NoAccessException, NotFoundException {
 		if (tag == null || tag.isEmpty())
 			return;
 		Snippet snippet = findSnippetThrowsException(id);
 		Session session = getSession();
 		if (!session.getPolicy().canEditSnippet(session, snippet))
 			throw new NoAccessException();
 
 		snippet.removeTag(Tag.createTag(tag));
 	}
 
 	@Override
 	public void addComment(long id, String comment) throws NoAccessException, NotFoundException {
 		if (comment == null || comment.isEmpty())
 			return;
 
 		Session session = getSession();
 		if (!session.getPolicy().canComment(session))
 			throw new NoAccessException();
 		User user = session.getUser();
 		if (user == null)
 			throw new NoAccessException();
 
 		Snippet snippet = Snippet.getSnippet(id);
 		if (snippet == null)
 			throw new NotFoundException("Snippet with id " + id + " not found");
 
 		try {
 			Comment objComment = null;
 			objComment = snippet.addComment(comment, user);
 			logInfo("Added new comment with id=" + objComment.getHashID());
 		} catch (IOException e) {
 			logError("IOException during adding a new comment: " + e.getMessage());
 			System.err.println("IOException during creating of new comment on snippet " + id + ": " + e.getMessage());
 			e.printStackTrace(System.err);
 			throw new RemoteException();
 		}
 
 	}
 
 	@Override
 	public void create(String name, String desc, String code, String language, String license, String category, List<String> tags)
 			throws NoAccessException, IllegalArgumentException {
 
 		if (name == null || name.isEmpty())
 			throw new IllegalArgumentException("New snippet name cannot be empty");
 		if (desc == null || desc.isEmpty())
 			throw new IllegalArgumentException("New snippet desc cannot be empty");
 		if (code == null || code.isEmpty())
 			throw new IllegalArgumentException("New snippet code cannot be empty");
 		if (language == null || language.isEmpty())
 			throw new IllegalArgumentException("New snippet language cannot be empty");
 		if (license == null || license.isEmpty())
 			throw new IllegalArgumentException("New snippet language cannot be empty");
 
 		if (category != null && category.isEmpty())
 			category = null;
 		if (tags == null)
 			tags = new ArrayList<String>();
 
 		Category cat = Category.getCategory(category);
 		if (category != null && cat == null)
 			throw new IllegalArgumentException("Category not found");
 
 		Session session = getSession();
 		if (!session.getPolicy().canCreateSnippet(session, cat))
 			throw new NoAccessException();
 
 		User owner = session.getUser();
 		if (owner == null)
 			throw new NoAccessException();
 
 		List<Tag> tagList = new ArrayList<Tag>();
 		for (String tag : tags)
 			tagList.add(Tag.createTag(tag));
 
 		try {
 			Snippet result = Snippet.createSnippet(owner.getUsername(), name, desc, category, code, language, license, tagList);
 
 			if (result == null)
 				throw new RuntimeException("Unknown error");
 		} catch (IOException e) {
 			System.err.println("IOException while creating new snippet: " + e.getMessage());
 			e.printStackTrace(System.err);
 
 			throw new RuntimeException("Database access error");
 		}
 
 	}
 
 	@Override
 	public void addToFavorites(long id) throws NoAccessException, NotFoundException {
 		Session session = getSession();
 		Snippet snippet = Snippet.getSnippet(id);
 		if (snippet == null) {
 			// Snippet is not found
 			throw new NotFoundException("No snippet with id " + id + " found");
 		}
 
 		if (session.isLoggedIn()) {
 			User user = session.getUser();
 			if (user == null)
 				throw new NoAccessException();
 
 			user.addFavorite(snippet);
 		} else {
 			session.addFavorite(snippet);
 		}
 	}
 
 	/**
 	 * Searches for a snippet by its hash id and returns the snippet if found.
 	 * If not found, it throws a {@link NotFoundException}
 	 * 
 	 * @param id
 	 *            hash id of the snippet to search for
 	 * @return the found snippet
 	 * @throws NotFoundException
 	 *             Thrown if the snippet is not found
 	 */
 	private Snippet findSnippetThrowsException(long id) throws NotFoundException {
 		Snippet snippet = Snippet.getSnippet(id);
 		if (snippet == null)
 			throw new NotFoundException("Snippet with id " + id + " could not be found");
 		return snippet;
 	}
 
 	@Override
 	public void edit(XSnippet snippet) throws NoAccessException, NotFoundException, IllegalArgumentException {
 		if (snippet == null)
 			return;
 
 		Session session = getSession();
 		User user = session.getUser();
 		Snippet origin = Snippet.getSnippet(snippet.hash);
 
 		if (origin == null)
 			throw new NotFoundException();
 		if (!session.getPolicy().canEditSnippet(session, origin))
 			throw new NoAccessException();
 		if (user == null)
 			throw new NoAccessException();
 
 		// Access guaranteed. Edit snippet
 		origin.edit(snippet); // Can throw a IllegalArgumentException
 
 	}
 
 	@Override
 	public void editCode(long snippedID, String code) throws NoAccessException, NotFoundException {
 		if (code == null || code.isEmpty())
 			return;
 
 		Session session = getSession();
 		User user = session.getUser();
 		Snippet snippet = Snippet.getSnippet(snippedID);
 
 		if (snippet == null)
 			throw new NotFoundException();
 		if (!session.getPolicy().canEditSnippet(session, snippet))
 			throw new NoAccessException();
 		if (user == null)
 			throw new NoAccessException();
 
 		// Check if we really need a update
 		Code old = snippet.getCode();
 		if (old.code.equals(code))
 			return;
 
 		// Create new code object
 		Code newCode = Code.createCode(code, old.language, snippet.id, (old.getVersion() + 1));
 		snippet.setCode(newCode);
 	}
 
 	@Override
 	public List<String> getSupportedLanguages() {
 		List<String> result = Snippet.getSupportedLanguages();
 		result.add(ISnippet.moreLanguages);
 		return result;
 	}
 
 	@Override
 	public List<String> getMoreLanguages() {
 		List<String> result = Snippet.getNonDefaultLanguages();
 		return result;
 	}
 
 	@Override
 	public boolean hasDownloadableSource(long snippet_id) throws NotFoundException {
 		Snippet snippet = Snippet.getSnippet(snippet_id);
 		if (snippet == null)
 			throw new NotFoundException();
 
 		Code code = snippet.getCode();
 		if (code == null)
 			return false;
 		else
 			return snippet.getCode().hasDownloadableSource();
 	}
 
 	@Override
 	public long getDownloadSourceTicket(long snippet_id) throws NotFoundException, NoAccessException {
 		Snippet snippet = Snippet.getSnippet(snippet_id);
 		if (snippet == null)
 			throw new NotFoundException();
 
 		Code code = snippet.getCode();
 		if (code == null)
 			throw new NotFoundException("Snippet does not contain any source codes");
 
 		if (!code.hasDownloadableSource())
 			throw new NotFoundException("Snippet does not provide any downloadable source code packets");
 
 		// Creates a new ticket
 		long ticket = SourceDownloader.createTicket(code.code);
 		return ticket;
 	}
 
 	@Override
 	public boolean canEdit(long snippet_id) throws NotFoundException {
 		Session session = getSession();
 		Snippet snippet = Snippet.getSnippet(snippet_id);
 		if (snippet == null)
 			throw new NotFoundException();
 
 		if (!session.getPolicy().canEditSnippet(session, snippet))
 			return false;
 		User user = session.getUser();
 		if (user == null)
 			return false;
 
 		return true;
 
 	}
 
 	@Override
 	public XSnippet getSnippetOfDay() {
 		Snippet snippetofDay = Snippet.getSnippetOfDay();
 		if (snippetofDay == null)
 			return null;
 
 		return toXSnippet(snippetofDay);
 	}
 
 	@Override
 	public List<String> getSearchSuggestions() {
 		Session session = getSession();
 		return session.getSearchSuggestions(10);
 	}
 
 	@Override
 	public void increaseViewCounter(long id) throws NotFoundException {
 		Snippet snippet = Snippet.getSnippet(id);
 		if (snippet == null)
 			throw new NotFoundException("Snippet " + id + " cannot be found");
 		snippet.increaseViewCounter();
 	}
 
 	@Override
 	public void removeFavorite(long id) throws NotFoundException {
 		Session session = getSession();
 		Snippet snippet = Snippet.getSnippet(id);
 		if (snippet == null) {
 			// Snippet is not found
 			throw new NotFoundException("No snippet with id " + id + " found");
 		}
 
 		if (session.isLoggedIn()) {
 			User user = session.getUser();
 			if (user == null)
 				return;
 
 			user.removeFavorite(snippet);
 		} else {
 			session.removeFavorite(snippet);
 		}
 	}
 }
