 package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentMap;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.TimingUtility;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.CloneDAO;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;
 
 public class CFilter extends CDetector {
 
 	public static void main(final String[] args) {
 
 		final long startTime = System.nanoTime();
 
 		System.out.print("identifying source files ... ");
 		final CFilter filter = new CFilter();
 		final SortedSet<String> paths = filter.identifyFiles();
 		System.out.println(": done.");
 
 		System.out.print("obtaining file contents ... ");
 		final ConcurrentMap<String, List<Statement>> contents = filter
 				.getFileContent(paths);
 		System.out.println(": done.");
 
 		System.out.print("identifying changed places ... ");
 		List<ModificationPattern> patterns;
 		try {
 			final ReadOnlyDAO readOnlyDAO = ReadOnlyDAO.getInstance();
 			patterns = readOnlyDAO.getModificationPatterns(2, 1);
 			readOnlyDAO.close();
 		} catch (final Exception e) {
 			patterns = new ArrayList<ModificationPattern>();
 			e.printStackTrace();
 			System.exit(0);
 		}
 		final List<Clone> changedPlaces = new ArrayList<Clone>();
 		for (final ModificationPattern pattern : patterns) {
 			final CodeFragment patterncode = pattern.getModifications().get(0).after;
 			for (final Entry<String, List<Statement>> entry : contents
 					.entrySet()) {
 				final String path = entry.getKey();
 				final List<Statement> statements = entry.getValue();
 				changedPlaces.addAll(filter.getChangedPlaces(path, statements,
 						patterncode.statements));
 			}
 		}
 		System.out.print(": done, ");
 		System.out.print(Integer.toString(changedPlaces.size()));
 		System.out.println(" places.");
 
 		System.out.print("extracting changed clones ... ");
 		Map<Integer, List<Clone>> clones;
 		try {
 			final ReadOnlyDAO readOnlyDAO = ReadOnlyDAO.getInstance();
 			clones = readOnlyDAO.getClones();
 			readOnlyDAO.close();
 		} catch (final Exception e) {
 			clones = new HashMap<Integer, List<Clone>>();
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		CloneDAO cloneDAO;
 		try {
 			cloneDAO = new CloneDAO(false);
 		} catch (final Exception e) {
 			cloneDAO = null;
 			e.printStackTrace();
 			System.exit(0);
 		}
 		for (final List<Clone> cloneset : clones.values()) {
 			for (final Clone clone : cloneset) {
 				int number = 0;
 				for (final Clone place : changedPlaces) {
 					if (filter.isOverlapped(place, clone)) {
 						number++;
 					}
 				}
 				if (0 < number) {
 					cloneDAO.updateChanged(clone, number);
 				}
 			}
 		}
 		System.out.println("done.");
 
 		System.out.print("merging clone sets ... ");
 		for (final Clone place : changedPlaces) {
 			final SortedSet<Integer> mergedSetIDs = new TreeSet<Integer>();
 			for (final Entry<Integer, List<Clone>> cloneset : clones.entrySet()) {
 				final int setID = cloneset.getKey();
 				for (final Clone clone : cloneset.getValue()) {
 					if (filter.isOverlapped(place, clone)) {
 						mergedSetIDs.add(setID);
 					}
 				}
 			}
 			if (1 < mergedSetIDs.size()) {
				cloneDAO.updateGroupID(mergedSetIDs.first(), mergedSetIDs);
 			}
 		}
		cloneDAO.close();
 		System.out.println(": done.");
 
 		final long endTime = System.nanoTime();
 		System.out.print("execution time: ");
 		System.out.println(TimingUtility.getExecutionTime(startTime, endTime));
 	}
 
 	private List<Clone> getChangedPlaces(final String path,
 			final List<Statement> statements, final List<Statement> pattern) {
 
 		int pIndex = 0;
 		int startLine = 0;
 		int endLine = 0;
 		final List<Clone> changedPlaces = new ArrayList<Clone>();
 		for (int index = 0; index < statements.size(); index++) {
 
 			if (statements.get(index).hash == pattern.get(pIndex).hash) {
 				if (0 == pIndex) {
 					startLine = statements.get(index).getStartLine();
 				}
 				pIndex++;
 				if (pIndex == pattern.size()) {
 					pIndex = 0;
 					endLine = statements.get(index).getEndLine();
 					changedPlaces.add(new Clone(path, 0, startLine, endLine));
 					startLine = 0;
 					endLine = 0;
 				}
 			}
 
 			else {
 				pIndex = 0;
 			}
 		}
 
 		return changedPlaces;
 	}
 
 	private boolean isOverlapped(final Clone place, final Clone clone) {
 
 		if (!place.path.equals(clone.path)) {
 			return false;
 		}
 
 		else if ((place.startLine < clone.startLine)
 				&& (place.endLine < clone.startLine)) {
 			return false;
 		}
 
 		else if ((place.startLine > clone.endLine)
 				&& (place.endLine > clone.endLine)) {
 			return false;
 		}
 
 		else {
 			return true;
 		}
 	}
 }
