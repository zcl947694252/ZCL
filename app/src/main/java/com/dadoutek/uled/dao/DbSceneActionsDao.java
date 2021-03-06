package com.dadoutek.uled.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.dadoutek.uled.model.dbModel.DbSceneActions;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DB_SCENE_ACTIONS".
*/
public class DbSceneActionsDao extends AbstractDao<DbSceneActions, Long> {

    public static final String TABLENAME = "DB_SCENE_ACTIONS";

    /**
     * Properties of entity DbSceneActions.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property BelongSceneId = new Property(1, long.class, "belongSceneId", false, "BELONG_SCENE_ID");
        public final static Property GroupAddr = new Property(2, int.class, "groupAddr", false, "GROUP_ADDR");
        public final static Property ColorTemperature = new Property(3, int.class, "colorTemperature", false, "COLOR_TEMPERATURE");
        public final static Property Brightness = new Property(4, int.class, "brightness", false, "BRIGHTNESS");
        public final static Property Color = new Property(5, int.class, "color", false, "COLOR");
        public final static Property IsOn = new Property(6, boolean.class, "isOn", false, "IS_ON");
        public final static Property IsEnableBright = new Property(7, boolean.class, "isEnableBright", false, "IS_ENABLE_BRIGHT");
        public final static Property IsEnableWhiteBright = new Property(8, boolean.class, "isEnableWhiteBright", false, "IS_ENABLE_WHITE_BRIGHT");
        public final static Property DeviceType = new Property(9, int.class, "deviceType", false, "DEVICE_TYPE");
        public final static Property CircleOne = new Property(10, int.class, "circleOne", false, "CIRCLE_ONE");
        public final static Property CircleTwo = new Property(11, int.class, "circleTwo", false, "CIRCLE_TWO");
        public final static Property CircleThree = new Property(12, int.class, "circleThree", false, "CIRCLE_THREE");
        public final static Property CircleFour = new Property(13, int.class, "circleFour", false, "CIRCLE_FOUR");
        public final static Property RgbType = new Property(14, int.class, "rgbType", false, "RGB_TYPE");
        public final static Property GradientType = new Property(15, int.class, "gradientType", false, "GRADIENT_TYPE");
        public final static Property GradientId = new Property(16, int.class, "gradientId", false, "GRADIENT_ID");
        public final static Property GradientSpeed = new Property(17, int.class, "gradientSpeed", false, "GRADIENT_SPEED");
        public final static Property GradientName = new Property(18, String.class, "gradientName", false, "GRADIENT_NAME");
    }

    private Query<DbSceneActions> dbScene_ActionsQuery;

    public DbSceneActionsDao(DaoConfig config) {
        super(config);
    }
    
    public DbSceneActionsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DB_SCENE_ACTIONS\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"BELONG_SCENE_ID\" INTEGER NOT NULL ," + // 1: belongSceneId
                "\"GROUP_ADDR\" INTEGER NOT NULL ," + // 2: groupAddr
                "\"COLOR_TEMPERATURE\" INTEGER NOT NULL ," + // 3: colorTemperature
                "\"BRIGHTNESS\" INTEGER NOT NULL ," + // 4: brightness
                "\"COLOR\" INTEGER NOT NULL ," + // 5: color
                "\"IS_ON\" INTEGER NOT NULL ," + // 6: isOn
                "\"IS_ENABLE_BRIGHT\" INTEGER NOT NULL ," + // 7: isEnableBright
                "\"IS_ENABLE_WHITE_BRIGHT\" INTEGER NOT NULL ," + // 8: isEnableWhiteBright
                "\"DEVICE_TYPE\" INTEGER NOT NULL ," + // 9: deviceType
                "\"CIRCLE_ONE\" INTEGER NOT NULL ," + // 10: circleOne
                "\"CIRCLE_TWO\" INTEGER NOT NULL ," + // 11: circleTwo
                "\"CIRCLE_THREE\" INTEGER NOT NULL ," + // 12: circleThree
                "\"CIRCLE_FOUR\" INTEGER NOT NULL ," + // 13: circleFour
                "\"RGB_TYPE\" INTEGER NOT NULL ," + // 14: rgbType
                "\"GRADIENT_TYPE\" INTEGER NOT NULL ," + // 15: gradientType
                "\"GRADIENT_ID\" INTEGER NOT NULL ," + // 16: gradientId
                "\"GRADIENT_SPEED\" INTEGER NOT NULL ," + // 17: gradientSpeed
                "\"GRADIENT_NAME\" TEXT);"); // 18: gradientName
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DB_SCENE_ACTIONS\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DbSceneActions entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getBelongSceneId());
        stmt.bindLong(3, entity.getGroupAddr());
        stmt.bindLong(4, entity.getColorTemperature());
        stmt.bindLong(5, entity.getBrightness());
        stmt.bindLong(6, entity.getColor());
        stmt.bindLong(7, entity.getIsOn() ? 1L: 0L);
        stmt.bindLong(8, entity.getIsEnableBright() ? 1L: 0L);
        stmt.bindLong(9, entity.getIsEnableWhiteBright() ? 1L: 0L);
        stmt.bindLong(10, entity.getDeviceType());
        stmt.bindLong(11, entity.getCircleOne());
        stmt.bindLong(12, entity.getCircleTwo());
        stmt.bindLong(13, entity.getCircleThree());
        stmt.bindLong(14, entity.getCircleFour());
        stmt.bindLong(15, entity.getRgbType());
        stmt.bindLong(16, entity.getGradientType());
        stmt.bindLong(17, entity.getGradientId());
        stmt.bindLong(18, entity.getGradientSpeed());
 
        String gradientName = entity.getGradientName();
        if (gradientName != null) {
            stmt.bindString(19, gradientName);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DbSceneActions entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getBelongSceneId());
        stmt.bindLong(3, entity.getGroupAddr());
        stmt.bindLong(4, entity.getColorTemperature());
        stmt.bindLong(5, entity.getBrightness());
        stmt.bindLong(6, entity.getColor());
        stmt.bindLong(7, entity.getIsOn() ? 1L: 0L);
        stmt.bindLong(8, entity.getIsEnableBright() ? 1L: 0L);
        stmt.bindLong(9, entity.getIsEnableWhiteBright() ? 1L: 0L);
        stmt.bindLong(10, entity.getDeviceType());
        stmt.bindLong(11, entity.getCircleOne());
        stmt.bindLong(12, entity.getCircleTwo());
        stmt.bindLong(13, entity.getCircleThree());
        stmt.bindLong(14, entity.getCircleFour());
        stmt.bindLong(15, entity.getRgbType());
        stmt.bindLong(16, entity.getGradientType());
        stmt.bindLong(17, entity.getGradientId());
        stmt.bindLong(18, entity.getGradientSpeed());
 
        String gradientName = entity.getGradientName();
        if (gradientName != null) {
            stmt.bindString(19, gradientName);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DbSceneActions readEntity(Cursor cursor, int offset) {
        DbSceneActions entity = new DbSceneActions( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // belongSceneId
            cursor.getInt(offset + 2), // groupAddr
            cursor.getInt(offset + 3), // colorTemperature
            cursor.getInt(offset + 4), // brightness
            cursor.getInt(offset + 5), // color
            cursor.getShort(offset + 6) != 0, // isOn
            cursor.getShort(offset + 7) != 0, // isEnableBright
            cursor.getShort(offset + 8) != 0, // isEnableWhiteBright
            cursor.getInt(offset + 9), // deviceType
            cursor.getInt(offset + 10), // circleOne
            cursor.getInt(offset + 11), // circleTwo
            cursor.getInt(offset + 12), // circleThree
            cursor.getInt(offset + 13), // circleFour
            cursor.getInt(offset + 14), // rgbType
            cursor.getInt(offset + 15), // gradientType
            cursor.getInt(offset + 16), // gradientId
            cursor.getInt(offset + 17), // gradientSpeed
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18) // gradientName
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DbSceneActions entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setBelongSceneId(cursor.getLong(offset + 1));
        entity.setGroupAddr(cursor.getInt(offset + 2));
        entity.setColorTemperature(cursor.getInt(offset + 3));
        entity.setBrightness(cursor.getInt(offset + 4));
        entity.setColor(cursor.getInt(offset + 5));
        entity.setIsOn(cursor.getShort(offset + 6) != 0);
        entity.setIsEnableBright(cursor.getShort(offset + 7) != 0);
        entity.setIsEnableWhiteBright(cursor.getShort(offset + 8) != 0);
        entity.setDeviceType(cursor.getInt(offset + 9));
        entity.setCircleOne(cursor.getInt(offset + 10));
        entity.setCircleTwo(cursor.getInt(offset + 11));
        entity.setCircleThree(cursor.getInt(offset + 12));
        entity.setCircleFour(cursor.getInt(offset + 13));
        entity.setRgbType(cursor.getInt(offset + 14));
        entity.setGradientType(cursor.getInt(offset + 15));
        entity.setGradientId(cursor.getInt(offset + 16));
        entity.setGradientSpeed(cursor.getInt(offset + 17));
        entity.setGradientName(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DbSceneActions entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DbSceneActions entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DbSceneActions entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "actions" to-many relationship of DbScene. */
    public List<DbSceneActions> _queryDbScene_Actions(long belongSceneId) {
        synchronized (this) {
            if (dbScene_ActionsQuery == null) {
                QueryBuilder<DbSceneActions> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.BelongSceneId.eq(null));
                dbScene_ActionsQuery = queryBuilder.build();
            }
        }
        Query<DbSceneActions> query = dbScene_ActionsQuery.forCurrentThread();
        query.setParameter(0, belongSceneId);
        return query.list();
    }

}
