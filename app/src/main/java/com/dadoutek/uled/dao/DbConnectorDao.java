package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.dbModel.DbConnector;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_CONNECTOR".
*/
public class DbConnectorDao extends AbstractDao<DbConnector, Long> {

    public static final String TABLENAME = "DB_CONNECTOR";

    /**
     * Properties of entity DbConnector.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MeshAddr = new Property(1, int.class, "meshAddr", false, "MESH_ADDR");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Open = new Property(3, boolean.class, "open", false, "OPEN");
        public final static Property MacAddr = new Property(4, String.class, "macAddr", false, "MAC_ADDR");
        public final static Property MeshUUID = new Property(5, int.class, "meshUUID", false, "MESH_UUID");
        public final static Property ProductUUID = new Property(6, int.class, "productUUID", false, "PRODUCT_UUID");
        public final static Property BelongGroupId = new Property(7, Long.class, "belongGroupId", false, "BELONG_GROUP_ID");
        public final static Property Index = new Property(8, int.class, "index", false, "INDEX");
        public final static Property GroupName = new Property(9, String.class, "groupName", false, "GROUP_NAME");
        public final static Property Color = new Property(10, int.class, "color", false, "COLOR");
        public final static Property Version = new Property(11, String.class, "version", false, "VERSION");
        public final static Property BoundMac = new Property(12, String.class, "boundMac", false, "BOUND_MAC");
        public final static Property RouterName = new Property(13, String.class, "routerName", false, "ROUTER_NAME");
        public final static Property BelongRouterMacAddr = new Property(14, String.class, "belongRouterMacAddr", false, "BELONG_ROUTER_MAC_ADDR");
        public final static Property Status = new Property(15, int.class, "status", false, "STATUS");
        public final static Property Rssi = new Property(16, int.class, "rssi", false, "RSSI");
        public final static Property IsSupportOta = new Property(17, boolean.class, "isSupportOta", false, "IS_SUPPORT_OTA");
        public final static Property IsMostNew = new Property(18, boolean.class, "isMostNew", false, "IS_MOST_NEW");
    }


    public DbConnectorDao(DaoConfig config) {
        super(config);
    }
    
    public DbConnectorDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_CONNECTOR\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"MESH_ADDR\" INTEGER NOT NULL ," + // 1: meshAddr
                "\"NAME\" TEXT," + // 2: name
                "\"OPEN\" INTEGER NOT NULL ," + // 3: open
                "\"MAC_ADDR\" TEXT," + // 4: macAddr
                "\"MESH_UUID\" INTEGER NOT NULL ," + // 5: meshUUID
                "\"PRODUCT_UUID\" INTEGER NOT NULL ," + // 6: productUUID
                "\"BELONG_GROUP_ID\" INTEGER," + // 7: belongGroupId
                "\"INDEX\" INTEGER NOT NULL ," + // 8: index
                "\"GROUP_NAME\" TEXT," + // 9: groupName
                "\"COLOR\" INTEGER NOT NULL ," + // 10: color
                "\"VERSION\" TEXT," + // 11: version
                "\"BOUND_MAC\" TEXT," + // 12: boundMac
                "\"ROUTER_NAME\" TEXT," + // 13: routerName
                "\"BELONG_ROUTER_MAC_ADDR\" TEXT," + // 14: belongRouterMacAddr
                "\"STATUS\" INTEGER NOT NULL ," + // 15: status
                "\"RSSI\" INTEGER NOT NULL ," + // 16: rssi
                "\"IS_SUPPORT_OTA\" INTEGER NOT NULL ," + // 17: isSupportOta
                "\"IS_MOST_NEW\" INTEGER NOT NULL );"); // 18: isMostNew
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_CONNECTOR\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbConnector entity) {
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
        stmt.bindLong(4, entity.getOpen() ? 1L: 0L);
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getMeshUUID());
        stmt.bindLong(7, entity.getProductUUID());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(8, belongGroupId);
        }
        stmt.bindLong(9, entity.getIndex());
 
        String groupName = entity.getGroupName();
        if (groupName != null) {
            stmt.bindString(10, groupName);
        }
        stmt.bindLong(11, entity.getColor());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(12, version);
        }
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(13, boundMac);
        }
 
        String routerName = entity.getRouterName();
        if (routerName != null) {
            stmt.bindString(14, routerName);
        }
 
        String belongRouterMacAddr = entity.getBelongRouterMacAddr();
        if (belongRouterMacAddr != null) {
            stmt.bindString(15, belongRouterMacAddr);
        }
        stmt.bindLong(16, entity.getStatus());
        stmt.bindLong(17, entity.getRssi());
        stmt.bindLong(18, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(19, entity.getIsMostNew() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbConnector entity) {
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
        stmt.bindLong(4, entity.getOpen() ? 1L: 0L);
 
        String macAddr = entity.getMacAddr();
        if (macAddr != null) {
            stmt.bindString(5, macAddr);
        }
        stmt.bindLong(6, entity.getMeshUUID());
        stmt.bindLong(7, entity.getProductUUID());
 
        Long belongGroupId = entity.getBelongGroupId();
        if (belongGroupId != null) {
            stmt.bindLong(8, belongGroupId);
        }
        stmt.bindLong(9, entity.getIndex());
 
        String groupName = entity.getGroupName();
        if (groupName != null) {
            stmt.bindString(10, groupName);
        }
        stmt.bindLong(11, entity.getColor());
 
        String version = entity.getVersion();
        if (version != null) {
            stmt.bindString(12, version);
        }
 
        String boundMac = entity.getBoundMac();
        if (boundMac != null) {
            stmt.bindString(13, boundMac);
        }
 
        String routerName = entity.getRouterName();
        if (routerName != null) {
            stmt.bindString(14, routerName);
        }
 
        String belongRouterMacAddr = entity.getBelongRouterMacAddr();
        if (belongRouterMacAddr != null) {
            stmt.bindString(15, belongRouterMacAddr);
        }
        stmt.bindLong(16, entity.getStatus());
        stmt.bindLong(17, entity.getRssi());
        stmt.bindLong(18, entity.getIsSupportOta() ? 1L: 0L);
        stmt.bindLong(19, entity.getIsMostNew() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbConnector readEntity(Cursor cursor, int offset) {
        DbConnector entity = new DbConnector( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // meshAddr
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.getShort(offset + 3) != 0, // open
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // macAddr
            cursor.getInt(offset + 5), // meshUUID
            cursor.getInt(offset + 6), // productUUID
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // belongGroupId
            cursor.getInt(offset + 8), // index
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // groupName
            cursor.getInt(offset + 10), // color
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // version
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // boundMac
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // routerName
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // belongRouterMacAddr
            cursor.getInt(offset + 15), // status
            cursor.getInt(offset + 16), // rssi
            cursor.getShort(offset + 17) != 0, // isSupportOta
            cursor.getShort(offset + 18) != 0 // isMostNew
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbConnector entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMeshAddr(cursor.getInt(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setOpen(cursor.getShort(offset + 3) != 0);
        entity.setMacAddr(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setMeshUUID(cursor.getInt(offset + 5));
        entity.setProductUUID(cursor.getInt(offset + 6));
        entity.setBelongGroupId(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setIndex(cursor.getInt(offset + 8));
        entity.setGroupName(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setColor(cursor.getInt(offset + 10));
        entity.setVersion(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setBoundMac(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setRouterName(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setBelongRouterMacAddr(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setStatus(cursor.getInt(offset + 15));
        entity.setRssi(cursor.getInt(offset + 16));
        entity.setIsSupportOta(cursor.getShort(offset + 17) != 0);
        entity.setIsMostNew(cursor.getShort(offset + 18) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbConnector entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbConnector entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbConnector entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
