package me.szilard95.hnreader.network;

/**
 * Created by szilard95 on 11/25/17.
 */

public interface NetworkingActivity {
    void cancelLoading();

    HnApi getApi();
}
