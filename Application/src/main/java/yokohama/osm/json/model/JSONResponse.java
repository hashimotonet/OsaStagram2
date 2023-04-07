package yokohama.osm.json.model;

public class JSONResponse extends JSONRequest {

    /**
     * ユーザ権限
     */
    private String authority;

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
