package com.sara.popularmovies.PopularMoviesAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sara.popularmovies.Model.MovieReview.Review;
import com.sara.popularmovies.R;

import java.util.ArrayList;
public class ReviewsAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Review> reviews;

    public ReviewsAdapter(Context context, ArrayList<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Object getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View row=layoutInflater.inflate(R.layout.list_item_reviews, parent, false);

        Review review=reviews.get(position);
        TextView ReviewAuthor=(TextView)row.findViewById(R.id.txt_ReviewAuthor);
        ReviewAuthor.setText(review.getAuthor());
        TextView ReviewContent=(TextView)row.findViewById(R.id.txt_ReviewContent);
        ReviewContent.setText(review.getContent());

        return row;
    }
}

