package me.szilard95.hnreader;

import android.content.Context;
import android.support.design.widget.Snackbar;
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


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
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
            viewHolder.tvBody.setText("[deleted]");
        else
            viewHolder.tvBody.setText(Html.fromHtml(item.getText()));

        ViewGroup.MarginLayoutParams m = (ViewGroup.MarginLayoutParams) viewHolder.llComment.getLayoutParams();
        m.leftMargin = (int) Math.floor(item.getLevel() * 10 * context.getResources().getDisplayMetrics().density);

        viewHolder.tvDate.setText((new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(Long.parseLong(item.getTime()) * 1000))));

        viewHolder.llCommentHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(viewHolder.llComment, "Test", Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
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
