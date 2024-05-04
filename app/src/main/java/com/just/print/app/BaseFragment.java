package com.just.print.app;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.just.print.db.bean.Mark;
import com.just.print.util.ToastUtil;

import java.util.List;

abstract public class BaseFragment extends Fragment {
    protected abstract int getLayoutId();

    @Override
    public Context getContext() {
        return getActivity().getBaseContext();
    }

    View mRoot;

    EventBus eventBus;

    public EventBus getEventBus() {
        if (eventBus == null)
            if (getActivity() instanceof EventBus) {
                eventBus = (EventBus) getActivity();
            } else {
                eventBus = new EventBus.EventBusImpl();
            }
        return eventBus;
    }

    protected View findViewById(int id) {
        return mRoot.findViewById(id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mRoot == null) {
            mRoot = inflater.inflate(getLayoutId(), container, false);
        } else {
            if (mRoot.getParent() != null) {
                ViewGroup v = (ViewGroup) mRoot.getParent();
                v.removeView(mRoot);
            }
            //container.removeView(mRoot);
        }
        return mRoot;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreated(savedInstanceState);
    }

    public void showToast(String text) {
        ToastUtil.showToast(getContext(), text);
    }

    public abstract void onCreated(Bundle savedInstanceState);

    public interface onChoiceMarks {
        void onChoiceMarks(List<Mark> list);
    }
}
