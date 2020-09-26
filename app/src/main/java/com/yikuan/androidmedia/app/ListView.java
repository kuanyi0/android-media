package com.yikuan.androidmedia.app;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author yikuan
 * @date 2020/09/26
 */
public class ListView extends RecyclerView {
    private ListAdapter mAdapter = new ListAdapter(new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return false;
        }
    });

    public ListView(@NonNull Context context) {
        this(context, null);
    }

    public ListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(mAdapter);
    }

    public void setList(List<String> list) {
       mAdapter.submitList(list);
    }
}
