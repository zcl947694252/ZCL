package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.DbModel.DbSensor;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_SENSOR".
*/
public class DbSensorDao extends AbstractDao<DbSensor, Long> {

    public static final String TABLENAME = "DB_SENSOR";

    /**
     * Properties of entity DbSensor.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MeshAddr = new Property(1, int.class, "meshAddr", false, "MESH_ADDR");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property ControlGroupAddr = new Property(3, String.class, "controlGroupAddr", false, "CONTROL_GROUP_ADDR");
        public final static Property MacAddr = new Property(4, String.class, "macAddr", false, "MAC_ADDR");
        public final static Property ProductUUID = new Property(5, int.class, "productUUID", false, "PRODUCT_UUID");
        public final static Property Index = new Property(6, int.class, "index", false, "INDEX");
        public final static Property BelongGroupId = new Property(7, Long.class, "belongGroupId", false, "BELONG_GROUP_ID");
        public final static Property Version = new Property(8, String.class, "version", false, "VERSION");
        public final static Property Rssi = new Property(9, int.class, "rssi", false, "RSSI");
        public final static Property OpenTag = new Property(10, int.class, "openTag", false, "OPEN_TAG");
        public final static Property SetType = new Property(11, int.class, "setType", false, "SET_TYPE");
        public final static Property SceneId = new Property(12, int.class, "sceneId", false, "SCENE_ID");
    }


    public DbSensorDao(DaoConfig config) {
        super(config);
    }
    
    public DbSensorDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_SENSOR\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"MESH_ADDR\" INTEGER NOT NULL ," + // 1: meshAddr
                "\"NAME\" TEXT," + // 2: name
                "\"CONTROL_GROUP_ADDR\" TEXT," + // 3: controlGroupAddr
                "\"MAC_ADDR\" TEXT," + // 4: macAddr
                "\"PRODUCT_UUID\" INTEGER NOT NULL ," + // 5: productUUID
                "\"INDEX\" INTEGER NOT NULL ," + // 6: index
                "\"BELONG_GROUP_ID\" INTEGER," + // 7: belongGroupId
                "\"VERSION\" TEXT," + // 8: version
                "\"RSSI\" INTEGER NOT NULL ," + // 9: rssi
                "\"OPEN_TAG\" INTEGER NOT NULL ," + // 10: openTag
                "\"SET_TYPE\" INTEGER NOT NULL ," + // 11: setType
                "\"SCENE_ID\" INTEGER NOT NULL );"); // 12: sceneId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_SENSOR\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbSensor entity) {
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
 
        String controlGroupAddr = entity.getControlGroupAddr();
        if (controlGroupAddr != null) {
            stmt.bindString(4, controlGroupAddr);
        }
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getProductUUID());
        stmt.bindLong(7, entity.getIndex());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(8, belongGroupId);
        }
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(9, version);
        }
        stmt.bindLong(10, entity.getRssi());
        stmt.bindLong(11, entity.getOpenTag());
        stmt.bindLong(12, entity.getSetType());
        stmt.bindLong(13, entity.getSceneId());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbSensor entity) {
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
 
        String controlGroupAddr = entity.getControlGroupAddr();
        if (controlGroupAddr != null) {
            stmt.bindString(4, controlGroupAddr);
        }
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getProductUUID());
        stmt.bindLong(7, entity.getIndex());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(8, belongGroupId);
        }
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(9, version);
        }
        stmt.bindLong(10, entity.getRssi());
        stmt.bindLong(11, entity.getOpenTag());
        stmt.bindLong(12, entity.getSetType());
        stmt.bindLong(13, entity.getSceneId());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbSensor readEntity(Cursor cursor, int offset) {
        DbSensor entity = new DbSensor( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // meshAddr
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // controlGroupAddr
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // macAddr
            cursor.getInt(offset + 5), // productUUID
            cursor.getInt(offset + 6), // index
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // belongGroupId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // version
            cursor.getInt(offset + 9), // rssi
            cursor.getInt(offset + 10), // openTag
            cursor.getInt(offset + 11), // setType
            cursor.getInt(offset + 12) // sceneId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbSensor entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMeshAddr(cursor.getInt(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setControlGroupAddr(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setMacAddr(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setProductUUID(cursor.getInt(offset + 5));
        entity.setIndex(cursor.getInt(offset + 6));
        entity.setBelongGroupId(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setVersion(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setRssi(cursor.getInt(offset + 9));
        entity.setOpenTag(cursor.getInt(offset + 10));
        entity.setSetType(cursor.getInt(offset + 11));
        entity.setSceneId(cursor.getInt(offset + 12));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbSensor entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbSensor entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbSensor entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
