 package org.mitre.medcafe.restlet;
 
 import org.json.JSONObject ;
 import org.mitre.medcafe.util.Repository;
 import org.mitre.medcafe.util.WebUtils;
 
 import com.google.gson.*;
 import org.projecthdata.hdata.schemas._2009._06.patient_information.Patient;
 import java.io.IOException;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Put;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 public class PatientResource extends ServerResource {
 
     /** The underlying Item object. */
     //Patient item;
 
     /** The sequence of characters that identifies the resource. */
     String id;
     String repository;
 
     @Override
     protected void doInit() throws ResourceException {
         // Get the "id" attribute value taken from the URI template
         // /items/{id}.
         this.id = (String) getRequest().getAttributes().get("id");
         this.repository = (String) getRequest().getAttributes().get("repository");
         // Get the item directly from the "persistence layer".
         //this.item = getItems().get(id);
         /* System.out.println("Found PatientResource");
         for(Variant v : getVariants())
         {
             System.out.println(String.valueOf(v));
         } */
 
         //setExisting(this.item != null);
     }
 
     /*
     @Get("html")
     public Representation toHtml(){
 
     	StringBuffer buf = new StringBuffer();
     	StringBuffer endBuf = new StringBuffer();
 
     	buf.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\"example" + this.id +"\">");
     	buf.append("");
 
     	String[] titles = new String[]{"Patient ID","Name", "Address", "Phone numbers", "Gender",
     									"Languages", "Birthdate", "Maritial Status","Race","Guardian", "Birth place" };
 
     	String[] values = new String[]{this.id,"", "", " ", "",
 				"", "", " ","","", "" };
 
     	StringBuffer patientData = new StringBuffer();
     	patientData.append("<thead><tr><th></th><th></th></tr></thead>");
     	patientData.append("<tbody>");
     	int i=0;
     	for (String title:titles)
     	{
     		patientData.append("<tr class=\"gradeX\"><td>" + title + "</td><td>" + values[i]+ "</td></tr>" );
     		i++;
     	}
 
     	endBuf.append("</tbody></table>");
     	return new StringRepresentation( buf.toString() + patientData.toString()
                  + endBuf.toString());
 
     }
     */
 
     @Get("json")
     public JsonRepresentation toJson(){
         Repository r = Repositories.getRepository( repository );
        if (r == null)
        {
        	 System.out.println("PatientResource : toJSON: Cannot find the repository " + repository);
        	 return new JsonRepresentation(WebUtils.buildErrorJson( "Could not find the repository " + repository ));
        }
         Patient pat = r.getPatient( id );
     //convert to JSON
        try{
         System.out.println(WebUtils.bundleJsonResponse("patient_data",pat,repository,id).getText());
         }
         catch (IOException IOe)
         {
         	System.out.println("Couldn't print");
         } 
 
         //convert to JSON
         return WebUtils.bundleJsonResponse( "patient_data", pat, repository, id );
     }
 }
