 package com.se1by;
 
 import java.io.File;
 import entagged.audioformats.AudioFile;
 import entagged.audioformats.AudioFileIO;
 import entagged.audioformats.Tag;
 import entagged.audioformats.exceptions.CannotReadException;
 import entagged.audioformats.exceptions.CannotWriteException;
 
 public class Logic {
 	private File sourceFile;
 	private AudioFile audioFile;
 	private Tag tag;
 
 	public Logic(File f) {
 		try {
 			setSourceFile(f);
 			setAudioFile(AudioFileIO.read(f));
 			setTag(audioFile.getTag());
 		} catch (CannotReadException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public boolean saveFile(String name) {
 		try {
 			String extension = sourceFile.getName().substring(
 					sourceFile.getName().lastIndexOf(".") + 1);
 			audioFile.commit();
 			System.out.println(sourceFile.getParent() + "\\" + name + "."
 					+ extension);
			sourceFile.renameTo(new File(sourceFile.getParent() + "\\" + name
					+ "." + extension));
			sourceFile = new File(sourceFile.getParent() + "\\" + name + "."
					+ extension);
 			setAudioFile(AudioFileIO.read(sourceFile));
 			setTag(audioFile.getTag());
 		} catch (CannotWriteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CannotReadException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 
 	public String getArtist() {
 		return tag.getFirstArtist();
 	}
 
 	public void setArtist(String artist) {
 		tag.setArtist(artist);
 	}
 
 	public String getTitle() {
 		return tag.getFirstTitle();
 	}
 
 	public void setTitle(String title) {
 		tag.setTitle(title);
 	}
 
 	public String getGenre() {
 		return tag.getFirstGenre();
 	}
 
 	public void setGenre(String genre) {
 		tag.setGenre(genre);
 	}
 
 	public String getAlbum() {
 		return tag.getFirstAlbum();
 	}
 
 	public void setAlbum(String album) {
 		tag.setAlbum(album);
 	}
 
 	public String getTrack() {
 		return tag.getFirstTrack();
 		//return tag.getTrack().toString().replaceFirst("\\D*(\\d*).*", "$1");
 	}
 
 	public void setTrack(String track) {
 		tag.setTrack(track);
 	}
 
 	public String getYear() {
 		return tag.getFirstYear();
 		//return tag.getYear().toString().replaceFirst("\\D*(\\d*).*", "$1");
 	}
 
 	public void setYear(String year) {
 		tag.setYear(year);
 	}
 
 	public File getSourceFile() {
 		return sourceFile;
 	}
 
 	public void setSourceFile(File sourceFile) {
 		this.sourceFile = sourceFile;
 	}
 
 	public AudioFile getAudioFile() {
 		return audioFile;
 	}
 
 	public void setAudioFile(AudioFile audioFile) {
 		this.audioFile = audioFile;
 	}
 
 	public Tag getTag() {
 		return tag;
 	}
 
 	public void setTag(Tag tag) {
 		this.tag = tag;
 	}
 }
