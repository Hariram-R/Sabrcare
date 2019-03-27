package com.sabrcare.app.records;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.onecode.s3.callback.S3Callback;
import com.onecode.s3.model.S3Credentials;
import com.sabrcare.app.R;
import com.sabrcare.app.Remedley;

import java.io.File;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadDialogFragment extends DialogFragment {

    public String filepath;
    private RequestQueue requestQueue;
    Map<String,String> recordsMap = new ArrayMap<>();
    static String filename;


    public static UploadDialogFragment newInstance(String filepath){
        UploadDialogFragment uploadDialogFragment = new UploadDialogFragment();
        // NOTE: args in bundle not referred to, filepath is set in ReportFolderActivity. Remove if needed.
        Bundle args = new Bundle();
        args.putString("filePath",filepath);
        uploadDialogFragment.setArguments(args);
        return uploadDialogFragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_upload_dialog,null);
        final TextView fileName = view.findViewById(R.id.fileName);
        Button cancel = view.findViewById(R.id.cancelbtn);
        Button upload = view.findViewById(R.id.uploadbtn);
        String str2 = filepath.substring(filepath.lastIndexOf('/')+1);
        filename=str2;
        fileName.setText(str2);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Rename file function
                uploadS3Client("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiaGFyaS4yNTk5QGdtYWlsLmNvLmluIiwiZXhwIjoxNTU0Mjk4OTUyfQ.qy7W-tdcSVGrEoZrNialM4VFURvX3UJ9o6Ifde5HN6s"
                        ,filepath,filename);
                getActivity().getSupportFragmentManager().popBackStack();

            }
        });

        return view;
    }

    public void uploadS3Client(final String token, String path, final String name){

        AmazonS3 s3client = new AmazonS3Client(Remedley.credentials);
        TransferUtility transferUtility = new TransferUtility(s3client, getContext());
        final TransferObserver observer = transferUtility.upload(
                "remedley-user-data",  //this is the bucket name on S3
                token+"_"+name, //this is the path and name
                new File(path)); //path to the file locally
                //CannedAccessControlList.PublicRead //to make the file public

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {
                    Log.e("Upload","SUCCESSSSSSSSSS");
                    Toast.makeText(getContext(),"Upload succesful",Toast.LENGTH_SHORT).show();
                    uploadFile("https://s3-ap-south-1.amazonaws.com/remedley-user-data/"+token+"_"+name);
                } else if (state.equals(TransferState.FAILED)) {
                    Toast.makeText(getContext(),"Upload failed. Please try again",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.e("Upload",(bytesCurrent/bytesTotal)*100+"");
            }
            @Override
            public void onError(int id, Exception ex) {
                Log.e("uploadErr",ex.getLocalizedMessage());
            }
        });
    }

    void uploadFile(String finalUrl){


        String uploadUrl = getResources().getString(R.string.apiUrl) + "records/add/files";

        recordsMap.put("token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiaGFyaS4yNTk5QGdtYWlsLmNvLmluIiwiZXhwIjoxNTU0Mjk4OTUyfQ.qy7W-tdcSVGrEoZrNialM4VFURvX3UJ9o6Ifde5HN6s");
        recordsMap.put("folderName",ReportFolderActivity.folderName);
        if(ReportFolderActivity.isImage){
            recordsMap.put("fileType","Image");
        } else {
            recordsMap.put("fileType","PDF");
        }
        recordsMap.put("fileName",filename);
        recordsMap.put("fileURL",finalUrl);

        requestQueue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
          @Override
          public Map<String,String> getHeaders(){
              return recordsMap;
          }
        };
        requestQueue.add(stringRequest);

    }

    //("token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiaGFyaS4yNTk5QGdtYWlsLmNvLmluIiwiZXhwIjoxNTU0Mjk4OTUyfQ.qy7W-tdcSVGrEoZrNialM4VFURvX3UJ9o6Ifde5HN6s");
        }
