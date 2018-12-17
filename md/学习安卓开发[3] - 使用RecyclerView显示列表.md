在上一篇*学习安卓开发[2] - 在Activity中托管Fragment*中了解了使用Fragment的好处和方法，本次记录的是在进行列表展示时RecyclerView的使用。

- RecyclerView介绍
- RecyclerView及其相关类
- RecyclerView的应用
    -   引入RecyclerView
    -   关联RecyclerView和fragment
    -   ViewHolder
    -   Adapter
    -   将Adapter和RecyclerView关联

#### RecyclerView介绍
很多时候都需要进行列表的展示，比如商品列表，一般的做法是创建一个商品的通用布局，在请求到商品列表数据后，将商品数据转换为商品对象并与一个商品View绑定，这样循环操作就实现了列表的效果。
但如果列表项有很多怎么办呢，如果一次性初始化全部的View很容易搞垮程序。在PC和Web程序中可以使用分页的方式，但如果照搬到运行移动APP的小屏设备体验会非常差。在小屏设备适合上下滑动的方式，那么能否将上下滑动与分页结合，每次只初始化足够一屏显示的view数量呢，答案是肯定的，RecyclerView就是干这个的。

RecyclerView的作用的是按需创建View对象，当View被滑动到屏幕外后，RecyclerView便会将其回收再利用。

#### RecyclerView及其相关类
要实现这个功能，RecyclerView还需要ViewHolder和Adapter的协助，它们之间的关系为：
图中没有显示Adapter的位置，实际上它工作在在RecyclerView和ViewHoler之间，负责为RecyclerView提供ViewHoler对象。Adapter是一个控制器对象，从模型层获取数据，然后提供给RecyclerView显示，起动桥梁的作用。

#### RecyclerView的应用
##### 引入RecyclerView
RecyclerView类来自Google支持库，所以首先需要添加RecyclerView依赖库，这里使用的是recyclerview-v7支持库。然后就可以在列表布局文件中使用它了：
```
<android.support.v7.widget.RecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/crime_recycler_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</android.support.v7.widget.RecyclerView>
```
注意要给其指定id。
##### 关联RecyclerView和fragment
```
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

    mCrimeRecyclerView = (RecyclerView) view
            .findViewById(R.id.crime_recycler_view);
    mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    return view;
}
```
代码使用了setLayoutManager()，因为RecyclerView无法独立工作，需要LayoutManager的支持，RecyclerView在创建完视图后，就立即转交给了LayoutManager,屏幕上列表项的摆放就是LayoutManager负责的，此外它还负责屏幕的滚动行为。

##### ViewHolder
ViewHolder的职责相对简单，既容纳单个列表项View。基本的ViewHolder使用方式如下，其中list_item_crime为单个列表项View的名称。
```
private class CrimeHolder extends RecyclerView.ViewHolder{
    public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.list_item_crime, parent, false));
    }
}
```

##### Adapter
在需要显示新创建的ViewHolder或让View对象与已经创建的ViewHolder关联时，RecyclerView会去问Adapter要，RecyclerView工作在较高的抽象层，不会关心具体的View对象，这是Adapter需要做的事。

```
private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

    private List<Crime> mCrimes;

    public CrimeAdapter(List<Crime> crimes) {
        mCrimes = crimes;
    }

    @Override
    public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        return new CrimeHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(CrimeHolder holder, int position) {
        Crime crime = mCrimes.get(position);
        holder.bind(crime);
    }

    @Override
    public int getItemCount() {
        return mCrimes.size();
    }
}
```

##### 将Adapter和RecyclerView关联
编写好了RecyclerView、ViewHoler和Adapter，接下来只需将将Adapter和RecyclerView关联，就可以正常工作了
编写updateUI方法，然后在onCreateView()中，返回view之前调用：
```
private void updateUI() {
    CrimeLab crimeLab = CrimeLab.get(getActivity());
    List<Crime> crimes = crimeLab.getCrimes();

    mAdapter = new CrimeAdapter(crimes);
    mCrimeRecyclerView.setAdapter(mAdapter);
}
```

