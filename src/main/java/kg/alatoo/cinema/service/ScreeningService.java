package kg.alatoo.cinema.service;

import kg.alatoo.cinema.dao.FilmDao;
import kg.alatoo.cinema.dao.HallDao;
import kg.alatoo.cinema.dao.ScreeningDao;
import kg.alatoo.cinema.exception.NotFoundException;
import kg.alatoo.cinema.exception.ValidationException;
import kg.alatoo.cinema.model.Film;
import kg.alatoo.cinema.model.Hall;
import kg.alatoo.cinema.model.Screening;

import java.time.LocalDateTime;
import java.util.List;

/** Правила для сеансов: только в будущем и без наложения в одном зале. */
public class ScreeningService {
    private final ScreeningDao screeningDao;
    private final FilmDao filmDao;
    private final HallDao hallDao;

    public ScreeningService(ScreeningDao screeningDao, FilmDao filmDao, HallDao hallDao) {
        this.screeningDao = screeningDao;
        this.filmDao = filmDao;
        this.hallDao = hallDao;
    }

    public List<Hall> getAllHalls() {
        return hallDao.findAll();
    }

    public Screening addScreening(int filmId, int hallId, LocalDateTime start, double basePrice) {
        Film film = filmDao.findById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм #" + filmId + " не найден");
        }
        Hall hall = hallDao.findById(hallId);
        if (hall == null) {
            throw new NotFoundException("Зал #" + hallId + " не найден");
        }
        Screening screening = new Screening(film, hall, start, basePrice);
        checkRules(screening);
        return screeningDao.create(screening);
    }

    public List<Screening> getAllScreenings() {
        return screeningDao.findAll();
    }

    public List<Screening> getUpcomingScreenings() {
        return screeningDao.findUpcoming();
    }

    public Screening getScreening(int id) {
        Screening found = screeningDao.findById(id);
        if (found == null) {
            throw new NotFoundException("Сеанс #" + id + " не найден");
        }
        return found;
    }

    public List<Screening> searchByFilmTitle(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Введите текст для поиска");
        }
        return screeningDao.searchByFilmTitle(text.trim());
    }

    public Screening updateScreening(int id, LocalDateTime start, double basePrice) {
        Screening screening = getScreening(id);
        screening.setStartTime(start);
        screening.setBasePrice(basePrice);
        checkRules(screening);
        screeningDao.update(screening);
        return screening;
    }

    public void deleteScreening(int id) {
        if (!screeningDao.delete(id)) {
            throw new NotFoundException("Сеанс #" + id + " не найден");
        }
    }

    private void checkRules(Screening screening) {
        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Нельзя поставить сеанс в прошлое");
        }
        for (Screening other : screeningDao.findByHall(screening.getHall().getId())) {
            if (other.getId() != screening.getId() && screening.overlapsWith(other)) {
                throw new ValidationException("Зал занят: пересекается с сеансом " + other);
            }
        }
    }
}
