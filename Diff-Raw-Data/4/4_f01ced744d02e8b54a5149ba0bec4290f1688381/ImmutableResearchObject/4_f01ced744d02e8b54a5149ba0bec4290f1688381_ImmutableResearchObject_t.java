 package pl.psnc.dl.wf4ever.model.ROEVO;
 
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.evo.EvoType;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.EvoBuilder;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.Manifest;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 
 import com.hp.hpl.jena.query.Dataset;
 
 /**
  * An immutable a research object, i.e. a snapshot or an archive.
  * 
  * The immutable research object can be compared to other immutable research objects, which is implemented by comparing
  * the snapshot/archive dates.
  * 
  * @author piotrekhol
  * 
  */
 public class ImmutableResearchObject extends ResearchObject implements Comparable<ImmutableResearchObject> {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(ImmutableResearchObject.class);
 
     private ImmutableEvoInfo evoInfo;
 
 
     /**
      * Constructor.
      * 
      * @param user
      *            user creating the instance
      * @param dataset
      *            custom dataset
      * @param useTransactions
      *            should transactions be used. Note that not using transactions on a dataset which already uses
      *            transactions may make it unreadable.
      * @param uri
      *            RO URI
      */
     public ImmutableResearchObject(UserMetadata user, Dataset dataset, boolean useTransactions, URI uri) {
         super(user, dataset, useTransactions, uri);
     }
 
 
     /**
      * Create a new immutable research object as a copy of a live one. Copies all aggregated resources, changes URIs in
      * annotation bodies.
      * 
      * @param uri
      *            URI of the copy
      * @param researchObject
      *            live research object
      * @param builder
      *            model instance builder
     * @param evoType
     *            evolution type
      * @return the new research object
      */
     public static ImmutableResearchObject create(URI uri, ResearchObject researchObject, Builder builder,
             EvoType evoType) {
         if (ResearchObject.get(builder, uri) != null) {
             throw new ConflictException("Research Object already exists: " + uri);
         }
         ImmutableResearchObject immutableResearchObject = builder.buildImmutableResearchObject(uri,
             researchObject.getCreator(), researchObject.getCreated());
         immutableResearchObject.setCopyDateTime(DateTime.now());
         immutableResearchObject.setCopyAuthor(builder.getUser());
         immutableResearchObject.setCopyOf(researchObject);
         EvoBuilder evoBuilder = EvoBuilder.get(evoType);
         immutableResearchObject.copy(researchObject.getManifest(), evoBuilder);
         immutableResearchObject.save(evoType);
         // copy the ro:Resources
         for (pl.psnc.dl.wf4ever.model.RO.Resource resource : researchObject.getResources().values()) {
             try {
                 immutableResearchObject.copy(resource, evoBuilder);
             } catch (BadRequestException e) {
                 LOGGER.warn("Failed to copy the resource", e);
             }
         }
         //copy the annotations
         for (Annotation annotation : researchObject.getAnnotations().values()) {
             try {
                 immutableResearchObject.copy(annotation, evoBuilder);
             } catch (BadRequestException e) {
                 LOGGER.warn("Failed to copy the annotation", e);
             }
         }
         //copy the folders
         for (Folder folder : researchObject.getFolders().values()) {
             immutableResearchObject.copy(folder, evoBuilder);
         }
         return immutableResearchObject;
     }
 
 
     /**
      * Get a Research Object if it exists.
      * 
      * @param builder
      *            model instance builder
      * @param uri
      *            uri
      * @return an existing Research Object or null
      */
     public static ImmutableResearchObject get(Builder builder, URI uri) {
         ImmutableResearchObject researchObject = builder.buildImmutableResearchObject(uri);
         if (!researchObject.getManifest().isNamedGraph()) {
             return null;
         }
         return researchObject;
     }
 
 
     public EvoType getEvoType() {
         return getEvoInfo().getEvoType();
     }
 
 
     public ResearchObject getLiveRO() {
         return (ResearchObject) getCopyOf();
     }
 
 
     public boolean isFinalized() {
         return getImmutableEvoInfo().isFinalized();
     }
 
 
     public void setFinalized(boolean finalized) {
         getImmutableEvoInfo().setFinalized(finalized);
     }
 
 
     public ImmutableEvoInfo getImmutableEvoInfo() {
         if (evoInfo == null) {
             evoInfo = ImmutableEvoInfo.get(builder, getFixedEvolutionAnnotationBodyUri(), this);
             if (evoInfo != null) {
                 evoInfo.load();
             }
         }
         return evoInfo;
     }
 
 
     @Override
     public EvoInfo getEvoInfo() {
         return getImmutableEvoInfo();
     }
 
 
     /**
      * Generate new evolution information, including the evolution annotation. This method will change the live RO evo
      * info only if this property is set (it isn't for not-finalized snapshots/archives).
      * 
      * @param evoType
      *            snapshot or archive
      */
     @Override
     public void createEvoInfo(EvoType evoType) {
         try {
             evoInfo = ImmutableEvoInfo.create(builder, getFixedEvolutionAnnotationBodyUri(), this, evoType);
 
             this.evoInfoAnnotation = annotate(evoInfo.getUri(), this);
             this.getManifest().serialize();
         } catch (BadRequestException e) {
             LOGGER.error("Failed to create the evo info annotation", e);
         }
     }
 
 
     public void copy(Manifest manifest, EvoBuilder evoBuilder) {
         manifest = manifest.copy(builder, this);
     }
 
 
     @Override
     public int compareTo(ImmutableResearchObject o) {
         if (o == null || o.getCopyDateTime() == null) {
             return 1;
         }
         if (getCopyDateTime() == null) {
             return -1;
         }
         return getCopyDateTime().compareTo(o.getCopyDateTime());
     }
 
 
     @Override
     public DateTime getCopyDateTime() {
         if (super.getCopyDateTime() == null) {
             getImmutableEvoInfo();
         }
         return super.getCopyDateTime();
     }
 
 }
