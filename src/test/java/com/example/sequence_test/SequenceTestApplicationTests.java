package com.example.sequence_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SequenceTestApplicationTests {

    @Autowired
    private MemoService memoService;

    private List<UUID> memoIdList;

    @BeforeEach
    void init() {
        memoIdList = IntStream.range(0, 100)
                .mapToObj(i -> {
                    MemoCreateDTO request = MemoCreateDTO.builder()
                            .title("제목" + i + 1)
                            .content("내용1" + i + 1)
                            .build();
                    return memoService.createMemo(request);
                })
                .collect(Collectors.toList());
    }

    @Test
    void createMemo() {
        List<Memo> memos = memoService.findAll();

        assertThat(memos.get(0).getSequence()).isEqualTo(1024);
        assertThat(memos.get(1).getSequence()).isEqualTo(2048);
    }

    @Test
    @DisplayName("4번째 메모를 2번째 메모로 옮기기")
    void changeSequenceTest1() {
        memoService.changeSequence(memoIdList.get(3), 1);

        List<Memo> memos = memoService.findAll();
        assertThat(memos.get(1).getId()).isEqualTo(memoIdList.get(3));
    }

    @Test
    @DisplayName("4번째 메모를 1번째 메모로 옮기기")
    void changeSequenceTest2() {
        memoService.changeSequence(memoIdList.get(3), 0);

        List<Memo> memos = memoService.findAll();
        assertThat(memos.get(0).getId()).isEqualTo(memoIdList.get(3));
    }

    @Test
    @DisplayName("1번째 메모를 4번째 메모로 옮기기")
    void changeSequenceTest3() {
        memoService.changeSequence(memoIdList.get(0), 3);

        List<Memo> memos = memoService.findAll();
        assertThat(memos.get(3).getId()).isEqualTo(memoIdList.get(0));
    }

    @Test
    @DisplayName("재배열 발생시키기")
    void changeSequenceTest4() {

        Collections.reverse(memoIdList);

//        memoIdList.forEach(memoId -> memoService.changeSequence(memoId, 0));
        IntStream.range(0, 30).forEach(i -> memoService.changeMemoSequence(memoIdList.get(50 - i), 0));

        Collections.reverse(memoIdList);

        List<Memo> memos = memoService.findAll();

//        IntStream.range(0, 1000).forEach(i -> assertThat(memos.get(i).getId()).isEqualTo(memoIdList.get(i)));

    }

}
