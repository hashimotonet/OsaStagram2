package yokohama.osm.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public final class URLHolder implements Serializable {
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    private String thumbnail;

    public String getAlt() { return alt; }

    public void setAlt(String alt) { this.alt = alt; }

    @JsonIgnore
    private String alt;
}
