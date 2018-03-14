package com.example.czm.sxrecyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import czm.android.support.v7.widget.DividerItemDecoration;
import czm.android.support.v7.widget.LinearLayoutManager;
import czm.android.support.v7.widget.RecyclerView;
import czm.android.support.v7.widget.SXRecyclerView;

public class ItemClickActivity extends AppCompatActivity {


    SXRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (SXRecyclerView) findViewById(R.id.recyclerview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new Adapter(this));


        mRecyclerView.setOnItemClickListener(new SXRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                Toast.makeText(ItemClickActivity.this, "click " + position, Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView.setOnItemLongClickListener(new SXRecyclerView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
                Toast.makeText(ItemClickActivity.this, "long click " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }


    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        LayoutInflater mLayoutInflater;
        private final int TYPE_TITLE = 0;
        private final int TYPE_ITEM = 1;

        public Adapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_TITLE:
                    View titleview = mLayoutInflater.inflate(R.layout.item_title_layout, parent, false);
                    return new TitleViewHolder(titleview);
                case TYPE_ITEM:
                    View view = mLayoutInflater.inflate(R.layout.item_layout, parent, false);
                    return new MyViewHolder(view);
                    default:
                        return null;
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_TITLE){
                ((TitleViewHolder)holder).mTextView.setText("title");

            }else if (getItemViewType(position) == TYPE_ITEM){
                ((MyViewHolder)holder).mTextView.setText("item " + position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 5 == 0) {
                return TYPE_TITLE;
            }
            return TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return 100;
        }

        /**
         * TYPE_TITLE 类型的Item不需要响应点击、长按事件，故返回false
         * @param position
         * @return
         */
        @Override
        public boolean isEnable(int position) {
            if (getItemViewType(position) == TYPE_TITLE){
                return false;
            }
            return true;
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mTextView;
        ImageView mImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.img_check);
        }
    }


    class TitleViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public TitleViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.title_tv);
        }
    }

}
