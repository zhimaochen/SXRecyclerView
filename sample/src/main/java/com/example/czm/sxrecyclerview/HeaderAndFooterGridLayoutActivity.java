package com.example.czm.sxrecyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import czm.android.support.v7.widget.GridLayoutManager;
import czm.android.support.v7.widget.RecyclerView;
import czm.android.support.v7.widget.SXRecyclerView;

public class HeaderAndFooterGridLayoutActivity extends AppCompatActivity {


    SXRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (SXRecyclerView) findViewById(R.id.recyclerview);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(new Adapter(this));


        mRecyclerView.setOnItemClickListener(new SXRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                Toast.makeText(HeaderAndFooterGridLayoutActivity.this, "click " + position, Toast.LENGTH_SHORT).show();
            }
        });


        initHeaderAndFooter(mRecyclerView);


        mRecyclerView.setSelector(R.drawable.green_ripple_background);

    }

    class Adapter extends RecyclerView.Adapter<MyViewHolder> {

        LayoutInflater mLayoutInflater;

        public Adapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.grid_item_layout, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.mTextView.setText("item " + position);

        }

        @Override
        public int getItemCount() {
            return 50;
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.grid_item_tv);
        }
    }


    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }


    private void initHeaderAndFooter(final SXRecyclerView recyclerView) {
        View headerView = LayoutInflater.from(this).inflate(R.layout.recyclerview_header_view, null);
        // 将view包装成Viewholder
        final HeaderViewHolder header = new HeaderViewHolder(headerView);
        header.mTextView = (TextView) headerView.findViewById(R.id.txt_id);
        header.mTextView.setText("This is Header View 1");
        header.mTextView.setBackgroundColor(0xFFB0C4DE);
        header.mTextView.setOnClickListener(new View.OnClickListener() {
            boolean isClicked = false;

            @Override
            public void onClick(View v) {
                if (isClicked) {
                    recyclerView.removeHeaderView(header);
                } else {
                    isClicked = true;
                    Toast.makeText(HeaderAndFooterGridLayoutActivity.this, "click Header 1  再次点击删除此Header", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //添加此被包装过的headerview到SXRecyclerview中
        recyclerView.addHeaderView(header);


        //添加多个HeaderView时，需要重新创建view和Viewholder，不能使用已经创建过Headerview进行重复添加
        View headerView2 = LayoutInflater.from(this).inflate(R.layout.recyclerview_header_view, null);
        final HeaderViewHolder header2 = new HeaderViewHolder(headerView2);
        header2.mTextView = (TextView) headerView2.findViewById(R.id.txt_id);
        header2.mTextView.setText("This is Header View 2");
        header2.mTextView.setBackgroundColor(0xFFB0EE88);
        header2.mTextView.setOnClickListener(new View.OnClickListener() {
            boolean isClicked = false;

            @Override
            public void onClick(View v) {
                if (isClicked) {
                    recyclerView.removeHeaderView(header2);
                } else {
                    isClicked = true;
                    Toast.makeText(HeaderAndFooterGridLayoutActivity.this, "click Header 2  再次点击删除此Header", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.addHeaderView(header2);


        View footerView = LayoutInflater.from(this).inflate(R.layout.recyclerview_header_view, null);
        final HeaderViewHolder footer = new HeaderViewHolder(footerView);
        footer.mTextView = (TextView) footerView.findViewById(R.id.txt_id);
        footer.mTextView.setText("This is Footer View");
        footer.mTextView.setBackgroundColor(0xFF6495ED);
        footer.mTextView.setOnClickListener(new View.OnClickListener() {
            boolean isClicked = false;

            @Override
            public void onClick(View v) {
                if (isClicked) {
                    recyclerView.removeFooterView(footer);
                } else {
                    isClicked = true;
                    Toast.makeText(HeaderAndFooterGridLayoutActivity.this, "click Footer  再次点击删除此Footer", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.addFooterView(footer);
    }
}
