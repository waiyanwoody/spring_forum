package com.example.communityforum.service;

import com.example.communityforum.dto.post.*;
import com.example.communityforum.dto.user.UserSummaryDTO;
import com.example.communityforum.exception.PermissionDeniedException;
import com.example.communityforum.exception.ResourceNotFoundException;
import com.example.communityforum.mapper.PostMapper;
import com.example.communityforum.persistence.entity.Post;
import com.example.communityforum.persistence.entity.Tag;
import com.example.communityforum.persistence.entity.User;
import com.example.communityforum.persistence.repository.*;
import com.example.communityforum.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final SecurityUtils securityUtils;
    private final LikeRepository  likeRepository;
    private final TagRepository tagRepository;
    private final PostMapper  postMapper;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    public Page<PostListResponseDTO> getAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAll(pageable);

        User user = securityUtils.getCurrentUser();
        List<Long> postIds = postPage.getContent()
                .stream()
                .map(Post::getId)
                .toList();
        Map<Long, Long> likeCountMap = getLikeCountMap(postIds);
        Map<Long, Long> commentCountMap = getCommentCountMap(postIds);

        return postPage.map(post -> postMapper.toListDTO(post, user, likeCountMap,commentCountMap));

    }

    public PostDetailResponseDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow( () -> new ResourceNotFoundException("Post",id));

        User user = securityUtils.getCurrentUser();

       return postMapper.toDetailDTO(post,user);
    }

    // get like count for post
    private Map<Long, Long> getLikeCountMap(List<Long> postIds) {
        List<Object[]> counts = likeRepository.countLikesByPostIds(postIds);
        return counts.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
        ));
    }

    // get comment count for post
    private Map<Long, Long> getCommentCountMap(List<Long> postIds) {
        List<Object[]> counts = commentRepository.countCommentsByPostIds(postIds);
        return counts.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
        ));
    }

    // get all posts by user id
    public UserPostsResponseDTO getPostsByUserId(Long userId, int page, int pageSize) {
        // 1️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Pagination
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findAllByUser_Id(userId, pageable);

        // 3️⃣ Fetch like counts and comment counts in bulk
        List<Long> postIds = postPage.getContent()
                .stream()
                .map(Post::getId)
                .toList();
        Map<Long, Long> likeCountMap = getLikeCountMap(postIds);
        Map<Long, Long> commentCountMap = getCommentCountMap(postIds);

        // 4️⃣ Map posts using helper function
        List<PostSummaryDTO> postDTOs = postPage.getContent().stream()
                .map(post -> postMapper.mapToPostSummaryDTO(post, likeCountMap,commentCountMap))
                .toList();

        // 5️⃣ Author info
        UserSummaryDTO authorDTO = UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar_path(user.getAvatarPath())
                .build();

        // 6️⃣ Return response
        return UserPostsResponseDTO.builder()
                .author(authorDTO)
                .posts(postDTOs)
                .page(page)
                .pageSize(pageSize)
                .totalPosts(postPage.getTotalElements())
                .build();
    }

    public PostDetailResponseDTO addPost(PostRequestDTO request) {
        User currentUser = securityUtils.getCurrentUser();
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        //ensure post has owner
        post.setUser(currentUser);
        post.setCreatedAt(LocalDateTime.now());

        post.setTags(processTags(request.getTags()));

        postRepository.save(post);
        return postMapper.toDetailDTO(post, currentUser);
    }
    
    private Set<Tag> processTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>(); // return an empty set instead of null
        }
        return tagNames.stream()
                .filter(tagName -> tagName != null && !tagName.trim().isEmpty())
                .map(tagName -> tagRepository.findByNameIgnoreCase(tagName.trim())
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName.trim()).build())))
                .collect(Collectors.toSet());
    }

    // update post
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    public PostDetailResponseDTO updatePost(long id, PostRequestDTO request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));

        // security check
        securityUtils.checkOwnerOrAdmin(existingPost);

        // update only allowed fields
        existingPost.setTitle(request.getTitle());
        existingPost.setContent(request.getContent());

        postRepository.save(existingPost);

        return postMapper.toDetailDTO(existingPost,securityUtils.getCurrentUser());
    }


    //soft delete post for admin or owner
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void softDeletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        User currentUser = securityUtils.getCurrentUser();

        // security check
        securityUtils.checkOwnerOrAdmin(post);

        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(currentUser);
        postRepository.save(post);
    }

    // restore soft-deleted post
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public PostDetailResponseDTO restorePost(Long postId) {

        Post post = postRepository.findDeletedById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Soft-deleted Post", postId));

        User currentUser = securityUtils.getCurrentUser();

        // security check
        securityUtils.checkOwnerOrAdmin(post);

        post.setDeletedAt(null);
        post.setDeletedBy(null);
        postRepository.save(post);

        return postMapper.toDetailDTO(post,currentUser);
    }

    // hard delete post
//    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwner(@postRepository.findById(#postId).orElse(null))")
    @Transactional
    public void hardDeletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        // security check
        securityUtils.checkOwnerOrAdmin(post);

        // permanent removal
        postRepository.deleteById(postId);
    }
}
