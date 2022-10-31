package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descriptionElement = document.select(".style-ugc").first();
        return descriptionElement.text();
    }

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 5; i++) {
            Connection connection = Jsoup.connect(String.format("%s?page=%s", PAGE_LINK, i + 1));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element linkElement = titleElement.child(0);
                Element dateElement1 = dateElement.child(0);
                String vacancyName = titleElement.text();
                String lDate = String.format("%s", dateElement1.attr("datetime"));
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s%n %s%n", vacancyName, link, lDate);
            });
        }
    }
}