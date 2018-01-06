package com.hnulab.sharebike.em.entity;

/**
 * Created by lj_xwl on 2017/9/25.
 */

public class RedPackageLocation {
    double e_longitude;
    double e_latitfude;

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

    @Override
    public String toString() {
        return "RedPackageLocation{" +
                "e_longitude=" + e_longitude +
                ", e_latitfude=" + e_latitfude +
                '}';
    }
}
