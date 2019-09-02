## Api 接口
  
   
   ```
   public interface ApiService {
    String HOST = "https://apiv1.starschina.com";

    @GET("/cms/sdk/v1.0/stream/list")//获取直播列表
    Observable<ChannelList> getChannelList(@QueryMap Map<String, Object> params);

    @GET("/cms/sdk/v1.0/epg/current")//获取直播当前播放的节目单列表
    Observable<CurrentEpg> getCurrentEpg(@QueryMap Map<String, Object> params,
                                         @Query("stream_id") List<String> ids);

    @GET("/cms/sdk/v1.0/epg/list")//获取所有节⽬目单列列表
    Observable<EpgListEntity> getEpgs(@QueryMap Map<String, Object> params);

    /**
     * 根据点播视频的ID 获取视频的信息与播放流 by maoqitian
     */
    @GET("/cms/sdk/v1.0/video")
    Observable<VodChannelEntity> getVodChannel(@QueryMap Map<String, Object> params, @Query("id") int vodId);
}


   ```
## 网络工具类，retrofit 结合 okhttp
  
  
  ```
  public class NetworkUtils {

    private Retrofit.Builder mRetrofitBuild;

    private static NetworkUtils mInstance;

    private NetworkUtils() {
        mRetrofitBuild = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient());
    }

    public static NetworkUtils getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkUtils();
        }

        return mInstance;
    }

    public <T> T getService(final Class<T> service, String url, boolean useGson) {
        if (useGson) {
            Log.i("demo", "getService");
            Log.i("demo", "url"+url);
            return mRetrofitBuild.addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url).build().create(service);
        }else {
            Log.i("demo", "getService ");
            Log.i("demo", "url"+url);
            return mRetrofitBuild.addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(url).build().create(service);
        }
    }


    public OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        OkHttpClient client = clientBuilder.connectTimeout(30, TimeUnit.MINUTES)
                .writeTimeout(30, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();
        return client;
    }
}



  ```
  
## 数据返回统一处理

  
  ```
  public abstract class BaseObserver<T> implements Observer<T>{


    //请求成功
    public abstract void onSuccess(T result);
    //请求失败
    public abstract void onFailure(Throwable e, String errorMsg);

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(T t) {
       /* if(vodChannelEntity.getErr_code() == -1){
            onFailure(new Exception(vodChannelEntity.getErr_msg()), vodChannelEntity.getErr_msg());//该异常可以汇报服务端
        }else{
            onSuccess(vodChannelEntity);
        }*/
        onSuccess(t);
    }



    /*@Override
    public void onNext(@NonNull CIBNResponseModel<T> tcibnResponseModel) {
           if(tcibnResponseModel.getRet() == -1){
               onFailure(new Exception(tcibnResponseModel.getMsg()), tcibnResponseModel.getMsg());//该异常可以汇报服务端
           }else{
               onSuccess(tcibnResponseModel.getData());
           }
    }*/

    @Override
    public void onError(Throwable e) {
        onFailure(e, RxExceptionUtil.exceptionHandler(e));
    }

    @Override
    public void onComplete() {

    }
}

  ```
  
## 对基本返回数据的再次封装 加入 Dialog  

  
  ```
  public abstract class ProgressObserver<T>  extends BaseObserver<T> {

    private ProgressDialog mMaterialDialog;
    private Context mContext;
    private String mLoadingText;

    public ProgressObserver(Context context){
        this(context, null);
    }

    public ProgressObserver(Context context, String loadingText){
        mContext = context;
        mLoadingText = loadingText;
    }


    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (!d.isDisposed()) {
            mMaterialDialog = new ProgressDialog(mContext);
            mMaterialDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mMaterialDialog.setCanceledOnTouchOutside(false);
            mMaterialDialog.setMessage(mLoadingText == null ? "正在加载中..." : mLoadingText);
            mMaterialDialog.show();
        }
    }

    @Override
    public void onComplete() {
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        super.onError(e);
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
        }
    }
}


  ```
## 异常处理类


  ```
  public class RxExceptionUtil {
    public static String exceptionHandler(Throwable e){
        String errorMsg = "未知错误";
        if (e instanceof UnknownHostException) {
            errorMsg = "网络不可用";
        } else if (e instanceof SocketTimeoutException) {
            errorMsg = "请求网络超时";
        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            errorMsg = convertStatusCode(httpException);
        } else if (e instanceof ParseException || e instanceof JSONException
                || e instanceof com.alibaba.fastjson.JSONException) {
            errorMsg = "数据解析错误";
        }
        return errorMsg;
    }

    private static String convertStatusCode(HttpException httpException) {
        String msg;
        if (httpException.code() >= 500 && httpException.code() < 600) {
            msg = "服务器处理请求出错";
        } else if (httpException.code() >= 400 && httpException.code() < 500) {
            msg = "服务器无法处理请求";
        } else if (httpException.code() >= 300 && httpException.code() < 400) {
            msg = "请求被重定向到其他页面";
        } else {
            msg = httpException.message();
        }
        return msg;
    }
}


  ```
  

## 线程调度 处理内存泄漏  

  
   ```
   public class RxSchedulers {
    public static <T> ObservableTransformer<T, T> observableIO2Main(final Context context) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                Observable<T> observable = upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
                return composeContext(context,observable);
            }
        };
    }


    private static <T> ObservableSource<T> composeContext(Context context, Observable<T> observable) {
        if(context instanceof RxActivity) {
            return (ObservableSource<T>) observable.compose(((RxActivity) context).bindUntilEvent(ActivityEvent.DESTROY));
        } else if(context instanceof RxFragmentActivity){
            return (ObservableSource<T>) observable.compose(((RxFragmentActivity) context).bindUntilEvent(ActivityEvent.DESTROY));
        }else if(context instanceof RxAppCompatActivity){
            return (ObservableSource<T>) observable.compose(((RxAppCompatActivity) context).bindUntilEvent(ActivityEvent.DESTROY));
        }else {
            return observable;
        }
    }
}

 
   ```

## 如何使用
  
  
  ```
  NetworkUtils.getInstance().getService(ApiService.class, ApiService.HOST, true)
				.getVodChannel(ThinkoEnvironment.getRequestParamsMap(),id)
		        .compose(RxSchedulers.observableIO2Main(this))
				.subscribe(new ProgressObserver<VodChannelEntity>(this) {
					@Override
					public void onSuccess(VodChannelEntity result) {
						
					}

					@Override
					public void onFailure(Throwable e, String errorMsg) {
					
					}
				});

  ```



