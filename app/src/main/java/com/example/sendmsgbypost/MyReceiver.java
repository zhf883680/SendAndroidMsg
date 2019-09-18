package com.example.sendmsgbypost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1) {

        //Toast.makeText(arg0, "收到短信", Toast.LENGTH_LONG).show();

        Object[] objs = (Object[]) arg1.getExtras().get("pdus");

        for(Object obj : objs)
        {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
            String from = smsMessage.getOriginatingAddress();

            String body = smsMessage.getMessageBody();

            Map<String, String> params = new HashMap<String, String>();
            if(body.indexOf("验证码")>-1||body.indexOf("提取码")>-1){
                params.put("TA_action_on", "1");
                params.put("TA_title", from);
                params.put("TA_content", body);
                try{
                    Thread thread = new MyThread1(params);
                    thread.start();
                }
                catch (Exception e){
                    //Toast.makeText(arg0, e.toString(), Toast.LENGTH_LONG).show();
                    //System.out.println(e);
                }

                //String text = "发送者:"+from +"  内容："+body;
                //Toast.makeText(arg0, text, Toast.LENGTH_LONG).show();
            }

        }
    }
    public class MyThread1 extends Thread
    {
        private Map<String, String> params;
        public MyThread1(Map<String, String> params)
        {
            this.params = params;
        }
        public void run()
        {
            try {
                // todo 此处使用了server酱的webhook地址,请自行设置自己的值
                boolean result = sendPostRequest("", params, "UTF-8");
            }
            catch (Exception e){
                //Toast.makeText("123", e.toString(), Toast.LENGTH_LONG).show();
                System.out.println(e);
            }
        }
    }
    public static boolean sendPostRequest(String path,
                                         Map<String, String> params, String encoding) throws Exception {
        StringBuilder data = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                data.append(entry.getKey()).append("=");
                data.append(URLEncoder.encode(entry.getValue(), encoding));// 编码
                data.append('&');
            }
            data.deleteCharAt(data.length() - 1);
        }
        byte[] entity = data.toString().getBytes(); // 得到实体数据
        HttpURLConnection connection = (HttpURLConnection) new URL(path)
                .openConnection();
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",
                String.valueOf(entity.length));

        connection.setDoOutput(true);// 允许对外输出数据
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(entity);

        if (connection.getResponseCode() == 200) {
            return true;
        }
        return false;
    }
}
