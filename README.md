# spring_study_week4

1. 테스트 코드

## 테스트 코드의 작성
* 테스트코드를 작성할 수 있는 부분은 많지만 도메인 객체는 의존성을 가지는 부분이 없기 때문에 테스트 코드를 입력하기 심플함.
* 객체 안에 커서를 두고 Ctrl + Shift + T를 누르면 테스트코드 작성을 위한 팁이 나옴.
* given, when, then으로 나누어 작성

## 테스트의 종류
+ 유닛 테스트(개발자들에게 중요)
  + 함수, 기능이 정상적으로 동작하는지를 보여주기 위한 코드(검증의 목표)
  + 가장 작은 단위. 메서드, 함수별로 테스트
+ 통합 테스트
  + 함수들이 모인 전체 모듈(전체 코드, DB 등의 외부환경, 실행되는 환경)이 정상적으로 작동하는지 확인하는 테스트
  + 스프링에서는 하나의 API를 진입부터 마지막까지 테스트하는 것을 의미
+ 인수 테스트
  + 고객의 요구사항을 충실히 구현했는지 고객이 직접 진행하는 테스트
+ 시스템 테스트
  + 제품 완성 후 배포까지 한 상태에서 진행하는 테스트

## TDD
+ 테스트 코드를 먼저 작성하고 함수의 스펙을 정하는 것
  + 먼저 실패하게 되는 코드를 만들어서 확인해보고, 성공하는 코드를 만들면 됨
+ 테스트 코드가 성공 가능하도록 소스코드를 작성해야 함

### Article.update() 테스트 코드 작성
```java
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
```
```java
public class Article {
    // ...
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
    // ...
}
```
### Article.of() 테스트 코드 작성
```java
class ArticleTest {
    // ...
  
    @Test
    public void of_호출하면_Article_객체를_반환해야_한다() {
        // given
        ArticleDto.ReqPost request = ArticleDto.ReqPost.builder()
                .title("title")
                .content("content")
                .build();

        // when
        Article article = Article.of(request);

        // then
        assertThat(article.getTitle()).isEqualTo("title");
        assertThat(article.getContent()).isEqualTo("content");
    }
}

```
## 작성한 테스트코드 한번에 확인하기
+ gradle -> verificaton -> test
+ CI/CD 환경을 구축하게 되면 형상이 PUSH 되었을 때 지속적으로 테스트 코드를 확인하게 해서 이상이 없는지 오류 검출

### 서비스 계층 유닛 테스트
+ Service에 해당하는 계층을 테스트하는 유닛 테스트
+ 다른 Service나 Repository는 의존하는 구조가 일반적임
+ Mocking을 기본적으로 사용해서 테스트 한다.

### Moking이란?
+ 실제 값으로 테스트를 하기 어렵기 때문에 가짜 오브젝트를 생성해서 가짜 값을 사용할 수 있게 해주는 것

### Moking이 필요한 이유
+ 유닛테스트는 하나의 기능을 테스트하는 것을 목적으로 함
+ 테스트의 속도를 위해.
  + 서비스 레이어에 필요한 Bean만 가져오기 위해 Mockito 라이브러리를 활용 -> 모두 Mock 객체를 가짐
+ 의존성 문제
  + 실제 객체로 테스트를 하면 다른 의존성을 배제하기 쉽지 않음
+ 외부 API가 정상적 작동을 하지 않을 때
  + 내가 테스트 코드를 작성할 때는 외부 API가 응답을 잘 했찌만, CI에서 테스트할 때는 외부 API를 제공하는 서비스가 뻗어서 테스트의 결과가 엉킬 수 있음 -> 외부 API에 너무 의존적이므로 Mocking 해줘야 함.
+ 외부 API 호출에 비용이 발생하는 경우

### ArticleService 테스트 코드 작성
+ Mocking을 통해 ArticleService 객체가 가지는 의존성 제거
```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    private ArticleService articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Test
    public void findById_id_에_맞는_Article_객체를_가져온다() {
        //given
        Long idStub = 1L;
        Optional<Article> articleOptionStub = Optional.of(Article.builder()
                .id(idStub).title("title").content("content").build());

        when(articleRepository.findById(idStub)).thenReturn(articleOptionStub);

        //when
        Article result = articleService.findById(idStub);

        //then
        assertThat(result.getId()).isEqualTo(articleOptionStub.get().getId());

    }
}
```
## 예외 테스트
```java
//ArticleServiceTest.java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
  // ...
  @Test
  public void findById_id_에_맞는_Article_객체가_없다면_DATA_IS_NOT_FOUND_오류발생() {
    // given
    Long idStub = 1L;

    // when
    when(articleRepository.findById(idStub)).thenReturn(Optional.empty());

    // then
    assertThatThrownBy(() -> articleService.findById(idStub))
            .isInstanceOf(ApiException.class)
            .hasMessage("article is not found");
  }
}
```

## 테스트 커버리지
+ 어느 정도로 작성하는가?
+ 회사마다 다르지만 테스트 코드를 많이 작성한 회사는 참고할 문서가 많다는 것을 의미
+ 시스템에 테스트 코드가 많다면 안정적인 서비스 제공 가능
+ 그러나, 테스트 코드를 작성하는 것 자체가 시간이 소요되는 일이다.

## AnyMatcher 활용하기 (자세한건 추후 추가 예정)
+ 고정적인 stub 객체를 생성하는 것이 싫다면 any 함수 사용 가능
+ 사용하려는 모든 값을 any로 바꿔야 함
```
Long idStub = anyLong();
```
+ 다음과 같이 활용
```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
  // ...
  @Test
  public void findById_id_에_맞는_Article_객체를_가져온다() {
    //given
    Long idStub = anyLong();
    Optional<Article> articleOptionStub = Optional.of(Article.builder()
            .id(eq(idStub)).title(anyString()).content(anyString()).build());

    when(articleRepository.findById(eq(idStub))).thenReturn(articleOptionStub);

    //when
    Article result = articleService.findById(idStub);

    //then
    assertThat(result.getId()).isEqualTo(articleOptionStub.get().getId());

  }
}
```

## Repository 유닛 테스트
+ DB와 관련된 영역을 테스트 코드 작성하는 것은 사실상 어려움
+ DB를 update 하는 함수를 테스트한다고 했을 때 실제 데이터가 수정된다면??
  + RollBack 처리의 문제 발생
+ 변경된 DB의 내용이 다른 테스트 코드에 영향을 준다면??
  + 테스트 코드 중 DB에 아무것도 없을 때 조회하면 오류를 반환해야 정상인 케이스 존재
  + 만약 다른 테스트 코드에서 그 DB에 INSERT를 했다면?

+ @DataJdbcTest
  + h2 데이터베이스를 자동적으로 하나의 데이터 베이스 벤더로 적용
+ Repository는 의존성을 가지지 않음
  + Mock을 사용하지 않고 실제 객체를 사용하기 위해 Bean을 가져오는 SpringExtension.class를 사용
    + Reposiroty에 해당하는 Bean만 가져옴
+ 테스트 코드에서는 순환참조에 대해 생각하지 않고 필드 주입으로 작성함

### 테스트 코드 작성을 위한 간단한 쿼리 메소드
```java
public interface ArticleRepository extends CrudRepository<Article, Long> {
    List<Article> findByTitle(String title);
}
```

###Repository 유닛 테스트 코드 작성
```java
@ExtendWith(SpringExtension.class)
@DataJpaTest
class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void findByTitle_title_조회하면_일치하는_Article_객체들이_반환된다() {
        // given
        String javaStub = "Java";
        String pythonStub = "Python";

        articleRepository.save(Article.builder().title(javaStub).build());
        articleRepository.save(Article.builder().title(javaStub).build());
        articleRepository.save(Article.builder().title(pythonStub).build());

        // when
        List<Article> articles = articleRepository.findByTitle(javaStub);

        // then
        assertThat(articles.size()).isEqualTo(2);
        assertTrue(articles.stream().allMatch(a -> javaStub.equals(a.getTitle())), "title 필드 값이 모두 " + javaStub + " 인가?");
    }
}
```

## Controller 유닛 테스트
+ @WebMvcTest(ArticleController.class)를 사용하여 Controller와 관련된 Bean만 가져옴
```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
 //...
}
```
+ Controller는 Repository와 달리 Service에 대한 의존성 존재
  + Service에 대한 의존성이 있어 가짜객체를 사용해야 하지만, Controller는 실제 객체를 사용하고 Service만 가짜를 사용하고싶다면?
    + @MockBean을 사용하여 객체를 스프링 컨테이너에 등록

```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
}
```
+ Controlller 함수는 API 진입점으로 활용됨 -> API를 호출할 수 있는 클라이언트 MockMvc를 추가
```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;
}
```
### MockMvc를 활용해 테스트코드 구현
```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Response.ok() 함수 호출시 code 값이 ApiCode.SUCCESS 값을 가져야 한다.")
	// 한글로 만들기 싫을 때 사용할 수 있음.
    public void get_whenYouCallOk_ThenReturnSuccessCode() throws Exception {
        // given
        Long idStub = 1L;
        when(articleService.findById(idStub)).thenReturn(Article.builder().id(idStub).build());

        // when, then
        mvc.perform(get("/api/v1/article/" + idStub)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print()) // print를 사용하지 않는다면 내용 출력이 안됨.
                .andExpect(status().isOk());
    }
}
```

+ 실제 값을 받고싶다면?
```java
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Response.ok() 함수 호출시 code 값이 ApiCode.SUCCESS 값을 가져야 한다.")
    public void get_whenYouCallOk_ThenReturnSuccessCode() throws Exception {
        // given
        Long idStub = 1L;
        when(articleService.findById(idStub)).thenReturn(Article.builder().id(idStub).build());

        // when
        String response = mvc.perform(get("/api/v1/article/" + idStub)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        assertThat(ApiCode.SUCCESS.getName()).isSubstringOf(response);
    }
}
```
