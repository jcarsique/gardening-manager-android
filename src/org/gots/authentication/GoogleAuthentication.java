package org.gots.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gots.ui.LoginActivity;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class GoogleAuthentication {
    private String CLIENT_ID = "473239775303-khctmm26flfc9c3m97ge3uss4ajo8c3r.apps.googleusercontent.com";

    private String CLIENT_SECRET = "sdxIz8qR2xdIE4FaYb3CZYvz";

    private String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    final String G_PLUS_SCOPE = "https://www.googleapis.com/auth/plus.me";

    final String USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";

    protected String TAG = "GoogleAuthentication";

    private Context mContext;

    public GoogleAuthentication(Context context) {
        this.mContext = context;
    }

//    protected void getAPIToken() {
//        new AsyncTask<String, Void, String>() {
//
//            @Override
//            protected String doInBackground(String... params) {
//                try {
//                    final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id="
//                            + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI;
//                    final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope="
//                            + URLEncoder.encode(G_PLUS_SCOPE + " " + USERINFO_SCOPE);
//
//                    HttpClient httpclient = new DefaultHttpClient();
//                    HttpGet httpget = new HttpGet(SCOPED_AUTHORIZE_URL);
//
//                    HttpResponse response = httpclient.execute(httpget);
//                    StatusLine serverCode = response.getStatusLine();
//                    int code = serverCode.getStatusCode();
//                    if (code == 200) {
//                        InputStream is = response.getEntity().getContent();
//                        JSONArray jsonArray = new JSONArray(convertStreamToString(is));
//                        String authToken = (String) jsonArray.opt(0);
//                        return authToken;
//                        // bad token, invalidate and get a new one
//                    } else if (code == 401) {
//                        Log.e(TAG, "Server auth error: " + response.getStatusLine());
//                        return null;
//                        // unknown error, do something else
//                    } else {
//                        Log.e("Server returned the following error code: " + serverCode, "");
//                        return null;
//                    }
//                } catch (MalformedURLException e) {
//                } catch (IOException e) {
//                } catch (JSONException e) {
//                } finally {
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String accessToken) {
//                Log.d("AccessToken", " " + accessToken);
//            }
//        }.execute();
//    }

    protected String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                inputStream.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public String getToken(String accountName) throws UserRecoverableAuthException, IOException, GoogleAuthException {
        String token = null;
        final String G_PLUS_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.me";
        final String USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";

        final String SCOPES = G_PLUS_SCOPE + " " + USERINFO_SCOPE;
        token = GoogleAuthUtil.getToken(mContext, accountName, SCOPES);

        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int serverCode = con.getResponseCode();
        // successful query
        if (serverCode == 200) {
            try {
                InputStream is = con.getInputStream();
                JSONArray jsonArray;
                jsonArray = new JSONArray(convertStreamToString(is));
                String name = (String) jsonArray.opt(0);

                // String name = getFirstName(readResponse(is));
                Log.d(TAG, "Hello " + name + "!");
                is.close();
            } catch (JSONException e) {
                return null;
            }
            // bad token, invalidate and get a new one
        } else if (serverCode == 401) {
            GoogleAuthUtil.invalidateToken(mContext, token);
            // Log.e(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
            Log.e(TAG, "Server auth error: ");
            // unknown error, do something else
        } else {
            Log.e("Server returned the following error code: " + serverCode, null);
        }

        return token;
    }
}