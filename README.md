# JPA_Programming
자바 ORM 표준 JPA 프로그래밍

1. SQL 중심적인 개발의 문제점
2. 패러다임의 불일치 (객체와 디비의 불일치)
    - 관계형 디비는 데이터를 잘 정규화해서 보관하는게 목표
    - 객체는 속성과 기능을 잘 캡슐화해서 사용하는게 목표
      객체를 SQL로 결국엔 변환해서 코드를 짜야된다.

# JPA
- Java Persistence API (자바 진영의 ORM 기술 표준)
- ORM?
    - Object-relational mapping(객체 관계 매핑)
    - 객체는 객체대로 설계하고 관계형 디비는 관계형 디비대로 설계
- 인터페이스의 모음


## JPA 동작 - 저장
<img width="913" alt="스크린샷 2021-10-10 오후 2 17 08" src="https://user-images.githubusercontent.com/72979429/136683283-1f6e0d9d-ece3-4f0b-8f31-3860bcc5c8ab.png">
- JPA가 멤버 객체를 분석해 적절한 SQL 쿼리를 생성 후 JDBC API를 생성해서 Insert

## JPA를 왜 사용해야 하는가?
- 유지보수 : 기존코드의 필드 변경시 모든 SQL을 수정해야한다.
- 패러다임의 불일치 해결
- 동일한 트랙잭션에서 조회한 엔티티는 같음을 보장

## 영속성 컨텍스트 (EntityManager)
- 영속성 컨텍스트? 
  - 엔티티를 영구 저장하는 환경, 애플리케이션과 데이터베이스 사이에서 객체를 보관하는 가상의 데이터 베이스 같은 역할을 한다.

![jpa_persistence.png](./img/jpa_persistence.png)
- EntityManagerFactory에서 고객의 요청이 올때마다 EntityManager를 생성
- EntityManager는 내부적으로 데이터 베이스 커넥션을 사용해서 디비를 사용. EntityManager를 통해서 영속성 컨텍스트에 접근

![jpa_persistence.png](./img/jpa_persistence2.png)
- J2EE, 스프링 프레임워크와 같은 컨테이너 환경에서 EntityManager와 영속성 컨텍스트가 N:1로 매핑

### 엔티티의 생명주기
1. 비영속 (new/transient)
   - 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
``` java
// 단순히 객체를 생성한 상태(비영속),
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");
```

2. 영속 (managed)
    - 영속성 컨텍스트에 관리되는 상태
``` java
// 단순히 객체를 생성한 상태(비영속),
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");

// EntityManager 얻어오기
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();

// EntityManager안에 있는 영속성 컨텍스트에 Member 객체가 들어가면서 영속 상태가 된다.
// 객체를 저장한 상태(영속), EntityManager를 사용해 Member를 저장
// 영속상태가 된다고 해서 바로 db 쿼리가 날라가는게 아님
em.persist(member);
```

3. 준영속(detached)
    - 영속성 컨텍스트에 저장되었다가 분리된 상태

4. 삭제(removed)
    - 삭제된 상태

### 영속성 컨텍스트 상세 동작
![select_cache.png](./img/select_cache.png)
- em.find() 호출시 1차 캐시를 먼저 탐색

![select_db.png](./img/select_db.png)
- 1차 캐시에 데이터가 없을 경우 db에서 조회 후 1차 캐시에 저장, 그 이후 반환
- **영속성 컨텍스트는 트랜잭션 단위가 끝날때 같이 종료된다, 즉 고객의 요청이 들어와서 비즈니스 로직이 끝나면 영속 컨텍스트를 지우기 때문에 1차 캐시 데이터도 날라감.
그렇기 때문에 여러명의 고객에게 사용되는 캐시는 아님**
  
### Flush
- 영속성 컨텍스트의 변경내용을 데이터베이스에 반영
- 데이터베이스 commit이 발생하면 자동으로 Flush가 발생


# 엔티티 매핑
## 객체와 테이블 매핑
### @Entity
- @Entity가 붙은 클래스는 JPA가 관리
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수
- **기본 생성자 필수**

### @Table
- 엔티티와 매핑할 데이터 베이스 테이블 지정

## 데이터베이스 스키마 자동 생성
- DDL을 애플리케이션 실행 시점에 자동 생성
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
- 단, 생성된 DDL은 Real 서버에서는 사용하지 않거나, 적절히 다듬은 후 사용하는 것을 권장
  
## 속성
- create : 기존 테이블 삭제 후 다시 생성
  ![create_table_log.png](./img/create_table_log.png)
- create-drop : create와 같으나 종료시점에 테이블 DROP
- update : 변경분만 반영 (운영 DB에는 사용 X) (지우는건 안된다. 추가만 가능)
- validate : 엔티티와 테이블이 정상 매핑되었는지만 확인
- none : 사용하지 않음
**운영 장비에는 절대 create, create-drop, update 사용하면 안된다.**

## DDL 생성 기능
### @Column(nullable = false, length = 10)
- DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다.
- nullable(DDL) : null값의 허용 여부를 설정. false로 설정하면 DDL 생성시에 not null 제약조건이 붙는다.
- unique(DDL) : @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용 (로그에 이상한 값으로 찍혀서 사용은 비추...)
 
### 기본키 맵핑 방법
@GeneratedValue
- 값을 자동 할당하는 방법
  - AUTO(default) : db 방언에 따라 자동지정
  - IDENTITY : 기본키 생성을 데이터베이스에 위임 (DB에 따라 기본 생성이 달라짐)
  - SEQUENCE : 주로 oracle db에서 많이 사용
    
## 객체 연관관계
### mappedBy
- 객체와 테이블간의 관계를 맺는 차이를 이해 
- 객체에는 연관관계가 되는 키포인트가 2가지가 있다. (단방향 연관관계가 2개 있는것)
    - Member에서 Team으로 가는 연관관계 (단방향)
    - Team에서 Member로 가는 연관관계 (단방향)
    - 즉 객체는 서로 참조값을 넣어줘야 한다.
- 테이블의 연관관계는 키포인트는 1가지
    - 왜래키 값으로 조인을 하면 양쪽의 데이터를 알 수 있다. 즉, 외래키 값 하나로 모든 연관관계를 알 수 있다.
      ![relation_table.png](./img/relation_table.png)
- **객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단방향 관계 2개다.**

- **양방향 매칭 규칙**
    - 객체의 두 관계중 하나를 연관관계의 주인으로 지정
    - **연관관계의 주인만이 외래 키를 관리(등록, 수정)**
    - **주인이 아닌쪽은 읽기만 가능**
    - 주인은 mappedBy 속성 사용 X
    - 주인이 아니면 mappedBy속성으로 주인 지정
    
- 그러면 누구를 주인으로??
    - 외래키가 있는곳을 주인으로 정하자
