package com.example.communityforum.events;

public record EmailVerificationRequested(String to, String subject, String body) {}