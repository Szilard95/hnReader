package me.szilard95.hnreader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import me.szilard95.hnreader.R;
import me.szilard95.hnreader.model.Item;

public class StoriesActivity extends ThemeActivity {

    private Item hnItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        hnItem = (Item) getIntent().getSerializableExtra(Item.INTENT_ID);
        ((TextView) findViewById(R.id.stories_item_title)).setText(hnItem.getTitle());
        ((TextView) findViewById(R.id.story_body)).setText(hnItem.getText().equals("") ? getString(R.string.no_content) : Html.fromHtml(hnItem.getText()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar hnItem clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, hnItem.getTitle() + " - " + hnItem.getText());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.action_comments) {
            Intent i = new Intent(StoriesActivity.this, CommentsActivity.class);
            i.putExtra(Item.INTENT_ID, hnItem);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
