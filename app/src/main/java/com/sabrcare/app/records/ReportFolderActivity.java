package com.sabrcare.app.records;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.sabrcare.app.PathUtil;
import com.sabrcare.app.R;
import com.sabrcare.app.auth.SignInActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class ReportFolderActivity extends AppCompatActivity {

    RecyclerView imgList;
    RecyclerView pdfList;
    ArrayList<String> imageName;
    ArrayList<String> imageURL;
    ArrayList<String> pdfName;
    ArrayList<String> pdfURL;

    private boolean showUploadDialog;
    private int imgFlag;

    public FloatingActionButton photoFab,uploadFileFab;
    public FloatingActionMenu fabMenu;

    private String filePath;

    static String folderName = "";
    static boolean isImage;

    private static final int RC_CAMERA_STORAGE = 123;
    private static final int RC_READ_STORAGE = 120;
    private static final int PICK_IMAGE = 100;
    private static final int PICK_PDF = 1;

    Map<String,String> fileHeaders = new ArrayMap<>();
    private RequestQueue listFiles;

    ImageRecordsAdapter imageRecordsAdapter;
    FileRecordsAdapter fileRecordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_folder);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("folderName"));
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportFolderActivity.this.finish();
            }
        });

        folderName=getIntent().getStringExtra("folderName");
        imgList = findViewById(R.id.ImagesRV);
        pdfList = findViewById(R.id.pdfsRV);
        photoFab = findViewById(R.id.TakePhoto);
        uploadFileFab = findViewById(R.id.UploadFile);
        fabMenu = findViewById(R.id.material_design_android_floating_action_menu2);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(this,3);
        imgList.setLayoutManager(gridLayoutManager);
        pdfList.setLayoutManager(gridLayoutManager2);

        loadFiles();

        final String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        photoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            @AfterPermissionGranted(RC_CAMERA_STORAGE)
            public void onClick(View view) {
                if(EasyPermissions.hasPermissions(ReportFolderActivity.this,perms)){
                   ImagePicker.create(ReportFolderActivity.this)
                            .folderMode(true)
                            .toolbarArrowColor(Color.parseColor("#FFFFFF"))
                            .limit(1)
                            .returnMode(ReturnMode.CAMERA_ONLY)
                            .toolbarFolderTitle("Select Image to upload")
                            .showCamera(true)
                            .single()
                            .imageDirectory("Remedley Records")
                            .theme(R.style.AppThemeNoActionBar)
                            .start(PICK_IMAGE);


                }else {
                    EasyPermissions.requestPermissions(new PermissionRequest.Builder(ReportFolderActivity.this,RC_CAMERA_STORAGE,perms)
                            .setRationale(R.string.rationale_camera_storage)
                            .build());
                }
            }
        });

        uploadFileFab.setOnClickListener( new View.OnClickListener() {
            @Override
            @AfterPermissionGranted(RC_READ_STORAGE)
            public void onClick(View view) {
                if(EasyPermissions.hasPermissions(ReportFolderActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    fileIntent.setType("application/pdf");
                    startActivityForResult(Intent.createChooser(fileIntent,"Select PDF"),PICK_PDF);

                } else {
                    EasyPermissions.requestPermissions(new PermissionRequest.Builder(ReportFolderActivity.this,RC_READ_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            .setRationale(R.string.rationale_read_storage)
                            .build());
                }
            }
        });
    }


    void loadFiles()    {
        String baseUrl = getResources().getString(R.string.apiUrl);
        String filesURL = baseUrl+"records/show/files";
        //TODO Maintain auth token
        fileHeaders.put("token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiaGFyaS4yNTk5QGdtYWlsLmNvLmluIiwiZXhwIjoxNTU0Mjk4OTUyfQ.qy7W-tdcSVGrEoZrNialM4VFURvX3UJ9o6Ifde5HN6s");
        fileHeaders.put("folderName",folderName);

        listFiles = Volley.newRequestQueue(this);
        StringRequest getfiles = new StringRequest(Request.Method.GET, filesURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                imageName=new ArrayList<>();
                imageURL = new ArrayList<>();
                pdfName = new ArrayList<>();
                pdfURL = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if(jsonObject.getString("recordsFileType").equals("Image")){
                            imageName.add(jsonObject.getString("recordsName"));
                            imageURL.add(jsonObject.getString("recordsURL"));
                        }
                        else{
                            pdfName.add(jsonObject.getString("recordsName"));
                            pdfURL.add(jsonObject.getString("recordsURL"));
                        }

                    }
                } catch (JSONException e) {
                    Log.e("RecordsError",e.toString());
                }

                imageRecordsAdapter = new ImageRecordsAdapter(imageName,imageURL,ReportFolderActivity.this);
                fileRecordsAdapter = new FileRecordsAdapter(pdfName,pdfURL,ReportFolderActivity.this);
                imgList.setAdapter(imageRecordsAdapter);
                pdfList.setAdapter(fileRecordsAdapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("LoadError",error.toString());
                Toast.makeText(ReportFolderActivity.this,"File loading error. Please try again",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String,String> getHeaders(){
                return fileHeaders;
            }
        };
        listFiles.add(getfiles);
        //header--> token,folderName
        //add hardcode fetch url

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        fabMenu.close(false);

        if(requestCode==PICK_IMAGE && resultCode == RESULT_OK && data != null){
            com.esafirm.imagepicker.model.Image image = com.esafirm.imagepicker.features.ImagePicker.getFirstImageOrNull(data);
            filePath = image.getPath();
            showUploadDialog = true;
            isImage=true;
            imgFlag=1;
        }

        else if(requestCode==PICK_PDF&& resultCode == RESULT_OK && data!=null) {
            filePath = PathUtil.getPath(this,data.getData());
            //ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
            //filePath=files.get(0).getPath();
            System.out.println("data>>>>>>>"+data.getData());
            System.out.println("filepath>>>>>"+filePath);
            showUploadDialog = true;
            isImage=false;
            imgFlag=0;
        }
        else {
            Toast.makeText(getApplicationContext(),"Error loading file. Please try again",Toast.LENGTH_SHORT).show();
            showUploadDialog=false;
            imgFlag=0;
        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if(showUploadDialog && imgFlag==0){
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction ft = fm.beginTransaction();
            DialogFragment uploadDialog = UploadDialogFragment.newInstance(filePath);
            ((UploadDialogFragment) uploadDialog).filepath=filePath;
            ft.addToBackStack("Upload Fragment Dialog");

            uploadDialog.show(ft,"");

            showUploadDialog = false;
        }
        else if(showUploadDialog && imgFlag==1){
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction ft = fm.beginTransaction();

            DialogFragment uploadDialog = UploadDialogFragment.newInstance(filePath);
            ((UploadDialogFragment) uploadDialog).filepath=filePath;
            ft.addToBackStack("Upload Fragment Dialog");

            uploadDialog.show(ft,"");

            showUploadDialog = false;
        }
    }
}

//eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiaGFyaS4yNTk5QGdtYWlsLmNvLmluIiwiZXhwIjoxNTU0Mjk4OTUyfQ.qy7W-tdcSVGrEoZrNialM4VFURvX3UJ9o6Ifde5HN6s
//TODO Load files
