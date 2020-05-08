 package org.atlasapi.media.entity.simple;
 
 import java.lang.reflect.Constructor;
 import java.util.Map;
 
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.EntityType;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableMap;
 
 public abstract class ContentIdentifier {
 
     protected String uri;
     protected String type;
     protected String id;
 
     public ContentIdentifier() {
     }
 
     public ContentIdentifier(String uri, String type) {
         this(uri, type, null);
     }
     
     public ContentIdentifier(String uri, String type, String id) {
         this.uri = uri;
         this.type = type;
         this.id = id;
     }
     
     public void setId(String id) {
         this.id = id;
     }
     
     public String getId() {
         return id;
     }
     
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getType() {
         return type;
     }
 
     public abstract ContentIdentifier copy();
 
     public static class ItemIdentifier extends ContentIdentifier {
 
         public ItemIdentifier() {
         }
 
         public ItemIdentifier(String uri) {
             this(uri, null);
         }
         
         public ItemIdentifier(String uri, String id) {
             super(uri, EntityType.ITEM.toString(), id);
         }
 
         @Override
         public ItemIdentifier copy() {
             return new ItemIdentifier(uri, id);
         }
     }
 
     public static class EpisodeIdentifier extends ContentIdentifier {
         
         public EpisodeIdentifier() {
         }
 
         public EpisodeIdentifier(String uri) {
             this(uri, null);
         }
         
         public EpisodeIdentifier(String uri, String id) {
             super(uri, EntityType.EPISODE.toString(), id);
         }
 
         @Override
         public EpisodeIdentifier copy() {
             return new EpisodeIdentifier(uri, id);
         }
     }
 
     public static class FilmIdentifier extends ContentIdentifier {
         
         public FilmIdentifier() {
         }
 
         public FilmIdentifier(String uri) {
             this(uri, null);
         }
         
         public FilmIdentifier(String uri, String id) {
             super(uri, EntityType.FILM.toString(), id);
         }
 
         @Override
         public FilmIdentifier copy() {
             return new FilmIdentifier(uri, id);
         }
     }
 
     public static class BrandIdentifier extends ContentIdentifier {
         
         public BrandIdentifier() {
         }
 
         public BrandIdentifier(String uri) {
             this(uri, null);
         }
         
         public BrandIdentifier(String uri, String id) {
             super(uri, EntityType.BRAND.toString(), id);
         }
 
         @Override
         public BrandIdentifier copy() {
             return new BrandIdentifier(uri, id);
         }
     }
 
     public static class SeriesIdentifier extends ContentIdentifier {
         
         public SeriesIdentifier() {
         }
 
         public SeriesIdentifier(String uri) {
             this(uri, null);
         }
         
         public SeriesIdentifier(String uri, String id) {
             super(uri, EntityType.SERIES.toString(), id);
         }
 
         @Override
         public SeriesIdentifier copy() {
             return new SeriesIdentifier(uri, id);
         }
     }
 
     public static class PersonIdentifier extends ContentIdentifier {
         
         public PersonIdentifier() {
         }
 
         public PersonIdentifier(String uri) {
             this(uri, null);
         }
         
         public PersonIdentifier(String uri, String id) {
             super(uri, EntityType.PERSON.toString(), id);
         }
 
         @Override
         public PersonIdentifier copy() {
             return new PersonIdentifier(uri, id);
         }
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this).add("id", id).add("uri", uri).add("type", type).toString();
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         } else if (obj instanceof ContentIdentifier) {
             return Objects.equal(this.uri, ((ContentIdentifier)obj).uri) && Objects.equal(this.type, ((ContentIdentifier)obj).uri);
         } else {
             return false;
         }
     }
     
     @Override
     public int hashCode() {
         return Objects.hashCode(uri);
     }
 
     private static Map<EntityType, Class<? extends ContentIdentifier>> typeMap = ImmutableMap.<EntityType, Class<? extends ContentIdentifier>> builder()
             .put(EntityType.PERSON, PersonIdentifier.class)
             .put(EntityType.SERIES, SeriesIdentifier.class)
             .put(EntityType.BRAND, BrandIdentifier.class)
             .put(EntityType.ITEM, ItemIdentifier.class)
             .put(EntityType.EPISODE, EpisodeIdentifier.class)
             .put(EntityType.FILM, FilmIdentifier.class)
     .build();
 
     public static ContentIdentifier identifierFor(Described described) {
         return create(EntityType.from(described), described.getCanonicalUri());
     }
 
 	private static ContentIdentifier create(EntityType type, String uri) {
 		try {
             Class<? extends ContentIdentifier> idType = typeMap.get(type);
             Constructor<? extends ContentIdentifier> constructor = idType.getConstructor(String.class);
             return constructor.newInstance(uri);
         } catch (Exception e) {
             throw new RuntimeException("Can't create content identifier for " + uri);
         }
 	}
     
     public static ContentIdentifier identifierFor(ChildRef childRef) {
         return create(childRef.getType(), childRef.getUri());
     }
 
     public static ContentIdentifier identifierFrom(String canonicalUri, String type) {
         try {
             Class<? extends ContentIdentifier> idType = typeMap.get(EntityType.from(type));
             Constructor<? extends ContentIdentifier> constructor = idType.getConstructor(String.class);
             ContentIdentifier newInstance = constructor.newInstance(canonicalUri);
             return newInstance;
         } catch (Exception e) {
             throw new RuntimeException("Can't create content identifier for " + canonicalUri);
         }
     }
     
     public static final Function<ContentIdentifier, String> TO_ID = new Function<ContentIdentifier, String>() {
         @Override
         public String apply(ContentIdentifier input) {
             return input.getId();
         }
     };
    
    public static final Function<ContentIdentifier, ContentIdentifier> COPY = new Function<ContentIdentifier, ContentIdentifier>() {
        @Override
        public ContentIdentifier apply(ContentIdentifier input) {
            return input.copy();
        }
    };
 }
