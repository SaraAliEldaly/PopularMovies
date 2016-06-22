package com.sara.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

import com.sara.popularmovies.Model.MovieModel;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }
        if(isTablet()){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PopularMovieFragment()).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.container2, new DetailFragment()).commit();
        }else{
            PopularMovieFragment popularMovieFragment=new PopularMovieFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, popularMovieFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MovieModel.Movie movie = (MovieModel.Movie) (parent.getItemAtPosition(position));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        DetailFragment detailFragment=new DetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(DetailFragment.ARG_MOVIE, movie);
        detailFragment.setArguments(args);
        if(isTablet())
        {
            if (findViewById(R.id.container2) == null) {
                transaction.add(R.id.container2, detailFragment);
            }else{
                transaction.replace(R.id.container2, detailFragment);
            }
        }
        else
        {
            transaction.replace(R.id.container, detailFragment);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private static boolean isTablet() {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
