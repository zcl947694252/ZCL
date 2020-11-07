package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.dbModel.DbLight;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_LIGHT".
*/
public class DbLightDao extends AbstractDao<DbLight, Long> {

    public static final String TABLENAME = "DB_LIGHT";

    /**
     * Properties of entity DbLight.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MeshAddr = new Property(1, int.class, "meshAddr", false, "MESH_ADDR");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property GroupName = new Property(3, String.class, "groupName", false, "GROUP_NAME");
        public final static Property Brightness = new Property(4, int.class, "brightness", false, "BRIGHTNESS");
        public final static Property ColorTemperature = new Property(5, int.class, "colorTemperature", false, "COLOR_TEMPERATURE");
        public final static Property MacAddr = new Property(6, String.class, "macAddr", false, "MAC_ADDR");
        public final static Property SixMac = new Property(7, String.class, "sixMac", false, "SIX_MAC");
        public final static Property MeshUUID = new Property(8, int.class, "meshUUID", false, "MESH_UUID");
        public final static Property ProductUUID = new Property(9, int.class, "productUUID", false, "PRODUCT_UUID");
        public final static Property BelongGroupId = new Property(10, Long.class, "belongGroupId", false, "BELONG_GROUP_ID");
        public final static Property Index = new Property(11, int.class, "index", false, "INDEX");
        public final static Property BoundMac = new Property(12, String.class, "boundMac", false, "BOUND_MAC");
        public final static Property Color = new Property(13, int.class, "color", false, "COLOR");
        public final static Property Version = new Property(14, String.class, "version", false, "VERSION");
        public final static Property BoundMacName = new Property(15, String.class, "boundMacName", false, "BOUND_MAC_NAME");
        public final static Property Status = new Property(16, int.class, "status", false, "STATUS");
        public final static Property Rssi = new Property(17, int.class, "rssi", false, "RSSI");
        public final static Property IsSupportOta = new Property(18, boolean.class, "isSupportOta", false, "IS_SUPPORT_OTA");
        public final static Property IsMostNew = new Property(19, boolean.class, "isMostNew", false, "IS_MOST_NEW");
        public final static Property IsGetVersion = new Property(20, boolean.class, "isGetVersion", false, "IS_GET_VERSION");
        public final static Property BelongRegionId = new Property(21, int.class, "belongRegionId", false, "BELONG_REGION_ID");
        public final static Property Uid = new Property(22, int.class, "uid", false, "UID");
    }


    public DbLightDao(DaoConfig config) {
        super(config);
    }
    
    public DbLightDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_LIGHT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"MESH_ADDR\" INTEGER NOT NULL ," + // 1: meshAddr
                "\"NAME\" TEXT," + // 2: name
                "\"GROUP_NAME\" TEXT," + // 3: groupName
                "\"BRIGHTNESS\" INTEGER NOT NULL ," + // 4: brightness
                "\"COLOR_TEMPERATURE\" INTEGER NOT NULL ," + // 5: colorTemperature
                "\"MAC_ADDR\" TEXT," + // 6: macAddr
                "\"SIX_MAC\" TEXT," + // 7: sixMac
                "\"MESH_UUID\" INTEGER NOT NULL ," + // 8: meshUUID
                "\"PRODUCT_UUID\" INTEGER NOT NULL ," + // 9: productUUID
                "\"BELONG_GROUP_ID\" INTEGER," + // 10: belongGroupId
                "\"INDEX\" INTEGER NOT NULL ," + // 11: index
                "\"BOUND_MAC\" TEXT," + // 12: boundMac
                "\"COLOR\" INTEGER NOT NULL ," + // 13: color
                "\"VERSION\" TEXT," + // 14: version
                "\"BOUND_MAC_NAME\" TEXT," + // 15: boundMacName
                "\"STATUS\" INTEGER NOT NULL ," + // 16: status
                "\"RSSI\" INTEGER NOT NULL ," + // 17: rssi
                "\"IS_SUPPORT_OTA\" INTEGER NOT NULL ," + // 18: isSupportOta
                "\"IS_MOST_NEW\" INTEGER NOT NULL ," + // 19: isMostNew
                "\"IS_GET_VERSION\" INTEGER NOT NULL ," + // 20: isGetVersion
                "\"BELONG_REGION_ID\" INTEGER NOT NULL ," + // 21: belongRegionId
                "\"UID\" INTEGER NOT NULL );"); // 22: uid
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_LIGHT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbLight entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getMeshAddr());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String groupName = entity.getGroupName();
        if (groupName != null) {
            stmt.bindString(4, groupName);
        }
        stmt.bindLong(5, entity.getBrightness());
        stmt.bindLong(6, entity.getColorTemperature());
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(7, macAddr);
        }
 
        String sixMac = entity.getSixMac();
        if (sixMac != null) {
            stmt.bindString(8, sixMac);
        }
        stmt.bindLong(9, entity.getMeshUUID());
        stmt.bindLong(10, entity.getProductUUID());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(11, belongGroupId);
        }
        stmt.bindLong(12, entity.getIndex());
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(13, boundMac);
        }
        stmt.bindLong(14, entity.getColor());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(15, version);
        }
 
        String boundMacName = entity.getBoundMacName();
        if (boundMacName != null) {
            stmt.bindString(16, boundMacName);
        }
        stmt.bindLong(17, entity.getStatus());
        stmt.bindLong(18, entity.getRssi());
        stmt.bindLong(19, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(20, entity.getIsMostNew() ? 1L: 0L);
        stmt.bindLong(21, entity.getIsGetVersion() ? 1L: 0L);
        stmt.bindLong(22, entity.getBelongRegionId());
        stmt.bindLong(23, entity.getUid());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbLight entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getMeshAddr());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String groupName = entity.getGroupName();
        if (groupName != null) {
            stmt.bindString(4, groupName);
        }
        stmt.bindLong(5, entity.getBrightness());
        stmt.bindLong(6, entity.getColorTemperature());
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(7, macAddr);
        }
 
        String sixMac = entity.getSixMac();
        if (sixMac != null) {
            stmt.bindString(8, sixMac);
        }
        stmt.bindLong(9, entity.getMeshUUID());
        stmt.bindLong(10, entity.getProductUUID());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(11, belongGroupId);
        }
        stmt.bindLong(12, entity.getIndex());
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(13, boundMac);
        }
        stmt.bindLong(14, entity.getColor());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(15, version);
        }
 
        String boundMacName = entity.getBoundMacName();
        if (boundMacName != null) {
            stmt.bindString(16, boundMacName);
        }
        stmt.bindLong(17, entity.getStatus());
        stmt.bindLong(18, entity.getRssi());
        stmt.bindLong(19, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(20, entity.getIsMostNew() ? 1L: 0L);
        stmt.bindLong(21, entity.getIsGetVersion() ? 1L: 0L);
        stmt.bindLong(22, entity.getBelongRegionId());
        stmt.bindLong(23, entity.getUid());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbLight readEntity(Cursor cursor, int offset) {
        DbLight entity = new DbLight( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // meshAddr
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // groupName
            cursor.getInt(offset + 4), // brightness
            cursor.getInt(offset + 5), // colorTemperature
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // macAddr
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // sixMac
            cursor.getInt(offset + 8), // meshUUID
            cursor.getInt(offset + 9), // productUUID
            cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10), // belongGroupId
            cursor.getInt(offset + 11), // index
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // boundMac
            cursor.getInt(offset + 13), // color
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // version
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // boundMacName
            cursor.getInt(offset + 16), // status
            cursor.getInt(offset + 17), // rssi
            cursor.getShort(offset + 18) != 0, // isSupportOta
            cursor.getShort(offset + 19) != 0, // isMostNew
            cursor.getShort(offset + 20) != 0, // isGetVersion
            cursor.getInt(offset + 21), // belongRegionId
            cursor.getInt(offset + 22) // uid
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbLight entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMeshAddr(cursor.getInt(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setGroupName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setBrightness(cursor.getInt(offset + 4));
        entity.setColorTemperature(cursor.getInt(offset + 5));
        entity.setMacAddr(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSixMac(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setMeshUUID(cursor.getInt(offset + 8));
        entity.setProductUUID(cursor.getInt(offset + 9));
        entity.setBelongGroupId(cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10));
        entity.setIndex(cursor.getInt(offset + 11));
        entity.setBoundMac(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setColor(cursor.getInt(offset + 13));
        entity.setVersion(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setBoundMacName(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setStatus(cursor.getInt(offset + 16));
        entity.setRssi(cursor.getInt(offset + 17));
        entity.setIsSupportOta(cursor.getShort(offset + 18) != 0);
        entity.setIsMostNew(cursor.getShort(offset + 19) != 0);
        entity.setIsGetVersion(cursor.getShort(offset + 20) != 0);
        entity.setBelongRegionId(cursor.getInt(offset + 21));
        entity.setUid(cursor.getInt(offset + 22));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbLight entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbLight entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbLight entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
