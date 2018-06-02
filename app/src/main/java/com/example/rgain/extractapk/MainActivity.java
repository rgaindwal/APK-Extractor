package com.example.rgain.extractapk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;

    androidx.appcompat.widget.Toolbar toolbar;
    private boolean isSearchOpened;
    private EditText edtSeach;
    private ImageButton btnSearch;

    RecyclerView recView;
    MyAdapter adapter;

    ArrayList<listItemModel> res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isSearchOpened = FALSE;

        edtSeach = findViewById(R.id.editTextSearch);
        btnSearch = findViewById(R.id.btnSearch);
        ImageButton btnMore = findViewById(R.id.btnMore);

        recView = findViewById(R.id.recView);
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(this));


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSearchOpened ){
                    isSearchOpened = TRUE;
                    edtSeach.setVisibility(View.VISIBLE);
                    edtSeach.requestFocus();
                    btnSearch.setImageResource(R.drawable.ic_baseline_close_24px);
                }

                else{
                    isSearchOpened = FALSE;
                    edtSeach.setVisibility(View.INVISIBLE);
                    btnSearch.setImageResource(R.drawable.ic_search_black_48px);
                }
            }
        });

        edtSeach.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });


        List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);

        res = new ArrayList<>();
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

    private void filter(String editable){
        ArrayList<listItemModel> filteredList = new ArrayList<>();
        for(listItemModel item : res){
            if(item.getAppname().toLowerCase().contains(editable.toLowerCase())){
                filteredList.add(item);
            }
        }

        adapter.filterList(filteredList);
    }


    private void createDialog(int position, String name) {
        final int clickedPosition = position;
        final String clickedName = name;
        Toast.makeText(this, "Yay", Toast.LENGTH_SHORT).show();

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        LayoutInflater inflater = this.getLayoutInflater();

        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog, null);
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
            requestGranted = 1;
        } else {
            requestGranted = requestStoragePermission();
        }

        if (requestGranted == 1) {
            int i = extractFileToExternal(clickedPosition, clickedName);

        } else {
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
                            granted[0] = 1;
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
            granted[0] = 1;
        }
        if (granted[0] == 1)
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
            Log.d("1", "extractFileToExternal: " + packageName3);
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
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}




