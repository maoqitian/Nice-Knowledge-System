package com.mao.aidlapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.os.RemoteException
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import com.mao.aidlapplication.bean.ParData
import com.mao.aidlapplication.service.RemoteService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //远程服务对象
    lateinit var mRemoteService: IRemoteService

    var isBind:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bind.setOnClickListener {
            Log.i("TAG", "[ClientActivity] bindRemoteService");

            val intent = Intent(this,RemoteService::class.java)
            bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE)
            isBind = true

            tvShowMsg.text = "开始绑定"
        }

        unbind.setOnClickListener {
            if(!isBind) return@setOnClickListener
            unbindService(mServiceConnection)
            Log.i("TAG", "[ClientActivity] unbindRemoteService ==>");
            tvShowMsg.text = "解除绑定"
        }

        kill.setOnClickListener {

            Log.i("TAG", "[ClientActivity] killRemoteService");
            try {
                Process.killProcess(mRemoteService.pid)

                tvShowMsg.text = "杀死进程"
            } catch (e: RemoteException) {
                e.printStackTrace()

            }
        }


    }



    val remoteMessageListener = object :IRmoteMessageListener.Stub(){

        override fun onReceiveRmoteData(remoteData: ParData) {
            var pidInfo: String? = null
            //当前运行在 Binder线程池中线程 如果访问 UI 线程使用 handler 发送消息即可
            pidInfo = "onReceiveRmoteData"+ remoteData.data2 + ", data1="+ remoteData.data1

            Log.i("TAG", "[ClientActivity] onReceiveRmoteData  "+pidInfo)
        }

    }


    private val mServiceConnection :ServiceConnection = object : ServiceConnection{

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {

             mRemoteService = IRemoteService.Stub.asInterface(p1)

            var pidInfo: String? = null

            val mParData = mRemoteService.parData

            pidInfo = "pid="+ mRemoteService.getPid() + mParData.data2 + ", data1="+ mParData.data1

            tvShowMsg.text = pidInfo
            Log.i("TAG", "[ClientActivity] onServiceConnected  "+pidInfo)

            //注册服务端回调
            mRemoteService.registerListener(remoteMessageListener)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i("TAG", "[ClientActivity] onServiceDisconnected")
            tvShowMsg.text = "解除绑定";

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if(remoteMessageListener.asBinder().isBinderAlive){
            Log.i("TAG", "[ClientActivity] unregisterListener ")
            mRemoteService.unregisterListener(remoteMessageListener)

        }
        unbindService(mServiceConnection)
    }
}