package kot.android.photoblog.model;

import java.util.Date;

public class Notifications {
    private String message;
    private String title;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Notifications(String message, String title, Date date) {
        this.message = message;
        this.title = title;
        this.date = date;
    }

    public Notifications() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
