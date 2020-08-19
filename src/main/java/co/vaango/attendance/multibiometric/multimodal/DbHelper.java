package co.vaango.attendance.multibiometric.multimodal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private  static final int DATABASE_VERSION = 1;
    private  static final String CREATE_TABLE = "CREATE TABLE "+ DbVisit.TABLE_NAME + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+ DbVisit.NAME +" TEXT,"+DbVisit.SYNC_STATUS+" INTEGER);";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+ DbVisit.TABLE_NAME;

    public DbHelper(Context context){
        super(context,DbVisit.DATABASE_NAME,null,DATABASE_VERSION);
        Log.d("facecheck","DbHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("facecheck","onCreate");
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void saveToLocalDatabase(String name,int sync_status, SQLiteDatabase database){

        Log.d("facecheck","saveToLocalDatabase = "+name+" - "+sync_status);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbVisit.NAME,name);
        contentValues.put(DbVisit.SYNC_STATUS,sync_status);
        database.insert(DbVisit.TABLE_NAME,null,contentValues);
        Log.d("facecheck","insert = "+DbVisit.TABLE_NAME);
    }

    public Cursor readFromLocalDatabase(SQLiteDatabase database){
        String[] projection = {DbVisit.NAME,DbVisit.SYNC_STATUS};
        return (database.query(DbVisit.TABLE_NAME,projection,null,null,null,null,null));
    }

    public void UpdateLocalDatabase(String name,int sync_status, SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbVisit.SYNC_STATUS,sync_status);
        String selection = DbVisit.NAME+" LIKE ?";
        String[] selection_args = {name};
        database.update(DbVisit.TABLE_NAME,contentValues,selection,selection_args);
    }

}
