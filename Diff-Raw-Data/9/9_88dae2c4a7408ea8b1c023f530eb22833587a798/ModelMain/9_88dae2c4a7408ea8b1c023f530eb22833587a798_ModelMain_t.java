 package ch.ffhs.inf09.pa.mashup_platform.var.portrait_of_italian_cars.model;
 
 import ch.ffhs.inf09.pa.mashup_platform.common.util.ExceptionMP;
 import ch.ffhs.inf09.pa.mashup_platform.config.Config;
 import ch.ffhs.inf09.pa.mashup_platform.core.system.config.ConfigMashup;
 import ch.ffhs.inf09.pa.mashup_platform.core.system.model.Content;
 import ch.ffhs.inf09.pa.mashup_platform.core.system.model.Model;
 import ch.ffhs.inf09.pa.mashup_platform.core.system.model.db.DB;
 import ch.ffhs.inf09.pa.mashup_platform.var.portrait_of_italian_cars.model.db.DBPortraitOfItalianCars;
 
 /**
  * The model class represents the example mashup "Portrait Of Italian Cars".
  * 
  * @author Joël
  * 
  */
 public class ModelMain extends Model {
 	public ModelMain(ConfigMashup config) {
 		super(config);
 	}
 
 	public void setPageNr(int pagenr) throws ExceptionMP {
 		super.setPageNr(pagenr);
 		int start = pagenr * numberRecordsPerPage;
 		Content content = new Content();
 		content.setCaption(page.getName());
 		String filepath = Config.getFilepathVar()
				+ "/portrait_of_italian_cars/config/db/DBPortraitOfItalianCars.properties";
 		DB db = new DBPortraitOfItalianCars(filepath);
 		db.fillIn(content, start, numberRecordsPerPage);
 		page.setContent(content);
 	}
 }
