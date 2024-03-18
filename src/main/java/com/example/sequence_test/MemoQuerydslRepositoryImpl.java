package com.example.sequence_test;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.sequence_test.QMemo.memo;

@Repository
@RequiredArgsConstructor
public class MemoQuerydslRepositoryImpl implements MemoQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public int getLastSequenceOrDefault(int defaultValue) {

        JPAQuery<Memo> query = jpaQueryFactory
                .select(memo)
                .from(memo)
                .orderBy(memo.sequence.desc())
                .limit(1);

        Memo memo = query.fetchOne();

        return memo == null ? defaultValue : memo.getSequence();
    }

    @Override
    public List<Memo> findAllOrderBySequenceASC() {
        JPAQuery<Memo> query = jpaQueryFactory
                .select(memo)
                .from(memo)
                .orderBy(memo.sequence.asc());

        return query.fetch();
    }

    @Override
    public Optional<Memo> findByOffSetAndLimitOrderBySequenceASC(int offset, int limit) {
        JPAQuery<Memo> query = jpaQueryFactory
                .select(memo)
                .from(memo)
                .offset(offset)
                .limit(1)
                .orderBy(memo.sequence.asc());
        return Optional.ofNullable(query.fetchOne());
    }
}
