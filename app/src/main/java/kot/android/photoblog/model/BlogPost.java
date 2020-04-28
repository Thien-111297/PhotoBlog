package kot.android.photoblog.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class BlogPost extends BlogPostId{
    private String user_id;
    private String thumb;
    private String description;
    private String image_url;
    private Date timestamp;

    public BlogPost(String user_id, String thumb, String description, String image_url, Date timestamp) {
        this.user_id = user_id;
        this.thumb = thumb;
        this.description = description;
        this.image_url = image_url;
        this.timestamp = timestamp;
    }

    public BlogPost() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
