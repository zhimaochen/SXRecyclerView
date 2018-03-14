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

public class MultiChoiceActivity extends AppCompatActivity {


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
                if (mRecyclerView.isInMutiChoiceState()){
                    Toast.makeText(MultiChoiceActivity.this, "该数据项不可选中", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MultiChoiceActivity.this, "click " + position, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRecyclerView.setOnItemLongClickListener(new SXRecyclerView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
                if (mRecyclerView.isInMutiChoiceState()){
                    Toast.makeText(MultiChoiceActivity.this, "该数据项不可选中", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MultiChoiceActivity.this, "long click " + position, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        //设置选择模式为多选模式
        mRecyclerView.setChoiceMode(SXRecyclerView.CHOICE_MODE_MULTIPLE);
        //设置多选模式监听器
        mRecyclerView.setMultiChoiceModeListener(new SXRecyclerView.MultiChoiceModeListener() {
            TextView textView;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int checkedItemCount = mRecyclerView.getCheckedItemCount();
                if (checkedItemCount == 0) {
                    textView.setText("选择数据项");
                } else {
                    textView.setText("选择了" + checkedItemCount + "项");
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                textView = new TextView(MultiChoiceActivity.this);
                textView.setGravity(Gravity.CENTER);
                textView.setText("选择数据项");
                textView.setTextSize(16);
                mode.setCustomView(textView);
                getMenuInflater().inflate(R.menu.recycler_multi, menu);
                //返回true才能正常创建ActionMode，从而启动多选模式
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.confirm) {
                    List<Integer> checkedPos = mRecyclerView.getCheckedItemPositions();
                    if (checkedPos == null || checkedPos.size() == 0) {
                        Toast.makeText(MultiChoiceActivity.this, "没有选中任何数据", Toast.LENGTH_SHORT).show();
                    } else {
                        String text = "";
                        for (int i = 0; i < checkedPos.size(); i++) {
                            text = text + checkedPos.get(i) + "  ";
                        }
                        Toast.makeText(MultiChoiceActivity.this, "选中的数据项有：" + text, Toast.LENGTH_SHORT).show();
                    }

                    //退出多选模式
                    mRecyclerView.finishMultiChoice();
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

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
            View view = mLayoutInflater.inflate(R.layout.item_layout, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.mTextView.setText("item " + position);

        }

        @Override
        public int getItemCount() {
            return 100;
        }

        //返回false的数据项不可被选中
        @Override
        public boolean isSelectable(int position) {
            if (position % 5 == 0) {
                return false;
            }
            return true;
        }
    }

    // 实现Checkable接口，可以简单直接地实现多选标记功能
    class MyViewHolder extends RecyclerView.ViewHolder implements Checkable {

        TextView mTextView;
        ImageView mImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.img_check);
        }

        //选中此ItemView时该方法会回调
        @Override
        public void setChecked(boolean checked) {
            if (checked) {
                mImageView.setVisibility(View.VISIBLE);
            } else {
                mImageView.setVisibility(View.GONE);
            }
        }


        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public void toggle() {

        }
    }
}
