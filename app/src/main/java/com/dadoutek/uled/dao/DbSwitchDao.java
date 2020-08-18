package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.dbModel.DbSwitch;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_SWITCH".
*/
public class DbSwitchDao extends AbstractDao<DbSwitch, Long> {

    public static final String TABLENAME = "DB_SWITCH";

    /**
     * Properties of entity DbSwitch.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MeshAddr = new Property(1, int.class, "meshAddr", false, "MESH_ADDR");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property ControlGroupAddr = new Property(3, int.class, "controlGroupAddr", false, "CONTROL_GROUP_ADDR");
        public final static Property MacAddr = new Property(4, String.class, "macAddr", false, "MAC_ADDR");
        public final static Property ProductUUID = new Property(5, int.class, "productUUID", false, "PRODUCT_UUID");
        public final static Property ControlSceneId = new Property(6, String.class, "controlSceneId", false, "CONTROL_SCENE_ID");
        public final static Property Index = new Property(7, int.class, "index", false, "INDEX");
        public final static Property BelongGroupId = new Property(8, Long.class, "belongGroupId", false, "BELONG_GROUP_ID");
        public final static Property Rssi = new Property(9, int.class, "rssi", false, "RSSI");
        public final static Property Keys = new Property(10, String.class, "keys", false, "KEYS");
        public final static Property GroupIds = new Property(11, String.class, "groupIds", false, "GROUP_IDS");
        public final static Property SceneIds = new Property(12, String.class, "sceneIds", false, "SCENE_IDS");
        public final static Property ControlGroupAddrs = new Property(13, String.class, "controlGroupAddrs", false, "CONTROL_GROUP_ADDRS");
        public final static Property Version = new Property(14, String.class, "version", false, "VERSION");
        public final static Property IsMostNew = new Property(15, boolean.class, "isMostNew", false, "IS_MOST_NEW");
        public final static Property BoundMac = new Property(16, String.class, "boundMac", false, "BOUND_MAC");
        public final static Property RouterName = new Property(17, String.class, "routerName", false, "ROUTER_NAME");
        public final static Property RouterId = new Property(18, long.class, "routerId", false, "ROUTER_ID");
        public final static Property IsChecked = new Property(19, Boolean.class, "isChecked", false, "IS_CHECKED");
        public final static Property IsSupportOta = new Property(20, boolean.class, "isSupportOta", false, "IS_SUPPORT_OTA");
        public final static Property Type = new Property(21, int.class, "type", false, "TYPE");
    }


    public DbSwitchDao(DaoConfig config) {
        super(config);
    }
    
    public DbSwitchDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_SWITCH\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"MESH_ADDR\" INTEGER NOT NULL ," + // 1: meshAddr
                "\"NAME\" TEXT," + // 2: name
                "\"CONTROL_GROUP_ADDR\" INTEGER NOT NULL ," + // 3: controlGroupAddr
                "\"MAC_ADDR\" TEXT," + // 4: macAddr
                "\"PRODUCT_UUID\" INTEGER NOT NULL ," + // 5: productUUID
                "\"CONTROL_SCENE_ID\" TEXT," + // 6: controlSceneId
                "\"INDEX\" INTEGER NOT NULL ," + // 7: index
                "\"BELONG_GROUP_ID\" INTEGER," + // 8: belongGroupId
                "\"RSSI\" INTEGER NOT NULL ," + // 9: rssi
                "\"KEYS\" TEXT," + // 10: keys
                "\"GROUP_IDS\" TEXT," + // 11: groupIds
                "\"SCENE_IDS\" TEXT," + // 12: sceneIds
                "\"CONTROL_GROUP_ADDRS\" TEXT," + // 13: controlGroupAddrs
                "\"VERSION\" TEXT," + // 14: version
                "\"IS_MOST_NEW\" INTEGER NOT NULL ," + // 15: isMostNew
                "\"BOUND_MAC\" TEXT," + // 16: boundMac
                "\"ROUTER_NAME\" TEXT," + // 17: routerName
                "\"ROUTER_ID\" INTEGER NOT NULL ," + // 18: routerId
                "\"IS_CHECKED\" INTEGER," + // 19: isChecked
                "\"IS_SUPPORT_OTA\" INTEGER NOT NULL ," + // 20: isSupportOta
                "\"TYPE\" INTEGER NOT NULL );"); // 21: type
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_SWITCH\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbSwitch entity) {
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
        stmt.bindLong(4, entity.getControlGroupAddr());
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getProductUUID());
 
        String controlSceneId = entity.getControlSceneId();
        if (controlSceneId != null) {
            stmt.bindString(7, controlSceneId);
        }
        stmt.bindLong(8, entity.getIndex());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(9, belongGroupId);
        }
        stmt.bindLong(10, entity.getRssi());
 
        String keys = entity.getKeys();
        if (keys != null) {
            stmt.bindString(11, keys);
        }
 
        String groupIds = entity.getGroupIds();
        if (groupIds != null) {
            stmt.bindString(12, groupIds);
        }
 
        String sceneIds = entity.getSceneIds();
        if (sceneIds != null) {
            stmt.bindString(13, sceneIds);
        }
 
        String controlGroupAddrs = entity.getControlGroupAddrs();
        if (controlGroupAddrs != null) {
            stmt.bindString(14, controlGroupAddrs);
        }
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(15, version);
        }
        stmt.bindLong(16, entity.getIsMostNew() ? 1L: 0L);
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(17, boundMac);
        }
 
        String routerName = entity.getRouterName();
        if (routerName != null) {
            stmt.bindString(18, routerName);
        }
        stmt.bindLong(19, entity.getRouterId());
 
        Boolean isChecked = entity.getIsChecked();
        if (isChecked != null) {
            stmt.bindLong(20, isChecked ? 1L: 0L);
        }
        stmt.bindLong(21, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(22, entity.getType());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbSwitch entity) {
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
        stmt.bindLong(4, entity.getControlGroupAddr());
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getProductUUID());
 
        String controlSceneId = entity.getControlSceneId();
        if (controlSceneId != null) {
            stmt.bindString(7, controlSceneId);
        }
        stmt.bindLong(8, entity.getIndex());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(9, belongGroupId);
        }
        stmt.bindLong(10, entity.getRssi());
 
        String keys = entity.getKeys();
        if (keys != null) {
            stmt.bindString(11, keys);
        }
 
        String groupIds = entity.getGroupIds();
        if (groupIds != null) {
            stmt.bindString(12, groupIds);
        }
 
        String sceneIds = entity.getSceneIds();
        if (sceneIds != null) {
            stmt.bindString(13, sceneIds);
        }
 
        String controlGroupAddrs = entity.getControlGroupAddrs();
        if (controlGroupAddrs != null) {
            stmt.bindString(14, controlGroupAddrs);
        }
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(15, version);
        }
        stmt.bindLong(16, entity.getIsMostNew() ? 1L: 0L);
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(17, boundMac);
        }
 
        String routerName = entity.getRouterName();
        if (routerName != null) {
            stmt.bindString(18, routerName);
        }
        stmt.bindLong(19, entity.getRouterId());
 
        Boolean isChecked = entity.getIsChecked();
        if (isChecked != null) {
            stmt.bindLong(20, isChecked ? 1L: 0L);
        }
        stmt.bindLong(21, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(22, entity.getType());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbSwitch readEntity(Cursor cursor, int offset) {
        DbSwitch entity = new DbSwitch( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // meshAddr
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.getInt(offset + 3), // controlGroupAddr
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // macAddr
            cursor.getInt(offset + 5), // productUUID
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // controlSceneId
            cursor.getInt(offset + 7), // index
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // belongGroupId
            cursor.getInt(offset + 9), // rssi
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // keys
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // groupIds
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // sceneIds
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // controlGroupAddrs
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // version
            cursor.getShort(offset + 15) != 0, // isMostNew
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // boundMac
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // routerName
            cursor.getLong(offset + 18), // routerId
            cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0, // isChecked
            cursor.getShort(offset + 20) != 0, // isSupportOta
            cursor.getInt(offset + 21) // type
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbSwitch entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMeshAddr(cursor.getInt(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setControlGroupAddr(cursor.getInt(offset + 3));
        entity.setMacAddr(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setProductUUID(cursor.getInt(offset + 5));
        entity.setControlSceneId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setIndex(cursor.getInt(offset + 7));
        entity.setBelongGroupId(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setRssi(cursor.getInt(offset + 9));
        entity.setKeys(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setGroupIds(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setSceneIds(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setControlGroupAddrs(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setVersion(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setIsMostNew(cursor.getShort(offset + 15) != 0);
        entity.setBoundMac(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setRouterName(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setRouterId(cursor.getLong(offset + 18));
        entity.setIsChecked(cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0);
        entity.setIsSupportOta(cursor.getShort(offset + 20) != 0);
        entity.setType(cursor.getInt(offset + 21));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbSwitch entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbSwitch entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbSwitch entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
