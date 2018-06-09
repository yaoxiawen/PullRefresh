package com.yxw.pullrefresh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yxw.pullrefresh.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private View headerView;
    private ImageView ivArrow;
    private ProgressBar pb;
    private TextView tvState;
    private TextView tvTime;
    private View footerView;
    private int headerViewHeight;
    private int footerViewHeight;
    private int downY;
    private int paddingTop = -headerViewHeight;
    private final int PULL_REFRESH = 0;
    private final int RELEASE_REFRESH = 1;
    private final int REFRESHING = 3;
    private int currentState = PULL_REFRESH;
    private RotateAnimation upRA, downRA;
    private boolean isLoading = false;

    /**
     * 构造方法1，使用代码new对象时使用
     * @param context
     */
    public RefreshListView(Context context) {
        super(context);
        init();
    }

    /**
     * 构造方法2，使用布局文件时使用
     * @param context
     * @param attrs
     */
    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        setOnScrollListener(this);//设置滑动监听
        initHeaderView();
        initRotateAnimation();
        initFooterView();
    }

    /**
     * 初始化头布局
     */
    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.layout_header, null);
        //获取控件高度
        headerView.measure(0, 0);
        headerViewHeight = headerView.getMeasuredHeight();
        //初始化
        ivArrow = headerView.findViewById(R.id.iv_arrow);
        pb = headerView.findViewById(R.id.pb);
        tvState = headerView.findViewById(R.id.tv_state);
        tvTime = headerView.findViewById(R.id.tv_time);
        //设置头布局隐藏
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        //添加头布局
        addHeaderView(headerView);
    }

    /**
     * 旋转动画
     */
    private void initRotateAnimation() {
        upRA = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        upRA.setDuration(300);
        //旋转之后停留在该位置
        upRA.setFillAfter(true);
        downRA = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downRA.setDuration(300);
        downRA.setFillAfter(true);
    }

    /**
     * 初始化尾布局
     */
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.layout_footer, null);
        //获取控件高度
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        //设置尾布局隐藏
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        //添加尾布局
        addFooterView(footerView);
    }

    /**
     * 触摸事件，刷新数据，做头布局操作
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentState == REFRESHING) {
                    break;
                }
                int dy = (int) (ev.getY() - downY);
                paddingTop = -headerViewHeight + dy / 2;
                //下拉刷新，此时触摸事件不让listview处理
                if (paddingTop > -headerViewHeight && getFirstVisiblePosition() == 0) {
                    if (paddingTop >= 0 && currentState != RELEASE_REFRESH) {
                        currentState = RELEASE_REFRESH;
                    } else if (paddingTop < 0 && currentState != PULL_REFRESH) {
                        currentState = PULL_REFRESH;
                    }
                    refreshHeaderView();
                    headerView.setPadding(0, paddingTop, 0, 0);
                    return true;//把触摸事件拦截掉
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == REFRESHING) {
                    break;
                }
                //下拉刷新，此时触摸事件不让listview处理
                if (paddingTop > -headerViewHeight && getFirstVisiblePosition() == 0) {
                    if (paddingTop >= 0) {
                        currentState = REFRESHING;
                        paddingTop = 0;
                    } else {
                        currentState = PULL_REFRESH;
                        paddingTop = -headerViewHeight;
                    }
                    refreshHeaderView();
                    headerView.setPadding(0, paddingTop, 0, 0);
                    //进行刷新操作
                    if (currentState == REFRESHING) {
                        //确保用户有设置监听，才进行刷新数据操作
                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }
                    }
                    return true;//把触摸事件拦截掉
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 更新头布局ui的操作
     */
    private void refreshHeaderView() {
        switch (currentState) {
            case PULL_REFRESH:
                ivArrow.setVisibility(VISIBLE);
                pb.setVisibility(INVISIBLE);
                tvState.setText("下拉刷新");
                ivArrow.startAnimation(upRA);
                break;
            case RELEASE_REFRESH:
                ivArrow.setVisibility(VISIBLE);
                pb.setVisibility(INVISIBLE);
                tvState.setText("松开刷新");
                ivArrow.startAnimation(downRA);
                break;
            case REFRESHING:
                pb.setVisibility(VISIBLE);
                //清楚一下箭头的动画，再将箭头INVISIBLE
                ivArrow.clearAnimation();
                ivArrow.setVisibility(INVISIBLE);
                tvState.setText("正在刷新...");
                break;
        }
    }

    /**
     * 刷新完成之后，更新ui的方法，改变头布局内容和隐藏头布局
     */
    public void completeRefresh() {
        //public是暴露给用户使用的
        ivArrow.setVisibility(VISIBLE);
        pb.setVisibility(INVISIBLE);
        tvState.setText("下拉刷新");
        tvTime.setText("最后刷新时间:" + getCurrentTime());
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        currentState = PULL_REFRESH;
    }

    /**
     * 获取当前时间
     * @return
     */
    private String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 暴露刷新的接口
     */
    private OnRefreshListener refreshListener;

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * 滑动事件，加载数据，做尾布局操作
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE &&
                getLastVisiblePosition() == getCount() - 1 && !isLoading) {
            //防止重复加载
            isLoading = true;
            footerView.setPadding(0, 0, 0, 0);
            setSelection(getCount());
            //确保用户有设置监听，才进行加载数据操作
            if (loadListener != null){
                loadListener.onLoad();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 加载完成之后，更新ui的方法，隐藏尾布局
     */
    public void completeLoad() {
        //public是暴露给用户使用的
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        isLoading = false;
    }
    /**
     * 暴露加载的接口
     */
    private OnLoadListener loadListener;

    public void setOnLoadListener(OnLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
