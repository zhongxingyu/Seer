 package model.data.xml.writers;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collection;
 
 import model.*;
 import model.data.exceptions.DataSaveException;
 import model.data.exceptions.RecordNotFoundException;
 
 import org.apache.ecs.Doctype;
 import org.apache.ecs.xml.XML;
 import org.apache.ecs.xml.XMLDocument;
 
 /**
  * VideoWriter.java
  * 
  * @author Christopher Bertels (chbertel@uos.de)
  * @date 15.09.2008
  */
 public class VideoWriter extends AbstractWriter
 {
 	public VideoWriter(String videosFile) throws DataSaveException,
 			FileNotFoundException
 	{
 		super(videosFile);
 	}
 
 	public void saveVideos(Collection<Video> videosToSave) throws IOException,
 			RecordNotFoundException
 	{
 		XMLDocument document = new XMLDocument();
 		document.addToProlog(new Doctype("videos", "'videoList'", "'videos.dtd'"));
 		
 		XML videosTag = new XML("videos");
		videosTag.addXMLAttribute("minID", Integer.toString(Video.getMinID()));
 		videosTag.addXMLAttribute("minVideoUnitID", Integer.toString(VideoUnit.getMinID()));
 
 		document.addElement(videosTag);
 
 		for (Video vid : videosToSave)
 		{
 			XML videoTag = new XML("video")
 				.addXMLAttribute("vID", Integer.toString(vid.getID()))
 				.addXMLAttribute("title", vid.getTitle())
 				.addXMLAttribute("releaseYear", Integer.toString(vid.getReleaseYear()))
 				.addXMLAttribute("priceCategoryID", Integer.toString(vid.getPriceCategory().getID()))
 				.addXMLAttribute("ratedAge", Integer.toString(vid.getRatedAge()));
 			
 			for(VideoUnit unit : vid.getVideoUnits())
 			{
 				videoTag.addElement(new XML("videoUnit")
 					.addXMLAttribute("uID", Integer.toString(unit.getID()))
 					.addXMLAttribute("videoID", Integer.toString(vid.getID()))
 				);				
 			}
 			
 			document.addElement(videoTag);
 		}
 
 		writeToFile(document);
 	}
 }
