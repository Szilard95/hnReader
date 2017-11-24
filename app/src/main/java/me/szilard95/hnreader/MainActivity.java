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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orm.SugarRecord;

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
    static List<Long> top = new ArrayList<>();
    Retrofit retrofit;
    List<Item> itemList = new ArrayList<Item>();
    boolean updating = false;
    private HnApi api;
    private ItemAdapter itemAdapter;
    private Call currentCall;
    private boolean endReached = false;
    private transient boolean shouldSave = true;

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
                if (updating) return;
                itemAdapter.clear();
                top.clear();
                endReached = false;
                Toast.makeText(MainActivity.this, "Updating", Toast.LENGTH_SHORT).show();
                new test().execute(currentCall);
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
        final SugarExclusionStrategy strategy = new SugarExclusionStrategy(SugarRecord.class);
        final Gson gson = new GsonBuilder()
                .addDeserializationExclusionStrategy(strategy)
                .addSerializationExclusionStrategy(strategy)
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://hacker-news.firebaseio.com")
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        api = retrofit.create(HnApi.class);

        itemList = Item.listAll(Item.class);

        itemAdapter = new ItemAdapter(itemList, this);
        RecyclerView recyclerViewItems = findViewById(
                R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);
        recyclerViewItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1) && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !updating && !endReached) {
                    new test().execute(currentCall);
                    Toast.makeText(MainActivity.this, "Loading more...", Toast.LENGTH_SHORT).show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        currentCall = api.getTopStories();

        if (itemList.size() == 0) {
            new test().execute(currentCall);
        }
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

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        shouldSave = false;
        if (id == R.id.nav_top) {
            shouldSave = true;
            currentCall = api.getTopStories();
        } else if (id == R.id.nav_new) {
            currentCall = api.getNewStories();
        } else if (id == R.id.nav_best) {
            currentCall = api.getBestStories();
        } else if (id == R.id.nav_ask) {
            currentCall = api.getAskStories();
        } else if (id == R.id.nav_show) {
            currentCall = api.getShowStories();
        } else if (id == R.id.nav_jobs) {
            currentCall = api.getJobStories();
        }
        itemAdapter.clear();
        top.clear();
        endReached = false;
        Toast.makeText(MainActivity.this, "Updating", Toast.LENGTH_SHORT).show();
        new test().execute(currentCall);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // TODO: HIGHLY EXPERIMENTAL

    private CallStatus retrofit(Call<List<Long>> call) {
        try {
            if (top.size() == 0)
                top = call.clone().execute().body();
            int listSize = itemList.size();
//            Log.d("SIZE", "listSize: " + listSize + " topSize: " + top.size());
            int numToFetch = Math.min(top.size() - listSize, 10);
            if (numToFetch <= 0) return CallStatus.END;
            for (int i = listSize; i < listSize + numToFetch; i++) {
                Item item = api.getItem(top.get(i)).execute().body();
                itemList.add(item);
                if (shouldSave) item.save();
            }
            return CallStatus.OK;
        } catch (IOException e) {
            return CallStatus.ERROR;
        }
    }

    interface HnApi {
        @GET("/v0/item/{item}.json")
        Call<Item> getItem(@Path("item") long id);

        @GET("/v0/item/{user}.json")
        Call<Item> getUser(@Path("user") String id);

        @GET("/v0/maxitem.json")
        Call<Long> getMaxItem();

        @GET("/v0/topstories.json")
        Call<List<Long>> getTopStories();

        @GET("/v0/newstories.json")
        Call<List<Long>> getNewStories();

        @GET("/v0/beststories.json")
        Call<List<Long>> getBestStories();

        @GET("/v0/askstories.json")
        Call<List<Long>> getAskStories();

        @GET("/v0/showstories.json")
        Call<List<Long>> getShowStories();

        @GET("/v0/jobstories.json")
        Call<List<Long>> getJobStories();
    }

    private class test extends AsyncTask<Call<List<Long>>, Void, CallStatus> {
        @Override
        protected void onPostExecute(CallStatus s) {
            itemAdapter.notifyDataSetChanged();
            updating = false;
            if (s == CallStatus.END)
                endReached = true;
            Toast.makeText(MainActivity.this, s.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected CallStatus doInBackground(Call<List<Long>>[] calls) {
            updating = true;
            return retrofit(calls[0]);
        }
    }
}
