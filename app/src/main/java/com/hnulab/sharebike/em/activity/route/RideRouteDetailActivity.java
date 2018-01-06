package com.hnulab.sharebike.em.activity.route;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.route.RidePath;
import com.hnulab.sharebike.em.R;
import com.hnulab.sharebike.em.base.BaseActivity;
import com.hnulab.sharebike.em.util.AMapUtil;

/**
 * 骑行路线详情
 */
public class RideRouteDetailActivity extends BaseActivity {
	private RidePath mRidePath;
	private TextView mTitleWalkRoute;
	private ListView mRideSegmentList;
	private RideSegmentListAdapter mRideSegmentListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);
		setTitle("骑行路线详情");
		showContentView();
		getIntentData();
		mTitleWalkRoute = (TextView) findViewById(R.id.firstline);
		String dur = AMapUtil.getFriendlyTime((int) mRidePath.getDuration());
		String dis = AMapUtil
				.getFriendlyLength((int) mRidePath.getDistance());
		mTitleWalkRoute.setText(dur + "(" + dis + ")");
		mRideSegmentList = (ListView) findViewById(R.id.bus_segment_list);
		mRideSegmentListAdapter = new RideSegmentListAdapter(
				this.getApplicationContext(), mRidePath.getSteps());
		mRideSegmentList.setAdapter(mRideSegmentListAdapter);

	}

	private void getIntentData() {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		mRidePath = intent.getParcelableExtra("ride_path");
	}


}
