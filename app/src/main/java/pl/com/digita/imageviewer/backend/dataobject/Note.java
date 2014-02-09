package pl.com.digita.imageviewer.backend.dataobject;

import com.google.gson.annotations.Expose;

/**
 * Data object for Note
 */
public class Note {

    @Expose
    private int id;

    @Expose
    private String title;

    @Expose
    private String pictureUrl;

    public int getId() {
        return id;
    }

    public void setId(int pId) {
        id = pId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pPictureUrl) {
        pictureUrl = pPictureUrl;
    }
}
