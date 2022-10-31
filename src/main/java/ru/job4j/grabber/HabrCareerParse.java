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

    private final DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element dateElement = row.select(".vacancy-card__date").first();
            Element titleElement = row.select(".vacancy-card__title").first();
            LocalDateTime date = dateTimeParser.parse(dateElement.child(0).attr("datetime"));
            Element linkElement = titleElement.child(0);
            String postLink = String.format("https://career.habr.com/%s", linkElement.attr("href"));
            String title = titleElement.text();
            String descript;
            try {
                descript = retrieveDescription(postLink);
                } catch (IOException e) {
                throw new RuntimeException(e);
                }
                posts.add(new Post(title, postLink, descript, date));
            }
        );
        return posts;
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descriptionElement = document.select(".style-ugc").first();
        return descriptionElement.text();
    }

    public static void main(String[] args) throws IOException {
        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.addAll(parse.list((String.format("%s?page=%s", PAGE_LINK, i + 1))));
            list.forEach(p -> p.setId(list.indexOf(p)));
        }
        list.forEach(System.out::println);
    }
}