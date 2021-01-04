package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.dbModel.DbDiyGradient;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_DIY_GRADIENT".
*/
public class DbDiyGradientDao extends AbstractDao<DbDiyGradient, Long> {

    public static final String TABLENAME = "DB_DIY_GRADIENT";

    /**
     * Properties of entity DbDiyGradient.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property Type = new Property(2, int.class, "type", false, "TYPE");
        public final static Property Speed = new Property(3, int.class, "speed", false, "SPEED");
        public final static Property Index = new Property(4, int.class, "index", false, "INDEX");
        public final static Property BelongRegionId = new Property(5, Long.class, "belongRegionId", false, "BELONG_REGION_ID");
        public final static Property Uid = new Property(6, int.class, "uid", false, "UID");
    }

    private DaoSession daoSession;


    public DbDiyGradientDao(DaoConfig config) {
        super(config);
    }
    
    public DbDiyGradientDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_DIY_GRADIENT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"NAME\" TEXT," + // 1: name
                "\"TYPE\" INTEGER NOT NULL ," + // 2: type
                "\"SPEED\" INTEGER NOT NULL ," + // 3: speed
                "\"INDEX\" INTEGER NOT NULL ," + // 4: index
                "\"BELONG_REGION_ID\" INTEGER," + // 5: belongRegionId
                "\"UID\" INTEGER NOT NULL );"); // 6: uid
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_DIY_GRADIENT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbDiyGradient entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
        stmt.bindLong(3, entity.getType());
        stmt.bindLong(4, entity.getSpeed());
        stmt.bindLong(5, entity.getIndex());
 
        Long belongRegionId = entity.getBelongRegionId();
        if (belongRegionId != null) {
            stmt.bindLong(6, belongRegionId);
        }
        stmt.bindLong(7, entity.getUid());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbDiyGradient entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
        stmt.bindLong(3, entity.getType());
        stmt.bindLong(4, entity.getSpeed());
        stmt.bindLong(5, entity.getIndex());
 
        Long belongRegionId = entity.getBelongRegionId();
        if (belongRegionId != null) {
            stmt.bindLong(6, belongRegionId);
        }
        stmt.bindLong(7, entity.getUid());
    }

    @Override
    protected final void attachEntity(DbDiyGradient entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbDiyGradient readEntity(Cursor cursor, int offset) {
        DbDiyGradient entity = new DbDiyGradient( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.getInt(offset + 2), // type
            cursor.getInt(offset + 3), // speed
            cursor.getInt(offset + 4), // index
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // belongRegionId
            cursor.getInt(offset + 6) // uid
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbDiyGradient entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setType(cursor.getInt(offset + 2));
        entity.setSpeed(cursor.getInt(offset + 3));
        entity.setIndex(cursor.getInt(offset + 4));
        entity.setBelongRegionId(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setUid(cursor.getInt(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbDiyGradient entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbDiyGradient entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbDiyGradient entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
