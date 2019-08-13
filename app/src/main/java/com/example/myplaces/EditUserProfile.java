package com.example.myplaces;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class EditUserProfile  extends Fragment {

    public static EditText nameFull,phonenumber;
    public static ImageView myImage ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edituserprofile,container,false);

        myImage = (ImageView)view.findViewById(R.id.profilePhoto);

        Drawable drawable = UserProfile.profilePhoto.getDrawable();
        if(drawable != null){
            myImage.setImageDrawable(drawable);
        }

        nameFull = (EditText) view.findViewById(R.id.fullNameTextView);
        if (!UserProfile.name.getText().toString().equals(UserProfile.defName)) {
            nameFull.setText(UserProfile.name.getText().toString());
        }

        phonenumber = (EditText)view.findViewById(R.id.phoneNumberTextView);

        if (!UserProfile.phone.getText().toString().equals(UserProfile.defNum)) {
            phonenumber.setText(UserProfile.phone.getText().toString());
        }
        return view;
    }
}
