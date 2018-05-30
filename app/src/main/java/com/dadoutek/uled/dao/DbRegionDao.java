package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.DbModel.DbRegion;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_REGION".
*/
public class DbRegionDao extends AbstractDao<DbRegion, Long> {

    public static final String TABLENAME = "DB_REGION";

    /**
     * Properties of entity DbRegion.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ControlMesh = new Property(1, String.class, "controlMesh", false, "CONTROL_MESH");
        public final static Property ControlMeshPwd = new Property(2, String.class, "controlMeshPwd", false, "CONTROL_MESH_PWD");
        public final static Property InstallMesh = new Property(3, String.class, "installMesh", false, "INSTALL_MESH");
        public final static Property InstallMeshPwd = new Property(4, String.class, "installMeshPwd", false, "INSTALL_MESH_PWD");
        public final static Property BelongAccount = new Property(5, String.class, "belongAccount", false, "BELONG_ACCOUNT");
    }


    public DbRegionDao(DaoConfig config) {
        super(config);
    }
    
    public DbRegionDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_REGION\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"CONTROL_MESH\" TEXT," + // 1: controlMesh
                "\"CONTROL_MESH_PWD\" TEXT," + // 2: controlMeshPwd
                "\"INSTALL_MESH\" TEXT," + // 3: installMesh
                "\"INSTALL_MESH_PWD\" TEXT," + // 4: installMeshPwd
                "\"BELONG_ACCOUNT\" TEXT);"); // 5: belongAccount
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_REGION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbRegion entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String controlMesh = entity.getControlMesh();
        if (controlMesh != null) {
            stmt.bindString(2, controlMesh);
        }
 
        String controlMeshPwd = entity.getControlMeshPwd();
        if (controlMeshPwd != null) {
            stmt.bindString(3, controlMeshPwd);
        }
 
        String installMesh = entity.getInstallMesh();
        if (installMesh != null) {
            stmt.bindString(4, installMesh);
        }
 
        String installMeshPwd = entity.getInstallMeshPwd();
        if (installMeshPwd != null) {
            stmt.bindString(5, installMeshPwd);
        }
 
        String belongAccount = entity.getBelongAccount();
        if (belongAccount != null) {
            stmt.bindString(6, belongAccount);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbRegion entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String controlMesh = entity.getControlMesh();
        if (controlMesh != null) {
            stmt.bindString(2, controlMesh);
        }
 
        String controlMeshPwd = entity.getControlMeshPwd();
        if (controlMeshPwd != null) {
            stmt.bindString(3, controlMeshPwd);
        }
 
        String installMesh = entity.getInstallMesh();
        if (installMesh != null) {
            stmt.bindString(4, installMesh);
        }
 
        String installMeshPwd = entity.getInstallMeshPwd();
        if (installMeshPwd != null) {
            stmt.bindString(5, installMeshPwd);
        }
 
        String belongAccount = entity.getBelongAccount();
        if (belongAccount != null) {
            stmt.bindString(6, belongAccount);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbRegion readEntity(Cursor cursor, int offset) {
        DbRegion entity = new DbRegion( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // controlMesh
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // controlMeshPwd
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // installMesh
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // installMeshPwd
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // belongAccount
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbRegion entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setControlMesh(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setControlMeshPwd(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setInstallMesh(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setInstallMeshPwd(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setBelongAccount(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbRegion entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbRegion entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbRegion entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}