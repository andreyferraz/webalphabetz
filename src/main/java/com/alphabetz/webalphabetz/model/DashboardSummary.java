package com.alphabetz.webalphabetz.model;

public record DashboardSummary(
        long totalSlides,
        long totalPhotos,
        long totalPosts,
        long totalCategories,
        long totalApplications,
        long totalAdmins,
        String todayLabel) {
}
