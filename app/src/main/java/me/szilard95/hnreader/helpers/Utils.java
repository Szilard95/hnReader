package me.szilard95.hnreader.helpers;

import android.content.Context;
import android.widget.Toast;

import me.szilard95.hnreader.R;

public final class Utils {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    // https://stackoverflow.com/questions/9589381/remove-extra-line-breaks-after-html-fromhtml
    public static CharSequence trim(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        }
        while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.subSequence(0, i + 1);
    }

    public static void showErrorToast(Context context) {
        Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
    }

    public static void showLoadingToast(Context context) {
        Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show();
    }
}
