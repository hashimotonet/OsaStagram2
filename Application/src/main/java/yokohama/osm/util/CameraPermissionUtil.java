package yokohama.osm.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

//import yokohama.osm.Manifest;

public class CameraPermissionUtil {
    /**
     * 権限があるか確認し、権限がなければ要求する
     *
     * @return 権限があるときはtrue
     */
    public static boolean checkAndRequestPermissions(final @NonNull Activity activity, final @IntRange(from = 0) int requestCode) {
        String[] permissionNeeded;
        permissionNeeded = permissionNeeded(activity, new String[]{
                Manifest.permission.CAMERA
        });
        if (permissionNeeded.length > 0) {
            ActivityCompat.requestPermissions(activity, permissionNeeded, requestCode);
            return false;
        }
        return true;
    }

    private static String[] permissionNeeded(final @NonNull Activity activity, String[] permissions) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        return listPermissionsNeeded.toArray(new String[0]);
    }
}