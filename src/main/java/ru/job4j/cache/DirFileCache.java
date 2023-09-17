package ru.job4j.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirFileCache extends AbstractCache<String, String> {

    private final String cachingDir;

    public DirFileCache(String cachingDir) {
        this.cachingDir = cachingDir;
    }

    @Override
    protected String load(String key) {
        Path name = Paths.get(cachingDir, key);
        String res = null;
        if (Files.exists(name)) {
            try {
                res = Files.readString(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (res == null) {
            System.out.println("Файл отсутствует в директории");
        }
        return res;
    }

}