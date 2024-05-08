package ru.iu3.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.iu3.backend.model.Museum;
import ru.iu3.backend.model.Painting;
import ru.iu3.backend.repository.MuseumRepository;
import ru.iu3.backend.tools.DataValidationException;

import java.util.*;


/**
 * Класс - контроллер музея
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("api/v1")
public class MuseumsController {
    // Репозиторий нашего музея
    @Autowired
    MuseumRepository museumRepository;

    /**
     * Метод, который выдаёт список музеев
     * @return - список музеев, представленный в формате JSON
     */
    @GetMapping("/museums")
    public Page<Museum> getAllMuseums(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return museumRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/museums/{id}")
    public ResponseEntity<Museum> getMuseum(@PathVariable(value = "id") Long museumID)
            throws DataValidationException {
        Museum museum = museumRepository.findById(museumID)
                .orElseThrow(() -> new DataValidationException("Музей не найден"));

        return ResponseEntity.ok(museum);
    }

    /**
     * Метод, который осуществляет предоставление доступа к картине из вывода музея
     * @param museumID - ID музея
     * @return - блок музеев, если таковы есть
     */
    @GetMapping("/museums/{id}/paintings")
    public ResponseEntity<List<Painting>> getPaintingMuseums(@PathVariable(value = "id") Long museumID) {
        Optional<Museum> cc = museumRepository.findById(museumID);
        if (cc.isPresent()) {
            return ResponseEntity.ok(cc.get().paintings);
        }

        return ResponseEntity.ok(new ArrayList<Painting>());
    }

    /**
     * Метод, который добавляет country в таблиц
     * RequestBody - это наш экземпляр (через curl передаётся в виде JSON)
     * @param museum - наш экземпляр класса museum
     * @return - статус (ОК/НЕ ОК)
     */
    @PostMapping("/museums")
    public ResponseEntity<Object> createMuseum(@RequestBody Museum museum) throws DataValidationException {
        try {
            // Попытка сохранить что-либо в базу данных
            Museum newMusem = museumRepository.save(museum);
            return new ResponseEntity<Object>(newMusem, HttpStatus.OK);
        } catch (Exception exception) {
            if (exception.getMessage().contains("countries.name_UNIQUE")) {
                throw new DataValidationException("Эта страна уже есть в базе");
            } else {
                throw new DataValidationException("Неизвестная ошибка");
            }
        }
    }

    /**
     * Метод, который обновляет данные в таблице
     * @param museumID - указываем id по которому будем обновлять данные
     * @param museumDetails - сводки по Museum
     * @return - ОК/НЕ ОК
     */
    @PutMapping("/museums/{id}")
    public ResponseEntity<Museum> updateMuseum(@PathVariable(value = "id") Long museumID,
            @RequestBody Museum museumDetails) throws DataValidationException {
        try {
            Museum museum = museumRepository.findById(museumID).
                    orElseThrow(() -> new DataValidationException("Музей с таким индексом не найдена"));

            museum.name = museumDetails.name;
            museum.location = museumDetails.location;

            museumRepository.save(museum);
            return ResponseEntity.ok(museum);
        } catch (Exception exception) {
            if (exception.getMessage().contains("ConstraintViolationException")) {
                throw new DataValidationException("Этот музей уже есть в базе");
            } else {
                throw new DataValidationException("Неизвестная ошибка");
            }
        }
    }

    @PostMapping("/deletemuseums")
    public ResponseEntity deleteMuseums(@Validated @RequestBody List<Museum> museums) {
        museumRepository.deleteAll(museums);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Метод, который удаляет информацию из базы данных
     * @param museumID - по какому ID-шнику удаляем информацию
     * @return - возвращает true, если удалено успешно, false - в противном случае
     */
    @DeleteMapping("/museums/{id}")
    public ResponseEntity<Object> deleteCountry(@PathVariable(value = "id") Long museumID) {
        Optional<Museum> museum = museumRepository.findById(museumID);
        Map<String, Boolean> resp = new HashMap<>();

        // Возвратит true, если объект существует (не пустой)
        if (museum.isPresent()) {
            museumRepository.delete(museum.get());
            resp.put("deleted", Boolean.TRUE);
        } else {
            resp.put("deleted", Boolean.FALSE);
        }

        return ResponseEntity.ok(resp);
    }
}