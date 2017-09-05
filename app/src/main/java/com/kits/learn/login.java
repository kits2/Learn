package com.kits.learn;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;

/**
 * Created by blkj on 2017/7/23.
 */
import com.kits.learn.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.kits.learn.WebService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class login extends Activity implements OnClickListener {

    // 登陆按钮
    private Button logbtn, upload;
    // 调试文本，注册文本
    private TextView regtv;
    // 显示用户名和密码
    EditText username, password;
    // 创建等待框
    private ProgressDialog dialog;
    // 返回的数据
    private String info;

    SharedPreferences share;

    SharedPreferences.Editor editor;

    private static Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // 获取控件
        username = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.pass);
        logbtn = (Button) findViewById(R.id.login);
        regtv = (Button) findViewById(R.id.register);
        upload = (Button) findViewById(R.id.upload);

        // 设置按钮监听器
        logbtn.setOnClickListener(this);
        regtv.setOnClickListener(this);
        upload.setOnClickListener(this);

        share = getSharedPreferences("kits",MODE_PRIVATE);
        editor=share.edit();




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                // 检测网络，无法检测wifi
                if (!checkNetwork()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "网络未连接", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                }
                // 提示框
                dialog = new ProgressDialog(this);
                dialog.setTitle("提示");
                dialog.setMessage("正在登陆，请稍后...");
                dialog.setCancelable(false);
                dialog.show();
                // 创建子线程，分别进行Get和Post传输
                new Thread(new MyThread()).start();
                break;
            case R.id.register:
                Intent regItn = new Intent(login.this, register.class);
                startActivity(regItn);
                break;

            case R.id.upload:
                verifyStoragePermissions(login.this);
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                //打开系统提供的图片选择界面
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                //传参以在返回本界面时触发加载图片的功能
                startActivityForResult(intent, 0x1);
        }
        ;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x1 && resultCode == RESULT_OK) {
            if (data != null) {
                ContentResolver resolver = getContentResolver();
                try {
                    // 获取圖片URI
                    Uri uri = data.getData();
                    // 将URI转换为路径：
                    String[] proj = { MediaStore.Images.Media.DATA };
                    Cursor cursor = managedQuery(uri, proj, null, null, null);
                    //  这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    String photoPath = cursor.getString(column_index);
                    sendImage(photoPath);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }



    // 子线程接收数据，主线程修改数据
    public class MyThread implements Runnable {
        @Override
        public void run() {
            WebService web = new WebService();
            info = web.executeHttpGet("LogLet", username.getText().toString(), password.getText().toString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    String b = "t";
                    if (info.equals(b)) {
                        editor.putString("username",username.getText().toString());
                        editor.commit();
                        Toast.makeText(getApplicationContext(), "登陆成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "登陆失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // 检测网络
    private boolean checkNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    private void sendImage(String filename) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        //String filename = "/storage/emulated/0/Tencent/QQ_Images/10185978111AC3014E52F048F6128CCF82476F980D.jpg";
        Bitmap bm = BitmapFactory.decodeFile(filename, opt);
        bm.compress(Bitmap.CompressFormat.PNG, 60, stream);
        byte[] bytes = stream.toByteArray();
        String img = new String(Base64.encodeToString(bytes, Base64.DEFAULT));
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String user = share.getString("username",null);
        params.add("username",user);
        params.add("img", img);
        client.post("http://172.20.10.81:8080/dream/UpLoadPhotoServlet", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast.makeText(login.this, "Upload Success!", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(login.this, "Upload Fail!", Toast.LENGTH_LONG).show();
            }
        });
    }

         @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                 if (requestCode==1){
                       if (permissions[0].equals(Manifest.permission.RECORD_AUDIO)&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                           Toast.makeText(this,"666",Toast.LENGTH_SHORT).show();//得到权限之后去做的业务
                            }else {//没有获得到权限
                                 Toast.makeText(this,"你不给权限我就不好干事了啦",Toast.LENGTH_SHORT).show();
                            }
                     }
            }

    private static final String[] PERMISSION_EXTERNAL_STORAGE = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_EXTERNAL_STORAGE = 100;

    private void verifyStoragePermissions(Activity activity) {
        int permissionWrite = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_EXTERNAL_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}
