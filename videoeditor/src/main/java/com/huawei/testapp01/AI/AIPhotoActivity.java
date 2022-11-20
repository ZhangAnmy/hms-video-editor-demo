/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.testapp01.AI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting;
import com.huawei.testapp01.BaseActivity;
import com.huawei.testapp01.R;
import com.huawei.testapp01.callback.ImageSegmentationResultCallBack;
import com.huawei.testapp01.image.StillCutPhotoActivity;
import com.huawei.testapp01.utils.BitmapUtils;
import com.huawei.testapp01.utils.ImageUtils;
import com.huawei.testapp01.view.GraphicOverlay;

import java.io.IOException;

public class AIPhotoActivity extends BaseActivity implements ImageSegmentationResultCallBack {
    private GraphicOverlay graphicOverlay;

    private ImageView preview;

    private Uri imageUri;

    private Uri imgBackgroundUri;

    private Bitmap originBitmap;

    private Bitmap backgroundBitmap;

    private static String TAG = "AIPhotoActivity";

    private Integer maxWidthOfImage;

    private Integer maxHeightOfImage;

    boolean isLandScape;

    private int REQUEST_CHOOSE_ORIGINPIC = 2001;

    private int REQUEST_CHOOSE_BACKGROUND = 2002;

    private static final String KEY_IMAGE_URI = "KEY_IMAGE_URI";

    private static final String KEY_IMAGE_MAX_WIDTH = "KEY_IMAGE_MAX_WIDTH";

    private static final String KEY_IMAGE_MAX_HEIGHT = "KEY_IMAGE_MAX_HEIGHT";

    private Bitmap processedImage;

    // Portrait foreground image.
    private Bitmap foreground;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        createImageTransactor();
        this.isLandScape =
                (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == this.REQUEST_CHOOSE_ORIGINPIC) && (resultCode == Activity.RESULT_OK)) {
            // In this case, imageUri is returned by the chooser, save it.
            this.imageUri = data.getData();
            this.loadOriginImage();
        } else if ((requestCode == this.REQUEST_CHOOSE_BACKGROUND)) {
            if (data == null) {
                Toast.makeText(this.getApplicationContext(), R.string.please_select_picture, Toast.LENGTH_SHORT).show();
            } else {
                this.imgBackgroundUri = data.getData();
                this.loadOriginImage();
                Pair<Integer, Integer> targetedSize = this.getTargetSize();
                this.backgroundBitmap = BitmapUtils.loadFromPath(AIPhotoActivity.this, this.imgBackgroundUri, targetedSize.first, targetedSize.second);
            }
        }
    }

    private MLImageSegmentationAnalyzer analyzer;

    private void createImageTransactor() {
        //Set the image segmentation analyzer,BODY_SEG-->Detection mode 0: detection based on the portrait model
        MLImageSegmentationSetting setting = new MLImageSegmentationSetting.Factory().setAnalyzerType(MLImageSegmentationSetting.BODY_SEG).create();
        this.analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting);
        if (this.isChosen(this.originBitmap)) {
            MLFrame mlFrame = new MLFrame.Creator().setBitmap(this.originBitmap).create();
            Task<MLImageSegmentation> task = this.analyzer.asyncAnalyseFrame(mlFrame);
            task.addOnSuccessListener(new OnSuccessListener<MLImageSegmentation>() {
                @Override
                public void onSuccess(MLImageSegmentation mlImageSegmentationResults) {
                    // Transacting logic for segment success.
                    if (mlImageSegmentationResults != null) {
                        AIPhotoActivity.this.foreground = mlImageSegmentationResults.getForeground();
                        AIPhotoActivity.this.preview.setImageBitmap(AIPhotoActivity.this.foreground);
                        AIPhotoActivity.this.processedImage = ((BitmapDrawable) ((ImageView) AIPhotoActivity.this.preview).getDrawable()).getBitmap();
                    } else {
                        AIPhotoActivity.this.displayFailure();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // Transacting logic for segment failure.
                    AIPhotoActivity.this.displayFailure();
                    return;
                }
            });
        } else {
            Toast.makeText(this.getApplicationContext(), R.string.please_select_picture, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void displayFailure() {
        Toast.makeText(this.getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
    }

    private boolean isChosen(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        } else {
            return true;
        }
    }

    private void loadOriginImage() {
        if (this.imageUri == null) {
            return;
        }
        // Clear the overlay first.
        this.graphicOverlay.clear();
        Pair<Integer, Integer> targetedSize = this.getTargetSize();
        @SuppressLint({"NewApi", "LocalSuppress"}) int targetWidth = targetedSize.first;
        @SuppressLint({"NewApi", "LocalSuppress"}) int targetHeight = targetedSize.second;
        this.originBitmap = BitmapUtils.loadFromPath(AIPhotoActivity.this, this.imageUri, targetWidth, targetHeight);
        // Determine how much to scale down the image.
        Log.i("imageSlicer", "resized image size width:" + this.originBitmap.getWidth() + ",height: " + this.originBitmap.getHeight());
        this.preview.setImageBitmap(this.originBitmap);
    }

    // Returns max width of image.
    private Integer getMaxWidthOfImage() {
        if (this.maxWidthOfImage == null) {
            if (this.isLandScape) {
                this.maxWidthOfImage = ((View) this.preview.getParent()).getHeight();
            } else {
                this.maxWidthOfImage = ((View) this.preview.getParent()).getWidth();
            }
        }
        return this.maxWidthOfImage;
    }

    // Returns max height of image.
    private Integer getMaxHeightOfImage() {
        if (this.maxHeightOfImage == null) {
            if (this.isLandScape) {
                this.maxHeightOfImage = ((View) this.preview.getParent()).getWidth();
            } else {
                this.maxHeightOfImage = ((View) this.preview.getParent()).getHeight();
            }
        }
        return this.maxHeightOfImage;
    }

    // Gets the targeted size(width / height).
    @SuppressLint("NewApi")
    private Pair<Integer, Integer> getTargetSize() {
        Integer targetWidth;
        Integer targetHeight;
        Integer maxWidth = this.getMaxWidthOfImage();
        Integer maxHeight = this.getMaxHeightOfImage();
        targetWidth = this.isLandScape ? maxHeight : maxWidth;
        targetHeight = this.isLandScape ? maxWidth : maxHeight;
        Log.i(AIPhotoActivity.TAG, "height:" + targetHeight + ",width:" + targetWidth);
        return new Pair<>(targetWidth, targetHeight);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.analyzer != null) {
            try {
                this.analyzer.stop();
            } catch (IOException e) {
                Log.e(TAG, "Stop analyzer failed: " + e.getMessage());
            }
        }
        this.imageUri = null;
        this.imgBackgroundUri = null;
        BitmapUtils.recycleBitmap(this.originBitmap, this.backgroundBitmap, this.foreground, this.processedImage);
        if (this.graphicOverlay != null) {
            this.graphicOverlay.clear();
            this.graphicOverlay = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(AIPhotoActivity.KEY_IMAGE_URI, this.imageUri);
        if (this.maxWidthOfImage != null) {
            outState.putInt(AIPhotoActivity.KEY_IMAGE_MAX_WIDTH, this.maxWidthOfImage);
        }
        if (this.maxHeightOfImage != null) {
            outState.putInt(AIPhotoActivity.KEY_IMAGE_MAX_HEIGHT, this.maxHeightOfImage);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void callResultBitmap(Bitmap bitmap) {
        this.processedImage = bitmap;
    }
}
