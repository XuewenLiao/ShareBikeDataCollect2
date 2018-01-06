package com.hnulab.sharebike.em;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hnulab.sharebike.em.activity.DestinationActivity;
import com.hnulab.sharebike.em.activity.LoginActivity;
import com.hnulab.sharebike.em.activity.MyMessageActivity;
import com.hnulab.sharebike.em.activity.MyWalletActivity;
import com.hnulab.sharebike.em.activity.PersonalInformationActivity;
import com.hnulab.sharebike.em.activity.UserKnowActivity;
import com.hnulab.sharebike.em.broadcast.BluetoothReceiver;
import com.hnulab.sharebike.em.databinding.ActivityMainBinding;
import com.hnulab.sharebike.em.dialog.LoadDialog;
import com.hnulab.sharebike.em.dialog.RedpackageDialog;
import com.hnulab.sharebike.em.entity.EnvData;
import com.hnulab.sharebike.em.entity.RedPackageLocation;
import com.hnulab.sharebike.em.lib.LocationTask;
import com.hnulab.sharebike.em.lib.OnLocationGetListener;
import com.hnulab.sharebike.em.lib.PositionEntity;
import com.hnulab.sharebike.em.lib.PutRedpackageUtils;
import com.hnulab.sharebike.em.lib.RegeocodeTask;
import com.hnulab.sharebike.em.lib.RouteTask;
import com.hnulab.sharebike.em.lib.Sha1;
import com.hnulab.sharebike.em.lib.Utils;
import com.hnulab.sharebike.em.overlay.WalkRouteOverlay;
import com.hnulab.sharebike.em.util.AMapUtil;
import com.hnulab.sharebike.em.util.BluetoothAutoConnectUtils;
import com.hnulab.sharebike.em.util.CommonUtils;
import com.hnulab.sharebike.em.util.Distance;
import com.hnulab.sharebike.em.util.ToastUtil;
import com.hnulab.sharebike.em.view.statusbar.StatusBarUtil;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


//import com.hnulab.sharebike.em.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements AMap.OnCameraChangeListener,
        AMap.OnMapLoadedListener, OnLocationGetListener, View.OnClickListener, RouteTask.OnRouteCalculateListener,
        AMap.OnMapTouchListener, RouteSearch.OnRouteSearchListener, AMap.OnMapClickListener, AMap.InfoWindowAdapter {
    public static final String TAG = "MainActivity";
    public static final int REQUEST_CODE = 1;
    //地图view
    MapView mMapView = null;
    //初始化地图控制器对象
    AMap aMap;
    //刷新定位
    ImageView iv_refresh, iv_scan_code;

    //定位
    private LocationTask mLocationTask;
    //逆地理编码功能
    private RegeocodeTask mRegeocodeTask;
    //绘制点标记
    private Marker mPositionMark, mInitialMark, tempMark;//可移动、圆点、点击
    //初始坐标、移动记录坐标
    private LatLng mStartPosition, mRecordPositon;
    //默认添加一次
    private boolean mIsFirst = true;
    //就第一次显示位置
    private boolean mIsFirstShow = true;

    private LatLng initLocation;

    // 一定需要对应的bean
    private ActivityMainBinding mBinding;

    private NavigationView navView;
    private DrawerLayout drawerLayout;
    private FrameLayout llTitleMenu;
    private Toolbar toolbar;

    private ValueAnimator animator = null;//坐标动画
    private BitmapDescriptor initBitmap, moveBitmap, smallIdentificationBitmap, smallredpacageBitmap, bigIdentificationBitmap, bigredpacageBitmap;//定位圆点、可移动、所有标识（车）
    private RouteSearch mRouteSearch;

    private WalkRouteResult mWalkRouteResult;
    private LatLonPoint mStartPoint = null;//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = null;//终点，116.481288,39.995576
    private final int ROUTE_TYPE_WALK = 3;
    private boolean isClickIdentification = false;
    WalkRouteOverlay walkRouteOverlay;//路线
    private String[] time;
    private String distance;

    //蓝牙广播
    //广播action
    private String ACTION_UPDATEUI = "com.hnulab.sharebike.update";
    //设备mac地址key
    public static String EXTRA_DEVICE_ADDRESS = "address";
    //广播接收者
    BroadcastReceiver broadcastReceiver;
    //获取本地蓝牙适配器，即蓝牙设备
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    //用来保存存储的文件名
    public String filename = "";
    //蓝牙设备
    BluetoothDevice _device = null;
    //蓝牙通信socket
    BluetoothSocket _socket = null;
    boolean bRun = true;
    boolean bThread = false;
    //宏定义查询设备句柄
    private final static int REQUEST_CONNECT_DEVICE = 1;
    //SPP服务UUID号
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    //输入流，用来接收蓝牙数据
    private InputStream is;
    //显示用数据缓存
    private String smsg = "";
    //保存用数据缓存
    private String fmsg = "";
    public boolean flag = true;
    String message = "";
    int nn = 1;

    public String key = "";
    //当前二氧化碳浓度
    private boolean isStartPick = false;
    //最新环境数据
    private static EnvData envData = new EnvData();
    //蓝牙权限
    private int MY_PERMISSION_REQUEST_CONSTANT = 1;


    //时间获取格式
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private List<EnvData> envDatas = new ArrayList<>();//自动传数据集合
    private List<EnvData> redDatas = new ArrayList<>();//主动传数据集合
    private boolean isUpload = false;//上传数据标志位（锁功能）
    private ArrayList<BitmapDescriptor> icons;
    private List<RedPackageLocation> redPackageLocations;
    private Thread redSendThread;//红包所在地主动发数据线程
    private Thread redLocation;//获取红包线程
    private boolean isIinitRed = true;//初始化红包标志位
    private ArrayList<Marker> updataMarkers;
    private double redLongitude;
    private double redLatitude;
    private boolean isSuccessSend = false;//主动传数据成功
    private int timeCount;//刷新时长
    private Timer timer;

    private enum handler_key {
        //自动上传数据成功
        UPLOADSUCCESS,
        //主动上传数据成功
        REDUPLOADSUCCESS,
        //主动上传数据失败（测试用）
        REDUPLOADFAIL,
        //不在采集范围(人没在湖师大)
        OUYOFPALACE,
        //不在获取红包范围内
        OUTOFREDRANGE,
        //当前位置信息
        LOCATION,
        //展示数据
        SHOWDATA,
        //倒计时通知
        TICK_TIME,
        //数据丢失
        DROP_DATA,

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case UPLOADSUCCESS:
                    ToastUtil.show(MainActivity.this, "The data was uploaded successfully" + envData.toString());//数据上传成功
                    break;
                case SHOWDATA:
                    ToastUtil.show(MainActivity.this, "this data：" + envData.toString());//数据上传成功
                    break;
                case REDUPLOADSUCCESS:
                    ToastUtil.show(MainActivity.this, "Red location data uploaded successfully");//红包所在地数据上传成功
                    break;
                case REDUPLOADFAIL:
                    String erro1 = (String) msg.obj;
                    ToastUtil.show(MainActivity.this, "Red card location upload data failed（Not in the specified area）" + erro1);//红包所在地上传数据失败
                    break;
                case OUYOFPALACE:
                    String erro = (String) msg.obj;
                    ToastUtil.show(MainActivity.this, "Your current location is not in the scope of collection" + erro + envData.toString());
                    //您当前位置不在采集范围(人没在湖师大)
                    break;
                case OUTOFREDRANGE:
                    ToastUtil.show(MainActivity.this, "You are not in the red envelope to get the range");//您不在红包获取范围
                    break;
                case LOCATION:
                    ToastUtil.show(MainActivity.this, (String) msg.obj);
                    break;
                case TICK_TIME:
                    if (timeCount > 0) {
                        timeCount--;
//                        ToastUtil.show(MainActivity.this,"正在刷新，"+ timeCount +"s后可继续刷新");
                    } else {
                        timer.cancel();
//                        iv_refresh.setClickable(true);
                        iv_refresh.setEnabled(true);
                    }
                    break;
                case DROP_DATA:
                    ToastUtil.show(MainActivity.this,"数据不完整" + envData.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化xutils3.5
        x.Ext.init(getApplication());
        x.Ext.setDebug(org.xutils.BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        x.view().inject(this);

        ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
        mBinding = DataBindingUtil.setContentView(this, com.hnulab.sharebike.em.R.layout.activity_main);
        initId();
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, drawerLayout, CommonUtils.getColor(com.hnulab.sharebike.em.R.color.colorTheme));
        initToolbar();
        initDrawerLayout();
        //获取地图控件引用
        mMapView = (MapView) findViewById(com.hnulab.sharebike.em.R.id.map);
        iv_refresh = (ImageView) findViewById(com.hnulab.sharebike.em.R.id.iv_refresh);
        iv_refresh.setOnClickListener(this);
        iv_scan_code = (ImageView) findViewById(com.hnulab.sharebike.em.R.id.iv_scan_code);
        iv_scan_code.setOnClickListener(this);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initBitmap();
        initAMap();
        initLocation();
        RouteTask.getInstance(getApplicationContext())
                .addRouteCalculateListener(this);
        Log.e(TAG, "sha1" + Sha1.sHA1(this));
        //Android 6.0 蓝牙权限问题
        if (Build.VERSION.SDK_INT >= 6.0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CONSTANT);
        }
        //蓝牙连接功能
        initbroadcast();
        if (_bluetooth == null) {
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // 设置设备可以被搜索
        new Thread() {
            public void run() {
                if (_bluetooth.isEnabled() == false) {
                    _bluetooth.enable();
                }
            }
        }.start();

    }

    private void initBitmap() {
        initBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.location_marker);
        moveBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.icon_loaction_start);
        smallIdentificationBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.stable_cluster_marker_one_normal);
        bigIdentificationBitmap = BitmapDescriptorFactory
                .fromResource(com.hnulab.sharebike.em.R.drawable.stable_cluster_marker_one_select);
        bigredpacageBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_red_package_big);
        smallredpacageBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.marker_red_package2);
    }

    private void initId() {
        drawerLayout = mBinding.drawerLayout;
        navView = mBinding.navView;
        toolbar = mBinding.include.toolbar;
        llTitleMenu = mBinding.include.llTitleMenu;
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initDrawerLayout() {
        navView.inflateHeaderView(com.hnulab.sharebike.em.R.layout.nav_header_main);
        View headerView = navView.getHeaderView(0);
        RelativeLayout rl_header_bg = (RelativeLayout) headerView.findViewById(R.id.rl_header_bg);
        LinearLayout ll_nav_trip = (LinearLayout) headerView.findViewById(R.id.ll_nav_trip);
        LinearLayout ll_nav_money = (LinearLayout) headerView.findViewById(R.id.ll_nav_money);
        LinearLayout ll_nav_message = (LinearLayout) headerView.findViewById(R.id.ll_nav_message);
        LinearLayout ll_nav_guide = (LinearLayout) headerView.findViewById(R.id.ll_nav_guide);
        LinearLayout ll_nav_setting = (LinearLayout) headerView.findViewById(R.id.ll_nav_setting);
        rl_header_bg.setOnClickListener(this);
        ll_nav_trip.setOnClickListener(this);
        ll_nav_money.setOnClickListener(this);
        ll_nav_message.setOnClickListener(this);
        ll_nav_guide.setOnClickListener(this);
        ll_nav_setting.setOnClickListener(this);
        llTitleMenu.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, DestinationActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 初始化地图控制器对象
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mRouteSearch = new RouteSearch(this);
            mRouteSearch.setRouteSearchListener(this);
            aMap.getUiSettings().setZoomControlsEnabled(false);
//            aMap.getUiSettings().setGestureScaleByMapCenter(true);
//            aMap.getUiSettings().setScrollGesturesEnabled(false);
            aMap.setOnMapTouchListener(this);
            aMap.setOnMapLoadedListener(this);
            aMap.setOnCameraChangeListener(this);
            aMap.setOnMapClickListener(this);
            // 绑定 Marker 被点击事件
            aMap.setOnMarkerClickListener(markerClickListener);
            aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
            //设置为英语
            aMap.setMapLanguage(AMap.ENGLISH);

        }
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
        mLocationTask = LocationTask.getInstance(getApplicationContext());
        mLocationTask.setOnLocationGetListener(this);
        mRegeocodeTask = new RegeocodeTask(getApplicationContext());

    }

    // 定义 Marker 点击事件监听
    AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {

        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        @Override
        public boolean onMarkerClick(final Marker marker) {
            Log.e(TAG, "点击的Marker");
            Log.e(TAG, marker.getPosition() + "");
            isClickIdentification = true;
            // TODO: 2017/9/14 点击Markerbug
            if (tempMark != null) {

//                tempMark.remove();
                //遍历点，恢复点对应图标 // FIXME: 2017/9/28 
                ArrayList<Marker> markers = PutRedpackageUtils.markers;
                for (Marker marker1 : markers) {
                    if (marker1.equals(tempMark)) {
//                        tempMark.remove();
                        if (marker1.getIcons().get(0).equals(bigIdentificationBitmap)) {
                            tempMark.setIcon(smallIdentificationBitmap);
                        } else {
                            tempMark.setIcon(smallredpacageBitmap);
                        }
                    }
                }

//                tempMark.remove();

                walkRouteOverlay.removeFromMap();
//                tempMark = null;
            }

            startAnim(marker);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                        tempMark = marker;
                        Log.e(TAG, mPositionMark.getPosition().latitude + "===" + mPositionMark.getPosition().longitude);
                        mStartPoint = new LatLonPoint(mRecordPositon.latitude, mRecordPositon.longitude);
                        mPositionMark.setPosition(mRecordPositon);
                        mEndPoint = new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude);
                        // TODO: 2017/9 判断图片
                        icons = marker.getIcons();
                        if (icons.get(0).equals(Utils.bitmapBike)) {//如果图片一辆车
                            marker.setIcon(bigIdentificationBitmap);
                        } else {

                            /**
                             * description:判断点击的红包位置和用户当前位置的距离，若用户所在位置处于红包半径的规定范围内，则可以点击。
                             * marker.getPosition().latitude:点击的红包经纬度
                             * mStartPoint.getLatitude():当前经纬度
                             * radius:获取红包半径
                             * distance：用户与红包距离
                             * auther：xuewenliao
                             * time：2017/9/26 9:37
                             */
//                            double radius = Distance.GetRadius(redPackageLocations);
                            double radius = 0.00002;
//                            double radius = 19.6;

                            Log.i("radius", "radius:" + String.valueOf(radius));

//                            double distance = Math.abs(Distance.GetDistance(mInitialMark.getPosition().latitude, mInitialMark.getPosition().longitude, marker.getPosition().latitude, marker.getPosition().longitude));
//                            double distance = Math.abs(Distance.GetDistance(mStartPoint.getLatitude(), mStartPoint.getLongitude(), marker.getPosition().latitude, marker.getPosition().longitude));
//                            double Ladistance = Math.abs(mStartPoint.getLatitude() - marker.getPosition().latitude);
//                            double Lodistance = Math.abs(mStartPoint.getLongitude() - marker.getPosition().longitude);
                            //移动点坐标--测试
//                            double distance = Distance.getDistance(mStartPoint.getLatitude(),mStartPoint.getLongitude(), marker.getPosition().latitude, marker.getPosition().longitude);
                            //真实原点坐标--真实
                            double distance = Distance.getDistance(mInitialMark.getPosition().latitude,mInitialMark.getPosition().longitude, marker.getPosition().latitude, marker.getPosition().longitude);
                            Log.i("radius", "distance:" + String.valueOf(distance));
//                          Ladistance < radius && Lodistance < radius

                            if (distance<radius) {


                                //开启红包主动传数据线程
                                redSendThread = new Thread(new RedSendThread());
                                redSendThread.start();

                                if (isSuccessSend = true) {

                                //弹出一个Dialog
                                RedpackageDialog RedpackageDialog = com.hnulab.sharebike.em.dialog.RedpackageDialog.getInstance();
                                RedpackageDialog.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.load_dialog);
                                RedpackageDialog.getInstance().show(getSupportFragmentManager(), "");
                                } else {
                                    Message msg = new Message();
                                    msg.what = handler_key.REDUPLOADFAIL.ordinal();
                                    handler.sendMessage(msg);
                                }


                            } else {
                                Message msg = new Message();
                                msg.what = handler_key.OUTOFREDRANGE.ordinal();
                                handler.sendMessage(msg);
                            }

                        }

                        marker.setPosition(marker.getPosition());
                        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);//出行路线规划
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        }
    };

    //红包地区主动循环发送线程
    class RedSendThread implements Runnable {

        @Override
        public void run() {
            try {

                while (isStartPick) {
                    //克隆数据
                    EnvData clone = null;

                    try {
                        clone = envData.clone();
//                        clone.setE_latitfude(mStartPoint.getLatitude());
//                        clone.setE_longitude(mStartPoint.getLongitude());
                        //移动点坐标
//                        clone.setE_latitfude(mStartPoint.getLatitude());
//                        clone.setE_longitude(mStartPoint.getLongitude());
                        //原点坐标
                        clone.setE_latitfude(mInitialMark.getPosition().latitude);
                        clone.setE_longitude(mInitialMark.getPosition().longitude);
                        Log.i("clone2",mInitialMark.getPosition().latitude+"..."+mInitialMark.getPosition().longitude);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                    redDatas.add(clone);
                    Thread.sleep(2000);
                    if (redDatas.size() == 2) {

//                        new Thread(new SendRedCollectinThread()).start();
                        Gson gson = new Gson();
                        String sendData = gson.toJson(redDatas);
                        RequestParams params = new RequestParams("http://39.108.151.208:9030/sharebike/evn_data/open_redpackage_data/");
                        params.addHeader("Content-type", "application/json");
                        params.setCharset("UTF-8");
                        params.setAsJsonContent(true);
                        params.setBodyContent(sendData);

                        Log.i("server", "run_SUCCESS");

                        x.http().post(params, redcallback);

                    }
                    Log.i("redDatas", redDatas.toString());
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //主动发数据
    private Callback.CommonCallback<String> redcallback = new Callback.CommonCallback<String>() {
        @Override
        public void onSuccess(String result) {
//            redDatas.clear();

//            tempMark.remove();

            isSuccessSend = true;

            Message msg = new Message();
            msg.what = handler_key.REDUPLOADSUCCESS.ordinal();
            handler.sendMessage(msg);

            //移除所有红包
//            tempMark.remove();
//            PutRedpackageUtils.markers.clear();
//            updataMarkers.clear();

            //隐藏红包
//            PutRedpackageUtils.hideMarkers();

//            if (isIinitRed == true) {
//                PutRedpackageUtils.removeMarkers();
//            } else {
//                for (Marker marker : updataMarkers) {
//                    marker.remove();
//                    marker.destroy();
//                }
//                updataMarkers.clear();
//            }


            //重新开启加载红包线程
            redLocation = new Thread(new RedLocation());
            redLocation.start();

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            String errorMessege = ex.getMessage();
            Log.i("errorMesseges",errorMessege);

            isSuccessSend = false;

            Message msg = new Message();
            msg.what = handler_key.REDUPLOADFAIL.ordinal();
            msg.obj = errorMessege;
            handler.sendMessage(msg);
            redDatas.clear();
        }

        @Override
        public void onCancelled(CancelledException cex) {
            redDatas.clear();
        }

        @Override
        public void onFinished() {
            redSendThread.interrupt();
//            redSendThread.stop();
            redDatas.clear();
        }
    };


    private void startAnim(Marker marker) {
        ScaleAnimation anim = new ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f);
        anim.setDuration(300);
        // TODO: 2017/10/8 3D--2D
//        marker.setAnimation(anim);
//        marker.startAnimation();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO 解除蓝牙绑定
        try {
            BluetoothAutoConnectUtils.removeBond(_device);
            Log.e("removeBond", "onDestroy-->removeBond");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        PutRedpackageUtils.removeMarkers();
        mMapView.onDestroy();
        mLocationTask.onDestroy();
        RouteTask.getInstance(getApplicationContext()).removeRouteCalculateListener(this);
        //解除蓝牙绑定

        //关闭蓝牙连接
        if (_socket != null)  //关闭连接socket
            try {
                _socket.close();
            } catch (IOException e) {
            }
//                    	_bluetooth.disable();  //关闭蓝牙服务

        // 注销广播
        unregisterReceiver(broadcastReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        // 保证定位图标在地图上方，点击后不会卡死
        if (mInitialMark != null) {
//            mInitialMark.setToTop();
            mInitialMark.setVisible(true);
//            mInitialMark.setDraggable(true);

        }
        if (mPositionMark != null) {
//            mPositionMark.setToTop();
            mInitialMark.setVisible(true);
//            mInitialMark.setDraggable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_title_menu:// 开启菜单
                drawerLayout.openDrawer(GravityCompat.START);
                // 关闭
//                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.rl_header_bg:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PersonalInformationActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_trip:
                // TODO: 2017/10/8 3D-->2D
//                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
//                mBinding.drawerLayout.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        MyTripActivity.start(MainActivity.this);
//                    }
//                }, 360);
                break;
            case R.id.ll_nav_money:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyWalletActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_message:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyMessageActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_guide:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UserKnowActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.ll_nav_setting:
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                mBinding.drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoginActivity.start(MainActivity.this);
                    }
                }, 360);
                break;
            case R.id.iv_refresh:
                reFreshTimer();
//                clickRefresh();
                break;
            case R.id.iv_scan_code:
                //TODO 点击扫码

//                BluetoothReceiver.BLUETOOTH_ADDRESS = "98:D3:32:11:21:AE";
//                BluetoothReceiver.BLUETOOTH_PIN = "1234";
//                BluetoothConnect();

                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);

//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.icon(BitmapDescriptorFactory
//                        .fromResource(R.drawable.stable_cluster_marker_one_normal));

//                markerOptions.position( new LatLng(28.1876000000,112.9460000000));
//                Marker marker = aMap.addMarker(markerOptions);
//                markers.add(marker);
                break;
        }
    }

    private void reFreshTimer() {
//        iv_refresh.setClickable(false);
        iv_refresh.setEnabled(false);
        timeCount = 5;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        },1000,1000);

        clickRefresh();//刷新界面
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * description:二维码扫描回调
         * auther：luojie
         * time：2017/9/12 13:40
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);//取到扫描结果
                    String[] datas = result.split("&");
                    if (datas.length < 1) {
                        Toast.makeText(this, "解析失败", Toast.LENGTH_LONG).show();
                    } else {
                        // TODO: 2017/9/12 蓝牙解析回调
                        String pin = datas[0].split("=")[1];
                        String address = datas[1].split("=")[1];
                        BluetoothReceiver.BLUETOOTH_ADDRESS = address;
                        BluetoothReceiver.BLUETOOTH_PIN = pin;
                        Toast.makeText(this, "ping：" + pin + "\naddress：" + address, Toast.LENGTH_LONG).show();
                        //蓝牙连接
                        BluetoothConnect();
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * description:通知设备进行蓝牙连接
     * auther：luojie
     * time：2017/9/12 16:35
     */
    private void BluetoothConnect() {
        if (_bluetooth.isEnabled() == false) {  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            _bluetooth.enable();
            return;
        }
        if (_socket == null) {
            //触发系统广播ACTION_FOUND
            _bluetooth.startDiscovery();
        } else {
            //关闭连接socket
            try {
                is.close();
                _socket.close();
                _socket = null;
                bRun = false;
//                                        mButton.setText("连接");
                BluetoothAutoConnectUtils.removeBond(_device);
                Log.e("removeBond", "removeBond");
            } catch (IOException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    //注册蓝牙连接反馈的广播
    private void initbroadcast() {
        // 动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATEUI);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
    }

    /**
     * description:蓝牙连接成功，进行数据更新
     * auther：luojie
     * time：2017/9/13 10:46
     */
    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 响应返回结果
            // MAC地址，由DeviceListActivity设置返回
            String address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
            // 得到蓝牙设备句柄
            _device = _bluetooth.getRemoteDevice(address);

            // 用服务号得到socket
            try {
                _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
            }
            try {
                _socket.connect();
                Toast.makeText(context, "连接" + _device.getName() + "成功！", Toast.LENGTH_SHORT).show();
//                                        mButton.setText("断开");
            } catch (IOException e) {
                try {
                    Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
                    _socket.close();
                    _socket = null;
                } catch (IOException ee) {
                    Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            //打开接收线程
            try {
                is = _socket.getInputStream();   //得到蓝牙数据输入流
            } catch (IOException e) {
                Toast.makeText(context, "接收数据失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bThread == false) {
                ReadThread.start();
                bThread = true;
            } else {
                bRun = true;
            }
        }

    }


    //蓝牙数据接收线程
    Thread ReadThread = new Thread() {

        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            String message1 = "";
            //接收线程
            while (true) {
                try {
                    num = is.read(buffer);//读入数据
                    n = 0;
                    String s0 = new String(buffer, 0, num);
                    fmsg += s0;    //保存收到数据
                    for (i = 0; i < num; i++) {
                        //(buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)
                        if ((buffer[i] == 0x0a) && (buffer[i + 1] == 0x0d)&&(buffer[i+2] == 0x0a)) {
                            buffer_new[n] = 0x0a;
                            i++;
                        } else {
                            buffer_new[n] = buffer[i];
                        }
                        n++;
                    }

                    //获取数据
//                    String result = new String(buffer_new, 0, buffer_new.length - 1).split("\n")[0];
                    String result = new String(buffer_new, 0, buffer_new.length).split("\n")[0];
                    //数据按空格划分 ，PM按加号划分
                    String[] mp_data = result.split(" ");
                    if (mp_data.length == 3) {//25+33+39 1096.88ppm 23.0C 60.0% 2437214msX

//                        String[] mp_data = split[0].split("\\+");
                        envData.setE_pm1(Double.parseDouble(mp_data[1].split(":")[1]));
                        envData.setE_pm2_5(Double.parseDouble(mp_data[0].split(":")[1]));
                        envData.setE_pm10(Double.parseDouble(mp_data[2].split(":")[1]));
                        //split[1]-->1602.29ppm 二氧化碳
//                        envData.setE_co2(Double.parseDouble(split[1].substring(0, split[1].length() - 3)));
                        //split[2]-->27.20C 温度
//                        envData.setE_temperature(Double.parseDouble(split[2].substring(0, split[2].length() - 1)));
                        //split[3]-->67.3%  湿度
//                        envData.setE_humidity(Double.parseDouble(split[3].substring(0, split[3].length() - 1)));

                        Log.i("环境数据", "原始数据：-->" + result);
                        Log.i("环境数据", "浓度：-->" + envData.toString());
                        //开始采集数据
                        isStartPick = true;
                    } else {

                        String key = mp_data[0].split(":")[0];
                        switch (key) {
                            case "PM1":
                                envData.setE_pm1(Double.parseDouble(mp_data[1].split(":")[1]));
                            case "PM2":
                                envData.setE_pm2_5(Double.parseDouble(mp_data[0].split(":")[1]));
                            case "PM10":
                                envData.setE_pm10(Double.parseDouble(mp_data[2].split(":")[1]));

                        }
                        Message msg = new Message();
                        msg.what = handler_key.DROP_DATA.ordinal();
                        handler.sendMessage(msg);
                        Log.i("环境数据","蓝牙数据格式不合法");
                    }
                    //延迟1s
                    Thread.sleep(2000);
//                                                  String[] split = s.split("\n");
//                                                  if (split!=null) {
//                                                            System.out.println("Co2浓度：-->"+split[0]);
//                                                  }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.e(TAG, "onCameraChange" + cameraPosition.target);
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.e(TAG, "onCameraChangeFinish" + cameraPosition.target);
        if (!isClickIdentification) {
            mRecordPositon = cameraPosition.target;
        }
//        ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);

        mStartPosition = cameraPosition.target;
        mRegeocodeTask.setOnLocationGetListener(this);
        mRegeocodeTask
                .search(mStartPosition.latitude, mStartPosition.longitude);

//        mRegeocodeTask
//                .search(mInitialMark.getPosition().latitude,mInitialMark.getPosition().longitude);

        Utils.removeMarkers();
        if (mIsFirst) {
            // TODO: 2017/9/14 实现：
            // 1、实际红包和实际车辆；2、改为传三个参数：地图、LatLng集合（经度坐标、纬度坐标、红包是否已抢标志）

            redLocation = new Thread(new RedLocation());
            redLocation.start();
//            new Thread(new RedLocation()).start();
//            Utils.addEmulateData(aMap, mStartPosition);
            iv_refresh.setVisibility(View.VISIBLE);
            iv_scan_code.setVisibility(View.VISIBLE);
            createInitialPosition(cameraPosition.target.latitude, cameraPosition.target.longitude);//当前经纬度
            createMovingPosition();
            //逆地址转换定位点坐标
//            mRegeocodeTask
//                .search(mInitialMark.getPosition().latitude,mInitialMark.getPosition().longitude);

            mIsFirst = false;
        }

        // 保证定位图标在地图上方，点击后不会卡死
        if (mInitialMark != null) {
//            mInitialMark.setToTop();
            mInitialMark.setVisible(true);
        }
        if (mPositionMark != null) {
//            mPositionMark.setToTop();
            mInitialMark.setVisible(true);
            if (!isClickIdentification) {
                animMarker();
            }
        }
    }

    //获取红包经纬度子线程
    class RedLocation implements Runnable {

        @Override
        public void run() {
            RequestParams params = new RequestParams("http://39.108.151.208:9030/sharebike/evn_data/get_redpackage_data/");
            params.addHeader("Content-type", "application/json");
            params.setCharset("UTF-8");


            x.http().get(params, redLocationCallback);


        }
    }

    private Callback.CommonCallback<String> redLocationCallback = new Callback.CommonCallback<String>() {

        @Override
        public void onSuccess(String result) {
            String jsonBack = result;
            Type type = new TypeToken<List<RedPackageLocation>>() {
            }.getType();
            redPackageLocations = new Gson().fromJson(jsonBack, type);
            if (isIinitRed) {
                isIinitRed = false;
                PutRedpackageUtils.addEmulateData(aMap, mStartPosition, redPackageLocations);
            } else {
                PutRedpackageUtils.upMarkers(aMap, mStartPosition, redPackageLocations);
                System.out.println(1);
//                aMap.notifyAll();
//                for (int i = 0; i < redPackageLocations.size(); i++) {
//                    redLongitude = redPackageLocations.get(i).getE_longitude();
//                    redLatitude = redPackageLocations.get(i).getE_latitfude();
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.icon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.marker_red_package));
//
//                    markerOptions.position(new LatLng(redLatitude, redLongitude));
//                    Marker marker = aMap.addMarker(markerOptions);
//                    updataMarkers = new ArrayList<Marker>();
//                    updataMarkers.add(marker);
//
//                }

//                PutRedpackageUtils.markers=updataMarkers;
            }
            Log.i("red", redPackageLocations.toString());
            Log.i("red", "success");
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            Log.i("red", "onError");
        }

        @Override
        public void onCancelled(CancelledException cex) {
            Log.i("red", "onCancelled");
        }

        @Override
        public void onFinished() {
            Log.i("red", "onFinished");
        }
    };


    /**
     * 地图加载完成
     */
    @Override
    public void onMapLoaded() {
        mLocationTask.startLocate();
    }

    /**
     * 创建初始位置图标
     */
    private void createInitialPosition(double lat, double lng) {
        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.setFlat(true);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(new LatLng(lat, lng));
        markerOptions.icon(initBitmap);
        mInitialMark = aMap.addMarker(markerOptions);
        // TODO: 2017/10/8 3D-->2D
//        mInitialMark.setClickable(false);
    }

    /**
     * 创建移动位置图标
     */
    private void createMovingPosition() {
        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.setFlat(true);
//        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(new LatLng(0, 0));
        markerOptions.icon(moveBitmap);
        mPositionMark = aMap.addMarker(markerOptions);
        mPositionMark.setPositionByPixels(mMapView.getWidth() / 2,
                mMapView.getHeight() / 2);
        // TODO: 2017/10/8 3D-->2D
//        mPositionMark.setClickable(false);
    }

    //一秒定位一次，获取到所有位置信息
    @Override
    public void onLocationGet(PositionEntity entity) {
        // todo 这里在网络定位时可以减少一个逆地理编码
        ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);

        Log.e("onLocationGet", "onLocationGet" + entity.address);

//        Message msg = new Message();
//        msg.what = handler_key.LOCATION.ordinal();
//        msg.obj = entity.address;
//        handler.sendMessage(msg);


        RouteTask.getInstance(getApplicationContext()).setStartPoint(entity);
        mStartPosition = new LatLng(entity.latitue, entity.longitude);
        if (mIsFirstShow) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    mStartPosition, 17);
            aMap.animateCamera(cameraUpate);
            mIsFirstShow = false;
        }
        mInitialMark.setPosition(mStartPosition);
        initLocation = mStartPosition;
        Log.e("onLocationGet", "onLocationGet" + mStartPosition);
        //如果环境监测数据不为空，则开始填充数据
        if (isStartPick) {
            EnvData clone = null;
            //封装环境数据信息
            //获取当前时间
            Date curDate = new Date(System.currentTimeMillis());
            envData.setE_latitfude(entity.latitue);
            envData.setE_longitude(entity.longitude);
            envData.setE_city(entity.city);
            envData.setE_address(entity.address);
            envData.setE_time(formatter.format(curDate));
            try {
                clone = envData.clone();


            } catch (Exception e) {
            }
            System.out.println(envData.getE_latitfude());
            System.out.println(envData.getE_longitude());
            System.out.println(envData.getE_time());
//            System.out.println(envData.getE_co2());
            System.out.println(envData.getE_city());
            System.out.println(envData.getE_address());

            Log.i("clone1",entity.latitue+"..."+entity.longitude);

            //填充数据到集合
            envDatas.add(clone);
            Log.e("envData", "数据：" + clone.toString());
            //如果缓存数据已经有2条
            if (envDatas.size() == 2 && isUpload == false) {
                // TODO: 2017/9/14 线程锁处理
                isUpload = true;
                /**
                 * description:传数据到服务器
                 * auther：xuewenliao
                 * time：2017/9/13 21:07
                 */
                Log.i("server", "come");
                Thread loginThread = new Thread(new SendDataThread());
                loginThread.start();

//                Message msg = new Message();
//                msg.what = handler_key.UPLOADSUCCESS.ordinal();
//                handler.sendMessage(msg);
                Log.i("server", "start");

            }
            //每两条数据进行一次Toast显示
            if (envDatas.size() % 2 == 0) {
                Message msg = new Message();
                msg.what = handler_key.SHOWDATA.ordinal();
                handler.sendMessage(msg);
            }
        }


    }

    class SendDataThread implements Runnable {

        @Override
        public void run() {
            Log.i("server", "run");
            Gson gson = new Gson();
            String sendData = gson.toJson(envDatas);
            RequestParams params = new RequestParams("http://39.108.151.208:9030/sharebike/evn_data/single/");
            params.addHeader("Content-type", "application/json");
            params.setCharset("UTF-8");
            params.setAsJsonContent(true);
            params.setBodyContent(sendData);

            Log.i("server", "run_SUCCESS");
            x.http().post(params, callback);
        }
    }

    private Callback.CommonCallback<String> callback = new Callback.CommonCallback<String>() {
        @Override
        public void onSuccess(String result) {
            Log.i("server", "REGISTER_SUCCESS");
            envDatas.clear();
            Log.i("server", "clear");
            isUpload = false;
            System.out.print(1);

            Message msg = new Message();
            msg.what = handler_key.UPLOADSUCCESS.ordinal();
            handler.sendMessage(msg);
            Log.i("server", "start");
            //接收数据
//            String jsonBack = result;
//            EnvData data = new Gson().fromJson(jsonBack,EnvData.class);
//            Log.i("data",data.toString());

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {

            String error = ex.getMessage();
            Log.i("error",error);
            //坐标没在湖师大在此处接收数据
            Message msg = new Message();
            msg.what = handler_key.OUYOFPALACE.ordinal();
            msg.obj = error;
            handler.sendMessage(msg);

            envDatas.clear();
            isUpload = false;
            Log.i("server", "CONNECT_FAIL");
        }

        @Override
        public void onCancelled(CancelledException cex) {
            envDatas.clear();
            isUpload = false;
            Log.i("server", "onCancelled");
        }

        @Override
        public void onFinished() {
            envDatas.clear();
            isUpload = false;
            Log.i("server", "onFinished");
        }
    };


    @Override
    public void onRegecodeGet(PositionEntity entity) {
        Log.e(TAG, "onRegecodeGet" + entity.address);
        entity.latitue = mStartPosition.latitude;
        entity.longitude = mStartPosition.longitude;
        RouteTask.getInstance(getApplicationContext()).setStartPoint(entity);
        RouteTask.getInstance(getApplicationContext()).search();
        Log.e(TAG, "onRegecodeGet" + mStartPosition);
    }

    @Override
    public void onRouteCalculate(float cost, float distance, int duration) {
        Log.e(TAG, "cost" + cost + "---" + "distance" + distance + "---" + "duration" + duration);
        PositionEntity endPoint = RouteTask.getInstance(getApplicationContext()).getEndPoint();
        mRecordPositon = new LatLng(endPoint.latitue, endPoint.longitude);
        clickMap();
        RouteTask.getInstance(getApplicationContext()).setEndPoint(null);
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() >= 2) {
            aMap.getUiSettings().setScrollGesturesEnabled(false);
        } else {
            aMap.getUiSettings().setScrollGesturesEnabled(true);
        }
    }

    private void animMarker() {
        if (animator != null) {
            animator.start();
            return;
        }
        animator = ValueAnimator.ofFloat(mMapView.getHeight() / 2, mMapView.getHeight() / 2 - 30);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(150);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mPositionMark.setPositionByPixels(mMapView.getWidth() / 2, Math.round(value));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPositionMark.setIcon(moveBitmap);
            }
        });
        animator.start();
    }

    private void endAnim() {
        if (animator != null && animator.isRunning())
            animator.end();
    }


    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        LoadDialog.getInstance().dismiss();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    time = AMapUtil.getFriendlyTimeArray(dur);
                    distance = AMapUtil.getFriendlyLength(dis);
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    tempMark.setTitle(des);
                    tempMark.showInfoWindow();
                    Log.e(TAG, des);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(this, R.string.no_result);
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(this, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(this, "终点未设置");
        }
        showDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }

    private void showDialog() {
        LoadDialog loadDialog = LoadDialog.getInstance();
        loadDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.load_dialog);
        LoadDialog.getInstance().show(getSupportFragmentManager(), "");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        clickMap();
    }

    private void clickRefresh() {


//        //重新开启加载红包线程
//        Thread refreshRed = new Thread(new RedLocation());
//        refreshRed.start();

        clickInitInfo();
        //移动坐标回圆点
//        if (initLocation != null) {
//            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
//                    initLocation, 17f);
//            aMap.animateCamera(cameraUpate);
//        }
    }

    private void clickMap() {
        clickInitInfo();
        if (mRecordPositon != null) {
            CameraUpdate cameraUpate = CameraUpdateFactory.newLatLngZoom(
                    mRecordPositon, 17f);
            aMap.animateCamera(cameraUpate);
        }
    }

    /**
     * 点击map后事件处理
     */
    private void clickInitInfo() {
        isClickIdentification = false;
        //刷新红包线程
//        updataMarkers.clear();
        Thread refreshRed = new Thread(new RedLocation());
        refreshRed.start();

//        tempMark.remove();
        //遍历点，恢复点对应图标
        ArrayList<Marker> markers = PutRedpackageUtils.markers;
        if (null != tempMark) {
//            for (Marker marker : markers) {
//                if (marker.equals(tempMark)) {
//                    if (marker.getIcons().get(0).equals(bigIdentificationBitmap)) {
//                        tempMark.setIcon(smallIdentificationBitmap);
//                    } else {
////                        tempMark.setIcon(smallredpacageBitmap);
//                    }
//                }
//            }
            tempMark.hideInfoWindow();
            walkRouteOverlay.removeFromMap();
            tempMark.remove();
            tempMark = null;
        }
        if (null != walkRouteOverlay) {
            walkRouteOverlay.removeFromMap();
        }
    }

    // TODO: 2017/9/20 点击图标后信息的显示
    @Override
    public View getInfoWindow(Marker marker) {
        Log.e(TAG, "getInfoWindow");
        View infoWindow = getLayoutInflater().inflate(
                R.layout.info_window, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        TextView tv_distance = (TextView) view.findViewById(R.id.tv_distance);
        tv_distance.setText(distance);

//        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
//        TextView tv_time_info = (TextView) view.findViewById(R.id.tv_time_info);
//        tv_time.setText(time[0]);
//        tv_time_info.setText(time[1]);
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.e(TAG, "getInfoContents");
        return null;
    }


}