 package kfs.kfsDbi;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.sql.Blob;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  *
  * @author pavedrim
  */
 public class kfsBlob extends kfsColObject {
 
     private final kfsDbServerType serverType;
 
     public kfsBlob(final String name, final String label, final int position, final kfsDbServerType serverType) {
         super(name, label, position, true);
         this.serverType = serverType;
     }
 
     @Override
     public String getColumnCreateTable(kfsDbServerType serverType) {
         if (serverType == kfsDbServerType.kfsDbiMysql) {
             return getColumnName() + " MEDIUMBLOB";
         }
         if (serverType == kfsDbServerType.kfsDbiPostgre) {
             return getColumnName() + " bytea ";
         }
         if (serverType == kfsDbServerType.kfsDbiSqlite) {
             return getColumnName() + " BLOB ";
         }
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Class<?> getColumnJavaClass() {
         return kfsBlobData.class;
     }
 
     @Override
     public Object getObject(kfsRowData row) {
         return (kfsBlobData) super.getObject(row);
     }
 
     public kfsBlobData getData(kfsRowData row) {
         return (kfsBlobData) super.getObject(row);
     }
 
     public void setData(byte[] d, kfsRowData r) {
         super.setObject(new kfsBlobData(d), r);
     }
 
     public void setData(kfsBlobData d, kfsRowData r) {
         super.setObject(d, r);
     }
 
     public void setData(InputStream value, kfsRowData r) {
         this.setObject(new kfsBlobData(value), r);
     }
 
     @Override
     public void setParam(int inx, PreparedStatement ps, kfsRowData row) throws SQLException {
         kfsBlobData data = getData(row);
         if (!data.isNull()) {
             if (serverType == kfsDbServerType.kfsDbiSqlite) {
                 ps.setBytes(inx, data.getBytes());
             } else if (serverType == kfsDbServerType.kfsDbiPostgre) {
                 ps.setBinaryStream(inx, data.getInputStream(), data.getLength());
             } else {
                 ps.setBinaryStream(inx, data.getInputStream(), data.getLength());
             }
         } else {
            if (serverType == kfsDbServerType.kfsDbiPostgre) {
                ps.setBinaryStream(inx, new ByteArrayInputStream(new byte[0]));
            } else {
                ps.setNull(inx, java.sql.Types.BLOB);
            }
         }
     }
 
     @Override
     public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
         if (serverType == kfsDbServerType.kfsDbiOracle) {
             super.setObject(new kfsBlobData(ps.getBinaryStream(inx)), row);
         } else if (serverType == kfsDbServerType.kfsDbiPostgre) {
             super.setObject(new kfsBlobData(ps.getBytes(inx)), row);
         } else if (serverType == kfsDbServerType.kfsDbiSqlite) {
             super.setObject(new kfsBlobData(ps.getBytes(inx)), row);
         } else {
             Blob bl = ps.getBlob(inx);
             if (bl != null) {
                 super.setObject(new kfsBlobData(bl.getBinaryStream()), row);
             } else {
                 super.setObject(new kfsBlobData(new byte[0]), row);
             }
         }
     }
 }
