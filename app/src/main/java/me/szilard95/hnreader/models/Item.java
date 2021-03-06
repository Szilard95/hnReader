package me.szilard95.hnreader.models;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Item extends SugarRecord<Item> implements Serializable {
    @Ignore
    public static final String INTENT_ID = "item";
    @Ignore
    private boolean isCached = false;
    @Ignore
    private int level = 0;
    @SerializedName("id")
    private Long hnId;
    private String title;
    private String type;
    private String parent;
    private long parentStory;
    @Ignore
    private List<Long> kids;
    private String kidsAsString;
    private String time;
    private String url;
    private String score;
    private long descendants;
    private String by;
    private boolean deleted;
    private String text;

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getHnId() {
        return hnId;
    }

    public void setHnId(Long id) {
        this.hnId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<Long> getKids() {
        if (kids != null) return kids;
        return getKidsAsList();
    }

    public void setKids(List<Long> kids) {
        this.kids = kids;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public long getDescendants() {
        return descendants;
    }

    public void setDescendants(long descendants) {
        this.descendants = descendants;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setKidsAsString() {
        if (kids == null) return;
        StringBuilder sb = new StringBuilder();
        for (Long kid : kids) {
            sb.append(kid);
            sb.append(" ");
        }
        kidsAsString = sb.toString();
    }

    private List<Long> getKidsAsList() {
        if (kidsAsString == null) return null;
        Scanner s = new Scanner(kidsAsString);
        List<Long> l = new ArrayList<>();
        while (s.hasNextLong())
            l.add(s.nextLong());
        return l;
    }

    public long getParentStory() {
        return parentStory;
    }

    public void setParentStory(long parentStory) {
        this.parentStory = parentStory;
    }
}