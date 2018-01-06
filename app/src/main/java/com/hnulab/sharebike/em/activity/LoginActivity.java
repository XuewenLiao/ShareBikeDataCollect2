package com.hnulab.sharebike.em.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hnulab.sharebike.em.R;
import com.hnulab.sharebike.em.base.BaseActivity;
import com.hnulab.sharebike.em.databinding.ActivityLoginBinding;
//import com.hnulab.sharebike.em.databinding.ActivityLoginBinding;

/**
 * Created by Administrator on 2017/4/27.
 */

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("手机登陆");
        showContentView();
    }
    public static void start(Context mContext) {
        Intent intent = new Intent(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }
}
