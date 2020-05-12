package com.space.controller;

import com.space.exceptions.BadRequestException;
import com.space.exceptions.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {
    private ShipService shipService;

    @Autowired
    public void setShipService(ShipService shipService) {
        this.shipService = shipService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> getShip(@PathVariable("id") Long shipId) {
        if(shipId <= 0) throw new BadRequestException();

        Ship ship = shipService.getById(shipId);

        if(ship == null) throw new NotFoundException();

        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long shipId) {
        if(shipId <= 0) throw new BadRequestException();

        return shipService.deleteById(shipId);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if(ship == null) throw new BadRequestException();

        Ship createdShip = shipService.createNewShip(ship);
        return new ResponseEntity<>(createdShip, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long shipId, @RequestBody Ship ship) {
        if(shipId <= 0) throw new BadRequestException();

        Ship updatedShip = shipService.updateExistingShip(shipId, ship);

        return new ResponseEntity<>(updatedShip, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<Ship>> getShipsList(@RequestParam(name = "name", required = false) String name,
                                                   @RequestParam(name = "planet", required = false) String planet,
                                                   @RequestParam(name = "shipType", required = false) ShipType shipType,
                                                   @RequestParam(name = "after", required = false) Long dateAfter,
                                                   @RequestParam(name = "before", required = false) Long dateBefore,
                                                   @RequestParam(name = "isUsed", required = false) Boolean isUsed,
                                                   @RequestParam(name = "minSpeed", required = false) Double minSpeed,
                                                   @RequestParam(name = "maxSpeed", required = false) Double maxSpeed,
                                                   @RequestParam(name = "minCrewSize", required = false) Integer minCrewSize,
                                                   @RequestParam(name = "maxCrewSize", required = false) Integer maxCrewSize,
                                                   @RequestParam(name = "minRating", required = false) Double minRating,
                                                   @RequestParam(name = "maxRating", required = false) Double maxRating,
                                                   @RequestParam(name = "order", required = false, defaultValue = "ID") ShipOrder shipOrder,
                                                   @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                                   @RequestParam(name = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        Specification<Ship> shipSpecification = Specification.where(shipService.filterByName(name))
                        .and(shipService.filterByPlanet(planet)
                        .and(shipService.filterByShipType(shipType))
                        .and(shipService.filterByProdDate(dateAfter, dateBefore))
                        .and(shipService.filterByUsed(isUsed))
                        .and(shipService.filterBySpeed(minSpeed, maxSpeed))
                        .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
                        .and(shipService.filterByRating(minRating, maxRating)));

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(shipOrder.getFieldName()));

        return new ResponseEntity<>(shipService.getShipList(shipSpecification, pageable).getContent(), HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Integer> getShipsCount(@RequestParam(name = "name", required = false) String name,
                                                 @RequestParam(name = "planet", required = false) String planet,
                                                 @RequestParam(name = "shipType", required = false) ShipType shipType,
                                                 @RequestParam(name = "after", required = false) Long dateAfter,
                                                 @RequestParam(name = "before", required = false) Long dateBefore,
                                                 @RequestParam(name = "isUsed", required = false) Boolean isUsed,
                                                 @RequestParam(name = "minSpeed", required = false) Double minSpeed,
                                                 @RequestParam(name = "maxSpeed", required = false) Double maxSpeed,
                                                 @RequestParam(name = "minCrewSize", required = false) Integer minCrewSize,
                                                 @RequestParam(name = "maxCrewSize", required = false) Integer maxCrewSize,
                                                 @RequestParam(name = "minRating", required = false) Double minRating,
                                                 @RequestParam(name = "maxRating", required = false) Double maxRating) {

        Specification<Ship> shipSpecification = Specification.where(shipService.filterByName(name))
                .and(shipService.filterByPlanet(planet)
                .and(shipService.filterByShipType(shipType))
                .and(shipService.filterByProdDate(dateAfter, dateBefore))
                .and(shipService.filterByUsed(isUsed))
                .and(shipService.filterBySpeed(minSpeed, maxSpeed))
                .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
                .and(shipService.filterByRating(minRating, maxRating)));

        return new ResponseEntity<>(shipService.getShipCount(shipSpecification), HttpStatus.OK);
    }
}
