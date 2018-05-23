package com.example.rgain.extractapk;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;

    android.support.v7.widget.Toolbar toolbar;
    private MenuItem mSearchAction;
    MenuItem mSettings;
    private boolean isSearchOpened = false;
    private EditText edtSeach;

    RecyclerView recView;
    MyAdapter adapter;
    List<ApplicationInfo> installedApps;
    PackageManager pm;
    List<ApplicationInfo> apps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);
        recView = findViewById(R.id.recView);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(this));

        List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);

        ArrayList<listItemModel> res = new ArrayList<>();
        listItemModel newInfo;

        for (int i = 0; i < apps.size(); i++) {
            PackageInfo p = apps.get(i);

            if (!isSystemPackage(p)) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                String pName = p.packageName;
                String versionName = p.versionName;
                int versionCode = p.versionCode;
                Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
                String fileName = p.applicationInfo.publicSourceDir;
//                Log.d("FILENAME ", "onCreate: " +i + " "+  fileName );
                newInfo = new listItemModel(appName, pName, versionName, versionCode, icon, fileName);
                res.add(newInfo);
            }
        }

        Collections.sort(res, new Comparator<listItemModel>() {
            @Override
            public int compare(listItemModel o1, listItemModel o2) {
                String name1 = o1.getAppname();
                String name2 = o2.getAppname();
                return name1.compareToIgnoreCase(name2);
            }
        });

        adapter = new MyAdapter(res, this, res);
        recView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recView.getContext(), 1);
        recView.addItemDecoration(dividerItemDecoration);


        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String name) {
                createDialog(position, name);
            }
        });
    }


    private void createDialog(int position, String name) {
        final int clickedPosition = position;
        final String clickedName = name;
        Toast.makeText(this, "Yay", Toast.LENGTH_SHORT).show();

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog, null);
        dialogBuilder.setView(dialogView);

        final CardView extract = dialogView.findViewById(R.id.cardExtract);
        final CardView info = dialogView.findViewById(R.id.cardShowInfo);

        AlertDialog b = dialogBuilder.create();
        b.show();

        extract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Onclick listener
                extractFile(clickedPosition, clickedName);

            }
        });
    }

    private void extractFile(int clickedPosition, String clickedName) {
        //Extract apk
        int requestGranted = -1;
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permission Already  Granted", Toast.LENGTH_SHORT).show();
            requestGranted=1;
        } else {
            requestGranted = requestStoragePermission();
        }

        if(requestGranted==1){
            int i= extractFileToExternal(clickedPosition,clickedName);

        }
        else{
            Toast.makeText(MainActivity.this, "No permission", Toast.LENGTH_SHORT).show();
        }
    }

    private int requestStoragePermission() {
        final int[] granted = new int[1];

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is needed to write the apk file to the external Storage")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            granted[0] =1;
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            granted[0] =1;
        }
        if(granted[0] == 1)
            return 1;
        else
            return 0;
    }

    int extractFileToExternal(int clickedPosition, String clickedPackageName) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        //TODO: external storage
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File savepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        InputStream in;
        OutputStream outputStream;
        //FileOutputStream outputStream;

        for (ResolveInfo info : apps) {
            String packageName3 = info.activityInfo.packageName;
            Log.d("1", "extractFileToExternal: "  + packageName3);
            Log.d("2", "Clicked: " + clickedPackageName);
            if (packageName3.equals(clickedPackageName)) {
                try {
                    String label = info.activityInfo.applicationInfo.
                            loadLabel(getPackageManager()).toString();

                    File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
                    in = new FileInputStream(file);
                    String fileOutPutPath = savepath.toString() + "/" + label + ".apk";
                    outputStream = new FileOutputStream
                            (fileOutPutPath);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    in.close();
                    outputStream.close();
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    return 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    return 1;
                }
            }
        }
        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        return 0;
    }



    private boolean isSystemPackage(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}




