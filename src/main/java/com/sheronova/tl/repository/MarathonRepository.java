package com.sheronova.tl.repository;

import com.sheronova.tl.model.Quest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MarathonRepository {

    @NonNull
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearOldData(Integer userId) {
        String query = "UPDATE tl_marathon SET result = 0 WHERE user_id = " + userId;
        jdbcTemplate.update(query);
    }

    @Transactional
    public void fillMarathonTable(Integer userId) {
        String query = "INSERT INTO tl_marathon (user_id, quest_id, result) " +
                "SELECT " + userId + ", id, 0 FROM tl_quests";
        jdbcTemplate.update(query);
    }

    @Transactional(readOnly = true)
    public Integer getRandomQuestId(Integer userId) {
        String query = "SELECT quest_id FROM tl_marathon WHERE result = 0 AND user_id = " +
                 + userId + " ORDER BY random() LIMIT 1";
        return jdbcTemplate.queryForObject(query, Integer.class);
    }

    @Transactional
    public void setQuestResult(Integer userId, Integer questId, Integer result) {
        String query = "UPDATE tl_marathon SET result = " + result +
                " WHERE quest_id = " + questId + " AND user_id = " + userId;
        jdbcTemplate.update(query);
    }

    @Transactional(readOnly = true)
    public Pair<Integer, Integer> getCorrectAndWrongCount(Integer userID) {
        String correctCount = "SELECT count(*) FROM tl_marathon " +
                " WHERE result = 1 AND user_id = " + userID;
        Integer correct = jdbcTemplate.queryForObject(correctCount, Integer.class);
        String wrongCount = "SELECT count(*) FROM tl_marathon " +
                " WHERE result = -1 AND user_id = " + userID;
        Integer wrong = jdbcTemplate.queryForObject(wrongCount, Integer.class);
        return Pair.of(correct, wrong);
    }
}
