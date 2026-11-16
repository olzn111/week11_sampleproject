package edu.handong.web11.controller;

import edu.handong.web11.domain.Post;
import edu.handong.web11.dto.PostForm;
import edu.handong.web11.service.PostService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class PostController {
    private final PostService posts;

    public PostController(PostService posts) {
        this.posts = posts;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/posts";
    }

    @GetMapping("/posts")
    public String list(Model model) {
        model.addAttribute("posts", posts.all());
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String form(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }
/* /posts 요청을 받아서 폼 데이터와 첨부파일 리스트 files를 전달받는다.*/
    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm form, BindingResult errors,
                         @RequestParam(name = "files", required = false) List<MultipartFile> files) {
       //내용이나 제목 유효성 검사에서 실패하면 저장하지 않고 다시 작성으로 돌아간다.
        if (errors.hasErrors()) return "posts/form";
        // 파일 리스트가 null이면 빈 리스트로 넘어가 PostService에서 게시글과 첨부파일을 DB에 저장하고 생성된 게시글 id를 받는다.
        Long id = posts.create(form, files == null ? List.of() : files);
       //새로고침 시 중복 등록을 막기 위해 방금 만든 게시글의 상세 페이지로 리다이렉트한다.
        return "redirect:/posts/" + id;
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", posts.get(id));
        return "posts/detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Post post = posts.get(id);
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());
        model.addAttribute("postForm", form);
        model.addAttribute("postId", id);
        return "posts/form";
    }

    @PostMapping("/posts/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("postForm") PostForm form, BindingResult errors,
                         Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("postId", id);
            return "posts/form";
        }
        posts.update(id, form);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        posts.delete(id);
        return "redirect:/posts";
    }
}
