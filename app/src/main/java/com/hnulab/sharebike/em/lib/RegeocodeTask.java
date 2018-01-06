/**  
 * Project Name:Android_Car_Example  
 * File Name:RegeocodeTask.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月2日下午6:24:53  
 *  
 */

package com.hnulab.sharebike.em.lib;

import android.content.Context;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * ClassName:RegeocodeTask <br/>
 * Function: 简单的封装的逆地理编码功能 <br/>
 * Date: 2015年4月2日 下午6:24:53 <br/>
 * 
 * @author yiyi.qi
 * @version
 * @since JDK 1.6
 * @see
 */
public class RegeocodeTask implements OnGeocodeSearchListener {
	private static final float SEARCH_RADIUS = 50;
	private OnLocationGetListener mOnLocationGetListener;

	private GeocodeSearch mGeocodeSearch;
	private String address;
	private String city;

	public RegeocodeTask(Context context) {
		ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
		mGeocodeSearch = new GeocodeSearch(context);
		mGeocodeSearch.setOnGeocodeSearchListener(this);
	}

	public void search(double latitude, double longitude) {
		ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
		RegeocodeQuery regecodeQuery = new RegeocodeQuery(new LatLonPoint(
				latitude, longitude), SEARCH_RADIUS, GeocodeSearch.AMAP);//此类定义了地理编码查询的关键字和查询城市。


		mGeocodeSearch.getFromLocationAsyn(regecodeQuery);//根据给定的经纬度和最大结果数返回逆地理编码的结果列表。
	}

	public void setOnLocationGetListener(
			OnLocationGetListener onLocationGetListener) {
		mOnLocationGetListener = onLocationGetListener;
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
//		ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult regeocodeReult,
			int resultCode) {
		if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
//			ServiceSettings.getInstance().setLanguage(ServiceSettings.ENGLISH);
			if (regeocodeReult != null
					&& regeocodeReult.getRegeocodeAddress() != null
					&& mOnLocationGetListener != null) {
				address = regeocodeReult.getRegeocodeAddress()
						.getFormatAddress();
				city = regeocodeReult.getRegeocodeAddress().getCityCode();

				PositionEntity entity = new PositionEntity();
				entity.address = address;
				entity.city = city;
				mOnLocationGetListener.onRegecodeGet(entity);
				Log.i("ReverseAddress", address);

			}
		}
		//TODO 可以根据app自身需求对查询错误情况进行相应的提示或者逻辑处理
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}
}
