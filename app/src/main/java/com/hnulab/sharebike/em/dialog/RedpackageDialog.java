package com.hnulab.sharebike.em.dialog;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hnulab.sharebike.em.R;

import animation.Rotate3dAnimation;

import static razerdp.util.SimpleAnimUtil.getDefaultAlphaAnimation;

/**
 * description:红包
 * auther：xuewenliao
 * time：2017/9/22 10:34
 */

public class RedpackageDialog extends DialogFragment {

    private static Activity activity;
    private View view;
    private ImageView iv_open;
    private ImageView iv_cancel;
    private TextView tv_collectionTime;
    private TextView tv_tip;
    public static int secondleft = 6000;//服务器传来的倒计时时间
    private String money = "0.5 $";//金额


    public static RedpackageDialog getInstance() {
        return FirstQuote.instance;
    }

    //在第一次被引用时被加载
    static class FirstQuote {
        private static RedpackageDialog instance = new RedpackageDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);//点击背景Dialog不消失
        view = inflater.inflate(R.layout.popup_redpackage, container);
        initView();
        setEvent();


        //设置红包抖动动画
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.popup_anima);
        relativeLayout.setAnimation(setShowAnimation());

        //倒计时
        new DoneTimer().start();
        return view;
    }


    class DoneTimer extends CountDownTimer {


        public DoneTimer() {
            super(secondleft, 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv_collectionTime.setText("Countdown\n" + millisUntilFinished / 1000 + "s");
            tv_collectionTime.setTextSize(20);
            tv_collectionTime.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        @Override
        public void onFinish() {
            startRotate();
            tv_tip.setText("gift money");
            tv_tip.setTextSize(25);
            // TODO: 2017/9/22 将来要改成传过来的金额
            tv_collectionTime.setText(money);
            tv_collectionTime.setTextSize(30);
            tv_collectionTime.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
    }

    private void initView() {
        iv_open = (ImageView) view.findViewById(R.id.iv_open);
        iv_cancel = (ImageView) view.findViewById(R.id.iv_cancel);
        tv_collectionTime = (TextView) view.findViewById(R.id.tv_collectionTime);
        tv_tip = (TextView) view.findViewById(R.id.tv_tip);

    }

    private void setEvent() {
//        iv_open.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startRotate();
//            }
//        });

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RedpackageDialog.this.dismiss();
            }
        });
    }

    //红包抖动动画
    private Animation setShowAnimation() {
        AnimationSet set = new AnimationSet(false);
        Animation shakeAnima = new RotateAnimation(0, 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shakeAnima.setInterpolator(new CycleInterpolator(5));
        shakeAnima.setDuration(600);
        set.addAnimation(getDefaultAlphaAnimation());
        set.addAnimation(shakeAnima);
        return set;
    }

    //按钮旋转动画
    private void startRotate() {
        float centerX = iv_open.getWidth() / 2.0f;
        float centerY = iv_open.getHeight() / 2.0f;
        float centerZ = 0f;
        Rotate3dAnimation rotate3dAnimationX = new Rotate3dAnimation(360, 0, centerX, centerY, centerZ, Rotate3dAnimation.ROTATE_Y_AXIS, true);
        rotate3dAnimationX.setDuration(1000);
        iv_open.startAnimation(rotate3dAnimationX);
    }

}
