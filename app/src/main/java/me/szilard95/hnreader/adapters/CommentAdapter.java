package me.szilard95.hnreader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.szilard95.hnreader.R;
import me.szilard95.hnreader.helpers.UserClickListener;
import me.szilard95.hnreader.helpers.Utils;
import me.szilard95.hnreader.models.Item;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    public static final int MAX_USER_DESCRIPTION_LINES = 15;
    private Context context;
    private List<Item> commentList;

    public CommentAdapter(List<Item> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_comment, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Item item = commentList.get(position);
        viewHolder.tvUser.setText(item.getBy());

        if (item.getDeleted())
            viewHolder.tvBody.setText(R.string.deleted);
        else
            viewHolder.tvBody.setText(Utils.trim(Html.fromHtml(item.getText())));

        ViewGroup.MarginLayoutParams m = (ViewGroup.MarginLayoutParams) viewHolder.llComment.getLayoutParams();
        m.leftMargin = (int) Math.floor(item.getLevel() * 10 * context.getResources().getDisplayMetrics().density);

        viewHolder.tvDate.setText((new SimpleDateFormat(Utils.DATE_TIME_PATTERN).format(new Date(Long.parseLong(item.getTime()) * 1000))));
        viewHolder.llCommentHeader.setOnClickListener(new UserClickListener(context, viewHolder.llComment, item));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void clear() {
        commentList.clear();
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUser;
        public TextView tvBody;
        public TextView tvDate;
        public LinearLayout llComment;
        public LinearLayout llCommentHeader;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.comment_user);
            tvDate = itemView.findViewById(R.id.comment_date);
            tvBody = itemView.findViewById(R.id.comment_body);
            llCommentHeader = itemView.findViewById(R.id.comment_header);
            llComment = itemView.findViewById(R.id.comment);
        }
    }
}
