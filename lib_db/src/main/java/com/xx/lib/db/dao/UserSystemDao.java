package com.xx.lib.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.xx.lib.db.entity.UserSystem;

import java.util.List;

import io.reactivex.Flowable;

/**
 * @author someone
 * @date 2019-05-30
 */
@Dao
public interface UserSystemDao extends CommonDao<UserSystem> {
    @Query("SELECT * FROM  UserSystem")
    Flowable<List<UserSystem>> getAll();

    @Query("SELECT * FROM UserSystem")
    List<UserSystem> getAllList();

    @Query("SELECT * FROM UserSystem WHERE uid = :uid")
    List<UserSystem> getUserByUid(long uid);
}
