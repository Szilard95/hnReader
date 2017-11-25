package me.szilard95.hnreader;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;

/**
 * Created by szilard95 on 11/25/17.
 */

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

    // https://stackoverflow.com/questions/31141110/android-textview-autolink-and-a-href-clickable
    public static Spannable linkifyHtml(String html, int linkifyMask) {
        Spanned text = Html.fromHtml(html);
        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);
        Linkify.addLinks(buffer, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        return buffer;
    }
}
