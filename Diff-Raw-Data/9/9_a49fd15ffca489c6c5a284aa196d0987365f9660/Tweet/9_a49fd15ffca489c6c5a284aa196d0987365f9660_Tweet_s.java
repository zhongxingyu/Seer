 package com.personalityextractor.commons.data;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class Tweet {
 	
 	private String text;
 	private List<String> sentences =null;
 	private List<String> links =null;
 	private List<String> hashTags =null;
 	private List<String> analyzedhashTags =null;
 
 	private boolean isReply=false;
 	
 	public Tweet(String text){
 		this.text = text;
 		this.hashTags = new ArrayList<String>();
 		this.links = new ArrayList<String>();
 		this.sentences = new ArrayList<String>();
 		this.analyzedhashTags = new ArrayList<String>();
 		tokenize();
 		
 	}
 	
 	private boolean isNewsArticle(String text){
 		String[] split = text.split("\\s+");
 		double numCaps =0.0;
 		Pattern p = Pattern.compile("^[A-Z]+.*");
 		for(String token : split){
 			if(p.matcher(token).matches()){
 				numCaps++;
 			}
 		}
 		if((numCaps/((double) split.length)) > 0.5)
 				return true;
 		
 		return false;
 	}
 
 	private static String determineCase(char c){
 		String ccase = "";
 		if (Character.isUpperCase(c)) {
 			ccase="u";
 		} else{
 			ccase="l";
 		}
 		return ccase;
 	}
 	private static String analyzeHashTags(String tag) {
 		char[] arr = tag.toCharArray();
 		List<String> words = new ArrayList<String>();
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < arr.length; i++) {
 			sb.append(arr[i]);
 			String ccase = determineCase(arr[i]);
 			String ncase = "";
 			if(i<arr.length-1){
 				ncase= determineCase(arr[i+1]);
 			}
 			if(!ccase.equalsIgnoreCase(ncase) && sb.length()>1){
 				words.add(sb.toString());
 				sb = new StringBuffer();				
 			}
 		}
 		words.add(sb.toString());
 
 		sb = new StringBuffer();
 		for (String s : words) {
 			sb.append(s + " ");
 		}
 		return sb.toString().trim();
 	}
 	
	private void tokenize(){
		
 		if(this.text.startsWith("@")){
 			this.isReply= true;
 		}
 		String[] tokens = this.text.split("\\s+");
 		StringBuffer plainText = new StringBuffer();
 		//Pattern p = Pattern.compile("^(http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
 		//Pattern p = Pattern.compile("http://[a-z.0-9+/%&#~\\\\-_]");
 		
 		for(String token : tokens){
 			if(token.equalsIgnoreCase("rt") || token.startsWith("@")){
 				continue;
 			}
 			
 			if(token.startsWith("http://") || token.endsWith(".com")){
 				links.add(token);
 				continue;
 			}
 			
 			if(token.contains("http://") || token.contains(".com")){
 //				if(this.links==null)
 //					this.links= new ArrayList<String>();
 //				links.add(token);
 				continue;
 			}
 			
 			if(token.startsWith("#")){
 				this.hashTags.add(token);
 				String analyzedHashTag = analyzeHashTags(token.replace("#",""));
 				this.analyzedhashTags.add(analyzedHashTag);
 				token=analyzedHashTag;
 				continue;
 			}
 			
 			plainText.append(token+" ");
 		}
 		
 		String pText = plainText.toString().replaceAll("'s", "");
 		String[] sentences = pText.trim().split("[:;\"?/><,\\.!@%^()\\-+=~`{}|]+");
 		this.sentences = new ArrayList<String>();
 		for(String sentence : sentences){
 			if((sentence=sentence.trim()).length()!=0){
 //				if(isNewsArticle(sentence))
 //					sentence= sentence.toLowerCase();
 				this.sentences.add(sentence);
 			}
 		}
 	}
 	
 	public List<String> getLinks(){
 		return this.links;
 	}
 	
 	public List<String> getSentences(){
 		return this.sentences;
 	}
 	
 	public List<String> getHashTagsEntities(){
 		return this.analyzedhashTags;
 	}
 	public List<String> getHashTags(){
 		return this.hashTags;
 	}
 	public boolean isReply(){
 		return this.isReply;
 	}
 	
 	public static void main(String[] args){
 		String s = "WhatEverthatIS";
 		System.out.println(analyzeHashTags(s));
 //		String text = "Are you a do-good geek? @EFF is hiring a \"Technology Generalist\": http://bit.ly/jeb8x7";
 //		Tweet t = new Tweet(text);
 //		System.out.println(t.sentences);
 //		System.out.println(t.links);
 	}
 
 }
