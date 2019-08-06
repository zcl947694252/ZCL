package com.dadoutek.uled.model.HttpModel

import com.dadoutek.uled.model.DbModel.DBUtils
import com.dadoutek.uled.model.DbModel.DbRegion
import com.dadoutek.uled.model.Response
import com.dadoutek.uled.network.NetworkFactory
import com.dadoutek.uled.network.NetworkTransformer
import com.dadoutek.uled.region.bean.RegionBean
import com.dadoutek.uled.region.bean.ShareCodeBean
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RegionModel {

    fun add(token: String, dbRegion: DbRegion, id: Long, changeId: Long?): Observable<String>? {
        return NetworkFactory.getApi()
                .addRegion(token, dbRegion, changeId!!.toInt())
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {
                    DBUtils.deleteDbDataChange(id)
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun update(token: String, rid: Int, dbRegion: DbRegion, id: Long): Observable<String>? {
        return NetworkFactory.getApi()
                .updateRegion(token, rid, dbRegion)
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {
                    DBUtils.deleteDbDataChange(id)
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun delete(token: String, rid: Int, id: Long): Observable<String>? {
        return NetworkFactory.getApi()
                .deleteRegion(token, rid)
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {
                    DBUtils.deleteDbDataChange(id)
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun get(): Observable<MutableList<RegionBean>>? {
        return NetworkFactory.getApi()
                .getRegionActivityList()
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {
                    for (item in it) {
                        val dbRegion = DbRegion()
                        dbRegion.installMeshPwd = item.installMeshPwd
                        dbRegion.controlMeshPwd = item.controlMeshPwd
                        dbRegion.belongAccount = item.belongAccount
                        dbRegion.controlMesh = item.controlMesh
                        dbRegion.installMesh = item.installMesh
                        dbRegion.name = item.name
                        dbRegion.id = item.id
                        DBUtils.saveRegion(dbRegion, true)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getAuthorizerList(): Observable<MutableList<RegionBean>>? {
        return NetworkFactory.getApi()
                .getAuthorizerList()
                .compose(NetworkTransformer())
                .observeOn(Schedulers.io())
                .doOnNext {
                    for (item in it) {
                        val dbRegion = DbRegion()
                        dbRegion.installMeshPwd = item.installMeshPwd
                        dbRegion.controlMeshPwd = item.controlMeshPwd
                        dbRegion.belongAccount = item.belongAccount
                        dbRegion.controlMesh = item.controlMesh
                        dbRegion.installMesh = item.installMesh
                        dbRegion.name = item.name
                        dbRegion.id = item.id

                        DBUtils.saveRegion(dbRegion, true)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    /*区域新接口*/
    fun addRegions(token: String, dbRegion: DbRegion, rid: Long?): Observable<Response<Any>>? {
        return NetworkFactory.getApi()
                .addRegionNew(token, dbRegion, rid!!)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getAuthorizationCode(regionId: Long):Observable<Response<ShareCodeBean>>?{
        return NetworkFactory.getApi()
                .regionAuthorizationCode(regionId)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                }
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun removeAuthorizationCode(regionId: Long, type: Int):Observable<Response<String>>?{
        return NetworkFactory.getApi()
                .removeAuthorizeCode(regionId,type)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                }
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun removeRegion(id: Long):Observable<Response<String>>?{
        return NetworkFactory.getApi()
                .removeRegion(id.toInt())
                .subscribeOn(Schedulers.io())
                .doOnNext {
                }
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun parseQRCode(code: String):Observable<Response<String>>?{
        return NetworkFactory.getApi()
                .parseQRCode(code)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                }
                .observeOn(AndroidSchedulers.mainThread())
    }  fun cancelAuthorize(ref_id:Long, rid:Long):Observable<Response<String>>?{
        return NetworkFactory.getApi()
                .cancelAuthorize(ref_id, rid)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                }
                .observeOn(AndroidSchedulers.mainThread())
    }



}
