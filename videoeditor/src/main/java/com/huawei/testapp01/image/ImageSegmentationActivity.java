/**
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.testapp01.image;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.huawei.testapp01.R;
import com.huawei.testapp01.adpter.TabFragmentAdapter;
import com.huawei.testapp01.BaseActivity;
import com.huawei.testapp01.fragment.BackgroundChangeFragment;
import com.huawei.testapp01.fragment.CaptureImageFragment;

import java.util.ArrayList;
import java.util.List;

public class ImageSegmentationActivity extends BaseActivity implements View.OnClickListener {
    private List<Fragment> mFragmentList;
    private TextView mBgChangeTv;
    private TextView mCaptureImgTv;
    private ViewPager mViewPager;
    private View mBgChangeLine;
    private View mCaptureImgLine;
    private TabFragmentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image_segmentation);
        this.initView();
        this.setStatusBar();
        this.setStatusBarFontColor();
    }

    private void initView() {
        this.mBgChangeTv = this.findViewById(R.id.fragment_one);
        this.mCaptureImgTv = this.findViewById(R.id.fragment_two);
        this.mBgChangeLine = this.findViewById(R.id.line_one);
        this.mCaptureImgLine = this.findViewById(R.id.line_two);
        this.mViewPager = this.findViewById(R.id.view_pager);

        this.findViewById(R.id.back).setOnClickListener(this);
        this.mBgChangeTv.setOnClickListener(this);
        this.mCaptureImgTv.setOnClickListener(this);
        this.mViewPager.setOnPageChangeListener(new PagerChangeListener());

        this.mFragmentList = new ArrayList<>();
        this.mFragmentList.add(new BackgroundChangeFragment());
        this.mFragmentList.add(new CaptureImageFragment());

        this.mAdapter = new TabFragmentAdapter(this.getSupportFragmentManager(), this.mFragmentList);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setCurrentItem(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_one:
                this.mViewPager.setCurrentItem(0);
                this.setBgChangeView();
                break;
            case R.id.fragment_two:
                this.mViewPager.setCurrentItem(1);
                this.setCaptureImageView();
                break;
            case R.id.back:
                this.finish();
                break;
            default:
                break;
        }
    }

    private void setBgChangeView() {
        this.mBgChangeTv.setTextColor(this.getResources().getColor(R.color.button_background));
        this.mCaptureImgTv.setTextColor(Color.BLACK);
        this.mBgChangeLine.setVisibility(View.VISIBLE);
        this.mCaptureImgLine.setVisibility(View.GONE);
    }

    private void setCaptureImageView() {
        this.mBgChangeTv.setTextColor(Color.BLACK);
        this.mCaptureImgTv.setTextColor(this.getResources().getColor(R.color.button_background));
        this.mBgChangeLine.setVisibility(View.GONE);
        this.mCaptureImgLine.setVisibility(View.VISIBLE);
    }

    /**
     * Set a ViewPager listening event. When the ViewPager is swiped left or right, the menu bar selected state changes accordingly.
     *
     */
    public class PagerChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    ImageSegmentationActivity.this.setBgChangeView();
                    break;
                case 1:
                    ImageSegmentationActivity.this.setCaptureImageView();
                    break;
                default:
                    break;
            }
        }
    }

    public void onBackPressed(View view) {
        this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
