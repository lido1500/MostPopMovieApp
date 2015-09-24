package com.example.walidelshafai.udacity_google_mcit_popmoviesapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.adapters.GridMoviePosterAdapter;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.adapters.GridMoviePosterCursorAdapter;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.data.MovieContract;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Movie> mMovies;
    private GridMoviePosterAdapter mAdapter;
    private GridMoviePosterCursorAdapter mCursorAdapter;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    private final String PREF_SORT = "pref_sort";
    private final String PREF_SEARCH = "pref_search";
    private int mPrefSort;
    private String mPrefSearchPopular = "popularity.desc";
    private String mPrefSearchHRated = "vote_average.desc";
    private String mPrefSearchHRevenue = "revenue.desc";
    public String searchParam;
    private Bundle savedState = null;
    private boolean mRestored = false;
    private boolean isConnected;
    private boolean mTwoPane;
    private boolean mFirstOpen;
    private final int MSG_INITIAL_OPEN = 5;
    private static final int MOVIES_LOADER = 0;
    private static final String MOVIEDETAILFRAGMENT_TAG = "MDFTAG";
    public Fragment fragment = this;
    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_GENRES,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_SORT,
            MovieContract.MovieEntry.COLUMN_DATE_ADDED
    };

    public interface Callback {
        public void onItemSelected(int movieId, boolean preLoaded);
    }

    public void setIfTwoPane(boolean twoPane) {
        this.mTwoPane = twoPane;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) fragmentView.findViewById(R.id.gridview);
        mCursorAdapter = new GridMoviePosterCursorAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mCursorAdapter);

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Cursor mCursor = mCursorAdapter.getCursor();
                if (mCursor.moveToPosition(position)) {
                    mGridView.setSelection(position);
                    int movieId = mCursor.getInt(mCursor.getColumnIndex(MovieContract.MovieEntry._ID));
                    if (movieId != 0) {
                        String backdrop = mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH));
                        boolean preLoaded = true;
                        if (backdrop == null || backdrop.equals("") || backdrop.equals("null")) {
                            preLoaded = false;
                        }
                        ((Callback) getActivity())
                                .onItemSelected(movieId, preLoaded);
                    }
                }

            }
        });

        setHasOptionsMenu(true);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mLastFirstVisibleItem = 0;
            ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(!mTwoPane) {
                    if (view.getId() == mGridView.getId()) {
                        final int currentFirstVisibleItem = mGridView.getFirstVisiblePosition();

                        if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                            mActionBar.hide();
                        } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                            mActionBar.show();
                        }

                        mLastFirstVisibleItem = currentFirstVisibleItem;
                    }
                }
            }
        });
        ActionBar mActionBar;
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected) {
            Toast.makeText(getActivity(), "No internet connection available, running in offline mode!", Toast.LENGTH_LONG).show();
        }
        mCursorAdapter.setOfflineMode(isConnected);
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefSort = Utils.getSortSection(getActivity());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        searchParam = sharedPref.getString(PREF_SEARCH, mPrefSearchPopular);
        if(savedInstanceState != null && savedInstanceState.containsKey("movies")) {
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            mCursorAdapter = new GridMoviePosterCursorAdapter(getActivity(), null, 0);
            mRestored = true;
        }
        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle("movies");
            mFirstOpen = true;
        }
        if(savedState != null) {
            mMovies = savedState.getParcelableArrayList("movies");
            mCursorAdapter = new GridMoviePosterCursorAdapter(getActivity(), null, 0);
        } else {
            if(mRestored == false) {
                if(Utils.checkIfSectionDataIsCurrent(getActivity())) {

                } else {
                    if(mPrefSort != 20) {
                        MovieAsyncTask task = new MovieAsyncTask();
                        task.execute();
                    }
                }
            }
        }

        mRestored = false;
        savedState = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveState();
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        state.putParcelableArrayList("movies", mMovies);
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mMovies != null && !mMovies.isEmpty()) {
            outState.putBundle("movies", (savedState != null) ? savedState : saveState());
            outState.putParcelableArrayList("movies", mMovies);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if(mPrefSort == 1) {
            mActionBar.setTitle(R.string.most_popular);
        } else if (mPrefSort == 5) {
            mActionBar.setTitle(R.string.highest_rated);
        } else if(mPrefSort == 11) {
            mActionBar.setTitle(R.string.highest_revenue);
        } else if(mPrefSort == 20) {
            mActionBar.setTitle(R.string.favorites);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_sort, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        mPrefSort = Utils.getSortSection(getActivity());

        MenuItem sort_hr = menu.findItem(R.id.action_sort_highest_rated);
        MenuItem sort_p = menu.findItem(R.id.action_sort_popular);
        MenuItem sort_rev = menu.findItem(R.id.action_sort_highest_revenue);
        MenuItem sort_fav = menu.findItem(R.id.action_sort_favorite);

        if(mPrefSort == 1) {
            sort_p.setVisible(false);
            sort_rev.setVisible(true);
            sort_hr.setVisible(true);
            sort_fav.setVisible(true);
        } else if (mPrefSort == 5) {
            sort_p.setVisible(true);
            sort_rev.setVisible(true);
            sort_hr.setVisible(false);
            sort_fav.setVisible(true);
        } else if (mPrefSort == 11) {
            sort_p.setVisible(true);
            sort_hr.setVisible(true);
            sort_rev.setVisible(false);
            sort_fav.setVisible(true);
        } else if(mPrefSort == 20) {
            sort_p.setVisible(true);
            sort_hr.setVisible(true);
            sort_rev.setVisible(true);
            sort_fav.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Utils.setSortSection(getActivity(), id);
        mFirstOpen = false;

        if (id == R.id.action_sort_popular) {
            searchParam = mPrefSearchPopular;
            mPrefSort = 1;
            if(Utils.checkIfSectionDataIsCurrent(getActivity())) {
                onSortChanged();
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
            return true;
        } else if(id == R.id.action_sort_highest_rated) {
            searchParam = mPrefSearchHRated;
            mPrefSort = 5;
            if(Utils.checkIfSectionDataIsCurrent(getActivity())) {
                onSortChanged();
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
            return true;
        } else if(id == R.id.action_sort_highest_revenue) {
            searchParam = mPrefSearchHRevenue;
            mPrefSort = 11;
            if(Utils.checkIfSectionDataIsCurrent(getActivity())) {
                onSortChanged();
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
            return true;
        } else if(id == R.id.action_sort_favorite) {
            searchParam = mPrefSearchHRevenue;
            mPrefSort = 20;
            onSortChanged();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    void onSortChanged( ) {
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if(mPrefSort == 1) {
            mActionBar.setTitle(R.string.most_popular);
        } else if (mPrefSort == 5) {
            mActionBar.setTitle(R.string.highest_rated);
        } else if(mPrefSort == 11) {
            mActionBar.setTitle(R.string.highest_revenue);
        } else if(mPrefSort == 20) {
            mActionBar.setTitle(R.string.favorites);
        }
        if(mTwoPane) {
            clearDetails();
        }
    }

    private void clearDetails() {
        MovieDetailFragment mdf = (MovieDetailFragment)getActivity().getSupportFragmentManager().findFragmentByTag(MOVIEDETAILFRAGMENT_TAG);
        getActivity().getSupportFragmentManager().beginTransaction()
                .detach(mdf)
                .commit();
    }

    private void initialClick() {
        if(mTwoPane) {
            if(!mFirstOpen) {
                mGridView.performItemClick(mGridView.getChildAt(0), 0, mCursorAdapter.getItemId(0));
                mFirstOpen = true;
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_INITIAL_OPEN) {
                initialClick();
            }
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = null;
        int sortSection = Utils.getSortSection(getActivity());
        Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(sortSection);
        return new CursorLoader(getActivity(),
                moviesSortedUri,
                MOVIES_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

        if(!mFirstOpen) {
            handler.sendEmptyMessage(MSG_INITIAL_OPEN);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    public class MovieAsyncTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

        private final String API_KEY = "f2e34a604f20735f13ab81ffa775c643";
        private final String LOG_TAG = MainActivity.class.getSimpleName();
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("Most Popular Movies");
            mProgressDialog.setMessage("Populating Movie List..!!");
            mProgressDialog.setIcon(R.drawable.cinema);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {
            Uri.Builder builder = new Uri.Builder();
            if(searchParam == mPrefSearchHRated) {
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", searchParam)
                        .appendQueryParameter("api_key", API_KEY)
                        .appendQueryParameter("vote_count.gte", "80");
            } else {
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", searchParam)
                        .appendQueryParameter("api_key", API_KEY);
            }
            InputStream stream = null;
            try {
                URL url = new URL(builder.build().toString());

                Log.d(LOG_TAG, "The URL is: " + url.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json");
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(LOG_TAG, "The response code is: " + responseCode + " " + conn.getResponseMessage());

                stream = conn.getInputStream();

                Reader reader = null;
                reader = new InputStreamReader(stream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                String json = bufferedReader.readLine();

                return(parseJson(json));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException ie) {
                ie.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            mMovies = movies;
            ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            if(mPrefSort == 1) {
                mActionBar.setTitle(R.string.most_popular);
            } else if (mPrefSort == 5) {
                mActionBar.setTitle(R.string.highest_rated);
            } else if (mPrefSort == 11) {
                mActionBar.setTitle(R.string.highest_revenue);
            }
            mProgressDialog.dismiss();
            onSortChanged();
        }

        private ArrayList<Movie> parseJson(String stream) {

            String stringFromStream = stream;
            ArrayList<Movie> results = new ArrayList<Movie>();
            try {
                JSONObject jsonObject = new JSONObject(stringFromStream);
                JSONArray array = (JSONArray) jsonObject.get("results");
                Utils.cleanUpOldData(getActivity());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonMovieObject = array.getJSONObject(i);
                    Movie movie = new Movie(
                            Integer.parseInt(jsonMovieObject.getString("id")),              //id
                            jsonMovieObject.getString("original_title"),                    //title
                            jsonMovieObject.getString("release_date"),                      //release_date
                            jsonMovieObject.getString("overview"),                          //overview
                            jsonMovieObject.getString("poster_path"),                       //poster_path
                            Double.parseDouble(jsonMovieObject.getString("vote_average"))   //vote_average
                    );
                    JSONArray genres = (JSONArray) jsonMovieObject.get("genre_ids");
                    StringBuilder strGenre = new StringBuilder();
                    ArrayList<Integer> genre_ids = new ArrayList<Integer>();
                    for (int x = 0; x < genres.length(); x++) {
                        genre_ids.add(genres.getInt(x));
                        strGenre.append(genres.getInt(x) + "");
                        if(x < (genres.length() - 1)) {
                            strGenre.append(",");
                        }
                    }
                    movie.setGenres(strGenre.toString());
                    results.add(movie);
                    Log.d(LOG_TAG, "Added movie: " + movie.getTitle());
                    Cursor curMov = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMovieUri(0, movie.getId()), null, null, null, null);
                    if(curMov.moveToFirst()) {
                        Time dayTime = new Time();
                        dayTime.setToNow();
                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
                        dayTime = new Time();
                        ContentValues cv = new ContentValues();
                        int sort = curMov.getInt(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_SORT));
                        int addSort = Utils.getSortSection(getActivity());
                        sort = Utils.getUpdatedMovieSortValue(addSort, sort);
                        cv.put(MovieContract.MovieEntry.COLUMN_SORT, sort);
                        cv.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));
                        getActivity().getContentResolver().update(MovieContract.MovieEntry.buildMovieUri(0, movie.getId()), cv, MovieContract.MovieEntry._ID + " = ?", new String[] { movie.getId() + ""});
                    } else {
                        Time dayTime = new Time();
                        dayTime.setToNow();
                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
                        dayTime = new Time();
                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry._ID, movie.getId());
                        cv.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                        cv.put(MovieContract.MovieEntry.COLUMN_GENRES, strGenre.toString());
                        cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                        cv.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                        cv.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                        cv.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                        cv.put(MovieContract.MovieEntry.COLUMN_SORT, Utils.getSortSection(getActivity()));
                        cv.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));
                        getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,cv);
                    }
                }
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(LOG_TAG, "Error parsing JSON. String was: " + stringFromStream);
            }
            return results;
        }
    }


}
