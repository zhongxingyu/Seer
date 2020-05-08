 package de.everald.extdirectspring.objects;
 
 import java.util.Date;
 
 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;
 import ch.ralscha.extdirectspring.generator.Model;
 import ch.ralscha.extdirectspring.generator.ModelField;
 import ch.ralscha.extdirectspring.generator.ModelType;
 
 @Data
 @AllArgsConstructor
 @NoArgsConstructor
 @Model(value = "sample.model.GridObject", readMethod = "extdirectService.getList", paging = true)
 public class GridObject {
 	private Integer	itemKey;
 	private String	name;
	@ModelField(dateFormat="time",type=ModelType.DATE)
 	private Date	timestamp;
 }
