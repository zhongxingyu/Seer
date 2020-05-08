 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.SourceListJsonRepresentation;
 import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
 import org.restlet.Context;
 import org.restlet.resource.Variant;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.Representation;
 import org.restlet.data.*;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.util.List;
 
 /**
  * @author Jalpa Patel
  */
 public class SourcesResource  extends AbstractCollectionResource<Source> {
     private SourceDao sourceDao;
     private StudyCalendarXmlCollectionSerializer<Source> xmlSerializer;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         getVariants().add(new Variant(MediaType.APPLICATION_JSON));
     }
 
     public List<Source> getAllObjects() throws ResourceException {
         return sourceDao.getAll();
     }
 
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
             return new SourceListJsonRepresentation(getAllObjects());
         } else {
             return super.represent(variant);
         }
     }
 
     public StudyCalendarXmlCollectionSerializer<Source> getXmlSerializer() {
         return xmlSerializer;
     }
 
     @Required
     public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Source> xmlSerializer) {
         this.xmlSerializer = xmlSerializer;
     }
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 }
