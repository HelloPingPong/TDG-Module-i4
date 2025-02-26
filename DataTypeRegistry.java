package com.example.tdg.service.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Registry for all data generators.
 * Provides methods to lookup generators by type.
 */
@Component
public class DataTypeRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(DataTypeRegistry.class);
    
    private final Map<String, DataGenerator> generators = new HashMap<>();
    private final Map<String, Map<String, DataGenerator>> categorizedGenerators = new TreeMap<>();
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * Initializes the registry by scanning for all DataGenerator beans.
     */
    @PostConstruct
    public void init() {
        Map<String, Object> generatorBeans = applicationContext.getBeansWithAnnotation(DataGeneratorType.class);
        
        for (Object bean : generatorBeans.values()) {
            if (bean instanceof DataGenerator) {
                DataGenerator generator = (DataGenerator) bean;
                registerGenerator(generator);
            }
        }
        
        logger.info("Registered {} data generators", generators.size());
    }
    
    /**
     * Manually register a generator.
     * 
     * @param generator The generator to register
     */
    public void registerGenerator(DataGenerator generator) {
        String type = generator.getType();
        generators.put(type, generator);
        
        // Also organize by category
        Class<?> generatorClass = generator.getClass();
        if (generatorClass.isAnnotationPresent(DataGeneratorType.class)) {
            DataGeneratorType annotation = generatorClass.getAnnotation(DataGeneratorType.class);
            String category = annotation.category();
            
            categorizedGenerators.computeIfAbsent(category, k -> new TreeMap<>())
                    .put(type, generator);
        }
        
        logger.debug("Registered generator: {}", type);
    }
    
    /**
     * Get a generator by type.
     * 
     * @param type The generator type
     * @return Optional containing the generator if found
     */
    public Optional<DataGenerator> getGenerator(String type) {
        return Optional.ofNullable(generators.get(type));
    }
    
    /**
     * Get all registered generators.
     * 
     * @return Map of type to generator
     */
    public Map<String, DataGenerator> getAllGenerators() {
        return new HashMap<>(generators);
    }
    
    /**
     * Get generators organized by category.
     * 
     * @return Map of category to map of type to generator
     */
    public Map<String, Map<String, DataGenerator>> getCategorizedGenerators() {
        return new HashMap<>(categorizedGenerators);
    }
}
