package com.xx.lib.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.xx.lib.db.entity.UserSetting;

import java.util.List;

import io.reactivex.Flowable;

/**
 * @author someone
 * @date 2019-05-30
 */
@Dao
public interface UserSettingDao extends CommonDao<UserSetting> {

    @Query("SELECT * FROM  UserSetting")
    Flowable<List<UserSetting>> getAll();

    @Query("SELECT * FROM UserSetting")
    List<UserSetting> getAllList();

    @Query("SELECT * FROM UserSetting WHERE token = :token")
    Flowable<List<UserSetting>> getSettingByToken(String token);

}
