 package ir.exercise1.textindexer;
 
 import ir.exercise1.textindexer.reader.collection.CollectionReaderInterface;
 import ir.exercise1.textindexer.reader.collection.ClassCollectionReader;
 import ir.exercise1.textindexer.reader.document.TextDocumentReader;
import ir.exercise1.textindexer.reader.file.FilesystemReader;
 import ir.exercise1.textindexer.document.DocumentInterface;
 import ir.exercise1.textindexer.document.ClassDocument;
 import ir.exercise1.textindexer.document.ClassDocumentFactory;
 import ir.exercise1.textindexer.collection.CollectionInterface;
 
 /**
  * Main
  *
  * @author Florian Eckerstorfer <florian@eckerstorfer.co>
  */
 class Main
 {
 
     public static void main(String[] args)
     {
         System.out.println("Let's start by reading files from the file system.");
 
        CollectionReaderInterface reader = new ClassCollectionReader("./data/20_newsgroups_subset", new TextDocumentReader(new ClassDocumentFactory(), new FilesystemReader()));
         CollectionInterface collection = reader.read();
         while (collection.hasNext()) {
             ClassDocument doc = (ClassDocument)collection.next();
             System.out.println(doc.getClassName() + ": " + doc.getName());
             // System.out.println(doc.getContent());
         }
     }
 }
