 package org.ilaborie.jgit.flow.feature;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 
 import org.eclipse.jgit.api.MergeCommand;
 import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
 import org.eclipse.jgit.lib.Ref;
 import org.ilaborie.jgit.flow.GitFlowCommand;
 import org.ilaborie.jgit.flow.repository.GitFlowRepository;
 
 /**
  * The git-flow feature finish command
  */
 public class FeatureFinishCommand extends GitFlowCommand<MergeResult> {
 
 	/** The feature name */
 	private String name;
 
 	/**
 	 * Instantiates a new git-flow init the command.
 	 * 
 	 * @param repo
 	 *            the repository
 	 */
 	public FeatureFinishCommand(GitFlowRepository repo) {
 		super(repo);
 	}
 
 	/**
 	 * Sets the feature name.
 	 * 
 	 * @param name
 	 *            the feature name
 	 * @return the command
 	 */
 	public FeatureFinishCommand setName(String name) {
 		this.name = checkNotNull(name);
 		return this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jgit.api.GitCommand#call()
 	 */
 	@Override
 	public MergeResult call() throws GitAPIException {
 		checkNotNull(this.name);
 		this.requireGitFlowInitialized();
 		this.requireCleanWorkingTree();
 
 		// Branch name
 		String prefix = this.getConfig().getFeaturePrefix();
 		String branch = prefix + this.name;
 
 		// Checkout to develop
 		String develop = this.getConfig().getDevelopBranch();
 		this.checkoutTo(develop);
 
 		try {
 			// Merge branch to develop
			MergeCommand mergeCmd = this.git.merge().setFastForward(
					FastForwardMode.NO_FF);
 			Ref featureRef = this.getRepository().getRef(branch);
 			mergeCmd.include(featureRef);
 
 			return mergeCmd.call();
 		} catch (IOException e) {
 			throw new WrongRepositoryStateException(
 					"Cannot find feature branch", e);
 		}
 	}
 }
