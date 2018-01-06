package com.hnulab.sharebike.em.entity;

import java.util.List;

/**
 * Created by lj_xwl on 2017/9/25.
 */

public class RedPackageLocations {
    public List<RedPackageLocation> redPackageLocations;

    public List<RedPackageLocation> getRedPackageLocations() {
        return redPackageLocations;
    }

    public void setRedPackageLocations(List<RedPackageLocation> redPackageLocations) {
        this.redPackageLocations = redPackageLocations;
    }

    @Override
    public String toString() {
        return "RedPackageLocations{" +
                "redPackageLocations=" + redPackageLocations +
                '}';
    }
}
