package com.sheronova.tl.service;

import com.sheronova.tl.model.Quest;
import com.sheronova.tl.repository.QuestRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QuestService {

    @NonNull
    private QuestRepository questRepository;

    @Transactional(readOnly = true)
    public Quest getQuestById(Integer id) {
        return questRepository.findById(id).orElse(null);
    }

}
