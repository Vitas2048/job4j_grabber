package ru.job4j.grabber;

import ru.job4j.grabber.Post;
import ru.job4j.grabber.Store;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        String url = cfg.getProperty("url");
        String login = cfg.getProperty("login");
        String password = cfg.getProperty("password");
        try {
            cnn = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (var statement = cnn.prepareStatement("insert into post(name, text, link, created) values(?, ?, ?, ?)")) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (var statement = cnn.createStatement()) {
            var selection = statement.executeQuery("select * from post");
            while (selection.next()) {
                list.add(useSelection(selection));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (var statement = cnn.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            var selection = statement.executeQuery();
            if (selection.next()) {
                post = useSelection(selection);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post useSelection(ResultSet selection) throws SQLException {
        return new Post(selection.getInt("id"), selection.getString("name"), selection.getString("link"),
                selection.getString("text"), selection.getTimestamp("created").toLocalDateTime());
    }
}
