package me.szilard95.hnreader.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.szilard95.hnreader.R;
import me.szilard95.hnreader.Utils;
import me.szilard95.hnreader.activity.CommentsActivity;
import me.szilard95.hnreader.activity.StoriesActivity;
import me.szilard95.hnreader.model.Item;
import me.szilard95.hnreader.network.CallStatus;
import me.szilard95.hnreader.network.NetworkingActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private Context context;
    private List<Item> itemList;

    public ItemAdapter(List<Item> placesList, Context context) {
        this.itemList = placesList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Item item = itemList.get(position);
        viewHolder.tvUser.setText(item.getBy());
        viewHolder.tvTitle.setText(item.getTitle());
        String domain = "";
        try {
            domain = item.getUrl() == null ? null : (new URI(item.getUrl()).getHost());
            if (domain == null) domain = "";
        } catch (Exception e) {
            domain = "";
        }
        viewHolder.tvDomain.setText(domain);
        viewHolder.tvScore.setText(item.getScore() + context.getString(R.string.point));
        viewHolder.tvDate.setText((new SimpleDateFormat(Utils.DATE_TIME_PATTERN).format(new Date(Long.parseLong(item.getTime()) * 1000))));
        viewHolder.tvNum.setText(String.valueOf(position + 1));
        viewHolder.tvComments.setText(String.valueOf(item.getDescendants()));


        viewHolder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NetworkingActivity) context).cancelLoading();
                Intent i = new Intent(context, CommentsActivity.class);
                i.putExtra(Item.INTENT_ID, item);
                context.startActivity(i);
            }
        });
        viewHolder.llInner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = itemList.get(position);
                ((NetworkingActivity) context).cancelLoading();

                if (item.getUrl() == null || item.getUrl().equals("")) {
                    Intent i = new Intent(context, StoriesActivity.class);
                    i.putExtra(Item.INTENT_ID, item);
                    context.startActivity(i);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(item.getUrl()));
                    context.startActivity(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


//    public void addItem(Item item) {
//        itemList.add(item);
////        item.save();
//        notifyDataSetChanged();
//    }

    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }

    public void saveItem(int adapterPosition) {
        final Item hnItem = itemList.get(adapterPosition);
        List<Item> items = Item.find(Item.class, "hn_Id = ?", hnItem.getHnId().toString());
        if (items.size() > 0) {
            Item existing = items.get(0);
            existing.delete();
        }

        notifyItemChanged(adapterPosition);

        ((NetworkingActivity) context).getApi().getItem(hnItem.getHnId()).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                Item hnItem = response.body();
                if (hnItem == null) return;
                hnItem.setKidsAsString();
                hnItem.setParentStory(hnItem.getHnId());
                hnItem.save();
                if (hnItem.getDescendants() <= 0) return;
                new RequestComments().execute(hnItem);
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                hnItem.save();
            }
        });
    }

    public void deleteSave(int adapterPosition) {
        Item i = itemList.get(adapterPosition);
        i.delete();
        itemList.remove(i);
        notifyItemRemoved(adapterPosition);
        Item.deleteAll(Item.class, "parent_Story = ?", i.getHnId().toString());
    }

    public void clearSaves() {
        Item.deleteAll(Item.class);
        clear();
    }

    public void add(Item item) {
        itemList.add(item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public TextView tvTitle;
        public TextView tvDomain;
        public TextView tvScore;
        public TextView tvDate;
        public TextView tvNum;
        public TextView tvComments;
        public LinearLayout llInner;
        public ImageButton btnComments;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.item_user);
            tvDate = itemView.findViewById(R.id.item_date);
            tvTitle = itemView.findViewById(R.id.item_title);
            tvDomain = itemView.findViewById(R.id.item_domain);
            tvScore = itemView.findViewById(R.id.item_score);
            tvNum = itemView.findViewById(R.id.item_num);
            tvComments = itemView.findViewById(R.id.item_comments);
            btnComments = itemView.findViewById(R.id.item_btn_comments);
            llInner = itemView.findViewById(R.id.item_inner_layout);
        }
    }

    private class RequestComments extends AsyncTask<Item, Void, CallStatus> {
        @Override
        protected CallStatus doInBackground(Item[] item) {
            return retrofit(item[0], 0);
        }

        private CallStatus retrofit(Item item, int level) {
            try {
                if (isCancelled()) return CallStatus.CANCELLED;
                if (item.getKids() == null) return CallStatus.NO_COMMENTS;
                for (Long kid : item.getKids()) {
                    Item i = ((NetworkingActivity) context).getApi().getItem(kid).execute().body();
                    i.setKidsAsString();
                    i.setParentStory(item.getParentStory());
                    i.save();
                    if (isCancelled()) return CallStatus.CANCELLED;
                    if (i.getKids() != null) retrofit(i, level + 1);
                }
                return CallStatus.OK;
            } catch (IOException e) {
                return CallStatus.ERROR;
            }
        }

        @Override
        protected void onPostExecute(CallStatus callStatus) {
            Toast.makeText(context, callStatus.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
