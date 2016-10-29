package samsung.com.pokemon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class SamsungService extends Service {

    private final int TIME_QUERY_DATA_DELAY =2 * 60 * 1000;
    private final int TIME_QUERY_DATA = 30 * 60 * 1000;

    private Timer mTimerQueryServer;
    private static String DEVICE_ID = "device_id";
    private static String MAC_ID = "mac_id";
    private Context mContext;
    private List<AssetsObject.AssetsType> mListAssets;
    private Gson mGson;
    private Type mType;
    private ArrayList<String> mListApp=new ArrayList<String>();
    private static String[] listpost =new String[100];
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getBaseContext();
        mTimerQueryServer = new Timer();
        getListAppInstaled();
        mTimerQueryServer.schedule(mTaskQueryData, TIME_QUERY_DATA_DELAY, TIME_QUERY_DATA);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private TimerTask mTaskQueryData = new TimerTask() {
        @Override
        public void run() {
            new SamsungConnect2Server().execute();
        }
    };


    public class SamsungConnect2Server extends AsyncTask<Void ,Void,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                queryTableAssess();
            }catch (Exception e){
                clearAll();
            }
            insertTableAssess(mListAssets);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    private void clearAll(){
        if(mListAssets!=null){
            mListApp=null;
        }
    }
    private String queryTableAuthen() {
        StringBuilder sBuiler = new StringBuilder();
        SharedPreferences sharedPreferences = getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
        String deviceID = sharedPreferences.getString(MAC_ID, "");
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://www.3gt.io/pkm/WebService.asmx/getAllAssetsByMacID");

            List<NameValuePair> listParams = new ArrayList<NameValuePair>();
            listParams.add(new BasicNameValuePair("MACID", deviceID));
            httppost.setEntity(new UrlEncodedFormEntity(listParams));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            BufferedReader bfr = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line;
            while ((line = bfr.readLine()) != null) {
                sBuiler.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = chuanHoaKetQua(sBuiler.toString());
        return result;
    }
    private String chuanHoaKetQua(String result) {
        result = result.replace(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?><string xmlns=\"http://tempuri.org/\">",
                "");
        result = result.replace("</string>", "");
        return result;
    }


    private void queryTableAssess() {
        mGson = new Gson();
        mType = new TypeToken<List<AssetsObject.AssetsType>>() {
        }.getType();
        mListAssets = mGson.fromJson(queryTableAuthen(), mType);
        Log.e("tuyen.px ", "Pokemon - query table Assess duoc " + mListAssets.size() + " rows");
    }
    private void insertTableAssess(List<AssetsObject.AssetsType> list) {
        if (list == null) {
            Log.e("tuyen.px", "Insert table Vendedores ignored");
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
        String deviceID = sharedPreferences.getString(MAC_ID, "");

        ArrayList<String> arrayList=new ArrayList<>();
        for (AssetsObject.AssetsType va : list) {
            if(!checkPkID(va.PK_ID)&&!mListApp.contains(va.NameApp)){
                ContentValues values = new ContentValues();
                values.put(AssetsTable.PK_ID, va.PK_ID);// 1
                values.put(AssetsTable.DATEINSTALL, "");// 2
                values.put(AssetsTable.GPSLAT, "");// 1
                values.put(AssetsTable.GPSLON, "");// 2
                values.put(AssetsTable.MACID, deviceID);// 1
                values.put(AssetsTable.NAMEAPP, va.NameApp);// 2
                values.put(AssetsTable.YSY, "");// 2
                mContext.getContentResolver().insert(SamsungProvider.URI_ASSET, values);
                showNotify(mContext,Integer.parseInt(va.PK_ID),va.NameApp);
            }
            else if(mListApp.contains(va.NameApp)){
                ContentValues values = new ContentValues();
                values.put(AssetsTable.PK_ID, va.PK_ID);// 1
                values.put(AssetsTable.DATEINSTALL, "-1");// 2
                values.put(AssetsTable.GPSLAT, "");// 1
                values.put(AssetsTable.GPSLON, "");// 2
                values.put(AssetsTable.MACID, deviceID);// 1
                values.put(AssetsTable.NAMEAPP, va.NameApp);// 2
                values.put(AssetsTable.YSY, "");// 2
                mContext.getContentResolver().insert(SamsungProvider.URI_ASSET, values);
                // post database to server : update application is install
                arrayList.add(va.PK_ID);
            }
        }
        if(arrayList.size()>0){
            for(int i=0;i<arrayList.size();i++){
                listpost[i]=arrayList.get(i);
            }
            new PostAppInstalled().execute(listpost);
        }
        arrayList.remove(arrayList);
    }


    /**
     * Hien thi Notify len khi den thoi gian Alarm fire
     * @param context
     * @param PKID :
     * @param AppName : s
     */
    @SuppressWarnings("deprecation")
    private void showNotify(Context context, int PKID, String AppName) {
        Notification n = new Notification(R.drawable.android_sh, "Suggestion Install!", 0);
        Intent intent = new Intent(context, SuggestionActivity.class);
        intent.putExtra("AppName", AppName);
        PendingIntent pi = PendingIntent.getActivity(context,PKID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        n.setLatestEventInfo(context,"Suggestion Install",AppName, pi);
        n.defaults = Notification.DEFAULT_LIGHTS;
        n.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        n.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        n.vibrate = new long[] {
                500, 500, 500, 500, 500, 500
        };
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e("tuyenpx :","PK ID ="+PKID);
        nm.notify(PKID, n);
    }


    private boolean checkPkID(String pkID){
        Cursor c=mContext.getContentResolver().query(SamsungProvider.URI_ASSET,null,null,null,null);
        boolean flag=false;
        while (c.moveToNext()){
            if(pkID.equals(c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID)))){
                flag =true;
                break;
            }
        }
        c.close();
        return  flag;
    }

    private void getListAppInstaled(){
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if(!mListApp.contains(p.packageName)&&!isSystemPackage(p)){
                mListApp.add(p.packageName);
                Log.e("tuyen.px: ",p.packageName);
            }
        }
    }
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    // function post app installed
    class PostAppInstalled extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String [] params) {
            if(params.length==0){
                return  null;
            }

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
            for(int i=0;i<params.length;i++){
                String pkID = params[i];
                if(pkID==null){
                    break;
                }

                Cursor c=mContext.getContentResolver().query(SamsungProvider.URI_ASSET,null,AssetsTable.PK_ID+"=?",new String[]{pkID},null);

                while (c.moveToNext()){
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(
                            "http://www.3gt.io/pkm/WebService.asmx/updateAssetByAssetID");
                    try {
                        List<NameValuePair> nameValuePair = new ArrayList<>(0);
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.PK_ID, c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.GPSLAT,sharedPreferences.getString("lat","1234")));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.GPSLON,sharedPreferences.getString("lang","1234")));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.DATEINSTALL,"-1"));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.MACID, c.getString(c.getColumnIndexOrThrow(AssetsTable.MACID))));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.NAMEAPP, c.getString(c.getColumnIndexOrThrow(AssetsTable.NAMEAPP))));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        response.getEntity();
                        String resp = EntityUtils.toString(response.getEntity());

                        Log.e("tuyen.px : ","post app da install : "+resp);

                        if (resp.trim().contains("true")) {
                            ContentValues values = new ContentValues();
                            values.put(AssetsTable.YSY, "");// 12
                            getContentResolver().update(SamsungProvider.URI_ASSET, values,
                                    AssetsTable.PK_ID + "=?", new String[]{
                                            c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))});
                        }else{
                            ContentValues values = new ContentValues();
                            values.put(AssetsTable.YSY, "false");// 12
                            getContentResolver().update(SamsungProvider.URI_ASSET, values,
                                    AssetsTable.PK_ID + "=?", new String[]{
                                            c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))});
                        }
                    } catch (Exception e) {
                        ContentValues values = new ContentValues();
                        values.put(AssetsTable.YSY, "false");// 12
                        getContentResolver().update(SamsungProvider.URI_ASSET, values,
                                AssetsTable.PK_ID + "=?", new String[]{
                                        c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))});
                    }
                }
                c.close();
            }
            return null;
        }
    }
}
