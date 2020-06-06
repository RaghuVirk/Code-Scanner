package com.google.android.gms.samples.vision.barcodereader;

import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
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
        ft.add(R.id.fm,CodeScannerFragment.newInstance(Main2Activity.this, false, true, false, true, "Scanner Text", new CodeScannerFragment.BarcodeCapturedListener() {
            @Override
            public void onBarcodeScanned(Barcode barcode) {
                Toast.makeText(Main2Activity.this, "Scanned Code : "+barcode.displayValue, Toast.LENGTH_SHORT).show();
            }
        })).commit();
    }

    public void onClick(View view) {

    }

    @Override
    public void onBarcodeDetected(Barcode barcode) {

    }
}
