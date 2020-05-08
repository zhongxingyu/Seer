 package at.yawk.fimfiction.html;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import lombok.AccessLevel;
 import lombok.experimental.FieldDefaults;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import at.yawk.fimfiction.Chapter;
 import at.yawk.fimfiction.Chapter.ChapterBuilder;
 import at.yawk.fimfiction.Character;
 import at.yawk.fimfiction.Story.Category;
 import at.yawk.fimfiction.Story.ContentRating;
 import at.yawk.fimfiction.Story.FavoriteState;
 import at.yawk.fimfiction.Story.Status;
 import at.yawk.fimfiction.User;
 import at.yawk.fimfiction.User.UserBuilder;
 
 /**
  * Class for parsing pretty much any data in search HTML.
  * 
  * @author Yawkat
  */
 @FieldDefaults(level = AccessLevel.PRIVATE)
 public class FullSearchParser extends AbstractSearchParser {
     /*
      * Do not waste your time trying to understand this if you don't need to.
      * This class is highly complex and messy but that is hardly avoidable with
      * complex lexer parser classes.
      */
     
     final SimpleDateFormat fimfictionDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
     int stage;
     StringBuilder title;
     UserBuilder author;
     StringBuilder authorName;
     boolean favorited;
     Set<Category> categories;
     StringBuilder description;
     ChapterBuilder chapter;
     StringBuilder chapterTitle;
     Set<Character> characters;
     List<Chapter> chapters;
     
     @Override
     public void reset() {
         super.reset();
         this.stage = 0;
         this.title = null;
         this.author = null;
         this.authorName = null;
         this.favorited = false;
         this.categories = null;
         this.description = null;
         this.chapter = null;
         this.chapterTitle = null;
         this.characters = null;
         this.chapters = null;
     }
     
     @Override
     public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
         switch (this.stage) {
         case 0:
             if (qName.equals("div") && "content_box post_content_box story_content_box".equals(atts.getValue("class"))) {
                 this.startStory();
                 this.getCurrentBuilder().id(Integer.parseInt(atts.getValue("id").substring(6)));
                 this.stage = 1;
             }
             break;
         case 1:
             String src = atts.getValue("src");
             this.author = User.builder();
             if (src != null && !src.equals("//www.fimfiction-static.net/images/avatars/none_64.png")) {
                 try {
                     this.author.profileImageUrl(new URL("http:" + src));
                 } catch (final MalformedURLException e) {
                     throw new SAXException(e);
                 }
             }
             this.stage = 2;
             break;
         case 2:
             if (qName.equals("div") && "right".equals(atts.getValue("class"))) {
                 this.stage = 3;
             }
             break;
         case 3:
             this.stage = "track_container".equals(atts.getValue("class")) ? 4 : 6;
             break;
         case 4:
             if (qName.equals("a")) {
                 this.favorited = atts.getValue("class").contains("favourite_button_selected");
                 this.stage = 5;
             }
             break;
         case 5:
             if (qName.equals("input")) {
                 final boolean email = atts.getValue("checked") != null;
                 this.getCurrentBuilder().favorited(this.favorited ? email ? FavoriteState.FAVORITED_EMAIL : FavoriteState.FAVORITED : FavoriteState.NOT_FAVORITED);
                 this.stage = 6;
             }
             break;
         case 6:
             if (qName.equals("a")) {
                 final String onClick = atts.getValue("onclick");
                 if (onClick != null && onClick.startsWith("Ra")) {
                     this.getCurrentBuilder().ratingToken(onClick.substring(onClick.indexOf('\'') + 1, onClick.lastIndexOf('\'')));
                 }
                 this.stage = 7;
             }
             break;
         case 8:
             if (qName.equals("a")) {
                 this.stage = 9;
             }
             break;
         case 10:
             if (qName.equals("b")) {
                 this.stage = 300;
             }
             break;
         case 301:
             if (qName.equals("span")) {
                 final String title = atts.getValue("title");
                 if (title != null) {
                     this.getCurrentBuilder().totalViewCount(toIntLiberal(title));
                     this.stage = 11;
                 }
             }
             break;
         case 12:
             if (qName.equals("a")) {
                 this.getCurrentBuilder().readLater(atts.getValue("class").contains("read_it_later_selected"));
             }
             this.stage = 14;
             break;
         case 14:
             if (qName.equals("a")) {
                 try {
                     this.getCurrentBuilder().url(new URL("http://www.fimfiction.net" + atts.getValue("href")));
                 } catch (final MalformedURLException e) {
                     throw new SAXException(e);
                 }
                 this.title = new StringBuilder();
                 this.stage = 15;
             }
             break;
         case 16:
             if (qName.equals("a")) {
                 this.authorName = new StringBuilder();
                 this.stage = 17;
             }
             break;
         case 18:
             if (qName.equals("img") && atts.getValue("src").equals("//www.fimfiction-static.net/images/icons/views.png")) {
                 this.stage = 19;
             }
             break;
         case 20:
             if (qName.equals("div") && "description".equals(atts.getValue("class"))) {
                 this.stage = 21;
             }
             break;
         case 21:
             this.stage = "story_image".equals(atts.getValue("class")) ? 22 : 25;
             break;
         case 22:
             try {
                 this.getCurrentBuilder().imageUrl(new URL("http:" + atts.getValue("href")));
             } catch (final MalformedURLException e) {
                 throw new SAXException(e);
             }
             this.stage = 23;
             break;
         case 23:
             try {
                 this.getCurrentBuilder().thumbnailUrl(new URL("http:" + atts.getValue("src")));
             } catch (final MalformedURLException e) {
                 throw new SAXException(e);
             }
             this.stage = 24;
             break;
         case 24:
             if (qName.equals("a")) {
                 this.stage = 25;
             } else {
                 if (this.categories != null) {
                     this.getCurrentBuilder().categories(Collections.unmodifiableSet(this.categories));
                     this.categories = null;
                 }
                 this.description = new StringBuilder();
                 this.stage = 26;
             }
             break;
         case 27:
             this.description.append('<');
             this.description.append(qName);
             for (int i = 0; i < atts.getLength(); i++) {
                 this.description.append(' ');
                 this.description.append(atts.getQName(i));
                 this.description.append("=\"");
                 this.description.append(atts.getValue(i).replace("\"", "\\\""));
                 this.description.append('"');
             }
             this.description.append('>');
             break;
         case 28:
             if (qName.equals("ul")) {
                 this.chapters = new ArrayList<Chapter>();
                 this.stage = 29;
             }
             break;
         case 29:
             if (qName.equals("div")) {
                 final String clazz = atts.getValue("class");
                 if (clazz != null && clazz.contains("chapter_container")) {
                     if (!clazz.contains("chapter_expander")) {
                         this.stage = 100;
                     }
                 }
             }
             if (qName.equals("li") && "save_ordering".equals(atts.getValue("class"))) {
                 this.getCurrentBuilder().chapters(Collections.unmodifiableList(this.chapters));
                 this.getCurrentBuilder().chapterCount(this.chapters.size());
                 this.chapters = null;
                 this.stage = 38;
             }
             break;
         case 100:
             this.stage = 101;
             break;
         case 101:
             this.stage = 30;
             break;
         case 30:
             this.chapter = Chapter.builder();
            if (qName.equals("img")) {
                this.chapter.unread("//www.fimfiction-static.net/images/icons/new.png".equals(atts.getValue("src")));
                 this.stage = 31;
             } else if (qName.equals("a")) {
                 try {
                     this.chapter.url(new URL("http://www.fimfiction.net" + atts.getValue("href")));
                 } catch (final MalformedURLException e) {
                     throw new SAXException(e);
                 }
                 this.chapterTitle = new StringBuilder();
                 this.stage = 32;
             }
             break;
         case 31:
             if (qName.equals("a")) {
                 try {
                     this.chapter.url(new URL("http://www.fimfiction.net" + atts.getValue("href")));
                 } catch (final MalformedURLException e) {
                     throw new SAXException(e);
                 }
                 this.chapterTitle = new StringBuilder();
                 this.stage = 32;
             }
             break;
         case 35:
             this.stage = 36;
             break;
         case 37:
             if (qName.equals("a")) {
                 this.chapter.id(toIntLiberal(atts.getValue("href")));
                 this.chapters.add(this.chapter.build());
                 this.chapter = null;
                 this.stage = 29;
             }
             break;
         case 203:
             if (qName.equals("div")) {
                 this.stage = 204;
             }
             break;
         case 205:
             if (qName.equals("br")) {
                 this.stage = 39;
             }
             break;
         case 39:
             if (qName.equals("span")) {
                 this.stage = 40;
             }
             break;
         case 41:
             if (qName.equals("br")) {
                 this.stage = 42;
             }
             if (qName.equals("a")) {
                 this.stage = 43;
             }
             break;
         case 42:
             if (qName.equals("span")) {
                 this.stage = 43;
             }
             break;
         case 43:
         case 44:
             if (qName.equals("img")) {
                 src = atts.getValue("src");
                 final int i = src.lastIndexOf('.');
                 if (i > 46) {
                     final String id = src.substring(46, i);
                     for (final Character character : Character.values()) {
                         if (id.equals(getImageId(character))) {
                             this.characters.add(character);
                             break;
                         }
                     }
                 }
             }
             if (qName.equals("i")) {
                 this.getCurrentBuilder().characters(Collections.unmodifiableSet(this.characters));
                 this.characters = null;
                 this.endStory();
                 this.stage = 0;
             }
             ;
             break;
         }
     }
     
     @Override
     public void endElement(final String uri, final String localName, final String qName) {
         if (this.stage == 0) {
             return;
         }
         switch (this.stage) {
         case 11:
             if (qName.equals("div")) {
                 this.stage = 12;
             }
             break;
         case 15:
             this.getCurrentBuilder().title(this.title.toString());
             this.stage = 16;
             break;
         case 17:
             this.author.name(this.authorName.toString());
             this.getCurrentBuilder().author(this.author.build());
             this.authorName = null;
             this.stage = 18;
             break;
         case 26:
             this.stage = 27;
             break;
         case 27:
             if (qName.equals("div")) {
                 this.getCurrentBuilder().description(this.description.toString());
                 this.description = null;
                 this.stage = 28;
             } else {
                 this.description.append("</");
                 this.description.append(qName);
                 this.description.append('>');
             }
             break;
         case 32:
             this.chapter.title(this.chapterTitle.toString());
             this.chapterTitle = null;
             this.stage = 33;
             break;
         case 33:
             if (qName.equals("b")) {
                 this.stage = 34;
             }
             break;
         case 36:
             this.stage = 37;
             break;
         case 38:
             if (qName.equals("a")) {
                 this.stage = 200;
             }
             break;
         case 200:
             if (qName.equals("a")) {
                 this.stage = 201;
             }
             break;
         case 201:
             if (qName.equals("a")) {
                 this.stage = 202;
             }
             break;
         case 202:
             if (qName.equals("a")) {
                 this.stage = 203;
             }
             break;
         }
     }
     
     @Override
     public void characters(final char[] ch, final int start, final int length) throws SAXException {
         if (this.stage == 0) {
             return;
         }
         final String asString = new String(ch, start, length).trim();
         if (asString.isEmpty()) {
             return;
         }
         switch (this.stage) {
         case 7:
             this.getCurrentBuilder().likeCount(toIntLiberal(asString));
             this.stage = 8;
             break;
         case 9:
             this.getCurrentBuilder().dislikeCount(toIntLiberal(asString));
             this.stage = 10;
             break;
         case 300:
             this.getCurrentBuilder().commentCount(toIntLiberal(asString));
             this.stage = 301;
             break;
         case 15:
             this.title.append(asString);
             break;
         case 17:
             this.authorName.append(asString);
             break;
         case 19:
             this.getCurrentBuilder().maximumChapterViewCount(toIntLiberal(asString.substring(0, asString.indexOf('('))));
             this.getCurrentBuilder().totalViewCount(toIntLiberal(asString.substring(asString.indexOf('('))));
             this.stage = 20;
             break;
         case 25:
             if (this.categories == null) {
                 this.categories = EnumSet.noneOf(Category.class);
             }
             for (final Category category : Category.values()) {
                 if (getDisplayName(category).equals(asString)) {
                     this.categories.add(category);
                     break;
                 }
             }
             this.stage = 24;
             break;
         case 27:
             this.description.append(asString);
             break;
         case 32:
             this.chapterTitle.append(asString);
             break;
         case 34:
             this.chapter.modificationDate(this.parseDate(asString));
             this.stage = 35;
             break;
         case 36:
             final int c = toIntLiberal(asString, -1);
             if (c >= -1) {
                 this.chapter.wordCount(c);
             }
             break;
         case 203:
             if (asString.equals("Complete")) {
                 this.getCurrentBuilder().status(Status.COMPLETE);
             } else if (asString.equals("Incomplete")) {
                 this.getCurrentBuilder().status(Status.INCOMPLETE);
             } else if (asString.equals("Cancelled")) {
                 this.getCurrentBuilder().status(Status.CANCELLED);
             } else if (asString.equals("On Hiatus")) {
                 this.getCurrentBuilder().status(Status.ON_HIATUS);
             } else if (asString.equals("Everyone")) {
                 this.getCurrentBuilder().contentRating(ContentRating.EVERYONE);
             } else if (asString.equals("Teen")) {
                 this.getCurrentBuilder().contentRating(ContentRating.TEEN);
             } else if (asString.equals("Mature")) {
                 this.getCurrentBuilder().contentRating(ContentRating.MATURE);
             } else if (asString.equals("Sex")) {
                 this.getCurrentBuilder().sex(true);
             } else if (asString.equals("Gore")) {
                 this.getCurrentBuilder().gore(true);
             }
             break;
         case 204:
             final int words = toIntLiberal(asString, -1);
             if (words >= 0) {
                 this.getCurrentBuilder().wordCount(words);
                 this.stage = 205;
             }
             break;
         case 40:
             this.getCurrentBuilder().firstPostedDate(this.parseDate(asString));
             this.characters = EnumSet.noneOf(Character.class);
             this.stage = 41;
             break;
         case 43:
             this.getCurrentBuilder().modificationDate(this.parseDate(asString));
             this.stage = 44;
             break;
         }
     }
     
     private long parseDate(final String date) throws SAXException {
         try {
             return this.fimfictionDateFormat.parse(date.replaceAll("(st|nd|rd|th)", "")).getTime();
         } catch (final ParseException e) {
             return 0L;
         }
     }
     
     private static int toIntLiberal(final String toInt) {
         return toIntLiberal(toInt, 0);
     }
     
     private static int toIntLiberal(final String toInt, final int defaultValue) {
         if (toInt != null) {
             int result = 0;
             boolean modified = false;
             for (int i = 0, l = toInt.length(); i < l; i++) {
                 final char c = toInt.charAt(i);
                 final int value = c - '0';
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
     
     private static String getDisplayName(final Category category) {
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
     
     private static String getImageId(final Character character) {
         switch (character) {
         case CUTIE_MARK_CRUSADERS:
             return "cmc";
         case PRINCESS_CELESTIA:
             return "celestia";
         case FLIM_AND_FLAM:
             return "flimflamicon";
         case CRANKY_DOODLE_DONKEY:
             return "cranky doodle icon";
         case MATILDA:
             return "matilda icon";
         case MR_CAKE:
             return "mr cake icon";
         case MRS_CAKE:
             return "mrs cake icon";
         case IRON_WILL:
             return "ironwillicon";
         case PRINCESS_CADENCE:
             return "Cadence";
         case SHINING_ARMOR:
             return "shining-armor";
         case QUEEN_CHRYSALIS:
             return "queen-chrysalis";
         case KING_SOMBRA:
             return "king-sombra";
         case BIG_MACINTOSH:
             return "big_mac";
         case BONBON:
             return "bon_bon";
         case DJ_P0N3:
             return "dj_pon3";
         case DINKY_HOOVES:
             return "dinkyicon";
         case NURSE_REDHEART:
             return "nurse_red_heart";
         case ORIGINAL_CHARACTER:
             return "oc";
         case SPARKLER:
             return "hZQ9cvj";
         case TWILIGHT_SPARKLE:
             return "twilight_sparkle";
         case RAINBOW_DASH:
             return "rainbow_dash";
         case PINKIE_PIE:
             return "pinkie_pie";
         case APPLEJACK:
             return "applejack";
         case RARITY:
             return "rarity";
         case FLUTTERSHY:
             return "fluttershy";
         case SPIKE:
             return "spike";
         case MAIN_6:
             return "main_6";
         case TWILICORN:
             return "twilicorn";
         case APPLE_BLOOM:
             return "apple_bloom";
         case SCOOTALOO:
             return "scootaloo";
         case SWEETIE_BELLE:
             return "sweetie_belle";
         case BABS_SEED:
             return "babs_seed";
         case PRINCESS_LUNA:
             return "princess_luna";
         case NIGHTMARE_MOON:
             return "nightmare_moon";
         case GILDA:
             return "gilda";
         case ZECORA:
             return "zecora";
         case TRIXIE:
             return "trixie";
         case CHERILEE:
             return "cherilee";
         case THE_MAYOR:
             return "the_mayor";
         case HOITY_TOITY:
             return "hoity_toity";
         case PHOTO_FINISH:
             return "photo_finish";
         case SAPPHIRE_SHORES:
             return "sapphire_shores";
         case SPITFIRE:
             return "spitfire";
         case SOARIN:
             return "soarin";
         case PRINCE_BLUEBLOOD:
             return "prince_blueblood";
         case LITTLE_STRONGHEART:
             return "little_strongheart";
         case DISCORD:
             return "discord";
         case MARE_DO_WELL:
             return "mare_do_well";
         case FANCYPANTS:
             return "fancypants";
         case DARING_DO:
             return "daring_do";
         case WONDERBOLTS:
             return "wonderbolts";
         case DIAMOND_DOGS:
             return "diamond_dogs";
         case CRYSTAL_PONIES:
             return "crystal_ponies";
         case LIGHTNING_DUST:
             return "lightning_dust";
         case SUNSET_SHIMMER:
             return "sunset_shimmer";
         case PIE_SISTERS:
             return "pie_sisters";
         case CHERRY_JUBILEE:
             return "cherry_jubilee";
         case CAKE_TWINS:
             return "cake_twins";
         case FLASH_SENTRY:
             return "flash_sentry";
         case GRANNY_SMITH:
             return "granny_smith";
         case BRAEBURN:
             return "braeburn";
         case DIAMOND_TIARA:
             return "diamond_tiara";
         case SILVER_SPOON:
             return "silver_spoon";
         case TWIST:
             return "twist";
         case SNIPS:
             return "snips";
         case SNAILS:
             return "snails";
         case PIPSQUEAK:
             return "pipsqueak";
         case FEATHERWEIGHT:
             return "featherweight";
         case ANGEL:
             return "angel";
         case WINONA:
             return "winona";
         case OPALESCENCE:
             return "opalescence";
         case GUMMY:
             return "gummy";
         case OWLOWISCIOUS:
             return "owlowiscious";
         case PHILOMENA:
             return "philomena";
         case TANK:
             return "tank";
         case DERPY_HOOVES:
             return "derpy_hooves";
         case LYRA:
             return "lyra";
         case CARAMEL:
             return "caramel";
         case DOCTOR_WHOOVES:
             return "doctor_whooves";
         case OCTAVIA:
             return "octavia";
         case BERRY_PUNCH:
             return "berry_punch";
         case CARROT_TOP:
             return "carrot_top";
         case FLEUR_DE_LIS:
             return "fleur_de_lis";
         case COLGATE:
             return "colgateicon";
         case THUNDERLANE:
             return "thunderlane";
         case FLITTER_AND_CLOUDCHASER:
             return "flitter_and_cloudchaser";
         case RUMBLE:
             return "rumble";
         case ROSELUCK:
             return "roseluck";
         case CHANGELINGS:
             return "changelings";
         case NOTEWORTHY:
             return "noteworthy";
         case FLOWER_PONIES:
             return "flower_ponies";
         case RAINDROPS:
             return "raindrops";
         case CLOUDKICKER:
             return "cloudkicker";
         case SPA_PONIES:
             return "spa_ponies";
         case OTHER:
             return "other";
         default:
             throw new IllegalArgumentException(character.name());
         }
     }
 }
