package samsung.com.pokemon;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SamSunger on 5/23/2015.
 */
public class AssetsTable {

    public static final String TBL_NAME = "tbl_Asset";
    public static final String _ID = "id";//1
    public static final String PK_ID = "PkID";//2
    public static final String GPSLAT = "GPSLAT";//3
    public static final String GPSLON = "GPSLON";//4
    public static final String DATEINSTALL = "DATEINSTALL";//4
    public static final String MACID = "MACID";//4
    public static final String NAMEAPP = "NAMEAPP";//4
    public static final String YSY = "YSY";//4

    private static String createData() {
        StringBuilder sBuiler = new StringBuilder();
        sBuiler.append("create table " + TBL_NAME + " (");
        sBuiler.append(_ID + " integer primary key autoincrement, ");//1
        sBuiler.append(PK_ID + " text, ");//2
        sBuiler.append(GPSLAT + " text, ");//3
        sBuiler.append(GPSLON + " text, ");//2
        sBuiler.append(DATEINSTALL + " text, ");//3
        sBuiler.append(MACID + " text, ");//2
        sBuiler.append(NAMEAPP + " text, ");//3
        sBuiler.append(YSY + " text);");//4
        return sBuiler.toString();
    }
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(createData());
    }



    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
        onCreate(database);
    }

}
