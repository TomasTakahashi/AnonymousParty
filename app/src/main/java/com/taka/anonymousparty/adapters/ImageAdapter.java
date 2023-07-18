package com.taka.anonymousparty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.models.Image;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private ArrayList<Image> dataList;
    private Context context;
    LayoutInflater layoutInflater;

    public ImageAdapter(Context context, ArrayList<Image> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @Override
    public int getCount() {
        return dataList.size();
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (layoutInflater == null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null){
            view = layoutInflater.inflate(R.layout.item_image, null);
        }
        ImageView gridImage = view.findViewById(R.id.gridImage);
        Glide.with(context).load(dataList.get(i).getImageURL()).into(gridImage);
        return view;
    }
}