package com.mao.aidlapplication.service

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.mao.aidlapplication.IRemoteService
import com.mao.aidlapplication.IRmoteMessageListener
import com.mao.aidlapplication.bean.ParData
import java.util.concurrent.atomic.AtomicBoolean


/**
 *  author : maoqitian
 *  date : 2021/8/22 15:06
 *  description : 远程 Service
 */
class RemoteService : Service(){

    lateinit var mParData: ParData

    val isServiceDestroyed = AtomicBoolean(false)

    //RemoteCallbackList 为系统提供的专门用于删除跨进程 listener 接口
    var mListenerList = RemoteCallbackList<IRmoteMessageListener>()


    private val TAG :String = "RemoteService"

    override fun onCreate() {
        super.onCreate()
        mParData = ParData()
        mParData.data1 = 10
        mParData.data2 = "远程进程Binder服务获取数据成功"

        Log.i(TAG, "[RemoteService] onCreate");


        //休眠五秒向客户端发送数据
        Thread(RemoteServiceWork()).start()

    }



    inner class  RemoteServiceWork :Runnable{
        override fun run() {
           if(!isServiceDestroyed.get()){
               //休眠五秒 发生数据给客户端
               Thread.sleep(5000)

               val parData = ParData()
               parData.data1 = 11
               parData.data2 = "远程服务自动向客户端发送数据"

               try {
                   callClientToMessage(parData)
               }catch (e:RemoteException){
                   e.printStackTrace()
               }
           }
        }

    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(TAG,"[RemoteService] onBind");
        return mBinder
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "[RemoteService] onUnbind");
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceDestroyed.set(true)
        Log.i(TAG, "[RemoteService] onDestroy");
    }


    private val mBinder: IRemoteService.Stub = object : IRemoteService.Stub() {
        override fun getParData(): ParData {
            return mParData
        }

        //客户端注册监听 方便服务端发生数据给到客户端
        override fun registerListener(listener: IRmoteMessageListener?) {

                mListenerList.register(listener)
                Log.i(TAG, "listener add success ")
        }

        //客户端取消注册监听
        override fun unregisterListener(listener: IRmoteMessageListener?) {

                mListenerList.unregister(listener)
                Log.i(TAG, "listener remove success ")

        }

        @Throws(RemoteException::class)
        override fun getPid(): Int {
            Log.i(TAG, "[RemoteService] getPid()=" + Process.myPid())
            return Process.myPid()
        }


        /**此处可用于权限拦截 */
        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return super.onTransact(code, data, reply, flags)
        }
    }

    //通知注册的客户端回调数据
    fun callClientToMessage(parData: ParData){
        //beginBroadcast 和 finishBroadcast 必须要配对使用
        val beginBroadcast = mListenerList.beginBroadcast()
        for (i in 0 until beginBroadcast){
            val broadcastItem = mListenerList.getBroadcastItem(i)
            Log.i(TAG, "remote service callClientToMessage：$parData.data1")
            broadcastItem.onReceiveRmoteData(parData)
        }

        mListenerList.finishBroadcast()
    }

}