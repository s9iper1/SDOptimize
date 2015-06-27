package com.android.sd.optimize;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHandler {

	private SQLiteDatabase db = null;
    private String LOG_TAG = AppGlobals.getLogTag(getClass());

	public DBHandler(Context context) {
        //filename contains the data tobe uploaded
        db = context.openOrCreateDatabase("database.db", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists pending "
                + "(id integer primary key autoincrement, filename varchar , tobedeleted varchar );");
    }

	void createNewFileNameForUpload(String column,String item) {
		ContentValues values = new ContentValues();
		values.put(column, item);
		db.insert("pending", null, values);
	}

    void deleteUploadedFile(String column,String value) {
			db.delete("pending", column + " = ?", new String[]{value});
			Log.i(LOG_TAG, "Entry deleted");
		}

	ArrayList<String> retrieveDate(String column) {
		Cursor cursor;
		cursor = db.rawQuery("select * from pending", null);
		ArrayList<String> arrayList = new ArrayList<>();
		while (cursor.moveToNext()) {
			String itemname = cursor.getString(cursor.getColumnIndex(column));
				arrayList.add(itemname);
		}
		return arrayList;
	}
}