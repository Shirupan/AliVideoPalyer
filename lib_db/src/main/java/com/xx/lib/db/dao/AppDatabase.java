package com.xx.lib.db.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.xx.lib.db.entity.UserSetting;
import com.xx.lib.db.entity.UserSystem;

/**
 * @author someone
 * @date 2019-05-30
 */
@Database(entities = {UserSystem.class,
        UserSetting.class},
        version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DBName = "userDB.db";
    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = createDB(context);
                }
            }
        }
        return instance;
    }

    private static AppDatabase createDB(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DBName)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                    }
                })
                // .addMigrations(Migration_1_2)
                .build();
    }

    //获取单个dao实例
    public abstract UserSystemDao getUserDao();

    public abstract UserSettingDao getUserSettingDao();

    /**
     * 版本库升级
     */
    private static Migration Migration_1_2 = new Migration(1, 2) {


        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };
}
