package com.sara.popularmovies.PopularMoviesAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sara.popularmovies.Model.MovieModel;
import com.sara.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PopularMoviesAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<MovieModel.Movie> movieModels;
    public PopularMoviesAdapter(Context context, ArrayList<MovieModel.Movie> movies) {
        this.context=context;
        this.movieModels= movies;
    }

    @Override
    public int getCount() {
        return movieModels.size();
    }

    @Override
    public Object getItem(int position) {
        return movieModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row=layoutInflater.inflate(R.layout.grid_item_popularmovies, parent, false);

        ImageView MovieImage=(ImageView)row.findViewById(R.id.grd_item_popularmovies_imageview);

        final MovieModel.Movie temp=movieModels.get(position);
        Picasso.with(context).load("http://image.tmdb.org/t/p/w185/"+temp.posterPath).placeholder(R.drawable.temp).into(MovieImage);

        return row;
    }
}