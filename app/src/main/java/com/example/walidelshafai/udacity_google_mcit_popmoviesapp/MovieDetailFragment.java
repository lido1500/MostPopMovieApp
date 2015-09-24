package com.example.walidelshafai.udacity_google_mcit_popmoviesapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.bumptech.glide.Glide;
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

public class MovieDetailFragment extends Fragment {

    private ArrayList<Movie> mMovies;
    public Movie mMovie;
    public int movieId;
    public TextView txtTitle, txtTagline, txtReleaseDate, txtRunTime, txtGenre, txtRating, txtOverview;
    public RatingBar ratingBar;
    public ImageView imgPoster, imgBackdrop;
    public ActionBar mActionBar;
    public LinearLayout linGenre, linDate, linLength, linCast, linTrail, linRev,linMasRev, linMasTrail;
    public ToggleButton togFav;
    public boolean isFav;
    public int movieSortVal;
    private boolean mTwoPane;
    private boolean isConnected;
    private ShareActionProvider mShareActionProvider;
    private String mShareMovieTitle;
    private String mShareTrailerURL;

    public void setIfTwoPane(boolean twoPane) {
        this.mTwoPane = twoPane;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if(getArguments() != null) {
            movieId = getArguments().getInt("movieId");
        }

        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.show();
        txtTitle = (TextView) fragmentView.findViewById(R.id.detail_txt_title);
        txtTagline = (TextView) fragmentView.findViewById(R.id.detail_txt_tagline);
        txtGenre = (TextView) fragmentView.findViewById(R.id.detail_txt_genre);
        txtRunTime = (TextView) fragmentView.findViewById(R.id.detail_txt_running_time);
        txtReleaseDate = (TextView) fragmentView.findViewById(R.id.detail_txt_release_date);
        txtRating = (TextView) fragmentView.findViewById(R.id.detail_txt_rating);
        txtOverview = (TextView) fragmentView.findViewById(R.id.detail_txt_overview);
        imgPoster = (ImageView) fragmentView.findViewById(R.id.detail_img_poster);
        imgBackdrop = (ImageView) fragmentView.findViewById(R.id.detail_img_backdrop);
        ratingBar = (RatingBar) fragmentView.findViewById(R.id.detail_rat_rating);
        linDate = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_date);
        linGenre = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_genre);
        linLength = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_duration);
        linCast = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_cast);
        linTrail = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_trailers);
        linRev = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_reviews);
        linMasRev = (LinearLayout) fragmentView.findViewById(R.id.detail_master_lin_rev);
        linMasTrail = (LinearLayout) fragmentView.findViewById(R.id.detail_master_lin_trail);
        togFav = (ToggleButton) fragmentView.findViewById(R.id.detail_tog_fav);
        setHasOptionsMenu(true);
        MainActivityFragment maf = (MainActivityFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_movies);
        if ( null != maf ) {
            setIfTwoPane(true);
        }
        if(!mTwoPane) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
        }

        togFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int favVal = 0;
                if(togFav.isChecked()) {
                    Toast.makeText(getActivity(), mMovie.getTitle() + " added to favorites!", Toast.LENGTH_SHORT).show();
                    isFav = true;
                    favVal = 20;
                } else {
                    Toast.makeText(getActivity(), mMovie.getTitle() + " removed from favorites!", Toast.LENGTH_SHORT).show();
                    isFav = false;
                    favVal = -20;
                }
                ContentValues cv = new ContentValues();
                Cursor curMov = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMovieUri(0, movieId), null, null, null, null);
                if(curMov.moveToFirst()) {
                    movieSortVal = curMov.getInt(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_SORT));
                    cv.put(MovieContract.MovieEntry.COLUMN_SORT, movieSortVal + favVal);
                    getActivity().getContentResolver().update(MovieContract.MovieEntry.buildMovieUri(0, mMovie.getId()), cv, MovieContract.MovieEntry._ID + " = ?", new String[]{mMovie.getId() + ""});
                    getActivity().getContentResolver().notifyChange(MovieContract.MovieEntry.buildMovieUri(0, mMovie.getId()), null);
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("movie", mMovie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(movieId != 0) {
            if (savedInstanceState == null || !savedInstanceState.containsKey("movie")) {
                Cursor curMov = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMovieUri(0, movieId), null, null, null, null);

                if (curMov.moveToFirst()) {
                    String backdrop = curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH));
                    if (backdrop != null && !backdrop.equals("null") && !backdrop.equals(""))
                        if (Utils.checkIfDatesAreFresh(curMov.getLong(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE_ADDED))) || !isConnected) {
                            Cursor curCast = getActivity().getContentResolver().query(MovieContract.CastEntry.buildCastUriForMovie(movieId), null, null, null, MovieContract.CastEntry.COLUMN_ORDER + " ASC");
                            Cursor curTrailer = getActivity().getContentResolver().query(MovieContract.TrailerEntry.buildTrailerUriForMovie(movieId), null, null, null, null);
                            Cursor curReviews = getActivity().getContentResolver().query(MovieContract.ReviewEntry.buildReviewUriForMovie(movieId), null, null, null, null);
                            Movie movie = new Movie(
                                    curMov.getInt(curMov.getColumnIndex(MovieContract.MovieEntry._ID)),
                                    curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)),
                                    curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)),
                                    curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)),
                                    curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)),
                                    curMov.getDouble(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE))
                            );

                            movieSortVal = curMov.getInt(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_SORT));
                            isFav = Utils.isRecordInFavorite(getActivity(), movieSortVal);
                            movie.setTagline(curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_TAGLINE)));
                            movie.setBackdrop(curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH)));
                            movie.setRunTime(curMov.getInt(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME)));
                            movie.setGenres(curMov.getString(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_GENRES)));
                            if (curCast.moveToFirst()) {
                                ArrayList<String[]> cast_det = new ArrayList<String[]>();
                                do {
                                    cast_det.add(new String[]{
                                            curCast.getString(curCast.getColumnIndex(MovieContract.CastEntry.COLUMN_CAST_ID)),
                                            curCast.getString(curCast.getColumnIndex(MovieContract.CastEntry.COLUMN_NAME)),
                                            curCast.getString(curCast.getColumnIndex(MovieContract.CastEntry.COLUMN_CHARACTER)),
                                            curCast.getString(curCast.getColumnIndex(MovieContract.CastEntry.COLUMN_PROFILE_PATH)),
                                            curCast.getString(curCast.getColumnIndex(MovieContract.CastEntry.COLUMN_ORDER)),
                                    });
                                } while (curCast.moveToNext());
                                movie.setCast(cast_det);
                            }
                            if (curTrailer.moveToFirst()) {
                                ArrayList<String[]> trailer_det = new ArrayList<String[]>();
                                do {
                                    trailer_det.add(new String[]{
                                            curTrailer.getString(curTrailer.getColumnIndex(MovieContract.TrailerEntry.COLUMN_NAME)),
                                            curTrailer.getString(curTrailer.getColumnIndex(MovieContract.TrailerEntry.COLUMN_SOURCE))
                                    });
                                } while (curTrailer.moveToNext());
                                movie.setTrailers(trailer_det);
                            }

                            if (curReviews.moveToFirst()) {
                                ArrayList<String[]> reviews_det = new ArrayList<String[]>();
                                do {
                                    reviews_det.add(new String[]{
                                            curReviews.getString(curReviews.getColumnIndex(MovieContract.ReviewEntry.COLUMN_REVIEW_ID)),
                                            curReviews.getString(curReviews.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR)),
                                            curReviews.getString(curReviews.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT))
                                    });
                                } while (curReviews.moveToNext());
                                movie.setReviews(reviews_det);
                            }
                            mMovie = movie;
                            updatePageInfo();
                        } else {
                            if(isConnected) {
                                MovieAsyncTask task = new MovieAsyncTask();
                                task.execute();
                            }
                        }
                    else {
                        if(isConnected) {
                            MovieAsyncTask task = new MovieAsyncTask();
                            task.execute();
                        }
                    }
                } else {
                    if(isConnected) {
                        MovieAsyncTask task = new MovieAsyncTask();
                        task.execute();
                    }
                }
            } else {
                mMovie = savedInstanceState.getParcelable("movie");
                if (mMovie != null) {
                    updatePageInfo();
                } else {
                    if(isConnected) {
                        MovieAsyncTask task = new MovieAsyncTask();
                        task.execute();
                    }
                }
            }
        }
    }

    public void updatePageInfo() {
        Utils utils = new Utils();
        txtTitle.setText(mMovie.getTitle());
        mShareMovieTitle = mMovie.getTitle();
        if(isFav) {
            togFav.setChecked(true);
        } else {
            togFav.setChecked(false);
        }
        if(mMovie.getGenres().isEmpty()) {
            linGenre.setVisibility(View.GONE);
        } else {
            txtGenre.setText(utils.getGenres(mMovie.getGenres()));
        }
        if(mMovie.getRunTime() == 0) {
            linLength.setVisibility(View.GONE);
        } else {
            txtRunTime.setText(mMovie.getRunTime() + " min");
        }
        if(mMovie.getReleaseDate().equals("") || mMovie.getReleaseDate() == null) {
            linDate.setVisibility(View.GONE);
        } else {
            txtReleaseDate.setText(mMovie.getReleaseDate());
        }
        Glide.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getPosterPath(), 3)).into(imgPoster);
        if(mMovie.getBackdrop().equals("") || mMovie.getBackdrop() == null || mMovie.getBackdrop().equals("null")) {
            imgBackdrop.setVisibility(View.GONE);
            txtTagline.setVisibility(View.GONE);
        } else {
            Glide.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getBackdrop(), 4)).into(imgBackdrop);
            if(!mMovie.getTagline().equals("")) {
                txtTagline.setText("\"" + mMovie.getTagline() + "\"");
            }
        }
        if(mMovie.getCast() == null || mMovie.getCast().isEmpty()) {
            linCast.setVisibility(View.GONE);
        } else {
            ArrayList<String[]> cast = mMovie.getCast();
            for (int x = 0; x < cast.size(); x++ ) {
                View card = getActivity().getLayoutInflater().inflate(R.layout.castcard, null);

                ImageView img = (ImageView) card.findViewById(R.id.castcard_img);
                TextView txtName = (TextView) card.findViewById(R.id.castcard_txt_name);
                TextView txtCharacter = (TextView) card.findViewById(R.id.castcard_txt_character);

                txtName.setText(cast.get(x)[1]);
                txtCharacter.setText(cast.get(x)[2]);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(3, 0, 3, 0);
                card.setLayoutParams(lp);
                linCast.addView(card);
                String sil = cast.get(x)[3];
                if(sil.isEmpty() || sil.equals("null")) {
                    Glide.with(getActivity()).load(R.drawable.silhouette).centerCrop().into(img);
                } else {
                    Glide.with(getActivity()).load(utils.constructMoviePosterURL(cast.get(x)[3], 3)).centerCrop().into(img);
                }

            }
        }

        if(mMovie.getTrailers() == null || mMovie.getTrailers().isEmpty()) {
            linMasTrail.setVisibility(View.GONE);
        } else {
            final ArrayList<String[]> trailers = mMovie.getTrailers();
            for(int p = 0; p < trailers.size(); p++) {
                View card = getActivity().getLayoutInflater().inflate(R.layout.trailercard, null);

                ImageView img = (ImageView) card.findViewById(R.id.trailercard_img);
                TextView txtName = (TextView) card.findViewById(R.id.trailercard_txt_name);

                txtName.setText(trailers.get(p)[0]);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(10, 0, 10, 0);
                card.setLayoutParams(lp);
                linTrail.addView(card);
                final String videoURL = trailers.get(p)[1];
                String trailerPoster = "http://img.youtube.com/vi/" + videoURL + "/0.jpg";
                Glide.with(getActivity()).load(trailerPoster).centerCrop().into(img);

                if(p == 0) {
                    mShareTrailerURL = Uri.parse("http://www.youtube.com/watch?v=" + videoURL).toString();
                }

                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoURL)));
                    }
                });

            }
        }
        if(mMovie.getReviews() == null || mMovie.getReviews().isEmpty()) {
            linMasRev.setVisibility(View.GONE);
        } else {
            final ArrayList<String[]> reviews = mMovie.getReviews();
            for(int r = 0; r < reviews.size(); r++) {
                View card = getActivity().getLayoutInflater().inflate(R.layout.reviewcard, null);

                TextView txtAuthor = (TextView) card.findViewById(R.id.reviewcard_author);
                TextView txtContent = (TextView) card.findViewById(R.id.reviewcard_content);

                txtAuthor.setText(reviews.get(r)[1]);
                txtContent.setText(reviews.get(r)[2]);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(10, 0, 10, 0);
                card.setLayoutParams(lp);
                linRev.addView(card);
            }
        }
        txtRating.setText(mMovie.getVoteAverage() + " / 10");
        ratingBar.setRating((float) mMovie.getVoteAverage());
        txtOverview.setText(mMovie.getOverview());
        mActionBar.setTitle(mMovie.getTitle());

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareMovieTitle != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private String mShareText = "Check out this trailer for the movie ";

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareText + mShareMovieTitle + "! " + mShareTrailerURL);
        return shareIntent;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }

        return false;
    }

    public class MovieAsyncTask extends AsyncTask<Void, Void, Movie> {

        private final String API_KEY = "f2e34a604f20735f13ab81ffa775c643";
        private final String LOG_TAG = MainActivity.class.getSimpleName();
        private ProgressDialog mProgressDialog;
        public Movie movieObj;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle("Most Popular Movies");
            mProgressDialog.setMessage("Retrieving Movie Information..!!");
            mProgressDialog.setIcon(R.drawable.cinema);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Movie doInBackground(Void... params) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(movieId + "")
                    .appendQueryParameter("api_key", API_KEY);

            InputStream stream = null;
            try {
                String completeUrl = builder.build().toString() + "&append_to_response=credits,reviews,trailers";
                URL url = new URL(completeUrl);

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

                movieObj = parseJson(json);
                return(movieObj);

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
        protected void onPostExecute(Movie movie) {
            super.onPostExecute(movie);

            mMovie = movie;

            updatePageInfo();

            mProgressDialog.dismiss();
        }

        private Movie parseJson(String stream) {

            String stringFromStream = stream;

            try {
                JSONObject jsonMovieObject = new JSONObject(stringFromStream);

                Movie movie = new Movie(
                        Integer.parseInt(jsonMovieObject.getString("id")),              //id
                        jsonMovieObject.getString("title"),                             //title
                        jsonMovieObject.getString("release_date"),                      //release_date
                        jsonMovieObject.getString("overview"),                          //overview
                        jsonMovieObject.getString("poster_path"),                       //poster_path
                        Double.parseDouble(jsonMovieObject.getString("vote_average"))   //vote_average
                );

                JSONArray genres = (JSONArray) jsonMovieObject.get("genres");
                StringBuilder genre_ids = new StringBuilder();
                for (int x = 0; x < genres.length(); x++) {
                    JSONObject jsonGenreObject = genres.getJSONObject(x);
                    genre_ids.append(Integer.parseInt(jsonGenreObject.getString("id")) + "");
                    if(x < (genres.length() - 1)) {
                        genre_ids.append(",");
                    }
                }
                movie.setGenres(genre_ids.toString());

                JSONObject credits = (JSONObject) jsonMovieObject.get("credits");
                JSONArray cast = (JSONArray) credits.get("cast");
                ArrayList<String[]> cast_det = new ArrayList<String[]>();
                for(int p = 0; p < cast.length(); p++) {
                    JSONObject jsonCastObject = cast.getJSONObject(p);
                    cast_det.add(new String[] { jsonCastObject.getString("cast_id"), jsonCastObject.getString("name"), jsonCastObject.getString("character"),
                            jsonCastObject.getString("profile_path"), jsonCastObject.getString("order") });

                    Cursor curCast = getActivity().getContentResolver().query(MovieContract.CastEntry.buildCastUri(movie.getId(), Integer.parseInt(cast_det.get(p)[0])), null,
                            null, null, MovieContract.CastEntry.COLUMN_ORDER + " ASC");

                    if(curCast.moveToFirst()) {

                    } else {
                        Time dayTime = new Time();
                        dayTime.setToNow();

                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                        dayTime = new Time();

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.CastEntry.COLUMN_MOVIE_ID, movie.getId());
                        cv.put(MovieContract.CastEntry.COLUMN_CAST_ID, cast_det.get(p)[0]);
                        cv.put(MovieContract.CastEntry.COLUMN_NAME, cast_det.get(p)[1]);
                        cv.put(MovieContract.CastEntry.COLUMN_CHARACTER, cast_det.get(p)[2]);
                        cv.put(MovieContract.CastEntry.COLUMN_PROFILE_PATH, cast_det.get(p)[3]);
                        cv.put(MovieContract.CastEntry.COLUMN_ORDER, cast_det.get(p)[4]);
                        cv.put(MovieContract.CastEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));

                        getActivity().getContentResolver().insert(MovieContract.CastEntry.buildCastUri(movie.getId(), Integer.parseInt(cast_det.get(p)[0])), cv);
                    }

                }
                movie.setCast(cast_det);

                JSONObject trailers = (JSONObject) jsonMovieObject.get("trailers");
                JSONArray youtube = (JSONArray) trailers.get("youtube");
                ArrayList<String[]> trailer_det = new ArrayList<String[]>();
                for(int v = 0; v < youtube.length(); v++) {
                    JSONObject jsonTrailerObject = youtube.getJSONObject(v);
                    trailer_det.add(new String[] { jsonTrailerObject.getString("name"), jsonTrailerObject.getString("source") });

                    Cursor curTrailer = getActivity().getContentResolver().query(MovieContract.TrailerEntry.buildTrailerUri(movie.getId(), trailer_det.get(v)[1]), null,
                            null, null, null);

                    if(curTrailer.moveToFirst()) {

                    } else {
                        Time dayTime = new Time();
                        dayTime.setToNow();

                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                        dayTime = new Time();

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movie.getId());
                        cv.put(MovieContract.TrailerEntry.COLUMN_NAME, trailer_det.get(v)[0]);
                        cv.put(MovieContract.TrailerEntry.COLUMN_SOURCE, trailer_det.get(v)[1]);
                        cv.put(MovieContract.TrailerEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));

                        getActivity().getContentResolver().insert(MovieContract.TrailerEntry.buildTrailerUri(movie.getId(), trailer_det.get(v)[1]), cv);
                    }
                }
                movie.setTrailers(trailer_det);

                JSONObject reviews = (JSONObject) jsonMovieObject.get("reviews");
                JSONArray results = (JSONArray) reviews.get("results");
                ArrayList<String[]> reviews_det = new ArrayList<String[]>();
                for(int r = 0; r < results.length(); r++) {
                    JSONObject jsonReviewObject = results.getJSONObject(r);
                    reviews_det.add(new String[] { jsonReviewObject.getString("id"), jsonReviewObject.getString("author"), jsonReviewObject.getString("content") });

                    Cursor curReviews = getActivity().getContentResolver().query(MovieContract.ReviewEntry.buildReviewUri(movie.getId(), reviews_det.get(r)[0]), null,
                            null, null, null);

                    if(curReviews.moveToFirst()) {

                    } else {
                        Time dayTime = new Time();
                        dayTime.setToNow();

                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                        dayTime = new Time();

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movie.getId());
                        cv.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, reviews_det.get(r)[0]);
                        cv.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, reviews_det.get(r)[1]);
                        cv.put(MovieContract.ReviewEntry.COLUMN_CONTENT, reviews_det.get(r)[2]);
                        cv.put(MovieContract.ReviewEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));

                        getActivity().getContentResolver().insert(MovieContract.ReviewEntry.buildReviewUri(movie.getId(), reviews_det.get(r)[1]), cv);
                    }
                }
                movie.setReviews(reviews_det);

                Cursor curMov = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMovieUri(0, movie.getId()), null, null, null, null);
                if(curMov.moveToFirst()) {
                    ContentValues cv = new ContentValues();

                    movie.setBackdrop(jsonMovieObject.getString("backdrop_path"));
                    movie.setTagline(jsonMovieObject.getString("tagline"));
                    String runtime = jsonMovieObject.getString("runtime");
                    if(!runtime.equals("null") && !runtime.equals("") && runtime != null) {
                        movie.setRunTime(Integer.parseInt(runtime));
                        cv.put(MovieContract.MovieEntry.COLUMN_RUNTIME, runtime);
                    }

                    cv.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, jsonMovieObject.getString("backdrop_path"));
                    cv.put(MovieContract.MovieEntry.COLUMN_TAGLINE, jsonMovieObject.getString("tagline"));
                    getActivity().getContentResolver().update(MovieContract.MovieEntry.buildMovieUri(0, movie.getId()), cv, MovieContract.MovieEntry._ID + " = ?", new String[]{movie.getId() + ""});
                }

                return movie;
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(LOG_TAG, "Error parsing JSON. String was: " + stringFromStream);
            }

            return null;
        }
    }
}
