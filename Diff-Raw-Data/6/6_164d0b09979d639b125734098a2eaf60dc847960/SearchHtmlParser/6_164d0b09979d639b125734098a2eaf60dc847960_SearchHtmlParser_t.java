 package at.yawk.fimfiction.core;
 
 import static at.yawk.fimfiction.data.Story.StoryKey.*;
 
 import at.yawk.fimfiction.data.*;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 /**
  * Class for parsing pretty much any data in search HTML.
  *
  * @author Yawkat
  */
 class SearchHtmlParser extends SearchParser {
     /*
      * Do not waste your time trying to understand if you don't need to.
      * class is highly complex and messy but that is hardly avoidable with
      * complex lexer parser classes.
      */
 
     final SimpleDateFormat fimfictionDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
 
     boolean idOnly;
 
     int stage;
 
     @Nullable Story story;
 
     @Nullable StringBuilder title;
     @Nullable User author;
     @Nullable StringBuilder authorName;
     @Nullable FavoriteState favorited;
     @Nullable Set<Category> categories;
     @Nullable FormattedStringParser.FormattedStringHandler description;
     @Nullable Chapter chapter;
     @Nullable StringBuilder chapterTitle;
     @Nullable Set<FimCharacter> characters;
     @Nullable List<Chapter> chapters;
     int characterId;
     @Nullable Optional<User> loggedIn;
     @Nullable String nonce;
 
     @Override
     public void startElement(@Nonnull String uri,
                              @Nonnull String localName,
                              @Nonnull String qName,
                              @Nonnull Attributes attributes) throws SAXException {
         switch (stage) {
         case 0:
             if ("div".equals(qName) &&
                 "content_box post_content_box story_content_box".equals(attributes.getValue("class"))) {
                 story = Story.createMutable();
                 story.set(ID, Integer.parseInt(attributes.getValue("id").substring(6)));
                 if (idOnly) {
                     finishedStories.add(story);
                     story = null;
                 } else {
                     stage = 1;
                 }
             } else if (nonce == null && "a".equals(qName)) {
                 String nonce = attributes.getValue("data-nonce");
                 if (nonce != null) { this.nonce = nonce; }
             }
             break;
         case 1:
             String src = attributes.getValue("src");
             author = User.createMutable();
             if (src != null && !"//www.fimfiction-static.net/images/avatars/none_64.png".equals(src)) {
                 try {
                     author.set(User.UserKey.URL_PROFILE_IMAGE, Optional.existing(new URL("http:" + src)));
                 } catch (MalformedURLException e) {
                     throw new SAXException(e);
                 }
             } else {
                 author.set(User.UserKey.URL_PROFILE_IMAGE, Optional.missing(URL.class));
             }
             stage = 2;
             break;
         case 2:
             if ("div".equals(qName) && "right".equals(attributes.getValue("class"))) {
                 stage = 3;
             }
             break;
         case 3:
             stage = "track_container".equals(attributes.getValue("class")) ? 4 : 6;
             break;
         case 4:
             if ("a".equals(qName)) {
                 favorited = attributes.getValue("class").contains("favourite_button_selected") ?
                         FavoriteState.FAVORITED :
                         FavoriteState.NOT_FAVORITED;
                 stage = 5;
             }
             break;
         case 5:
             if ("input".equals(qName)) {
                 if (favorited != null) {
                     boolean email = attributes.getValue("checked") != null;
                     assert story != null;
                     story.set(FAVORITE_STATE,
                               email && favorited.isFavorited() ? FavoriteState.FAVORITED_WITH_EMAIL : favorited);
                 }
                 stage = 6;
             }
             break;
         case 6:
             if ("a".equals(qName)) {
                 String onClick = attributes.getValue("onclick");
                 if (onClick != null && onClick.startsWith("Ra")) {
                     assert story != null;
                     story.set(RATING_TOKEN, onClick.substring(onClick.indexOf('\'') + 1, onClick.lastIndexOf('\'')));
                 }
                 stage = 7;
             }
             break;
         case 8:
             if ("a".equals(qName)) {
                 stage = 9;
             }
             break;
         case 10:
             if ("b".equals(qName)) {
                 stage = 300;
             }
             break;
         case 301:
             if ("span".equals(qName)) {
                 String title = attributes.getValue("title");
                 if (title != null) {
                     assert story != null;
                     story.set(VIEW_COUNT_TOTAL, toIntLiberal(title));
                     stage = 11;
                 }
             }
             break;
         case 12:
             if ("a".equals(qName)) {
                 assert story != null;
                 story.set(READ_LATER_STATE, attributes.getValue("class").contains("read_it_later_selected"));
             }
             stage = 14;
             break;
         case 14:
             if ("a".equals(qName)) {
                 try {
                     assert story != null;
                     story.set(URL, new URL("http://www.fimfiction.net" + attributes.getValue("href")));
                 } catch (MalformedURLException e) {
                     throw new SAXException(e);
                 }
                 title = new StringBuilder();
                 stage = 15;
             }
             break;
         case 16:
             if ("a".equals(qName)) {
                 authorName = new StringBuilder();
                 stage = 17;
             }
             break;
         case 18:
             if ("img".equals(qName) &&
                 "//www.fimfiction-static.net/images/icons/views.png".equals(attributes.getValue("src"))) {
                 stage = 19;
             }
             break;
         case 20:
             if ("div".equals(qName) && "description".equals(attributes.getValue("class"))) {
                 stage = 21;
             }
             break;
         case 21:
             if ("story_image".equals(attributes.getValue("class"))) { stage = 22; } else {
                 assert story != null;
                 Optional<URL> optional = Optional.missing(URL.class);
                 story.set(URL_COVER, optional);
                 story.set(URL_THUMBNAIL, optional);
                 stage = 25;
             }
             break;
         case 22:
             try {
                 assert story != null;
                 story.set(URL_COVER, Optional.existing(new URL("http:" + attributes.getValue("href"))));
             } catch (MalformedURLException e) {
                 throw new SAXException(e);
             }
             stage = 23;
             break;
         case 23:
             try {
                 assert story != null;
                 story.set(URL_THUMBNAIL, Optional.existing(new URL("http:" + attributes.getValue("src"))));
             } catch (MalformedURLException e) {
                 throw new SAXException(e);
             }
             stage = 24;
             break;
         case 24:
             if ("a".equals(qName)) {
                 stage = 25;
             } else {
                 if (categories != null) {
                     assert story != null;
                     story.set(CATEGORIES, categories);
                     categories = null;
                 }
                 description = new FormattedStringParser.FormattedStringHandler();
                 stage = 26;
             }
             break;
         case 27:
             assert description != null;
             description.startElement(uri, localName, qName, attributes);
             break;
         case 28:
             if ("ul".equals(qName)) {
                 chapters = Lists.newArrayList();
                 stage = 29;
             }
             break;
         case 29:
             if ("div".equals(qName)) {
                 String clazz = attributes.getValue("class");
                 if (clazz != null && clazz.contains("chapter_container")) {
                     if (!clazz.contains("chapter_expander")) {
                         stage = 100;
                     }
                 }
             }
             if ("li".equals(qName) && "save_ordering".equals(attributes.getValue("class"))) {
                 assert story != null;
                 assert chapters != null;
                 story.set(CHAPTERS, chapters);
                 story.set(CHAPTER_COUNT, chapters.size());
                 chapters = null;
                 stage = 38;
             }
             break;
         case 100:
             stage = 101;
             break;
         case 101:
             stage = 30;
             break;
         case 30:
             chapter = Chapter.createMutable();
             if ("i".equals(qName)) {
                 if (attributes.getValue("class") != null) {
                     chapter.set(Chapter.ChapterKey.UNREAD, attributes.getValue("class").length() == 17);
                     stage = 31;
                 }
             } else if ("a".equals(qName)) {
                 try {
                     chapter.set(Chapter.ChapterKey.URL,
                                 new URL("http://www.fimfiction.net" + attributes.getValue("href")));
                 } catch (MalformedURLException e) {
                     throw new SAXException(e);
                 }
                 chapterTitle = new StringBuilder();
                 stage = 32;
             }
             break;
         case 31:
             if ("a".equals(qName)) {
                 try {
                     assert chapter != null;
                     chapter.set(Chapter.ChapterKey.URL,
                                 new URL("http://www.fimfiction.net" + attributes.getValue("href")));
                 } catch (MalformedURLException e) {
                     throw new SAXException(e);
                 }
                 chapterTitle = new StringBuilder();
                 stage = 32;
             }
             break;
         case 35:
             stage = 36;
             break;
         case 37:
             if ("a".equals(qName)) {
                 assert chapter != null;
                 chapter.set(Chapter.ChapterKey.ID, toIntLiberal(attributes.getValue("href")));
                 assert chapters != null;
                 chapters.add(chapter);
                 chapter = null;
                 stage = 29;
             }
             break;
         case 203:
             if ("div".equals(qName)) {
                 assert story != null;
                 if (!story.has(SEX)) { story.set(SEX, false); }
                 if (!story.has(GORE)) { story.set(GORE, false); }
                 stage = 204;
             }
             break;
         case 205:
             if ("br".equals(qName)) {
                 stage = 39;
             }
             break;
         case 39:
             if ("span".equals(qName)) {
                 stage = 40;
             }
             break;
         case 41:
             if ("br".equals(qName)) {
                 stage = 42;
             }
             if ("a".equals(qName)) {
                 stage = 43;
             }
             break;
         case 42:
             if ("span".equals(qName)) {
                 stage = 43;
             }
             break;
         case 43:
         case 44:
             if ("img".equals(qName)) {
                 src = attributes.getValue("src");
                 try {
                     assert characters != null;
                     characters.add(FimCharacter.DefaultCharacter
                                                .getOrCreateCharacter(characterId, new URL("http:" + src)));
                 } catch (MalformedURLException e) {
                     throw new SAXException(e);
                 }
             } else if ("i".equals(qName)) {
                 assert story != null;
                 assert characters != null;
                 story.set(CHARACTERS, characters);
                 characters = null;
                 finishedStories.add(story);
                 story = null;
                 stage = 0;
             } else if ("a".equals(qName)) {
                 characterId = toIntLiberal(attributes.getValue("href"));
             }
             break;
         }
     }
 
     @Override
     public void endElement(@Nonnull String uri, @Nonnull String localName, @Nonnull String qName) throws SAXException {
         if (stage == 0) {
             return;
         }
         switch (stage) {
         case 11:
             if ("div".equals(qName)) {
                 stage = 12;
             }
             break;
         case 15:
             assert story != null;
             assert title != null;
             story.set(TITLE, title.toString());
             stage = 16;
             break;
         case 17:
             assert authorName != null;
             assert author != null;
             author.set(User.UserKey.NAME, authorName.toString());
             assert story != null;
             story.set(AUTHOR, author);
             authorName = null;
             stage = 18;
             break;
         case 26:
             stage = 27;
             break;
         case 27:
             assert description != null;
             if ("div".equals(qName)) {
                 assert story != null;
                 story.set(DESCRIPTION, description.builder.build());
                 description = null;
                 stage = 28;
             } else {
                 description.endElement(uri, localName, qName);
             }
             break;
         case 32:
             assert chapterTitle != null;
             assert chapter != null;
             chapter.set(Chapter.ChapterKey.TITLE, chapterTitle.toString());
             chapterTitle = null;
             stage = 33;
             break;
         case 33:
             if ("b".equals(qName)) {
                 stage = 34;
             }
             break;
         case 36:
             stage = 37;
             break;
         case 38:
             if ("a".equals(qName)) {
                 stage = 200;
             }
             break;
         case 200:
             if ("a".equals(qName)) {
                 stage = 201;
             }
             break;
         case 201:
             if ("a".equals(qName)) {
                 stage = 202;
             }
             break;
         case 202:
             if ("a".equals(qName)) {
                 stage = 203;
             }
             break;
         }
     }
 
     @Override
     public void characters(@Nonnull char[] ch, int start, int length) throws SAXException {
         if (stage == 0) {
             if (loggedIn == null) {
                 String asString = new String(ch, start, length).trim();
                 if (asString.contains("var static_url = \"//www.fimfiction-static.net\";")) {
                     int i = asString.indexOf("logged_in_user.id = ");
                     if (i == -1) {
                         loggedIn = Optional.missing(User.class);
                     } else {
                         String sub1 = asString.substring(i + 20);
                         String sub2 = sub1.substring(0, sub1.indexOf(';'));
                         loggedIn = Optional.existing(User.createMutable().set(User.UserKey.ID, Integer.parseInt(sub2)));
                     }
                 }
             }
             return;
         }
         String asString = new String(ch, start, FormattedStringParser.clipWhitespace(ch, start, length, false));
         if (asString.isEmpty() && stage != 27) {
             return;
         }
         switch (stage) {
         case 7:
             assert story != null;
             story.set(LIKE_COUNT, toIntLiberal(asString));
             stage = 8;
             break;
         case 9:
             assert story != null;
             story.set(DISLIKE_COUNT, toIntLiberal(asString));
             stage = 10;
             break;
         case 300:
             assert story != null;
             story.set(COMMENT_COUNT, toIntLiberal(asString));
             stage = 301;
             break;
         case 15:
             assert title != null;
             title.append(asString);
             break;
         case 17:
             assert authorName != null;
             authorName.append(asString);
             break;
         case 19:
             assert story != null;
             story.set(VIEW_COUNT_MAXIMUM_CHAPTER, toIntLiberal(asString.substring(0, asString.indexOf('('))));
             story.set(VIEW_COUNT_TOTAL, toIntLiberal(asString.substring(asString.indexOf('('))));
             stage = 20;
             break;
         case 25:
             if (categories == null) {
                 categories = EnumSet.noneOf(Category.class);
             }
             for (Category category : Category.values()) {
                 if (getDisplayName(category).equals(asString)) {
                     categories.add(category);
                     break;
                 }
             }
             stage = 24;
             break;
         case 27:
             assert description != null;
             description.characters(ch, start, length);
             break;
         case 32:
             assert chapterTitle != null;
             chapterTitle.append(asString);
             break;
         case 34:
             assert story != null;
             story.set(DATE_UPDATED, parseDate(asString));
             stage = 35;
             break;
         case 36:
             int c = toIntLiberal(asString, -1);
             if (c >= -1) {
                 assert chapter != null;
                 chapter.set(Chapter.ChapterKey.WORD_COUNT, c);
             }
             break;
         case 203:
             assert story != null;
             asString = asString.trim();
             if ("Complete".equals(asString)) {
                 story.set(STATUS, StoryStatus.COMPLETED);
             } else if ("Incomplete".equals(asString)) {
                 story.set(STATUS, StoryStatus.INCOMPLETE);
             } else if ("Cancelled".equals(asString)) {
                 story.set(STATUS, StoryStatus.CANCELLED);
             } else if ("On Hiatus".equals(asString)) {
                 story.set(STATUS, StoryStatus.ON_HIATUS);
             } else if ("Everyone".equals(asString)) {
                 story.set(CONTENT_RATING, ContentRating.EVERYONE);
             } else if ("Teen".equals(asString)) {
                 story.set(CONTENT_RATING, ContentRating.TEEN);
             } else if ("Mature".equals(asString)) {
                 story.set(CONTENT_RATING, ContentRating.MATURE);
             } else if ("Sex".equals(asString)) {
                 story.set(SEX, true);
             } else if ("Gore".equals(asString)) {
                 story.set(GORE, true);
             }
             break;
         case 204:
             int words = toIntLiberal(asString, -1);
             if (words >= 0) {
                 assert story != null;
                 story.set(WORD_COUNT, words);
                 stage = 205;
             }
             break;
         case 40:
             assert story != null;
             story.set(DATE_FIRST_POSTED, parseDate(asString));
             characters = Sets.newHashSet();
             stage = 41;
             break;
         case 43:
             assert story != null;
             story.set(DATE_UPDATED, parseDate(asString));
             stage = 44;
             break;
         }
     }
 
     @Nonnull
     private Date parseDate(@Nonnull String date) throws SAXException {
         try {
             return fimfictionDateFormat.parse(date.replaceAll("(st|nd|rd|th)", ""));
         } catch (ParseException e) {
             throw new SAXException(e);
         }
     }
 
     private static int toIntLiberal(String toInt) {
         return toIntLiberal(toInt, 0);
     }
 
     private static int toIntLiberal(@Nullable String toInt, int defaultValue) {
         if (toInt != null) {
             int result = 0;
             boolean modified = false;
             for (int i = 0, l = toInt.length(); i < l; i++) {
                 char c = toInt.charAt(i);
                 int value = c - '0';
                 if (value >= 0 && value < 10) {
                     result *= 10;
                     result += value;
                     modified = true;
                 }
             }
             if (modified) {
                 return result;
             }
         }
         return defaultValue;
     }
 
     @Nonnull
     private static String getDisplayName(@Nonnull Category category) {
         switch (category) {
         case ADVENTURE:
             return "Adventure";
         case ALTERNATE_UNIVERSE:
             return "Alternate Universe";
         case ANTHRO:
             return "Anthro";
         case COMEDY:
             return "Comedy";
         case CROSSOVER:
             return "Crossover";
         case DARK:
             return "Dark";
         case HUMAN:
             return "Human";
         case RANDOM:
             return "Random";
         case ROMANCE:
             return "Romance";
         case SAD:
             return "Sad";
         case SLICE_OF_LIFE:
             return "Slice of Life";
         case TRAGEDY:
             return "Tragedy";
         default:
             throw new IllegalArgumentException(category.name());
         }
     }
 }
