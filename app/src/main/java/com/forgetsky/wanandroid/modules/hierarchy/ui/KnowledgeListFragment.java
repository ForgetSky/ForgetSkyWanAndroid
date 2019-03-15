package com.forgetsky.wanandroid.modules.hierarchy.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.forgetsky.wanandroid.R;
import com.forgetsky.wanandroid.base.fragment.BaseFragment;
import com.forgetsky.wanandroid.core.constant.Constants;
import com.forgetsky.wanandroid.modules.hierarchy.contract.KnowledgeListContract;
import com.forgetsky.wanandroid.modules.hierarchy.presenter.KnowledgeListPresenter;
import com.forgetsky.wanandroid.modules.homepager.bean.ArticleItemData;
import com.forgetsky.wanandroid.modules.homepager.bean.ArticleListData;
import com.forgetsky.wanandroid.modules.homepager.ui.ArticleListAdapter;
import com.forgetsky.wanandroid.utils.CommonUtils;
import com.forgetsky.wanandroid.utils.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class KnowledgeListFragment extends BaseFragment<KnowledgeListPresenter> implements KnowledgeListContract.View {

    private static final String TAG = "ProjectListFragment";

    @BindView(R.id.smart_refresh_layout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.project_list_recycler_view)
    RecyclerView mRecyclerView;

    private List<ArticleItemData> mArticleList;
    private ArticleListAdapter mAdapter;

    private int cid;

    public static KnowledgeListFragment newInstance(Bundle bundle) {
        KnowledgeListFragment fragment = new KnowledgeListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_project_list;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void initView() {
        initRecyclerView();
    }

    @Override
    protected void initEventAndData() {
        assert getArguments() != null;
        cid = getArguments().getInt(Constants.KNOWLEDGE_CID);
        initRefreshLayout();
        mPresenter.getKnowledgeListData(cid, true);
    }

    private void initRecyclerView() {
        mArticleList = new ArrayList<>();
        mAdapter = new ArticleListAdapter(R.layout.item_article_list, mArticleList);
        mAdapter.setOnItemClickListener((adapter, view, position) -> startArticleDetailPager(view, position));
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> clickChildEvent(view, position));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            mPresenter.getKnowledgeListData(cid, false);
            refreshLayout.finishRefresh();
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            mPresenter.loadMore(cid);
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
                true, position, Constants.KNOWLEDGE_PAGER);
    }

    private void clickChildEvent(View view, int position) {
        switch (view.getId()) {
            case R.id.tv_article_chapterName:
                //todo chapter click
//                startSingleChapterKnowledgePager(position);
                break;
            case R.id.iv_article_like:
                collectClickEvent(position);
                break;
            case R.id.tv_article_tag:
                //todo tag click
//                clickTag(position);
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
    public void showKnowledgeListData(ArticleListData articleListData, boolean isRefresh) {
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
