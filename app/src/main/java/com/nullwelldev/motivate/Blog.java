package com.nullwelldev.motivate;

/**
 * Created by Chiru on 22-Nov-16.
 */

public class Blog {

    private String title, desc, image;

    public Blog(){

    }

    public Blog(String title, String desc, String image) {
        this.title = title;
        this.desc = desc;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImages(String image) {
        this.image = image;
    }
}
