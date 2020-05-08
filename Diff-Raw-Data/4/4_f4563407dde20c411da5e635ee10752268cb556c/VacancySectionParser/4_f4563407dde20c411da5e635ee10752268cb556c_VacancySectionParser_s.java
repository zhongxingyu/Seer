 package ru.xrm.app.parsers;
 
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import ru.xrm.app.config.Config;
 import ru.xrm.app.config.Entry;
 import ru.xrm.app.domain.Section;
 import ru.xrm.app.misc.SectionSet;
 
 public class VacancySectionParser {
 
 	Config config;
 	String html;
 	Section sections;
 	
 	public VacancySectionParser(Config config, String html){
 		this.config = config;
 		this.html = html;
 	}
 	
 	public List<Section> parse() {
 		SectionSet sections=SectionSet.getInstance();
 		//List<Section> sections = new ArrayList<Section>();
 	
 		List<Entry> vacancySectionProperties=config.getVacancySectionProperties();
 			
 		Document doc = Jsoup.parse(html);
 		for (Entry prop:vacancySectionProperties){
 			// here we got multiple elements for each vacancySectionProperty
 			Elements elems=doc.select(prop.getCssQuery());
 			Object value="";
 			int idx=0;
 			for (Element e:elems){
 
 				if (prop.getElementWalker() != null){
 					e=prop.getElementWalker().walk(e);
 				}
 				if (prop.getElementEvaluator() !=null){
 					value=prop.getElementEvaluator().evaluate(e);
 				}
 				if (prop.getPropertyTransformer() !=null){
 					value = prop.getPropertyTransformer().transform(value.toString());
 				}
 				
 				Section section;
 				
 				// if object of some index does not exists, create it and add to result
 				try{
					section=sections.getByIndex(idx);
				}catch(IndexOutOfBoundsException e1){
 					section=new Section();
 					sections.add(section);
 				}			
 				
 				// fill property for current object
 				try {
 					section.setProperty(prop.getKey(), value);
 				} catch (SecurityException e1) {
 					e1.printStackTrace();
 				} catch (IllegalArgumentException e1) {
 					e1.printStackTrace();
 				} catch (NoSuchFieldException e1) {
 					// configuration exception
 					e1.printStackTrace();
 				} catch (IllegalAccessException e1) {
 					e1.printStackTrace();
 				}
 				
 				idx++;
 			}
 		}
 		return sections.getSections();
 	}
 }
