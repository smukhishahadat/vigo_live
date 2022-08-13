package com.bakbakum.shortvdo.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.adapter.CommentAdapter;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.SessionManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CommentSheetViewModel extends ViewModel {
    public String postId;
    public String comment;
    public boolean allowComments;
    public ObservableInt commentCount = new ObservableInt(0);
    public ObservableBoolean isLoading = new ObservableBoolean(false);
    public ObservableBoolean isEmpty = new ObservableBoolean(true);
    public CommentAdapter adapter = new CommentAdapter();
    public int start = 0;
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    public SessionManager sessionManager;
    private int count = 15;
    private CompositeDisposable disposable = new CompositeDisposable();

    public void afterCommentTextChanged(CharSequence s) {
        comment = s.toString();
    }


    public void fetchComments(boolean isLoadMore) {

        disposable.add(Global.initRetrofit().getPostComments(postId, count, start)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.set(true))
                .doOnTerminate(() -> {
                    onLoadMoreComplete.setValue(true);
                    isLoading.set(false);
                })
                .subscribe((comment, throwable) -> {

                    if (comment != null && comment.getData() != null && !comment.getData().isEmpty()) {

                        if (isLoadMore) {
                            adapter.loadMore(comment.getData());
                        } else {
                            adapter.updateData(comment.getData());
                        }
                        start = start + count;
                    }
                    isEmpty.set(adapter.mList.isEmpty());

                }));
    }

    public void onLoadMore() {
        fetchComments(true);
    }

    public void addComment() {
        if (!TextUtils.isEmpty(comment)) {
            callApiToSendComment();
        }
    }

    private void callApiToSendComment() {
        disposable.add(Global.initRetrofit().addComment(Global.ACCESS_TOKEN, postId, comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.set(true))
                .doOnTerminate(() -> isLoading.set(false))
                .subscribe((comment, throwable) -> {
                    if (comment != null && comment.getStatus() != null) {
                        Log.d("ADDED", "Success");
                        start = 0;
                        fetchComments(false);
                        onLoadMoreComplete.setValue(false);
                        commentCount.set(commentCount.get() + 1);
                    }
                }));
    }

    public void callApitoDeleteComment(String commentId, int position) {
        disposable.add(Global.initRetrofit().deleteComment(Global.ACCESS_TOKEN, commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.set(true))
                .doOnTerminate(() -> isLoading.set(false))
                .subscribe((deleteComment, throwable) -> {
                    if (deleteComment != null && deleteComment.getStatus() != null) {
                        Log.d("DELETED", "Success");
                        adapter.mList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeRemoved(position, adapter.mList.size());
                        commentCount.set(commentCount.get() - 1);

                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
