// IRemoteService.aidl
package com.mao.aidlapplication;
import com.mao.aidlapplication.bean.ParData;
import com.mao.aidlapplication.IRmoteMessageListener;

// Declare any non-default types here with import statements

interface IRemoteService {

    int getPid();

    ParData getParData();

    void registerListener(IRmoteMessageListener listener);
    void unregisterListener(IRmoteMessageListener listener);
}
