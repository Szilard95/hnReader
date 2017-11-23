package me.szilard95.hnreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Retrofit retrofit;
    private HnApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                new test().execute();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // TODO
        retrofit = new Retrofit.Builder()
                .baseUrl("https://hacker-news.firebaseio.com")
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(HnApi.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_top) {

        } else if (id == R.id.nav_new) {

        } else if (id == R.id.nav_best) {

        } else if (id == R.id.nav_ask) {

        } else if (id == R.id.nav_show) {

        } else if (id == R.id.nav_jobs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String retrofit() {
        try {
            List<Long> top = api.getTopStories().execute().body();
            List<HnItem> l = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                l.add(api.getItem(top.get(i)).execute().body());
            }
            StringBuilder msg = new StringBuilder();
            for (HnItem hnItem : l) {
                msg.append('[').append(hnItem.score).append("] ").append(hnItem.title).append('\n');
            }
            return msg.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    // TODO: HIGHLY EXPERIMENTAL

    interface HnApi {
        @GET("/v0/item/{item}.json")
        Call<HnItem> getItem(@Path("item") long id);

        @GET("/v0/maxitem.json")
        Call<Long> getMaxItem();

        @GET("/v0/topstories.json")
        Call<List<Long>> getTopStories();
    }

    private static class HnItem {
        public long id;
        public String title;
        public String url;
        public long score;
    }

    private class test extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            ((TextView) findViewById(R.id.EXP_content)).setText(s);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return retrofit();
        }
    }
}
