package com.space.service;

import com.space.exceptions.BadRequestException;
import com.space.exceptions.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
public class ShipService {
    ShipRepository shipRepository;

    @Autowired
    public void setShipRepository(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Integer getShipCount(Specification<Ship> specifications) {
        return shipRepository.findAll(specifications).size();
    }

    public Page<Ship> getShipList(Specification<Ship> specifications, Pageable pageable) {
        return shipRepository.findAll(specifications, pageable);
    }

    public Ship getById(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new NotFoundException();
        }
        return shipRepository.findById(id).get();
    }

    public ResponseEntity<Ship> deleteById(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new NotFoundException();
        }
        shipRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Ship createNewShip(Ship ship) {
        if(ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null || ship.getProdDate() == null ||
           ship.getSpeed() == null || ship.getCrewSize() == null) throw new BadRequestException();

        checkShipName(ship);
        checkShipPlanet(ship);
        checkShipProdDate(ship);
        checkShipSpeed(ship);
        checkShipCrewCount(ship);

        if (ship.getUsed() == null) ship.setUsed(false);

        ship.setRating(shipRating(ship));

        return shipRepository.save(ship);
    }

    public Ship updateExistingShip(Long id, Ship ship) {
        Ship updatedShip = getById(id);

        String name = ship.getName();
        String planet = ship.getPlanet();
        ShipType shipType = ship.getShipType();
        Date date = ship.getProdDate();
        Boolean isUsed = ship.getUsed();
        Double speed = ship.getSpeed();
        Integer crewSize = ship.getCrewSize();

        if(name != null) {
            checkShipName(ship);
            updatedShip.setName(name);
        }
        if(planet != null) {
            checkShipPlanet(ship);
            updatedShip.setPlanet(planet);
        }
        if(shipType != null) {
            updatedShip.setShipType(shipType);
        }
        if(date != null) {
            checkShipProdDate(ship);
            updatedShip.setProdDate(date);
        }
        if(isUsed != null) {
            updatedShip.setUsed(isUsed);
        }
        if(speed != null) {
            checkShipSpeed(ship);
            updatedShip.setSpeed(speed);
        }
        if(crewSize != null) {
            checkShipCrewCount(ship);
            updatedShip.setCrewSize(crewSize);
        }

        updatedShip.setRating(shipRating(updatedShip));

        return shipRepository.save(updatedShip);
    }

    public Specification<Ship> filterByName(String name) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (name == null) {
                    return null;
                }
                return criteriaBuilder.like(ship.get("name"), "%" + name + "%");
            }
        };
    }

    public Specification<Ship> filterByPlanet(String planet) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (planet == null) {
                    return null;
                }
                return criteriaBuilder.like(ship.get("planet"), "%" + planet + "%");
            }
        };
    }

    public Specification<Ship> filterByShipType(ShipType shipType) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (shipType == null) {
                    return null;
                }
                return criteriaBuilder.equal(ship.get("shipType"), shipType);
            }
        };
    }

    public Specification<Ship> filterByUsed(Boolean isUsed) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (isUsed == null) {
                    return null;
                }
                return criteriaBuilder.equal(ship.get("isUsed"), isUsed);
            }
        };
    }

    public Specification<Ship> filterByProdDate(Long after, Long before) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (before == null && after == null) {
                    return null;
                }
                if (before == null) {
                    Date dateAfter = new Date(after);
                    return criteriaBuilder.greaterThanOrEqualTo(ship.get("prodDate"), dateAfter);
                }
                if (after == null) {
                    Date dateBefore = new Date(before);
                    return criteriaBuilder.lessThanOrEqualTo(ship.get("prodDate"), dateBefore);
                }

                Date dateAfter = new Date(after);
                Date dateBefore = new Date(before);

                return criteriaBuilder.between(ship.get("prodDate"), dateAfter, dateBefore);
            }
        };
    }

    public Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (maxSpeed == null && minSpeed == null) {
                    return null;
                }
                if (maxSpeed == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(ship.get("speed"), minSpeed);
                }
                if (minSpeed == null) {
                    return criteriaBuilder.lessThanOrEqualTo(ship.get("speed"), maxSpeed);
                }

                return criteriaBuilder.between(ship.get("speed"), minSpeed, maxSpeed);
            }
        };
    }

    public Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (maxCrewSize == null && minCrewSize == null) {
                    return null;
                }
                if (maxCrewSize == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(ship.get("crewSize"), minCrewSize);
                }
                if (minCrewSize == null) {
                    return criteriaBuilder.lessThanOrEqualTo(ship.get("crewSize"), maxCrewSize);
                }

                return criteriaBuilder.between(ship.get("crewSize"), minCrewSize, maxCrewSize);
            }
        };
    }

    public Specification<Ship> filterByRating(Double minRating, Double maxRating) {
        return new Specification<Ship>() {
            public Predicate toPredicate(Root<Ship> ship,
                                         CriteriaQuery<?> criteriaQuery,
                                         CriteriaBuilder criteriaBuilder) {
                if (maxRating == null && minRating == null) {
                    return null;
                }
                if (maxRating == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(ship.get("rating"), minRating);
                }
                if (minRating == null) {
                    return criteriaBuilder.lessThanOrEqualTo(ship.get("rating"), maxRating);
                }

                return criteriaBuilder.between(ship.get("rating"), minRating, maxRating);
            }
        };
    }

    private double shipRating(Ship ship) {
        Date prodDate = ship.getProdDate();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(prodDate);
        int date = calendar.get(Calendar.YEAR);

        double coefficient = ship.getUsed() ? 0.5 : 1;
        double rating = (80 * ship.getSpeed() * coefficient) / (3019 - date + 1);

        BigDecimal bd = new BigDecimal(rating).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void checkShipName(Ship ship) {
        String name = ship.getName();
        if (name.length() < 1 || name.length() > 50) {
            throw new BadRequestException();
        }
    }

    private void checkShipPlanet(Ship ship) {
        String planet = ship.getPlanet();
        if (planet.length() < 1 || planet.length() > 50) {
            throw new BadRequestException();
        }
    }

    private void checkShipProdDate(Ship ship) {
        Date prodDate = ship.getProdDate();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(prodDate);
        int date = calendar.get(Calendar.YEAR);

        if (date < 2800 || date > 3019) {
            throw new BadRequestException();
        }
    }

    private void checkShipSpeed(Ship ship) {
        double speed = ship.getSpeed();
        if (speed < 0.01 || speed > 0.99) {
            throw new BadRequestException();
        }
    }

    private void checkShipCrewCount(Ship ship) {
        int crewSize = ship.getCrewSize();
        if (crewSize < 1 || crewSize > 9999) {
            throw new BadRequestException();
        }
    }
}
