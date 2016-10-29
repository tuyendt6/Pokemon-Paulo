package samsung.com.pokemon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class RegisterActivity extends Activity {
    private EditText mNumberPhone;
    private Button mRegister;
    private static String DEVICE_ID = "device_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
        if(sharedPreferences.getString("flag","false").equals("true")){
            Intent i= new Intent(getBaseContext(),MainActivity.class);
            i.putExtra("NumberPhone","");
            startActivity(i);
            finish();
        }else{
            setContentView(R.layout.layout_first);
            if(!isNetworkOnline()){
                showDialogAuthenFail();
                finish();
            }
            mNumberPhone=(EditText)findViewById(R.id.edtnumver);
            mRegister=(Button) findViewById(R.id.btnregister);

            mRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mNumberPhone.getText().toString().trim().equals("")){
                        Toast.makeText(getBaseContext(),"number phone is not null",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences(DEVICE_ID, Context.MODE_PRIVATE);
                    Intent i= new Intent(getBaseContext(),MainActivity.class);
                    i.putExtra("NumberPhone",mNumberPhone.getText().toString().trim());
                    startActivity(i);
                    SharedPreferences.Editor editor= sharedPreferences.edit();
                    editor.putString("flag","true");
                    editor.commit();
                    finish();

                }
            });
        }

    }


    private boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;
    }

    private void showDialogAuthenFail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Application requires InterNet  setup!");
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



}
