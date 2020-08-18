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
        public final static Property MeshUUID = new Property(7, int.class, "meshUUID", false, "MESH_UUID");
        public final static Property ProductUUID = new Property(8, int.class, "productUUID", false, "PRODUCT_UUID");
        public final static Property BelongGroupId = new Property(9, Long.class, "belongGroupId", false, "BELONG_GROUP_ID");
        public final static Property Index = new Property(10, int.class, "index", false, "INDEX");
        public final static Property BoundMac = new Property(11, String.class, "boundMac", false, "BOUND_MAC");
        public final static Property Color = new Property(12, int.class, "color", false, "COLOR");
        public final static Property Version = new Property(13, String.class, "version", false, "VERSION");
        public final static Property Status = new Property(14, int.class, "status", false, "STATUS");
        public final static Property Rssi = new Property(15, int.class, "rssi", false, "RSSI");
        public final static Property IsSupportOta = new Property(16, boolean.class, "isSupportOta", false, "IS_SUPPORT_OTA");
        public final static Property IsMostNew = new Property(17, boolean.class, "isMostNew", false, "IS_MOST_NEW");
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
                "\"MESH_UUID\" INTEGER NOT NULL ," + // 7: meshUUID
                "\"PRODUCT_UUID\" INTEGER NOT NULL ," + // 8: productUUID
                "\"BELONG_GROUP_ID\" INTEGER," + // 9: belongGroupId
                "\"INDEX\" INTEGER NOT NULL ," + // 10: index
                "\"BOUND_MAC\" TEXT," + // 11: boundMac
                "\"COLOR\" INTEGER NOT NULL ," + // 12: color
                "\"VERSION\" TEXT," + // 13: version
                "\"STATUS\" INTEGER NOT NULL ," + // 14: status
                "\"RSSI\" INTEGER NOT NULL ," + // 15: rssi
                "\"IS_SUPPORT_OTA\" INTEGER NOT NULL ," + // 16: isSupportOta
                "\"IS_MOST_NEW\" INTEGER NOT NULL );"); // 17: isMostNew
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
        stmt.bindLong(8, entity.getMeshUUID());
        stmt.bindLong(9, entity.getProductUUID());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(10, belongGroupId);
        }
        stmt.bindLong(11, entity.getIndex());
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(12, boundMac);
        }
        stmt.bindLong(13, entity.getColor());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(14, version);
        }
        stmt.bindLong(15, entity.getStatus());
        stmt.bindLong(16, entity.getRssi());
        stmt.bindLong(17, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(18, entity.getIsMostNew() ? 1L: 0L);
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
        stmt.bindLong(8, entity.getMeshUUID());
        stmt.bindLong(9, entity.getProductUUID());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(10, belongGroupId);
        }
        stmt.bindLong(11, entity.getIndex());
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(12, boundMac);
        }
        stmt.bindLong(13, entity.getColor());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(14, version);
        }
        stmt.bindLong(15, entity.getStatus());
        stmt.bindLong(16, entity.getRssi());
        stmt.bindLong(17, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(18, entity.getIsMostNew() ? 1L: 0L);
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
            cursor.getInt(offset + 7), // meshUUID
            cursor.getInt(offset + 8), // productUUID
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9), // belongGroupId
            cursor.getInt(offset + 10), // index
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // boundMac
            cursor.getInt(offset + 12), // color
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // version
            cursor.getInt(offset + 14), // status
            cursor.getInt(offset + 15), // rssi
            cursor.getShort(offset + 16) != 0, // isSupportOta
            cursor.getShort(offset + 17) != 0 // isMostNew
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
        entity.setMeshUUID(cursor.getInt(offset + 7));
        entity.setProductUUID(cursor.getInt(offset + 8));
        entity.setBelongGroupId(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
        entity.setIndex(cursor.getInt(offset + 10));
        entity.setBoundMac(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setColor(cursor.getInt(offset + 12));
        entity.setVersion(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setStatus(cursor.getInt(offset + 14));
        entity.setRssi(cursor.getInt(offset + 15));
        entity.setIsSupportOta(cursor.getShort(offset + 16) != 0);
        entity.setIsMostNew(cursor.getShort(offset + 17) != 0);
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
