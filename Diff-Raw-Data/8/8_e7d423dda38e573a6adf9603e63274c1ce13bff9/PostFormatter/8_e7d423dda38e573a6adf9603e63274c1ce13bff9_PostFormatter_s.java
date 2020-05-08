 package daniel.blog.post;
 
 import daniel.blog.comment.Comment;
 import daniel.blog.comment.CommentFormFormatter;
 import daniel.blog.comment.CommentFormatter;
 import daniel.data.function.Function;
 import daniel.data.option.Option;
 import daniel.data.sequence.Sequence;
 import daniel.web.html.AnchorBuilder;
 import daniel.web.html.Element;
 import daniel.web.html.Tag;
 
 public final class PostFormatter {
   private PostFormatter() {}
 
   public static Element full(Post post, Sequence<Comment> commentsByDate) {
     Element.Builder builder = new Element.Builder(Tag.DIV);
     builder.addRawText(post.getContent());
     Option<Element> optCommentsSection = getCommentsSection(commentsByDate);
     if (optCommentsSection.isDefined())
       builder.addChild(optCommentsSection.getOrThrow());
    builder.addChild(CommentFormFormatter.getAddCommentForm(post));
    return builder.build();
   }
 
   private static Option<Element> getCommentsSection(Sequence<Comment> commentsByDate) {
     if (commentsByDate.isEmpty())
       return Option.none();
 
     Sequence<Element> commentElements = commentsByDate.map(new Function<Comment, Element>() {
       @Override public Element apply(Comment comment) {
         return CommentFormatter.full(comment);
       }
     });
 
     String headerText = String.format("%d Comments", commentElements.getSize());
     Element commentsHeader = new Element.Builder(Tag.H4).addEscapedText(headerText).build();
     return Option.some(new Element.Builder(Tag.SECTION)
         .addChild(commentsHeader)
         .addChildren(commentElements)
         .build());
   }
 
   public static Element summaryLink(Post post) {
     return new AnchorBuilder()
         .setHref(PostUrlFactory.getViewUrl(post))
         .addEscapedText(post.getSubject())
         .build();
   }
 }
