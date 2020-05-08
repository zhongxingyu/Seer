 package fr.meijin.run4win.converter;
 
 import org.apache.commons.lang.StringUtils;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zkplus.databind.TypeConverter;
 import org.zkoss.zul.Image;
 import org.zkoss.zul.Label;
 
 import fr.meijin.run4win.util.IdentityEnum;
 
 public class IdentityConverter implements TypeConverter {
 
 	@Override
 	public Object coerceToBean(Object o, Component c) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object coerceToUi(Object o, Component c) {
 		if(o instanceof String){
 			String id = (String) o;
 			if (c instanceof Image) {
 				
				return "/images/"+id.replaceAll(" ", "").toLowerCase()+".gif";
 			} else if (c instanceof Label) {
 
 				return IdentityEnum.getDisplayName(id);
 			}
 		}
 		return "";
 	}
 	
 	public static String coerceToHTML(String id) {
 		if(id == null)
 			return "";
 		
 		if (StringUtils.equals(IdentityEnum.CHAOS_THEORY.getFilename(), id))
 			return "http://www.run4games.com/forum/images/smilies/chaostheory.png";
 		else if (StringUtils.equals(IdentityEnum.CUSTOM_BIOTICS_ENGINEERED_FOR_SUCCESS.getFilename(), id))
 			return "http://www.run4games.com/forum/images/smilies/custombiotic.png";
 		else if (StringUtils.equals(IdentityEnum.THE_PROFESSOR.getFilename(), id))
 			return "http://www.run4games.com/forum/images/smilies/theprofessor.png";
 		else if (StringUtils.equals(IdentityEnum.RIELLE.getFilename(), id))
 			return "http://www.run4games.com/forum/images/smilies/rielle.png";
 		else if (StringUtils.equals(IdentityEnum.EXILE.getFilename(), id))
 			return "http://www.run4games.com/forum/images/smilies/exile.png";
 
 		return "http://www.run4games.com/forum/images/smilies/"+ id + ".gif";
 	}
 }
