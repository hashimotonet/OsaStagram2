package yokohama.osm.json.model;

public class JSONUploadRequest extends JSONRequest{

    /**
     * 画像データのBase64表現
     */
    private String base64;

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
