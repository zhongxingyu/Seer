 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class Paragraph extends Model {
 	
 	/** HTML content of the paragraph */
 	@Required @MaxSize(1000)
 	@Lob
 	public String content;
 	
 	/** Answers of the paragraph */
 	@OneToMany(mappedBy="parent", cascade=CascadeType.REMOVE) // FIXME Utiliser CascadeType.ALL
 	public List<Post> answers;
 	
 	/** Footnotes referenced by this post */
 	@OneToMany(mappedBy="paragraph", cascade=CascadeType.REMOVE) // FIXME Idem (et virer les footNotes.save plus bas)
 	public List<FootNote> footNotes;
 	
 	/** Post which this paragraph belongs to */
 	@Required
 	@ManyToOne
 	public Post post;
 	
 	/** Number of the post (added to sort paragraphs) */
 	public Integer number;
 	
 	public Paragraph(Post post, String content, Integer number)
 	{
 		this.post = post;
 		this.content = content;
 		this.answers = new ArrayList<Post>();
 		this.footNotes = new ArrayList<FootNote>();
 		this.number = number;
 	}
 	
 	@Override
 	public String toString() {
 		return content.substring(0, Math.min(content.length(), 40));
 	}
 	
 	/**
 	 * Reply to this paragraph
 	 * @param author Author of the reply
 	 * @param content Content of the reply post
 	 */
 	public Post reply(User author, String content) {
 		Thread thread = post.thread;
 		Post reply = Post.create(author, content, this, thread);
 		return reply;
 	}
 	
 	public boolean hasAnswers() {
 		return answers.size() != 0;
 	}
 
 	/**
 	 * Convenient function to add a footnote to a paragraph
 	 */
 	public void addFootNote(FootNote footNote) {
 		footNote.paragraph = this;
 		footNotes.add(footNote);
 		footNote.save();
 	}
 	
 	public boolean hasFootNotes() {
 		return footNotes.size() != 0;
 	}
 }
