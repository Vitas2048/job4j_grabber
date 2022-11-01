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

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String PAGE_NUM = PAGE_LINK.concat("?page=%s");

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

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
        List<Post> list = parse.list(PAGE_NUM);
        list.forEach(System.out::println);
    }

    private Post parsePost(Element element) {
        Element dateElement = element.select(".vacancy-card__date").first();
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        LocalDateTime dateTime = dateTimeParser.parse(dateElement.child(0).attr("datetime"));
        String postLink = String.format("https://career.habr.com/%s", linkElement.attr("href"));
        String title = titleElement.text();
        String descript = retrieveDescription(postLink);
        return new Post(title, postLink, descript, dateTime);
    }


    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGE_NUMBERS; i++) {
        Connection connection = Jsoup.connect(String.format(link, i + 1));
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> posts.add(parsePost(row)));
        }
        return posts;
    }
}