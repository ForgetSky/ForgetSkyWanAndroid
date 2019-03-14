package com.forgetsky.wanandroid.modules.wxarticle.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.forgetsky.wanandroid.R;
import com.forgetsky.wanandroid.base.fragment.BaseFragment;
import com.forgetsky.wanandroid.core.constant.Constants;
import com.forgetsky.wanandroid.modules.homepager.bean.ArticleItemData;
import com.forgetsky.wanandroid.modules.homepager.bean.ArticleListData;
import com.forgetsky.wanandroid.modules.wxarticle.contract.WxArticleListContract;
import com.forgetsky.wanandroid.modules.wxarticle.presenter.WxArticleListPresenter;
import com.forgetsky.wanandroid.utils.CommonUtils;
import com.forgetsky.wanandroid.utils.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WxArticleListFragment extends BaseFragment<WxArticleListPresenter> implements WxArticleListContract.View {

    private static final String TAG = "WxArticleListFragment";

    @BindView(R.id.smart_refresh_layout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.wx_list_recycler_view)
    RecyclerView mRecyclerView;

    private List<ArticleItemData> mArticleList;
    private WxArticleListAdapter mAdapter;

    private int id;

    public static WxArticleListFragment newInstance(Bundle bundle) {
        WxArticleListFragment fragment = new WxArticleListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wx_list;
    }

    @Override
    protected void initView() {
        initRecyclerView();
    }

    @Override
    protected void initEventAndData() {
        assert getArguments() != null;
        id = getArguments().getInt(Constants.WX_CHAPTER_ID);
        initRefreshLayout();
        mPresenter.getWxArticlesData(id, true);
    }

    private void initRecyclerView() {
        mArticleList = new ArrayList<>();
        mAdapter = new WxArticleListAdapter(R.layout.item_article_list, mArticleList);
        mAdapter.setOnItemClickListener((adapter, view, position) -> startArticleDetailPager(view, position));
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> clickChildEvent(view, position));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            mPresenter.getWxArticlesData(id, false);
            refreshLayout.finishRefresh();
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            mPresenter.loadMore();
            refreshLayout.finishLoadMore();
        });
    }

    private void startArticleDetailPager(View view, int position) {
        if (mAdapter.getData().size() <= 0 || mAdapter.getData().size() < position) {
            return;
        }

        CommonUtils.startArticleDetailActivity(_mActivity,
                mAdapter.getData().get(position).getId(),
                mAdapter.getData().get(position).getTitle(),
                mAdapter.getData().get(position).getLink(),
                mAdapter.getData().get(position).isCollect(),
                true, position, Constants.WX_PAGER);
    }

    private void clickChildEvent(View view, int position) {
        switch (view.getId()) {
            case R.id.iv_article_like:
                collectClickEvent(position);
                break;
            default:
                break;
        }
    }

    private void collectClickEvent(int position) {
        if (mPresenter.getLoginStatus()) {
            if (mAdapter.getData().get(position).isCollect()) {
                mPresenter.cancelCollectArticle(position, mAdapter.getData().get(position).getId());
            } else {
                mPresenter.addCollectArticle(position, mAdapter.getData().get(position).getId());
            }
        } else {
            CommonUtils.startLoginActivity(_mActivity);
            ToastUtils.showToast(_mActivity, getString(R.string.login_first));
        }
    }

    @Override
    public void showWxArticlesData(ArticleListData articleListData, boolean isRefresh) {
        if (mAdapter == null) {
            return;
        }
        if (isRefresh) {
            mArticleList = articleListData.getDatas();
            mAdapter.replaceData(articleListData.getDatas());
        } else {
            mArticleList.addAll(articleListData.getDatas());
            mAdapter.addData(articleListData.getDatas());
        }
    }


    public void jumpToTheTop() {
        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void showCollectSuccess(int position) {
        mAdapter.getData().get(position).setCollect(true);
        mAdapter.setData(position, mAdapter.getData().get(position));
        ToastUtils.showToast(_mActivity, getString(R.string.collect_success));
    }

    @Override
    public void showCancelCollectSuccess(int position) {
        mAdapter.getData().get(position).setCollect(false);
        mAdapter.setData(position, mAdapter.getData().get(position));
        ToastUtils.showToast(_mActivity, getString(R.string.cancel_collect));
    }

}
