 /**
  * 
  */
 package hu.cubussapiens.modembed.assembly.persistence;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 
 import embedded.assembly.AssemblyFactory;
 import embedded.assembly.Code;
 import embedded.assembly.Field;
 import embedded.assembly.FieldType;
 import embedded.assembly.Instruction;
 import embedded.assembly.InstructionSet;
 import embedded.assembly.Section;
 
 /**
  * @author balage
  *
  */
 public class InstructionSetResource extends ResourceImpl {
 
 	/**
 	 * @param uri
 	 */
 	public InstructionSetResource(URI uri) {
 		super(uri);
 	}
 
 	private FieldType parseFieldType(char f){
 		switch(f){
 		case 'n':
 			return FieldType.RELATIVE_CODE_ADDRESS;
 		case 'x':
 			return FieldType.DONT_CARE;
 		case 'k':
 			return FieldType.LITERAL;
 		case 'f':
 			return FieldType.MEM_ADDRESS;
 		case 'c':
 			return FieldType.CODE_ADDRESS;
 		case 'b':
 			return FieldType.BIT_SELECT;
 		default:
 			return FieldType.OTHER;
 		}
 	}
 
 	private Section parseSection(String v){
 		if (v.startsWith("0") || v.startsWith("1")){
 			Code code = AssemblyFactory.eINSTANCE.createCode();
 			code.setLength(v.length());
 			code.setCode(Integer.parseInt(v, 2));
 			return code;
 		}
 		
 		Field field = AssemblyFactory.eINSTANCE.createField();
 		String fs = v.substring(1);
 		int i  = fs.indexOf(':');
 		if (i != -1){
 			String ps = fs.substring(i+1);
 			parseSectionParam(field, ps);
 			fs = fs.substring(0,i);
 		}
 		int length = Integer.parseInt(fs);
 		field.setLength(length);
 		field.setType(parseFieldType(v.charAt(0)));
 		return field;
 	}
 	
 	private void parseSectionParam(Field field, String ps){
 		String n = "";
 		char c = 0;
 		int i = 0;
		while(Character.isLetter(c = ps.charAt(i))){
 			n += c;
 			i++;
 		}
 		field.setParameter(n);
 		field.setParamshift(Integer.parseInt(ps.substring(n.length())));
 	}
 	
 	private String[] split(String line){
 		List<String> result = new ArrayList<String>();
 		String current = "";
 		line = line.trim();
 		while(line.length() > 0){
 			char c = line.charAt(0);
 			line = line.substring(1);
			if (c==':' || Character.isLetterOrDigit(c)){
 				current += c;
 			}else{
 				if (current.length() > 0){
 					result.add(current);
 					current = "";
 				}
 			}
 		}
 		if (current.length() > 0){
 			result.add(current);
 		}
 		return result.toArray(new String[result.size()]);
 	}
 	
 	@Override
 	protected void doLoad(InputStream inputStream, Map<?, ?> options)
 			throws IOException {
 		
 		InstructionSet iset = AssemblyFactory.eINSTANCE.createInstructionSet();
 		
 		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
 		
 		String line = null;
 		
 		while((line = r.readLine()) != null){
 			
 			line = line.trim();
 			if (line.startsWith("\\")){
 				
 				if (line.startsWith("\\name ")){
 					iset.setName(line.substring("\\name ".length()).trim());
 				}
 				
 				if (line.startsWith("\\wsize ")){
 					String ws = line.substring("\\wsize ".length()).trim();
 					iset.setWordsize(Integer.parseInt(ws));
 				}
 				
 			}else{
 				String[] v = split(line);
 				if (v.length > 0){
 
 					Instruction ins = AssemblyFactory.eINSTANCE.createInstruction();
 
 					ins.setName(v[0].trim());
 					for(int i=1;i<v.length;i++){
 						Section s = parseSection(v[i]);
 						if (s != null){
 							ins.getSections().add(s);
 						}
 					}
 					EList<Section> ss = ins.getSections();
 					int l = 0;
 					for(int i=ss.size()-1;i>=0;i--){
 						Section s = ss.get(i);
 						s.setStart(l);
 						l += s.getLength();
 					}
 
 					iset.getInstructions().add(ins);
 				}
 			}
 			
 		}
 		
 		r.close();
 		
 		getContents().clear();
 		getContents().add(iset);
 		
 	}
 	
 }
