/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.virk.codescanner;

import android.content.Context;

import com.virk.codescanner.cameraUI.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private Context mContext;
    private boolean shouldPreviewCode;
    private CodeDetectionListener listener;

    public BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> mGraphicOverlay,
                                 Context mContext, boolean shouldPreviewCode, CodeDetectionListener listener) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.mContext = mContext;
        this.shouldPreviewCode = shouldPreviewCode;
        this.listener = listener;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {

        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay, shouldPreviewCode);
        if (listener != null) {
            listener.onCodeDetected(barcode);
        }
        return new BarcodeGraphicTracker(mGraphicOverlay, graphic, mContext);
    }

    public interface CodeDetectionListener {
        void onCodeDetected(Barcode barcode);
    }
}

