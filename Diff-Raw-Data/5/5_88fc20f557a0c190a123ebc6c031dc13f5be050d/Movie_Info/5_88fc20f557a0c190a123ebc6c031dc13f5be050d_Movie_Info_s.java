 package model;
 
 import java.util.ArrayList;
 
 import util.RegexUtil;
 
 public class Movie_Info{
 	private String movie_name = null;
 	private String haibao_path = null;
 	private ArrayList<String> names = new ArrayList<String>();
 	private ArrayList<String> downloadlinks = new ArrayList<String>();
 	private ArrayList<String> downloadnames = new ArrayList<String>();
 	
 	public Movie_Info(){}
 	public Movie_Info(String name, String path){
 		this.movie_name = name;
 		this.haibao_path = path;
 	}
 	private Movie_Info(Movie_Info info){
 		this.movie_name = info.movie_name;
 		this.haibao_path = info.haibao_path;
 		this.names = info.names;
 		this.downloadlinks = info.downloadlinks;
 		this.downloadnames = info.downloadnames;
 	}
 	public boolean hasName(){
 		return this.movie_name != null;
 	}
 	public boolean hasHaiBaoPath(){
 		return this.haibao_path != null;
 	}
 	public void setMovieName(String movie_name){
 		this.movie_name = movie_name;
 	}
 	public void setHaiBaoPath(String haibao_path){
 		this.haibao_path = haibao_path;
 	}
 	public void setNames(ArrayList<String> names){
 		this.names = names;
 	}
 //	public void setDownLoadLinks(ArrayList<String> downloadlinks){
 //		this.downloadlinks = downloadlinks;
 //	}
 //	public void setDownLoadNames(ArrayList<String> downloadnames){
 //		this.downloadnames = downloadnames;
 //	}
 	public void addName(String name){
 		this.names.add(name);
 	}
 	public void addDownLoadLinks(ArrayList<String> downloadlinks, String downloadname){
 		this.downloadlinks.addAll(downloadlinks);
 		for(int i = 0; i < downloadlinks.size() ; i++){
 			this.downloadnames.add(downloadname);
 		}
 	}
 	public void addDownLoadLinks(String downloadlink, String downloadname){
 		this.downloadlinks.add(downloadlink);
 		this.downloadnames.add(downloadname);
 	}
 	public String getMovieName(){
 		return this.movie_name;
 	}
 	public String getHaiBaoPath(){
 		return this.haibao_path;
 	}
 	public ArrayList<String> getNames(){
 		return this.names;
 	}
 	public ArrayList<String> getDownLoadLinks(){
 		return this.downloadlinks;
 	}
 	public ArrayList<String> getDownLoadNames(){
 		return this.downloadnames;
 	}
 	
 	@Override
 	public String toString(){
 		StringBuffer sb = new StringBuffer("movie name: " + movie_name + "\nhaibao path: " + haibao_path + "\n");
 		if(names.size() != 0){
 			sb.append("movie has names: ");
 			for(int i = 0; i < names.size(); i ++){
 				sb.append(names.get(i) + " ");
 			}
 		}
 		sb.append("\n");
 		if(downloadlinks.size() != 0){
 			sb.append("movie has down loads : ");
 			for(int i = 0; i < downloadlinks.size(); i ++){
 				sb.append("\n	" + downloadnames.get(i) + "	" + downloadlinks.get(i));
 			}
 		}
 		return sb.toString();
 	}
 	
 	@Override
 	public Movie_Info clone(){
 		return new Movie_Info(this);
 	}
 	
 	public Movie_Info convertForMySQL(){
 		try {
 			if(movie_name != null){
 				movie_name = movie_name.replaceAll("'","''");
 				movie_name = RegexUtil.formatMovieName(movie_name);
 			}
 			if(haibao_path != null){
 				haibao_path = haibao_path.replaceAll("'","''");
 			}
 			for(int i = 0 ; i < names.size(); i ++){
				names.set(i, names.get(i).replaceAll("'","''"));
 			}
 			for(int i = 0 ; i < downloadlinks.size(); i ++){
 				downloadlinks.set(i, downloadlinks.get(i).replaceAll("'","''"));
 			}
 			for(int i = 0 ; i < downloadnames.size(); i ++){
				downloadnames.set(i, RegexUtil.formatMovieName(downloadnames.get(i).replaceAll("'","''")));
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return this;
 	}
 }
