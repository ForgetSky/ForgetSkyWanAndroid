package com.forgetsky.wanandroid.modules.hierarchy.presenter;

import com.forgetsky.wanandroid.R;
import com.forgetsky.wanandroid.app.WanAndroidApp;
import com.forgetsky.wanandroid.core.constant.Constants;
import com.forgetsky.wanandroid.core.event.CollectEvent;
import com.forgetsky.wanandroid.core.rx.BaseObserver;
import com.forgetsky.wanandroid.modules.hierarchy.contract.KnowledgeListContract;
import com.forgetsky.wanandroid.modules.homepager.bean.ArticleListData;
import com.forgetsky.wanandroid.modules.main.presenter.CollectEventPresenter;
import com.forgetsky.wanandroid.utils.RxUtils;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import javax.inject.Inject;

public class KnowledgeListPresenter extends CollectEventPresenter<KnowledgeListContract.View>
        implements KnowledgeListContract.Presenter {

    @Inject
    KnowledgeListPresenter() {
    }

    private int currentPage = 0;
    private boolean isRefresh = true;
    private int cid;

    @Override
    public void getKnowledgeListData(int cid, boolean isShowStatusView) {
        this.cid = cid;
        addSubscribe(mDataManager.getKnowledgeListData(currentPage, cid)
                .compose(RxUtils.SchedulerTransformer())
                .filter(articleListData -> mView != null)
                .subscribeWith(new BaseObserver<ArticleListData>(mView,
                        WanAndroidApp.getContext().getString(R.string.failed_to_obtain_article_list),
                        isShowStatusView) {
                    @Override
                    public void onSuccess(ArticleListData articleListData) {
                        if(isShowStatusView && currentPage == 0 &&
                                articleListData.getDatas().size() < 1) {
                            mView.showEmpty();
                        } else {
                            mView.showKnowledgeListData(articleListData, isRefresh);
                        }
                    }
                }));
    }

    @Override
    public void reload() {
        getKnowledgeListData(cid, true);
    }

    @Override
    public void loadMore(int cid) {
        isRefresh = false;
        currentPage++;
        getKnowledgeListData(cid, false);
    }

    @Override
    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Subscriber(tag = Constants.KNOWLEDGE_PAGER)
    public void collectEvent(CollectEvent collectEvent) {
        if (mView == null) return;
        if (collectEvent.isCancel()) {
            mView.showCancelCollectSuccess(collectEvent.getArticlePostion());
        } else {
            mView.showCollectSuccess(collectEvent.getArticlePostion());
        }
    }
}
