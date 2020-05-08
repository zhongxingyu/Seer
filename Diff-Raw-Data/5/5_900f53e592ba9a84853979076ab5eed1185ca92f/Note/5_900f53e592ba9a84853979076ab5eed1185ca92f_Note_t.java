 package com.github.nyao.gwtgithub.client;
 
 import com.github.nyao.gwtgithub.client.models.AJSON;
 import com.github.nyao.gwtgithub.client.models.Repo;
 import com.github.nyao.gwtgithub.client.models.gitdata.BlobCreated;
 import com.github.nyao.gwtgithub.client.models.gitdata.Commit;
 import com.github.nyao.gwtgithub.client.models.gitdata.Reference;
 import com.github.nyao.gwtgithub.client.models.gitdata.Tree;
 import com.github.nyao.gwtgithub.client.values.gitdata.BlobValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.CommitValue;
 //import com.github.nyao.gwtgithub.client.values.gitdata.CommiterValue;
import com.github.nyao.gwtgithub.client.values.gitdata.ReferenceCreateValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.TreeValue;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class Note implements EntryPoint {
 
     private final GitHubApi api = new GitHubApi();
     private Repo r;
 
     @Override
     public void onModuleLoad() {
         
         api.setAccessToken("");
         api.getRepo("", "", new CreateBlob());
     }
     
     private final class CreateBlob implements AsyncCallback<AJSON<Repo>> {
 
         @Override
         public void onSuccess(AJSON<Repo> result) {
             r = result.getData();
             // Blob
             BlobValue blob = new BlobValue();
             blob.setContent("hogehoge");
             blob.setEncoding("utf-8");
             api.createBlob(r, blob, new CreateTree());
         }
 
         @Override
         public void onFailure(Throwable caught) {
             caught.printStackTrace();
         }
     }
 
     private final class CreateTree implements AsyncCallback<BlobCreated> {
         @Override
         public void onSuccess(BlobCreated blob) {
             // Tree
             TreeValue rootTree = new TreeValue();
             TreeValue[] tree = new TreeValue[1];
             rootTree.setTree(tree);
             tree[0] = new TreeValue();
             tree[0].setSha(blob.getSha());
             tree[0].setPath("order");
             tree[0].setType("blob");
            tree[0].setMode("100666");
             
             api.createTree(r, rootTree, new CreateCommit());
         }
 
         @Override
         public void onFailure(Throwable caught) {
             caught.printStackTrace();
         }
     }
 
     private final class CreateCommit implements AsyncCallback<Tree> {
         @Override
         public void onSuccess(Tree tree) {
             // Commit
             CommitValue commit = new CommitValue();
             commit.setMessage("a message");
 //            CommiterValue commiter = new CommiterValue();
 //            commiter.setName("Yusuke");
 //            commiter.setEmail("hahah");
 //            commiter.setDate("now");
 //            commit.setAuthor(commiter);
 //            commit.setParents(null);
             commit.setTree(tree.getSha());
             
             api.createCommit(r, commit, new CreateRefrence());
         }
 
         @Override
         public void onFailure(Throwable caught) {
             caught.printStackTrace();
         }
     }
 
     private final class CreateRefrence implements AsyncCallback<Commit> {
         @Override
         public void onSuccess(Commit commit) {
             // Reference
             ReferenceCreateValue ref = new ReferenceCreateValue();
             ref.setRef("refs/heads/boostgh2");
             ref.setSha(commit.getSha());
             api.createReference(r, ref, new AsyncCallback<Reference>() {
                 @Override
                 public void onSuccess(Reference result) {
                     System.out.println(result.getRef());
                     System.out.println(result.getUrl());
                 }
                 
                 @Override
                 public void onFailure(Throwable caught) {
                     caught.printStackTrace();
                 }
             });
         }
 
         @Override
         public void onFailure(Throwable caught) {
             caught.printStackTrace();
         }
     }
 
 }
