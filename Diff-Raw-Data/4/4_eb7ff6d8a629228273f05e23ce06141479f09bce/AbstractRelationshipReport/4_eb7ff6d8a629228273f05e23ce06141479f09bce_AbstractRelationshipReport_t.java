 package org.commonjava.redhat.maven.rv.report;
 
 import static org.apache.commons.io.IOUtils.closeQuietly;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.graph.common.ref.ProjectVersionRef;
 import org.apache.maven.graph.effective.EProjectWeb;
 import org.apache.maven.graph.effective.rel.ProjectRelationship;
 import org.commonjava.redhat.maven.rv.ValidationException;
 import org.commonjava.redhat.maven.rv.session.ValidatorSession;
 
 public abstract class AbstractRelationshipReport<T extends ProjectRelationship<?>>
     implements ValidationReport
 {
     public void write( final ValidatorSession session )
         throws IOException, ValidationException
     {
         PrintWriter writer = null;
         try
         {
             writer = session.getReportWriter( this );
 
             final EProjectWeb web = session.getProjectWeb();
 
             final Set<ProjectVersionRef> refs = getProjectReferences( session );
             for ( final ProjectVersionRef ref : refs )
             {
                 final Set<T> rels = filter( web.getDirectRelationships( ref ) );
                 final List<T> digested = sort( rels );
 
                 if ( !digested.isEmpty() )
                 {
                     writer.printf( "\n\n%s:\n-------------------------------------------------\n", ref );
 
                     for ( final T rel : digested )
                     {
                         print( rel, writer, session );
                     }
                 }
             }
         }
         finally
         {
             closeQuietly( writer );
         }
     }
 
     protected abstract void print( T rel, PrintWriter writer, ValidatorSession session );
 
     protected abstract Set<ProjectVersionRef> getProjectReferences( ValidatorSession session );
 
     protected abstract Set<T> filter( Set<ProjectRelationship<?>> rels );
 
     protected abstract List<T> sort( Set<T> rels );
 
 }
