package com.example.walidelshafai.udacity_google_mcit_popmoviesapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;
    static final String DATABASE_NAME = "movies.db";
    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " REAL NOT NULL," +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                MovieContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT," +
                MovieContract.MovieEntry.COLUMN_GENRES + " TEXT," +
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                MovieContract.MovieEntry.COLUMN_BACKDROP_PATH + " TEXT," +
                MovieContract.MovieEntry.COLUMN_TAGLINE + " TEXT," +
                MovieContract.MovieEntry.COLUMN_RUNTIME + " REAL," +
                MovieContract.MovieEntry.COLUMN_SORT + " INTEGER, " +
                MovieContract.MovieEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        final String SQL_CREATE_CAST_TABLE = "CREATE TABLE " + MovieContract.CastEntry.TABLE_NAME + " (" +
                MovieContract.CastEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.CastEntry.COLUMN_CAST_ID + " INTEGER NOT NULL, " +
                MovieContract.CastEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieContract.CastEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MovieContract.CastEntry.COLUMN_CHARACTER + " TEXT, " +
                MovieContract.CastEntry.COLUMN_PROFILE_PATH + " TEXT, " +
                MovieContract.CastEntry.COLUMN_ORDER + " INTEGER, " +
                MovieContract.CastEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + MovieContract.TrailerEntry.TABLE_NAME + " (" +
                MovieContract.TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieContract.TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MovieContract.TrailerEntry.COLUMN_SOURCE + " TEXT NOT NULL, " +
                MovieContract.TrailerEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME + " (" +
                MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.ReviewEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                MovieContract.ReviewEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_CAST_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.CastEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
