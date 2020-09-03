package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.gateway.bean.DbRouter;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_ROUTER".
*/
public class DbRouterDao extends AbstractDao<DbRouter, Long> {

    public static final String TABLENAME = "DB_ROUTER";

    /**
     * Properties of entity DbRouter.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Uid = new Property(1, int.class, "uid", false, "UID");
        public final static Property BelongRegionId = new Property(2, int.class, "belongRegionId", false, "BELONG_REGION_ID");
        public final static Property Name = new Property(3, String.class, "name", false, "NAME");
        public final static Property MacAddr = new Property(4, String.class, "macAddr", false, "MAC_ADDR");
        public final static Property TimeZoneHour = new Property(5, int.class, "timeZoneHour", false, "TIME_ZONE_HOUR");
        public final static Property TimeZoneMin = new Property(6, int.class, "timeZoneMin", false, "TIME_ZONE_MIN");
        public final static Property ProductUUID = new Property(7, int.class, "productUUID", false, "PRODUCT_UUID");
        public final static Property State = new Property(8, int.class, "state", false, "STATE");
        public final static Property Version = new Property(9, String.class, "version", false, "VERSION");
        public final static Property Ble_version = new Property(10, String.class, "ble_version", false, "BLE_VERSION");
        public final static Property Esp_version = new Property(11, String.class, "esp_version", false, "ESP_VERSION");
        public final static Property LastOnlineTime = new Property(12, String.class, "lastOnlineTime", false, "LAST_ONLINE_TIME");
        public final static Property LastOfflineTime = new Property(13, String.class, "lastOfflineTime", false, "LAST_OFFLINE_TIME");
        public final static Property Open = new Property(14, int.class, "open", false, "OPEN");
        public final static Property IsSelect = new Property(15, boolean.class, "isSelect", false, "IS_SELECT");
    }


    public DbRouterDao(DaoConfig config) {
        super(config);
    }
    
    public DbRouterDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_ROUTER\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"UID\" INTEGER NOT NULL ," + // 1: uid
                "\"BELONG_REGION_ID\" INTEGER NOT NULL ," + // 2: belongRegionId
                "\"NAME\" TEXT," + // 3: name
                "\"MAC_ADDR\" TEXT," + // 4: macAddr
                "\"TIME_ZONE_HOUR\" INTEGER NOT NULL ," + // 5: timeZoneHour
                "\"TIME_ZONE_MIN\" INTEGER NOT NULL ," + // 6: timeZoneMin
                "\"PRODUCT_UUID\" INTEGER NOT NULL ," + // 7: productUUID
                "\"STATE\" INTEGER NOT NULL ," + // 8: state
                "\"VERSION\" TEXT," + // 9: version
                "\"BLE_VERSION\" TEXT," + // 10: ble_version
                "\"ESP_VERSION\" TEXT," + // 11: esp_version
                "\"LAST_ONLINE_TIME\" TEXT," + // 12: lastOnlineTime
                "\"LAST_OFFLINE_TIME\" TEXT," + // 13: lastOfflineTime
                "\"OPEN\" INTEGER NOT NULL ," + // 14: open
                "\"IS_SELECT\" INTEGER NOT NULL );"); // 15: isSelect
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_ROUTER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbRouter entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUid());
        stmt.bindLong(3, entity.getBelongRegionId());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(4, name);
        }
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getTimeZoneHour());
        stmt.bindLong(7, entity.getTimeZoneMin());
        stmt.bindLong(8, entity.getProductUUID());
        stmt.bindLong(9, entity.getState());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(10, version);
        }
 
        String ble_version = entity.getBle_version();
        if (ble_version != null) {
            stmt.bindString(11, ble_version);
        }
 
        String esp_version = entity.getEsp_version();
        if (esp_version != null) {
            stmt.bindString(12, esp_version);
        }
 
        String lastOnlineTime = entity.getLastOnlineTime();
        if (lastOnlineTime != null) {
            stmt.bindString(13, lastOnlineTime);
        }
 
        String lastOfflineTime = entity.getLastOfflineTime();
        if (lastOfflineTime != null) {
            stmt.bindString(14, lastOfflineTime);
        }
        stmt.bindLong(15, entity.getOpen());
        stmt.bindLong(16, entity.getIsSelect() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbRouter entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUid());
        stmt.bindLong(3, entity.getBelongRegionId());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(4, name);
        }
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getTimeZoneHour());
        stmt.bindLong(7, entity.getTimeZoneMin());
        stmt.bindLong(8, entity.getProductUUID());
        stmt.bindLong(9, entity.getState());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(10, version);
        }
 
        String ble_version = entity.getBle_version();
        if (ble_version != null) {
            stmt.bindString(11, ble_version);
        }
 
        String esp_version = entity.getEsp_version();
        if (esp_version != null) {
            stmt.bindString(12, esp_version);
        }
 
        String lastOnlineTime = entity.getLastOnlineTime();
        if (lastOnlineTime != null) {
            stmt.bindString(13, lastOnlineTime);
        }
 
        String lastOfflineTime = entity.getLastOfflineTime();
        if (lastOfflineTime != null) {
            stmt.bindString(14, lastOfflineTime);
        }
        stmt.bindLong(15, entity.getOpen());
        stmt.bindLong(16, entity.getIsSelect() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbRouter readEntity(Cursor cursor, int offset) {
        DbRouter entity = new DbRouter( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // uid
            cursor.getInt(offset + 2), // belongRegionId
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // name
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // macAddr
            cursor.getInt(offset + 5), // timeZoneHour
            cursor.getInt(offset + 6), // timeZoneMin
            cursor.getInt(offset + 7), // productUUID
            cursor.getInt(offset + 8), // state
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // version
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // ble_version
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // esp_version
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // lastOnlineTime
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // lastOfflineTime
            cursor.getInt(offset + 14), // open
            cursor.getShort(offset + 15) != 0 // isSelect
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbRouter entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUid(cursor.getInt(offset + 1));
        entity.setBelongRegionId(cursor.getInt(offset + 2));
        entity.setName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setMacAddr(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setTimeZoneHour(cursor.getInt(offset + 5));
        entity.setTimeZoneMin(cursor.getInt(offset + 6));
        entity.setProductUUID(cursor.getInt(offset + 7));
        entity.setState(cursor.getInt(offset + 8));
        entity.setVersion(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setBle_version(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setEsp_version(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setLastOnlineTime(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setLastOfflineTime(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setOpen(cursor.getInt(offset + 14));
        entity.setIsSelect(cursor.getShort(offset + 15) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbRouter entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbRouter entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbRouter entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}