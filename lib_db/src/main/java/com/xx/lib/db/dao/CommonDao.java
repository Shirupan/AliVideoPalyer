package com.xx.lib.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

/**
 * @author someone
 * @date 2019-05-30
 */
@Dao
public interface CommonDao<T> {
    @Insert
    void insert(T... item);

    @Update
    void update(T item);

    @Delete
    void delete(T... item);

}
