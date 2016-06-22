package com.sara.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;
    private static final int MOVIES = 10;
    private static final int MOVIE_WITH_ID = 20;

    private static final int TRAILERS_FOR_MOVIE = 30;
    private static final int REVIEWS_FOR_MOVIE = 40;
    private static final int REVIEWS = 50;
    private static final int TRAILERS = 60 ;


    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry._ID + " = ? ";

    private static final String sTrailerMovieSelection =
            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sReviewMovieSelection =
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final SQLiteQueryBuilder sFavoriteMoviesQueryBuilder;

    static{
        sFavoriteMoviesQueryBuilder = new SQLiteQueryBuilder();

        sFavoriteMoviesQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.TrailerEntry.TABLE_NAME +
                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID +
                        " = " + MovieContract.TrailerEntry.TABLE_NAME +
                        "." + MovieContract.TrailerEntry.COLUMN_MOVIE_ID);
        sFavoriteMoviesQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.ReviewEntry.TABLE_NAME +
                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID +
                        " = " + MovieContract.ReviewEntry.TABLE_NAME +
                        "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID);
    }

    private Cursor getMovieWithId (Uri uri, String[] projection, String sortOrder) {
        int id = MovieContract.MovieEntry.getIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                sMovieIdSelection,
                new String[] {id + ""},
                null,
                null,
                sortOrder
        )
        );
    }

    private Cursor getTrailerForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME,
                projection,
                sTrailerMovieSelection,
                new String[] {movieId + ""},
                null,
                null,
                sortOrder
        )
        );
    }

    private Cursor getReviewForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);
        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME,
                projection,
                sReviewMovieSelection,
                new String[] {movieId + ""},
                null,
                null,
                sortOrder
        )
        );
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILERS + "/#", TRAILERS_FOR_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_TRAILERS , TRAILERS);
        matcher.addURI(authority, MovieContract.PATH_REVIEWS + "/#", REVIEWS_FOR_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_REVIEWS , REVIEWS);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILERS_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case REVIEWS_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_WITH_ID: {
                retCursor = getMovieWithId(uri, projection, sortOrder);
                break;
            }
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                    );
                break;
            }

            case TRAILERS_FOR_MOVIE: {
                retCursor = getTrailerForMovie(uri, projection, sortOrder);
                break;
            }

            case REVIEWS_FOR_MOVIE: {
                retCursor = getReviewForMovie(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id = -1 ;
        switch (match) {
            case MOVIES: {
                try {
                    _id = db.insertOrThrow(MovieContract.MovieEntry.TABLE_NAME, null, values);
                }
                catch (SQLException ex)
                {
                    Log.d("DEBUG", ex.toString());
                }
                finally {
                    if (_id > 0)
                        returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
            }

            case REVIEWS: {
                try {
                    _id = db.insertOrThrow(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                }
                catch (SQLException ex)
                {
                    Log.d("DEBUG", ex.toString());
                }
                finally {
                    if (_id > 0)
                        returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
            }

            case TRAILERS :
            {
                try {
                    _id = db.insertOrThrow(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                }
                catch (SQLException ex)
                {
                    Log.d("DEBUG", ex.toString());
                }
                finally {
                    if (_id > 0)
                        returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    break;
                }
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted,rrowsDeleted,trowsDeleted;
        selectionArgs = new String[] {MovieContract.MovieEntry.getIdFromUri(uri) + ""};
        trowsDeleted=db.delete(MovieContract.TrailerEntry.TABLE_NAME,sTrailerMovieSelection,selectionArgs);
        rrowsDeleted=db.delete(MovieContract.ReviewEntry.TABLE_NAME,sReviewMovieSelection,selectionArgs);
        rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, sMovieIdSelection, selectionArgs);
        if((rowsDeleted != 0)|| (rrowsDeleted!=0)  || trowsDeleted!= 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

   @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        if(null == selection) selection = "1";
        switch (match) {
            case MOVIE_WITH_ID: {
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
       if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }



}
