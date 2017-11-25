package me.szilard95.hnreader.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.szilard95.hnreader.R;
import me.szilard95.hnreader.adapter.CommentAdapter;
import me.szilard95.hnreader.model.Item;
import me.szilard95.hnreader.network.CallStatus;
import me.szilard95.hnreader.network.HnApi;
import me.szilard95.hnreader.network.NetworkManager;
import me.szilard95.hnreader.network.NetworkingActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends ThemeActivity implements NetworkingActivity {
    private List<Item> commentList = new ArrayList<Item>();
    private Item hnItem;
    private HnApi api;
    private CommentAdapter commentAdapter;
    private AsyncTask<List<Long>, Void, CallStatus> loadCommments;
    private TextView tvNoComments;

    public HnApi getApi() {
        return api;
    }

    public void cancelLoading() {
        if (loadCommments != null) loadCommments.cancel(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelLoading();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hnItem.getKids() == null) return;
                cancelLoading();
                loadCommments = new RequestComments().execute(hnItem.getKids());
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        hnItem = (Item) getIntent().getSerializableExtra(Item.INTENT_ID);


        api = NetworkManager.getInstance().getApi();
        commentAdapter = new CommentAdapter(commentList, this);
        RecyclerView recyclerViewComments = findViewById(
                R.id.recyclerViewComments);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
        Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
        api.getItem(hnItem.getHnId()).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                hnItem = response.body();
                loadCommments = new RequestComments().execute(hnItem.getKids());
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Toast.makeText(CommentsActivity.this, R.string.error_loading, Toast.LENGTH_SHORT).show();
            }
        });

        ((TextView) findViewById(R.id.comments_item_title)).setText(hnItem.getTitle());
        tvNoComments = findViewById(R.id.comments_empty);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, hnItem.getTitle() + getString(R.string.hn_item_url) + hnItem.getHnId());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private class RequestComments extends AsyncTask<List<Long>, Void, CallStatus> {
        @Override
        protected void onPreExecute() {
            commentAdapter.clear();
        }

        @Override
        protected void onPostExecute(CallStatus s) {
            commentAdapter.notifyDataSetChanged();
            if (s == CallStatus.NO_COMMENTS)
                tvNoComments.setVisibility(View.VISIBLE);
            else {
                tvNoComments.setVisibility(View.GONE);
                Toast.makeText(CommentsActivity.this, s.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected CallStatus doInBackground(List<Long>[] kids) {
            return retrofit(kids[0], 0);
        }

        private CallStatus retrofit(List<Long> kids, int level) {
            try {
                if (isCancelled()) return CallStatus.CANCELLED;
                if (kids == null) return CallStatus.NO_COMMENTS;
                for (Long kid : kids) {
                    Item i = api.getItem(kid).execute().body();
                    i.setLevel(level);
                    commentList.add(i);
                    publishProgress();
                    if (isCancelled()) return CallStatus.CANCELLED;
                    if (i.getKids() != null) retrofit(i.getKids(), level + 1);
                }
                return CallStatus.OK;
            } catch (IOException e) {
                return CallStatus.ERROR;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            commentAdapter.notifyItemInserted(commentList.size() - 1);
        }

        @Override
        protected void onCancelled(CallStatus callStatus) {
            commentAdapter.clear();
        }
    }
}
