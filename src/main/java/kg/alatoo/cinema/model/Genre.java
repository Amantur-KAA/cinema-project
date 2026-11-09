package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

import java.util.Objects;

/** Жанр фильма (таблица genres). */
public class Genre {
    private int id;
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        setName(name);
    }

    public Genre(String name) {
        this(0, name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название жанра не может быть пустым");
        }
        this.name = name.trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Genre other = (Genre) obj;
        return id == other.id && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
