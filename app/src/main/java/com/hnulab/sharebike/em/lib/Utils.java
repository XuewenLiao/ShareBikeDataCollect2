/**
 * Project Name:Android_Car_Example
 * File Name:Utils.java
 * Package Name:com.amap.api.car.example
 * Date:2015年4月7日下午3:43:05
 */

package com.hnulab.sharebike.em.lib;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.hnulab.sharebike.em.R;

import java.util.ArrayList;


/**
 * ClassName:Utils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:   TODO ADD REASON. <br/>
 * Date:     2015年4月7日 下午3:43:05 <br/>
 *
 * @author yiyi.qi
 * @see
 * @since JDK 1.6
 */
public class Utils {

    public static ArrayList<Marker> markers = new ArrayList<Marker>();
    public static BitmapDescriptor bitmapDescriptor;
    public static boolean isMarkbike;
    //车的对象
    public static BitmapDescriptor bitmapBike = BitmapDescriptorFactory
            .fromResource(R.drawable.stable_cluster_marker_one_normal);
    //红包的对象
    public static BitmapDescriptor bitmapPackage = BitmapDescriptorFactory
            .fromResource(R.drawable.marker_red_package);


    /**
     * 添加模拟测试的车的点
     */
    public static void addEmulateData(AMap amap, LatLng center) {
        if (markers.size() == 0) {

            for (int i = 0; i < 20; i++) {
                if (i % 3 == 0) {
                    isMarkbike = true;
                    bitmapDescriptor = bitmapBike;
                } else {
                    isMarkbike = false;
                    bitmapDescriptor = bitmapPackage;
                }
                double latitudeDelt;
                double longtitudeDelt;
                if (i % 2 == 0) {
                    latitudeDelt = (Math.random() - 0.5) * 0.1;
                    longtitudeDelt = (Math.random() - 0.5) * 0.1;
                } else {
                    latitudeDelt = (Math.random() - 0.5) * 0.01;
                    longtitudeDelt = (Math.random() - 0.5) * 0.01;
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(bitmapDescriptor);

                markerOptions.position(new LatLng(center.latitude + latitudeDelt, center.longitude + longtitudeDelt));
                Marker marker = amap.addMarker(markerOptions);
                markers.add(marker);
            }
        } else {
            for (Marker marker : markers) {
                double latitudeDelt = (Math.random() - 0.5) * 0.1;
                double longtitudeDelt = (Math.random() - 0.5) * 0.1;
                marker.setPosition(new LatLng(center.latitude + latitudeDelt, center.longitude + longtitudeDelt));

            }
        }
    }

    /**
     * 移除marker
     */
    public static void removeMarkers() {
        for (Marker marker : markers) {
            marker.remove();
            marker.destroy();
        }
        markers.clear();
    }

}
  
