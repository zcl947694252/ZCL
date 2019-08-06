package com.dadoutek.uled.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.dadoutek.uled.model.DbModel.DbUser;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_USER".
*/
public class DbUserDao extends AbstractDao<DbUser, Long> {

    public static final String TABLENAME = "DB_USER";

    /**
     * Properties of entity DbUser.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Avatar = new Property(1, String.class, "avatar", false, "AVATAR");
        public final static Property Channel = new Property(2, String.class, "channel", false, "CHANNEL");
        public final static Property Email = new Property(3, String.class, "email", false, "EMAIL");
        public final static Property Name = new Property(4, String.class, "name", false, "NAME");
        public final static Property Account = new Property(5, String.class, "account", false, "ACCOUNT");
        public final static Property Phone = new Property(6, String.class, "phone", false, "PHONE");
        public final static Property Token = new Property(7, String.class, "token", false, "TOKEN");
        public final static Property Password = new Property(8, String.class, "password", false, "PASSWORD");
        public final static Property Last_region_id = new Property(9, String.class, "last_region_id", false, "LAST_REGION_ID");
        public final static Property Authorizer_user_id = new Property(10, String.class, "authorizer_user_id", false, "AUTHORIZER_USER_ID");
    }


    public DbUserDao(DaoConfig config) {
        super(config);
    }
    
    public DbUserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_USER\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"AVATAR\" TEXT," + // 1: avatar
                "\"CHANNEL\" TEXT," + // 2: channel
                "\"EMAIL\" TEXT," + // 3: email
                "\"NAME\" TEXT," + // 4: name
                "\"ACCOUNT\" TEXT," + // 5: account
                "\"PHONE\" TEXT," + // 6: phone
                "\"TOKEN\" TEXT," + // 7: token
                "\"PASSWORD\" TEXT," + // 8: password
                "\"LAST_REGION_ID\" TEXT," + // 9: last_region_id
                "\"AUTHORIZER_USER_ID\" TEXT);"); // 10: authorizer_user_id
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_USER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbUser entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(2, avatar);
        }
 
        String channel = entity.getChannel();
        if (channel != null) {
            stmt.bindString(3, channel);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(4, email);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(5, name);
        }
 
        String account = entity.getAccount();
        if (account != null) {
            stmt.bindString(6, account);
        }
 
        String phone = entity.getPhone();
        if (phone != null) {
            stmt.bindString(7, phone);
        }
 
        String token = entity.getToken();
        if (token != null) {
            stmt.bindString(8, token);
        }
 
        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(9, password);
        }
 
        String last_region_id = entity.getLast_region_id();
        if (last_region_id != null) {
            stmt.bindString(10, last_region_id);
        }
 
        String authorizer_user_id = entity.getAuthorizer_user_id();
        if (authorizer_user_id != null) {
            stmt.bindString(11, authorizer_user_id);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbUser entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(2, avatar);
        }
 
        String channel = entity.getChannel();
        if (channel != null) {
            stmt.bindString(3, channel);
        }
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(4, email);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(5, name);
        }
 
        String account = entity.getAccount();
        if (account != null) {
            stmt.bindString(6, account);
        }
 
        String phone = entity.getPhone();
        if (phone != null) {
            stmt.bindString(7, phone);
        }
 
        String token = entity.getToken();
        if (token != null) {
            stmt.bindString(8, token);
        }
 
        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(9, password);
        }
 
        String last_region_id = entity.getLast_region_id();
        if (last_region_id != null) {
            stmt.bindString(10, last_region_id);
        }
 
        String authorizer_user_id = entity.getAuthorizer_user_id();
        if (authorizer_user_id != null) {
            stmt.bindString(11, authorizer_user_id);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbUser readEntity(Cursor cursor, int offset) {
        DbUser entity = new DbUser( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // avatar
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // channel
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // email
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // name
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // account
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // phone
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // token
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // password
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // last_region_id
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10) // authorizer_user_id
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbUser entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAvatar(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setChannel(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setEmail(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAccount(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPhone(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setToken(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setPassword(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setLast_region_id(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setAuthorizer_user_id(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbUser entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbUser entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbUser entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
