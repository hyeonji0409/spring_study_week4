package com.artineer.spring_study_week2.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {
    @Test
    // Test는 물론 함수가 어떻게 동작하는지를 알려주는 문서의 역할을 하기 때문에 설명을 자세히 적어야 한다.
    public void update_호출하면_title_content_필드_값이_변경되어야_한다() {
        //given
        Article article = Article.builder()
                .title("title before")
                .content("content before")
                .build();

        //when
        article.update("title after", "content after");

        //then
        assertThat("title after").isEqualTo(article.getTitle());
        assertThat("content after").isEqualTo(article.getContent());
    }
}