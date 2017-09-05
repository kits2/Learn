package com.kits.learn;

/**
 * Created by blkj on 2017/7/23.
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
public class WebService {

        private static String IP = "172.20.10.81:8080";

        // 通过Get方式获取HTTP服务器数据
        public static String executeHttpGet(String mode,String username, String password) {

            HttpURLConnection conn = null;
            InputStream is = null;

            try {
                // 用户名 密码
                // URL 地址
                String path = "http://" + IP + "/dream/"+ mode;
                path = path + "?username=" + username + "&password=" + password;

                conn = (HttpURLConnection) new URL(path).openConnection();
                conn.setConnectTimeout(3000); // 设置超时时间
                conn.setReadTimeout(3000);
                conn.setDoInput(true);
                conn.setRequestMethod("GET"); // 设置获取信息方式
                conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

                if (conn.getResponseCode() == 200) {
                    is = conn.getInputStream();
                    String t = parseInfo(is);
                    return t;
                }

            }catch (Exception e) {
                e.printStackTrace();
            } /*finally {
                // 意外退出时进行连接关闭保护
                if (conn != null) {
                    conn.disconnect();
                }
                if (is != null)
                {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }*/
            return "i";
        }

        // 将输入流转化为 String 型
        private static String parseInfo(InputStream inStream) throws Exception {
            byte[] data = read(inStream);
            // 转化为字符串
            return new String(data, "UTF-8");
        }

        // 将输入流转化为byte型
        public static byte[] read(InputStream inStream) throws Exception {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inStream.close();
            return outputStream.toByteArray();
        }
    }


