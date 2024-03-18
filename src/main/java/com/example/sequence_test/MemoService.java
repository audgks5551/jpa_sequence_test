package com.example.sequence_test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;

    private final MemoQuerydslRepository memoQuerydslRepository;

    private final InternalMemoService internalMemoService;

    private final static int DEFAULT_ADD_INDEX = 1024;

    private final static int DEFAULT_LAST_SEQUENCE_DEFAULT_VALUE = 0;

    @Transactional
    public UUID createMemo(MemoCreateDTO dto) {
        Memo memo = Memo.builder()
                .id(UUID.randomUUID())
                .title(dto.getTitle())
                .content(dto.getContent())
                .sequence(getLastSequence() + DEFAULT_ADD_INDEX)
                .build();

        Memo savedMemo = memoRepository.save(memo);

        return savedMemo.getId();
    }

    @Transactional
    public List<Memo> findAll() {
        return memoQuerydslRepository.findAllOrderBySequenceASC();
    }

    private Integer getLastSequence() {
        return memoQuerydslRepository.getLastSequenceOrDefault(DEFAULT_LAST_SEQUENCE_DEFAULT_VALUE);
    }

    @Transactional
    public void changeMemoSequence(UUID memoId, int rank) {
        internalMemoService.changeSequence(memoId, rank);
    }

    @Transactional
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
//                reordering();
//                changeSequence(memoId, rank);
                throw new RuntimeException("에러");
            }

            changeMemo.updateSequence(newSequence);
            return;
        }

        // 순서를 낮추는 경우
        Optional<Memo> _before_memo = findByRank(rank-1);

        if (_before_memo.isEmpty()) {
            if (rankMemo.getSequence() == DEFAULT_LAST_SEQUENCE_DEFAULT_VALUE) {
//                reordering();
//                changeSequence(memoId, rank);
                throw new RuntimeException("에러");
            }
            changeMemo.updateSequence(rankMemo.getSequence() / 2);
            return;
        }

        Memo before_memo = _before_memo.get();

        int newSequence = (before_memo.getSequence() + rankMemo.getSequence()) / 2;
        if (newSequence == before_memo.getSequence()) {
//            reordering();
//            changeSequence(memoId, rank);
            throw new RuntimeException("에러");
        }
        changeMemo.updateSequence(newSequence);
    }

    private void reordering() {
        List<Memo> memos = findAll();

        IntStream.range(0, memos.size())
                .forEach(i -> {
                    Memo memo = memos.get(i);
                    memo.updateSequence(DEFAULT_ADD_INDEX * (i+1));
                });
    }

    private Memo findByIdElseThrow(UUID memoId) {
        return memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("memo를 찾을 수 없습니다."));
    }

    private Optional<Memo> findByRank(int rank) {
        if (rank < 0) {
            return Optional.empty();
        }

        return memoQuerydslRepository.findByOffSetAndLimitOrderBySequenceASC(rank, 1);
    }
}
