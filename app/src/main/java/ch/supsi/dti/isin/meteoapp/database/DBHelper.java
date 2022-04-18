package ch.supsi.dti.isin.meteoapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "MeteoApp_DB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + DBSchema.LocationsTable.TABLE_NAME + "("
                + " _id integer primary key autoincrement, "
                + DBSchema.LocationsTable.Cols.ID
                + ", "
                + DBSchema.LocationsTable.Cols.NAME
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
