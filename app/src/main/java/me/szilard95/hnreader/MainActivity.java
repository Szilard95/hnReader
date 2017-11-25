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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MainActivity extends ThemeActivity implements NavigationView.OnNavigationItemSelectedListener, NetworkingActivity {

    public static final int MIN_TO_FETCH = 15;
    private static List<Long> currentStories;
    private static List<Item> itemList;
    boolean updating = false;
    private HnApi api;
    private ItemAdapter itemAdapter;
    private Call currentCall;
    private boolean endReached;
    private AsyncTask storyLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentStories = new ArrayList<>();
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this);
        endReached = false;
        api = NetworkManager.getInstance().getApi();

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

        RecyclerView recyclerViewItems = findViewById(
                R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);
        recyclerViewItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1) && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !updating && !endReached) {
                    storyLoading = new RequestStories().execute(currentCall);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                itemAdapter.saveItem(viewHolder.getAdapterPosition());
                Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });

        touchHelper.attachToRecyclerView(recyclerViewItems);

        currentCall = api.getTopStories();
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
            cancelLoading();
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        cancelLoading();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        if (id == R.id.nav_top) {
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
        } else if (id == R.id.nav_saves) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, SavesActivity.class));
            return true;
        }
        itemAdapter.clear();
        currentStories.clear();
        endReached = false;

        storyLoading = new RequestStories().execute(currentCall);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void cancelLoading() {
        if (storyLoading != null) storyLoading.cancel(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelLoading();
    }

    private class RequestStories extends AsyncTask<Call<List<Long>>, Void, CallStatus> {
        @Override
        protected void onPreExecute() {
            updating = true;
            Toast.makeText(MainActivity.this, R.string.loading, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(CallStatus s) {
            updating = false;
            itemAdapter.notifyDataSetChanged();
            if (s == CallStatus.END)
                endReached = true;
            Toast.makeText(MainActivity.this, s.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected CallStatus doInBackground(Call<List<Long>>[] calls) {
            return retrofit(calls[0]);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            itemAdapter.notifyItemInserted(itemList.size() - 1);
        }

        @Override
        protected void onCancelled(CallStatus callStatus) {
            updating = false;
            currentStories.clear();
            //itemAdapter.clear();
            endReached = false;
        }

        private CallStatus retrofit(Call<List<Long>> call) {
            try {
                if (currentStories.size() == 0)
                    currentStories = call.clone().execute().body();
                if (isCancelled()) return CallStatus.CANCELLED;
                int listSize = itemList.size();

                int numToFetch = Math.min(currentStories.size() - listSize, MIN_TO_FETCH);
                if (numToFetch <= 0) return CallStatus.END;
                for (int i = listSize; i < listSize + numToFetch; i++) {
                    if (isCancelled()) return CallStatus.CANCELLED;
                    Item item = api.getItem(currentStories.get(i)).execute().body();
                    itemList.add(item);
                    publishProgress();
                }
                return CallStatus.OK;
            } catch (Exception e) {
                return CallStatus.ERROR;
            }
        }
    }
}
