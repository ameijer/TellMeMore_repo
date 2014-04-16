/*
 * File: AndroidContext.java
 * Date: Apr 16, 2014
 * 
 * Adapted for ELEC429, Independent Study
 * 
 * Note: this file has been taken from https://github.com/couchbase/couchbase-lite-android/blob/master/src/main/java/com/couchbase/lite/android/AndroidContext.java
 * This file is part of the couchbase lite project 
 */

package com.google.android.glass.TMM;


import java.io.File;

public class AndroidContext implements Context {

    private android.content.Context wrappedContext;
    private NetworkReachabilityManager networkReachabilityManager;

    public AndroidContext(android.content.Context wrappedContext) {
        this.wrappedContext = wrappedContext;
    }

    @Override
    public File getFilesDir() {
        return wrappedContext.getFilesDir();
    }

    @Override
    public void setNetworkReachabilityManager(NetworkReachabilityManager networkReachabilityManager) {
        this.networkReachabilityManager = networkReachabilityManager;
    }

    @Override
    public NetworkReachabilityManager getNetworkReachabilityManager() {
        if (networkReachabilityManager == null) {
            networkReachabilityManager = new AndroidNetworkReachabilityManager(this);
        }
        return networkReachabilityManager;
    }

    public android.content.Context getWrappedContext() {
        return wrappedContext;
    }

}
