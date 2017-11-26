package me.szilard95.hnreader.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orm.SugarRecord;

import me.szilard95.hnreader.helpers.SugarExclusionStrategy;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    public static final String HN_USER_URL = "https://news.ycombinator.com/user?id=";
    public static final String HN_ITEM_URL = "https://news.ycombinator.com/item?id=";
    private static final String ENDPOINT_ADDRESS = "https://hacker-news.firebaseio.com/";
    private static NetworkManager instance;
    private HnApi api;

    private NetworkManager() {
        final SugarExclusionStrategy strategy = new SugarExclusionStrategy(SugarRecord.class);
        final Gson gson = new GsonBuilder()
                .addDeserializationExclusionStrategy(strategy)
                .addSerializationExclusionStrategy(strategy)
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ENDPOINT_ADDRESS)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        api = retrofit.create(HnApi.class);
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public HnApi getApi() {
        return api;
    }
}