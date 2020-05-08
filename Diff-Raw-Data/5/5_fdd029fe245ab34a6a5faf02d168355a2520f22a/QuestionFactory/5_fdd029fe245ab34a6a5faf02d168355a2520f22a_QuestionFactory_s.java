 package my.triviagame.bll;
 
 import com.google.common.collect.Lists;
 import com.sun.tools.javac.util.Pair;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import my.triviagame.dal.*;
 import my.triviagame.dal.IDAL.DALException;
 
 
 /**
  * TODO class-level documentation.
  */
 public class QuestionFactory {
     private int MAX_ITERATION = 100;
 
     public static enum QuestionType {
         matchAlbumsToGenreQuestionType,               // DONE
         matchAlbumsToYearQuestionType,                // DONE
         matchTrackToAlbumQuestionType,                // DONE
         matchTrackToArtistQuestionType,               // DONE
         multipleAlbumHasNumberOfTracksQuestionType,   // DONE
         multipleArtistWithMostAlbumsQuestionType,     // DONE
         multipleCountAlbumsByArtistQuestionType,      // DONE
         multipleSongNotByArtistQuestionType,          // DONE
         multipleTrackToArtistQuestionType,            // DONE
         sortAlbumsByYearQuestionType,                 // DONE
         sortTracksByLengthQuestionType,               // DONE
         sortTrackByOrderInAlbumQuestionType,          // DONE
     }
 
     public QuestionFactory(IDAL databaseLayer) {
         this.databaseLayer = databaseLayer;
     }
 
     protected Question generateQuestion(QuestionType type) throws Throwable {
         /*
          * TODO @guyr
          * We wanted to make all the factory methods private, but it doesnt work with getMethod
          * It may work if we get the proper factory method by using a HashSet instead.
          * This is low priority therefore its on my TODO list
          */
         Method method = QuestionFactory.class.getMethod(type.toString());
         try {
             return (Question) (method.invoke(this));
         } catch (InvocationTargetException ex) {
             throw ex.getCause();
         }
     }
 
     public MatchQuestion matchAlbumsToGenreQuestionType() throws DALException {
         String question = "Match the following albums to genre:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentGenres();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(album.getTitle(), album.getGenre()));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MatchQuestion matchAlbumsToYearQuestionType() throws DALException {
         String question = "Match the following albums to year:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentYears();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(album.getTitle(), Integer.toString(album.getYear())));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MatchQuestion matchTrackToAlbumQuestionType() throws DALException {
         String question = "Match the following tracks to albums:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbums();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(album.getTitle(),
                     this.getRandomTrackFromAlbum(album).getTitle()));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MatchQuestion matchTrackToArtistQuestionType() throws DALException {
         String question = "Match the following tracks to artists:";
         Collection<ITrackDescriptor> tracks = getFourTracksByDifferentArtists();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (ITrackDescriptor track: tracks) {
             matches.add(new Pair<String, String>(track.getTitle(), track.getArtistName()));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MultipleChoiceQuestion multipleAlbumHasNumberOfTracksQuestionType() throws DALException {
         String question = "Which of the following albums has %s tracks?";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentNumberOfTracks();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         IAlbumDescriptor winningAlbum = null;
         for (IAlbumDescriptor album: albums) {
             if (winningAlbum == null) {
                 winningAlbum = album;
             }
             answers.add(new MultipleChoiceAnswer(album.getTitle()));
         }
         String formattedQuestion = String.format(
                 question, Integer.toString(databaseLayer.getAlbumTrackDescriptors(winningAlbum).size()));
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
 
     }
 
     public MultipleChoiceQuestion multipleArtistWithMostAlbumsQuestionType() throws DALException {
         String question = "Which of the following artists has the most albums?";
         Collection<IArtistDescriptor> artists = getFourDifferentArtistsWithDifferentNumberOfAlbums();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         IArtistDescriptor winningArtist = null;
         MultipleChoiceAnswer winningAnswer = null;
         for (IArtistDescriptor artist: artists) {
             MultipleChoiceAnswer answer = new MultipleChoiceAnswer(artist.getName());
             if (winningArtist == null ||
                     databaseLayer.getArtistAlbumDescriptors(winningArtist).size() >
                     databaseLayer.getArtistAlbumDescriptors(artist).size()) {
                 winningAnswer = answer;
             }
             answers.add(answer);
         }
         return new MultipleChoiceQuestion(question, winningAnswer, answers);
 
     }
 
     public MultipleChoiceQuestion multipleCountAlbumsByArtistQuestionType() throws DALException {
         String question = "Which of the following artists has %s albums?";
         Collection<IArtistDescriptor> artists = getFourDifferentArtists();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         IArtistDescriptor winningArtist = null;
         for (IArtistDescriptor artist: artists) {
             if (winningArtist == null) {
                 winningArtist = artist;
             }
             answers.add(new MultipleChoiceAnswer(artist.getName()));
         }
         Integer count = databaseLayer.getArtistTrackDescriptors(winningArtist).size();
         String formattedQuestion = String.format(question, count.toString());
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
     }
 
     public MultipleChoiceQuestion multipleSongNotByArtistQuestionType() throws DALException {
         String question = "Which of the following songs is not performed by %s?";
         IArtistDescriptor selectedArtist = getRandomArtistWithAtleastFourTracks();
         List<String> trackNames = new ArrayList<String>(4);
         trackNames.addAll(this.getThreeTrackNamesFromArtist(selectedArtist));
         addAnotherTrackToSetFromADifferentArtist(trackNames, selectedArtist);
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         IArtistDescriptor winningArtist = null;
         for (int i=0; i<4; i++) {
             answers.add(new MultipleChoiceAnswer(trackNames.get(i)));
         }
         String formattedQuestion = String.format(question, selectedArtist.getName());
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
     }
 
     /**
      * Implementation for the game question of tracks by different artists
      */
     public MultipleChoiceQuestion multipleTrackToArtistQuestionType() throws DALException {
         String question = "Which of the following tracks was performed by %s?";
         Collection<ITrackDescriptor> tracks = getFourTracksByDifferentArtists();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         Iterator<ITrackDescriptor> iterator = tracks.iterator();
         ITrackDescriptor winningTrack = null;
         for (ITrackDescriptor track: tracks) {
             if (winningTrack == null) {
                 winningTrack = track;
             }
             answers.add(new MultipleChoiceAnswer(track.getTitle()));
         }
         String formattedQuestion = String.format(question, winningTrack.getArtistName());
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
     }
 
     public OrderedQuestion sortAlbumsByYearQuestionType() throws DALException {
         String question = "Sort the following albums by year:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentYears();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(Integer.toString(album.getYear()), album.getTitle()));
         }
         OrderedAnswer answer = new OrderedAnswer(matches);
         return new OrderedQuestion(question, answer);
     }
 
     public OrderedQuestion sortTracksByLengthQuestionType() throws DALException {
         String question = "Sort the following tracks by length:";
         Collection<ITrackDescriptor> tracks = getFourRandomTracksByDifferentLength();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (ITrackDescriptor track: tracks) {
            matches.add(new Pair<String, String>(track.getTitle(), Integer.toString(track.getLengthInSeconds())));
         }
         OrderedAnswer answer = new OrderedAnswer(matches);
         return new OrderedQuestion(question, answer);
     }
 
     public OrderedQuestion sortTrackByOrderInAlbumQuestionType() throws DALException {
         String question = "Sort the following tracks by their order in album %s:";
         IAlbumDescriptor selectedAlbum = getAlbumWithAtleastFourTracks();
         Set<ITrackDescriptor> tracks = new HashSet<ITrackDescriptor>(4);
         populateTracksSetWithRandomTracksFromAlbum(tracks, selectedAlbum);
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         List<ITrackDescriptor> sortedTrackListByTrackNumber = this.sortTracksByTrackNumber(tracks);
         for (ITrackDescriptor track: sortedTrackListByTrackNumber) {
             matches.add(new Pair<String, String>(Integer.toString(track.getTrackNum()), track.getTitle()));
         }
         OrderedAnswer answer = new OrderedAnswer(matches);
         String formattedQuestion = String.format(question, selectedAlbum.getTitle());
         return new OrderedQuestion(formattedQuestion, answer);
     }
     private void addAnotherTrackToSetFromADifferentArtist(List<String> trackNames, IArtistDescriptor selectedArtist) throws DALException {
         int iterations = 0;
         while (trackNames.size()<4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             for (IArtistDescriptor artist: getRandomArtists(4)) {
                 if (artist.getId() == selectedArtist.getId()) {
                     continue;
                 }
                 for (ITrackDescriptor differentTrack:
                         this.databaseLayer.getArtistTrackDescriptors(artist)) {
                     if (!trackNames.contains(differentTrack.getTitle())) {
                         trackNames.add(differentTrack.getTitle());
                         break;
                     }
                 }
             }
         }
     }
 
     private IArtistDescriptor getRandomArtistWithAtleastFourTracks() throws DALException {
         IArtistDescriptor selectedArtist = null;
         int iterations = 0;
         while (selectedArtist == null ) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IArtistDescriptor> randomArtists = this.getRandomArtists(12);
             for (IArtistDescriptor artist: randomArtists) {
                 // It is necessary to check for duplicated track numbers because of Issue #15
                 Set<Integer> trackNumbers = new HashSet<Integer>();
                 for (ITrackDescriptor track: this.databaseLayer.getArtistTrackDescriptors(artist)) {
                     trackNumbers.add(new Integer(track.getTrackNum()));
                 }
                 if (trackNumbers.size() > 3) {
                     selectedArtist = artist;
                     break;
                 }
             }
         }
         return selectedArtist;
     }
 
     private void populateTracksSetWithRandomTracksFromAlbum(Set<ITrackDescriptor> tracks, IAlbumDescriptor selectedAlbum) throws DALException {
         int iterations = 0;
         while (tracks.size() < 4) {
 
             ITrackDescriptor track = this.getRandomTrackFromAlbum(selectedAlbum);
             if (tracks.contains(track)) {
                 continue;
             }
             tracks.add(track);
         }
     }
 
     private IAlbumDescriptor getAlbumWithAtleastFourTracks() throws DALException {
         IAlbumDescriptor selectedAlbum = null;
         int iterations=0;
         while (selectedAlbum == null) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection <IAlbumDescriptor> randomAlbums = this.getRandomAlbums(4);
             for (IAlbumDescriptor album: randomAlbums) {
                if (databaseLayer.getAlbumTrackDescriptors(album).size() >= 4) {
                    selectedAlbum = album;
                    break;
                }
             }
         }
         return selectedAlbum;
     }
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbums() throws DALException {
          Set<IAlbumDescriptor> chosenAlbums = new HashSet<IAlbumDescriptor>(4);
          int iterations=0;
          while (chosenAlbums.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IAlbumDescriptor> randomAlbums = getRandomAlbums(12);
             for (IAlbumDescriptor album: randomAlbums) {
                 if (chosenAlbums.size() == 4) {
                  break;
                 }
                 if (this.databaseLayer.getAlbumTrackDescriptors(album).isEmpty()) {
                     continue;
                 }
                 chosenAlbums.add(album);
             }
          }
          return chosenAlbums;
     }
 
     private Collection<IArtistDescriptor> getFourDifferentArtists() throws DALException {
          Set<IArtistDescriptor> chosenArtists = new HashSet<IArtistDescriptor>(4);
          int iterations=0;
          while (chosenArtists.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IArtistDescriptor> randomArtists = getRandomArtists(12);
             for (IArtistDescriptor artist: randomArtists) {
                 if (chosenArtists.size() == 4) {
                     break;
                 }
                 if (this.databaseLayer.getArtistTrackDescriptors(artist).isEmpty()) {
                     continue;
                 }
                 chosenArtists.add(artist);
             }
          }
          return chosenArtists;
     }
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbumsByDifferentGenres() throws DALException {
         Collection<IAlbumDescriptor> selectedAlbums = new HashSet<IAlbumDescriptor>(4);
         Set<String> genres = new HashSet<String>(4);
         int iterations=0;
         while (selectedAlbums.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IAlbumDescriptor> albums = getRandomAlbums(12);
             for (IAlbumDescriptor album : albums) {
                 if (genres.contains(album.getGenre())) {
                     continue;
                 }
                 if (selectedAlbums.size() == 4) {
                     break;
                 }
                 genres.add(album.getGenre());
                 selectedAlbums.add(album);
 
             }
         }
         return selectedAlbums;
     }
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbumsByDifferentNumberOfTracks() throws DALException {
         Collection<IAlbumDescriptor> selectedAlbums = new HashSet<IAlbumDescriptor>(4);
         Set<Integer> numberOfTracks = new HashSet<Integer>(4);
         int iterations=0;
         while (selectedAlbums.size() < 4){
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IAlbumDescriptor> albums = getRandomAlbums(12);
             for (IAlbumDescriptor album : albums) {
                 List<ITrackDescriptor> albumTracks = databaseLayer.getAlbumTrackDescriptors(album);
                 if (numberOfTracks.contains(albumTracks.size())) {
                     continue;
                 }
                 if (selectedAlbums.size() == 4) {
                     break;
                 }
                 numberOfTracks.add(albumTracks.size());
                 selectedAlbums.add(album);
             }
         }
         return selectedAlbums;
     }
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbumsByDifferentYears() throws DALException {
         Collection<IAlbumDescriptor> selectedAlbums = new HashSet<IAlbumDescriptor>(4);
         Set<Short> years = new HashSet<Short>(4);
         int iterations=0;
         while (selectedAlbums.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IAlbumDescriptor> albums = getRandomAlbums(12);
             for (IAlbumDescriptor album : albums) {
                 if (years.contains(album.getYear())) {
                     continue;
                 }
                 if (album.getYear() < 10) {
                     continue;
                 }
                 if (selectedAlbums.size() == 4) {
                     break;
                 }
                 years.add(album.getYear());
                 selectedAlbums.add(album);
             }
         }
         return selectedAlbums;
     }
 
 
     private Collection<IArtistDescriptor> getFourDifferentArtistsWithDifferentNumberOfAlbums() throws DALException {
         Collection<IArtistDescriptor> selectedArtists = new HashSet<IArtistDescriptor>(4);
         Set<Integer> numberOfAlbums = new HashSet<Integer>(4);
         int iterations=0;
         while (selectedArtists.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<IArtistDescriptor> artists = getRandomArtists(12);
             for (IArtistDescriptor artist: artists) {
                 List<IAlbumDescriptor> artistAlbums = databaseLayer.getArtistAlbumDescriptors(artist);
                 Integer numberOfAlbumsByCurrentArist = artistAlbums.size();
                 if (numberOfAlbums.contains(numberOfAlbumsByCurrentArist)) {
                     continue;
                 }
                 if (selectedArtists.size() == 4) {
                     break;
                 }
                 numberOfAlbums.add(numberOfAlbumsByCurrentArist);
                 selectedArtists.add(artist);
             }
         }
         return selectedArtists;
     }
 
     private Collection<ITrackDescriptor> getFourTracksByDifferentArtists() throws DALException {
         Collection<ITrackDescriptor> selectedTracks = new HashSet<ITrackDescriptor>(4);
         Set<String> artists = new HashSet<String>(4);
         int iterations=0;
         while (selectedTracks.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<ITrackDescriptor> tracks = getRandomTracks(12);
             for (ITrackDescriptor track : tracks) {
                 if (artists.contains(track.getArtistName())) {
                     continue;
                 }
                 if (selectedTracks.size() == 4) {
                     break;
                 }
                 artists.add(track.getArtistName());
                 selectedTracks.add(track);
             }
         }
         return selectedTracks;
     }
 
     private Collection<ITrackDescriptor> getFourRandomTracksByDifferentLength() throws DALException {
         Collection<ITrackDescriptor> selectedTracks = new HashSet<ITrackDescriptor>(4);
         Set<Short> lengths = new HashSet<Short>(4);
         int iterations=0;
         while (selectedTracks.size() < 4) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Collection<ITrackDescriptor> tracks = getRandomTracks(12);
             for (ITrackDescriptor track : tracks) {
                 if (lengths.contains(track.getLengthInSeconds())) {
                     continue;
                 }
                 if (selectedTracks.size() == 4) {
                     return selectedTracks;
                 }
                 lengths.add(track.getLengthInSeconds());
                 selectedTracks.add(track);
             }
         }
         return selectedTracks;
     }
 
     private Collection<IAlbumDescriptor> getRandomAlbums(int number) throws DALException {
         Collection<IAlbumDescriptor> collection = new HashSet<IAlbumDescriptor>(number);
         Random random = new Random();
         ITableStatistics stats = this.databaseLayer.getTrackTableStatistics();
         int iterations=0;
         while (collection.size() < number) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Set<Integer> randomIds = getSetRandomIds(number, stats);
             for (IAlbumDescriptor album:
                     this.databaseLayer.getAlbumDescriptors(Lists.newArrayList(randomIds))) {
                 if (collection.size() == number) {
                     break;
                 }
                 collection.add(album);
             }
         }
         return collection;
     }
 
     private Collection<IArtistDescriptor> getRandomArtists(int number) throws DALException {
         Collection<IArtistDescriptor> collection = new HashSet<IArtistDescriptor>(number);
         ITableStatistics stats = this.databaseLayer.getTrackTableStatistics();
         int iterations=0;
         while (collection.size() < number) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Set<Integer> randomIds = getSetRandomIds(number, stats);
             for (IArtistDescriptor artist:
                     this.databaseLayer.getArtistDescriptors(Lists.newArrayList(randomIds))) {
                 if (collection.size() == number) {
                     break;
                 }
                 collection.add(artist);
             }
         }
         return collection;
     }
 
     private Collection<ITrackDescriptor> getRandomTracks(int number) throws DALException {
         Collection<ITrackDescriptor> collection = new HashSet<ITrackDescriptor>(number);
         ITableStatistics stats = this.databaseLayer.getTrackTableStatistics();
         int iterations=0;
         while (collection.size() < number) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             Set<Integer> randomIds = getSetRandomIds(number, stats);
             for (ITrackDescriptor track:
                     this.databaseLayer.getTrackDescriptors(Lists.newArrayList(randomIds))) {
                 if (collection.size() == number) {
                     break;
                 }
                 collection.add(track);
             }
         }
         return collection;
     }
 
     private Set<Integer> getSetRandomIds(int number, ITableStatistics stats) {
         Random random = new Random();
         Set<Integer> randomIds = new HashSet<Integer>(number);
         while (randomIds.size() < number) {
             randomIds.add(stats.getMinId() +
                     random.nextInt(stats.getMaxId() - stats.getMinId() + 1));
         }
         return randomIds;
     }
 
     private ITrackDescriptor getRandomTrackFromAlbum(IAlbumDescriptor album) throws DALException {
         Random random = new Random();
         List<ITrackDescriptor> albumTracks = databaseLayer.getAlbumTrackDescriptors(album);
         int maxId = albumTracks.size();
         List<ITrackDescriptor> tracks = Lists.newArrayList(albumTracks);
         return tracks.get(random.nextInt(maxId));
     }
 
     private ITrackDescriptor getRandomTrackFromArtist(IArtistDescriptor artist) throws DALException {
         Random random = new Random();
         List <ITrackDescriptor> artistTracks = databaseLayer.getArtistTrackDescriptors(artist);
         int maxId = artistTracks.size();
         List<ITrackDescriptor> tracks = Lists.newArrayList(artistTracks);
         return tracks.get(random.nextInt(maxId));
     }
 
     private Set<Integer> getSetOfRandomNumbers(int maxId, int count) {
         Random random = new Random();
         Set<Integer> randomIds = new HashSet<Integer>(count);
         while (randomIds.size() < count) {
             randomIds.add(random.nextInt(maxId));
         }
         return randomIds;
     }
 
     private Collection<String> getThreeTrackNamesFromArtist(IArtistDescriptor artist) throws DALException {
         Random random = new Random();
         Collection<String> selectedtracks = new HashSet<String>(3);
         List<ITrackDescriptor> tracks = databaseLayer.getArtistTrackDescriptors(artist);
         int iterations=0;
         while (selectedtracks.size() < 3) {
             iterations+=1;
             if (iterations == MAX_ITERATION) {
                 throw new RuntimeException("Too many iterations.");
             }
             selectedtracks.add(tracks.get(random.nextInt(tracks.size()*100) % tracks.size()).getTitle());
         }
         return selectedtracks;
     }
 
     private List<ITrackDescriptor> sortTracksByTrackNumber(Collection<ITrackDescriptor> tracks) {
         List<ITrackDescriptor> sortedTracks = new ArrayList<ITrackDescriptor>();
         for (ITrackDescriptor track: tracks) {
             if (sortedTracks.contains(track)) {
                 continue;
             }
             int i=0;
             for (i=0; i<sortedTracks.size(); i++) {
                 if (sortedTracks.get(i).getTrackNum() > track.getTrackNum()) {
                     break;
                 }
             }
             sortedTracks.add(i, track);
         }
         return sortedTracks;
     }
 
     private IDAL databaseLayer;
 }
