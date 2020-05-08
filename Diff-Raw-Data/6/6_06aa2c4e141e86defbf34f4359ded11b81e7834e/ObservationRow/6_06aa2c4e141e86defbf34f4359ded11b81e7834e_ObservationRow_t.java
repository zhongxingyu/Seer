 package edu.ucla.cens.budburst.models;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 
 import edu.ucla.cens.budburst.Budburst;
 import edu.ucla.cens.budburst.data.SyncableRow;
 
 public class ObservationRow extends SyncableRow {
 
 	public Long species_id;
 	public Long site_id;
 	public Long phenophase_id;
 	public Long image_id;
 	public String note;
 	public String time;  // YYYY-MM-DD
 
 	@Override
 	public ArrayList<String> primaryKeys() {
 		ArrayList<String> ret = new ArrayList<String>();
 		ret.add("species_id");
 		ret.add("site_id");
 		ret.add("phenophase_id");
 		return ret;
 	}
 
 	public String getImagePath() {
 		return Budburst.OBSERVATION_PATH + image_id + ".jpg";
 	}
 
 	@Override
 	public MultipartEntity uploadEntity() {
 		MultipartEntity entity = super.uploadEntity();
 
 		File file = new File(getImagePath());
		if(file.exists()) {
			FileBody fileb = new FileBody(file, "image/jpeg");
			entity.addPart("image", fileb);
		}
 
 		return entity;
 	}
 
 	public boolean hasImage() {
 		return image_id != null && image_id != 0;
 	}
 
 }
