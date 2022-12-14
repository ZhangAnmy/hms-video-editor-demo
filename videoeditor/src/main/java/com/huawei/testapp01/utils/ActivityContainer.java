/*
 *   Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.huawei.testapp01.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityContainer {
    private static final ActivityContainer INSTANCE = new ActivityContainer();

    private static List<Activity> activityStack = new ArrayList<Activity>();

    private ActivityContainer() {
    }

    public static ActivityContainer getInstance() {
        return INSTANCE;
    }

    /**
     * addActivity
     * 
     * @param aty aty
     */
    public void addActivity(Activity aty) {
        activityStack.add(aty);
    }

    /**
     * removeActivity
     * 
     * @param aty aty
     */
    public void removeActivity(Activity aty) {
        activityStack.remove(aty);
    }
}
