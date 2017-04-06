package naveenpatidar.mindwaverecorder.databaseConnection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DataBaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "rawdata.db";
    private static final String TABLE_NAME = "dataset";
   // private static final String DATABASE_CREATE_SQL = "CREATE TABLE "+ TABLE_NAME + "( "+ "id integer primary key, firstname text, lastname text"+ ")";
   // private static final String DATA="user_data";
    private SQLiteDatabase db;
    private int dataArray[] = new int[500];
    int count = 0;
    private ArrayList<Integer> temp_data = new ArrayList<>();
    public DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null,1);
            db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
        //   _db.execSQL(DATABASE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
        // Destroy old database:
        // _db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Recreate new database:
        // onCreate(_db);
    }


    public void insertRawData(int id)
    {
        if(count < 1000)
        {
            count++;
            temp_data.add(id);
        }
        else
        {
            count = 0;
            insertData(temp_data);
            temp_data = new ArrayList<>();
        }
    }

    public void insertData(ArrayList<Integer> arrayList)
    {
/*        new Thread(new Runnable() {
            @Override
            public void run() {*/
                for(int a : arrayList)
                {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", a);
                    //db.beginTransaction();
                    db.insert(TABLE_NAME, null, contentValues);
                    //db.setTransactionSuccessful();
                    //db.endTransaction();
                }
           /* }
        });*/
    }


/*
    public ArrayList<String> getAllData()
    {
        Cursor res =  db.rawQuery( "select * from "+TABLE_NAME, null );
        res.moveToFirst();
        ArrayList<String> users = new ArrayList<>();

        while(!res.isAfterLast()){
            String id = res.getString(res.getColumnIndex("id"));
            String name = res.getString(res.getColumnIndex("firstname"));
            String email = res.getString(res.getColumnIndex("lastname"));
            users.add(id+" " + name + " "+ email);
            res.moveToNext();
        }
        return users;
    }
*/



    public boolean delete()
    {
        db.delete(TABLE_NAME, null, null);
        return true;
    }

    public int count()
    {
        Cursor result =db.rawQuery("select count(*) from "+TABLE_NAME,null);
        result.moveToFirst();
        return result.getInt(0);
    }
}
