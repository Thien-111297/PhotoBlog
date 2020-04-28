package kot.android.photoblog.model;

import com.google.firebase.firestore.Exclude;

public class BlogPostId {

    @Exclude
    public String blogPostId;

    public <T extends BlogPostId> T withId(final String id){
        this.blogPostId = id;
        return (T) this;
    }
}
