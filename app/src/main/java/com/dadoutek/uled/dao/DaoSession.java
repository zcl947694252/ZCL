package com.dadoutek.uled.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.dadoutek.uled.model.DbModel.DbLight;
import com.dadoutek.uled.model.DbModel.DbUser;
import com.dadoutek.uled.model.DbModel.DbGroup;
import com.dadoutek.uled.model.DbModel.DbSceneActions;
import com.dadoutek.uled.model.DbModel.DbDataChange;
import com.dadoutek.uled.model.DbModel.DbRegion;
import com.dadoutek.uled.model.DbModel.DbScene;

import com.dadoutek.uled.dao.DbLightDao;
import com.dadoutek.uled.dao.DbUserDao;
import com.dadoutek.uled.dao.DbGroupDao;
import com.dadoutek.uled.dao.DbSceneActionsDao;
import com.dadoutek.uled.dao.DbDataChangeDao;
import com.dadoutek.uled.dao.DbRegionDao;
import com.dadoutek.uled.dao.DbSceneDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dbLightDaoConfig;
    private final DaoConfig dbUserDaoConfig;
    private final DaoConfig dbGroupDaoConfig;
    private final DaoConfig dbSceneActionsDaoConfig;
    private final DaoConfig dbDataChangeDaoConfig;
    private final DaoConfig dbRegionDaoConfig;
    private final DaoConfig dbSceneDaoConfig;

    private final DbLightDao dbLightDao;
    private final DbUserDao dbUserDao;
    private final DbGroupDao dbGroupDao;
    private final DbSceneActionsDao dbSceneActionsDao;
    private final DbDataChangeDao dbDataChangeDao;
    private final DbRegionDao dbRegionDao;
    private final DbSceneDao dbSceneDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dbLightDaoConfig = daoConfigMap.get(DbLightDao.class).clone();
        dbLightDaoConfig.initIdentityScope(type);

        dbUserDaoConfig = daoConfigMap.get(DbUserDao.class).clone();
        dbUserDaoConfig.initIdentityScope(type);

        dbGroupDaoConfig = daoConfigMap.get(DbGroupDao.class).clone();
        dbGroupDaoConfig.initIdentityScope(type);

        dbSceneActionsDaoConfig = daoConfigMap.get(DbSceneActionsDao.class).clone();
        dbSceneActionsDaoConfig.initIdentityScope(type);

        dbDataChangeDaoConfig = daoConfigMap.get(DbDataChangeDao.class).clone();
        dbDataChangeDaoConfig.initIdentityScope(type);

        dbRegionDaoConfig = daoConfigMap.get(DbRegionDao.class).clone();
        dbRegionDaoConfig.initIdentityScope(type);

        dbSceneDaoConfig = daoConfigMap.get(DbSceneDao.class).clone();
        dbSceneDaoConfig.initIdentityScope(type);

        dbLightDao = new DbLightDao(dbLightDaoConfig, this);
        dbUserDao = new DbUserDao(dbUserDaoConfig, this);
        dbGroupDao = new DbGroupDao(dbGroupDaoConfig, this);
        dbSceneActionsDao = new DbSceneActionsDao(dbSceneActionsDaoConfig, this);
        dbDataChangeDao = new DbDataChangeDao(dbDataChangeDaoConfig, this);
        dbRegionDao = new DbRegionDao(dbRegionDaoConfig, this);
        dbSceneDao = new DbSceneDao(dbSceneDaoConfig, this);

        registerDao(DbLight.class, dbLightDao);
        registerDao(DbUser.class, dbUserDao);
        registerDao(DbGroup.class, dbGroupDao);
        registerDao(DbSceneActions.class, dbSceneActionsDao);
        registerDao(DbDataChange.class, dbDataChangeDao);
        registerDao(DbRegion.class, dbRegionDao);
        registerDao(DbScene.class, dbSceneDao);
    }
    
    public void clear() {
        dbLightDaoConfig.clearIdentityScope();
        dbUserDaoConfig.clearIdentityScope();
        dbGroupDaoConfig.clearIdentityScope();
        dbSceneActionsDaoConfig.clearIdentityScope();
        dbDataChangeDaoConfig.clearIdentityScope();
        dbRegionDaoConfig.clearIdentityScope();
        dbSceneDaoConfig.clearIdentityScope();
    }

    public DbLightDao getDbLightDao() {
        return dbLightDao;
    }

    public DbUserDao getDbUserDao() {
        return dbUserDao;
    }

    public DbGroupDao getDbGroupDao() {
        return dbGroupDao;
    }

    public DbSceneActionsDao getDbSceneActionsDao() {
        return dbSceneActionsDao;
    }

    public DbDataChangeDao getDbDataChangeDao() {
        return dbDataChangeDao;
    }

    public DbRegionDao getDbRegionDao() {
        return dbRegionDao;
    }

    public DbSceneDao getDbSceneDao() {
        return dbSceneDao;
    }

}
