package com.sara.popularmovies.Model;

import java.io.Serializable;
import java.util.List;

public class MovieModel implements Serializable{

    public List<Movie> results;
    public Integer totalPages;

  public class Movie implements Serializable{

         public String posterPath;
         public String overview;
         public String releaseDate;
         public Integer id;
         public String title;
         public Double popularity;
         public Integer voteCount;
         public Double voteAverage;
         public String runtime;
    }

}

