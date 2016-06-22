package com.sara.popularmovies;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sara.popularmovies.Model.MovieModel;
import com.sara.popularmovies.Model.MovieReview;
import com.sara.popularmovies.Model.MovieTrailer;
import com.sara.popularmovies.PopularMoviesAdapter.ReviewsAdapter;
import com.sara.popularmovies.PopularMoviesAdapter.TrailersAdapter;
import com.sara.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private TrailersAdapter trailersAdapter;
    public static final String ARG_MOVIE = "movie_arg";
    private ArrayList<MovieTrailer.Trailer> trailers;
    private ArrayList<MovieReview.Review>reviews;
    private ProgressBar progressBar;
    private ListView TrailersList;
    private LinearLayout linearLayout_Details;

    private MovieModel.Movie movie;
    private Integer MovieID;

    private TextView txt_RunTime;
    private ShareActionProvider mShareActionProvider;

    private ListView ReviewsList;
    private com.sara.popularmovies.PopularMoviesAdapter.ReviewsAdapter ReviewsAdapter;
    private int ReviewPage=1;
    private int ReviewTotalages=0;

    private static final int TRAILERS_LOADER=1;
    private static final int REVIEWS_LOADER=2;

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
    };

    private static final String[] TRAILERS_COLUMNS = {
            MovieContract.TrailerEntry.COLUMN_NAME,
            MovieContract.TrailerEntry.COLUMN_SOURCE,
    };

    private static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT
    };

    private static final int COL_TRAILER_NAME=0;
    private static final int COL_TRAILER_SOURCE=1;

    private static final int COL_REVIEW_AUTHOR=0;
    private static final int COL_REVIEW_CONTENT=1;

    private boolean listIsAtTop(ListView listView) {
        return listView.getChildCount() != 0 && listView.getChildAt(0).getTop() == 0;
    }
    public DetailFragment() {
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        final Activity activity= getActivity();
        trailers = new ArrayList<>();
        reviews=new ArrayList<>();
        progressBar = (ProgressBar) rootView.findViewById(R.id.progres);
        linearLayout_Details=(LinearLayout)rootView.findViewById(R.id.linear_Details);
        TrailersList=(ListView)rootView.findViewById(R.id.lst_Trailers);
        txt_RunTime=(TextView)rootView.findViewById(R.id.txt_RunTime);
        ToggleButton tog_Favorite = (ToggleButton) rootView.findViewById(R.id.tog_favorite);

        Bundle bundle=getArguments();
        if(bundle!=null) {
            movie = (MovieModel.Movie) bundle.getSerializable(ARG_MOVIE);
            MovieID = movie.id;
            ((TextView) rootView.findViewById(R.id.txt_MovieTitle))
                    .setText(movie.title);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.img_PopularMovie);
            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185/" + movie.posterPath).into(imageView);

            String[] dateParts=movie.releaseDate.split("-");

            ((TextView) rootView.findViewById(R.id.txt_ReleaseDate))
                    .setText(dateParts[0]);

            ((TextView) rootView.findViewById(R.id.txt_VoteAverage))
                    .setText((movie.voteAverage).toString()+"/10");

            ((TextView) rootView.findViewById(R.id.txt_MovieOverview))
                    .setText(movie.overview);
        }
        trailersAdapter = new TrailersAdapter(activity, trailers);
        TrailersList.setAdapter(trailersAdapter);
        TrailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MovieTrailer.Trailer trailer = (MovieTrailer.Trailer) trailersAdapter.getItem(position);
                Intent intent;
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailer.getKey()));
                    getActivity().startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                    getActivity().startActivity(intent);
                }
                startActivity(intent);
            }
        });

        ReviewsList=(ListView)rootView.findViewById(R.id.lst_Reviews);
        ReviewsAdapter=new ReviewsAdapter(activity,reviews);
        ReviewsList.setAdapter(ReviewsAdapter);

        ReviewsList.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }

        });

        ReviewsList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (listIsAtTop(ReviewsList) && scrollState == SCROLL_STATE_IDLE) {
                    ((ScrollView) view.getParent().getParent().getParent()).pageScroll(View.FOCUS_UP);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (ReviewPage < ReviewTotalages) {
                    ReviewPage++;
                    FetchMovieReviewsTask MovieReviewsTask = new FetchMovieReviewsTask();
                    MovieReviewsTask.execute(MovieID.toString(), Integer.toString(ReviewPage));
                }
            }
        });
    if(MovieID!=null) {
        FetchMovieRunTimeTask fetchMovieRunTimeTask = new FetchMovieRunTimeTask();
        fetchMovieRunTimeTask.execute(MovieID.toString());
        FetchMovieTrailersTask MovieTrailersTask = new FetchMovieTrailersTask();
        MovieTrailersTask.execute(MovieID.toString());

        FetchMovieReviewsTask MovieReviewsTask = new FetchMovieReviewsTask();
        MovieReviewsTask.execute(MovieID.toString(), "1");

        ContentResolver contentResolver = activity.getContentResolver();
        Cursor cursor = contentResolver. query(MovieContract.MovieEntry.buildMovieUri(MovieID),
                MOVIES_COLUMNS,
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            tog_Favorite.setChecked(true);
        } else {
            tog_Favorite.setChecked(false);
        }
    }
        tog_Favorite.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ContentResolver contentResolver = activity.getContentResolver();
                if (isChecked)
                {
                    ContentValues contentValues=new ContentValues();
                    contentValues.put(MovieContract.MovieEntry._ID,MovieID);
                    contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.title);
                    contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
                    contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.releaseDate);
                    contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.posterPath);
                    contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.voteAverage);
                    contentValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, txt_RunTime.getText().toString());

                    contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

                    contentValues.clear();
                    for (MovieTrailer.Trailer trailer : trailers){
                        contentValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID,MovieID);
                        contentValues.put(MovieContract.TrailerEntry.COLUMN_NAME, trailer.getName());
                        contentValues.put(MovieContract.TrailerEntry.COLUMN_SOURCE, trailer.getKey());
                        contentResolver.insert(MovieContract.TrailerEntry.CONTENT_URI, contentValues);
                        contentValues.clear();
                    }

                    for(MovieReview.Review review:reviews){
                        contentValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID,MovieID);
                        contentValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR,review.getAuthor());
                        contentValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT,review.getContent());
                        contentResolver.insert(MovieContract.ReviewEntry.CONTENT_URI, contentValues);
                        contentValues.clear();
                    }

                }else {
                    contentResolver.delete(MovieContract.MovieEntry.buildMovieUri(movie.id), null, null);
                }
            }
        });

        if(!((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)>= Configuration.SCREENLAYOUT_SIZE_LARGE)){
            Spinner spinner=(Spinner)activity.findViewById(R.id.spin_Sorting);
            spinner.setVisibility(View.GONE);
        }
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }
    private void createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + trailers.get(0).getKey());
        mShareActionProvider.setShareIntent(shareIntent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public class FetchMovieTrailersTask extends AsyncTask<String, Void, ArrayList<MovieTrailer.Trailer>> {

        private final String LOG_TAG = FetchMovieTrailersTask.class.getSimpleName();

        private ArrayList<MovieTrailer.Trailer> getMovieDetailsDataFromJson(String MovieDetailsJsonStr)
                throws JSONException {
            final String TMDB_RESULTS="results";
            final String TMDB_KEY="key";
            final String TMDB_NAME="name";
            final String TMDB_SITE="site";
            final String TMDB_Size="size";
            final String TMDB_type="type";

            JSONObject MovieDetailsJson = new JSONObject(MovieDetailsJsonStr);
            JSONArray MovieVideosArray = MovieDetailsJson.getJSONArray(TMDB_RESULTS);

            MovieTrailer movieTrailer = new MovieTrailer();

            for(int i = 0; i < MovieVideosArray.length(); i++) {
                MovieTrailer.Trailer trailer = new MovieTrailer().new Trailer();

                JSONObject VideoResult = MovieVideosArray.getJSONObject(i);
                trailer.setKey(VideoResult.getString(TMDB_KEY));
                trailer.setName(VideoResult.getString(TMDB_NAME));
                trailer.setSite(VideoResult.getString(TMDB_SITE));
                trailer.setSize(VideoResult.getInt(TMDB_Size));
                trailer.setType(VideoResult.getString(TMDB_type));
                trailers.add(trailer);
            }
            movieTrailer.setResults(trailers);
            return (ArrayList<MovieTrailer.Trailer>) movieTrailer.getResults();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<MovieTrailer.Trailer> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String MovieDetailsJsonStr;
            try {

                final String MovieDetail_BASE_URL="http://api.themoviedb.org/3/movie/";
                final String MovieDetail_URL="/videos?";
                final String APIKEY_PARAM="api_key";

                Uri builtUri = Uri.parse(MovieDetail_BASE_URL + params[0] + MovieDetail_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                MovieDetailsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieDetailsFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDetailsDataFromJson( MovieDetailsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList<MovieTrailer.Trailer> trailers) {
            progressBar.setVisibility(View.GONE);
            linearLayout_Details.setVisibility(View.VISIBLE);
            Log.d(LOG_TAG, trailers.toString());
            setListViewHeightBasedOnChildren(TrailersList);
            if(trailers!=null && trailers.size()!=0 && mShareActionProvider!=null){
                createShareMovieIntent();
            }
        }
    }

    public class FetchMovieReviewsTask extends AsyncTask<String, Void, MovieReview> {

        private final String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();

        private MovieReview getMovieReviewsDataFromJson(String MovieReviewsJsonStr)
                throws JSONException {
            final String TMDB_PAGE="page";
            final String TMDB_TotalPages="total_pages";
            final String TMDB_RESULTS="results";
            final String TMDB_ID="id";
            final String TMDB_AUTHOR="author";
            final String TMDB_CONTENT="content";
            final String TMDB_URL="url";

            JSONObject MovieReviewsJson = new JSONObject(MovieReviewsJsonStr);
            JSONArray MovieReviewsArray = MovieReviewsJson.getJSONArray(TMDB_RESULTS);

            MovieReview movieReview = new MovieReview();
            movieReview.setPage(MovieReviewsJson.getInt(TMDB_PAGE));
            movieReview.setTotalPages(MovieReviewsJson.getInt(TMDB_TotalPages));
            for(int i = 0; i <MovieReviewsArray.length(); i++) {
                MovieReview.Review review = new MovieReview().new Review();

                JSONObject reviewJson = MovieReviewsArray.getJSONObject(i);
                review.setId(reviewJson.getString(TMDB_ID));
                review.setAuthor(reviewJson.getString(TMDB_AUTHOR));
                review.setContent(reviewJson.getString(TMDB_CONTENT));
                review.setUrl(reviewJson.getString(TMDB_URL));

                reviews.add(review);
            }
            movieReview.setResults(reviews);
            return movieReview;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected MovieReview doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String MovieReviewsJsonStr;
            try {

                final String MovieReviws_BASE_URL="http://api.themoviedb.org/3/movie/";
                final String MovieReviews_URL="/reviews?";
                final String APIKEY_PARAM="api_key";
                final String PAGES_PARAM="page";

                Uri builtUri = Uri.parse(MovieReviws_BASE_URL + params[0] + MovieReviews_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .appendQueryParameter(PAGES_PARAM, params[1])
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                MovieReviewsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieReviewsFetchTask", "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieReviewsDataFromJson(MovieReviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(MovieReview movieReview) {
            progressBar.setVisibility(View.GONE);
            if(ReviewPage==1){
                ReviewPage=movieReview.getPage();
                ReviewTotalages=movieReview.getTotalPages();
            }
            ReviewsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(ReviewsList);
        }

    }

    public class FetchMovieRunTimeTask extends AsyncTask<String, Void, Integer> {

        private final String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();

        private Integer getMovieRunTimeFromJson(String MovieJsonStr)
                throws JSONException {
            final String TMDB_RunTime="runtime";

            JSONObject MovieJson = new JSONObject(MovieJsonStr);

            return MovieJson.getInt(TMDB_RunTime);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String MovieReviewsJsonStr;
            try {

                final String Movie_BASE_URL="http://api.themoviedb.org/3/movie/";
                final String Movie_URL="?";
                final String APIKEY_PARAM="api_key";

                Uri builtUri = Uri.parse(Movie_BASE_URL + params[0] + Movie_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                MovieReviewsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieRunTimeFetchTask", "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieRunTimeFromJson(MovieReviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Integer runtime) {
            if(runtime!=null)
                txt_RunTime.setText(Integer.toString(runtime)+"min");
        }

    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        switch (i){
            case(TRAILERS_LOADER):
                return new CursorLoader(getActivity(),
                        MovieContract.TrailerEntry.buildTrailerUriForMovie(MovieID),
                        TRAILERS_COLUMNS,
                        null,
                        null,
                        null);

            case(REVIEWS_LOADER):
                return new CursorLoader(getActivity(),
                        MovieContract.ReviewEntry.buildReviewUriForMovie(MovieID),
                        REVIEWS_COLUMNS,
                        null,
                        null,
                        null);

            default : return null ;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        int CursorID=cursorLoader.getId();
        switch (CursorID){
            case(TRAILERS_LOADER):
                if (cursor.moveToFirst())
                {
                    trailers.clear();
                    do{
                        MovieTrailer.Trailer trailer = new MovieTrailer().new Trailer();
                        trailer.setName(cursor.getString(COL_TRAILER_NAME));
                        trailer.setKey(cursor.getString(COL_TRAILER_SOURCE));
                        trailers.add(trailer);
                    }while (cursor.moveToNext());
                    if(trailers!=null && trailers.size()!=0){
                        createShareMovieIntent();
                    }
                }
                break;
            case(REVIEWS_LOADER):
                if (cursor.moveToFirst())
                {
                    reviews.clear();
                    do{
                        MovieReview.Review review=new MovieReview().new Review();
                        review.setAuthor(cursor.getString(COL_REVIEW_AUTHOR));
                        review.setContent(cursor.getString(COL_REVIEW_CONTENT));
                        reviews.add(review);
                    }while (cursor.moveToNext());

                }
                break;
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }



}