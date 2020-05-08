 package com.github.noctarius.waljdbc.io;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutput;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.junit.Test;
 
 import com.github.noctarius.waljdbc.Journal;
 import com.github.noctarius.waljdbc.JournalEntry;
 import com.github.noctarius.waljdbc.SimpleJournalEntry;
 import com.github.noctarius.waljdbc.exceptions.JournalException;
import com.github.noctarius.waljdbc.exceptions.ReplayCancellationException;
 import com.github.noctarius.waljdbc.io.disk.DiskJournal;
 import com.github.noctarius.waljdbc.spi.JournalEntryReader;
 import com.github.noctarius.waljdbc.spi.JournalEntryWriter;
 import com.github.noctarius.waljdbc.spi.JournalFlushedListener;
 import com.github.noctarius.waljdbc.spi.JournalNamingStrategy;
 import com.github.noctarius.waljdbc.spi.JournalRecordIdGenerator;
 import com.github.noctarius.waljdbc.spi.ReplayNotificationResult;
 
 public class BasicDiskJournalTestCase
 {
 
     private static final String TESTCHARACTERS =
         "QWERTZUIOPÜASDFGHJKLÖÄYXCVBNM;:_*'Ä_?=)(/&%$§!qwertzuiopü+#äölkjhgfdsayxcvbnm,.-@@ł€¶ŧ←↓→øþſðđŋħł«¢„“”µ·…";
 
     @Test
     public void createJournalFile()
         throws Exception
     {
         File path = new File( "target/journals/createJournalFile" );
         path.mkdirs();
 
         DiskJournal<TestRecord> journal =
             new DiskJournal<>( "createJournalFile", path.toPath(), new FlushListener(), 1024 * 1024,
                                new RecordIdGenerator(), new TestRecordReader(), new TestRecordWriter(),
                                new NamingStrategy() );
 
         journal.close();
 
         CountingFlushListener listener = new CountingFlushListener();
         journal =
             new DiskJournal<>( "loadBrokenJournal", path.toPath(), listener, 1024 * 1024, new RecordIdGenerator(),
                                new TestRecordReader(), new TestRecordWriter(), new NamingStrategy() );
         journal.close();
 
         assertEquals( 0, listener.getCount() );
     }
 
     @Test
     public void appendEntries()
         throws Exception
     {
         File path = new File( "target/journals/appendEntries" );
         path.mkdirs();
 
         DiskJournal<TestRecord> journal =
             new DiskJournal<>( "appendEntries", path.toPath(), new FlushListener(), 1024 * 1024,
                                new RecordIdGenerator(), new TestRecordReader(), new TestRecordWriter(),
                                new NamingStrategy() );
 
         JournalEntry<TestRecord> record1 = buildTestRecord( 1, "test1", (byte) 12 );
         JournalEntry<TestRecord> record2 = buildTestRecord( 2, "test2", (byte) 24 );
         JournalEntry<TestRecord> record3 = buildTestRecord( 4, "test3", (byte) 32 );
         JournalEntry<TestRecord> record4 = buildTestRecord( 8, "test4", (byte) 48 );
 
         journal.appendEntry( record1 );
         journal.appendEntry( record2 );
         journal.appendEntry( record3 );
         journal.appendEntry( record4 );
 
         journal.close();
 
         CountingFlushListener listener = new CountingFlushListener();
         journal =
             new DiskJournal<>( "loadBrokenJournal", path.toPath(), listener, 1024 * 1024, new RecordIdGenerator(),
                                new TestRecordReader(), new TestRecordWriter(), new NamingStrategy() );
         journal.close();
 
         assertEquals( 4, listener.getCount() );
         assertEquals( record1, listener.get( 0 ) );
         assertEquals( record2, listener.get( 1 ) );
         assertEquals( record3, listener.get( 2 ) );
         assertEquals( record4, listener.get( 3 ) );
     }
 
     @Test
     public void loadBrokenJournal()
         throws Exception
     {
         File path = new File( "target/journals/loadBrokenJournal" );
         path.mkdirs();
 
         DiskJournal<TestRecord> journal =
             new DiskJournal<>( "loadBrokenJournal", path.toPath(), new FlushListener(), 1024 * 1024,
                                new RecordIdGenerator(), new TestRecordReader(), new TestRecordWriter(),
                                new NamingStrategy() );
 
         JournalEntry<TestRecord> record1 = buildTestRecord( 1, "test1", (byte) 12 );
         JournalEntry<TestRecord> record2 = buildTestRecord( 2, "test2", (byte) 24 );
         JournalEntry<TestRecord> record3 = buildTestRecord( 4, "test3", (byte) 32 );
         JournalEntry<TestRecord> record4 = buildTestRecord( 8, "test4", (byte) 48 );
 
         journal.appendEntry( record1 );
         journal.appendEntry( record2 );
         journal.appendEntry( record3 );
         journal.appendEntry( record4 );
 
         journal.close();
 
         File file = new File( path, "journal-1" );
         RandomAccessFile raf = new RandomAccessFile( file, "rws" );
 
         raf.seek( file.length() - 1 );
         while ( raf.readByte() == 0 )
         {
             raf.seek( raf.getFilePointer() - 2 );
         }
         raf.seek( raf.getFilePointer() - 6 );
 
         byte[] data = new byte[7];
         raf.write( data );
         raf.close();
 
         CountingFlushListener listener = new CountingFlushListener();
         journal =
             new DiskJournal<>( "loadBrokenJournal", path.toPath(), listener, 1024 * 1024, new RecordIdGenerator(),
                                new TestRecordReader(), new TestRecordWriter(), new NamingStrategy() );
         journal.close();
 
         assertEquals( 3, listener.getCount() );
         assertEquals( record1, listener.get( 0 ) );
         assertEquals( record2, listener.get( 1 ) );
         assertEquals( record3, listener.get( 2 ) );
     }
 
     @Test
     public void loadShortenedJournal()
         throws Exception
     {
         File path = new File( "target/journals/loadShortenedJournal" );
         path.mkdirs();
 
         DiskJournal<TestRecord> journal =
             new DiskJournal<>( "loadShortenedJournal", path.toPath(), new FlushListener(), 1024 * 1024,
                                new RecordIdGenerator(), new TestRecordReader(), new TestRecordWriter(),
                                new NamingStrategy() );
 
         JournalEntry<TestRecord> record1 = buildTestRecord( 1, "test1", (byte) 12 );
         JournalEntry<TestRecord> record2 = buildTestRecord( 2, "test2", (byte) 24 );
         JournalEntry<TestRecord> record3 = buildTestRecord( 4, "test3", (byte) 32 );
         JournalEntry<TestRecord> record4 = buildTestRecord( 8, "test4", (byte) 48 );
 
         journal.appendEntry( record1 );
         journal.appendEntry( record2 );
         journal.appendEntry( record3 );
         journal.appendEntry( record4 );
 
         journal.close();
 
         File file = new File( path, "journal-1" );
         RandomAccessFile raf = new RandomAccessFile( file, "rws" );
 
         raf.seek( file.length() - 1 );
         while ( raf.readByte() == 0 )
         {
             raf.seek( raf.getFilePointer() - 2 );
         }
         long length = raf.getFilePointer() - 20;
         raf.setLength( length );
         raf.close();
 
         assertEquals( length, file.length() );
 
         CountingFlushListener listener = new CountingFlushListener();
         journal =
             new DiskJournal<>( "loadShortenedJournal", path.toPath(), listener, 1024 * 1024, new RecordIdGenerator(),
                                new TestRecordReader(), new TestRecordWriter(), new NamingStrategy() );
         journal.close();
 
         assertEquals( 3, listener.getCount() );
         assertEquals( record1, listener.get( 0 ) );
         assertEquals( record2, listener.get( 1 ) );
         assertEquals( record3, listener.get( 2 ) );
     }
 
     @Test
     public void findHolesInJournalAndAcceptIt()
         throws Exception
     {
         File path = new File( "target/journals/findHolesInJournalAndAcceptIt" );
         path.mkdirs();
 
         RecordIdGenerator recordIdGenerator = new RecordIdGenerator();
         DiskJournal<TestRecord> journal =
             new DiskJournal<>( "findHolesInJournalAndAcceptIt", path.toPath(), new FlushListener(), 1024 * 1024,
                                recordIdGenerator, new TestRecordReader(), new TestRecordWriter(), new NamingStrategy() );
 
         JournalEntry<TestRecord> record1 = buildTestRecord( 1, "test1", (byte) 12 );
         JournalEntry<TestRecord> record2 = buildTestRecord( 2, "test2", (byte) 24 );
         JournalEntry<TestRecord> record3 = buildTestRecord( 4, "test3", (byte) 32 );
         JournalEntry<TestRecord> record4 = buildTestRecord( 8, "test4", (byte) 48 );
 
         journal.appendEntry( record1 );
         journal.appendEntry( record2 );
 
         // Force to create a hole in the journal file (in normal operation that should not happen)
         recordIdGenerator.recordId++;
 
         journal.appendEntry( record3 );
         journal.appendEntry( record4 );
 
         journal.close();
 
         File file = new File( path, "journal-1" );
         RandomAccessFile raf = new RandomAccessFile( file, "rws" );
 
         raf.seek( file.length() - 1 );
         while ( raf.readByte() == 0 )
         {
             raf.seek( raf.getFilePointer() - 2 );
         }
         long length = raf.getFilePointer() - 20;
         raf.setLength( length );
         raf.close();
 
         assertEquals( length, file.length() );
 
         CountingFlushListener listener = new CountingFlushListener();
         journal =
             new DiskJournal<>( "findHolesInJournalAndAcceptIt", path.toPath(), listener, 1024 * 1024,
                                new RecordIdGenerator(), new TestRecordReader(), new TestRecordWriter(),
                                new NamingStrategy() );
         journal.close();
 
         assertEquals( 3, listener.getCount() );
         assertEquals( record1, listener.get( 0 ) );
         assertEquals( record2, listener.get( 1 ) );
         assertEquals( record3, listener.get( 2 ) );
     }
 
    @Test( expected = ReplayCancellationException.class )
     public void findHolesInJournalAndDeclineIt()
         throws Exception
     {
         File path = new File( "target/journals/findHolesInJournalAndDeclineIt" );
         path.mkdirs();
 
         RecordIdGenerator recordIdGenerator = new RecordIdGenerator();
         DiskJournal<TestRecord> journal =
             new DiskJournal<>( "findHolesInJournalAndDeclineIt", path.toPath(), new FlushListener(), 1024 * 1024,
                                recordIdGenerator, new TestRecordReader(), new TestRecordWriter(), new NamingStrategy() );
 
         JournalEntry<TestRecord> record1 = buildTestRecord( 1, "test1", (byte) 12 );
         JournalEntry<TestRecord> record2 = buildTestRecord( 2, "test2", (byte) 24 );
         JournalEntry<TestRecord> record3 = buildTestRecord( 4, "test3", (byte) 32 );
         JournalEntry<TestRecord> record4 = buildTestRecord( 8, "test4", (byte) 48 );
 
         journal.appendEntry( record1 );
         journal.appendEntry( record2 );
 
         // Force to create a hole in the journal file (in normal operation that should not happen)
         recordIdGenerator.recordId++;
 
         journal.appendEntry( record3 );
         journal.appendEntry( record4 );
 
         journal.close();
 
         File file = new File( path, "journal-1" );
         RandomAccessFile raf = new RandomAccessFile( file, "rws" );
 
         raf.seek( file.length() - 1 );
         while ( raf.readByte() == 0 )
         {
             raf.seek( raf.getFilePointer() - 2 );
         }
         long length = raf.getFilePointer() - 20;
         raf.setLength( length );
         raf.close();
 
         assertEquals( length, file.length() );
 
         try
         {
             CountingFlushListener listener = new CountingFlushListener( ReplayNotificationResult.Except );
             journal =
                 new DiskJournal<>( "findHolesInJournalAndDeclineIt", path.toPath(), listener, 1024 * 1024,
                                    new RecordIdGenerator(), new TestRecordReader(), new TestRecordWriter(),
                                    new NamingStrategy() );
         }
         catch ( JournalException e )
         {
             e.printStackTrace();
             throw e;
         }
     }
 
     private SimpleJournalEntry<TestRecord> buildTestRecord( int value, String name, byte type )
     {
         Random random = new Random( -System.nanoTime() );
 
         int stringLength = random.nextInt( 100 );
         StringBuilder sb = new StringBuilder( stringLength );
         for ( int i = 0; i < stringLength; i++ )
         {
             int charPos = random.nextInt( TESTCHARACTERS.length() );
             sb.append( TESTCHARACTERS.toCharArray()[charPos] );
         }
 
         TestRecord record = new TestRecord();
         record.value = value + random.nextInt( 1000000 );
         record.name = name + "-" + sb.toString();
         return new SimpleJournalEntry<TestRecord>( record, type );
     }
 
     public static class TestRecord
     {
 
         private int value;
 
         private String name;
 
         @Override
         public String toString()
         {
             return "TestRecord [value=" + value + ", name=" + name + "]";
         }
 
         @Override
         public int hashCode()
         {
             final int prime = 31;
             int result = 1;
             result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
             result = prime * result + value;
             return result;
         }
 
         @Override
         public boolean equals( Object obj )
         {
             if ( this == obj )
                 return true;
             if ( obj == null )
                 return false;
             if ( getClass() != obj.getClass() )
                 return false;
             TestRecord other = (TestRecord) obj;
             if ( name == null )
             {
                 if ( other.name != null )
                     return false;
             }
             else if ( !name.equals( other.name ) )
                 return false;
             if ( value != other.value )
                 return false;
             return true;
         }
 
     }
 
     public static class TestRecordReader
         implements JournalEntryReader<TestRecord>
     {
 
         @Override
         public JournalEntry<TestRecord> readJournalEntry( long recordId, byte type, byte[] data )
             throws IOException
         {
            try (ByteArrayInputStream in = new ByteArrayInputStream( data );
                            DataInputStream buffer = new DataInputStream( in ))
             {
                 int value = buffer.readInt();
                 String name = buffer.readUTF();
                 TestRecord record = new TestRecord();
                 record.value = value;
                 record.name = name;
                 return new SimpleJournalEntry<BasicDiskJournalTestCase.TestRecord>( record, type );
             }
         }
 
     }
 
     public static class TestRecordWriter
         implements JournalEntryWriter<TestRecord>
     {
 
         @Override
         public void writeJournalEntry( JournalEntry<TestRecord> entry, DataOutput out )
             throws IOException
         {
             TestRecord record = entry.getValue();
             out.writeInt( record.value );
             out.writeUTF( record.name );
         }
     }
 
     public static class RecordIdGenerator
         implements JournalRecordIdGenerator
     {
 
         private long recordId = 0;
 
         @Override
         public synchronized long nextRecordId()
         {
             return ++recordId;
         }
 
         @Override
         public long lastGeneratedRecordId()
         {
             return recordId;
         }
 
     }
 
     public static class NamingStrategy
         implements JournalNamingStrategy
     {
 
         @Override
         public String generate( long logNumber )
         {
             return "journal-" + String.valueOf( logNumber );
         }
 
         @Override
         public boolean isJournal( String name )
         {
             return name.startsWith( "journal-" );
         }
 
         @Override
         public long extractLogNumber( String name )
         {
             return Integer.parseInt( name.substring( name.indexOf( '-' ) + 1 ) );
         }
 
     }
 
     public static class FlushListener
         implements JournalFlushedListener<TestRecord>
     {
 
         @Override
         public void flushed( JournalEntry<TestRecord> entry )
         {
             System.out.println( "flushed: " + entry );
         }
 
         @Override
         public void failed( JournalEntry<TestRecord> entry, JournalException cause )
         {
             System.out.println( "failed: " + entry );
         }
 
         @Override
         public ReplayNotificationResult replayNotifySuspiciousRecordId( Journal<TestRecord> journal,
                                                                         JournalEntry<TestRecord> lastEntry,
                                                                         JournalEntry<TestRecord> nextEntry )
         {
             return ReplayNotificationResult.Continue;
         }
 
         @Override
         public ReplayNotificationResult replayRecordId( Journal<TestRecord> journal, JournalEntry<TestRecord> entry )
         {
             return ReplayNotificationResult.Continue;
         }
 
     }
 
     public static class CountingFlushListener
         extends FlushListener
     {
 
         private int count;
 
         private final List<JournalEntry<TestRecord>> records = new ArrayList<>();
 
         private final ReplayNotificationResult missingRecordIdResult;
 
         public CountingFlushListener()
         {
             this( ReplayNotificationResult.Continue );
         }
 
         public CountingFlushListener( ReplayNotificationResult missingRecordIdResult )
         {
             this.missingRecordIdResult = missingRecordIdResult;
         }
 
         @Override
         public ReplayNotificationResult replayNotifySuspiciousRecordId( Journal<TestRecord> journal,
                                                                         JournalEntry<TestRecord> lastEntry,
                                                                         JournalEntry<TestRecord> nextEntry )
         {
             return missingRecordIdResult;
         }
 
         @Override
         public ReplayNotificationResult replayRecordId( Journal<TestRecord> journal, JournalEntry<TestRecord> entry )
         {
             records.add( entry );
             count++;
             return super.replayRecordId( journal, entry );
         }
 
         public int getCount()
         {
             return count;
         }
 
         public JournalEntry<TestRecord> get( int index )
         {
             return records.get( index );
         }
 
     }
 
 }
