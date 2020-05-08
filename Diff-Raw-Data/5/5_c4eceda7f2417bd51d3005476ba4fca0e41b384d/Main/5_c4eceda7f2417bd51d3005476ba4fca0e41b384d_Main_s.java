 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.admin.tool.pruneloop;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.neo4j.admin.tool.RecordProcessor;
 import org.neo4j.admin.tool.SimpleStoreTool;
 import org.neo4j.kernel.impl.nioneo.store.Filter;
 import org.neo4j.kernel.impl.nioneo.store.NodeRecord;
 import org.neo4j.kernel.impl.nioneo.store.NodeStoreAccess;
 import org.neo4j.kernel.impl.nioneo.store.RelationshipRecord;
 import org.neo4j.kernel.impl.nioneo.store.RelationshipStoreAccess;
 
 public class Main extends SimpleStoreTool implements RecordProcessor<NodeRecord>
 {
     private final RelationshipStoreAccess rels = store.getRelStore();
     private final NodeStoreAccess nodes = store.getNodeStore();
     private final long[] rootNodes;
 
     Main( String[] args )
     {
         super( args );
         rootNodes = new long[args.length - 1];
         for ( int i = 0; i < rootNodes.length; i++ )
         {
             rootNodes[i] = Long.parseLong( args[i + 1] );
         }
     }
 
     public static void main( String[] args ) throws Throwable
     {
         main( Main.class, args );
     }
 
     @Override
     @SuppressWarnings( "unchecked" )
     protected void run() throws Throwable
     {
         if ( rootNodes.length > 0 )
             for ( long startNode : rootNodes )
             {
                 fixChainOf( startNode, nodes.forceGetRecord( startNode ) );
             }
         else
             process( this, nodes, Filter.IN_USE );
     }
 
     @Override
     public void process( NodeRecord record )
     {
         fixChainOf( record.getId(), record );
     }
 
     private void fixChainOf( final long nodeId, final NodeRecord node )
     {
         Set<Long> seen = new HashSet<Long>();
         RelationshipRecord rel = null;
        for ( long curId = node.getNextRel(), prevId = RelationshipStoreAccess.NO_NEXT_RECORD, nextId; curId != RelationshipStoreAccess.NO_NEXT_RECORD; prevId = curId, curId = nextId )
         {
             RelationshipRecord prev = rel;
             rel = rels.forceGetRecord( curId );
             boolean linked = false;
             if ( rel.inUse() )
             {
                 if ( rel.getFirstNode() == nodeId )
                 {
                     nextId = rel.getFirstNextRel();
                     boolean fixed = false;
                     try
                     {
                         if ( rel.getFirstPrevRel() != prevId )
                         {
                             rel.setFirstPrevRel( prevId );
                             fixed = true;
                         }
                         if ( !seen.add( Long.valueOf( nextId ) ) )
                         {
                             rel.setFirstNextRel( RelationshipStoreAccess.NO_NEXT_RECORD );
                             fixed = true;
                             return;
                         }
                     }
                     finally
                     {
                         if ( fixed )
                         {
                             System.out.println( rel );
                             rels.forceUpdateRecord( rel );
                         }
                         linked = true;
                     }
                 }
                 if ( rel.getSecondNode() == nodeId )
                 {
                     nextId = rel.getSecondNextRel();
                     boolean fixed = false;
                     try
                     {
                         if ( rel.getSecondPrevRel() != prevId )
                         {
                             rel.setSecondPrevRel( prevId );
                             fixed = true;
                         }
                         if ( !seen.add( Long.valueOf( nextId ) ) )
                         {
                             rel.setSecondNextRel( RelationshipStoreAccess.NO_NEXT_RECORD );
                             fixed = true;
                             return;
                         }
                     }
                     finally
                     {
                         if ( fixed )
                         {
                             System.out.println( rel );
                             rels.forceUpdateRecord( rel );
                         }
                         linked = true;
                     }
                 }
             }
             if ( !linked )
             {
                 // the relationship did not reference the node - break the chain
                 if ( prev != null )
                 { // in the middle of the chain
                     if ( prev.getFirstNode() == nodeId )
                     {
                         prev.setFirstNextRel( RelationshipStoreAccess.NO_NEXT_RECORD );
                     }
                     if ( prev.getSecondNode() == nodeId )
                     {
                         prev.setSecondNextRel( RelationshipStoreAccess.NO_NEXT_RECORD );
                     }
                     System.out.println( prev );
                     rels.forceUpdateRecord( prev );
                     return; // chain broken
                 }
                 else
                 { // in the start of the chain
                     node.setNextRel( RelationshipStoreAccess.NO_NEXT_RECORD );
                     System.out.println( node );
                     nodes.forceUpdateRecord( node );
                     return; // chain broken
                 }
             }
            else
                return; // should never happen
         }
     }
 }
