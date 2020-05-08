 package org.dawb.workbench.ui.expressions;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.fieldassist.ContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 
 public class ExpressionFunctionProposalProvider implements
 		IContentProposalProvider {
 	
 	private Map<String,List<ContentProposal>> proposalMap;
 	private int[] lastMatchBounds = new int[]{0,0};
 	private int lastPosition = 0;
 	private Pattern functionp = Pattern.compile("\\w++:\\w*+");
 	private Pattern namespacep = Pattern.compile("\\w++");
 	
 	/**
 	 * Construct a ExpressionFunctionProposalProvider whose content proposals are
 	 * always the specified array of Objects.
 	 * 
 	 * @param functions
 	 *            the Map of Objects that proposals are extracted from
 	 */
 	public ExpressionFunctionProposalProvider(Map<String, Object> functions) {
 		super();
 		
 		setProposals(functions);
 	}
 
 	@Override
 	public IContentProposal[] getProposals(String contents, int position) {
 		
 		lastPosition = position;
 		//empty string
 		if (contents.isEmpty()) {
 			Set<String> keys = proposalMap.keySet();
 			IContentProposal[] proposals = new ContentProposal[keys.size()];
 			int i = 0;
 			for (String key : keys) {
 				proposals[i++] = new ContentProposal(key);
 			}
 			
 			return proposals;
 		}
 		
 		//last with colon
 		
 		String sub = contents.substring(0, position);
 		Matcher m = functionp.matcher(sub);
 
 		String last = null;
 		int lastEnd = 0;
 		while (m.find()) {
 			last = m.group();
 			lastMatchBounds[0] = m.start();
 			lastMatchBounds[1] = m.end();
 			lastEnd = m.end();
 		}
 		
 		if (last == null || lastEnd != position) {
 			
 			m = namespacep.matcher(sub);
 			
 			while (m.find()) {
 				last = m.group();
 				lastEnd = m.end();
 				lastMatchBounds[0] = m.start();
 				lastMatchBounds[1] = m.end();
 			}
 			
 			if (last!=null && lastEnd == position) {
 
 				Set<String> keys = proposalMap.keySet();
 				List<IContentProposal> content = new ArrayList<IContentProposal>();
 
 				for (String key : keys) {
 					if (key.startsWith(last)) content.add(new ContentProposal(key));
 				}
 
 				return content.toArray(new IContentProposal[content.size()]);
 			} else {
 				Set<String> keys = proposalMap.keySet();
 				IContentProposal[] content = new IContentProposal[keys.size()];
 				int i = 0;
 				
 				for (String key : keys) content[i++] = new ContentProposal(key);
 				
 				return content;
 			}
 		}
 		 
 		String[] strArray = last.split(":");
 		
 		if (strArray.length == 2) {
 
 			List<ContentProposal> contentList = proposalMap.get(strArray[0]);
 			
 			if (contentList == null) return new IContentProposal[]{new ContentProposal("")};
 
 			List<IContentProposal> content = new ArrayList<IContentProposal>();
 
 			for (ContentProposal prop : contentList) {
 				if (prop.getContent().startsWith(strArray[1])) content.add(prop);
 			}
 
 			return content.toArray(new IContentProposal[content.size()]);
 		} else if (strArray.length == 1) {
 			List<ContentProposal> contentList = proposalMap.get(strArray[0]);
 			if (contentList == null) return new IContentProposal[]{new ContentProposal("")};
 			return contentList.toArray(new IContentProposal[contentList.size()]);
 		}
 		
 		return new IContentProposal[]{new ContentProposal("")};
 
 	}
 	
 	public void setProposals(Map<String, Object> functions) {
 		
 		List<String> combinedNames = new ArrayList<String>();
 		proposalMap = new HashMap<String, List<ContentProposal>>();
 		
 		for (String key : functions.keySet()) {
 			Object funcClass = functions.get(key);
 			Method[] methods = ((Class<?>)funcClass).getMethods();
 			
 			List<ContentProposal> methodNames = new ArrayList<ContentProposal>();
 			
 			for (Method method : methods){
 				combinedNames.add(key +":" +method.getName());
 				Type[] types = method.getGenericParameterTypes();
 				StringBuilder sb = new StringBuilder();
 				sb.append(method.getName() + "(");
 				
 				for(int i = 0; i< types.length ; ++i) {
 					if (types[i] instanceof Class<?>)  {
 						sb.append(getSimplifiedName(((Class<?>)types[i]).getSimpleName()));
 					}  else if (types[i] instanceof Object){
 						sb.append(getSimplifiedName(types[i].toString()));
 					}else sb.append(types[i].toString());
 					
 					
 					if (i != types.length -1) sb.append(", ");
 				}
 				
 				sb.append(")");
 				
 				methodNames.add(new ContentProposal(sb.toString()));
 			}
 			
 			proposalMap.put(key, methodNames);
 		}
 	}
 	
 	public int[] getLastMatchBounds() {
 		return lastMatchBounds;
 	}
 	
 	public int getLastPosition() {
 		return lastPosition;
 	}
 	
 	private String getSimplifiedName(final String name) {
 		
 		if (name.toLowerCase().contains("collection")) {
 			return "collection";
 		}
 		
 		if (name.toLowerCase().contains("dataset")) {
 			return "dataset";
 		}
 		
 		if (name.toLowerCase().contains("object")) {
 			return "data";
 		}
 		
 		return name;
 	}
 	
 }
