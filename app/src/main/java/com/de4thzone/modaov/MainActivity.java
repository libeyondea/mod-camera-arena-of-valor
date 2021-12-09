package com.de4thzone.modaov;

import com.de4thzone.modaov.BuildConfig;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    private static Context mContext;
    ProgressDialog mProgressDialog;
    private Button install;
    private Spinner spinner_percent;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private static String[] file_url_version = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/version.d4z"
    };

    private static String[] file_url_default = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/default/d4z.zip"
    };

    private static String[] file_url_7 = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/7percent/d4z.zip"
    };

    private static String[] file_url_10 = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/10percent/d4z.zip"
    };

    private static String[] file_url_15 = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/15percent/d4z.zip"
    };

    private static String[] file_url_20 = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/20percent/d4z.zip",
    };
    private static String[] file_url_25 = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/25percent/d4z.zip"
    };

    private static String[] file_url_30 = {
            "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/30percent/d4z.zip"
    };

    private String versionText = "";

    private static int VersionNow = 115;

    private int versionCode = BuildConfig.VERSION_CODE;

    private String versionName = BuildConfig.VERSION_NAME;

    private static String[] urlCheckVersion = { "https://github.com/libeyondea/mod-camera-arena-of-valor-server/raw/main/version-app.d4z" };

    private String urlUpdateApp = "https://server-mod-aov.herokuapp.com/mod-arena-of-valor"  + "-v" + versionCode + "(" + versionName + ")" + "-release.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        new checkNewVersionApp(MainActivity.this).execute(urlCheckVersion);

        new GetVersionAOV(MainActivity.this).execute();

        TextView tl = (TextView) findViewById(R.id.text_link);
        tl.setMovementMethod(LinkMovementMethod.getInstance());

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle("Install Data");
        mProgressDialog.setMessage("Installing file ");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        // execute this when the downloader must be fired

        //get the spinner from the xml.
        addItemsOnSpinnerPercent();
        addListenerOnInsPlu();

    }

    private String targetDownload(String versionText) {
        String temp = "/Android/data/com.garena.game.kgvn/files/Resources/" + versionText +"/Ages/Prefab_Characters/Prefab_Hero";
        return temp;
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private int count = 1;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            //File sdCardRoot = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.garena.game.kgvn/files/Resources/" + versionText +"/Ages/Prefab_Characters/Prefab_Hero/commonresource");
            //if (!sdCardRoot.exists()) {
            //    sdCardRoot.mkdirs();
            //}

            for (int i = 0; i < sUrl.length; i++) {
                try {
                    URL url = new URL(sUrl[i]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                    }

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();

                    File saveFolder = new File(Environment.getExternalStorageDirectory() + targetDownload(versionText) + "/" + getFileNameFromURL(url.toString()));
                    output = new FileOutputStream(saveFolder);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }

                    deleteRecursive(new File(Environment.getExternalStorageDirectory() + targetDownload(versionText) + "/commonresource"));

                    deleteRecursive(new File(Environment.getExternalStorageDirectory() + targetDownload(versionText) + "/Cam"));

                    unzip(saveFolder, new File(Environment.getExternalStorageDirectory() + targetDownload(versionText)));

                    if (saveFolder.exists()) {
                        saveFolder.delete();
                    }

                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
                count++;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setMessage("Installing file " + count + " / " + file_url_default.length);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            count = 1;
            if (result != null)
                Toast.makeText(context,"Install error: "+result, Toast.LENGTH_LONG).show();
            else
            {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Install success")
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create();
                dialog.show();
                Toast.makeText(context, "Install success", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("github.com");
            //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }

    private class checkNewVersionApp extends AsyncTask<String, Integer, String> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        public checkNewVersionApp(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                // Create a URL for the desired page
                URL urlCheck = new URL(sUrl[0]);
                // Read all the text returned by the server
                HttpURLConnection conn = (HttpURLConnection) urlCheck.openConnection();
                conn.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String str;
                String outPut = "";
                while ((str = in.readLine()) != null) {
                    outPut = str;
                }
                in.close();
                return outPut;
            } catch (MalformedURLException e) {
                return e.toString();
            } catch (IOException e) {
                return e.toString();
            }
        }
        protected void onPostExecute(String result) {
            mWakeLock.release();
            Toast.makeText(context,"Version app: " + result, Toast.LENGTH_LONG).show();
            // dismiss progress dialog and update ui
            try {
                if (!isNetworkConnected() && !isInternetAvailable()) {
                    //we are connected to a network
                    AlertDialog dialogUp = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Please connect internet")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                        finish();
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).setCancelable(false)
                            .create();
                    dialogUp.show();
                } else if (Integer.parseInt(result) > VersionNow) {
                    AlertDialog dialogUp = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Please download new version now")
                            .setNegativeButton("Download", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        Uri uri = Uri.parse(urlUpdateApp);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                        finish();
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).setCancelable(false)
                            .create();
                    dialogUp.show();
                }
            } catch (Exception e) {
                Toast.makeText(context,"ERROR: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public void addItemsOnSpinnerPercent() {
        spinner_percent = (Spinner) findViewById(R.id.spinner_percent);
        List<String> list = new ArrayList<String>();
        list.add("Select percent");
        list.add("Default");
        list.add("7%");
        list.add("10%");
        list.add("15%");
        list.add("20%");
        list.add("25%");
        list.add("30%");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_percent.setAdapter(dataAdapter);
    }

    public void addListenerOnInsPlu() {
        spinner_percent = (Spinner) findViewById(R.id.spinner_percent);
        install = (Button) findViewById(R.id.install);
        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Do you want to install?")
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                                switch (spinner_percent.getSelectedItemPosition()) {
                                    case 0:
                                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Please select percent")
                                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                    }
                                                })
                                                .create();
                                        dialog.show();
                                        break;
                                    case 1:
                                        downloadTask.execute(file_url_default);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                    case 2:
                                        downloadTask.execute(file_url_7);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                    case 3:
                                        downloadTask.execute(file_url_10);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                    case 4:
                                        downloadTask.execute(file_url_15);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                    case 5:
                                        downloadTask.execute(file_url_20);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                    case 6:
                                        downloadTask.execute(file_url_25);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                    case 7:
                                        downloadTask.execute(file_url_30);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                downloadTask.cancel(true); //cancel the task
                                            }
                                        });
                                        break;
                                }
                            }
                        }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create();
                dialog.show();
            }

        });
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        } else {
            Toast.makeText(MainActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private class GetVersionAOV extends AsyncTask<String, Void, String> {

        private Context context;

        public GetVersionAOV(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(file_url_version[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();

                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";

                        while ((line = bufferedReader.readLine()) != null)
                            versionText += line;
                    }
                    in.close();
                }

                return versionText;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }
            return versionText;

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                Toast.makeText(context,"Version AOV: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"Error: ", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getFileNameFromURL(String url) {
        if (url == null) {
            return "";
        }
        try {
            URL resource = new URL(url);
            String host = resource.getHost();
            if (host.length() > 0 && url.endsWith(host)) {
                // handle ...example.com
                return "";
            }
        }
        catch(MalformedURLException e) {
            return "";
        }

        int startIndex = url.lastIndexOf('/') + 1;
        int length = url.length();

        // find end index for ?
        int lastQMPos = url.lastIndexOf('?');
        if (lastQMPos == -1) {
            lastQMPos = length;
        }

        // find end index for #
        int lastHashPos = url.lastIndexOf('#');
        if (lastHashPos == -1) {
            lastHashPos = length;
        }

        // calculate the end index
        int endIndex = Math.min(lastQMPos, lastHashPos);
        return url.substring(startIndex, endIndex);
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                {
                    Toast.makeText(mContext, "Failed to ensure directory: " + dir.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

}