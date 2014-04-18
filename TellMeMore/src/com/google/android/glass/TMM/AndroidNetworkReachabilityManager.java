/*
 * File:AndroidNetworkReachabilityManager.java
 * Date: Apr 16, 2014
 * 
 * Adapted for ELEC429, Independent Study
 * 
 * Note: this file has been taken from https://github.com/couchbase/couchbase-lite-android/blob/master/src/main/java/com/couchbase/lite/android/AndroidContext.java
 * This file is part of the couchbase lite project 
 */

package com.google.android.glass.TMM;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;


public class AndroidNetworkReachabilityManager extends NetworkReachabilityManager {

    private boolean listening;
    private android.content.Context wrappedContext;
    private ConnectivityBroadcastReceiver receiver;
    private State state;

    public enum State {
        UNKNOWN,

        /** This state is returned if there is connectivity to any network **/
        CONNECTED,
        /**
         * This state is returned if there is no connectivity to any network. This is set to true
         * under two circumstances:
         * <ul>
         * <li>When connectivity is lost to one network, and there is no other available network to
         * attempt to switch to.</li>
         * <li>When connectivity is lost to one network, and the attempt to switch to another
         * network fails.</li>
         */
        NOT_CONNECTED
    }

    public AndroidNetworkReachabilityManager(AndroidContext context) {
        this.wrappedContext = context.getWrappedContext();
        this.receiver = new ConnectivityBroadcastReceiver();
        this.state = State.UNKNOWN;
    }


    public synchronized void startListening() {
        if (!listening) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            wrappedContext.registerReceiver(receiver, filter);
            listening = true;
        }
    }

    public synchronized void stopListening() {
        if (listening) {
            wrappedContext.unregisterReceiver(receiver);
            listening = false;
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || listening == false) {
                return;
            }

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                state = State.NOT_CONNECTED;
            } else {
                state = State.CONNECTED;
            }

            if (state == State.NOT_CONNECTED) {
                notifyListenersNetworkUneachable();
            }

            if (state == State.CONNECTED) {
                notifyListenersNetworkReachable();
            }

        }
    };

}
