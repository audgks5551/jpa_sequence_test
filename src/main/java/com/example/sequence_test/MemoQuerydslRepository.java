package com.example.sequence_test;

import java.util.List;
import java.util.Optional;

public interface MemoQuerydslRepository {

    int getLastSequenceOrDefault(int defaultValue);

    List<Memo> findAllOrderBySequenceASC();

    Optional<Memo> findByOffSetAndLimitOrderBySequenceASC(int offset, int limit);
}
