## Part 1

Spring Batch로 Hello Spring Batch 로그 찍어보기<br>
https://me2.kr/PLdRv

### 배치란?

- 큰 단위의 작업을 일괄 처리
- 대부분 처리랑이 많고 비 실시간성에 사용
  - 대용량 데이터 계산, 정산, 통계, 변환 등
- 컴퓨터 자원을 최대로 사용하기 위해 남는 시간대에 사용

### 스프링 배치란?

- 배치 처리를 하기 위한 Spring Framework 기반 기술
- 스프링 배치의 실행단위: Job, Step
- 비교적 간단한 작업 `Tasklet`, 대량 묶음 `Chunk`


## Part 2

Spring Batch의 기본 구조 <br>
https://me2.kr/IIIiI

### Job
- Job은 JobLauncher에 의해 실행
- Job은 배치의 실행 단위
- Job은 N개의 Step을 실행할 수 있으며 흐름을 관리할 수 있다.
### Step
- Step은 Job의 세부 실행 단위이고 N개가 등록 되어 실행
- Step
  - Chunk
  - Task
### Spring Batch 테이블 구조와 이해

이미지 삽입


## Part3

Spring Batch의 ExecutionContext 데이터 공유 <br>
https://me2.kr/CSwtI

```java
// StepExecutionContext에 공유 데이터 저장
stepExecutionContext.putString("stepKey", "Step context data");
...
// JobExecutionContext에 공유 데이터 저장
jobExecutionContext.putString("jobKey", "Job context data!!");
```

StepExecutionContext은 같은 Step에서만 값이 공유가 되고 JobExecutionContext는 Job에 포함된 모든 Step에서 접근 가능하다.

## Part4

Spring Batch Task와 Chunk <br>
https://me2.kr/Zlurg

- Task와 Chunk의 차이는 작업을 n개로 나누어서 처리할 수 있는지이다.
- Chunk는 3단계로 처리한다.
    1. Reader: 데이터를 읽어온다.
    2. Processor: 데이터를 가공, 처리한다.
    3. Writer: 데이터를 데이터 베이스에 저장 등 이후 처리를 한다.


> 💡 @JobScope, @StepScope 심화
> 
> - @Scope는 어떤 시점에 bean을 생성/소멸 시킬지 bean의 lifecycle을 설정하는 것을 말한다.
> - @JobScope는 job을 실행할 때 생성되고 종료될 때 소멸한다.
>     - Step에 선언 (Step을 Bean으로 등록해주어야한다.)
> - @StepScope는 step을 실행할 때 생성되고 종료될 때 소멸한다.
>    - Tasklet, Chunk(ItemReader, ItemProcessor, ItemWriter)에 선언
>      (Tasklet, Chunk을 Bean으로 등록해주어야한다.)
> 
> 한마디로 @JobScope를 붙인 Step은 Job의 lifecycle과 같아지고 @StepScope를 붙인 Task 혹은 Chunk는 Step과 lifecycle이 같아진다.
> 
> 이렇게 실행시점에 생성이 되기때문에 Thread safe하게 작동하게 된다.


## Part5

Spring Batch Jpa Chunk Reader & Writer <br>
https://me2.kr/arobm

|  | Cursor | Paging                                       |
| --- | --- |----------------------------------------------|
| 연결 | 배치 처리가 완료될 때까지 | 페이징 단위로 연결                                   |
| 장점 | Connection 연결 빈도가 낮음 | 적은 메모리<br>짧은 Connection 유지 시간<br>Thread Safe |
| 단점 | 많은 메모리<br>긴 Connection 유지 시간<br>Thread Safe 하지 않음 | DB Connection 빈도가 높음 |


## Part6

Spring Batch skip, retry 예외처리 <br>
https://me2.kr/IgUZg
### Skip
Exception이 발생했을 때 Skip을 통해 Step을 넘길 수 있다.

- `faultTolerant()` skip을 사용하기 위해 먼저 호출한다.
- `skip()` Excetion 클래스를 주입할 수 있다.
- `skipLimit()` skip할 수 있는 최대 횟수를 설정한다.

### Retry
Retry는 Step 수행 중에 간헐적으로 Exception이 발생할 경우 재시도를 하게 된다. 예를 들어 DB Deadlock이나 Network timeout이 있을 것이다.

- `faultTolerant()` skip을 사용하기 위해 먼저 호출한다.
- `retry()` Excetion 클래스를 주입할 수 있다.
- `retryLimit()` retry할 수 있는 최대 횟수를 설정한다.
- 재시도를 한 후에 Processor에 RetryTemplate를 통해서 이후 처리를 할 수 있다.


## Project (진행 중)
- 유저 저장: Tasklet

- 유저 등급 변경: Chunk
  - JpaPaginItemReader
  - ItemProcessor
  - ItemWriter

- 일별 주문 금액: Chunk
  - JdbcParingItemReader
  - FlatFileItemWriter(csv)

- ExecutionListener, JobExecutionDecide