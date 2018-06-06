package me.rishavagarwal.translate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class HistoryDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "translations";
    private static final String TABLE_HISTORY = "history";

    private static final String KEY_ID = "id";
    private static final String KEY_FROM_LANG = "from_lang";
    private static final String KEY_TO_LANG = "to_lang";
    private static final String KEY_FROM_TEXT = "from_text";
    private static final String KEY_TO_TEXT = "to_text";
    private static final String KEY_DATE = "date";

    public HistoryDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_FROM_LANG + " TEXT,"
                + KEY_TO_LANG + " TEXT,"
                + KEY_FROM_TEXT + " TEXT,"
                + KEY_TO_TEXT + " TEXT,"
                + KEY_DATE + " TEXT)";
        sqLiteDatabase.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);

        onCreate(sqLiteDatabase);
    }

    void addTranslation(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FROM_LANG, translation.getFrom_lang());
        values.put(KEY_TO_LANG, translation.getTo_lang());
        values.put(KEY_FROM_TEXT, translation.getFrom_text());
        values.put(KEY_TO_TEXT, translation.getTo_text());
        values.put(KEY_DATE, translation.getDate());

        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public Translation getTranslation(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HISTORY, new String[]{KEY_ID,
                        KEY_FROM_LANG, KEY_TO_LANG, KEY_FROM_TEXT, KEY_TO_TEXT, KEY_DATE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Translation translation = new Translation(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));

        return translation;
    }

    public List<Translation> getAllTranslations() {
        List<Translation> translationList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY + " ORDER BY " + KEY_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Translation translation = new Translation();
                translation.setId(Integer.parseInt(cursor.getString(0)));
                translation.setFrom_lang(cursor.getString(1));
                translation.setTo_lang(cursor.getString(2));
                translation.setFrom_text(cursor.getString(3));
                translation.setTo_text(cursor.getString(4));
                translation.setDate(cursor.getString(5));

                translationList.add(translation);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return translationList;
    }

    public int updateTranslation(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FROM_LANG, translation.getFrom_lang());
        values.put(KEY_TO_LANG, translation.getTo_lang());
        values.put(KEY_FROM_TEXT, translation.getFrom_text());
        values.put(KEY_TO_TEXT, translation.getTo_text());
        values.put(KEY_DATE, translation.getDate());

        return db.update(TABLE_HISTORY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(translation.getId())});
    }

    public void deleteTranslation(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, KEY_ID + " = ?",
                new String[]{String.valueOf(translation.getId())});
        db.close();
    }

    public int getTranslationsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HISTORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }
}