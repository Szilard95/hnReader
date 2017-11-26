package me.szilard95.hnreader.networking;

/**
 * Created by szilard95 on 11/25/17.
 */

public interface NetworkingActivity {
    void cancelLoading();

    HnApi getApi();
}
