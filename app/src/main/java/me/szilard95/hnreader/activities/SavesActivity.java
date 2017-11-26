package me.szilard95.hnreader.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import me.szilard95.hnreader.R;
import me.szilard95.hnreader.adapters.ItemAdapter;
import me.szilard95.hnreader.models.Item;
import me.szilard95.hnreader.networking.HnApi;
import me.szilard95.hnreader.networking.NetworkingActivity;

public class SavesActivity extends ThemeActivity implements NetworkingActivity {

    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saves);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<Item> itemList = Item.find(Item.class, "type != 'comment'");
        for (Item item : itemList) {
            item.setCached(true);
        }
        itemAdapter = new ItemAdapter(itemList, this);
        RecyclerView recyclerViewItems = findViewById(
                R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);
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
                itemAdapter.deleteSave(viewHolder.getAdapterPosition());
            }
        });

        touchHelper.attachToRecyclerView(recyclerViewItems);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_saves, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_saves) {
            itemAdapter.clearSaves();
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void cancelLoading() {
        return;
    }

    @Override
    public HnApi getApi() {
        return null;
    }
}
