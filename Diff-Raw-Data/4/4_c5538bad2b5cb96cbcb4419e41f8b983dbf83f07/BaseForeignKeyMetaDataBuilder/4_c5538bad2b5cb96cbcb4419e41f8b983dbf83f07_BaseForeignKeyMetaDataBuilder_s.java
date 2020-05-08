 package net.madz.db.core.meta.mutable.impl;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import net.madz.db.core.meta.DottedPath;
 import net.madz.db.core.meta.immutable.ColumnMetaData;
 import net.madz.db.core.meta.immutable.ForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.IndexMetaData;
 import net.madz.db.core.meta.immutable.SchemaMetaData;
 import net.madz.db.core.meta.immutable.TableMetaData;
 import net.madz.db.core.meta.immutable.types.CascadeRule;
 import net.madz.db.core.meta.immutable.types.KeyDeferrability;
 import net.madz.db.core.meta.mutable.ColumnMetaDataBuilder;
 import net.madz.db.core.meta.mutable.ForeignKeyMetaDataBuilder;
 import net.madz.db.core.meta.mutable.IndexMetaDataBuilder;
 import net.madz.db.core.meta.mutable.SchemaMetaDataBuilder;
 import net.madz.db.core.meta.mutable.TableMetaDataBuilder;
 
 public abstract class BaseForeignKeyMetaDataBuilder<SMDB extends SchemaMetaDataBuilder<SMDB, TMDB, CMDB, FMDB, IMDB, SMD, TMD, CMD, FMD, IMD>, TMDB extends TableMetaDataBuilder<SMDB, TMDB, CMDB, FMDB, IMDB, SMD, TMD, CMD, FMD, IMD>, CMDB extends ColumnMetaDataBuilder<SMDB, TMDB, CMDB, FMDB, IMDB, SMD, TMD, CMD, FMD, IMD>, FMDB extends ForeignKeyMetaDataBuilder<SMDB, TMDB, CMDB, FMDB, IMDB, SMD, TMD, CMD, FMD, IMD>, IMDB extends IndexMetaDataBuilder<SMDB, TMDB, CMDB, FMDB, IMDB, SMD, TMD, CMD, FMD, IMD>, SMD extends SchemaMetaData<SMD, TMD, CMD, FMD, IMD>, TMD extends TableMetaData<SMD, TMD, CMD, FMD, IMD>, CMD extends ColumnMetaData<SMD, TMD, CMD, FMD, IMD>, FMD extends ForeignKeyMetaData<SMD, TMD, CMD, FMD, IMD>, IMD extends IndexMetaData<SMD, TMD, CMD, FMD, IMD>>
         extends BaseMetaDataBuilder<FMD> implements ForeignKeyMetaDataBuilder<SMDB, TMDB, CMDB, FMDB, IMDB, SMD, TMD, CMD, FMD, IMD>,
         ForeignKeyMetaData<SMD, TMD, CMD, FMD, IMD> {
 
     protected List<ForeignKeyMetaData.Entry<SMD, TMD, CMD, FMD, IMD>> entryList = new LinkedList<ForeignKeyMetaData.Entry<SMD, TMD, CMD, FMD, IMD>>();
     protected CascadeRule updateRule, deleteRule;
     // TODO [Tracy] should it be included in jdbc only?
     protected KeyDeferrability deferrability;
     protected TMDB pkTable, fkTable;
     protected IMDB pkIndex, fkIndex;
     protected DottedPath foreignKeyPath;
 
     public class Entry implements ForeignKeyMetaData.Entry<SMD, TMD, CMD, FMD, IMD> {
 
         private final CMD fkColumn;
         private final CMD pkColumn;
         private final FMD key;
        private Short seq;
 
         public Entry(CMD fkColumn, CMD pkColumn, FMD key, Short seq) {
             super();
             this.fkColumn = fkColumn;
             this.pkColumn = pkColumn;
             this.key = key;
         }
 
         @Override
         public CMD getForeignKeyColumn() {
             return this.fkColumn;
         }
 
         @Override
         public CMD getPrimaryKeyColumn() {
             return this.pkColumn;
         }
 
         @Override
         public FMD getKey() {
             return this.key;
         }
 
         
         public Short getSeq() {
             return seq;
         }
         
     }
 
     @Override
     public String getForeignKeyName() {
         return this.foreignKeyPath.getName();
     }
 
     @Override
     public IMD getForeignKeyIndex() {
         return this.fkIndex.getMetaData();
     }
 
     @Override
     public TMD getForeignKeyTable() {
         return this.fkTable.getMetaData();
     }
 
     @Override
     public IMD getPrimaryKeyIndex() {
         return this.pkIndex.getMetaData();
     }
 
     @Override
     public TMD getPrimaryKeyTable() {
         return this.pkTable.getMetaData();
     }
 
     @Override
     public CascadeRule getDeleteCascadeRule() {
         return this.deleteRule;
     }
 
     @Override
     public CascadeRule getUpdateCascadeRule() {
         return this.updateRule;
     }
 
     @Override
     public KeyDeferrability getKeyDeferrability() {
         return this.deferrability;
     }
 
     @Override
     public List<ForeignKeyMetaData.Entry<SMD, TMD, CMD, FMD, IMD>> getEntrySet() {
         return this.entryList;
     }
 
     @Override
     public Integer size() {
         return this.entryList.size();
     }
 
     @Override
     public void addEntry(ForeignKeyMetaData.Entry<SMD, TMD, CMD, FMD, IMD> entry) {
         this.entryList.add(entry);
     }
 }
