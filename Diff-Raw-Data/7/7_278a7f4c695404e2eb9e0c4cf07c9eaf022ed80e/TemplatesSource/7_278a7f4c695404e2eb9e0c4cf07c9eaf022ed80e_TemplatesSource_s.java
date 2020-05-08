 package ru.spbau.WhereIsMyMoney.storage;
 
 import ru.spbau.WhereIsMyMoney.parser.Template;
import android.database.sqlite.SQLiteOpenHelper;
 
 public class TemplatesSource extends BaseDataSource {
	public TemplatesSource(SQLiteOpenHelper helper) {
		super(helper);
 	}
 	
 	public void addTemplate(Template template) {
 		TemplatesHelper.addTemplate(template, getDatabase());
 	}
 }
