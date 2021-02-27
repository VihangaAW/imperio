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

    public Boolean insertTaskLocal(String taskId, int averageTimeLocal, int averageTimeOffload)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("task_id", taskId);
        contentValues.put("time_local", averageTimeLocal);
        long result=DB.insert("historical_data_local", null, contentValues);
        if(result==-1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean insertTaskOffload(String taskId, int averageTimeLocal, int averageTimeOffload)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("task_id", taskId);
        contentValues.put("time_offload", averageTimeOffload);
        long result=DB.insert("historical_data_offload", null, contentValues);
        if(result==-1){
            return false;
        }else{
            return true;
        }
    }



    public Boolean updateTask(String taskId, int averageTimeLocal, int taskExecutedCountLocal, int averageTimeOffload, int taskExecutedCountOffload) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("average_time_local", averageTimeLocal);
        contentValues.put("task_executed_count_local", taskExecutedCountLocal);
        contentValues.put("average_time_offload", averageTimeOffload);
        contentValues.put("task_executed_count_offload", taskExecutedCountOffload);
        Cursor cursor = DB.rawQuery("Select * from historical_data where task_id = ?", new String[]{taskId});
        if (cursor.getCount() > 0) {
            long result = DB.update("historical_data", contentValues, "task_id=?", new String[]{taskId});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }}


    public Boolean deleteTask (String taskId)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from historical_data where task_id = ?", new String[]{taskId});
        if (cursor.getCount() > 0) {
            long result = DB.delete("historical_data", "task_id=?", new String[]{taskId});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public Cursor getTaskLocal (String taskId)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select * from historical_data_local where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        return cursor;
    }

    public Cursor getTaskOffload (String taskId)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        String query = "Select * from historical_data_offload where task_id = ?";
        Cursor cursor = DB.rawQuery(query, new String[]{taskId});
        return cursor;
    }


    //updateAverageTimeLocal
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


    //updateAverageTimeOffload
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


    public void resetAverageTime(String taskId){
        SQLiteDatabase DB = this.getWritableDatabase();
        long resultLocal = DB.delete("historical_data_local", "task_id=?", new String[]{taskId});
        long resultOffload = DB.delete("historical_data_offload", "task_id=?", new String[]{taskId});

    }

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
