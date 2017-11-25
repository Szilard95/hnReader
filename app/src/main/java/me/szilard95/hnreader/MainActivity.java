package me.szilard95.hnreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MainActivity extends ThemeActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int MIN_TO_FETCH = 15;
    static private List<Long> currentStories = new ArrayList<>();
    boolean updating = false;
    private List<Item> itemList = new ArrayList<Item>();
    private HnApi api;
    private ItemAdapter itemAdapter;
    private Call currentCall;
    private boolean endReached = false;
    private transient boolean shouldSave = true;
    private AsyncTask storyLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.top_stories);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updating) return;
                itemAdapter.clear();
                currentStories.clear();
                endReached = false;
                Toast.makeText(MainActivity.this, "Updating", Toast.LENGTH_SHORT).show();
                storyLoading = new RequestStories().execute(currentCall);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        api = NetworkManager.getInstance().getApi();

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
                    storyLoading = new RequestStories().execute(currentCall);
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

        if (itemList.size() < MIN_TO_FETCH) {
            storyLoading = new RequestStories().execute(currentCall);
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
    protected void onResume() {
        super.onResume();
        if (itemList.size() < MIN_TO_FETCH) {
            storyLoading = new RequestStories().execute(currentCall);
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
        if (storyLoading != null) storyLoading.cancel(true);
        int id = item.getItemId();
        shouldSave = false;
        if (id == R.id.nav_top) {
            shouldSave = true;
            currentCall = api.getTopStories();
            getSupportActionBar().setTitle(R.string.top_stories);
        } else if (id == R.id.nav_new) {
            currentCall = api.getNewStories();
            getSupportActionBar().setTitle(R.string.new_stories);
        } else if (id == R.id.nav_best) {
            currentCall = api.getBestStories();
            getSupportActionBar().setTitle(R.string.best_stories);
        } else if (id == R.id.nav_ask) {
            currentCall = api.getAskStories();
            getSupportActionBar().setTitle(R.string.ask_hn);
        } else if (id == R.id.nav_show) {
            currentCall = api.getShowStories();
            getSupportActionBar().setTitle(R.string.show_hn);
        } else if (id == R.id.nav_jobs) {
            currentCall = api.getJobStories();
            getSupportActionBar().setTitle(R.string.jobs);
        }
        itemAdapter.clear();
        currentStories.clear();
        endReached = false;
        Toast.makeText(MainActivity.this, "Updating", Toast.LENGTH_SHORT).show();
        storyLoading = new RequestStories().execute(currentCall);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (storyLoading != null) storyLoading.cancel(true);
    }

    private class RequestStories extends AsyncTask<Call<List<Long>>, Void, CallStatus> {
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

        @Override
        protected void onProgressUpdate(Void... values) {
            itemAdapter.notifyItemInserted(itemList.size() - 1);
        }

        @Override
        protected void onCancelled(CallStatus callStatus) {
            updating = false;
        }

        private CallStatus retrofit(Call<List<Long>> call) {
            try {
                if (currentStories.size() == 0)
                    currentStories = call.clone().execute().body();
                int listSize = itemList.size();
//            Log.d("SIZE", "listSize: " + listSize + " topSize: " + currentStories.size());
                int numToFetch = Math.min(currentStories.size() - listSize, MIN_TO_FETCH);
                if (numToFetch <= 0) return CallStatus.END;
                for (int i = listSize; i < listSize + numToFetch; i++) {
                    Item item = api.getItem(currentStories.get(i)).execute().body();
                    itemList.add(item);
                    publishProgress();
                    if (shouldSave) item.save();
                    if (isCancelled()) return CallStatus.CANCELLED;
                }
                return CallStatus.OK;
            } catch (IOException e) {
                return CallStatus.ERROR;
            }
        }
    }
}
