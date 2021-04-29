package com.de4thzone.modaov;

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
import android.net.NetworkInfo;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    // Progress Dialog
    private static Context mContext;
    ProgressDialog mProgressDialog, dialogIns;
    private Button dowload_plugins, install_plugins, delete_plugins;
    private Spinner spinner_percent;

    private static final int STORAGE_PERMISSION_CODE = 101;
    // File url to download
    private static String[] file_url = {
            "https://github.com/de4th-zone/mod-camera-arena-of-valor-server/raw/main/default/0.d4z",
            "https://github.com/de4th-zone/mod-camera-arena-of-valor-server/raw/main/20percent/1.d4z",
            "https://github.com/de4th-zone/mod-camera-arena-of-valor-server/raw/main/30percent/2.d4z",
            "https://github.com/de4th-zone/mod-camera-arena-of-valor-server/raw/main/version.d4z"
    };

    private String versionText = "";

    private static int VersionNow = 111;

    private static String[] urlCheckVersion = { "https://github.com/de4th-zone/mod-camera-arena-of-valor-server/raw/main/version-app.d4z" };

    private static String urlUpdateApp = "https://mega.nz/folder/C6YWwLjK#gdJjHF_OmmvQYJ1sa0sVQA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

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
        } else {
            setContentView(R.layout.activity_main);
            mContext = getApplicationContext();

            new checkNewVersionApp(MainActivity.this).execute(urlCheckVersion);

            TextView tl = (TextView) findViewById(R.id.text_link);
            tl.setMovementMethod(LinkMovementMethod.getInstance());

            TextView tlfb = (TextView) findViewById(R.id.text_link_fb);
            tlfb.setMovementMethod(LinkMovementMethod.getInstance());

            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Download and update plugins");
            mProgressDialog.setMessage("Downloading file ");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);

            dialogIns = new ProgressDialog(MainActivity.this);
            dialogIns.setTitle("Installing plugins");
            dialogIns.setIndeterminate(true);
            dialogIns.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialogIns.setCancelable(false);
            dialogIns.setCanceledOnTouchOutside(false);

            dowload_plugins = findViewById(R.id.dowload_plugins);
            delete_plugins = findViewById(R.id.delete_plugins);

            //get the spinner from the xml.
            addItemsOnSpinnerPercent();
            addListenerOnInsPlu();

            dowload_plugins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                } else {
                    final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        // Requesting the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                STORAGE_PERMISSION_CODE);
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Do you want to download plugins?")
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        downloadTask.execute(file_url);
                                        Toast.makeText(MainActivity.this,
                                                "Permission already granted",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            downloadTask.cancel(true); //cancel the task
                        }
                    });
                    mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadTask.cancel(true); //cancel the task
                        }
                    });
                }
            }
        });

            delete_plugins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    // Requesting the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            STORAGE_PERMISSION_CODE);
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Do you want to delete plugins?")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteRecursive(new File(mContext.getExternalFilesDir(null) + "/"));
                                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Delete plugins success")
                                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            })
                                            .create();
                                    dialog.show();
                                }
                            }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    dialog.show();
                }
            }
        });
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
            Toast.makeText(context,"Data: " + result, Toast.LENGTH_LONG).show();
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

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public void addItemsOnSpinnerPercent() {
        spinner_percent = (Spinner) findViewById(R.id.spinner_percent);
        List<String> list = new ArrayList<String>();
        list.add("Select percent");
        list.add("Default");
        list.add("20%");
        list.add("30%");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_percent.setAdapter(dataAdapter);
    }

    public void addListenerOnInsPlu() {
        spinner_percent = (Spinner) findViewById(R.id.spinner_percent);
        install_plugins = (Button) findViewById(R.id.install_plugins);
        install_plugins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File check_file_0 = new File(mContext.getExternalFilesDir(null) + "/0/0.d4z");
                File check_file_1 = new File(mContext.getExternalFilesDir(null) + "/1/1.d4z");
                File check_file_2 = new File(mContext.getExternalFilesDir(null) + "/2/2.d4z");
                File check_file_3 = new File(mContext.getExternalFilesDir(null) + "/3/3.d4z");
                if (!check_file_0.exists() || !check_file_1.exists() || !check_file_2.exists() || !check_file_3.exists()) {
                    AlertDialog dialoggg = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Please download plugins")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    dialoggg.show();
                    return;
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Do you want to install plugins?")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File version_text = new File(mContext.getExternalFilesDir(null) + "/3/3.d4z");
                                    try {
                                        BufferedReader br = new BufferedReader(new FileReader(version_text));
                                        String strLine;
                                        while ((strLine = br.readLine()) != null) {
                                            versionText = strLine;
                                        }
                                        br.close();
                                        Toast.makeText(MainActivity.this, versionText, Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    String[] dir_copy1 = {
                                            "/0/0.d4z",
                                            "/Android/data/com.garena.game.kgvn/files/Resources/" + versionText + "/AssetBundle/Scene/0.d4z"
                                    };
                                    String[] dir_copy2 = {
                                            "/1/1.d4z",
                                            "/Android/data/com.garena.game.kgvn/files/Resources/" + versionText + "/AssetBundle/Scene/1.d4z"
                                    };
                                    String[] dir_copy3 = {
                                            "/2/2.d4z",
                                            "/Android/data/com.garena.game.kgvn/files/Resources/" + versionText + "/AssetBundle/Scene/2.d4z"
                                    };
                                    final CopyandList copyfile = new CopyandList(MainActivity.this);
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
                                            copyfile.execute(dir_copy1);
                                            break;
                                        case 2:
                                            copyfile.execute(dir_copy2);
                                            break;
                                        case 3:
                                            copyfile.execute(dir_copy3);
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
            }
        });
    }

    // Function to check and request permission.
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

                        File sdCardRoot = new File(mContext.getExternalFilesDir(null) + "/", String.valueOf(i));
                        if (!sdCardRoot.exists()) {
                            sdCardRoot.mkdirs();
                        }
                        output = new FileOutputStream(mContext.getExternalFilesDir(null).toString() + "/" + i + "/" + i + ".d4z");

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
                mProgressDialog.setMessage("Downloading file " + count + " / " + file_url.length);
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
                    Toast.makeText(context,"Download error: " + result, Toast.LENGTH_LONG).show();
                else {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Download success")
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create();
                    dialog.show();
                    Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                }
            }
    }

    private class CopyandList extends AsyncTask<String, Integer, String> {
        String[] renameTo = {
                "/Android/data/com.garena.game.kgvn/files/Resources/" + versionText + "/AssetBundle/Scene/Scene_DESIGN_PVPTEST.assetbundle"
        };
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        public CopyandList(Context context) {
            this.context = context;
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
            dialogIns.show();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            dialogIns.setIndeterminate(false);
            dialogIns.setMax(100);
            dialogIns.setProgress(progress[0]);
        }
        @Override
        protected String doInBackground(String... inout) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(mContext.getExternalFilesDir(null).toString() + inout[0]);
                out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + inout[1]);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                // write the output file (You have now copied the file)
                out.flush();
                out.close();
                out = null;

                File from = new File(Environment.getExternalStorageDirectory().toString() + inout[1]);
                File to = new File(Environment.getExternalStorageDirectory() + renameTo[0]);
                from.renameTo(to);

            } catch (FileNotFoundException e) {
                return e.toString();
            } catch (IOException e) {
                return e.toString();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            mWakeLock.release();
            dialogIns.dismiss();
            if (result != null)
                Toast.makeText(context,"Install error: " + result, Toast.LENGTH_LONG).show();
            else {
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
}