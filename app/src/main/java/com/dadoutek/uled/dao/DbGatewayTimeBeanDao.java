package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.gateway.bean.DbGatewayTimeBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_GATEWAY_TIME_BEAN".
*/
public class DbGatewayTimeBeanDao extends AbstractDao<DbGatewayTimeBean, Long> {

    public static final String TABLENAME = "DB_GATEWAY_TIME_BEAN";

    /**
     * Properties of entity DbGatewayTimeBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Label_id = new Property(0, Long.class, "label_id", true, "_id");
        public final static Property Label_switch = new Property(1, int.class, "label_switch", false, "LABEL_SWITCH");
        public final static Property Hour = new Property(2, int.class, "hour", false, "HOUR");
        public final static Property Minute = new Property(3, int.class, "minute", false, "MINUTE");
        public final static Property Week = new Property(4, String.class, "week", false, "WEEK");
        public final static Property Index = new Property(5, int.class, "index", false, "INDEX");
        public final static Property IsNew = new Property(6, Boolean.class, "isNew", false, "IS_NEW");
        public final static Property SceneId = new Property(7, Long.class, "sceneId", false, "SCENE_ID");
        public final static Property SceneName = new Property(8, String.class, "sceneName", false, "SCENE_NAME");
    }


    public DbGatewayTimeBeanDao(DaoConfig config) {
        super(config);
    }
    
    public DbGatewayTimeBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_GATEWAY_TIME_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: label_id
                "\"LABEL_SWITCH\" INTEGER NOT NULL ," + // 1: label_switch
                "\"HOUR\" INTEGER NOT NULL ," + // 2: hour
                "\"MINUTE\" INTEGER NOT NULL ," + // 3: minute
                "\"WEEK\" TEXT," + // 4: week
                "\"INDEX\" INTEGER NOT NULL ," + // 5: index
                "\"IS_NEW\" INTEGER," + // 6: isNew
                "\"SCENE_ID\" INTEGER," + // 7: sceneId
                "\"SCENE_NAME\" TEXT);"); // 8: sceneName
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_GATEWAY_TIME_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbGatewayTimeBean entity) {
        stmt.clearBindings();
 
        Long label_id = entity.getLabel_id();
        if (label_id != null) {
            stmt.bindLong(1, label_id);
        }
        stmt.bindLong(2, entity.getLabel_switch());
        stmt.bindLong(3, entity.getStartHour());
        stmt.bindLong(4, entity.getStartMinute());
 
        String week = entity.getWeek();
        if (week != null) {
            stmt.bindString(5, week);
        }
        stmt.bindLong(6, entity.getIndex());
 
        Boolean isNew = entity.getIsNew();
        if (isNew != null) {
            stmt.bindLong(7, isNew ? 1L: 0L);
        }
 
        Long sceneId = entity.getSceneId();
        if (sceneId != null) {
            stmt.bindLong(8, sceneId);
        }
 
        String sceneName = entity.getSceneName();
        if (sceneName != null) {
            stmt.bindString(9, sceneName);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbGatewayTimeBean entity) {
        stmt.clearBindings();
 
        Long label_id = entity.getLabel_id();
        if (label_id != null) {
            stmt.bindLong(1, label_id);
        }
        stmt.bindLong(2, entity.getLabel_switch());
        stmt.bindLong(3, entity.getStartHour());
        stmt.bindLong(4, entity.getStartMinute());
 
        String week = entity.getWeek();
        if (week != null) {
            stmt.bindString(5, week);
        }
        stmt.bindLong(6, entity.getIndex());
 
        Boolean isNew = entity.getIsNew();
        if (isNew != null) {
            stmt.bindLong(7, isNew ? 1L: 0L);
        }
 
        Long sceneId = entity.getSceneId();
        if (sceneId != null) {
            stmt.bindLong(8, sceneId);
        }
 
        String sceneName = entity.getSceneName();
        if (sceneName != null) {
            stmt.bindString(9, sceneName);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbGatewayTimeBean readEntity(Cursor cursor, int offset) {
        DbGatewayTimeBean entity = new DbGatewayTimeBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // label_id
            cursor.getInt(offset + 1), // label_switch
            cursor.getInt(offset + 2), // hour
            cursor.getInt(offset + 3), // minute
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // week
            cursor.getInt(offset + 5), // index
            cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0, // isNew
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // sceneId
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // sceneName
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbGatewayTimeBean entity, int offset) {
        entity.setLabel_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setLabel_switch(cursor.getInt(offset + 1));
        entity.setStartHour(cursor.getInt(offset + 2));
        entity.setStartMinute(cursor.getInt(offset + 3));
        entity.setWeek(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setIndex(cursor.getInt(offset + 5));
        entity.setIsNew(cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0);
        entity.setSceneId(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setSceneName(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbGatewayTimeBean entity, long rowId) {
        entity.setLabel_id(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbGatewayTimeBean entity) {
        if(entity != null) {
            return entity.getLabel_id();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbGatewayTimeBean entity) {
        return entity.getLabel_id() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
