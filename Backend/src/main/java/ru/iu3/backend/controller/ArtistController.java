package ru.iu3.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.model.Artist;
import ru.iu3.backend.model.Country;
import ru.iu3.backend.repository.ArtistRepository;
import ru.iu3.backend.repository.CountryRepository;
import ru.iu3.backend.tools.DataValidationException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class ArtistController {
    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    CountryRepository countryRepository;

    @GetMapping("/artists")
    public Page getAllArtists(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return artistRepository.findAll(
                PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity getArtist(@PathVariable(value = "id") Long artistId) throws DataValidationException {
        Artist artists = artistRepository.findById(artistId).
                orElseThrow(() -> new DataValidationException("Художник с таким индексом не найден"));
        return ResponseEntity.ok(artists);
    }

    @PostMapping("/artists")
    public ResponseEntity<Object> createArtist(@RequestBody Artist artist)
            throws Exception {
        try {
            // Попытка сохранить что-либо в базу данных
            Artist newArtists = artistRepository.save(artist);
            return new ResponseEntity<Object>(newArtists, HttpStatus.OK);
        }
        catch(Exception exception) {
            if (exception.getMessage().contains("artists.name_UNIQUE")) {
                throw new DataValidationException("Эта страна уже есть в базе");
            } else {
                throw new DataValidationException("Неизвестная ошибка");
            }
        }
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable(value = "id") Long artistId,
            @RequestBody Artist artistDetails) {
        Artist Artist = null;
        Optional<Artist>
                cc = artistRepository.findById(artistId);
        if (cc.isPresent()) {
            Artist = cc.get();
            Artist.name = artistDetails.name;
            artistRepository.save(Artist);
            return ResponseEntity.ok(Artist);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found");
        }
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Object> deleteArtist(@PathVariable(value = "id") Long artistId) {
        Optional<Artist>
                Artist = artistRepository.findById(artistId);
        Map<String, Boolean>
                resp = new HashMap<>();
        if (Artist.isPresent()) {
            artistRepository.delete(Artist.get());
            resp.put("deleted", Boolean.TRUE);
        }
        else
            resp.put("deleted", Boolean.FALSE);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/deleteartists")
    public ResponseEntity deleteArtists(@Valid @RequestBody List artists) {
        artistRepository.deleteAll(artists);
        return new ResponseEntity(HttpStatus.OK);
    }
}