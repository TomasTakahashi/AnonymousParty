package com.taka.anonymousparty.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.taka.anonymousparty.R;
import com.taka.anonymousparty.providers.UsersProvider;

public class AboutActivity extends AppCompatActivity {
    private UsersProvider mUsersProvider;

    private View mActionBarView;
    private ImageView mImageViewBack;
    private TextView mTextViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        showCustomToolbar(R.layout.custom_back_toolbar);

        mUsersProvider.updateOnline(true, AboutActivity.this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        mUsersProvider.updateOnline(true, AboutActivity.this);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mUsersProvider.updateOnline(false, AboutActivity.this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void showCustomToolbar(int resource) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(resource, null);
        actionBar.setCustomView(mActionBarView);
        mImageViewBack = mActionBarView.findViewById(R.id.imageViewBack);
        mTextViewTitle = mActionBarView.findViewById(R.id.textViewTitle);
        mTextViewTitle.setText("About");

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}