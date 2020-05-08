 package org.ffplanner.controller.constraints;
 
 import org.ffplanner.bean.MovieBundleInFestivalBean;
 import org.ffplanner.bean.ShowingBean;
 import org.ffplanner.def.ConstraintDefinition;
import org.ffplanner.def.ScheduleConstraints;
 import org.ffplanner.entity.MovieBundleInFestival;
 import org.ffplanner.entity.Showing;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.charset.StandardCharsets;
 
 /**
  * @author Bogdan Dumitriu
  */
 public class ConstraintsDumper {
 
     private MovieBundleInFestivalBean movieBundleInFestivalBean;
 
     private ShowingBean showingBean;
 
    private final ScheduleConstraints scheduleConstraints;
 
    public ConstraintsDumper(ScheduleConstraints scheduleConstraints) {
         this.scheduleConstraints = scheduleConstraints;
     }
 
     public void setMovieBundleInFestivalBean(MovieBundleInFestivalBean movieBundleInFestivalBean) {
         this.movieBundleInFestivalBean = movieBundleInFestivalBean;
     }
 
     public void setShowingBean(ShowingBean showingBean) {
         this.showingBean = showingBean;
     }
 
     public void write(OutputStream outputStream) throws IOException {
         try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
             writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
             writer.append("<schedule-constraints>\n");
             writeMovies(writer);
             writeShowings(writer);
             writer.append("</schedule-constraints>\n");
         }
     }
 
     private void writeMovies(OutputStreamWriter writer) throws IOException {
         writer.append("  <movies>\n");
         for (ConstraintDefinition.Movie movieConstraint : scheduleConstraints.getMovieConstraints()) {
             writer.append("    <movie>\n");
             writer.append("      <id>");
             writer.append(String.valueOf(movieConstraint.getMovieId()));
             writer.append("</id>\n");
             writer.append("      <priority>");
             writer.append(String.valueOf(movieConstraint.getPriority()));
             writer.append("</priority>\n");
             if (movieBundleInFestivalBean != null) {
                 final MovieBundleInFestival movieBundle = movieBundleInFestivalBean.find(movieConstraint.getMovieId());
                 writer.append("      <title>");
                 writer.append(movieBundle.getMovieBundle().getOriginalTitle());
                 writer.append("</title>\n");
             }
             writer.append("    </movie>\n");
         }
         writer.append("  </movies>\n");
     }
 
     private void writeShowings(OutputStreamWriter writer) throws IOException {
         writer.append("  <showings>\n");
         for (ConstraintDefinition.Showing showingConstraint : scheduleConstraints.getShowingConstraints()) {
             writer.append("    <showing>\n");
             writer.append("      <id>");
             writer.append(String.valueOf(showingConstraint.getShowingId()));
             writer.append("</id>\n");
             writer.append("      <priority>");
             writer.append(String.valueOf(showingConstraint.getPriority()));
             writer.append("</priority>\n");
             if (showingBean != null) {
                 final Showing showing = showingBean.find(showingConstraint.getShowingId());
                 writer.append("      <datetime>");
                 writer.append(String.valueOf(showing.getDateTime()));
                 writer.append("</datetime>\n");
                 writer.append("      <venue-id>");
                 writer.append(String.valueOf(showing.getVenueId()));
                 writer.append("</venue-id>\n");
                 writer.append("      <movie-id>");
                 writer.append(String.valueOf(showing.getMovieBundleInFestival().getId()));
                 writer.append("</movie-id>\n");
                 writer.append("      <title>");
                 writer.append(showing.getMovieBundleInFestival().getMovieBundle().getOriginalTitle());
                 writer.append("</title>\n");
             }
             writer.append("    </showing>\n");
         }
         writer.append("  </showings>\n");
     }
 }
