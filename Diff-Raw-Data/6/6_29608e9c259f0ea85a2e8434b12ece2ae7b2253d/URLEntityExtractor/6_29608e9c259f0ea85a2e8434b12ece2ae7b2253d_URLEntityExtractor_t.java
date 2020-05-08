 package com.personalityextractor.url.data;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.personalityextractor.commons.data.Tweet;
 import com.personalityextractor.entity.WikipediaEntity;
 import com.personalityextractor.entity.extractor.EntityExtractFactory;
 import com.personalityextractor.entity.extractor.IEntityExtractor;
 import com.personalityextractor.entity.extractor.EntityExtractFactory.Extracter;
 import com.personalityextractor.entity.resolver.ViterbiResolver;
 import com.personalityextractor.url.HTMLParser.Readability.Readability;
 
 import cs224n.util.Counter;
 import cs224n.util.PriorityQueue;
 
 public class URLEntityExtractor {
 
 	ViterbiResolver vr = new ViterbiResolver();
 	
 	public static List<String> extractEntitiesinTitle(String urlStr) {
 		ArrayList<String> entities = new ArrayList<String>();
 		String urlContent = URLContent.fetchURLContent(urlStr);
 		if(urlContent==null)
 			return null;
 		String title = URLContent.fetchTitleString(urlContent);
 		IEntityExtractor extractor = EntityExtractFactory.produceExtractor(Extracter.NOUNPHRASE);
 		if(title==null)
 			return null;
 		Tweet t = new Tweet(title);
 		for (String sentence : t.getSentences()) {
 			entities.addAll(extractor.extract(sentence));
 		}
 		return entities;
 	}
 	
 	public static List<String> extractTopEntities(String url){
 		ArrayList<String> topEntities = new ArrayList<String>();
 		Readability read = new Readability();
 		String text = read.removeHTML(url);
 		if(text==null)
 			return null;
 		String[] lines = text.split("\n");
 		Counter<String> entities = new Counter<String>();
 		IEntityExtractor extractor = EntityExtractFactory.produceExtractor(Extracter.PROPERNOUNPHRASE);
 		int line_count =0;
 		for(String line : lines){
			line = line.trim();
			if(line.length()==0)
				continue;
 			if(line_count>2)
 				break;
 			line_count++;
 			System.out.println(line);
 			List<String> ents = extractor.extract(line);
 			System.out.println(ents);
 			if(ents!=null){
 				entities.incrementAll(ents, 1.0);
 			}
 		}
 		PriorityQueue<String> pq = entities.asPriorityQueue();
 		int count=0;
 		double leastCount = 0;
 		
 		while(pq.hasNext() && count <2){
 			count++;
 			leastCount = (entities.getCount(pq.next()));
 		}
 		for(String ent : entities.keySet()){
 			if(entities.getCount(ent)>=leastCount){
 				topEntities.add(ent);
 			}
 		}
 		
 		return topEntities;
 	}
 	
 	public List<WikipediaEntity> resolveEntitiesinTitle(List<String> entities){
 		return vr.resolve(entities);
 	}
 	
 	public List<String> readLinesinFile(String file){
 		List<String> lines = new ArrayList<String>();
 		try{
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line="";
 			while((line= br.readLine())!=null){
 				lines.add(line.trim());
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return lines;
 	}
 	
 	public static void main(String[] args) {
 		URLEntityExtractor uee = new URLEntityExtractor();
		System.out.println(uee.extractTopEntities("http://www.pcmag.com/article2/0,2817,2394487,00.asp#fbid=iY_0drVV-th"));
 	}
 		
 }
