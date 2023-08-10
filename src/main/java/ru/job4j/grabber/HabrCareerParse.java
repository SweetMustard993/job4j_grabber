package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final int PAGE_QUALITY = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        return rows.text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGE_QUALITY; i++) {
            String pageURL = String.format("%s%s", PAGE_LINK, i);
            Connection connection = Jsoup.connect(pageURL);
            Document document = null;
            try {
                document = connection.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert document != null;
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateCard = row.child(0);
                Element date = dateCard.child(0);
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                LocalDateTime postDate = dateTimeParser.parse(date.attr("datetime"));
                String postTitle = titleElement.text();
                String postLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String postDescription = null;
                try {
                    postDescription = retrieveDescription(postLink);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                posts.add(Post.of(postTitle, postLink, postDescription, postDate));
            });
        }
        return posts;
    }
}