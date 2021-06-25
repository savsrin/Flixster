package com.example.flixster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.databinding.ActivityMovieDetailsBinding;
import com.example.flixster.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class MovieDetailsActivity extends AppCompatActivity {
    Movie movie;
    String videoId;
    public static final String TAG = "MovieDetailsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovieDetailsBinding binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_movie_details);
        View view = binding.getRoot();
        setContentView(view);
        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));
        // set the title and overview
        binding.tvDetTitle.setText(movie.getTitle());
        binding.tvDetOverview.setText(movie.getOverview());
        String imageUrl;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageUrl = movie.getBackdropPath();
        } else {
            imageUrl = movie.getPosterPath();
        }
        Glide.with(this).load(imageUrl).placeholder(R.drawable.flicks_movie_placeholder).into(binding.ivDetPoster);
        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        binding.rbVoteAverage.setRating(voteAverage / 2.0f);
        if (movie.getAdult()) {
            binding.tvDetAdult.setText("Rated Adult");
        } else {
            binding.tvDetAdult.setText("Not Rated Adult");
        }
        binding.tvDetReleaseDate.setText("Released on " + movie.getReleaseDate() + " |");

        // YouTube stuff : get videoId of the first JSON object in the JSOn array
        String VIDEOS_URL = String.format("https://api.themoviedb.org/3/movie/%s/videos?api_key=%s&language=en-US",
                                         movie.getId().toString(), getString(R.string.movie_api_key));
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(VIDEOS_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray results = jsonObject.getJSONArray("results");
                    Log.i(TAG, "Results: " + results.toString());
                    videoId = results.getJSONObject(0).getString("key");
                } catch (JSONException e) {
                    videoId = "";
                    Log.e(TAG, "Hit json exception");
                }
                Log.i(TAG, "Video ID: " + videoId);

            }
            @Override
            public void onFailure(int i, Headers headers, String s, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });
        binding.ivPlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoId.equals("")) {
                    Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                    intent.putExtra("videoId", videoId);
                    MovieDetailsActivity.this.startActivity(intent);
                }
            }
        });



    }
}