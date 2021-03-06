# SimpleWeibo

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SimpleWeibo-green.svg?style=flat)](https://android-arsenal.com/details/1/2139)
[![Download](https://api.bintray.com/packages/8tory/maven/SimpleWeibo/images/download.svg)](https://bintray.com/8tory/maven/SimpleWeibo/_latestVersion)
[![JitPack](https://img.shields.io/github/tag/8tory/SimpleWeibo.svg?label=JitPack)](https://jitpack.io/#8tory/SimpleWeibo)
[![javadoc](https://img.shields.io/github/tag/8tory/SimpleWeibo.svg?label=javadoc)](https://jitpack.io/com/github/8tory/SimpleWeibo/simpleweibo/1.0.2/javadoc/index.html)
[![Build Status](https://travis-ci.org/8tory/SimpleWeibo.svg)](https://travis-ci.org/8tory/SimpleWeibo)
[![Join the chat at https://gitter.im/8tory/SimpleWeibo](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/8tory/SimpleWeibo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![](https://avatars0.githubusercontent.com/u/5761889?v=3&s=48)](https://github.com/Wendly)
[![](https://avatars3.githubusercontent.com/u/213736?v=3&s=48)](https://github.com/yongjhih)
Contributors..

![](art/SimpleWeibo.png)

Simple Weibo SDK turns Weibo API into a Java interface with RxJava.

[#Demo](#demo)

[![](art/screenshot-timeline.png)](#demo)

## Usage

My posts:

```java
weibo = SimpleWeibo.create(activity);

Observable<Status> myStatuses = weibo.getStatuses();
myStatuses.take(10).forEach(System.out::println);
```

logIn (default permissions):

```java
weibo.logIn().subscribe();
```

logInWithPermissions:

```java
weibo.logInWithPermissions("email", "statuses_to_me_read").subscribe();
```

## Diff

Using Weibo Core SDK:

```java
WeiboParameters params = new WeiboParameters(appId);
params.put("access_token", accessToken); // AbsOpenAPI.KEY_ACCESS_TOKEN
// put ...

new AsyncWeiboRunner(context).requestAsync(
  "https://api.weibo.com/2" + "/statuses/friends_timeline.json", // AbsOpenAPI.API_SERVER
  params,
  "GET", // AbsOpenAPI.HTTPMETHOD_GET
  new RequestListener() {
    @Override public void onComplete(String json) {
    }
    @Override public void onWeiboException(WeiboException e) {
    }
  }
);
```

Using Weibo SDK:

```java
StatusesAPI statusesApi = new StatusesAPI(context, appId, accessToken);
statusesApi.friendsTimeline(0L, 0L, 10, 1, false, 0, false, new RequestListener() {
    @Override public void onComplete(String json) {
        StatusList statusList = StatusList.parse(response);
        List<Status> statuses = statusList.statusList;
        // statusAdapter.addAll(statuses);
        // statusAdapter.notifyDataSetChanged();
    }
    @Override public void onWeiboException(WeiboException e) {
    }
  }
);
```

After, using SimpleWeibo:

```java
SimpleWeibo.create(activity)
           .getStatuses()
           .take(10)
           .forEach(System.out::println);
```

## Integration

AndroidManifest.xml:

```xml
<meta-data android:name="com.sina.weibo.sdk.ApplicationId" android:value="@string/weibo_app_id" />
<meta-data android:name="com.sina.weibo.sdk.RedirectUrl" android:value="@string/weibo_redirect_url" /> <!-- Optional -->
```

Activity:

```java
SimpleWeibo weibo;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    weibo = SimpleWeibo.create(activity);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    weibo.onActivityResult(requestCode, resultCode, data);
}
```

## Weibo Sharing

Add intent-filter on caller activity:

```xml
<intent-filter>
    <action android:name="com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

Though `onActivityResult(..)`, handle `onCreate(Bundle)`, `onNewIntent(Intent)`, `onResponse(BaseResponse)` methods:

```java
public MainActivity extends Activity implements IWeiboHandler.Response {
    SimpleWeibo weibo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weibo = SimpleWeibo.create(this);
        weibo.onCreate(this, this, savedInstanceState);

        // Bitmap bitmap = ...;
        weibo.share(this, "Hello", bitmap).subscribe(baseResponse -> {});
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        weibo.onNewIntent(this, intent);
    }
    @Override
    public void onResponse(BaseResponse baseResponse) {
        weibo.onResponse(baseResponse);
    }

    ...
}
```

## Add API using RetroWeibo

[![javadoc](https://img.shields.io/badge/javadoc-1.0.2-blue.svg?label=javadoc)](https://jitpack.io/com/github/8tory/SimpleWeibo/simpleweibo/1.0.2/javadoc/com/sina/weibo/simple/SimpleWeibo.html#method.summary)

Ready API:

```java
    @GET("/statuses/friends_timeline.json")
    public abstract Observable<Status> getStatuses(
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("base_app") boolean baseApp,
        @Query("trim_user") boolean trimUser,
        @Query("feature") int featureType
    );

    public Observable<Status> getStatuses() {
        // ...
    }

    @GET("/mentions.json")
    public abstract Observable<Status> getMentionedStatuses(
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("filter_by_author") int filterByAuthor,
        @Query("filter_by_source") int filterBySource,
        @Query("filter_by_type") int filterByType,
        @Query("trim_user") boolean trimUser
    );

    public Observable<Status> getMentionedStatuses() {
        // ...
    }

    @GET("/users/show.json")
    public abstract Observable<User> getUsersById(@Query("uid") long uid);

    @GET("/users/show.json")
    public abstract Observable<User> getUsersByName(@Query("screen_name") String screenName);

    @GET("/users/domain_show.json")
    public abstract Observable<User> getUsersByDomain(@Query("domain") String domain);

    @GET("/users/counts.json")
    public abstract Observable<User> getUsersCount(@Query("uids") long[] uids);

    @GET("/comments/show.json")
    public abstract Observable<Comment> getCommentsById(
        @Query("id") int id,
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("filter_by_author") int filterByAuthor
    );

    public Observable<Comment> getCommentsById(int id) {
        // ...
    }

    @GET("/comments/by_me.json")
    public abstract Observable<Comment> getCommentsByMe(
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("filter_by_source") int filterBySource
    );

    public Observable<Comment> getCommentsByMe() {
        // ...
    }

    public Observable<Comment> getCommentsByMe(int filterBySource) {
        // ...
    }

    @GET("/comments/to_me.json")
    public abstract Observable<Comment> getCommentsToMe(
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("filter_by_author") int filterByAuthor,
        @Query("filter_by_source") int filterBySource
    );

    public Observable<Comment> getCommentsToMe() {
        // ...
    }

    public Observable<Comment> getCommentsToMe(int filterByAuthor, int filterBySource) {
        // ...
    }

    @GET("/comments/timeline.json")
    public abstract Observable<Comment> getComments(
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("trim_user") boolean trimUser
    );

    public Observable<Comment> getComments() {
        // ...
    }

    public Observable<Comment> getComments(boolean trimUser) {
        // ...
    }

    @GET("/comments/mentions.json")
    public abstract Observable<Comment> getMentionedComments(
        @Query("since_id") long sinceId,
        @Query("max_id") long maxId,
        @Query("count") int count,
        @Query("page") int page,
        @Query("filter_by_author") int filterByAuthor,
        @Query("filter_by_source") int filterBySource
    );

    public Observable<Comment> getMentionedComments() {
        // ...
    }

    public Observable<Comment> getMentionedComments(int filterByAuthor, int filterBySource) {
        // ...
    }

    @GET("/comments/show_batch.json")
    public abstract Observable<Comment> getBatchComments(@Query("cids") long[] cids);
    
    @RetroWeibo.POST("https://m.api.weibo.com/2/messages/invite.json")
    public abstract Observable<Response> invite(@RetroWeibo.Query("uid") long uid, @RetroWeibo.Query("data") Invitation invitation);

    @RetroWeibo.POST("/statuses/update.json")
    public abstract Observable<Status> publishStatus(
        @RetroWeibo.Query("status") String content,
        @RetroWeibo.Query("long") double longtitude,
        @RetroWeibo.Query("lat") double latitude
    );

    @RetroWeibo.POST("/statuses/upload.json")
    public abstract Observable<Status> publishStatus(
        @RetroWeibo.Query("status") String content,
        @RetroWeibo.Query("pic") Bitmap picture,
        @RetroWeibo.Query("long") double longtitude,
        @RetroWeibo.Query("lat") double latitude
    );

    @RetroWeibo.POST("/statuses/upload_url_text.json")
    public abstract Observable<Status> publishStatus(
        @RetroWeibo.Query("status") String content,
        @RetroWeibo.Query("url") String pictureUrl,
        @RetroWeibo.Query("pic_id") String pictureId,
        @RetroWeibo.Query("long") double longtitude,
        @RetroWeibo.Query("lat") double latitude
    );

    public Observable<Status> publishStatus(
        String content,
        String pictureUrl,
        double longtitude,
        double latitude
    ) {
        // ...
    }

    @RetroWeibo.POST("/comments/create.json")
    public abstract Observable<Comment> publishComment(
        @RetroWeibo.Query("comment") String comment,
        @RetroWeibo.Query("id") long id,
        @RetroWeibo.Query("comment_ori") boolean pingback
    );

    public Observable<Comment> publishComment(String comment, long id) {
        // ...
    }

    public Observable<Comment> publishComment(String comment, Status status) {
        // ...
    }

    public Observable<Comment> publishComment(String comment, String id) {
        // ...
    }

    @RetroWeibo.POST("/comments/destroy.json")
    public abstract Observable<Comment> deleteComment(
        @RetroWeibo.Query("cid") long commentId
    );

    @RetroWeibo.POST("/comments/sdestroy_batch.json")
    public abstract Observable<Comment> deleteComments(
        @RetroWeibo.Query("cids") long[] commentIds
    );

    @RetroWeibo.POST("/comments/reply.json")
    public abstract Observable<Comment> replyComment(
        @RetroWeibo.Query("comment") String comment,
        @RetroWeibo.Query("cid") long cid,
        @RetroWeibo.Query("id") long id,
        @RetroWeibo.Query("without_mention") boolean withoutMention,
        @RetroWeibo.Query("comment_ori") boolean pingback
    );

    public Observable<Comment> replyComment(
        String comment,
        long cid,
        long id
    ) {
        // ...
    }

    public Observable<Comment> replyComment(String comment, Comment parentComment) {
        // ...
    }

    @RetroWeibo.POST("/oauth2/revokeoauth2")
    public abstract Observable<Response> revoke();

    public Observable<Response> logOut() {
        // ...
    }
```

[More ready APIs ..](simpleweibo/src/main/java/com/sina/weibo/simple/SimpleWeibo.java)


Add Model: [Status.java](simpleweibo/src/main/java/com/sina/weibo/simple/Status.java):

```java
@AutoJson
public abstract class Status implements android.os.Parcelable {
    @Nullable
    @AutoJson.Field(name = "created_at")
    public abstract String createdAt();
    @Nullable
    @AutoJson.Field
    public abstract String id();
    // ...
}
```

## Demo

* Sample code: [MainActivity.java](simpleweibo-app/src/main/java/com/sina/weibo/simple/app/MainActivity.java)
* apk: https://github.com/8tory/SimpleWeibo/releases/download/1.0.0/simpleweibo-app-debug.apk

## Installation

via jitpack:

```gradle
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.github.8tory.SimpleWeibo:simpleweibo:-SNAPSHOT'
}
```

via jcenter:

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'com.infstory:simpleweibo:1.0.1'
}
```

## See Also

* http://open.weibo.com/wiki/

## License

```
Copyright 2015 8tory, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
