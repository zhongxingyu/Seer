 /*
  * Copyright (C) 2011  Southern Storm Software, Pty Ltd.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.southernstorm.tvguide;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.text.SpannableString;
 
 public class TvProgramme {
 
     public TvProgramme(TvChannel channel) {
         this.channel = channel;
         directors = new ArrayList<String>();
         actors = new ArrayList<String>();
         presenters = new ArrayList<String>();
         categories = new ArrayList<String>();
         otherCredits = new TreeMap<String, List<String>>();
     }
 
     public TvChannel getChannel() { return channel; }
     
     public Calendar getStart() { return start; }
     public void setStart(Calendar start) { this.start = start; }
 
     public Calendar getStop() { return stop; }
     public void setStop(Calendar stop) { this.stop = stop; }
 
     public String getTitle() { return title; }
     public void setTitle(String title) { this.title = title; }
 
     public String getSubTitle() { return subTitle; }
     public void setSubTitle(String subTitle) { this.subTitle = subTitle; }
 
     public String getDescription() { return description; }
     public void setDescription(String desc) { this.description = desc; }
 
     public String getDate() { return date; }
     public void setDate(String date) { this.date = date; }
 
     public String getRating() { return rating; }
     public void setRating(String rating) { this.rating = rating; }
 
     public String getStarRating() { return starRating; }
     
     public List<String> getDirectors() { return directors; }
     public List<String> getActors() { return actors; }
     public List<String> getPresenters() { return presenters; }
     public List<String> getCategories() { return categories; }
     
     public String getEpisodeNumber() { return episodeNumber; }
     public int getSeason() { return season; }
     
     public int getYear() {
         if (date == null || date.length() == 0) {
             return 0;
         } else {
             try {
                 return Integer.valueOf(date);
             } catch (NumberFormatException e) {
                 return 0;
             }
         }
     }
 
     /**
      * Gets the duration of the programme in minutes.
      * 
      * @return the duration
      */
     public long getDuration() {
         if (start != null && stop != null)
             return (stop.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 60);
         else
             return 0;
     }
 
     /**
      * Loads the programme details from an XML input stream.
      *
      * When this method exits, the parser will be positioned just after
      * the programme end element.
      *
      * @param parser Pull parser containing the input.  Must be positioned
      * on the programme element.
      * @param convertTimezone true to convert date/time values to local time
      * @throws XmlPullParserException error in xml data
      * @throws IOException error reading the xml data
      */
     public void load(XmlPullParser parser, boolean convertTimezone) throws XmlPullParserException, IOException {
         start = Utils.parseDateTime(parser.getAttributeValue(null, "start"), convertTimezone);
         stop = Utils.parseDateTime(parser.getAttributeValue(null, "stop"), convertTimezone);
         int eventType = parser.next();
         while (eventType != XmlPullParser.END_DOCUMENT) {
             if (eventType == XmlPullParser.START_TAG) {
                 String name = parser.getName();
                 boolean handled = true;
                 switch (name.charAt(0)) {
                 case 'a':
                     if (name.equals("actor")) {
                         addToList(actors, Utils.getContents(parser, name));
                     } else if (name.equals("aspect")) {
                         aspectRatio = Utils.getContents(parser, name);
                     } else if (name.equals("adapter")) {
                         addOtherCredit("Adapted By", Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 case 'c':
                     if (name.equals("category")) {
                         String category = Utils.getContents(parser, name);
                         if (category != null) {
                             categories.add(category);
                             char ch = category.charAt(0);
                             if (ch == 'M') {
                                 if (category.equalsIgnoreCase("Movie") ||
                                         category.equalsIgnoreCase("Movies"))
                                     isMovie = true;
                             }
                         }
                     } else if (name.equals("credits")) {
                         // Container element - nothing to do.
                     } else if (name.equals("country")) {
                         country = Utils.getContents(parser, name);
                     } else if (name.equals("commentator")) {
                         addOtherCredit("Commentator", Utils.getContents(parser, name));
                     } else if (name.equals("composer")) {
                         addOtherCredit("Composer", Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 case 'd':
                     if (name.equals("desc")) {
                         description = Utils.getContents(parser, name);
                     } else if (name.equals("date")) {
                         date = Utils.getContents(parser, name);
                     } else if (name.equals("director")) {
                         addToList(directors, Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 case 'e':
                     if (name.equals("episode-num")) {
                         String system = parser.getAttributeValue(null, "system");
                         if (system != null && system.equals("xmltv_ns"))
                             episodeNumber = fixEpisodeNumber(Utils.getContents(parser, name));
                     } else if (name.equals("editor")) {
                         addOtherCredit("Editor", Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 case 'g':
                     if (name.equals("guest")) {
                         addOtherCredit("Guest", Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 case 'l':
                     if (name.equals("language")) {
                         language = Utils.getContents(parser, name);
                     } else {
                         handled = false;
                     }
                     break;
                 case 'o':
                     if (name.equals("orig-language")) {
                         originalLanguage = Utils.getContents(parser, name);
                     } else {
                         handled = false;
                     }
                     break;
                 case 'p':
                     if (name.equals("premiere")) {
                         isPremiere = true;
                     } else if (name.equals("previously-shown")) {
                         isRepeat = true;
                     } else if (name.equals("presenter")) {
                         addToList(presenters, Utils.getContents(parser, name));
                     } else if (name.equals("producer")) {
                         addOtherCredit("Producer", Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 case 'r':
                     if (name.equals("rating")) {
                         rating = Utils.getContents(parser, name);
                     } else {
                         handled = false;
                     }
                     break;
                 case 's':
                     if (name.equals("sub-title")) {
                         subTitle = Utils.getContents(parser, name);
                     } else if (name.equals("star-rating")) {
                         starRating = Utils.getContents(parser, name);
                     } else {
                         handled = false;
                     }
                     break;
                 case 't':
                     if (name.equals("title")) {
                         title = Utils.getContents(parser, name);
                     } else {
                         handled = false;
                     }
                     break;
                 case 'v':
                     if (name.equals("video")) {
                         // Container element - nothing to do.
                     } else {
                         handled = false;
                     }
                     break;
                 case 'w':
                     if (name.equals("writer")) {
                         addOtherCredit("Writer", Utils.getContents(parser, name));
                     } else {
                         handled = false;
                     }
                     break;
                 default:
                     handled = false;
                     break;
                 }
                 if (!handled) {
                     // The following are in the DTD, but not processed yet.
                     if (name.equals("present") ||
                             name.equals("quality") ||
                             name.equals("audio") ||
                             name.equals("stereo") ||
                             name.equals("last-chance") ||
                             name.equals("new") ||
                             name.equals("subtitles") ||
                             name.equals("review") ||
                             name.equals("url") ||
                             name.equals("length") ||
                             name.equals("icon")) {
                         System.out.println("Warning: unhandled standard programme element: " + name);
                     } else {
                         System.out.println("Warning: unknown programme element: " + name);
                     }
                 }
             } else if (eventType == XmlPullParser.END_TAG) {
                 if (parser.getName().equals("programme"))
                     break;
             }
             eventType = parser.next();
         }
     }
 
     private static int TITLE_COLOR = 0;
     private static int DETAILS_COLOR = 0xFF606060;
     private static int HEADING_COLOR = 0;
     private static int PREMIERE_COLOR = 0xFFFF0000;
 
     /**
      * Returns the short description of the programme to display in two lines.
      * 
      * @return the short description as a formatted SpannableString
      */
     public SpannableString getShortDescription() {
         RichTextFormatter formatter = new RichTextFormatter();
         formatter.setColor(TITLE_COLOR);
         if (isMovie)
             formatter.append("MOVIE: ");
         formatter.append(title);
         formatter.setColor(DETAILS_COLOR);
         if (date != null && !date.equals("0"))
             formatter.append(" (" + date + ")");
         if (rating != null) {
             formatter.append(" (" + rating + ")");
             if (isRepeat)
                 formatter.append("(R)");
         } else if (isRepeat) {
             formatter.append(" (R)");
         }
         String stars = formatStars(starRating);
         if (stars != null) {
             formatter.append(" ");
             formatter.append(stars);
         }
         String category = null;
         for (String cat: categories) {
             // Ignore generic categories that occur early in the list.
             if (cat.equalsIgnoreCase("Movie") || cat.equalsIgnoreCase("Movies") || cat.equalsIgnoreCase("series"))
                 continue;
             category = cat;
             break;
         }
         if (!isMovie && category != null) {
             formatter.append(", ");
             formatter.append(category);
         }
         formatter.nl();
         formatter.setItalic(true);
         if (subTitle != null)
             formatter.append(subTitle);
         if (episodeNumber != null) {
             formatter.append(" (Episode ");
             formatter.append(episodeNumber);
             formatter.append(")");
         }
         formatter.setItalic(false);
         if (isMovie) {
             // Add the category and first actor name to the second line for movies
             // because there usually will be no episode name.
             String actor = (actors.size() > 0 ? actors.get(0) : null);
             if (category != null)
                 formatter.append(category);
             if (actor != null) {
                 if (category != null)
                     formatter.append(", ");
                 formatter.append(actor);
             }
         }
         if (isPremiere) {
             formatter.append(", ");
             formatter.setBold(true);
             formatter.setColor(PREMIERE_COLOR);
             formatter.append("Premiere");
             formatter.setColor(DETAILS_COLOR);
             formatter.setBold(false);
         }
         return formatter.toSpannableString();
     }
 
     /**
      * Returns the long description of the programme to display when the item is expanded.
      * 
      * @return the long description as a formatted SpannableString
      */
     public SpannableString getLongDescription() {
         RichTextFormatter formatter = new RichTextFormatter();
         
         // Add the full programme description.
         formatter.setColor(DETAILS_COLOR);
         if (description != null) {
             formatter.append(description);
             formatter.endParagraph();
         }
         
         // Add the duration of the show.
         formatter.setColor(HEADING_COLOR);
         formatter.append("Duration: ");
         formatter.setColor(DETAILS_COLOR);
         formatter.append(Long.toString(getDuration()));
         formatter.append(" minutes, ");
         formatter.append(Utils.formatTime(getStart()));
         formatter.append(" to ");
         formatter.append(Utils.formatTime(getStop()));
         formatter.endParagraph();
         
         // Add categories, actors, directors, etc.
         formatCredits(formatter, "Categories", categories);
         formatCredits(formatter, "Starring", actors);
         formatCredits(formatter, "Presenter", presenters);
         for (String key: otherCredits.keySet())
             formatCredits(formatter, key, otherCredits.get(key));
         formatCredits(formatter, "Director", directors);
         
         // Other values.
         formatField(formatter, "Language", language);
         formatField(formatter, "Original language", originalLanguage);
         formatField(formatter, "Country", country);
         formatField(formatter, "Aspect ratio", aspectRatio);
         
         return formatter.toSpannableString();
     }
     
     private static void formatCredits(RichTextFormatter formatter, String name, List<String> credits) {
         if (credits.size() < 1)
             return;
         formatter.setColor(HEADING_COLOR);
         formatter.append(name);
         formatter.append(": ");
         formatter.setColor(DETAILS_COLOR);
         for (int index = 0; index < credits.size(); ++index) {
             if (index > 0)
                 formatter.append(", ");
             formatter.append(credits.get(index));
         }
         formatter.endParagraph();
     }
 
     private static void formatField(RichTextFormatter formatter, String name, String value) {
         if (value != null) {
             formatter.setColor(HEADING_COLOR);
             formatter.append(name);
             formatter.append(": ");
             formatter.setColor(DETAILS_COLOR);
             formatter.append(value);
         }
     }
     
     private static String formatStars(String starRating) {
         if (starRating == null)
             return null;
         String[] list = starRating.split("/");
         if (list.length >= 2) {
             int numer = Integer.valueOf(list[0]);
             int denom = Integer.valueOf(list[1]);
             if (numer > 0 && denom > 0) {
                 double stars = numer * 5.0 / denom;
                 if (stars < 0)
                     stars = 0;
                 else if (stars > 5)
                     stars = 5;
                 String rating = "";
                 int istars = (int)(stars);
                //double leftOver = stars - istars;
                 for (int black = 0; black < istars; ++black)
                     rating += '\u2605';  // Black star.
                // Android fonts don't have black outlined star.
                /*if (leftOver >= 0.5f) {
                     rating += '\u272D';  // Black outlined star.
                     ++istars;
                }*/
                 for (int white = istars; white < 5; ++white)
                     rating += '\u2606';  // White star.
                 return rating;
             }
         }
         return null;
     }
     
     private static void addToList(List<String> list, String str) {
         if (str != null)
             list.add(str);
     }
     
     private void addOtherCredit(String type, String str) {
         if (str == null)
             return;
         if (!otherCredits.containsKey(type))
             otherCredits.put(type, new ArrayList<String>());
         otherCredits.get(type).add(str);
     }
     
     // Episode numbers in the "xmltv_ns" system are of the form
     // A.B.C, where each of the numbers is 0-based, not 1-based.
     // The numbers could also have the form X/Y for multiple parts.
     // Fix the number so it is closer to what the user expects.
     private String fixEpisodeNumber(String str) {
         if (str == null)
             return str;
         String[] components = str.split("\\.");
         String result = "";
         boolean needDot = false;
         season = 0;
         for (int index = 0; index < components.length; ++index) {
             String comp = components[index];
             int slash = comp.indexOf('/');
             if (slash >= 0)
                 comp = comp.substring(0, slash);
             if (comp.length() == 0)
                 continue;
             if (needDot) {
                 if (index == 2)
                     result += ", Part ";
                 else
                     result += '.';
             }
             result += Integer.toString(Integer.valueOf(comp) + 1);
             if (!needDot)
                 season = Integer.valueOf(comp) + 1;
             needDot = true;
         }
         return result;
     }
 
     // Internal state.
     private TvChannel channel;
     private Calendar start;
     private Calendar stop;
     private String title;
     private String subTitle;
     private String description;
     private String date;
     private List<String> directors;
     private List<String> actors;
     private List<String> presenters;
     private Map< String, List<String> > otherCredits;
     private List<String> categories;
     private String rating;
     private String starRating;
     private String episodeNumber;
     private int season;
     private String language;
     private String originalLanguage;
     private String country;
     private String aspectRatio;
     private boolean isPremiere;
     private boolean isRepeat;
     private boolean isMovie;
 }
