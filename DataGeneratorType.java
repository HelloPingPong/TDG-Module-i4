package com.example.tdg.service.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark classes as data generators.
 * This enables automatic discovery and registration of generator types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataGeneratorType {
    
    /**
     * The category this generator belongs to (e.g., "Personal", "Address", "Finance").
     * Used for organizing generators in the UI.
     */
    String category() default "General";
    
    /**
     * Human-readable name for this generator.
     * If not provided, the generator type will be used.
     */
    String displayName() default "";
    
    /**
     * Brief description of what this generator produces.
     */
    String description() default "";
}
