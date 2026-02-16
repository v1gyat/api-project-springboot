package com.example.apiproject.service.strategy;

import com.example.apiproject.entity.AssignmentType;
import com.example.apiproject.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for dynamically selecting task assignment strategies
 * Spring auto-injects all TaskAssignmentStrategy implementations
 * Maps each strategy by its AssignmentType for O(1) lookup
 * 
 * Adding a new strategy:
 * 1. Create new enum value in AssignmentType
 * 2. Create new class implementing TaskAssignmentStrategy
 * 3. Annotate with @Component
 * 4. No changes needed here!
 */
@Component
public class TaskAssignmentStrategyFactory {

    private final Map<AssignmentType, TaskAssignmentStrategy> strategyMap;

    /**
     * Constructor receives all TaskAssignmentStrategy beans from Spring
     * Builds a map for fast strategy lookup by type
     * 
     * @param strategies List of all TaskAssignmentStrategy implementations
     */
    public TaskAssignmentStrategyFactory(List<TaskAssignmentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        TaskAssignmentStrategy::getType,
                        Function.identity()));
    }

    /**
     * Get the appropriate strategy for the given assignment type
     * 
     * @param type The assignment type requested
     * @return The corresponding strategy implementation
     * @throws BadRequestException if type is null or not supported
     */
    public TaskAssignmentStrategy getStrategy(AssignmentType type) {
        if (type == null) {
            throw new BadRequestException("Assignment type cannot be null");
        }

        TaskAssignmentStrategy strategy = strategyMap.get(type);

        if (strategy == null) {
            throw new BadRequestException("Invalid or unsupported assignment type: " + type);
        }

        return strategy;
    }
}
