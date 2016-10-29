package samsung.com.pokemon;

import android.app.Activity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class AssetsObject {

    public class AssetsType {
        @SerializedName(AssetsTable.PK_ID)
        public String PK_ID;// 1
        @SerializedName(AssetsTable.NAMEAPP)
        public String NameApp;// 2
    }
}
