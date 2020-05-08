 
 package musicplayer;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.lang.ProcessBuilder.Redirect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Random;
 import java.util.Scanner;
 import musicplayer.gui.Gui;
 
 /**
 * MusicLib isältää kappaleiden tiedot
  * @author tuurekau
  */
 public class MusicLib implements SongList {
     private ArrayList<Song> songs;
     private User owner;
     private File dataFile;
     private Random random;
     private Song selected;
     private MusicGui gui;
     private boolean shuffle;
     private boolean playing;
 
     public void setShuffle(boolean shuffle) {
         this.shuffle = shuffle;
     }
 
     /**
      * Konstruktorissa luodaan Random olio, jos sitä ei ole erikseen
      * annettu parametrina
      * @param owner kirjaston omistaja
      */
     public MusicLib(User owner){
         this(owner, new Random());
     }
     
     /**
      * Konstruktori luo uuden tiedoston käyttäjälle ja käy läpi tämän
      * kotihakemiston Music -kansion.
      * @param owner kirjaston omistaja
      * @param random jos random on määritelty, käytetään satunnaisen kappaleen
      * hakemisessa kyseistä randomia
      */
     public MusicLib(User owner, Random random) {
         this.owner = owner;
         makeDatafile(owner.getName());
         this.random = random;
         this.songs = new ArrayList<Song>();
         this.playing = false;
         
         //read files from dataFile
         load();
         
         //if nothing was loaded, try to scan users home
         if (getSongs().size() == 0){
             //get all files and folders from users Music folder if it exsists
             scanHome();
         }
     }
     
     
     static void recurseTree(File f, Collection<File> all) {
         File[] children = f.listFiles();
         if (children != null) {
             for (File child : children) {
                 all.add(child);
                 recurseTree(child, all);
             }
         }
     }
     
     /**
      * Lisää kappaleen kirjastoon jos sitä ei vielä löydy kirjastosta
      * @param song lisättävä kappale
      */
     public void addSong(Song song){
         if (song==null) return;
         if (this.songs.contains(song)) return;
         this.songs.add(song);
     }
     
     /**
      * Poistaa kappaleen kirjastosta
      * @param song poistettava kappale
      */
     public void removeSong(Song song){
         if (song==null) return;
         this.songs.remove(song);
         if (gui!=null){
             gui.updateSongList();
         }
     }
     
     /**
      * Poistaa tällä hetkellä valitun kappaleen kirjastosta
      */
     public void removeSelected() {
         boolean wasPlaying = playing;
         stopAll();
         Song remove = selected;
         next();
         removeSong(remove);
         setSelected(selected);
         if (wasPlaying) play();
     }
     
     /**
      *
      * @return satunnainen kappale
      */
     public Song getRandom(){
         if (songs.isEmpty()) return null;
         int index = random.nextInt(songs.size());
         return songs.get(index);
     }
 
     public Song getSelected() {
         return selected;
     }
 
     public void setSelected(Song selected) {
         this.selected = selected;
         if (gui!=null) {
             gui.setSelected(songs.indexOf(selected));
         }
     }
     
     /**
      *
      * Jos setSelectedille annetaan numero, joka ei ole negatiivinen tai
      * suurempi, kuin kappaleiden määrä, valitaan kappale, joka löytyy
      * kyseisestä indeksistä.
      * @param selected vaalittavan kappaleen indeksi
      */
     public void setSelected(int selected) {
         if (selected >=0 && selected < songs.size()){
             setSelected(songs.get(selected));
         }
     }
     
     /**
      * Skip vaihtaa kappaletta ja kirjaa sille miinuspisteitä, jos sitä
      * on soitettu vähemmän kuin 10%.
      */
     public void skip(){
         if (selected.getLength() > 0){
             //if we've played less than 10 percent of the song, lets dislike it
             if ((selected.getPosition()*100)/selected.getLength() < 10) selected.dislike();
         }
         next();
     }
     
     /**
      * Next vaihtaa kappaletta soittimessa. Jos shuffle on valittuna,
      * valitaan satunnainen kappale.
      */
     public void next(){
         boolean wasPlaying = playing;
         stopAll();
         if (shuffle){
             setSelected(getRandom());
             if (wasPlaying) play();
         }
         else{
             int i = getSongs().indexOf(getSelected())+1;
             if (i>=getSongs().size()) i=0;
             setSelected(i);
             if (i!=0 && wasPlaying) play();
         }
     }
     
     /**
      *
      * @return arraylist, joka sisältää kaikki kappaleet
      */
     public ArrayList<Song> getSongs(){
         return songs;
     }
     
     public void setSongs(ArrayList<Song> songs){
         if (songs==null) return;
         this.songs = songs;
     }
     
 
     /**
      *
      * @return tämän kirjaston tiedosto
      */
     public File getDataFile() {
         return dataFile;
     }
     
     /**
      * Tallentaa nykyisen kirjaston sille luotuun tiedostoon
      * @return onnistuiko tallennus
      */
     public boolean save() {
         stopAll();
         
         //make a json string of the arraylist
         Gson gson = new GsonBuilder().serializeNulls().create();
         String json = gson.toJson(songs);
         
         try{
             FileWriter fstream = new FileWriter(dataFile.getAbsolutePath());
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(json);
             out.close();
         }catch (Exception e){
             return false;
         }
         
         return true;
     }
     
     /**
      * Lataa kirjaston kappaleiden tiedot tiedostosta
      */
     public void load() {
         Gson gson = new GsonBuilder().serializeNulls().create();
         
         String json = "";
         
         try{
             Scanner fReader = new Scanner(dataFile);
             while(fReader.hasNextLine()){
                 json += fReader.nextLine()+"\n";
             }
         }
         catch(Exception e){}
         
         if (json.length() > 0){
             java.lang.reflect.Type listType = new TypeToken<ArrayList<Song>>(){}.getType();
             ArrayList<Song> songList = gson.fromJson(json, listType);
             
             setSongs(songList);
         }
     }
     
     /**
      * removeUser poistaa käyttäjän tiedoston data -kansiosta
      */
     public void removeUser() {
         if (dataFile.exists()) {
             dataFile.delete();
         }
     }
 
     /**
      * play() pysäyttää edelliset kappaleet ja aloittaa valitun kappaleen
      * soittamisen.
      */
     public void play(){
         stopAll();
         
         //if this song is very unpopular, lets increase the possibility to skip it
         Random rand = new Random();
         if (rand.nextInt(50) > selected.getPopular()){
             next();
         }
         
         while (!selected.play(this) && getSongs().size() != 0){
             removeSong(selected);
             next();
         }
         playing = true;
     }
     
     /**
      * stopAll() pysäyttää kaikki kappaleet
      */
     public void stopAll(){
         playing = false;
         for (Song s:getSongs()){
             s.stop();
             s.resetPlayer();
         }
     }
     
     private void makeDatafile(String name) {
         try{
             dataFile = new File("data/lib_"+name+".data");
             //create a file for this lib
             if (!dataFile.exists()) {
                 dataFile.createNewFile();
             }
         }catch(Exception e){
            
         }
     }
     
     private String makeNewDatafile(String name) {
         String o = name;
         try{
             //create a file for this lib
             int num = 0;
             File f;
             
             //create a new name for this file
             do{
                 num++;
                 if (num>0) o = name+"("+num+")";
                 dataFile = new File("data/lib_"+o+".data");
             } while(dataFile.exists());
 
             dataFile.createNewFile();
         }catch(Exception e){
             
         }
         return o;
     }
 
     void setGui(MusicGui gui) {
         this.gui = gui;
     }
 
     /**
      * Etsii musiikkitiedostoja kotihakemiston Music kansiosta
      */
     public void scanHome() {
         File musicFolder = new File(System.getProperty("user.home")+"/Music");
         addSongsFromFolder(musicFolder);
         if (gui!=null) {
             gui.updateSongList();
             setSelected(selected);
         }
     }
     
     private void addSongsFromFolder(File musicFolder) {
         if (musicFolder.exists()){
             ArrayList<File> all = new ArrayList<File>();
             recurseTree(musicFolder, all);
             //add the found music files to the library
             for(File f:all){
                 if (f.isFile() && !f.isHidden()) {
                     this.addSong(new Song(f));
                 }
             }
         }
     }
 }
