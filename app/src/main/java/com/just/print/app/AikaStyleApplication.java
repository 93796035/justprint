package com.just.print.app;

import android.app.Application;

import com.just.print.db.dao.DaoMaster;
import com.just.print.db.dao.MarkDao;
import com.just.print.util.L;

import java.io.File;

public class AikaStyleApplication extends Application {

    public volatile static AikaStyleApplication instance;
    private DaoMaster daoMaster;

//    public UDPService mUDPService;
//    ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mUDPService = ((UDPService.MyBinder) service).getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            L.i(TAG, "service disconnected!");
//        }
//    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        new XCrashHandler().init(this);// for unhanddle.

//        startService(new Intent(this, UDPService.class));
//        bindService(new Intent(this, UDPService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    public void initDaoMaster(String shopCode) {
        if (daoMaster == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                    this,
                    getDBPath(shopCode).getAbsolutePath(),
                    null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
        }
    }

    public File getDBPath(String dbname) {
        return getDatabasePath("JustPrinter_" + dbname);
    }

//    public DaoMaster newDaoMaster(String dbname) {
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
//                getDBPath(dbname).getAbsolutePath(), null);
//        DaoMaster daomaster = new DaoMaster(helper.getWritableDatabase());
//        return daomaster;
//    }

    public DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            L.e("AikaStyle","non-none daoMaster expected! db not inited!", null);
            new NullPointerException("Please initialize database!");
        }
        return daoMaster;
    }

    public static MarkDao getMarkDao(){
        return instance.getDaoMaster().newSession().getMarkDao();
    }
}
