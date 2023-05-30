package com.taka.anonymousparty.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.activities.ChatActivity;
import com.taka.anonymousparty.activities.RegisterActivity;

public class HomeFragment extends Fragment {

    View mView;
    FloatingActionButton mFab;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mFab = mView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNewChat();
            }
        });
        return mView;
    }

    private void goToNewChat() {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        startActivity(intent);
    }
}