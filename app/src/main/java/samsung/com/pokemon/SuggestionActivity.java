package samsung.com.pokemon;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class SuggestionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String packages = getIntent().getStringExtra("AppName");
        Log.e("tuyen.px","tuyen.px"+packages);
        startGooglePlay(packages);
        finish();
    }
    private  void startGooglePlay(String pakageName) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + pakageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + pakageName)));
        }
    }

}
