package me.szilard95.hnreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends AppCompatActivity {
    private List<Item> commentList = new ArrayList<Item>();
    private HnApi api;
    private CommentAdapter commentAdapter;
    private AsyncTask<List<Long>, Void, CallStatus> loadCommments;

    @Override
    protected void onPause() {
        super.onPause();
        if (loadCommments != null) loadCommments.cancel(true);
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Item item = (Item) getIntent().getSerializableExtra("item");


        api = NetworkManager.getInstance().getApi();
        commentAdapter = new CommentAdapter(commentList, this);
        RecyclerView recyclerViewComments = findViewById(
                R.id.recyclerViewComments);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
        api.getItem(item.getHnId()).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                Item item = response.body();
                Log.d("KIDS", item.getKids().toString());
                loadCommments = new RequestComments().execute(item.getKids());

            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {

            }
        });

        ((TextView) findViewById(R.id.comments_item_title)).setText(item.getTitle());
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RequestComments extends AsyncTask<List<Long>, Void, CallStatus> {
        @Override
        protected void onPostExecute(CallStatus s) {
            commentAdapter.notifyDataSetChanged();
            Toast.makeText(CommentsActivity.this, s.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected CallStatus doInBackground(List<Long>[] kids) {
            return retrofit(kids[0], 0);
        }

        private CallStatus retrofit(List<Long> kids, int level) {
            try {
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
            Log.d("ASYNCTASK", callStatus.toString());
        }
    }
}
