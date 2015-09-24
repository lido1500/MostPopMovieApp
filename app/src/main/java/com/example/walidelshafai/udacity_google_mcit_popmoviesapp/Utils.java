package com.example.walidelshafai.udacity_google_mcit_popmoviesapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.data.MovieContract;

public class Utils {

    private static final String PREF_SORT = "pref_sort";
    private static final String PREF_SEARCH = "pref_search";
    private static String mPrefSearchPopular = "popularity.desc";
    private static String mPrefSearchHRated = "vote_average.desc";
    private static String mPrefSearchHRevenue = "revenue.desc";

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_GENRES,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_SORT,
            MovieContract.MovieEntry.COLUMN_DATE_ADDED
    };

     public static boolean checkIfDatesAreFresh(long date) {
        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        dayTime = new Time();
        if (date <= dayTime.setJulianDay(julianStartDay - 1)) {
            return false;
        } else {
            return true;
        }
    }

    public String constructMoviePosterURL(String url, int imageSize) {
        String size;
        switch (imageSize) {
            case 0:
                size = "w92";
                break;
            case 1:
                size = "w154";
                break;
            case 2:
                size = "w185";
                break;
            case 3:
                size = "w342";
                break;
            case 4:
                size = "w500";
                break;
            case 5:
                size = "w780";
                break;
            case 6:
                size = "original";
                break;
            default:
                size = "w185";
                break;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath(size)
                .appendEncodedPath(url);
        return builder.build().toString();
    }

    public static String getSortString(int sort) {
        String sortString = "";
        switch (sort) {
            case 1:
                sortString = "1, 6, 12, 21, 17, 26, 32, 37";
                break;
            case 5:
                sortString = "5, 6, 16, 25, 17, 26, 36, 37";
                break;
            case 11:
                sortString = "11, 12, 16, 31, 17, 32, 36, 37";
                break;
            case 20:
                sortString = "20, 21, 25, 31, 26, 32, 36, 37";
                break;
        }
        return sortString;
    }

    public static int getUpdatedMovieSortValue(int currentSection, int currentRecordSortValue) {
        String[] sortString;

        switch (currentSection) {
            case 1:
                sortString = new String[]{"1", "6", "12", "21", "17", "26", "32", "37"};
                break;
            case 5:
                sortString = new String[]{"5", "6", "16", "25", "17", "26", "36", "37"};
                break;
            case 11:
                sortString = new String[]{"11", "12", "16", "31", "17", "32", "36", "37"};
                break;
            case 20:
                sortString = new String[]{"20", "21", "25", "31", "26", "32", "36", "37"};
                break;
            default:
                sortString = new String[]{};
        }
        for(int x = 0; x < sortString.length; x++) {
            if(currentRecordSortValue == Integer.parseInt(sortString[x])) {
                return currentRecordSortValue;
            }
        }
        return (currentRecordSortValue + currentSection);
    }

    public static String[] getSortStringArray(int sort) {
        String[] sortString;
        switch (sort) {
            case 1:
                sortString = new String[] { "1", "6", "12", "21", "17", "26", "32", "37" };
                break;
            case 5:
                sortString = new String[] { "5", "6", "16", "25", "17", "26", "36", "37" };
                break;
            case 11:
                sortString = new String[] { "11", "12", "16", "31", "17", "32", "36", "37" };
                break;
            case 20:
                sortString = new String[] { "20", "21", "25", "31", "26", "32", "36", "37" };
                break;
            default:
                sortString = new String[] {};
        }
        return sortString;
    }

    public String getGenres(String strGen) {
        StringBuilder genres;
        genres = new StringBuilder();
        String[] g = strGen.split(",");
        for(int x = 0; x < g.length; x++) {
            int genId = Integer.parseInt(g[x]);
            if(genres.length() != 0) {
                genres.append(", ");
            }
            switch (genId) {
                case 28:
                    genres.append("Action");
                    break;
                case 12:
                    genres.append("Adventure");
                    break;
                case 16:
                    genres.append("Animation");
                    break;
                case 35:
                    genres.append("Comedy");
                    break;
                case 80:
                    genres.append("Crime");
                    break;
                case 99:
                    genres.append("Documentary");
                    break;
                case 18:
                    genres.append("Drama");
                    break;
                case 10751:
                    genres.append("Family");
                    break;
                case 14:
                    genres.append("Fantasy");
                    break;
                case 10769:
                    genres.append("Foreign");
                    break;
                case 36:
                    genres.append("History");
                    break;
                case 27:
                    genres.append("Horror");
                    break;
                case 10402:
                    genres.append("Music");
                    break;
                case 9648:
                    genres.append("Mystery");
                    break;
                case 10749:
                    genres.append("Romance");
                    break;
                case 878:
                    genres.append("Science Fiction");
                    break;
                case 10770:
                    genres.append("TV Movie");
                    break;
                case 53:
                    genres.append("Thriller");
                    break;
                case 10752:
                    genres.append("War");
                    break;
                case 37:
                    genres.append("Western");
                    break;
                default:
                    break;
            }
        }
        return genres.toString();
    }

    public static int getSortSection(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(PREF_SORT, 1);
    }

    public static void setSortSection(Context context, int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (id == R.id.action_sort_popular) {
            editor.putInt(PREF_SORT, 1);
            editor.putString(PREF_SEARCH, mPrefSearchPopular);
            editor.commit();
        } else if(id == R.id.action_sort_highest_rated) {
            editor.putInt(PREF_SORT, 5);
            editor.putString(PREF_SEARCH, mPrefSearchHRated);
            editor.commit();
        } else if(id == R.id.action_sort_highest_revenue) {
            editor.putInt(PREF_SORT, 11);
            editor.putString(PREF_SEARCH, mPrefSearchHRevenue);
            editor.commit();
        } else if(id == R.id.action_sort_favorite) {
            editor.putInt(PREF_SORT, 20);
            editor.putString(PREF_SEARCH, "");
            editor.commit();
        }
    }

    public static boolean checkIfSectionDataIsCurrent(Context context) {
        String sortOrder = null;
        int sortSection = Utils.getSortSection(context);
        Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(sortSection);
        Cursor curMov = context.getContentResolver().query(moviesSortedUri, null, null, null, null);
        if(curMov.moveToFirst()) {
            while(curMov.moveToNext()) {
                long time = curMov.getLong(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE_ADDED));
                if(checkIfDatesAreFresh(time)) {

                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static boolean isRecordInFavorite(Context context, int sortNum) {
        String[] favArray = getSortStringArray(20);
        for(int x = 0; x < favArray.length; x++) {
            if(favArray[x].equals(sortNum + "")) {
                return true;
            }
        }
        return false;
    }

    public static void cleanUpOldData(Context context) {
        String sortOrder = null;
        int favSection = 20;
        Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(favSection);
        Cursor favCursor = context.getContentResolver().query(
                moviesSortedUri,
                MOVIES_COLUMNS,
                null,
                null,
                sortOrder);
        if(favCursor.moveToFirst()) {
            do {
                Time dayTime = new Time();
                dayTime.setToNow();

                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                dayTime = new Time();

                ContentValues cv = new ContentValues();

                int id = favCursor.getInt(favCursor.getColumnIndex(MovieContract.MovieEntry._ID));

                cv.put(MovieContract.MovieEntry.COLUMN_SORT, 20);
                cv.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));
                context.getContentResolver().update(MovieContract.MovieEntry.buildMovieUri(20, id), cv, MovieContract.MovieEntry._ID + " = ?", new String[] { id + ""});
            } while (favCursor.moveToNext());
        }
        Uri deleteURI = MovieContract.MovieEntry.buildMoviesSorted(Utils.getSortSection(context));
        context.getContentResolver().delete(deleteURI,
                MovieContract.MovieEntry.COLUMN_SORT + " IN ( ?, ?, ?, ?, ?, ?, ?, ? )",
                Utils.getSortStringArray(Utils.getSortSection(context)));
    }


}