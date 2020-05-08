 package hu.advancedweb.lms.evaluation;
 
import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 public class ExamTest {
 	/**
 	 * Oldal->kerdes -> [tipus, szoveg, inputok nevei, inputokhoz tartozo szovegek]<br>
 	 * Az utolso csak CHECKBOX es RADIO esetben<br>
 	 * Az inputok nevei vesszovel vannak elvalasztva(TEXT esetben csak 1 van)
 	 * */
	public Map<String, ? extends Map<String, ? extends List<String>>>	tests	= Maps.newHashMap();
 }
