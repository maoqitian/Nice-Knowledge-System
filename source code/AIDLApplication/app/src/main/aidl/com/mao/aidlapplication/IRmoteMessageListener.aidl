// IRmoteMessageListener.aidl
package com.mao.aidlapplication;
import com.mao.aidlapplication.bean.ParData;

// Declare any non-default types here with import statements

interface IRmoteMessageListener {

    void onReceiveRmoteData(in ParData remoteData);
}