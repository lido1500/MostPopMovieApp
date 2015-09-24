package com.example.walidelshafai.udacity_google_mcit_popmoviesapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MovieDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            int movieId = getIntent().getIntExtra("movieId", 0);
            Bundle arguments = new Bundle();
            arguments.putInt("movieId", movieId);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }
}
