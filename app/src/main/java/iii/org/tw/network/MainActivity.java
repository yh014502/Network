package iii.org.tw.network;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private ConnectivityManager cmg;
    private String data;
    private TextView mesg;
    private StringBuffer sb;
    private UIHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mesg=(TextView)findViewById(R.id.mesg);
        handler = new UIHandler();
        cmg=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info=cmg.getActiveNetworkInfo();
        if(info!=null&& info.isConnected()){
            try {
                Enumeration<NetworkInterface> ifs =NetworkInterface.getNetworkInterfaces();
                while(ifs.hasMoreElements()){
                    NetworkInterface ip=ifs.nextElement();
                    Enumeration<InetAddress> ips = ip.getInetAddresses();
                    while (ips.hasMoreElements()){
                        InetAddress ia = ips.nextElement();
                        Log.d("yisin", ia.getHostAddress());
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }else{
            Log.d("yisin","noconnect");
        }
    }
    //網路行為要包在執行緒裡才可以
    public void test1(View v){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL("http://data.coa.gov.tw/Service/OpenData/EzgoAttractions.aspx");
                    HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                    conn.connect();
//                    InputStream in=conn.getInputStream();
//                    int c;StringBuffer sb=new StringBuffer();
//                    while ((c=in.read())!=-1){
//                        sb.append((char)c);
//                    }
//                    in.close();
//                    Log.d("yisin",sb.toString());

                    BufferedReader reader=new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    data=reader.readLine();
                    reader.close();
                    parseJSON();
                }catch (Exception ee){
                    Log.d("yisin",ee.toString());
                }
            }
        }.start();

    }
    private void parseJSON(){
        sb = new StringBuffer();
        try {
            JSONArray root = new JSONArray(data);
            for (int i=0; i<root.length(); i++){
                JSONObject row = root.getJSONObject(i);
                String name = row.getString("Name");
                String addr = row.getString("Address");
                Log.d("yisin", name + " -> " + addr);
                sb.append(name + " -> " + addr + "\n");
            }
            handler.sendEmptyMessage(0);
        }catch(Exception ee){
            Log.d("yisin", ee.toString());
        }
    }

    private class UIHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mesg.setText(sb);


        }
    }

}
