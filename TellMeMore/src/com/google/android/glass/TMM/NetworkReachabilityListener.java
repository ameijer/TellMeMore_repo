package com.google.android.glass.TMM;

/**
 * Classes that want to register to the NetworkReachabilityManager to be notified of
 * network reachability events should implment this interface.
 */
public interface NetworkReachabilityListener {

    public void networkReachable();

    public void networkUnreachable();

}
