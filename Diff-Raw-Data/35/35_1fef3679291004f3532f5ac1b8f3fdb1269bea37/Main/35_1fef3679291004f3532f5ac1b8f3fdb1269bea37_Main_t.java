 package com.reviewer.model;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import com.reviewer.commands.AddComment;
 import com.reviewer.commands.AddFileComment;
 import com.reviewer.commands.NewReview;
 import com.reviewer.git.Git;
 import com.reviewer.git.Git.GitFile;
 import com.reviewer.git.Git.Tree;
 import com.reviewer.git.GitObject;
 import com.reviewer.io.Context;
 
 public class Main {
 	
 	private static String getLastCommit() throws IOException {
 		String commit = Git.rev_parse("refs/heads/review");
 		
 		if(!"refs/heads/review".equals(commit))
 			return commit;
 		
 		return null;
 	}
 
 	private static void updateGitInfo(String commit, List<? extends Command> commands) throws IOException {
 		String[] commit_info = Git.author_name_and_date(commit).split("[\t]");
 		String author = commit_info[0];
 		Date date = new Date(Long.parseLong(commit_info[1]) * 1000);
 		
 		for(Command command : commands) {
 			if(command instanceof GitObject)
 				((GitObject) command).gitInfo(commit, author, date);
 		}
 	}
 	
 	private static List<Command> getCommands(String commit) throws IOException {
 		Context context = new Context();
 		
 		List<Command> commands = new ArrayList<Command>();
 		
 		for(GitFile file : Git.ls_tree(commit)) {
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			Git.cat_file(file.sha1, out);
 			
 			try {
 				context.setBytes(out.toByteArray());
 				
 				List<Command> deserialized = context.deserialize();
 				
 				updateGitInfo(commit, deserialized);
 				commands.addAll(deserialized);
 			} catch(Exception e) {
 			}
 		}
 		
 		return commands;
 	}
 	
 	private static List<Command> getCommands(List<String> commits) throws IOException {
 		List<Command> commands = new ArrayList<Command>();
 		
 		for(String commit : commits)
 			commands.addAll(getCommands(commit));
 		
 		return commands;
 	}
 	
 	private static String cherryPick(String commit, String parent) throws IOException {
 		String tree = Git.get_tree(commit);
 		
 		String newCommit = Git.commit_tree("new review", tree, parent);
 		Git.update_ref("refs/heads/review", newCommit);
 		
 		return newCommit;
 	}
 	
 	private static String commitCommands(List<? extends Command> commands) throws IOException {
 		Context context = new Context();
 		context.serialize(commands);
 		
 		String sha1 = Git.hash_object(context.getBytes());
 		
 		Tree tree = Git.mktree();
 		tree.addBlob(Git.FileMode.REGULAR_NON_EXECUTABLE, sha1, "review.bin");
 		tree.end();
 		
 		String commit = Git.commit_tree("new review", tree.sha1, getLastCommit());
 		Git.update_ref("refs/heads/review", commit);
 		
 		updateGitInfo(commit, commands);
 		
 		return commit;
 	}
 	
 	private static void calculateHash(NewReview newReview) throws IOException {
 		String msg = "committer " + Git.var("GIT_COMMITTER_IDENT") + "\n";
 		
 		for(int i = 0; i < newReview.commits.size(); i++) {
 			String commit = newReview.commits.get(i);
 			
 			msg += "commit " + commit + "\n";
 		}
 		
 		msg += "\n" + newReview.message;
 		
 		newReview.reviewId = Git.calculate_hash(msg.getBytes());
 	}
 	
 	private static NewReview crateReview(String message, String... commits) throws Exception {
 		NewReview newReview = new NewReview();
 		newReview.message = message;
 		newReview.commits.addAll(Arrays.asList(commits));
 		calculateHash(newReview);
 		return newReview;
 	}
 	
 	private static AddFileComment createFileComment(String reviewId, String file, int line, String comment) {
 		AddFileComment addFileComment = new AddFileComment();
 		addFileComment.comment = comment;
 		addFileComment.file = file;
 		addFileComment.line = 123;
 		addFileComment.reviewId = reviewId;
 		return addFileComment;
 	}
 
 	private static AddComment createComment(String reviewId, String comment) {
 		AddComment addComment = new AddComment();
 		addComment.comment = comment;
 		addComment.reviewId = reviewId;
 		return addComment;
 	}
 	
 	private static void saveLocalCache(Reviewer reviewer) throws IOException {
 		saveLocalCache(reviewer, getLastCommit());
 	}
 	
 	private static void saveLocalCache(Reviewer reviewer, String lastCommit) throws IOException {
 		reviewer.lastCommit = lastCommit;
 		ObjectOutput output = new ObjectOutputStream(new FileOutputStream(getFile()));
 		output.writeObject(reviewer);
 		output.close();
 	}
 	
 	private static Reviewer loadLocalCache() throws IOException {
 		Reviewer reviewer;
 		
 		try {
 			ObjectInput input = new ObjectInputStream(new FileInputStream(getFile()));
 			reviewer = (Reviewer) input.readObject();
 		} catch(Exception ex) {
 			reviewer = new Reviewer();
 		}
 		
 		String reviewLocal = Git.rev_parse("refs/heads/review");
 		String reviewRemote = Git.rev_parse("refs/remotes/origin/review");
 		
 		if(reviewRemote != null && "refs/heads/review".equals(reviewLocal))
 			Git.update_ref("refs/heads/review", reviewRemote);
 		
 		doRebase(reviewer);
 		updateMemory(reviewer);
 		
 		return reviewer;
 	}
 
 	private static void updateMemory(Reviewer reviewer) throws IOException {
 		List<String> commits = Git.rev_list_range_reversed(reviewer.lastCommit, "refs/heads/review");
 		
 		if(!commits.isEmpty())
 			reviewer.apply(getCommands(commits));
 	}
 
 	private static void doRebase(Reviewer reviewer) throws IOException {
 		int counts[] = Git.rev_list_count_behind_and_ahead("refs/remotes/origin/review", "refs/heads/review");
 		
		if(counts[0] == 0 && counts[1] == 0)
			return;
 		
		if(counts[0] != 0 && counts[1] != 0) {
			List<String> nonFastForwardCommits = Git.rev_list_range_reversed("refs/remotes/origin/review", "refs/heads/review");
			
			Git.update_ref("refs/heads/review", "refs/remotes/origin/review");
			
			for(String commit : nonFastForwardCommits)
				cherryPick(commit, getLastCommit());
		} else {
			List<String> commits = Git.rev_list_range_reversed(reviewer.lastCommit, "refs/remotes/origin/review");
			
			if(!commits.isEmpty())
				reviewer.apply(getCommands(commits));
			
			Git.update_ref("refs/heads/review", "refs/remotes/origin/review");
			saveLocalCache(reviewer, Git.rev_parse("refs/remotes/origin/review"));
 		}
 	}
 	
 	private static void sendToServer(Reviewer reviewer) throws IOException {
 		if(Git.push("origin", "review"))
 			saveLocalCache(reviewer);
 	}
 	
 	private static void cleanup() throws IOException {
 		Git.delete_branch("review", true);
 		Git.delete_remote_branch("origin", "review");
 		getFile().delete();
 	}
 
 	private static File getFile() throws IOException {
 		return new File(Git.git_dir() + File.separator + "review.bin");
 	}
 	
 	public static void main(String[] args) throws Exception {
 //		cleanup();
 		
 		Reviewer reviewer = loadLocalCache();
 		
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		String line;
 		
 		while((line = in.readLine()) != null) {
 			String[] arg = line.split("[ ]");
 			
 			if("new-review".equals(arg[0])) {
 				NewReview newReview = crateReview(arg[1], Arrays.copyOfRange(arg, 2, arg.length));
 				commitCommands(Arrays.asList(newReview));
 				reviewer.apply(Arrays.asList(newReview));
 				
 				System.out.println(String.format("new review created: %s", newReview.reviewId));
 			}
 			
 			if("list-review".equals(arg[0])) {
 				List<Review> reviews = reviewer.getReviews();
 				for(Review review : reviews)
 					System.out.println(String.format("%s %s", review.reviewId, review.message));
 			}
 			
 			if("list-comments".equals(arg[0])) {
 				Review review = reviewer.findReview(arg[1]);
 				
 				for(Comment comment : review.comments)
 					System.out.println(comment);
 			}
 			
 			if("list-file-comments".equals(arg[0])) {
 				Review review = reviewer.findReview(arg[1]);
 				
 				for(FileComment fileComment : review.fileComments)
 					System.out.println(fileComment);
 			}
 			
 			if("add-comment".equals(arg[0])) {
 				AddComment addComment = createComment(arg[1], arg[2]);
 				commitCommands(Arrays.asList(addComment));
 				reviewer.apply(Arrays.asList(addComment));
 				
 				System.out.println(String.format("review commented"));
 			}
 			
 			if("add-file-comment".equals(arg[0])) {
 				AddFileComment addFileComment = createFileComment(arg[1], arg[2], Integer.parseInt(arg[3]), arg[4]);
 				commitCommands(Arrays.asList(addFileComment));
 				reviewer.apply(Arrays.asList(addFileComment));
 				
 				System.out.println(String.format("file commented"));
 			}
 			
 			if("cleanup".equals(arg[0])) {
 				cleanup();
 				
 				System.out.println("you just make a big shit");
 			}
 			
 			if("send".equals(arg[0])) {
 				sendToServer(reviewer);
 				
 				System.out.println("all local changes was sent to server");
 			}
 		}
 	}
 }
