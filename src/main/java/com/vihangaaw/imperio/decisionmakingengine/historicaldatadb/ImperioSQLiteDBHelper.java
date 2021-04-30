package com.vihangaaw.imperio.decisionmakingengine.historicaldatadb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ImperioSQLiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "imperio_historical_data";
    public static final String HISTORICAL_DATA_TABLE_NAME_LOCAL = "historical_data_local";
    public static final String HISTORICAL_DATA_COLUMN_TASK_ID_LOCAL = "task_id";
    public static final String HISTORICAL_DATA_COLUMN_AVERAGE_TIME_LOCAL = "time_local";
    public static final String HISTORICAL_DATA_TABLE_NAME_OFFLOAD = "historical_data_offload";
    public static final String HISTORICAL_DATA_COLUMN_TASK_ID_OFFLOAD = "task_id";
    public static final String HISTORICAL_DATA_COLUMN_AVERAGE_TIME_OFFLOAD = "time_offload";

    public ImperioSQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase DB = this.getWritableDatabase();
        this.onCreate(DB);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + HISTORICAL_DATA_TABLE_NAME_LOCAL + " (" +
                HISTORICAL_DATA_COLUMN_TASK_ID_LOCAL + " VARCHAR(100), " +
                HISTORICAL_DATA_COLUMN_AVERAGE_TIME_LOCAL + " INT" + ")");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + HISTORICAL_DATA_TABLE_NAME_OFFLOAD + " (" +
                HISTORICAL_DATA_COLUMN_TASK_ID_OFFLOAD + " VARCHAR(100), " +
                HISTORICAL_DATA_COLUMN_AVERAGE_TIME_OFFLOAD + " INT" + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HISTORICAL_DATA_TABLE_NAME_OFFLOAD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HISTORICAL_DATA_TABLE_NAME_LOCAL);
        onCreate(sqLiteDatabase);
    }

    /**
     * Returns all the records related to local for a given task id
     *
     * @param  taskId  String Task ID
     * @return Cursor
     */
    public Cursor getTaskLocal (String taskId)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select * from historical_data_local where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        return cursor;
    }

    /**
     * Returns all the records related to offload for a given task id
     *
     * @param  taskId  String Task ID
     * @return Cursor
     */
    public Cursor getTaskOffload (String taskId)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select * from historical_data_offload where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        return cursor;
    }


    /**
     * Add local execution time
     *
     * @param  taskId  String Task ID
     * @param  timeLocal  long execution time
     * @param  localMac  double mean absolute deviation of the local execution time
     * @return boolean      returns true if the insertion was successful
     */
    public Boolean AddExecutionTimeLocal(String taskId, long timeLocal, double localMac){
        SQLiteDatabase DB = this.getWritableDatabase();
        int executeCount = (getTaskLocal(taskId)).getCount();
        if(executeCount>10){
            if(Math.abs(timeLocal-getLocalAverage(taskId))<=localMac){
                ContentValues contentValues = new ContentValues();
                contentValues.put("task_id", taskId);
                contentValues.put("time_local", timeLocal);
                long result= DB.insert("historical_data_local", null, contentValues);
                if(result==-1){
                    return false;
                }else{
                    return true;
                }
            }
            return true;
        }
        else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("task_id", taskId);
            contentValues.put("time_local", timeLocal);
            long result= DB.insert("historical_data_local", null, contentValues);
            if(result==-1){
                return false;
            }else{
                return true;
            }
        }


    }


    /**
     * Add offload execution time
     *
     * @param  taskId  String Task ID
     * @param  timeOffload  long execution time
     * @param  offloadMac  double mean absolute deviation of the offload execution time
     * @return boolean      returns true if the insertion was successful
     */
    public Boolean AddExecutionTimeOffload(String taskId, long timeOffload, double offloadMac){
        SQLiteDatabase DB = this.getWritableDatabase();
        int executeCount = (getTaskOffload(taskId)).getCount();
        if(executeCount>10){
            if(Math.abs(timeOffload-getOffloadAverage(taskId))<=offloadMac){
                ContentValues contentValues = new ContentValues();
                contentValues.put("task_id", taskId);
                contentValues.put("time_offload", timeOffload);
                long result=DB.insert("historical_data_offload", null, contentValues);
                if(result==-1){
                    return false;
                }else{
                    return true;
                }
            }
            return true;
        }
        else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("task_id", taskId);
            contentValues.put("time_offload", timeOffload);
            long result=DB.insert("historical_data_offload", null, contentValues);
            if(result==-1){
                return false;
            }else{
                return true;
            }
        }
    }

    /**
     * Reset al the offload and local execution times in the database
     *
     * @param  taskId  String Task ID
     * @return void
     */
    public void resetAverageTime(String taskId){
        SQLiteDatabase DB = this.getWritableDatabase();
        long resultLocal = DB.delete("historical_data_local", "task_id=?", new String[]{taskId});
        long resultOffload = DB.delete("historical_data_offload", "task_id=?", new String[]{taskId});

    }

    /**
     * Returns average of the local execution time
     *
     * @param  taskId  String Task ID
     * @return double      returns average of the local execution time
     */
    public double getLocalAverage(String taskId){
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select time_local from historical_data_local where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        double avgLocalExecution = Double.parseDouble(cursor.getString(0));
        return avgLocalExecution;
    }

    /**
     * Returns average of the offload execution time
     *
     * @param  taskId  String Task ID
     * @return double      returns average of the offload execution time
     */
    public double getOffloadAverage(String taskId){
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select AVG(time_offload) from historical_data_offload where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        double avgOffloadExecution = Double.parseDouble(cursor.getString(0));
        return avgOffloadExecution;
    }

    /**
     * Returns mean absolute deviation of the local execution time
     *
     * @param  taskId  String Task ID
     * @return double      returns mean absolute deviation of the local execution time
     */
    public double getLocalMad(String taskId){
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select AVG(time_local) from historical_data_local where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        double avgLocalExecution = Double.parseDouble(cursor.getString(0));
        String queryToMad = "SELECT AVG(ABS(?-time_local)) FROM historical_data_local WHERE task_id = ?";
        Cursor cursorToMad = DB.rawQuery(queryToMad, new String[]{String.valueOf(avgLocalExecution), taskId});
        if (cursorToMad != null) {
            cursorToMad.moveToFirst();
        }
        return Double.parseDouble(cursorToMad.getString(0));
    }

    /**
     * Returns mean absolute deviation of the offload execution time
     *
     * @param  taskId  String Task ID
     * @return double      returns mean absolute deviation of the offload execution time
     */
    public double getOffloadMad(String taskId){
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select AVG(time_offload) from historical_data_offload where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        double avgLocalExecution = Double.parseDouble(cursor.getString(0));
        String queryToMad = "SELECT AVG(ABS(?-time_offload)) FROM historical_data_offload WHERE task_id = ?";
        Cursor cursorToMad = DB.rawQuery(queryToMad, new String[]{String.valueOf(avgLocalExecution), taskId});
        if (cursorToMad != null) {
            cursorToMad.moveToFirst();
        }
        return Double.parseDouble(cursorToMad.getString(0));
    }
}
