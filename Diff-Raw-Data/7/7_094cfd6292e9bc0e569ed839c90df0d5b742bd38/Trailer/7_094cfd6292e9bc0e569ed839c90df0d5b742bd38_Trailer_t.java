 package com.moviejukebox.traileraddictapi.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 public class Trailer {
     /*
      * Properties
      */
 
     private String combinedTitle = "";
     private String link = "";
     private String publishDate = "";
     private int trailerId = 0;
     private Map<TrailerSize, String> embed = new EnumMap<TrailerSize, String>(TrailerSize.class);
     /*
      * Properties for the detailed view from "simpleapi"
      */
     private String trailerTitle = "";
     private String filmTitle = "";
     private String description = "";
     private String studio = "";
     private List<String> directors = new ArrayList<String>();
     private List<String> writers = new ArrayList<String>();
     private List<String> cast = new ArrayList<String>();
     private String releaseDate = "";
 
     //<editor-fold defaultstate="collapsed" desc="Getter methods">
     public String getCombinedTitle() {
         return combinedTitle;
     }
 
     public String getLink() {
         return link;
     }
 
     public String getPublishDate() {
         return publishDate;
     }
 
     public int getTrailerId() {
         return trailerId;
     }
 
     public Map<TrailerSize, String> getEmbed() {
         return embed;
     }
 
     public String getEmbed(TrailerSize trailerSize) {
         return embed.get(trailerSize);
     }
 
     public String getTrailerTitle() {
         return trailerTitle;
     }
 
     public String getFilmTitle() {
         return filmTitle;
     }
 
     public String getDescription() {
         return description;
     }
 
     public String getStudio() {
         return studio;
     }
 
     public List<String> getDirectors() {
         return directors;
     }
 
     public List<String> getWriters() {
         return writers;
     }
 
     public List<String> getCast() {
         return cast;
     }
 
     public String getReleaseDate() {
         return releaseDate;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Setter methods">
     public void setCombinedTitle(String combinedTitle) {
         this.combinedTitle = stripCdata(combinedTitle);
     }
 
     public void setLink(String link) {
         this.link = link;
     }
 
     public void setPublishDate(String publishDate) {
         this.publishDate = publishDate;
     }
 
     public void setTrailerId(int trailerId) {
         this.trailerId = trailerId;
     }
 
     public void setEmbed(Map<TrailerSize, String> embed) {
         this.embed = embed;
     }
 
     public void addEmbed(String embedData) {
        this.embed.put(TrailerSize.custom, embedData);
     }
 
    public void addEmbed(TrailerSize size, String embedData) {
        this.embed.put(size, embedData);
     }
 
     public void setTrailerTitle(String trailerTitle) {
         this.trailerTitle = stripCdata(trailerTitle);
     }
 
     public void setFilmTitle(String filmTitle) {
         this.filmTitle = stripCdata(filmTitle);
     }
 
     public void setDescription(String description) {
         this.description = stripCdata(description);
     }
 
     public void setStudio(String studio) {
         this.studio = studio;
     }
 
     public void setDirectors(List<String> directors) {
         this.directors = directors;
     }
 
     /**
      * Caters for multiple elements separated with a comma
      *
      * @param directors
      */
     public void setDirectors(String directors) {
         this.directors.clear();
 
         if (directors.contains(",")) {
             setDirectors(Arrays.asList(directors.split(",")));
         } else {
             addDirector(directors);
         }
     }
 
     public void setWriters(List<String> writers) {
         this.writers = writers;
     }
 
     /**
      * Caters for multiple elements separated with a comma
      *
      * @param writers
      */
     public void setWriters(String writers) {
         this.writers.clear();
 
         if (writers.contains(",")) {
             setWriters(Arrays.asList(writers.split(",")));
         } else {
             addWriter(writers);
         }
     }
 
     public void setCast(List<String> cast) {
         this.cast = cast;
     }
 
     /**
      * Caters for multiple elements separated with a comma
      *
      * @param cast
      */
     public void setCast(String cast) {
         this.cast.clear();
 
         if (cast.contains(",")) {
             setCast(Arrays.asList(cast.split(",")));
         } else {
             addCast(cast);
         }
     }
 
     public void setReleaseDate(String releaseDate) {
         this.releaseDate = releaseDate;
     }
 
     public void addCast(String cast) {
         this.cast.add(cast);
     }
 
     public void addDirector(String director) {
         this.directors.add(director);
     }
 
     public void addWriter(String writer) {
         this.writers.add(writer);
     }
     //</editor-fold>
 
     private String stripCdata(String source) {
         if (source.contains("CDATA")) {
             return source.replace("<![CDATA[", "").replace("]]","").trim();
         } else {
             return source;
         }
     }
 
     @Override
     public String toString() {
         return "Trailer{" + "combinedTitle=" + combinedTitle + ", link=" + link + ", publishDate=" + publishDate + ", trailerId=" + trailerId + ", embed=" + embed + ", trailerTitle=" + trailerTitle + ", filmTitle=" + filmTitle + ", description=" + description + ", studio=" + studio + ", directors=" + directors + ", writers=" + writers + ", cast=" + cast + ", releaseDate=" + releaseDate + '}';
     }
 }
