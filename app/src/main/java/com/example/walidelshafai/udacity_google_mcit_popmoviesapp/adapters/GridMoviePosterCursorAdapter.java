package com.example.walidelshafai.udacity_google_mcit_popmoviesapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.R;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.Utils;
import com.example.walidelshafai.udacity_google_mcit_popmoviesapp.data.MovieContract;

public class GridMoviePosterCursorAdapter extends CursorAdapter {

    private boolean isConnected;

    public static class ViewHolder {
        public final ImageView imageView;
        public final TextView txtTitle;
        public final TextView txtGenre;
        public final RatingBar rtbRating;
        public final LinearLayout linOverlay;
        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.gmp_image);
            txtTitle = (TextView) view.findViewById(R.id.gmp_title);
            txtGenre = (TextView) view.findViewById(R.id.gmp_genre);
            rtbRating = (RatingBar) view.findViewById(R.id.gmp_rating);
            linOverlay = (LinearLayout) view.findViewById(R.id.gmp_lin_overlay);
        }
    }

    public GridMoviePosterCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_movie_poster, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Utils util = new Utils();
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.linOverlay.setVisibility(View.GONE);
        String title = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
        viewHolder.txtTitle.setText(title);
        String imagePath = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
        Glide.with(context).load(util.constructMoviePosterURL(imagePath, 3)).into(viewHolder.imageView);
        String g = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_GENRES));
        if(!g.isEmpty() && !g.equals("null") && !g.equals("")) {
            String genres = util.getGenres(g);
            viewHolder.txtGenre.setText(genres);
        }
        if(!isConnected) {
            String backdrop = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH));
            if(backdrop == null || backdrop.equals("") || backdrop.equals("null")) {
                viewHolder.linOverlay.setVisibility(View.VISIBLE);
            }
        }
        double rating = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE));
        viewHolder.rtbRating.setRating((float) rating / 2);
    }
    public void setOfflineMode(boolean offline) {
        this.isConnected = offline;
    }
}
