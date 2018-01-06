package com.hnulab.sharebike.em.util;

import com.hnulab.sharebike.em.entity.RedPackageLocation;

import java.util.List;

/**
     * description:计算网格单元距离和获取红包半径
     * auther：xuewenliao
     * time：2017/9/26 10:33
     */

public class Distance {
    private static final double EARTH_RADIUS = 6378137;//赤道半径
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static double GetDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s;//单位米
    }

    //将网格单元数据算出后分为四份，红包半径为网格单元的1/4
    public static double GetRadius(List<RedPackageLocation> redPackageLocations) {
        double unit = Distance.GetDistance(redPackageLocations.get(0).getE_longitude(), redPackageLocations.get(0).getE_latitfude(), redPackageLocations.get(1).getE_longitude(), redPackageLocations.get(1).getE_latitfude());
        double scale = unit / 4;
        return scale;
    }

    public static double getDistance(double lat1, double lon1 , double lat2, double lon2 ){
        double result = Math.sqrt((lat1-lat2)*(lat1-lat2)+(lon1-lon2)*(lon1-lon2));
        return result;
    }
}
