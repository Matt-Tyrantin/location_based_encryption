package com.example.location_basedencryption;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<String>
{
    private Context mContext;
    private int id;
    private List<String> items;

    public FileListAdapter(Context context, int textViewResourceId , List<String> list)
    {
        super(context, textViewResourceId, list);
        mContext = context;
        id = textViewResourceId;
        items = list;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        View mView = v;

        if(mView == null){
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = mView.findViewById(android.R.id.text1);
        TextView descText = mView.findViewById(android.R.id.text2);

        String filePath = items.get(position);
        if(filePath != null )
        {
            text.setText(items.get(position));

            if (new File(filePath).isDirectory()) {
                descText.setText("Directory");
            }
        }

        return mView;
    }
}
