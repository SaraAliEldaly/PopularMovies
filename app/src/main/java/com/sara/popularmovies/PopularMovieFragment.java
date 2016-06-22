package com.sara.popularmovies;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.sara.popularmovies.Model.MovieModel;
import com.sara.popularmovies.PopularMoviesAdapter.PopularMoviesAdapter;
import com.sara.popularmovies.data.MovieContract;

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


public class PopularMovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private PopularMoviesAdapter popularMoviesAdapter;
    private ArrayList<MovieModel.Movie> moviesArr;
    private Spinner spinner;
    private ProgressBar progressBar;

    private String page="1";
    private int mTotalPages=1;
    private String sortBy="popularity.desc";
    private String SortingSequence;

    private static final int MOVIES_LOADER=0;

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
    };
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_TITLE = 1;
    private static final int COL_MOVIE_RELEASE_DATE = 2;
    private static final int COL_MOVIE_OVERVIEW = 3;
    private static final int COL_MOVIE_POSTER_PATH = 4;
    private static final int COL_MOVIE_VOTE_AVERAGE = 5;
    private static final int COL_MOVIE_RUNTIME = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.popularmoviefragment, menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            SetIntialValues();
            updatePopularMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Activity activity = getActivity();
        GridView gridView = (GridView) rootView.findViewById(R.id.grd_MovieGridView);
        spinner=(Spinner)activity.findViewById(R.id.spin_Sorting);
        spinner.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progres);

        gridView.setOnItemClickListener((MainActivity) getActivity());
        if (moviesArr == null)
            moviesArr =new ArrayList<>();

       ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                  R.array.SortingSequence, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final PopularMovieFragment fragment = this ;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(spinner==null || spinner.getSelectedView()==null)
                    return ;

                SortingSequence=((TextView)spinner.getSelectedView()).getText().toString();
                switch (SortingSequence){
                    case "Most Popular":
                        sortBy="popularity.desc";
                        SetIntialValues();
                        updatePopularMovies();
                        break;
                    case "Highest Rated":
                        sortBy="vote_average.desc";
                        SetIntialValues();
                        updatePopularMovies();
                        break;
                    case "Favorites":
                        SetIntialValues();
                        getLoaderManager().initLoader(MOVIES_LOADER, null, fragment);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        popularMoviesAdapter = new PopularMoviesAdapter(activity, moviesArr);
        gridView.setAdapter(popularMoviesAdapter);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (Integer.parseInt(page) < mTotalPages) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount ) {
                        page = Integer.toString(Integer.parseInt(page) + 1);
                        updatePopularMovies();
                    }
                }
            }
        });
        if((activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)>= Configuration.SCREENLAYOUT_SIZE_LARGE){
        gridView.setNumColumns(3);
        }
        return rootView;
    }

    private void updatePopularMovies() {
        FetchPopularMoviesTask popularMoviesTask = new FetchPopularMoviesTask();
        popularMoviesTask.execute(page, sortBy);
    }

    private void SetIntialValues(){
        moviesArr.clear();
        page="1";
        mTotalPages=1;
    }
    public class FetchPopularMoviesTask extends AsyncTask<String, Void, MovieModel> {

        private final String LOG_TAG = FetchPopularMoviesTask.class.getSimpleName();

        private MovieModel getPopularMoviesDataFromJson(String popularMoviesJsonStr)
                throws JSONException {
            final String TMDB_MOVIES="results";
            final String TMDB_ID="id";
            final String TMDB_POSTERPATH="poster_path";
            final String TMDB_OVERVIEW="overview";
            final String TMDB_TITLE="title";
            final String TMDB_POPULARITY="popularity";
            final String TMDB_VOTECOUNT="vote_count";
            final String TMDB_VOTEAVERAGE="vote_average";
            final String TMDB_RELEASEDATE="release_date";
            final String TMDB_TOTALPAGES="total_pages";

            JSONObject popularMovieJson = new JSONObject(popularMoviesJsonStr);
            JSONArray popularMoviesArray = popularMovieJson.getJSONArray(TMDB_MOVIES);

            MovieModel movieModel = new MovieModel();
            movieModel.totalPages=popularMovieJson.getInt(TMDB_TOTALPAGES);
            movieModel.results = new ArrayList<>();
            for(int i = 0; i < popularMoviesArray.length(); i++) {
                MovieModel.Movie movie = new MovieModel().new Movie();

                JSONObject popularMovie = popularMoviesArray.getJSONObject(i);
                movie.id=popularMovie.getInt(TMDB_ID);
                movie.title=popularMovie.getString(TMDB_TITLE);
                movie.posterPath=popularMovie.getString(TMDB_POSTERPATH);
                movie.overview=popularMovie.getString(TMDB_OVERVIEW);
                movie.popularity=popularMovie.getDouble(TMDB_POPULARITY);
                movie.voteCount=popularMovie.getInt(TMDB_VOTECOUNT);
                movie.voteAverage=popularMovie.getDouble(TMDB_VOTEAVERAGE);
                movie.releaseDate=popularMovie.getString(TMDB_RELEASEDATE);
                movieModel.results.add(movie);
            }
            return  movieModel;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected MovieModel doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String popularMoviesJsonStr;
            try {

                final String POPULARMOVIES_BASE_URL="http://api.themoviedb.org/3/discover/movie?";
                final String SORTBY_PARAM="sort_by";
                final String PAGES_PARAM="page";
                final String APIKEY_PARAM="api_key";

                Uri builtUri = Uri.parse(POPULARMOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTBY_PARAM, params[1])
                        .appendQueryParameter(PAGES_PARAM, params[0])
                        .appendQueryParameter(APIKEY_PARAM,BuildConfig.THE_MOVIE_DB_API_KEY)
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
                popularMoviesJsonStr = buffer.toString();

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
                        Log.e("PopularMovieFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getPopularMoviesDataFromJson(popularMoviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(MovieModel movie) {

            progressBar.setVisibility(View.GONE);
            mTotalPages=movie.totalPages;
            for(MovieModel.Movie result:movie.results){
                moviesArr.add(result);
            }

            popularMoviesAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIES_COLUMNS,
                null,
                null,
                null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.moveToFirst() && SortingSequence.equals("Favorites"))
        {
            moviesArr.clear();
            do{
                MovieModel.Movie movie = new MovieModel().new Movie();
                movie.id = cursor.getInt(COL_MOVIE_ID);
                movie.title = cursor.getString(COL_MOVIE_TITLE);
                movie.posterPath=cursor.getString(COL_MOVIE_POSTER_PATH);
                movie.overview=cursor.getString(COL_MOVIE_OVERVIEW);
                movie.releaseDate=cursor.getString(COL_MOVIE_RELEASE_DATE);
                movie.voteAverage = cursor.getDouble(COL_MOVIE_VOTE_AVERAGE);
                movie.runtime = cursor.getString(COL_MOVIE_RUNTIME);
                moviesArr.add(movie);
            }while (cursor.moveToNext());
        }
        popularMoviesAdapter.notifyDataSetChanged();
    }
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}

