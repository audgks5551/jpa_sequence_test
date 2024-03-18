package com.example.sequence_test;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalMemoService {

    private final MemoRepository memoRepository;

    private final MemoQuerydslRepository memoQuerydslRepository;

    private final static int DEFAULT_ADD_INDEX = 1024;

    private final static int DEFAULT_LAST_SEQUENCE_DEFAULT_VALUE = 0;

    private final EntityManager entityManager;

    public void reordering() {
        entityManager.flush();
        entityManager.clear();
        entityManager.createNativeQuery("SET @SEQUENCE \\:= 0").executeUpdate();
        String query = """
                UPDATE memos SET sequence = (@SEQUENCE \\:= @SEQUENCE + 1024) ORDER BY sequence;
                """;
        entityManager.createNativeQuery(query).executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeSequence(UUID memoId, int rank) {
//        List<Memo> memos = findAll();
//
//        Memo memo = memos.stream().filter(m -> m.getId().equals(memoId))
//                .findFirst()
//                .get();
//
//        memos.remove(memo);
//        memos.add(rank, memo);

//        IntStream.range(0, memos.size())
//                .forEach(i -> {
//                    Memo m = memos.get(i);
//                    m.updateSequence(i+1);
//                });

        if (rank < 0) {
            throw new RuntimeException("rank가 0보다 작을 수 없습니다.");
        }

        Optional<Memo> _rankMemo = findByRank(rank);

        if (_rankMemo.isEmpty()) {
            throw new RuntimeException("%d번째의 rank의 메모가 존재하지 않습니다.".formatted(rank));
        }

        Memo rankMemo = _rankMemo.get();
        Memo changeMemo = findByIdElseThrow(memoId);

        // 동일한 메모일 경우
        if (rankMemo.equals(changeMemo)) {
            throw new RuntimeException("동일한 위치에 올 수 없습니다.");
        }

        // 순서를 올리는 경우
        if (changeMemo.getSequence() < rankMemo.getSequence()) {
            Optional<Memo> _after_memo = findByRank(rank + 1);

            if (_after_memo.isEmpty()) {
                changeMemo.updateSequence(rankMemo.getSequence() + DEFAULT_ADD_INDEX);
                return;
            }

            Memo afterMemo = _after_memo.get();
            int newSequence = (rankMemo.getSequence() + afterMemo.getSequence()) / 2;

            if (newSequence == rankMemo.getSequence()) {
                reordering();
                changeSequence(memoId, rank);
//                throw new RuntimeException("에러");
            }

            changeMemo.updateSequence(newSequence);
            return;
        }

        // 순서를 낮추는 경우
        Optional<Memo> _before_memo = findByRank(rank-1);

        if (_before_memo.isEmpty()) {
            if (rankMemo.getSequence() == DEFAULT_LAST_SEQUENCE_DEFAULT_VALUE) {
                reordering();
                changeSequence(memoId, rank);
//                throw new RuntimeException("에러");
            }
            changeMemo.updateSequence(rankMemo.getSequence() / 2);
            return;
        }

        Memo before_memo = _before_memo.get();

        int newSequence = (before_memo.getSequence() + rankMemo.getSequence()) / 2;
        if (newSequence == before_memo.getSequence()) {
            reordering();
            changeSequence(memoId, rank);
//            throw new RuntimeException("에러");
        }
        changeMemo.updateSequence(newSequence);
    }

    private Optional<Memo> findByRank(int rank) {
        if (rank < 0) {
            return Optional.empty();
        }

        return memoQuerydslRepository.findByOffSetAndLimitOrderBySequenceASC(rank, 1);
    }

    private Memo findByIdElseThrow(UUID memoId) {
        return memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("memo를 찾을 수 없습니다."));
    }
}
