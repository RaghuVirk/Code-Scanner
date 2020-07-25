package com.google.android.gms.samples.vision.barcodereader;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.virk.codescanner.BarcodeCaptureActivity;
import com.virk.codescanner.BarcodeGraphicTracker;
import com.virk.codescanner.CodeScannerFragment;

import java.util.List;

public class Main2Activity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        BarcodeCaptureActivity.startScanner(Main2Activity.this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fm,CodeScannerFragment.newInstance(Main2Activity.this, false, true, false, true, Barcode.ALL_FORMATS ,"Scanner Text")).commit();
       /* BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        barcodeCapture.setRetrieval(this);*/
//        barcodeCapture.refresh(true);
    }

    public void onClick(View view) {

    }

    @Override
    public void onBarcodeDetected(final Barcode barcode) {
        Log.i("TAG", "onBarcodeDetected: " + barcode.displayValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Main2Activity.this, barcode.displayValue + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onRetrieved(final Barcode barcode) {
        Log.d("TAG", "Barcode read: " + barcode.displayValue);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this)
                        .setTitle("code retrieved")
                        .setMessage(barcode.displayValue);
                builder.show();
            }
        });


    }


}
