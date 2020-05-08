 package utils;
 
 
 import com.google.common.collect.Lists;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.project.DumbAware;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.text.StringUtil;
 import content.Review;
 import ui.ReviewBalloonBuilder;
 import ui.forms.ReviewForm;
 
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.html.HTML;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.parser.ParserDelegator;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 /**
  * User: ktisha
  */
 public class ReviewManager implements DumbAware {
   private static ReviewManager ourInstance;
   private final Project myProject;
 
   private ReviewManager(Project project) {
     myProject = project;
   }
 
   public static ReviewManager getInstance(Project project) {
     if (ourInstance == null) {
       ourInstance = new ReviewManager(project);
     }
     return ourInstance;
   }
 
   public void addReview(final Editor editor, int startOffset, int endOffset, String path) {
     final Review review = new Review(startOffset, endOffset, path);
     final ReviewForm reviewForm = new ReviewForm(review, editor.getProject());
     final ReviewBalloonBuilder reviewBalloonBuilder = new ReviewBalloonBuilder();
     reviewBalloonBuilder.showBalloon(review, editor, reviewForm, ReviewBundle.message("review.addReview"));
     reviewForm.requestFocus();
   }
 
   public void saveReview(Review review) {
     if (!StringUtil.isEmptyOrSpaces(review.getText()))
       ReviewService.getInstance(myProject).addReview(review);
   }
 
   public List<Review> getReviews() {
     return ReviewService.getInstance(myProject).getReviews();
   }
 
   public List<String> getCommitsToReview() {
     return ReviewService.getInstance(myProject).getCommitsToReview();
   }
 
   public List<String> getAuthors() {
     final List<String> authors = Lists.newArrayList();
 
     HTMLEditorKit.ParserCallback callback =
       new HTMLEditorKit.ParserCallback() {
 
         @Override
         public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet set, int pos) {
          if ("user".equals(tag.toString()))
            authors.add(String.valueOf(set.getAttribute(HTML.getAttributeKey("name"))));
         }
       };
 
 
     try {
       URL url = new URL("http://127.0.0.1:8000/all_authors");
       final InputStream inputStream = url.openStream();
       Reader reader = new InputStreamReader(inputStream);
       try {
         new ParserDelegator().parse(reader, callback, true);
       }
       finally {
         reader.close();
       }
     }
     catch (MalformedURLException e) {
       e.printStackTrace();
     }
     catch (IOException e) {
       e.printStackTrace();
     }
     return authors;
     //return ReviewService.getInstance(myProject).getAuthors();
   }
 }
