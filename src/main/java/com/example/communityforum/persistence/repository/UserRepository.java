package com.example.communityforum.persistence.repository;

import com.example.communityforum.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long>
{

}