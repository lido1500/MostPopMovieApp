package com.example.walidelshafai.udacity_google_mcit_popmoviesapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.Utils;

public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;
    static final int MOVIES = 100;
    static final int MOVIES_SORTED = 101;
    static final int MOVIE_WITH_ID = 105;
    static final int CAST_WITH_ID = 200;
    static final int CAST_FOR_MOVIE = 201;
    static final int TRAILER_WITH_URL = 300;
    static final int TRAILERS_FOR_MOVIE = 301;
    static final int REVIEWS_WITH_ID = 400;
    static final int REVIEWS_FOR_MOVIE = 401;
    private static final String sMovieSortSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_SORT + " IN ? ";
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry._ID + " = ? ";
    private static final String sCastIdSelection =
            MovieContract.CastEntry.COLUMN_MOVIE_ID + " = ? " +
            "AND " + MovieContract.CastEntry.COLUMN_CAST_ID + " = ? ";
    private static final String sCastMovieSelection =
            MovieContract.CastEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String sTrailerIdSelection =
            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? " +
                    "AND " + MovieContract.TrailerEntry.COLUMN_SOURCE + " = ? ";
    private static final String sTrailerMovieSelection =
            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String sReviewIdSelection =
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? " +
                    "AND " + MovieContract.ReviewEntry.COLUMN_REVIEW_ID + " = ? ";
    private static final String sReviewMovieSelection =
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIES_SORTED);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_CAST + "/#", CAST_FOR_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_CAST + "/#/#", CAST_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILERS + "/#", TRAILERS_FOR_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_TRAILERS + "/#/*", TRAILER_WITH_URL);
        matcher.addURI(authority, MovieContract.PATH_REVIEWS + "/#", REVIEWS_FOR_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_REVIEWS + "/#/*", REVIEWS_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIES_SORTED: {
                retCursor = getMoviesBySortOrder(uri, projection, sortOrder);
                break;
            }
            case MOVIE_WITH_ID: {
                retCursor = getMovieWithId(uri, projection, sortOrder);
                break;
            }
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                    );
                break;
            }
            case CAST_WITH_ID: {
                retCursor = getCastWithId(uri, projection, sortOrder);
                break;
            }
            case CAST_FOR_MOVIE: {
                retCursor = getCastForMovie(uri, projection, sortOrder);
                break;
            }
            case TRAILER_WITH_URL: {
                retCursor = getTrailerWithURL(uri, projection, sortOrder);
                break;
            }
            case TRAILERS_FOR_MOVIE: {
                retCursor = getTrailerForMovie(uri, projection, sortOrder);
                break;
            }
            case REVIEWS_WITH_ID: {
                retCursor = getReviewWithId(uri, projection, sortOrder);
                break;
            }
            case REVIEWS_FOR_MOVIE: {
                retCursor = getReviewForMovie(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES_SORTED:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case CAST_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case CAST_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER_WITH_URL:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case REVIEWS_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case MOVIES: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(0, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CAST_WITH_ID: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.CastEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.CastEntry.buildCastUri(MovieContract.CastEntry.getMovieIdFromUri(uri), _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER_WITH_URL: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(MovieContract.TrailerEntry.getMovieIdFromUri(uri), MovieContract.TrailerEntry.getTrailerURLFromUri(uri));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS_WITH_ID: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(MovieContract.ReviewEntry.getMovieIdFromUri(uri), MovieContract.ReviewEntry.getReviewIdFromUri(uri));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if(null == selection) selection = "1";
        switch (match) {
            case MOVIES_SORTED: {
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        if(null == selection) selection = "1";
        switch (match) {
            case MOVIE_WITH_ID: {
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            int sortSection = Utils.getSortSection(getContext());
            Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(sortSection);
            getContext().getContentResolver().notifyChange(moviesSortedUri, null);
        }
        return rowsUpdated;
    }

    private void normalizeDate(ContentValues values) {
        if (values.containsKey(MovieContract.MovieEntry.COLUMN_DATE_ADDED)) {
            long dateValue = values.getAsLong(MovieContract.MovieEntry.COLUMN_DATE_ADDED);
            values.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, MovieContract.normalizeDate(dateValue));
        } else if (values.containsKey(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)) {
            long dateValue = values.getAsLong(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, MovieContract.normalizeDate(dateValue));
        }
    }

    private Cursor getMoviesBySortOrder (Uri uri, String[] projection, String sortOrder) {
        int sort = MovieContract.MovieEntry.getSortFromUri(uri);
        Utils utils = new Utils();
        String numQuest = MovieContract.MovieEntry.TABLE_NAME +
                "." + MovieContract.MovieEntry.COLUMN_SORT + " IN (?, ?, ?, ?, ?, ?, ?, ?)";
        String[] sortString = utils.getSortStringArray(sort);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                numQuest,
                sortString,
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getMovieWithId (Uri uri, String[] projection, String sortOrder) {
        int id = MovieContract.MovieEntry.getIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                sMovieIdSelection,
                new String[] {id + ""},
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getCastWithId (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.CastEntry.getMovieIdFromUri(uri);
        int castId = MovieContract.CastEntry.getCastIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.CastEntry.TABLE_NAME,
                projection,
                sCastIdSelection,
                new String[] {movieId + "", castId + ""},
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getCastForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.CastEntry.getMovieIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.CastEntry.TABLE_NAME,
                projection,
                sCastMovieSelection,
                new String[] {movieId + ""},
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getTrailerWithURL (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);
        String trailerURL = MovieContract.TrailerEntry.getTrailerURLFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME,
                projection,
                sTrailerIdSelection,
                new String[] {movieId + "", trailerURL + ""},
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getTrailerForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME,
                projection,
                sTrailerMovieSelection,
                new String[] {movieId + ""},
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getReviewWithId (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);
        String reviewId = MovieContract.ReviewEntry.getReviewIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME,
                projection,
                sReviewIdSelection,
                new String[] {movieId + "", reviewId + ""},
                null,
                null,
                sortOrder
            )
        );
    }

    private Cursor getReviewForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME,
                projection,
                sReviewMovieSelection,
                new String[] {movieId + ""},
                null,
                null,
                sortOrder
            )
        );
    }
}
