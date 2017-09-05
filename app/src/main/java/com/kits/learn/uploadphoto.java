package com.kits.learn;

/**
 * Created by celly on 2017/9/2.
 */
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

public class uploadphoto extends Activity{
    private void upLoadImage(String url) {

        try {
            AsyncHttpClient client = new AsyncHttpClient();
            //params参数中传入服务器需要上传的参数以及文件 File或者bitmap等
            RequestParams params = new RequestParams();
            params.put("user_id", "123");
            params.put("File", new File("/storage/emulated/0/xiami.apk"));

            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                    //super.onSuccess(statusCode, headers, responseBody);
                    //访问成功的回调 responseBody是响应内容
                    Toast.makeText(getApplicationContext(),"onSuccess",Toast.LENGTH_LONG).show();
                    Log.e("onSuccess", new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] responseBody, Throwable error) {
                    //super.onFailure(statusCode, headers, responseBody, error);
                    //访问失败的回调
                    Toast.makeText(getApplicationContext(),"onFailure",Toast.LENGTH_LONG).show();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
