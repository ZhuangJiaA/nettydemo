package com.sixeco.nettydemo.dto;

import java.io.Serializable;

public class GoodsPush implements Serializable {
    private int id;
    private String title;
    private String introduction;
    private String cover;
    private int type;
    private String type_name;
    private int sub_type;
    private String sub_type_name;
    private String issuer;
    private String start_time;
    private String end_time;
    private String resource;
    private String model;
    private int state;
    private int priority;
    private int position;
    private int loop_mode;

    @Override
    public String toString() {
        return "GoodsPush{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", introduction='" + introduction + '\'' +
                ", cover='" + cover + '\'' +
                ", type=" + type +
                ", type_name='" + type_name + '\'' +
                ", sub_type=" + sub_type +
                ", sub_type_name='" + sub_type_name + '\'' +
                ", issuer='" + issuer + '\'' +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", resource='" + resource + '\'' +
                ", model='" + model + '\'' +
                ", state=" + state +
                ", priority=" + priority +
                ", position=" + position +
                ", loop_mode=" + loop_mode +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getCover() {
        return cover;
    }

    public int getType() {
        return type;
    }

    public String getType_name() {
        return type_name;
    }

    public int getSub_type() {
        return sub_type;
    }

    public String getSub_type_name() {
        return sub_type_name;
    }

    public String getIssuer() {
        return issuer;
    }



    public String getResource() {
        return resource;
    }

    public String getModel() {
        return model;
    }

    public int getState() {
        return state;
    }

    public int getPriority() {
        return priority;
    }

    public int getPosition() {
        return position;
    }

    public int getLoop_mode() {
        return loop_mode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public void setSub_type(int sub_type) {
        this.sub_type = sub_type;
    }

    public void setSub_type_name(String sub_type_name) {
        this.sub_type_name = sub_type_name;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setLoop_mode(int loop_mode) {
        this.loop_mode = loop_mode;
    }
}