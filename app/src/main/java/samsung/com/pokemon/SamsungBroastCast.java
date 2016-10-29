package samsung.com.pokemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class SamsungBroastCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context,SamsungService.class));
    }
}
