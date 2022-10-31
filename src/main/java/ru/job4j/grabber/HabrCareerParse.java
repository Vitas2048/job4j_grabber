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

    public static final int PAGE_NUMBERS = 5;

    private final DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);


    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Element descriptionElement = document.select(".style-ugc").first();
        return descriptionElement.text();
    }

    public static void main(String[] args) {
        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = parse.list(PAGE_LINK);
        list.forEach(System.out::println);
    }

    private Post parsePost(Element dateElement, Element titleElement, Element linkElement) {
        LocalDateTime dateTime = dateTimeParser.parse(dateElement.child(0).attr("datetime"));
        String postLink = String.format("https://career.habr.com/%s", linkElement.attr("href"));
        String title = titleElement.text();
        String descript = retrieveDescription(postLink);
        return new Post(title, postLink, descript, dateTime);
    }

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < PAGE_NUMBERS; i++) {
        Connection connection = Jsoup.connect(String.format("%s?page=%s", link, i + 1));
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
                        Element linkElement = titleElement.child(0);
                        posts.add(parsePost(dateElement, titleElement, linkElement));
                    }
            );
            posts.forEach(p -> p.setId(posts.indexOf(p)));
        }
        return posts;
    }
}