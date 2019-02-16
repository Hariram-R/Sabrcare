package com.sabrcare.app;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadDialogFragment extends DialogFragment {

    String filepath;

    TextView fileName;
    Button cancel,upload;

    public UploadDialogFragment() {
        // Required empty public constructor
    }

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
        fileName = view.findViewById(R.id.fileName);
        cancel = view.findViewById(R.id.cancelbtn);
        upload = view.findViewById(R.id.uploadbtn);
        String str2 = filepath.substring(filepath.lastIndexOf('/')+1);
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
                //TODO: Initiate upload function
                getActivity().getSupportFragmentManager().popBackStack();

            }
        });

        return view;
    }

}
