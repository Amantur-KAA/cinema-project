package kg.alatoo.cinema.service;

import kg.alatoo.cinema.dao.FilmDao;
import kg.alatoo.cinema.dao.GenreDao;
import kg.alatoo.cinema.exception.NotFoundException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Film;
import kg.alatoo.cinema.model.Genre;

import java.util.List;

/** Правила для фильмов и жанров. Меню вызывает сервис, сервис вызывает DAO. */
public class FilmService {
    private final FilmDao filmDao;
    private final GenreDao genreDao;

    public FilmService(FilmDao filmDao, GenreDao genreDao) {
        this.filmDao = filmDao;
        this.genreDao = genreDao;
    }

    public List<Genre> getAllGenres() {
        return genreDao.findAll();
    }

    public Genre getGenre(int id) {
        Genre found = genreDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Жанр #" + id + " не найден");
        }
        return found;
    }

    public Film addFilm(String title, int duration, int ageRating, int genreId) {
        Film film = new Film(title, duration, ageRating, getGenre(genreId)); // валидация в конструкторе
        checkTitleIsFree(film.getTitle(), 0);
        return filmDao.create(film);
    }

    public List<Film> getAllFilms() {
        return filmDao.findAll();
    }

    public Film getFilm(int id) {
        Film found = filmDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Фильм #" + id + " не найден");
        }
        return found;
    }

    public List<Film> searchFilms(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Введите текст для поиска");
        }
        return filmDao.searchByTitle(text.trim());
    }

    public Film updateFilm(int id, String title, int duration, int ageRating, int genreId) {
        Film film = getFilm(id);
        film.setTitle(title);
        checkTitleIsFree(film.getTitle(), id);
        film.setDurationMinutes(duration);
        film.setAgeRating(ageRating);
        film.setGenre(getGenre(genreId));
        filmDao.update(film);
        return film;
    }

    /** Два фильма с одинаковым названием запутают кассира и отчёты. */
    private void checkTitleIsFree(String title, int ownId) {
        for (Film other : filmDao.searchByTitle(title)) {
            if (other.getTitle().equalsIgnoreCase(title) && other.getId() != ownId) {
                throw new ValidationException("Фильм «" + title + "» уже есть (#" + other.getId() + ")");
            }
        }
    }

    public void deleteFilm(int id) {
        if (!filmDao.delete(id)) {
            throw new NotFoundException("Фильм #" + id + " не найден");
        }
    }
}
