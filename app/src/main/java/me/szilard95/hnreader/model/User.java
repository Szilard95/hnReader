package me.szilard95.hnreader.model;

public class User {
    private String id;
    private long created;
    private long karma;
    private String about;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getKarma() {
        return karma;
    }

    public void setKarma(long karma) {
        this.karma = karma;
    }

    public String getAbout() {
        return about == null ? "" : about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
