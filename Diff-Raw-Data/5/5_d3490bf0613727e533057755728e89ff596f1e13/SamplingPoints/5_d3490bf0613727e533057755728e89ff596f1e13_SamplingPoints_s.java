 package de.hswt.hrm.inspection.ui.grid;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.swt.widgets.Display;
 
 import com.google.common.base.Throwables;
 
 import de.hswt.hrm.common.BundleUtil;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.inspection.model.SamplingPointType;
 import de.hswt.hrm.scheme.model.RenderedComponent;
 import de.hswt.hrm.scheme.service.ComponentConverter;
 
 public class SamplingPoints {
 	
 	private static final String[] DIRS = new String[]
 			{
 				"r",
 				"l",
 				"d",
 				"u"
 			};
 	private final RenderedComponent[] airMeasurement;
 	private final RenderedComponent[] waterAnalysis;
 	private final RenderedComponent[] contactCulture;
 	private final RenderedComponent[] photo;
 	private final RenderedComponent[] climateParameter;
 	private final RenderedComponent[] dustConcentration;
 	
 	private final Category c1;
 	private final Category c2;
 	
 	private final Display display;
 	
 	public SamplingPoints(Display display){
 		this.display = display;
 		c1 = getSamplePointCategory(2);
 		c2 = getSamplePointCategory(1);
 		try{
 			airMeasurement = getComponents("pl");
 			waterAnalysis = getComponents("pw");
 			contactCulture = getComponents("pkk");
 			photo = getComponents("pf");
 			climateParameter = getComponents("pkp");
 			dustConcentration = getComponents("ps");
 		}catch(Exception e){
 			throw Throwables.propagate(e);
 		}
 		
 	}
 	
 	public RenderedComponent getRenderedComponent(SamplingPointType type, boolean isEven){
 		RenderedComponent comp;
		int index = (isEven ? 1 : 0);
 		switch(type){
 		case airMeasurement:
 			comp = airMeasurement[index];
 			break;
 		case waterAnalysis:
 			comp = waterAnalysis[index];
 			break;
 		case contactCulture:
 			comp = contactCulture[index];
 			break;
 		case photo:
 			comp = photo[index];
 			break;
 		case climateParameter:
 			comp = climateParameter[index];
 			break;
 		case dustConcentration:
 			comp = dustConcentration[index];
 			break;
 		default:
 			throw new RuntimeException("Unknown SamplingPointType");
 		}
 		return comp;
 	}
 	
 	private byte[] getBytes(String fname) throws IOException{
 		try(InputStream in = BundleUtil.getStreamForFile(SamplingPoints.class, "resources/"+fname+".pdf")){
 			byte[] data = new byte[in.available()];
 			in.read(data);
 			return data;
 		}
 	}
 	
 	private RenderedComponent[] getComponents(String name) throws IOException{
 		Component comp1 = getComponent(name + "1");
 		Component comp2 = getComponent(name + "2");
 		comp1.setCategory(c1);
 		comp2.setCategory(c2);
 		RenderedComponent rc1 = ComponentConverter.convert(display, comp1);
 		RenderedComponent rc2 = ComponentConverter.convert(display, comp2);
 		return new RenderedComponent[] {rc1, rc2};
 	}
 	
 	private Component getComponent(String name) throws IOException{
 		return new Component
 				(name, getBytes(name+DIRS[0]), 
 						getBytes(name+DIRS[1]), 
 						getBytes(name+DIRS[2]), 
 						getBytes(name+DIRS[3]), false);
 		
 	}
 	
 	private Category getSamplePointCategory(int w){
		return new Category("Sampling Points", w, 1, 1, false);
 	}
 
 }
