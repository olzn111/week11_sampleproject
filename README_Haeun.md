# Week 11 — Attachment Board (Student Sample)
22500199 김하은

Project Structure & Spring MVC
1. PostController, PostService, PostRepository는 각각 어떤 역할을 하며, 서로 어떻게 연결되는가?
   -> PostController.java, PostService.java, PostRepository.java
   -> PostController는 클라이언트의 HTTP 요청을 받으면 응답뷰를 결정하고 , PostService는 CRUD나 파일 처리를 하고, PostRepository는 JPA를 통해 마리아디비와 직접 통신하여 데이터를 영속화한다.
   -> PostController → PostService → PostRepository 방향

2. 게시글 작성 시 HTML Form의 입력값과 첨부파일은 Controller에 어떻게 전달되는가?
   -> PostController.java - create(), posts/form.html
   -> HTML<form> 태그에 enctype="multipart/form-data" 에 텍스트 데이터와 바이너리 파일 데이터가 멀티 부분의 HTTP 요청으로 전송된다.

   
JPA & Entity Relationship
1. Post와 Attachment Entity는 어떤 관계이며, @ManyToOne과 @OneToMany는 각각 어떤 역할을 하는가?
    -> Post.java, Attachment.java
    -> post와 Attachment Entity는 하나의 게시글에 여러 개의 첨부파일을 가질 수 있는 관계이다. 
    -> @ManyToOne은 실제 DB 테이블의 post_id를 관리하는 역할을 하고, OneToMany(mappedBy = "post")는 게시글로 봤을 때 첨부파일 목록을 조회하기 위한 읽기 전용 매핑이다.
2. cascade = CascadeType.ALL과 orphanRemoval = true는 어떤 역할을 하며, 실제 디스크에 저장된 파일에도 영향을 주는가?
    -> Post.java (attachments 필드), PostService.java - delete()
    -> cascade = CascadeType.ALL은 부모 post의 저장이나 삭제 작업이 자식Attachment 엔티티로 전달되도록 하고,orphanRemoval = true는 부모리스트에서 없어진 엔티티를 데이터베이스에서 자동으로 삭제한다.

File Upload
1. MultipartFile은 무엇이며, 업로드된 파일은 어떤 과정을 거쳐 서버에 저장되는가?
    -> PostController.java - create(), FileStorageService.java - store()
    -> MultipartFile은 HTTP 멀티파트로 업로드 된 파일의 이름이나 크기 등을 다룰 수 있는 인터페이스다.
    -> FileStorageService.store()에서 빈 파일 -> file.getInputStream()과 Files.copy()를 이용해서 지정된 로컬 장소에 파일을 기록한 후에 Attachment 객체 반환하기 
2. 파일을 원본 이름 대신 UUID 기반 파일명으로 저장하는 이유는 무엇인가?
    -> FileStorageService.java - store()
    -> 다른 사용자가 동일한 이름을 파일을 업로드 하면 기존 파일에 다음 파일이 씌워지는 문제가 생길 수 있기 때문이다.

File Download & Security
1. 파일 다운로드 시 Resource, ResponseEntity, Content-Disposition은 각각 어떤 역할을 하는가?
    -> AttachmentController.java - download(), FileStorageService.java - load()
    ->Resource는 저장된 실제 파일을 읽어 HTTP 응답으로 할 수 있게하는 객체이고, ResponseEntity, Content-Disposition는 HTTP 상태 코드(200 OK), 헤더, 파일 본문(Resource)을 직접 구성해 반환한다.
2. 파일 저장 과정에서 normalize()와 startsWith()를 사용하는 이유는 무엇인가?
    -> FileStorageService.java - store(), load()
    -> 파일을 덮어쓰는 것을 방지할 수 있고 . 이나 ..같은 상대 경로를 최종 경로가 지정된 업로드 루트 폴더 내부에 있는지 알아본 후 검증에서 벗어나면 예외를 발생시킨다.

관련 소스파일 및 메서드명을 함께 명시
각 문항은 2~4문장 정도로 간단히 설명