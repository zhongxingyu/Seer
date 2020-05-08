 package utils;
 
 
 import app.ObjectRecognition;
 import models.Activity;
 import models.MovementClass;
 import models.ObjectClass;
 import models.Posture;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 
 public class Utils {
 
     public static final String TRAIN_DIRECTORY = "train/";
     public static final String HMM_DIRECTORY = "learned_HMMs/";
     public static final String DATA_DIRECTORY = "data/";
     public static final String ACTIVITY_FILE = "activity/activity_recognition.txt";
     public static final String ROOM_MODEL_FILE = "room_model/room_model.txt";
 
     public static final Map<Activity, List<String>> activityMap = initActivitiesMap();
     public static final String POSTURE_PREFIX = "posture_";
     public static final String SKELETON_PREFIX = "skeleton_";
     public static final String TMP = "~";
 
    public static boolean USE_OBJECT_RECOGNITION = true;
 
     /**
      * Initialise the mapping from activities to their list of postures of interest.
      *
      * @return mapping from activities to their list of postures of interest
      */
     private static Map<Activity, List<String>> initActivitiesMap() {
         Map<Activity, List<String>> map = new EnumMap<Activity, List<String>>(Activity.class);
         List<String> interestingPostures;
 
         interestingPostures = new LinkedList<String>();
         interestingPostures.add("generalPosture");
         interestingPostures.add("leftLegFirst");
         interestingPostures.add("rightLegFirst");
         interestingPostures.add("leftLegSecond");
         interestingPostures.add("rightLegSecond");
         map.put(Activity.Walking, interestingPostures);
 
         interestingPostures = new LinkedList<String>();
         interestingPostures.add("generalPosture");
         map.put(Activity.LyingDown, interestingPostures);
 
         return map;
 
 
     }
 
 
     /**
      * Make sorted list of the posture names of all the files in a directory.
      *
      * @param directoryFiles files in directory
      *
      * @return sorted list of the names of all the files in a directory
      */
     public static List<String> getFileNamesFromDirectory(File[] directoryFiles) {
         List<String> filesInDirectory;
         filesInDirectory = new LinkedList<String>();
 
         for (File subFile : directoryFiles) {
             if (subFile.isFile() && !subFile.getPath().endsWith(TMP) &&
                     subFile.getName().contains(POSTURE_PREFIX)) {
                 filesInDirectory.add(subFile.getPath());
             }
         }
 
         Collections.sort(filesInDirectory, new FileNameComparator());
         return filesInDirectory;
     }
 
     /**
      * Obtain a list of all the training file names sequences from the training
      * directory.
      *
      * @return list of all the training file names sequences
      */
     private static List<List<String>> getTrainingFileNames() {
 
         List<List<String>> fileNames = new LinkedList<List<String>>();
         List<String> filesInDirectory;
 
         File[] files = new File(TRAIN_DIRECTORY).listFiles(), directoryFiles;
 
         if(null == files)
             return fileNames;
 
         for (File file : files) {
             if (file.isDirectory()) {
 
                 directoryFiles = file.listFiles();
                 filesInDirectory = getFileNamesFromDirectory(directoryFiles);
                 fileNames.add(filesInDirectory);
 
             }
 
         }
 
         return fileNames;
     }
 
     /**
      * Get a list of pairs of posture objects and their corresponding skeleton file name
      * from a list of posture file names containing posture information.
      *
      * @param filesInSequence file names containing posture information
      *
      * @return list of pairs of posture objects and their corresponding skeleton file name
      *
      * @throws FileNotFoundException
      */
     public static List<Pair<Posture,String>> getPosturesFromFileSequence(List<String> filesInSequence) throws FileNotFoundException {
 
         List<Pair<Posture,String>> sequencePostures;
         sequencePostures = new ArrayList<Pair<Posture, String>>();
 
         for (String fileName : filesInSequence) {
             String skeletonFileName = getSkeletonFile(fileName);
             sequencePostures.add(new Pair<Posture, String>(new Posture(fileName),skeletonFileName));
         }
         return sequencePostures;
     }
 
 
     /**
      * Read all the training posture files from the training directory
      * and create a list of posture object sequences from all of them,
      * pairing each posture object with it's corresponding skeleton
      * file name.
      *
      * @return list of sequences of posture objects paired with their
      * corresponding skeleton file name
      *
      * @throws java.io.FileNotFoundException invalid file names
      */
     public static List<List<Pair<Posture,String>>> getTrainPostures() throws FileNotFoundException {
         List<List<String>> fileNames = getTrainingFileNames();
 
         List<List<Pair<Posture,String>>> postures = new ArrayList<List<Pair<Posture, String>>>();
         List<Pair<Posture,String>> sequencePostures;
         for (List<String> filesInSequence : fileNames) {
             sequencePostures = getPosturesFromFileSequence(filesInSequence);
             postures.add(sequencePostures);
 
         }
 
         return postures;
     }
 
     /**
      * Combines posture information with object recognition and position information
      * to get an observation index.
      *
      * @param observation observation index for the posture
      * @param skeletonFileName posture file name
      * @param objectRecognition object recognition module reference
      * @param  lastPosition the previous position
      *
      * @return a pair of observation index composed of posture and object detection information
      * and the new position on the greed
      */
     public static Pair<Integer,Pair<Integer,Integer>> addObjectRecognitionObservation(int observation, String skeletonFileName,
                                                       ObjectRecognition objectRecognition, Pair<Integer,Integer> lastPosition) {
 
 
         /* get result from object detection module*/
         Pair<ObjectClass, Pair<Integer, Integer>> result = objectRecognition.getResult(skeletonFileName);
         ObjectClass objectClass = result.getFirst();
 
         /* determine the type of movement based on the previous position and the current position*/
         MovementClass movementClass = MovementClass.getMovement(lastPosition, result.getSecond());
 
 
         /* update the last position */
         lastPosition = result.getSecond();
 
 
         /* combine the posture information with the object interaction
         information and the movement information */
         observation = addVariableToObservation(observation,
                 objectClass.getIndex(), ObjectClass.NUM_OBJECT_CLASSES);
 
         observation = addVariableToObservation(observation,
                 movementClass.getIndex(), MovementClass.NUM_MOVES);
 
         return new Pair<Integer, Pair<Integer, Integer>>(observation,lastPosition);
     }
 
 
 
 
     /**
      * Add a new variable to an observation to obtain a new observation index.
      *
      * @param observation        old observation index
      * @param variableIndex      index of variable to be added
      * @param variableDomainSize variable domain size
      * @return new observation index composed of the
      *         old observation index and the new variable
      */
     public static int addVariableToObservation(int observation, int variableIndex, int variableDomainSize) {
 
         return observation * variableDomainSize + (variableIndex - 1);
     }
 
 
     public static String getSkeletonFile(String postureFileName) {
         String s = new String(postureFileName);
 
         return s.replace(POSTURE_PREFIX, SKELETON_PREFIX);
     }
 }
