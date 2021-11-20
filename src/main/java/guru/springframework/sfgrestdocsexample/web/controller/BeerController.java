package guru.springframework.sfgrestdocsexample.web.controller;

import guru.springframework.sfgrestdocsexample.domain.Beer;
import guru.springframework.sfgrestdocsexample.repositories.BeerRepository;
import guru.springframework.sfgrestdocsexample.web.mappers.BeerMapper;
import guru.springframework.sfgrestdocsexample.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Created by jt on 2019-05-12.
 */
@RequiredArgsConstructor
@RequestMapping("/api/v1/beer")
@RestController
@Slf4j
public class BeerController {

    private final BeerMapper beerMapper;
    private final BeerRepository beerRepository;

    @GetMapping("/{beerId}")
    public ResponseEntity<BeerDto> getBeerById(@PathVariable("beerId") UUID beerId) {
        return new ResponseEntity<>(beerMapper.BeerToBeerDto(beerRepository.findById(beerId).get()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity saveNewBeer(@RequestBody @Validated BeerDto beerDto) {
        Beer newBeer = beerRepository.save(beerMapper.BeerDtoToBeer(beerDto));
        log.info("saveNewBeer - newBeer: " + newBeer);
        BeerDto newBeerDto = beerMapper.BeerToBeerDto(newBeer);
        log.info("saveNewBeer - newBeerDto: " + newBeerDto);
        return new ResponseEntity(newBeerDto, HttpStatus.CREATED);
    }

    @PutMapping("/{beerId}")
    public ResponseEntity updateBeerById(@PathVariable("beerId") UUID beerId, @RequestBody @Validated BeerDto beerDto) {
        log.info("updateBeerById - beerId: " + beerId);
        log.info("updateBeerById - beerDto: " + beerDto);
        beerRepository.findById(beerId).ifPresent(beer -> {
            beer.setBeerName(beerDto.getBeerName());
            beer.setBeerStyle(beerDto.getBeerStyle().name());
            beer.setPrice(beerDto.getPrice());
            beer.setUpc(beerDto.getUpc());
            log.info("updateBeerById - updatedBeer: " + beerRepository.save(beer));
        });

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
