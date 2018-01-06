/**  
 * Project Name:Android_Car_Example  
 * File Name:LocationTask.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月3日上午9:27:45  
 *  
 */

package com.hnulab.sharebike.em.lib;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.ServiceSettings;


/**
 * ClassName:LocationTask <br/>
 * Function: 简单封装了定位请求，可以进行单次定位和多次定位，注意的是在app结束或定位结束时注意销毁定位 <br/>
 * Date: 2015年4月3日 上午9:27:45 <br/>
 * 
 * @author yiyi.qi
 * @version
 * @since JDK 1.6
 * @see
 */
public class LocationTask implements AMapLocationListener,
		OnLocationGetListener {

	private AMapLocationClient mLocationClient;

	private static LocationTask mLocationTask;

	private Context mContext;

	private OnLocationGetListener mOnLocationGetlisGetListener;

	private RegeocodeTask mRegecodeTask;

	//fixme test数据
	private int sum=0;

	private LocationTask(Context context) {
		mLocationClient = new AMapLocationClient(context);
		mLocationClient.setLocationListener(this);
		mRegecodeTask = new RegeocodeTask(context);
		mRegecodeTask.setOnLocationGetListener(this);
		mContext = context;
	}

	public void setOnLocationGetListener(
			OnLocationGetListener onGetLocationListener) {
		mOnLocationGetlisGetListener = onGetLocationListener;
	}

	public static LocationTask getInstance(Context context) {
		if (mLocationTask == null) {
			mLocationTask = new LocationTask(context);
		}
		return mLocationTask;
	}

	/**  
	 * 开启单次定位
	 */
	public void startSingleLocate() {
		AMapLocationClientOption option=new AMapLocationClientOption();
		option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		option.setOnceLocation(true);
		mLocationClient.setLocationOption(option);

		mLocationClient.startLocation();

	}

	/**  
	 * 开启多次定位
	 */
	public void startLocate() {

		AMapLocationClientOption option=new AMapLocationClientOption();
		option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		option.setOnceLocation(false);
		//隔多长时间定位一次
		option.setInterval(2000);
		mLocationClient.setLocationOption(option);
		mLocationClient.startLocation();

	}

	/**  
	 * 结束定位，可以跟多次定位配合使用
	 */
	public void stopLocate() {
		mLocationClient.stopLocation();

	}

	/**  
	 * 销毁定位资源
	 */
	public void onDestroy() {
		mLocationClient.stopLocation();
		mLocationClient.onDestroy();
		mLocationTask = null;
	}


	// TODO: 2017/9/13 获取数据
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
			PositionEntity entity = new PositionEntity();

			mRegecodeTask
					.search(amapLocation.getLatitude(), amapLocation.getLongitude());


			entity.latitue = amapLocation.getLatitude();
			entity.longitude = amapLocation.getLongitude();

//			entity.latitue = amapLocation.getLatitude()+sum;
//			entity.longitude = amapLocation.getLongitude()+sum;
//			sum+=5;

			if (!TextUtils.isEmpty(amapLocation.getAddress())) {
				ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
//				entity.address = amapLocation.getAddress();
				entity.address = mRegecodeTask.getAddress();
				Log.i("CurrentAddress",entity.address);
			}
			if (!TextUtils.isEmpty(amapLocation.getCity())) {
				ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
//				entity.city = amapLocation.getCity();
				entity.city = mRegecodeTask.getCity();
			}
			mOnLocationGetlisGetListener.onLocationGet(entity);

		}
	}

	@Override
	public void onLocationGet(PositionEntity entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegecodeGet(PositionEntity entity) {
		// TODO Auto-generated method stub

	}

}
