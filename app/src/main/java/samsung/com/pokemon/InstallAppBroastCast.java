package samsung.com.pokemon;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class InstallAppBroastCast extends BroadcastReceiver {

    private  Context mContext;
    private static String DEVICE_ID = "device_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;

        Bundle b = intent.getExtras();
        int uid = b.getInt(Intent.EXTRA_UID);
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        new PostAppInstalled().execute(packages);


    }
    // function post app installed
    class PostAppInstalled extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String [] params) {

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);

            for(int i=0;i<params.length;i++){
                String AppName = params[i];
                if(AppName==null){
                    break;
                }
                Cursor c=mContext.getContentResolver().query(SamsungProvider.URI_ASSET,null,AssetsTable.NAMEAPP+"=?",new String[]{AppName},null);

                while (c.moveToNext()){
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(
                            "http://www.3gt.io/pkm/WebService.asmx/updateAssetByAssetID");
                    try {
                        List<NameValuePair> nameValuePair = new ArrayList<>(0);
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.PK_ID, c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.GPSLAT,sharedPreferences.getString("lat","")));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.GPSLON,sharedPreferences.getString("lang","")));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.DATEINSTALL,getCurrentDateAndTime()));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.MACID, c.getString(c.getColumnIndexOrThrow(AssetsTable.MACID))));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.NAMEAPP, c.getString(c.getColumnIndexOrThrow(AssetsTable.NAMEAPP))));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        response.getEntity();
                        String resp = EntityUtils.toString(response.getEntity());
                        Log.e("tuyen.px :","app duoc suggest : "+resp+" dateinstall : "+getCurrentDateAndTime());
                        if (resp.trim().contains("true")) {
                            ContentValues values = new ContentValues();
                            values.put(AssetsTable.YSY, "");// 12
                            mContext.getContentResolver().update(SamsungProvider.URI_ASSET, values,
                                    AssetsTable.PK_ID + "=?", new String[]{
                                            c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))});
                        }else{
                            ContentValues values = new ContentValues();
                            values.put(AssetsTable.YSY, "false");// 12
                            mContext.getContentResolver().update(SamsungProvider.URI_ASSET, values,
                                    AssetsTable.PK_ID + "=?", new String[]{
                                            c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))});
                        }
                    } catch (Exception e) {
                        ContentValues values = new ContentValues();
                        values.put(AssetsTable.YSY, "false");// 12
                        mContext.getContentResolver().update(SamsungProvider.URI_ASSET, values,
                                AssetsTable.PK_ID + "=?", new String[]{
                                        c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))});
                    }
                }
                c.close();
            }
            return null;
        }
    }
    //2015-05-15 00:00:00.000
    private String getCurrentDateAndTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000");
        String currentDateandTime = sdf.format(new Date());
        return  currentDateandTime;

    }




}
