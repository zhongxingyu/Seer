 package ru.spbau.WhereIsMyMoney.storage;
 
 import ru.spbau.WhereIsMyMoney.parser.Template;
import android.content.Context;
 
 public class TemplatesSource extends BaseDataSource {
	public TemplatesSource(Context context) {
		super(new TemplatesHelper(context));
 	}
 	
 	public void addTemplate(Template template) {
 		TemplatesHelper.addTemplate(template, getDatabase());
 	}
 }
