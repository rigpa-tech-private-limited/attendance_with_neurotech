package co.vaango.attendance.multibiometric.multimodal;

import co.vaango.attendance.R;
import co.vaango.attendance.multibiometric.utils.BaseActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

public class VisitsActivity extends BaseActivity {
    RecyclerView recyclerView;
    EditText Name;
    RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    ArrayList<Visit> arrayList = new ArrayList<Visit>();

    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visits);
        recyclerView = (RecyclerView) findViewById(R.id.visits_list);
        Name = (EditText) findViewById(R.id.name);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerAdapter(arrayList);
        recyclerView.setAdapter(adapter);
        dbHelper = new DbHelper(this);
        readFromLocalStorage();
    }

    public void submitName(View view) {
        String name = Name.getText().toString();
        Log.d("facecheck","submitName = "+name);
        saveToLocalStorage(name);
        Name.setText("");
    }

    private void readFromLocalStorage() {

        Log.d("facecheck","readFromLocalStorage = ");
        arrayList.clear();
        dbHelper = new DbHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.readFromLocalDatabase(sqLiteDatabase);
        while ((cursor.moveToNext())) {
            String name = cursor.getString(cursor.getColumnIndex(DbVisit.NAME));
            int sync_status = cursor.getInt(cursor.getColumnIndex(DbVisit.SYNC_STATUS));
            Log.d("facecheck","cursor = "+name);
            arrayList.add(new Visit(name, sync_status));
        }
        Log.d("facecheck","arrayList = "+arrayList);
        adapter.notifyDataSetChanged();
        cursor.close();
        dbHelper.close();
    }

    private void saveToLocalStorage(String name){
        dbHelper = new DbHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        if(checkNetworkConnection()){

        } else {
            Log.d("facecheck","saveToLocalStorage = "+name+" - "+DbVisit.SYNC_STATUS_FAILED);
            dbHelper.saveToLocalDatabase(name,DbVisit.SYNC_STATUS_FAILED,sqLiteDatabase);
        }
        readFromLocalStorage();
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
