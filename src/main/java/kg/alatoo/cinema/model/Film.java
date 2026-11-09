package kg.alatoo.cinema.model;

import kg.alatoo.cinema.exception.ValidationException;

import java.util.Objects;

/**
 * Фильм (таблица films).
 * Жанр хранится как объект Genre, а не как строка (композиция, неделя 2).
 */
public class Film {
    private int id;
    private String title;
    private int durationMinutes;
    private int ageRating;
    private Genre genre;

    public Film(int id, String title, int durationMinutes, int ageRating, Genre genre) {
        this.id = id;
        setTitle(title);
        setDurationMinutes(durationMinutes);
        setAgeRating(ageRating);
        setGenre(genre);
    }

    /** Новый фильм, которого ещё нет в базе (id = 0). */
    public Film(String title, int durationMinutes, int ageRating, Genre genre) {
        this(0, title, durationMinutes, ageRating, genre);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (title.trim().length() > 150) {
            throw new ValidationException("Название фильма слишком длинное (максимум 150 символов)");
        }
        this.title = title.trim();
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes < 1 || durationMinutes > 400) {
            throw new ValidationException("Длительность фильма должна быть от 1 до 400 минут");
        }
        this.durationMinutes = durationMinutes;
    }

    public int getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(int ageRating) {
        if (ageRating != 0 && ageRating != 6 && ageRating != 12 && ageRating != 16 && ageRating != 18) {
            throw new ValidationException("Возрастной рейтинг может быть только 0, 6, 12, 16 или 18");
        }
        this.ageRating = ageRating;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        if (genre == null) {
            throw new ValidationException("У фильма должен быть жанр");
        }
        this.genre = genre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Film other = (Film) obj;
        return id == other.id && durationMinutes == other.durationMinutes
                && ageRating == other.ageRating && Objects.equals(title, other.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, durationMinutes, ageRating);
    }

    @Override
    public String toString() {
        return "#" + id + " " + title + " (" + genre + ", " + durationMinutes + " мин, " + ageRating + "+)";
    }
}
