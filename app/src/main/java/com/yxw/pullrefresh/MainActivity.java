package com.yxw.pullrefresh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yxw.pullrefresh.view.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private RefreshListView rlv;
    private List<String> list;
    private List<String> addList;
    private List<String> newList;
    private MyAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                //加载完数据之后，要更新适配器和隐藏尾布局
                adapter.notifyDataSetChanged();
                rlv.completeLoad();
            } else if (msg.what == 1) {
                //刷新完数据之后，要更新适配器和隐藏头布局
                adapter.notifyDataSetChanged();
                rlv.completeRefresh();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        update();
    }

    private void initView() {
        rlv = findViewById(R.id.rlv);
        list = new ArrayList<>();
        addList = new ArrayList<>();
        newList = new ArrayList<>();
    }

    private void initData() {
        for (int i = 0; i < 30; i++) {
            list.add("listview原来的数据-" + i);
        }
        for (int i = 0; i < 5; i++) {
            addList.add("listview新加载的数据-" + i);
        }
        for (int i = 0; i < 5; i++) {
            newList.add("listview刷新的数据-" + i);
        }
        adapter = new MyAdapter();
        rlv.setAdapter(adapter);
    }

    private void update() {
        //刷新数据操作
        rlv.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.addAll(0, newList);
                handler.sendEmptyMessageDelayed(1, 2000);
            }
        });
        //加载数据操作
        rlv.setOnLoadListener(new RefreshListView.OnLoadListener() {
            @Override
            public void onLoad() {
                list.addAll(addList);
                handler.sendEmptyMessageDelayed(0, 2000);
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(MainActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(18);
            tv.setText(list.get(position));
            return tv;
        }
    }
}
