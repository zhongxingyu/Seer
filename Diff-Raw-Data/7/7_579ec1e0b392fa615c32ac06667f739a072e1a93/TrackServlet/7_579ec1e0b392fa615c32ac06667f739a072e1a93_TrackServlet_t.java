 package playlist.controller;
 
 import playlist.model.TracksDAO;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: stevelowenthal
  * Date: 9/19/13
  * Time: 5:22 PM
  *
  */
 public class TrackServlet extends HttpServlet {
 
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
   }
 
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     String artist = request.getParameter("artist");
     String genre = request.getParameter("genre");
 
    List<TracksDAO> tracks = null;
     if (artist != null) {
 
       // Assume we're searching by artist
       tracks = TracksDAO.listSongsByArtist(artist);
    } else if (genre != null) {
 
       // Assume we're searching by genre
       tracks = TracksDAO.listSongsByGenre(genre);
 
     }
 
     request.setAttribute("artist", artist);
    request.setAttribute("genre", genre);
     request.setAttribute("tracks", tracks);
     getServletContext().getRequestDispatcher("/tracks.jsp").forward(request,response);
 
   }
 
 }
