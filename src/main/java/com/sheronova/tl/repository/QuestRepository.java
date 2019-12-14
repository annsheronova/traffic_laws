package com.sheronova.tl.repository;

import com.sheronova.tl.model.Quest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestRepository extends CrudRepository<Quest, Integer> {

}
