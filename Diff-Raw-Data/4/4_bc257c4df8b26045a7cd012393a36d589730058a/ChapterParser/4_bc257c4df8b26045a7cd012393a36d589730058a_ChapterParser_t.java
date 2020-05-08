 package at.yawk.fimfiction.json;
 
 import static at.yawk.fimfiction.json.Util.getInt;
 import static at.yawk.fimfiction.json.Util.getLong;
 import static at.yawk.fimfiction.json.Util.getString;
 import at.yawk.fimfiction.Chapter;
 import at.yawk.fimfiction.Chapter.ChapterBuilder;
 
 import com.google.gson.JsonObject;
 
 /**
  * {@link IJsonParser} for {@link Chapter}s, used by {@link StoryParser}.
  * 
  * @author Yawkat
  */
 public class ChapterParser implements IJsonParser<Chapter> {
     @Override
     public final Chapter parse(final JsonObject object) {
         final ChapterBuilder builder = Chapter.builder();
         this.parseInto(object, builder);
         return builder.build();
     }
     
     /**
      * Parse the given {@link JsonObject} into the given {@link ChapterBuilder}.
      * Can be overridden for more extensive implementations.
      */
     protected void parseInto(final JsonObject parse, final ChapterBuilder builder) {
         builder.id(getInt(parse, "id"));
        // Sometimes null is returned as the title in a json chapter result.
        // This is a bug in the website.
        builder.title(parse.get("title").isJsonNull() ? "" : getString(parse, "title"));
         builder.wordCount(getInt(parse, "words"));
         builder.viewCount(getInt(parse, "views"));
         builder.modificationDate(getLong(parse, "date_modified") * 1000L);
     }
     
     @Override
     public void reset() {}
 }
