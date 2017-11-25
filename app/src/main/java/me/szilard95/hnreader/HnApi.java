package me.szilard95.hnreader;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface HnApi {
    @GET("/v0/item/{item}.json")
    Call<Item> getItem(@Path("item") long id);

    @GET("/v0/item/{user}.json")
    Call<Item> getUser(@Path("user") String id);

    @GET("/v0/maxitem.json")
    Call<Long> getMaxItem();

    @GET("/v0/topstories.json")
    Call<List<Long>> getTopStories();

    @GET("/v0/newstories.json")
    Call<List<Long>> getNewStories();

    @GET("/v0/beststories.json")
    Call<List<Long>> getBestStories();

    @GET("/v0/askstories.json")
    Call<List<Long>> getAskStories();

    @GET("/v0/showstories.json")
    Call<List<Long>> getShowStories();

    @GET("/v0/jobstories.json")
    Call<List<Long>> getJobStories();
}
