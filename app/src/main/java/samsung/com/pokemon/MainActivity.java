package samsung.com.pokemon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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


public class MainActivity extends Activity {

    private static String DEVICE_ID = "device_id";
    private static String MAC_ID = "mac_id";
    private Context mContext;
    private List<AssetsObject.AssetsType> mListAssets;
    private Gson mGson;
    private Type mType;
    private ArrayList<String> mListApp=new ArrayList<String>();
    private Location mLocation;
    private Double lat =0.0;
    private Double Lang=0.0;
    private String provider;
    LocationListener locationListener;
    LocationManager locationManager;
    private static  boolean postappinstalled=false;
    private static String[] listpost =new String[100];
    String Mac;
    String Phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext =getBaseContext();
        Phone =getIntent().getStringExtra("NumberPhone");
        SharedPreferences sharedPreferences = getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
        String deviceID = sharedPreferences.getString(MAC_ID, "");
        Mac=deviceID;
        getListAppInstaled();
        if (deviceID.equals("")) {
            String device = getDeviceID();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(MAC_ID, device);
            editor.commit();
            Mac=device;
        }
        // Post to PhoneTable
        if(!Phone.equals("")){
            new PostPhone().execute();
        }
        if(!isGpsEnable()){
            showDialogAuthenFail();
        }else{
            new SamsungConnect2Server().execute();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(true); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money :-)
        provider = locationManager.getBestProvider(criteria, false);
        mLocation = locationManager.getLastKnownLocation(provider);
        if (mLocation!=null){
            lat = mLocation.getLatitude();
            Lang = mLocation.getLongitude();
        }

        locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                doWorkWithNewLocation(location);
            }
        };

        long minTime = 5 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
        long minDistance = 10; // Minimum distance change for update in meters, i.e. 10 meters.

        locationManager.requestLocationUpdates(getProviderName(), minTime,
                minDistance, locationListener);




    }


    static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;

    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();
        if(isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if(isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if(timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }


    public String getProviderName() {
        LocationManager locationManager =  (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(true); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money :-)

        // Provide your criteria and flag enabledOnly that tells
        // LocationManager only to return active providers.
        return locationManager.getBestProvider(criteria, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
  //      pauseRev();
    }

    void doWorkWithNewLocation(Location location) {
        if(mLocation == null){
            mLocation = location;
            return;
        }
        if(isBetterLocation(mLocation, location)) {
            mLocation = location;
        }
        if (mLocation!=null){
            lat = mLocation.getLatitude();
            Lang = mLocation.getLongitude();
            SharedPreferences sharedPreferences = getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("lat",lat+"");
            editor.putString("lang",Lang+"");
            editor.commit();
             if(postappinstalled){
                 postappinstalled=!postappinstalled;
                 new PostAppInstalled().execute(listpost);
             }

        }
    }
    public String getDeviceID() {
        String dev_id = "35"
                + // we make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10; // 13 digits
        return dev_id;
    }

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
                Log.e("tuyen.px","sao e : "+e.toString());
                clearAll();
            }
            insertTableAssess(mListAssets);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent i =new Intent(getBaseContext(),SamsungService.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(i);
        }
    }


   private void clearAll(){
       if(mListAssets!=null){
           mListAssets=null;
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
        Log.e("tuyen.px","query record :" +result );
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
            postappinstalled=true;
           for(int i=0;i<arrayList.size();i++){
               listpost[i]=arrayList.get(i);
           }
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
        Log.e("tuyenpx","Notification = "+PKID);

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

    private boolean isGpsEnable(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enable=  service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return enable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10){
            if(isGpsEnable()){

            }else{
                finish();
            }
        }
    }

    private void showDialogAuthenFail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Application requires GPS setup!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent,10);
                finish();
            }
        });
        builder.create().show();
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
            for(int i=0;i<params.length;i++){

                String pkID = params[i];
                if(pkID==null){
                    break;
                }
                Log.e("Check PKID","app da cai : "+pkID);
                Cursor c=mContext.getContentResolver().query(SamsungProvider.URI_ASSET,null,AssetsTable.PK_ID+"=?",new String[]{pkID},null);

                while (c.moveToNext()){
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(
                            "http://www.3gt.io/pkm/WebService.asmx/updateAssetByAssetID");
                    try {
                        List<NameValuePair> nameValuePair = new ArrayList<>(0);
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.PK_ID, c.getString(c.getColumnIndexOrThrow(AssetsTable.PK_ID))));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.GPSLAT,lat+""));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.GPSLON,Lang+""));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.DATEINSTALL,"-1"));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.MACID, c.getString(c.getColumnIndexOrThrow(AssetsTable.MACID))));
                        nameValuePair.add(new BasicNameValuePair(AssetsTable.NAMEAPP, c.getString(c.getColumnIndexOrThrow(AssetsTable.NAMEAPP))));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        response.getEntity();
                        String resp = EntityUtils.toString(response.getEntity());
                        Log.e("tuyenpx : ","post app da cai : " +resp);
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

    class PostPhone extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void [] params) {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(
                            "http://www.3gt.io/pkm/WebService.asmx/addNewOrUpdatePhone");
                    try {
                        List<NameValuePair> nameValuePair = new ArrayList<>(0);
                        nameValuePair.add(new BasicNameValuePair("MACID",Mac));
                        nameValuePair.add(new BasicNameValuePair("PHONENR",Phone));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        response.getEntity();
                        String resp = EntityUtils.toString(response.getEntity());
                        Log.e("tuyen.px ","addPhoneNumber"+resp);
                        if (resp.trim().contains("true")) {

                        }else{

                        }
                    } catch (Exception e) {

                    }
            return null;
        }
    }


}
