package yokohama.osm.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//import com.goebl.david.Response;
//import com.goebl.david.Webb;

import org.json.JSONObject;

import yokohama.osm.json.model.JSONUploadRequest;

/**
 * インターネット接続を行うクラス。
 */
public class InternetConnection {

    /**
     * プライベートコンストラクタ
     */
    private InternetConnection() {
        super();
    }

    /**
     * サーバーサイドへの接続メソッド
     *
     * @param json
     * @return JSONオブジェクト
     */
//    public static JSONObject connect(JSONUploadRequest json) {
//        // create the client (one-time, can be used from different threads)
//        Webb webb = Webb.create();
//        webb.setBaseUri("https://localhost:8443/PhotoGallery");
//        webb.setDefaultHeader(Webb.HDR_USER_AGENT, "Content-Disposition: form-data");
//
//        // サーバーサイドに要求送信
//        Response<JSONObject> response = webb
//                .post("/Upload")
//                .param("id", json.getId())
//                .param("base64", json.getBase64())
//                .ensureSuccess()
//                .asJsonObject();
//
//        JSONObject apiResult = response.getBody();
//
//        return apiResult;
//    }
}
