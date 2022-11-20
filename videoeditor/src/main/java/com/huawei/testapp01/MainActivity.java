/*
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.testapp01;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.videoeditor.ai.HVEAIApplication;
import com.huawei.hms.videoeditor.ai.HVEAIFaceReenact;
import com.huawei.hms.videoeditor.ai.HVEAIProcessCallback;
import com.huawei.hms.videoeditor.common.network.http.ability.util.array.ArrayUtils;
import com.huawei.secure.android.common.intent.SafeIntent;
import com.huawei.testapp01.AI.AIPhotoActivity;
import com.huawei.testapp01.AI.ViewFileActivity;
import com.huawei.testapp01.AI.bean.AIInfoData;
import com.huawei.testapp01.AI.bean.MediaData;
import com.huawei.testapp01.adpter.AIListAdapter;
import com.huawei.testapp01.image.ImageSegmentationActivity;
import com.huawei.testapp01.image.StillCutPhotoActivity;
import com.huawei.testapp01.ui.decoration.GridItemDividerDecoration;
import com.huawei.testapp01.utils.SizeUtils;
import com.huawei.testapp01.view.CommonProgressDialog;

import com.huawei.hms.videoeditor.ui.mediapick.activity.PicturePickActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MEDIA_TYPE_PHOTO = 1;
    private static final int MEDIA_TYPE_VIDEO_OR_PHOTO = 2;
    private static final int REQUEST_CODE_OF_FACE_REENACT = 0x1001;
    private static final int REQUEST_CODE_OF_FACE_SMILE = 0x1002;
    private static final int REQUEST_CODE_OF_AI_COLOR = 0x1003;
    private static final int REQUEST_CODE_OF_PIC_SUBSTITUTION = 0x1004;
    public static final String SHOW_MEDIA_TYPE = "showMediaType";
    public static final String ACTION_TYPE = "action_type";
    public static final int ACTION_AI_TYPE = 1001;

    private String mFilePath = "";

    private HVEAIFaceReenact mFaceReenact;
    private CommonProgressDialog mFaceReenactDialog;

    private final String[] mPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        verifyStoragePermissions(this);
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initAdapter();
    }

    private void initView() {
        findViewById(R.id.fl_video_editor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.putExtra("fromHome", true);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        // Get the apk_key from agconnect-services.json client/api_key
        HVEAIApplication.getInstance().setApiKey("CgB6e3x9lt+luEGbAnu4YLu7hMd+RZCFo43Mzoi3brOTdv2B35L/TNqJHM4/9zv81s9ET2CDWKFfkbj7sU4Z7GbE");
    }

    private void initAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        List<AIInfoData> infoDataList = new ArrayList<>();
        infoDataList.add(new AIInfoData(getString(R.string.motion_photo), getString(R.string.motion_photo_subtitle),
                R.drawable.ic_motion_photo));
        infoDataList.add(new AIInfoData(getString(R.string.face_smile), getString(R.string.face_smile_subtitle),
                R.drawable.ic_face_smile));
        infoDataList.add(new AIInfoData(getString(R.string.ai_color), getString(R.string.ai_color_subtitle),
                R.drawable.ic_ai_color));
        infoDataList.add(new AIInfoData(getString(R.string.img_segment_sub), getString(R.string.img_segment_sub_subtitle),
                R.drawable.ic_ai_color)); //To do

        AIListAdapter listAdapter = new AIListAdapter(infoDataList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setItemAnimator(null);
        if (recyclerView.getItemDecorationCount() == 0) {
            recyclerView.addItemDecoration(new GridItemDividerDecoration(SizeUtils.dp2Px(this, 12f),
                    SizeUtils.dp2Px(this, 12f), ContextCompat.getColor(this, R.color.black)));
        }
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(listAdapter);

        listAdapter.setOnItemClickListener(new AIListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AIInfoData infoData, int position) {
                String title = infoData.getTitle();
                if (title.equals(getString(R.string.motion_photo))) {
                    Log.e(TAG, "###Start to start AI moving function");
                    startActivityForResult(new Intent(MainActivity.this, StillCutPhotoActivity.class),REQUEST_CODE_OF_AI_COLOR);
                } else if (title.equals(getString(R.string.face_smile))) {
                    startActivityForResult(MainActivity.this, MEDIA_TYPE_PHOTO,
                            REQUEST_CODE_OF_FACE_SMILE);
                } else if (title.equals(getString(R.string.ai_color))) {
                    startActivityForResult(MainActivity.this, MEDIA_TYPE_VIDEO_OR_PHOTO,
                            REQUEST_CODE_OF_AI_COLOR);
                } else if (title.equals(getString(R.string.img_segment_sub))) {
                    startActivity(new Intent(MainActivity.this, ImageSegmentationActivity.class));
                }
            }
        });
    }

    public static void startActivityForResult(Activity activity, int mediaType, int requestCode) {
        Intent intent = new Intent(activity, PicturePickActivity.class);
        /*intent.putExtra(SHOW_MEDIA_TYPE, mediaType);
        intent.putExtra(ACTION_TYPE, ACTION_AI_TYPE);*/
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || resultCode != RESULT_OK) {
            return;
        }
        SafeIntent safeIntent = new SafeIntent(data);
        ArrayList<MediaData> selectList = safeIntent.getParcelableArrayListExtra("select_result");
        if (!ArrayUtils.isEmpty(selectList)) {
            mFilePath = selectList.get(0).getPath();
            switch (requestCode) {
                case REQUEST_CODE_OF_FACE_REENACT:
                    faceReenact(mFilePath);
                    break;
                /*case REQUEST_CODE_OF_FACE_SMILE:
                    faceSmile(mFilePath);
                    break;
                case REQUEST_CODE_OF_AI_COLOR:
                    aiColor(mFilePath);
                    break;*/
                default:
                    break;
            }
        }
    }

    private void faceReenact(String imagePath) {
        initFaceReenactProgressDialog();
        mFaceReenact = new HVEAIFaceReenact();
        mFaceReenact.process(imagePath, new HVEAIProcessCallback<String>() {
            @Override
            public void onProgress(int progress) {
                if (!isValidActivity()) {
                    return;
                }
                runOnUiThread(() -> {
                    if (mFaceReenactDialog != null) {
                        if (!mFaceReenactDialog.isShowing()) {
                            mFaceReenactDialog.show();
                        }
                        mFaceReenactDialog.updateProgress(progress);
                    }
                });
            }

            @Override
            public void onSuccess(String result) {
                if (!isValidActivity()) {
                    return;
                }
                runOnUiThread(() -> {
                    if (mFaceReenactDialog != null) {
                        mFaceReenactDialog.updateProgress(0);
                        mFaceReenactDialog.dismiss();
                    }
                    ViewFileActivity.startActivity(MainActivity.this, result, true);
                });
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.e(TAG, errorMessage);
                if (!isValidActivity()) {
                    return;
                }
                runOnUiThread(() -> {
                    if (mFaceReenactDialog != null) {
                        mFaceReenactDialog.updateProgress(0);
                        mFaceReenactDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this, getString(R.string.ai_exception), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void initFaceReenactProgressDialog() {
        mFaceReenactDialog = new CommonProgressDialog(this, () -> {
            mFaceReenactDialog.dismiss();
            mFaceReenactDialog = null;
            if (mFaceReenact != null) {
                mFaceReenact.interruptProcess();
            }
        });
        mFaceReenactDialog.setTitleValue(getString(R.string.intelligent_processing));
        mFaceReenactDialog.setCanceledOnTouchOutside(false);
        mFaceReenactDialog.setCancelable(false);
        mFaceReenactDialog.show();
    }

    private void verifyStoragePermissions(MainActivity activity) {
        final int requestCode = 1;
        try {
            for (int i = 0; i < mPermissions.length; i++) {
                String permisson = mPermissions[i];
                int permissionRead = ActivityCompat.checkSelfPermission(activity, permisson);
                if (permissionRead != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, mPermissions, requestCode);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage());
        }
    }

    protected boolean isValidActivity() {
        return !isFinishing() && !isDestroyed();
    }
}