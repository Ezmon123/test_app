package com.project.psedataconverter;

import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.repository.DemandForPowerRepository;
import com.project.psedataconverter.service.DemandForPowerService;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

/**
 * assumptions:
 * entity without id is treated as new entity,
 * enity with id is treated as entity that must be update
 */
public class InMemoryDemandForPowerRepository implements DemandForPowerRepository{

    protected Map<Long, DemandForPower> database = new LinkedHashMap<>();

    @Override
    public <S extends DemandForPower> List<S> saveAll(Iterable<S> entities) {
        int size = (int)entities.spliterator().getExactSizeIfKnown();
        Long counter =(long) 1;
        for (DemandForPower demandForPower : entities) {
            demandForPower.setId(counter);
            database.put(counter, demandForPower);
            counter++;
        }
        return (List<S>) entities;
    }

    @Override
    public<S extends DemandForPower> S save(S demandForPower) {
        if (demandForPower.getId() == null) {
            int size = database.size();
            Long id = (long) size + 1;
            demandForPower.setId(id);
            database.put(id, demandForPower);
        } else {
            database.replace(demandForPower.getId(), demandForPower);
        }

        return demandForPower;
    }

    @Override
    public List<DemandForPower> findByActualPowerDemandIsNullOrderByIdAsc() {
        List<DemandForPower> demandForPowerList = new ArrayList<>();
        for (Long key : database.keySet()) {
            DemandForPower record = database.get(key);
            if (record.getActualPowerDemand() == null) {
                demandForPowerList.add(record);
            }
        }
        return demandForPowerList;
    }

    @Override
    public DemandForPower findTopByOrderByIdDesc() {
        List<Map.Entry<Long, DemandForPower>> entryList = new ArrayList<Map.Entry<Long, DemandForPower>>(database.entrySet());
        Map.Entry<Long, DemandForPower> lastEntry = entryList.get(entryList.size() - 1);
        Long key = lastEntry.getKey();
        return database.get(key);
    }

    @Override
    public List<DemandForPower> findAll() {
        List<DemandForPower> demandForPowerList = new ArrayList<>();
        for (Long id : database.keySet()) {
            demandForPowerList.add(database.get(id));
        }
        return demandForPowerList;
    }


    @Override
    public Optional<DemandForPower> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<DemandForPower> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(DemandForPower entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends DemandForPower> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
