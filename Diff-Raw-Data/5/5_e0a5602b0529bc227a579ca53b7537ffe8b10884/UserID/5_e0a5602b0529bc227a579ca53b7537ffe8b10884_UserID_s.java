 package ch.boxi.togetherLess.businessLogic.dto;
 
 import ch.boxi.javaUtil.id.ID;
 import ch.boxi.javaUtil.id.SimpleLongID;
 import ch.boxi.javaUtil.id.decorator.checkdigit.CheckDigitDecorator;
 import ch.boxi.javaUtil.id.decorator.checkdigit.algorythms.Mod9710Algorythm;
 import ch.boxi.javaUtil.id.decorator.format.FormatterDecorator;
 import ch.boxi.javaUtil.id.decorator.format.IDFormat;
 import ch.boxi.javaUtil.id.decorator.format.SimpleIDFormat;
 import ch.boxi.javaUtil.id.decorator.prefix.PrefixDecorator;
 
 public class UserID implements ID{
 	private static final long serialVersionUID = 4772807214227419835L;
 	private static final String ID_FORMAT = "{prefix|-}0##.###.###";
 	
 	private ID innerID = null;
 	
 	public UserID(long dbRepresentiv){
 		IDFormat Idformat = new SimpleIDFormat(ID_FORMAT);
 		innerID = new FormatterDecorator(
 					new CheckDigitDecorator(
 						new PrefixDecorator(
 							new SimpleLongID(dbRepresentiv), "UID")
 						, new Mod9710Algorythm())
 					, Idformat);
 	}
 	
 	@Override
 	public int compareTo(ID o) {
 		return ((Long)innerID.getLongValue()).compareTo(o.getLongValue());
 	}
 
 	@Override
 	public long getLongValue() {
 		return innerID.getLongValue();
 	}
 	
	
 
 }
