 package addon.movie;
 
 import bashoid.Addon;
 import bashoid.Message;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import utils.WebPage;
 
 public class Movie extends Addon {
 
     private static final String ENCODING = "UTF-8";
     private static final String REACT_MESSAGE = "movie ";
     private static final String ID_PREFIX = "/film/";
     private int reactionId = 0;
 
     private static class MovieException extends Exception {
 
        public MovieException(String message)
        {
             super(message);
         }
     }
 
     class MovieData {
         String title;
         String link_csfd;
         String link_imdb;
         String country;
         String year;
         String length;
         String director;
         String genre;
         String rating_csfd;
         String rating_imdb;
 
         /*public void print() {
             System.out.print("Title: " + title + "\n" +
                     "Link_csfd: " + link_csfd + "\n" +
                     "Link_imdb: " + link_imdb + "\n" +
                     "Country: " + country + "\n" +
                     "Year: " + year + "\n" +
                     "Length: " + length + "\n" +
                     "Director: " + director + "\n" +
                     "Genre: " + genre  + "\n" +
                     "Rating_csfd: " + rating_csfd + "\n" +
                     "Rating_imdb: " + rating_imdb + "\n");
         }*/
 
         @Override
         public String toString() {
             // Prometheus (2012), USA, 124 min, csfd: 70%, imdb: 7.5, http://csfd.cz/film/290958
             String result = title + " ";
             if (year != null)
                 result += "(" + year + ")";
             result += ", ";
             if (country != null)
                 result += country + ", ";
             if (length != null)
                 result += length + ", ";
             if (rating_csfd != null || rating_imdb != null) {
                 result += "Rating: ";
                 if (rating_csfd != null)
                     result += rating_csfd + " (csfd.cz), ";
                 if (rating_imdb != null)
                     result += rating_imdb + " (imdb.com), ";
             }
             result += link_csfd;
             return result;
         }
     }
 
     private String SearchMovieData(String movieSearch) throws Exception {
         String link = GetMovieLink(movieSearch);
         return GetMovieData(link);
     }
 
     private String GetMovieData(String link) throws Exception {
         MovieData data = new MovieData();
         data.link_csfd = link;
 
         LoadMovieData(data);
 
         if (data.link_imdb != null)
             LoadImdbData(data);
 
         return data.toString();
     }
 
     private String GetMovieLink(String movieSearch) throws Exception {
         movieSearch = movieSearch.replaceAll(" ", "+");
         String url = "http://www.csfd.cz/hledat/?q=" + movieSearch;
         WebPage page = WebPage.loadWebPage(url, ENCODING);
         Document doc = Jsoup.parse( page.getContent() );
 
         String bodyId = doc.body().attr("id");
         if (!bodyId.equals("ap-web-search"))
             throw new MovieException("Movie '" + movieSearch + "' not found");
 
         Element movies = doc.getElementById("search-films");
 
         if (!movies.getElementsByClass("not-found").isEmpty())
             throw new MovieException("Movie '" + movieSearch + "' not found");
 
         Element firstMovie = movies.getElementsByClass("subject").first();
         Element movieLink = firstMovie.getElementsByTag("a").first();
         String partialLink = movieLink.attr("href");
         return GenerateCsfdMovieLink(partialLink);
     }
 
     private String ExtractIdFromLink(String partialLink) {
         int beginIdx = partialLink.indexOf(ID_PREFIX)+ID_PREFIX.length();
         int endIdx = partialLink.indexOf("-", beginIdx);
         String id = endIdx != -1 ? partialLink.substring(beginIdx, endIdx) : partialLink.substring(beginIdx);
         return id;
     }
 
     private String GenerateCsfdMovieLink(String partialLink) {
         String id = ExtractIdFromLink(partialLink);
         return "http://csfd.cz/film/" + id;
     }
 
     private void LoadMovieData(MovieData data) throws Exception {
         WebPage page = WebPage.loadWebPage(data.link_csfd, ENCODING);
         Element movieInfo = Jsoup.parse( page.getContent() ).getElementById("profile");
         {
             Element title = movieInfo.getElementsByTag("h1").first();
             data.title = title.text().trim();
 
             Elements genres = movieInfo.getElementsByClass("genre");
             if (!genres.isEmpty())
                 data.genre = genres.first().text().trim();
 
             Element origin = movieInfo.getElementsByClass("origin").first();
             String[] originParts = origin.text().split(",");
             if (originParts.length > 0)
                 data.country = originParts[0].trim();
             if (originParts.length > 1)
                 data.year = originParts[1].trim();
             if (originParts.length > 2)
                 data.length = originParts[2].trim();
 
             Elements otherData = movieInfo.getElementsByTag("h4");
             for (Element ele : otherData) {
                 if (ele.text().equals("Režie:"))  {
                     Element directorLink = ele.parent().getElementsByTag("a").first();
                     data.director = directorLink.text();
                     break;
                 }
             }
         }
 
         Element sidebar = Jsoup.parse( page.getContent() ).getElementById("sidebar");
         {
             Element rating = sidebar.getElementById("rating");
             Element average = rating.getElementsByClass("average").first();
             if (!average.text().isEmpty())
                 data.rating_csfd = average.text().trim();
 
             Element share = sidebar.getElementById("share");
             Element links = share.getElementsByClass("links").first();
             Elements imdbs = links.getElementsByClass("imdb");
             if (!imdbs.isEmpty()) {
                 Element childImdb = imdbs.first();
                 Element imdb = childImdb.parent();
                 data.link_imdb = imdb.attr("href");
             }
         }
     }
 
     private void LoadImdbData(MovieData data) throws Exception {
         WebPage page = WebPage.loadWebPage(data.link_imdb, ENCODING);
 
         Elements sidebarList = Jsoup.parse( page.getContent() ).getElementsByClass("star-box-giga-star");
 
         if (sidebarList.isEmpty())
             return;
 
         Element sidebar = sidebarList.first();
         data.rating_imdb = sidebar.text().trim();
     }
 
     @Override
     public boolean shouldReact(Message message) {
         String messageText = message.text.trim();
         if (messageText.startsWith("movie") ||
             messageText.startsWith("movies") ||
             messageText.startsWith("vycsfdkuj") ||
             messageText.startsWith("vyfilmuj")) {
                 reactionId = 1;
                 return true;
         }
 
         if (messageText.startsWith("http://www.csfd.cz/film") ||
             messageText.startsWith("http://csfd.cz/film")) {
             reactionId = 2;
             return true;
         }
 
         reactionId = 0;
         return false;
     }
 
     @Override
     protected void setReaction(Message message) {
         try {
             String result;
             String messageText = message.text.trim();
             switch(reactionId) {
                 case 1: {
                     String movieSearch = messageText.substring(messageText.indexOf(" ") +1);
                     result = SearchMovieData(movieSearch);
                     break;
                 }
                 case 2: {
                     String csfdLink = GenerateCsfdMovieLink(messageText);
                     result = GetMovieData(csfdLink);
                     break;
                 }
                 default:
                     return;
             }
 
             reaction.add( result );
         } catch (Exception e) {
             if (e instanceof MovieException) {
                 reaction.add( e.getMessage() );
             }
             else {
                 System.out.println(e);
                 if (message.text.startsWith(REACT_MESSAGE))
                     setError("Cannot load given URL.", e);
             }
         }
     }
 }
