 package de.fkoeberle.autocommit.message;
 
 
 public final class WorkedOnPathCMF implements ICommitMessageFactory {
 	StringBuilder stringBuilder = new StringBuilder();
 
 	@CommitMessage
 	public CommitMessageTemplate workedOn = new CommitMessageTemplate(
			Translations.WorkedOnPathCMF_workedOn);
 
 	@Override
 	public String createMessageFor(FileSetDelta delta, ISession session) {
 		String prefix = findCommonPrefix(delta);
 		return workedOn.createMessageWithArgs(prefix);
 	}
 
 	private String findCommonPrefix(FileSetDelta delta) {
 		CommonPrefixFinder finder = new CommonPrefixFinder();
 		for (ModifiedFile file : delta.getChangedFiles()) {
 			finder.checkForShorterPrefix(file.getPath());
 		}
 		for (AddedFile file : delta.getAddedFiles()) {
 			finder.checkForShorterPrefix(file.getPath());
 		}
 		for (RemovedFile file : delta.getRemovedFiles()) {
 			finder.checkForShorterPrefix(file.getPath());
 		}
 		return finder.getPrefix();
 	}
 
 }
