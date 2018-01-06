package com.hnulab.sharebike.em.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hnulab.sharebike.em.R;
import com.hnulab.sharebike.em.base.BaseActivity;

/**
 * Created by Administrator on 2017/4/27.
 */

public class UserKnowActivity  extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_know);
        setTitle("用户指南");
        showContentView();
    }
    public static void start(Context mContext) {
        Intent intent = new Intent(mContext, UserKnowActivity.class);
        mContext.startActivity(intent);
    }
}