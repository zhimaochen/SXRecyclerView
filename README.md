# SXRecyclerView
对RecyclerView进行封装，实现了触摸反馈、点击、长按、多选、头尾部、悬浮头部等功能

## 简介

前段时间封装了一个RecyclerView相关的库，叫做SXRecyclerView。该库包含了很多常用的功能，包括**触摸反馈效果、点击事件、长按事件、多选功能、HeaderAndFooter、PinnedHeader**等等。在做应用开发的时候直接使用这样一个组件能很快的实现相关功能，提高开发的效率。

SXRecyclerView的实现并不是直接引入原生的RecyclerView，而是抽取整个RecyclerView库相关的源码，在原生源码基础上进行封装的。这样做的好处一是打破权限的限制，便于功能的封装与定制化；二是能对整个库进行完全的掌控，避免由于导入不同版本导致兼容性问题。

SXRecyclerView需要依赖support-v4包，如果导入SXRecyclerView的工程中也引入v4包，最好保证**引入的v4包是25.3.1及以上版本**，不然有可能导致编译出错。


下面说下该库的功能点及使用方式。

## 触摸反馈效果

SXRecyclerView已经实现了触摸反馈效果，默认进行点击、长按操作都会有水波纹动画。如果想自定义触摸反馈效果，可以通过以下方法来设置：

```java
public void setSelector(Drawable sel)
public void setSelector(int resID)

```

如果想去掉触摸反馈效果，可以通过`mRecyclerView.setSelector(null);`这样来设置。

## 点击事件和长按事件

原生的RecyclerView没有提供ItemView点击和长按功能，通常的做法是在Adapter中进行数据绑定时对每个ViewHolder对应的view设置`OnClickListener`、`OnLongClickListener`来实现相应的功能。现在不需要这样麻烦了。SXRecyclerView这个类在RecyclerView的基础上中实现了该功能，并提供了事件回调，调用代码如下:

```java
//点击事件 
mRecyclerView.setOnItemClickListener(new SXRecyclerView.OnItemClickListener() {
       @Override
       public void onItemClick(RecyclerView parent, View view, int position, long id) {
          //do something
       }
});


//长按事件
mRecyclerView.setOnItemLongClickListener(new SXRecyclerView.OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(RecyclerView parent, View view, int position, long id){
         //do something
        return true;
    }
});

```

默认情况下，设置了这两个监听器之后，SXRecyclerView会对每个ItemView都响应操作。如果想让某个ItemView不响应点击、长按事件，可以通过重写Adapter的`isEnable(int position)`方法来实现：

```java
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
```

原生RecyclerView没有该方法，此方法是SXRecyclerView添加的。此方法返回false就代表对应位置的ItemView不启用SXRecyclerView相关功能，也就是说不响应点击事件、长按事件、触摸反馈、多选功能等。该方法默认返回true。

示意图：
![click.gif](https://upload-images.jianshu.io/upload_images/148381-7f3de273ec0d29d7.gif?imageMogr2/auto-orient/strip)

## 多选功能

要使用多选功能，有两个必要的步骤：

1、设置多选模式，多选模式如下：

- **CHOICE_MODE_NONE** ： 不启动多选功能。默认值为这种。
- **CHOICE_MODE_MULTIPLE** ：主动触发启动多选功能。触发方式有长按和主动调用`startMultiChoice()`方法两种触发方式。

2、设置选择模式监听器，并根据需求实现相关的方法与ActionMode进行交互。代码如下：

```java
//设置选择模式为多选模式
mRecyclerView.setChoiceMode(SXRecyclerView.CHOICE_MODE_MULTIPLE);
//设置多选模式监听器
mRecyclerView.setMultiChoiceModeListener(new SXRecyclerView.MultiChoiceModeListener() {
    
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }
});

```

这里的多选模式是与ActionMode进行绑定交互的，具体的实现可以参考下demo。

默认情况下，所有的ItemView都是可以被选中的。如果要让某些项不可选中，除了前文提到的重写Adaper中的`isEnable(int position)`方法让对应位置的ItemView失效。还可以通过Adapter的另外一个方法来设置：

```java
        //返回false的数据项不可被选中
        @Override
        public boolean isSelectable(int position) {
            if (position % 5 == 0) {
                return false;
            }
            return true;
        }
```

不同于`isEnable(int position)`方法，`isSelectable(int position)`方法只作用于多选功能，返回false的ItemView只是不能被选中，包括点击、长按事件在内的其他功能都能响应。

一般使用多选功能都会在选中ItemView的时候对相应的ItemView进行标记，比如打个勾之类的操作。为了更简单直接地实现选中标记操作，SXRecyclerView提供了一种实现方式，就是对ViewHolder实现`Checkable`接口，这样在选中状态发生改变时，`setChecked(boolean checked)`方法都会被调用，我们就可以在该方法中对ItemView为所欲为了。

```java
    // 实现Checkable接口，可以简单直接地实现多选标记功能
    class MyViewHolder extends RecyclerView.ViewHolder implements Checkable {

        TextView mTextView;
        ImageView mImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.img_check);
        }

        //此ItemView选中状态发生改变时该方法会回调
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
```

**tip ：**多选功能的相关方法如下:

- `public boolean startMultiChoice()`：进入多选状态
- `public void finishMultiChoice()`：退出多选状态
- `public Boolean isInMutiChoiceState()`：判断SXRecyclerview是否处于多选状态
- `public void setItemChecked(int position, boolean value)`：设置某项的选中状态
- `public void clearChoices()`：清除所有的选中状态
- `public long[] getCheckedItemIds()`：获取当前选中的项的id集合
- `public List<Integer> getCheckedItemPositions()`：获取当前选中的项的position集合
- `public boolean isItemChecked(int position)`：判断某项是否选中
- `public int getCheckedItemCount()`：获取选中项总数
- `public void checkedAll()`：全选
- `public void unCheckedAll()`：全不选

示意图：
![multichoice.gif](https://upload-images.jianshu.io/upload_images/148381-814ff981610d2192.gif?imageMogr2/auto-orient/strip)


## PinnedHeader

PinnedHeader是指对ViewHolder进行分组,每个组都有一个Header,在滑动过程中，处于顶部的组的Header都会悬浮在SXRecyclerView顶部不会滑出屏幕,直到下一个Header滑到SXRecyclerView顶部位置时才会将上一个Header顶出屏幕。

实现PinnedHeader效果，有2个必要的步骤：

1、需要在其Adapter中实现以下接口

```java
public interface RecyclerPinnedHeaderAdapter<VH extends MzRecyclerView.ViewHolder> {

// 返回PinnedHeader 的编号id
// 通过id来对ViewHolder进行分组,相同id的viewHolder会分到通一组,对应相同的PinnedHeader
// 返回 负数 的时候，表示该 PinnedHeader 不显示
long getHeaderId(int position);

// 创建 PinnedHeader的样式
VH onCreateHeaderViewHolder(ViewGroup parent);

// 绑定 PinnedHeader的数据
void onBindHeaderViewHolder(VH holder, int position);

int getItemCount();
}

```

2、以实现了RecyclerPinnedHeaderAdapter的Adapter对象为参数，创建RecyclerPinnedHeaderDecoration对象，并通过addItemDecoration添加到SXRecyclerView中，这时已经可以显示PinnedHeader了。

```java
//创建RecyclerPinnedHeaderDecoration
RecyclerPinnedHeaderDecoration headersDecor = new RecyclerPinnedHeaderDecoration(adapter);
//将创建RecyclerPinnedHeaderDecoration作为一个ItemDecoration添加到Recyclerview中，从而实现将PinnedHeader绘制到SXRecyclerview中
mRecyclerView.addItemDecoration(headersDecor);

```

RecyclerPinnedHeaderDecoration提供了PinnedHeader监听器，悬浮在顶部的PinnedHeader发生改变（被顶出屏幕或者屏幕外有新的PinnedHeader进来）时触发监听器回调，通过此监听器可以对悬浮头添加一些标识或者做一些动画效果。示例代码如下：

```java
//可以通过对RecyclerPinnedHeaderDecoration设置监听器，来实现对切换PinnedHeader的监听
headersDecor.setPinnedHeaderListener(new RecyclerPinnedHeaderDecoration.OnPinnedHeaderChangeListener() {
            @Override
            public void OnPinnedHeaderChange(RecyclerView recyclerView, View currentHeader, int currentPosition, long currentHeaderId, View lastHeader, int lastPosition, long lastHeaderId) {
                //do something
            }
});

```

此外，SXRecyclerview还支持对PinnedHeader设置点击事件。示例代码如下:

```java
//可以通过RecyclerPinnedHeaderTouchListener设置点击PinnedHeader时的回调
RecyclerPinnedHeaderTouchListener touchListener = new RecyclerPinnedHeaderTouchListener(mRecyclerView, headersDecor);
touchListener.setOnHeaderClickListener(new RecyclerPinnedHeaderTouchListener.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View header, int position, long headerId, MotionEvent e) {
                // do something
            }
        });

//记得将RecyclerPinnedHeaderTouchListener添加到SXRecyclerview中才会生效
mRecyclerView.addOnItemTouchListener(touchListener);

```
示意图：
![pinnedheader.gif](https://upload-images.jianshu.io/upload_images/148381-77bc90c11c4c9b5b.gif?imageMogr2/auto-orient/strip)


## Header和Footer

SXRecyclerView提供了添加头尾部的功能。示例代码如下：

```java
//创建view对象
View headerView = LayoutInflater.from(this).inflate(R.layout.recyclerview_header_view, mRecyclerView,null);  
// 将view包装成Viewholder
final HeaderViewHolder header = new HeaderViewHolder(headerView);
header.mTextView.setText("This is Header View");
header.mTextView.setOnClickListener(new View.OnClickListener() {
       @Override
        public void onClick(View v) {
			// do something
        }
});
//添加此被包装过的headerview到SXRecyclerview中
mRecyclerView.addHeaderView(header);


View footerView = LayoutInflater.from(this).inflate(R.layout.recyclerview_footer_view, null);
final FooterViewHolder footer = new FooterViewHolder(footerView);
footer.mTextView.setText("This is Footer View");
footer.mTextView.setBackgroundColor(0xFF6495ED);
footer.mTextView.setOnClickListener(new View.OnClickListener() {
      @Override
       public void onClick(View v) {
		// do something
       }
});
//添加此被包装过的footerview到SXRecyclerview中
mRecyclerView.addFooterView(footer);

```

Header和Footer功能RecyclerView的三种布局方式都能支持，效果如下：

![HeaderAndFooter.gif](screens/HeaderAndFooter.gif)
![HeaderAndFooterGrid.gif](https://upload-images.jianshu.io/upload_images/148381-4baa8f0eb264be79.gif?imageMogr2/auto-orient/strip)
![HeaderAndFooterStag.gif](https://upload-images.jianshu.io/upload_images/148381-03896a9b9737a0f3.gif?imageMogr2/auto-orient/strip)


该库我会一直更新维护，以后会逐渐添加更多通用的功能，使用过程中有什么问题可以随时交流~

