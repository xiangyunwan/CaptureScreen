package com.baidu_lishuang10.capturescreen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mScreenShortBtn;
    private Button mScreenRecordBtn;
    private MediaProjectionManager mMpMngr;
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private Intent mResultIntent = null;
    private int mResultCode = 0;
    public static final String TAG = "MainAc";
    boolean isCapture;

    private Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    private Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScreenShortBtn = (Button) findViewById(R.id.btn_screen_short);
        mScreenRecordBtn = (Button) findViewById(R.id.btn_screen_record);
        mScreenShortBtn.setOnClickListener(this);
        mScreenRecordBtn.setOnClickListener(this);
        mMpMngr = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mResultIntent = ((MyApplication) getApplication()).getResultIntent();
        mResultCode = ((MyApplication) getApplication()).getResultCode();

        requestPermission(1, Manifest.permission.WRITE_EXTERNAL_STORAGE, new Runnable() {
            @Override
            public void run() {

            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });
        requestPermission(2, Manifest.permission.READ_EXTERNAL_STORAGE, new Runnable() {
            @Override
            public void run() {

            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });
        requestPermission(3, Manifest.permission.RECORD_AUDIO, new Runnable() {
            @Override
            public void run() {

            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });
        requestPermission(4, Manifest.permission.CAPTURE_AUDIO_OUTPUT, new Runnable() {
            @Override
            public void run() {

            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == mScreenShortBtn) {
            isCapture = true;
            startIntent();
            stopService(new Intent(getApplicationContext(),  RecordService.class ));
        } else if (view == mScreenRecordBtn) {
            isCapture=false;
            startIntent();
            stopService(new Intent(getApplicationContext(),  CaptureService.class ));
        }
    }

    private void startIntent() {
        if (mResultIntent != null && mResultCode != 0) {
            startService(new Intent(getApplicationContext(), isCapture?CaptureService.class:RecordService.class));
        } else {
            startActivityForResult(mMpMngr.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG,"get capture permission success!");
                mResultCode = resultCode;
                mResultIntent = data;
                ((MyApplication) getApplication()).setResultCode(resultCode);
                ((MyApplication) getApplication()).setResultIntent(data);
                ((MyApplication) getApplication()).setMpmngr(mMpMngr);
                startService(new Intent(getApplicationContext(),isCapture?CaptureService.class:RecordService.class));

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Runnable allowRun = allowablePermissionRunnables.get(requestCode);
            allowRun.run();
        } else {
            Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
            disallowRun.run();
        }
    }

    /**
     * 请求权限
     * @param id 请求授权的id 唯一标识即可
     * @param permission 请求的权限
     * @param allowableRunnable 同意授权后的操作
     * @param disallowableRunnable 禁止权限后的操作
     */
    protected void requestPermission(int id, String permission, Runnable allowableRunnable, Runnable disallowableRunnable) {
        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        allowablePermissionRunnables.put(id, allowableRunnable);
        if (disallowableRunnable != null) {
            disallowablePermissionRunnables.put(id, disallowableRunnable);
        }

        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //弹出对话框接收权限
                ActivityCompat.requestPermissions(this, new String[]{permission}, id);
                return;
            } else {
                allowableRunnable.run();
            }
        } else {
            allowableRunnable.run();
        }
    }
}
