package com.google.android.gms.samples.vision.barcodereader;

import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.virk.codescanner.BarcodeGraphicTracker;
import com.virk.codescanner.CodeScannerFragment;

public class Main2Activity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fm,CodeScannerFragment.newInstance(Main2Activity.this, false, true, false, true, "Scanner Text")).commit();
    }

    public void onClick(View view) {

    }

    @Override
    public void onBarcodeDetected(final Barcode barcode) {
        Log.i("TAG", "onBarcodeDetected: "+barcode.displayValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Main2Activity.this, barcode.displayValue+"", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
