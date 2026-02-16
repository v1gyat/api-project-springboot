package com.example.apiproject.entity;

/**
 * Enum representing different task assignment strategies
 * Add new types here when adding new assignment strategies
 */
public enum AssignmentType {
    /**
     * Manual assignment - requires userId
     * Manager explicitly chooses which user to assign
     */
    MANUAL,

    /**
     * Random assignment - userId not required
     * System randomly picks an available user
     */
    RANDOM,

    /**
     * Least loaded assignment - userId not required
     * System assigns to user with fewest active tasks
     */
    LEAST_LOADED
}
