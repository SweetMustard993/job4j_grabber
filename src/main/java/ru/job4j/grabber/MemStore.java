package ru.job4j.grabber;

import java.util.ArrayList;
import java.util.List;

public class MemStore implements Store {

    private final ArrayList<Post> posts = new ArrayList<>();
    private int ids = 1;

    private int indexOf(int id) {
        int index = -1;
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void save(Post post) {
        post.setId(ids++);
        posts.add(post);
    }

    @Override
    public List<Post> getAll() {
        return new ArrayList<>(posts);
    }

    @Override
    public Post findById(int id) {
        int index = indexOf(id);
        return index != -1 ? posts.get(index) : null;
    }
}
