package com.sheronova.tl.service;

import com.sheronova.tl.model.Quest;
import com.sheronova.tl.repository.MarathonRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MarathonService {

    @NonNull
    private UserService userService;

    @NonNull
    private QuestService questService;

    @NonNull
    private MarathonRepository marathonRepository;


    @Transactional
    public void beginMarathon(Integer userId) {
        marathonRepository.clearOldData(userId);
    }

    @Transactional
    public void insertNewData(Integer userId) {
        marathonRepository.fillMarathonTable(userId);
    }

    @Transactional
    public Quest getQuest(Integer userId) {
        Integer questId = marathonRepository.getRandomQuestId(userId);
        if (questId == null) {
            return null;
        }
        return questService.getQuestById(questId);
    }

    @Transactional
    public Pair<Integer, Integer> finishMarathon(Integer userID) {
        Pair<Integer, Integer> res = marathonRepository.getCorrectAndWrongCount(userID);
        marathonRepository.clearOldData(userID);
        return res;
    }

    @Transactional
    public void setQuestResult(Integer questId, Integer id, int result) {
        marathonRepository.setQuestResult(id, questId, result);
    }
}
