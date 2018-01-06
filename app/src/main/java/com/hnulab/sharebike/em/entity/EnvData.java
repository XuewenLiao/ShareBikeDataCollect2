package com.hnulab.sharebike.em.entity;

/**
 * Description：
 * Auther：luojie
 * E-mail：luojie@hnu.edu.cn
 * Time：2017/9/12 19:30
 */
public class EnvData implements Cloneable{
    //经度
    double e_longitude;
    //纬度
    double e_latitfude;
//    //二氧化碳浓度 单位%
//    double e_co2;
    //时间
    String e_time;
    //PM1
    double e_pm1;
//    double e_pm2_5;

    //PM2.5
    double e_pm2_5;
//    double e_pm5;
    //PM10
    double e_pm10;
//    //湿度 单位PPM
//    double e_humidity;
//    //温度
//    double e_temperature;
    //地址
    String e_address;
    //城市
    String e_city;

    public double getE_longitude() {
        return e_longitude;
    }

    public void setE_longitude(double e_longitude) {
        this.e_longitude = e_longitude;
    }

    public double getE_latitfude() {
        return e_latitfude;
    }

    public void setE_latitfude(double e_latitfude) {
        this.e_latitfude = e_latitfude;
    }

    public double getE_pm2_5() {
        return e_pm2_5;
    }

    public void setE_pm2_5(double e_pm2_5) {
        this.e_pm2_5 = e_pm2_5;
    }

    public double getE_pm1() {
        return e_pm1;
    }

    public void setE_pm1(double e_pm1) {
        this.e_pm1 = e_pm1;
    }

    public double getE_pm10() {
        return e_pm10;
    }

    public void setE_pm10(double e_pm10) {
        this.e_pm10 = e_pm10;
    }


    public String getE_address() {
        return e_address;
    }

    public void setE_address(String e_address) {
        this.e_address = e_address;
    }

    public String getE_city() {
        return e_city;
    }

    public void setE_city(String e_city) {
        this.e_city = e_city;
    }

    public String getE_time() {
        return e_time;
    }

    public void setE_time(String e_time) {
        this.e_time = e_time;
    }

    @Override
    public String toString() {
        return "EnvData{" +
                "e_longitude=" + e_longitude +
                ", e_latitfude=" + e_latitfude +
                ", e_time='" + e_time + '\'' +
                ", e_pm1=" + e_pm1 +
                ", e_pm2_5=" + e_pm2_5 +
                ", e_pm10=" + e_pm10 +
                ", e_address='" + e_address + '\'' +
                ", e_city='" + e_city + '\'' +
                '}';
    }

    @Override
    public EnvData clone() throws CloneNotSupportedException {
        EnvData cloned = (EnvData) super.clone();
        return cloned;
    }
}
