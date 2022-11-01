package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.Store;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());

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
            LOG.error("RuntimeException", e);
        }
    }

    @Override
    public void save(Post post) {
        String link = post.getLink();
        try (var statement = cnn.prepareStatement("insert into post(name, text, link, created)"
                + " values(?, ?, ?, ?) on conflict (link) do update set link = ?", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, link);
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.setString(5, link);
            statement.execute();
            try (ResultSet genKeys = statement.getGeneratedKeys()) {
                if (genKeys.next()) {
                    post.setId(genKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOG.error("RuntimeException", e);
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
            LOG.error("RuntimeException", e);
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
            LOG.error("RuntimeException", e);
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
