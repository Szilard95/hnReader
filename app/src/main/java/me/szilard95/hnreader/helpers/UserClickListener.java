package me.szilard95.hnreader.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import me.szilard95.hnreader.R;
import me.szilard95.hnreader.models.Item;
import me.szilard95.hnreader.models.User;
import me.szilard95.hnreader.networking.NetworkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.szilard95.hnreader.adapters.CommentAdapter.MAX_USER_DESCRIPTION_LINES;

public class UserClickListener implements View.OnClickListener {


    private View view;
    private Context context;
    private Item item;

    public UserClickListener(Context context, View view, Item item) {
        this.view = view;
        this.context = context;
        this.item = item;
    }

    @Override
    public void onClick(View v) {
        Snackbar.make(view, R.string.loading, Snackbar.LENGTH_INDEFINITE).show();

        NetworkManager.getInstance().getApi().getUser(item.getBy()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User u = response.body();
                CharSequence msg;
                msg = Utils.trim(Html.fromHtml("<b>" + u.getId() + "</b> (" + u.getKarma() + ")<br>" + u.getAbout()));
                showUserToast(msg, u.getId());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Snackbar.make(view, R.string.error_user, Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    private void showUserToast(CharSequence msg, final String userId) {
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE);
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(MAX_USER_DESCRIPTION_LINES);
        snackbar.setAction(R.string.profile, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(context.getString(R.string.hn_user_url) + userId));
                context.startActivity(i);
            }
        });
        snackbar.show();
    }
}
