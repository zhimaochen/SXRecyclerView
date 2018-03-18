package com.example.czm.sxrecyclerview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import czm.android.support.v7.widget.DividerItemDecoration;
import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.RecyclerView;
import czm.android.support.v7.widget.SXRecyclerView;

public class MainActivity extends AppCompatActivity {


    SXRecyclerView mRecyclerView;
    List<DataBin> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (SXRecyclerView) findViewById(R.id.recyclerview);

        initData();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new Adapter(this));


        mRecyclerView.setOnItemClickListener(new SXRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                DataBin dataBin = mData.get(position);
                Intent intent = new Intent(MainActivity.this, dataBin.mClass);
                startActivity(intent);
            }
        });

    }

    class Adapter extends RecyclerView.Adapter<MyViewHolder> {

        LayoutInflater mLayoutInflater;

        public Adapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.main_item_layout, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            DataBin dataBin = mData.get(position);
            holder.mTextView.setText(dataBin.mDescribe);

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.main_item_tv);
        }
    }

    private void initData() {
        mData = new ArrayList<>();
        mData.add(new DataBin("ItemClick", ItemClickActivity.class));
        mData.add(new DataBin("MultiChoice", MultiChoiceActivity.class));
        mData.add(new DataBin("Header And Footer", HeaderAndFooterActivity.class));
        mData.add(new DataBin("Header And Footer GridLayout", HeaderAndFooterGridLayoutActivity.class));
        mData.add(new DataBin("Header And Footer StaggeredGridLayout", HeaderAndFooterStaggeredGridLayoutActivity.class));
        mData.add(new DataBin("PinnedHeader", PinnedHeaderActivity.class));
    }

    class DataBin {
        public String mDescribe;
        public Class mClass;

        public DataBin(String describe, Class aClass) {
            mDescribe = describe;
            mClass = aClass;
        }
    }

}
