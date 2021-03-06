/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sina.weibo.simple.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

import rx.Observable;
import rx.Subscription;
import rx.functions.*;
import rx.subjects.*;

import rx.android.app.*;
import rx.android.view.ViewObservable;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;

public class RxCardsFragment extends Fragment {
    @InjectView(R.id.list)
    RecyclerView listView;
    @InjectView(R.id.refresh)
    SwipeRefreshLayout refreshView;

    //Subject<View, View> viewSubject = PublishSubject.create();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_swipe, container, false);
        ButterKnife.inject(this, view);
        //viewSubject.onNext(view);

        listAdapter = ListRecyclerAdapter.create();
        listAdapter.createViewHolder(new Func2<ViewGroup, Integer, CardViewHolder>() {
            @Override
            public CardViewHolder call(ViewGroup parent, Integer position) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);

                /*
                TypedValue typedValue = new TypedValue();
                parent.getContext().getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
                view.setBackgroundResource(typedValue.resourceId);
                */

                return new CardViewHolder(view);
            }
        });

        listView.setLayoutManager(new LinearLayoutManager(listView.getContext()));
        listView.setAdapter(listAdapter);

        refreshView.setOnRefreshListener(() -> {
            load();
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            //if (refreshView == null) {
                //viewSubject.asObservable().subscribe(v -> load()); // FIXME onDestoryView unsubscribe subscription?
            //} else {
                load();
            //}
        }
    }

    public Subscription load() {
        if (refreshView != null) refreshView.setRefreshing(true);
        return AppObservable.bindFragment(RxCardsFragment.this, items).toList().subscribe(list -> {
            android.util.Log.d("RetroWeibo", "list: " + list);
            android.util.Log.d("RetroWeibo", "list.size(): " + list.size());
            listAdapter.getList().clear();
            listAdapter.getList().addAll(list);
            listAdapter.notifyDataSetChanged();
        }, e -> {}, () -> {
            refreshView.setRefreshing(false);
        });
    }

    private ListRecyclerAdapter<RxCard, CardViewHolder> listAdapter;
    Observable<RxCard> items;

    public RxCardsFragment items(Observable<RxCard> items) {
        this.items = items;

        return this;
    }

    public static RxCardsFragment create() {
        return new RxCardsFragment();
    }

    public static class CardViewHolder extends BindViewHolder<RxCard> {
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.text1)
        TextView text1;
        @InjectView(R.id.message)
        TextView message;
        @InjectView(R.id.imageCard)
        CardView imageCard;
        @InjectView(R.id.image)
        ImageView image;
        @InjectView(R.id.comments)
        RecyclerView commentsView;
        @InjectView(R.id.likeCount)
        TextView likeCountView;
        @InjectView(R.id.like)
        ImageView likeView;
        @InjectView(R.id.comment)
        ImageView commentView;
        @InjectView(R.id.commentCount)
        TextView commentCountView;
        @InjectView(R.id.comment_avatar)
        ImageView commentAvatar;
        @InjectView(R.id.comment_edit)
        EditText commentEdit;
        @InjectView(R.id.send)
        ImageView sendView;

        ListRecyclerAdapter<String, CommentViewHolder> commentsAdapter;
        boolean liked;
        int likeCount;
        int commentCount;

        public CardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            commentsAdapter = ListRecyclerAdapter.create();
            commentsAdapter.createViewHolder(new Func2<ViewGroup, Integer, CommentViewHolder>() {
                @Override
                public CommentViewHolder call(ViewGroup parent, Integer position) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);

                    return new CommentViewHolder(view);
                }
            });

            commentsView.setLayoutManager(new MeasuredLinearLayoutManager(commentsView.getContext()));
            commentsView.setAdapter(commentsAdapter);
        }

        @Override
        public void onBind(int position, RxCard item) {
            icon.setVisibility(View.GONE);
            ViewObservable.bindView(icon, item.icon).filter(url -> !android.text.TextUtils.isEmpty(url)).subscribe(url -> {
                icon.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                    .load(url)
                    .fitCenter()
                    .into(icon);
            });

            itemView.setOnClickListener(v -> {}); // clear
            ViewObservable.bindView(text1, item.text1)
                .filter(name -> !android.text.TextUtils.isEmpty(name))
                .subscribe(name -> {
                    text1.setText(name);

                    itemView.setOnClickListener(v -> {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, CheeseDetailActivity.class);

                        if (!android.text.TextUtils.isEmpty(name)) intent.putExtra(CheeseDetailActivity.EXTRA_NAME, name);

                        context.startActivity(intent);
                    });
                });

            ViewObservable.bindView(message, item.message)
                .filter(s -> !android.text.TextUtils.isEmpty(s))
                .subscribe(s -> message.setText(s));

            image.setVisibility(View.GONE);
            imageCard.setVisibility(View.GONE);
            ViewObservable.bindView(image, item.image).filter(url -> !android.text.TextUtils.isEmpty(url)).subscribe(url -> {
                image.setVisibility(View.VISIBLE);
                imageCard.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                    .load(url)
                    .fitCenter()
                    .into(image);
            });

            likeCount = 0;
            likeCountView.setText("" + likeCount); // clear
            ViewObservable.bindView(likeCountView, item.likeCount).subscribe(i -> {
                likeCount = i;
                likeCountView.setText("" + likeCount);
            });

            liked = false;
            likeView.setOnClickListener(v -> {}); // clear
            Glide.with(itemView.getContext())
                .load(R.drawable.ic_thumb_up_outline)
                .fitCenter()
                .into(likeView);

            commentsView.setVisibility(View.GONE);
            commentsAdapter.getList().clear();
        }
    }

    public static class CommentViewHolder extends BindViewHolder<String> {
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.text1)
        TextView text1;
        @InjectView(R.id.like)
        ImageView likeView;
        @InjectView(R.id.likes)
        TextView likes;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        boolean liked;
        int likeCount;

        @Override
        public void onBind(int position, String item) {
        }
    }
}

