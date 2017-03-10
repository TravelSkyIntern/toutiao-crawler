package io.z77z.entity;

import java.util.Date;


import com.alibaba.fastjson.annotation.JSONField;

public class Funny {
    private Integer funnyId;

    private String articleGenre;

    @JSONField(format = "yyyy-MM-dd hh:mm:ss")
    private Date behotTime;

    private String chineseTag;

    private String groupId;

    private String hasGallery;

    private String imageUrl;

    private String isFeedAd;

    private String mediaAvatarUrl;

    private String mediaUrl;

    private String middleMode;

    private String moreMode;

    private String singleMode;

    private String source;

    private String sourceUrl;

    private String tag;

    private String tagUrl;

    private String title;

    private String commentsCount;

    private String document;

    public Integer getFunnyId() {
        return funnyId;
    }

    public void setFunnyId(Integer funnyId) {
        this.funnyId = funnyId;
    }

    public String getArticleGenre() {
        return articleGenre;
    }

    public void setArticleGenre(String articleGenre) {
        this.articleGenre = articleGenre;
    }

    public Date getBehotTime() {
        return behotTime;
    }

    public void setBehotTime(Date behotTime) {
        this.behotTime = behotTime;
    }

    public String getChineseTag() {
        return chineseTag;
    }

    public void setChineseTag(String chineseTag) {
        this.chineseTag = chineseTag;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getHasGallery() {
        return hasGallery;
    }

    public void setHasGallery(String hasGallery) {
        this.hasGallery = hasGallery;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIsFeedAd() {
        return isFeedAd;
    }

    public void setIsFeedAd(String isFeedAd) {
        this.isFeedAd = isFeedAd;
    }

    public String getMediaAvatarUrl() {
        return mediaAvatarUrl;
    }

    public void setMediaAvatarUrl(String mediaAvatarUrl) {
        this.mediaAvatarUrl = mediaAvatarUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMiddleMode() {
        return middleMode;
    }

    public void setMiddleMode(String middleMode) {
        this.middleMode = middleMode;
    }

    public String getMoreMode() {
        return moreMode;
    }

    public void setMoreMode(String moreMode) {
        this.moreMode = moreMode;
    }

    public String getSingleMode() {
        return singleMode;
    }

    public void setSingleMode(String singleMode) {
        this.singleMode = singleMode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTagUrl() {
        return tagUrl;
    }

    public void setTagUrl(String tagUrl) {
        this.tagUrl = tagUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}