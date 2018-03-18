package com.example.czm.sxrecyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import czm.android.support.v7.widget.DividerItemDecoration;
import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.PinnedHeader.RecyclerPinnedHeaderAdapter;
import czm.android.support.v7.widget.PinnedHeader.RecyclerPinnedHeaderDecoration;
import czm.android.support.v7.widget.PinnedHeader.RecyclerPinnedHeaderTouchListener;
import czm.android.support.v7.widget.RecyclerView;
import czm.android.support.v7.widget.SXRecyclerView;

public class PinnedHeaderActivity extends AppCompatActivity {


    SXRecyclerView mRecyclerView;

    String[] mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (SXRecyclerView) findViewById(R.id.recyclerview);


        mData = getRandArrays();
        Arrays.sort(mData);

        MyAdapter myAdapter = new MyAdapter(this);
        mRecyclerView.setAdapter(myAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //创建RecyclerPinnedHeaderDecoration
        final RecyclerPinnedHeaderDecoration headersDecor = new RecyclerPinnedHeaderDecoration(myAdapter);
        //可以通过对RecyclerPinnedHeaderDecoration设置监听器，来实现对切换PinnedHeader的监听，从而可以实现一些特殊需求
        headersDecor.setPinnedHeaderListener(new RecyclerPinnedHeaderDecoration.OnPinnedHeaderChangeListener() {
            @Override
            public void OnPinnedHeaderChange(RecyclerView recyclerView, View currentHeader, int currentPosition, long currentHeaderId, View lastHeader, int lastPosition, long lastHeaderId) {
                ((ImageView) currentHeader.findViewById(R.id.img)).setImageResource(R.drawable.triangle_down);
                ((ImageView) lastHeader.findViewById(R.id.img)).setImageResource(R.drawable.arrow_right);
            }
        });
        //将创建RecyclerPinnedHeaderDecoration作为一个ItemDecoration添加到Recyclerview中，从而实现将PinnedHeader绘制到Recyclerview中
        mRecyclerView.addItemDecoration(headersDecor);

        //可以通过RecyclerPinnedHeaderTouchListener设置点击PinnedHeader时的回调
        RecyclerPinnedHeaderTouchListener touchListener = new RecyclerPinnedHeaderTouchListener(mRecyclerView, headersDecor);
        touchListener.setOnHeaderClickListener(new RecyclerPinnedHeaderTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View header, int position, long headerId, MotionEvent e) {
                Toast.makeText(PinnedHeaderActivity.this, "click PinnedHeader " + mData[position].substring(0, 1), Toast.LENGTH_SHORT).show();
            }
        });
        //记得将RecyclerPinnedHeaderTouchListener添加到Recyclerview中才会生效
        mRecyclerView.addOnItemTouchListener(touchListener);

        mRecyclerView.setOnItemClickListener(new SXRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                Toast.makeText(PinnedHeaderActivity.this, "click " + position, Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    private String[] getRandArrays() {
        String[] arrays = new String[100];
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = ((char) ('A' + r.nextInt('Z' - 'A'))) + " " + Integer.toString(i);
        }
        return arrays;
    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerPinnedHeaderAdapter<RecyclerView.ViewHolder> {

        LayoutInflater mLayoutInflater;

        public MyAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.item_layout, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                ((MyViewHolder) holder).mTextView.setText(mData[position]);
            }

        }

        //通过这个方法对数据进行分组，返回同一个headerId的itemview将被归属于同一个PinnedHeader
        @Override
        public long getHeaderId(int position) {
            return mData[position].charAt(0);
        }

        // 创建PinnedHeader布局
        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = mLayoutInflater
                    .inflate(R.layout.recyclerview_pinneadheader_view, parent, false);
            return new PinneadHeaderViewHolder(view);
        }

        // 对PinnedHeader进行数据绑定
        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PinneadHeaderViewHolder) {
                String text = String.valueOf(mData[position].charAt(0));
                ((PinneadHeaderViewHolder) holder).mTextView.setText(text);
            }

        }

        @Override
        public int getItemCount() {
            return mData.length;
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;
        ImageView mImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.img_check);
        }
    }

    class PinneadHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView mImageView;

        public PinneadHeaderViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_pinnead_header);
            mImageView = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
