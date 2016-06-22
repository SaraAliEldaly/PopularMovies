package com.sara.popularmovies.PopularMoviesAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sara.popularmovies.Model.MovieTrailer.Trailer;
import com.sara.popularmovies.R;

import java.util.ArrayList;
public class TrailersAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Trailer> trailers;

    public TrailersAdapter(Context context, ArrayList<Trailer> trailers) {
        this.context = context;
        this.trailers = trailers;
    }

    @Override
    public int getCount() {
        return trailers.size();
    }

    @Override
    public Object getItem(int position) {
        return trailers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row=layoutInflater.inflate(R.layout.list_item_trailers, parent, false);

        Trailer trailer = trailers.get(position);
        TextView TrailerNum=(TextView)row.findViewById(R.id.txt_TrailerNum);
        TrailerNum.setText(trailer.getName());
        return row;
    }
}
