package yokohama.osm.activity.ui.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import yokohama.osm.R;
import yokohama.osm.activity.WebViewActivity;
import yokohama.osm.activity.ui.login.LoginViewModel;
import yokohama.osm.activity.ui.login.LoginViewModelFactory;
import yokohama.osm.camera2basic.CameraActivity;

public class LoginActivity extends AppCompatActivity {

    String TAG = "IMPORTANT";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                UserLoginTask loginTask = new UserLoginTask(usernameEditText.getText().toString(),
                                                            passwordEditText.getText().toString());
                //loginViewModel.login(usernameEditText.getText().toString(),
                //        passwordEditText.getText().toString());

                loginTask.execute();
            }
        });

        mLoginFormView = findViewById(R.id.container);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // attempt authentication against a network service.
            String url = "http://52.68.110.102:8080/PhotoGallery/SignIn";
            url = "https://192.168.11.15:8443/PhotoGallery/SignIn";

            CloudServerConnection connection = new CloudServerConnection(mEmail, mPassword,url);
            String authResult = connection.authenticate();

            Log.w(TAG, "authResult= " + authResult);

            if (authResult == null) {
                return false;
            }

            //Toast.makeText(getApplicationContext(), authResult, Toast.LENGTH_LONG).show();

            if (authResult.startsWith("success")) {
                return true;
            } else  {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            //showProgress(false);

            if (success) {
                //Toast.makeText(getApplicationContext(), "認証に成功しました。", Toast.LENGTH_LONG).show();
                Log.w(TAG, "認証に成功しました。");

                // カメラ画面へ遷移する
                nextPage();

                // 当画面を終了する
                LoginActivity.this.finish();

            } else {
                Toast.makeText(getApplicationContext(), "認証に失敗しました。", Toast.LENGTH_LONG).show();
//                mPasswordView.setError(getString(R.string.invalid_password));
//                mPasswordView.requestFocus();
            }
        }

        private void nextPage() {
            String id = mEmail;
            Intent intent = new Intent(LoginActivity.this, WebViewActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }

        /**
         * Shows the progress UI and hides the login form.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        private void showProgress(final boolean show) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class CloudServerConnection {

        String _email;
        String _password;
        String _url;

        CloudServerConnection(String email,
                              String password,
                              String url) {
            _email = email;
            _password = password;
            _url = url;
        }

        public String authenticate() {

            //http接続を行うHttpURLConnectionオブジェクトを宣言。finallyで確実に解放するためにtry外で宣言。
            HttpsURLConnection con = null;

            //http接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
            InputStream is = null;

            String queryString = "id=" + _email + "&password=" + _password;

            String result = null;

            SSLContext sslcontext = null;

            try {
                //証明書情報　全て空を返す
                TrustManager[] tm = {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }//function
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain,
                                                           String authType) throws CertificateException {
                            }//function
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain,
                                                           String authType) throws CertificateException {
                            }//function
                        }//class
                };
                sslcontext = SSLContext.getInstance("SSL");
                sslcontext.init(null, tm, null);
                //ホスト名の検証ルール　何が来てもtrueを返す
                HttpsURLConnection.setDefaultHostnameVerifier(
                        new HostnameVerifier(){
                            @Override
                            public boolean verify(String hostname,
                                                  SSLSession session) {
                                return true;
                            }//function
                        }//class
                );
            } catch (Exception e) {
                e.printStackTrace();
            }//try

            try {
                //URLオブジェクトを生成。
                URL url = new URL(_url);

                //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                con = (HttpsURLConnection) url.openConnection();

                con.setSSLSocketFactory(sslcontext.getSocketFactory());

                //http接続メソッドを設定。
                con.setRequestMethod("POST");

                // データを書き込む
                con.setDoOutput(true);

                // 応答取得
                con.setDoInput(true);

                // ヘッダ設定
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // キャッシュ有効
                con.setUseCaches(true);

                // 接続タイムアウト設定
                con.setConnectTimeout(900); // 15分間

                //接続。
                con.connect();

                // POSTデータ送信処理
                OutputStream out = null;

                try {
                    out = con.getOutputStream();
                    out.write(queryString.getBytes("UTF-8"));
                    out.flush();
                    Log.d("IMPORTANT", "flush");
                } catch (IOException e) {
                    // POST送信エラー
                    e.printStackTrace();
                    result = "POST送信エラー";
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }

                // 接続ステータスコード取得
                //
                int status = con.getResponseCode();

                Log.d("IMPORTANT", "status = " + status);

                //HttpURLConnectionオブジェクトからレスポンスデータを取得。
                is = con.getInputStream();

                //レスポンスデータであるInputStreamオブジェクトを文字列に変換。
                result = convertInputStreamToString(is);

                Log.d("IMPORTANT", "result = " + result);

            } catch (MalformedURLException ex) {
                ex.printStackTrace();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                //HttpURLConnectionオブジェクトがnullでないなら解放。
                if (con != null) {
                    con.disconnect();
                }
                //InputStreamオブジェクトがnullでないなら解放。
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return result;
        }

        public String convertInputStreamToString(InputStream is) throws IOException {
            InputStreamReader reader = new InputStreamReader(is);
            StringBuilder builder = new StringBuilder();
            char[] buf = new char[1024];
            int numRead;
            while (0 <= (numRead = reader.read(buf))) {
                builder.append(buf, 0, numRead);
                //Log.d("IMPORTANT", "builder.toString() = " + builder.toString());
            }
            return builder.toString();
        }
    }
}


